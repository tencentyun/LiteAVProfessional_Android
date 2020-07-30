package com.tencent.liteav.demo.videojoiner.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videojoiner.R;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.component.dialogfragment.VideoWorkProgressFragment;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;
import com.tencent.ugc.TXVideoJoiner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TCVideoJoinerPreviewActivity extends FragmentActivity implements View.OnClickListener, TXVideoJoiner.TXVideoPreviewListener, TXVideoJoiner.TXVideoJoinerListener {
    private static final String TAG = "TCVideoJoinerPreviewActivity";

    private final int MSG_SINGLE_VIDEO_INFO = 1000;
    private final int MSG_SINGLE_JOIN = 1002;

    private Context                   mContext;
    private TextView                  mButtonDone;
    private ImageButton               mImagePlay;
    private FrameLayout               mLayoutVideoView;
    private VideoWorkProgressFragment mFragmentWorkLoadingProgress;
    private TXVideoJoiner             mTXVideoJoiner;
    private TXVideoInfoReader         mVideoInfoReader;
    private TXPhoneStateListener      mPhoneListener;            // 电话监听
    private DialogUtil                mDialogUtil;
    private String                     mVideoOutputPath;
    private ArrayList<String>          mVideoSourceList;
    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;

    private TXVideoEditConstants.TXVideoInfo mVideoInfo;

    private BackGroundHandler mHandler;
    private HandlerThread     mHandlerThread;

    private int mRet = -1;
    private int mCurrentState = PlayState.STATE_NONE;       // 播放器当前状态

    private void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mLayoutVideoView;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoJoiner.initWithPreview(param);
    }

    public void startPlay() {
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoJoiner.startPlay();
            mCurrentState = PlayState.STATE_PLAY;
            mImagePlay.setImageResource(R.drawable.ugcjoin_ic_pause);
        }
    }

    public void resumePlay() {
        if (mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoJoiner.resumePlay();
            mCurrentState = PlayState.STATE_RESUME;
            mImagePlay.setImageResource(R.drawable.ugcjoin_ic_pause);
        }
    }

    public void pausePlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mTXVideoJoiner.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            mImagePlay.setImageResource(R.drawable.ugcjoin_ic_play);
        }
    }

    public void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY ||
                mCurrentState == PlayState.STATE_PREVIEW_AT_TIME || mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoJoiner.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            mImagePlay.setImageResource(R.drawable.ugcjoin_ic_play);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcjoin_activity_video_joiner_preview);

        initViews();
        initData();
        initPhoneListener();
        prepareVideoView();
    }

    private void prepareVideoView() {
        mRet = mTXVideoJoiner.setVideoPathList(mVideoSourceList);
        TXCLog.i(TAG, "prepareVideoView setVideoPathList mRet:" + mRet);
        if (mRet == TXVideoEditConstants.ERR_UNSUPPORT_VIDEO_FORMAT) {
            DialogUtil.showDialog(mContext, "视频合成失败", "本机型暂不支持此视频格式", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else if (mRet == TXVideoEditConstants.ERR_UNSUPPORT_AUDIO_FORMAT) {
            DialogUtil.showDialog(mContext, "视频合成失败", "暂不支持非单双声道的视频格式", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        initPlayerLayout();
    }

    private void initViews() {
        mButtonDone = (TextView) findViewById(R.id.btn_done);
        mButtonDone.setClickable(false);
        mImagePlay = (ImageButton) findViewById(R.id.btn_play);
        mLayoutVideoView = (FrameLayout) findViewById(R.id.video_view);

        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(this);
        mImagePlay.setOnClickListener(this);
        mButtonDone.setOnClickListener(this);
    }

    private void initData() {
        mContext = TCVideoJoinerPreviewActivity.this;
        mDialogUtil = new DialogUtil();

        mTCVideoFileInfoList = getIntent().getParcelableArrayListExtra(UGCKitConstants.INTENT_KEY_MULTI_CHOOSE);
        if (mTCVideoFileInfoList == null || mTCVideoFileInfoList.size() == 0) {
            finish();
            return;
        }
        mTXVideoJoiner = new TXVideoJoiner(this);
        mTXVideoJoiner.setTXVideoPreviewListener(this);

        mVideoInfoReader = TXVideoInfoReader.getInstance(this);

        mVideoSourceList = new ArrayList<>();
        for (int i = 0; i < mTCVideoFileInfoList.size(); i++) {
            if (Build.VERSION.SDK_INT >= 29) {
                mVideoSourceList.add(mTCVideoFileInfoList.get(i).getFileUri().toString());
            } else {
                mVideoSourceList.add(mTCVideoFileInfoList.get(i).getFilePath());
            }
        }

        mHandlerThread = new HandlerThread("LoadData");
        mHandlerThread.start();
        mHandler = new BackGroundHandler(mHandlerThread.getLooper());
    }

    private void initWorkLoadingProgress() {
        if (mFragmentWorkLoadingProgress == null) {
            mFragmentWorkLoadingProgress = new VideoWorkProgressFragment();
            mFragmentWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopGenerate();
                }
            });
        }
        mFragmentWorkLoadingProgress.setProgress(0);
    }

    private void createThumbFile(TXVideoEditConstants.TXVideoInfo videoInfo) {
        final TCVideoFileInfo fileInfo = mTCVideoFileInfoList.get(0);
        if (fileInfo == null) {
            return;
        }
        mButtonDone.setClickable(false);
        mImagePlay.setClickable(false);
        AsyncTask<TXVideoEditConstants.TXVideoInfo, String, Void> task = new AsyncTask<TXVideoEditConstants.TXVideoInfo, String, Void>() {

            @Override
            protected Void doInBackground(TXVideoEditConstants.TXVideoInfo... videoInfo) {
                saveThumbnail(videoInfo[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                startPreviewActivity();
                finish();
            }
        };
        task.execute(videoInfo);
    }

    private void saveThumbnail(TXVideoEditConstants.TXVideoInfo videoInfo) {
        final TCVideoFileInfo fileInfo = mTCVideoFileInfoList.get(0);
        String mediaFileName = fileInfo.getFileName();
        TXCLog.d(TAG, "fileName = " + mediaFileName);
        if (mediaFileName == null)
            mediaFileName = fileInfo.getFilePath().substring(fileInfo.getFilePath().lastIndexOf("/"), fileInfo.getFilePath().lastIndexOf("."));
        if (mediaFileName.lastIndexOf(".") != -1) {
            mediaFileName = mediaFileName.substring(0, mediaFileName.lastIndexOf("."));
        }

        File sdcardDir = getExternalFilesDir(null);
        if (sdcardDir == null) {
            TXCLog.e(TAG, "sdcardDir is null");
            return;
        }
        String folder = sdcardDir + File.separator + UGCKitConstants.DEFAULT_MEDIA_PACK_FOLDER + File.separator + mediaFileName;
        File appDir = new File(folder);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        String fileName = "thumbnail" + ".jpg";
        File file = new File(appDir, fileName);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (videoInfo.coverImage != null)
                videoInfo.coverImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileInfo.setThumbPath(file.getAbsolutePath());
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_done) {
            mButtonDone.setClickable(false);
            if (mFragmentWorkLoadingProgress == null) {
                initWorkLoadingProgress();
            }
            mFragmentWorkLoadingProgress.setProgress(0);
            mFragmentWorkLoadingProgress.setCancelable(false);
            mFragmentWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");
            loadSingleVideoInfo();
        } else if (i == R.id.back_ll) {
            mTXVideoJoiner.stopPlay();
            mTXVideoJoiner.cancel();
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.setVideoJoinerListener(null);
            finish();

        } else if (i == R.id.btn_play) {
            playVideo();
        }
    }

    private void loadSingleVideoInfo() {
        mHandler.sendEmptyMessage(MSG_SINGLE_VIDEO_INFO);
    }

    class BackGroundHandler extends Handler {

        public BackGroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SINGLE_VIDEO_INFO:
                    // Android 10（Q）Google官方尚未强制启用 App 沙箱运行，当且仅当 targetSDK 为 29 的时候才会在沙箱下运行
                    // Google官方预计 2020 年在 Android 11（R）强制启动沙箱机制，届时所有 app 无论 targetSDK 是否为 29，都运行在沙箱机制。
                    // 因此为了您的 app 保持较高兼容性，推荐您在系统版本为 Android 10或以上的设备，都使用 Google 官方推荐的 uri 统一资源定位符的方式传递给 SDK。
                    String source = "";
                    if (Build.VERSION.SDK_INT >= 29) {
                        source = mTCVideoFileInfoList.get(0).getFileUri().toString();
                    } else {
                        source = mTCVideoFileInfoList.get(0).getFilePath();
                    }
                    mVideoInfo = mVideoInfoReader.getVideoFileInfo(source);
                    sendMsgToMain(MSG_SINGLE_JOIN);
                    break;
            }

        }
    }

    private void sendMsgToMain(int msg) {
        if (mMainHandler != null) {
            Message mainMsg = new Message();
            mainMsg.what = msg;
            mainMsg.obj = mVideoInfo;
            mMainHandler.sendMessage(mainMsg);
        }
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SINGLE_JOIN:
                    if (mVideoInfo == null) {
                        if (mFragmentWorkLoadingProgress != null) {
                            mFragmentWorkLoadingProgress.setProgress(0);
                            mFragmentWorkLoadingProgress.dismiss();
                        }
                        mDialogUtil.showDialog(mContext, "视频合成失败", "暂不支持Android 4.3以下的系统", null);
                        mButtonDone.setClickable(true);
                        return;
                    }
                    startGenerateVideo();
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        TXCLog.i(TAG, "onResume");
        if (mRet == 0)
            playVideo();
    }

    public void playVideo() {
        TXCLog.i(TAG, "playVideo mCurrentState = " + mCurrentState + ",ret=" + mRet);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            startPlay();
        } else if ((mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY)) {
            pausePlay();
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            resumePlay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
        // 若当前处于生成状态，离开当前activity，直接停止生成
        if (mCurrentState == PlayState.STATE_GENERATE) {
            stopGenerate();
        }
        mButtonDone.setClickable(true);
        mButtonDone.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mFragmentWorkLoadingProgress != null) {
            mFragmentWorkLoadingProgress.setOnClickStopListener(null);
        }
        if (mTXVideoJoiner != null) {
            if (mCurrentState == PlayState.STATE_PLAY || mCurrentState == PlayState.STATE_PAUSE) {
                mTXVideoJoiner.stopPlay();
            } else if (mCurrentState == PlayState.STATE_GENERATE) {
                mTXVideoJoiner.cancel();
            }
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.setVideoJoinerListener(null);
        }

        if (mHandler != null) {
            mHandler.removeMessages(MSG_SINGLE_VIDEO_INFO);
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    public void startGenerateVideo() {
        stopPlay(); // 停止播放
        // 处于生成状态
        mCurrentState = PlayState.STATE_GENERATE;
        // 防止
        mButtonDone.setClickable(false);
        mButtonDone.setEnabled(false);
        // 生成视频输出路径
        mVideoOutputPath = VideoPathUtil.generateVideoPath();

        mTXVideoJoiner.setVideoJoinerListener(this);
        mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
        Toast.makeText(this, "开始视频合成", Toast.LENGTH_SHORT).show();
    }

    private void stopGenerate() {
        if (mFragmentWorkLoadingProgress != null) {
            mFragmentWorkLoadingProgress.setProgress(0);
            mFragmentWorkLoadingProgress.dismiss();
        }
        if (mCurrentState == PlayState.STATE_GENERATE) {
            mButtonDone.setEnabled(true);
            mButtonDone.setClickable(true);
            Toast.makeText(mContext, "取消视频合成", Toast.LENGTH_SHORT).show();

            mCurrentState = PlayState.STATE_NONE;
            if (mTXVideoJoiner != null) {
                mTXVideoJoiner.setVideoJoinerListener(null);
                mTXVideoJoiner.cancel();
            }
        }
    }

    @Override
    public void onPreviewProgress(int time) {
    }

    @Override
    public void onPreviewFinished() {
        TXCLog.d(TAG, "onPreviewFinished");
        mTXVideoJoiner.startPlay();
    }

    @Override
    public void onJoinProgress(float progress) {
        final int prog = (int) (progress * 100);
        TXCLog.d(TAG, "onJoinProgress progress = " + prog);
        mFragmentWorkLoadingProgress.setProgress(prog);
    }

    @Override
    public void onJoinComplete(final TXVideoEditConstants.TXJoinerResult result) {
        TXCLog.d(TAG, "===onJoinComplete===");
        mFragmentWorkLoadingProgress.dismiss();
        mButtonDone.setClickable(true);
        mButtonDone.setEnabled(true);
        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            if (mVideoInfo != null) {
                createThumbFile(mVideoInfo);
            } else {
                finish();
            }
        } else {
            mDialogUtil.showDialog(mContext, "视频合成失败", result.descMsg, null);
        }
        mCurrentState = PlayState.STATE_NONE;
    }

    private void startPreviewActivity() {
        Intent intent = new Intent(getApplicationContext(), TCVideoPreviewActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, mVideoOutputPath);
        intent.putExtra(UGCKitConstants.VIDEO_COVERPATH, mTCVideoFileInfoList.get(0).getThumbPath());
        TXVideoEditConstants.TXVideoInfo txVideoInfo = TXVideoInfoReader.getInstance(this).getVideoFileInfo(mVideoOutputPath);
        if (txVideoInfo != null) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_DURATION, TXVideoInfoReader.getInstance(this).getVideoFileInfo(mVideoOutputPath).duration);
        }
        startActivity(intent);
    }

    private void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    /*********************************************监听电话状态**************************************************/
    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TCVideoJoinerPreviewActivity> mEditer;

        public TXPhoneStateListener(TCVideoJoinerPreviewActivity editer) {
            mEditer = new WeakReference<TCVideoJoinerPreviewActivity>(editer);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TCVideoJoinerPreviewActivity activity = mEditer.get();
            if (activity == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    // 生成状态 取消生成
                    if (activity.mCurrentState == PlayState.STATE_GENERATE) {
                        activity.stopGenerate();
                    }
                    // 直接停止播放
                    activity.stopPlay();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }
}

