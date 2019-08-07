package com.tencent.liteav.demo.videoediter;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.liteav.demo.videoediter.common.utils.FileUtils;
import com.tencent.liteav.demo.videoediter.common.utils.TCConstants;
import com.tencent.liteav.demo.videoediter.common.widget.progress.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videoediter.common.utils.DialogUtil;
import com.tencent.liteav.demo.videoediter.common.TCConfirmDialog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by hans on 2017/11/7.
 * 对进入编辑的视频进行一步预处理的Activity
 */
public class TCVideoPreprocessActivity extends FragmentActivity implements
        View.OnClickListener,
        TXVideoEditer.TXVideoProcessListener {
    private static final String TAG = "TCVideoPreActivity";
    private String mInVideoPath;                                // 编辑的视频源路径
    private String mCoverPath;
    private TXVideoEditer mTXVideoEditer;                       // SDK接口类
    private VideoWorkProgressFragment mWorkProgressFragment;    // 生成进度条
    private VideoMainHandler mVideoMainHandler;                 // 加载完信息后的回调主线程Hanlder
    private Thread mLoadBackgroundThread;                       // 后台加载视频信息的线程
    private int mVideoResolution = -1;                          // 视频分辨率相关（从录制过来的这个参数才有效） -1说明不是从录制过来的
    private boolean mGenerateSuccess;                           // 是否预处理成功
    private int mVideoFrom;
    private int mCustomBitrate;
    private static AtomicBoolean isCancel;
    private boolean mNeedProcessVideo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_processor);
        TCVideoEditerWrapper.getInstance().clear();

        mInVideoPath = getIntent().getStringExtra(TCConstants.VIDEO_EDITER_PATH);
        mCoverPath = getIntent().getStringExtra(TCConstants.VIDEO_RECORD_COVERPATH);
        mNeedProcessVideo = getIntent().getBooleanExtra(TCConstants.VIDEO_EDITER_IMPORT, true);
        if (TextUtils.isEmpty(mInVideoPath)) {
            Toast.makeText(this, "发生未知错误,路径不能为空", Toast.LENGTH_SHORT).show();
            finish();
        }
        mVideoResolution = getIntent().getIntExtra(TCConstants.VIDEO_RECORD_RESOLUTION, -1);
        mCustomBitrate = getIntent().getIntExtra(TCConstants.RECORD_CONFIG_BITE_RATE, 0);
        mVideoFrom = getIntent().getIntExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_EDIT);
        isCancel = new AtomicBoolean(false);

        mTXVideoEditer = new TXVideoEditer(this);
        int ret = mTXVideoEditer.setVideoPath(mInVideoPath);
        if (ret != 0) {
            if (ret == TXVideoEditConstants.ERR_SOURCE_NO_TRACK) {
                DialogUtil.showDialog(this, "视频预处理失败", "不支持的视频格式", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_AUDIO_FORMAT) {
                DialogUtil.showDialog(this, "视频预处理失败", "暂不支持非单双声道的视频格式", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            return;
        }

        TCVideoEditerWrapper wrapper = TCVideoEditerWrapper.getInstance();
        wrapper.setEditer(mTXVideoEditer);

        initPhoneListener();

        // 开始加载视频信息
        mVideoMainHandler = new VideoMainHandler(this);
        mLoadBackgroundThread = new Thread(new LoadVideoRunnable(this));
        mLoadBackgroundThread.start();
    }

    private void initWorkLoadingProgress() {
        if (mWorkProgressFragment == null) {
            mWorkProgressFragment = VideoWorkProgressFragment.newInstance("视频预处理中...");
            mWorkProgressFragment.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelProcessVideo();
                }
            });
        }
        mWorkProgressFragment.setProgress(0);
    }


    private void loadVideoSuccess() {
        startProcess();
    }

    private void loadVideoFail() {
        DialogUtil.showDialog(this, "编辑失败", "暂不支持Android 4.3以下的系统", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * ===========================================开始预处理相关 ===========================================
     */
    private void startProcess() {
        if (!mNeedProcessVideo) {
            startEditActivity();
        } else {
            initWorkLoadingProgress();
            mWorkProgressFragment.show(getSupportFragmentManager(), "work_progress");
            mWorkProgressFragment.setProgress(0);
            mTXVideoEditer.setVideoProcessListener(this);
            int thumbnailCount = (int) TCVideoEditerWrapper.getInstance().getTXVideoInfo().duration / 1000;

            TXVideoEditConstants.TXThumbnail thumbnail = new TXVideoEditConstants.TXThumbnail();
            thumbnail.count = thumbnailCount;
            thumbnail.width = 100;
            thumbnail.height = 100;

            mTXVideoEditer.setThumbnail(thumbnail);
            mTXVideoEditer.setThumbnailListener(mThumbnailListener);

            mTXVideoEditer.processVideo();
        }
    }

    private TXVideoEditer.TXThumbnailListener mThumbnailListener = new TXVideoEditer.TXThumbnailListener() {
        @Override

        public void onThumbnail(int index, long timeMs, Bitmap bitmap) {
            Log.i(TAG, "onThumbnail: index = " + index + ",timeMs:" + timeMs);
            TCVideoEditerWrapper.getInstance().addThumbnailBitmap(timeMs, bitmap);
        }
    };

    @Override
    public void onProcessProgress(final float progress) {
        mWorkProgressFragment.setProgress((int) (progress * 100));
    }

    @Override
    public void onProcessComplete(final TXVideoEditConstants.TXGenerateResult result) {
        if (mWorkProgressFragment != null && mWorkProgressFragment.isAdded()) {
            mWorkProgressFragment.dismiss();
        }
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            FileUtils.deleteFile(mCoverPath);
            startEditActivity();
            mGenerateSuccess = true;
        } else {
            TCConfirmDialog confirmDialog = TCConfirmDialog.newInstance("错误", result.descMsg, false, "取消", "取消");
            confirmDialog.setCancelable(false);
            confirmDialog.setOnConfirmCallback(new TCConfirmDialog.OnConfirmCallback() {
                @Override
                public void onSureCallback() {
                    finish();
                }

                @Override
                public void onCancelCallback() {
                }
            });
            confirmDialog.show(getSupportFragmentManager(), "confirm_dialog");
        }
    }

    private void startEditActivity() {
        // 更新一下VideoInfo的时间
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        // 如果是从录制过来的话，需要传递一个分辨率参数下去。
        intent.putExtra(TCConstants.VIDEO_RECORD_RESOLUTION, mVideoResolution);
        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, mVideoFrom);
        intent.putExtra(TCConstants.VIDEO_EDITER_PATH, mInVideoPath);
        intent.putExtra(TCConstants.RECORD_CONFIG_BITE_RATE, mCustomBitrate);
        intent.putExtra(TCConstants.VIDEO_EDITER_IMPORT, mNeedProcessVideo);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mLoadBackgroundThread != null && !mLoadBackgroundThread.isInterrupted() && mLoadBackgroundThread.isAlive()) {
            mLoadBackgroundThread.interrupt();
            mLoadBackgroundThread = null;
        }

        if (mWorkProgressFragment != null) {
            mWorkProgressFragment.setOnClickStopListener(null);
        }
        if (mTXVideoEditer != null) {
            // 注意释放，不然容易内存泄漏
            if (mNeedProcessVideo) {
                mTXVideoEditer.setThumbnailListener(null);
                mTXVideoEditer.setVideoProcessListener(null);
            }
        }

        mTXVideoEditer = null;
    }

    protected void onStop() {
        super.onStop();
        cancelProcessVideo();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.editer_back_ll) {
            finish();

        } else if (i == R.id.editer_tv_done) {
            startProcess();

        } else if (i == R.id.editer_ib_play) {
        }
    }


    /**
     * 取消预处理视频
     */
    private void cancelProcessVideo() {
        if (!mGenerateSuccess) {
            if (mWorkProgressFragment != null)
                mWorkProgressFragment.dismiss();
            if (mTXVideoEditer != null) {
                isCancel.set(true);
                if (mNeedProcessVideo) {
                    mTXVideoEditer.cancel();
                    //取消后，销毁资源，避免影响处理下一个视频
                    TCVideoEditerWrapper.getInstance().clear();
                    mTXVideoEditer.release();
                    Toast.makeText(TCVideoPreprocessActivity.this, "取消预处理", Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }
    }

    /**
     * ===========================================加载视频相关 ===========================================
     */

    /**
     * 加在视频信息的runnable
     */
    private static class LoadVideoRunnable implements Runnable {
        private WeakReference<TCVideoPreprocessActivity> mWekActivity;

        LoadVideoRunnable(TCVideoPreprocessActivity activity) {
            mWekActivity = new WeakReference<TCVideoPreprocessActivity>(activity);
        }

        @Override
        public void run() {
            if (mWekActivity == null || mWekActivity.get() == null) {
                return;
            }
            TCVideoPreprocessActivity activity = mWekActivity.get();
            if (activity == null) return;
            TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(activity.mInVideoPath);

            if (isCancel.get()) {
                return;
            }
            if (info == null) {// error 发生错误
                activity.mVideoMainHandler.sendEmptyMessage(VideoMainHandler.LOAD_VIDEO_ERROR);
            } else {
                TCVideoEditerWrapper.getInstance().setTXVideoInfo(info);
                activity.mVideoMainHandler.sendEmptyMessage(VideoMainHandler.LOAD_VIDEO_SUCCESS);
            }
        }
    }

    /**
     * 主线程的Handler 用于处理load 视频信息的完后的动作
     */
    private static class VideoMainHandler extends Handler {
        static final int LOAD_VIDEO_SUCCESS = 0;
        static final int LOAD_VIDEO_ERROR = -1;
        private WeakReference<TCVideoPreprocessActivity> mWefActivity;


        VideoMainHandler(TCVideoPreprocessActivity activity) {
            mWefActivity = new WeakReference<TCVideoPreprocessActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TCVideoPreprocessActivity activity = mWefActivity.get();
            if (activity == null) return;
            switch (msg.what) {
                case LOAD_VIDEO_ERROR:
                    activity.loadVideoFail();
                    break;
                case LOAD_VIDEO_SUCCESS:
                    activity.loadVideoSuccess();
                    break;
            }
        }
    }

    /*********************************************监听电话状态**************************************************/
    private void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private TXPhoneStateListener mPhoneListener;

    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TCVideoPreprocessActivity> mWefActivity;

        public TXPhoneStateListener(TCVideoPreprocessActivity activity) {
            mWefActivity = new WeakReference<TCVideoPreprocessActivity>(activity);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TCVideoPreprocessActivity activity = mWefActivity.get();
            if (activity == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    // 直接停止播放
                    activity.cancelProcessVideo();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }


}
