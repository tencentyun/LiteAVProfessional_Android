package com.tencent.liteav.demo.videojoiner;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.tencent.liteav.demo.videojoiner.common.utils.TCConstants;
import com.tencent.liteav.demo.videojoiner.common.videopreview.TCVideoPreviewActivity;
import com.tencent.liteav.demo.videojoiner.common.view.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videojoiner.common.utils.DialogUtil;
import com.tencent.liteav.demo.videojoiner.common.utils.PlayState;
import com.tencent.liteav.demo.videojoiner.common.utils.TCEditerUtil;
import com.tencent.liteav.demo.videojoiner.common.utils.TCVideoFileInfo;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;
import com.tencent.ugc.TXVideoJoiner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TCVideoJoinerPreviewActivity extends FragmentActivity implements View.OnClickListener, TXVideoJoiner.TXVideoPreviewListener, TXVideoJoiner.TXVideoJoinerListener {
    private static final String TAG = "TCVideoJoinerPreviewActivity";

    private int mCurrentState = PlayState.STATE_NONE;       // 播放器当前状态

    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;

    private TextView mBtnDone;
    private ImageButton mIbPlay;
    private FrameLayout mVideoView;

    private String mVideoOutputPath;
    private ArrayList<String> mVideoSourceList;

    private TXVideoJoiner mTXVideoJoiner;
    private TXVideoInfoReader mVideoInfoReader;

    private BackGroundHandler mHandler;
    private final int MSG_SINGLE_VIDEO_INFO = 1000;
    private final int MSG_MULTI_VIDEO_INFO = 1001;
    private final int MSG_SINGLE_JOIN = 1002;
    private final int MSG_MULTI_JOIN = 1003;
    private HandlerThread mHandlerThread;
    private VideoWorkProgressFragment mWorkLoadingProgress;
    private Context mContext;
    private DialogUtil mDialogUtil;
    private TXVideoEditConstants.TXVideoInfo videoInfo;
    private TXVideoEditConstants.TXVideoInfo videoInfo2;

    private TXPhoneStateListener mPhoneListener;            // 电话监听
    private int mRet = -1;

    private void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mVideoView;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoJoiner.initWithPreview(param);
    }

    public void startPlay() {
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoJoiner.startPlay();
            mCurrentState = PlayState.STATE_PLAY;
            mIbPlay.setImageResource(R.drawable.ic_pause);
        }
    }

    public void resumePlay() {
        if (mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoJoiner.resumePlay();
            mCurrentState = PlayState.STATE_RESUME;
            mIbPlay.setImageResource(R.drawable.ic_pause);
        }
    }

    public void pausePlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mTXVideoJoiner.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            mIbPlay.setImageResource(R.drawable.ic_play);
        }
    }

    public void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY ||
                mCurrentState == PlayState.STATE_PREVIEW_AT_TIME || mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoJoiner.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            mIbPlay.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_joiner_preview);

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
        mBtnDone = (TextView) findViewById(R.id.btn_done);
        mBtnDone.setClickable(false);
        mIbPlay = (ImageButton) findViewById(R.id.btn_play);
        mVideoView = (FrameLayout) findViewById(R.id.video_view);

        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(this);
        mIbPlay.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
    }

    private void initData() {
        mContext = TCVideoJoinerPreviewActivity.this;
        mDialogUtil = new DialogUtil();

        mTCVideoFileInfoList = (ArrayList<TCVideoFileInfo>) getIntent().getSerializableExtra(TCConstants.INTENT_KEY_MULTI_CHOOSE);
        if (mTCVideoFileInfoList == null || mTCVideoFileInfoList.size() == 0) {
            finish();
            return;
        }
        mTXVideoJoiner = new TXVideoJoiner(this);
        mTXVideoJoiner.setTXVideoPreviewListener(this);

        mVideoInfoReader = TXVideoInfoReader.getInstance();

        mVideoSourceList = new ArrayList<>();
        for (int i = 0; i < mTCVideoFileInfoList.size(); i++) {
            mVideoSourceList.add(mTCVideoFileInfoList.get(i).getFilePath());
        }

        mHandlerThread = new HandlerThread("LoadData");
        mHandlerThread.start();
        mHandler = new BackGroundHandler(mHandlerThread.getLooper());
    }

    private void initWorkLoadingProgress() {
        if (mWorkLoadingProgress == null) {
            mWorkLoadingProgress = new VideoWorkProgressFragment();
            mWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopGenerate();
                }
            });
        }
        mWorkLoadingProgress.setProgress(0);
    }

    private void createThumbFile(TXVideoEditConstants.TXVideoInfo videoInfo) {
        final TCVideoFileInfo fileInfo = mTCVideoFileInfoList.get(0);
        if (fileInfo == null)
            return;
        mBtnDone.setClickable(false);
        mIbPlay.setClickable(false);
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
        String folder = Environment.getExternalStorageDirectory() + File.separator + TCConstants.DEFAULT_MEDIA_PACK_FOLDER + File.separator + mediaFileName;
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
            mBtnDone.setClickable(false);
            if (mWorkLoadingProgress == null) {
                initWorkLoadingProgress();
            }
            mWorkLoadingProgress.setProgress(0);
            mWorkLoadingProgress.setCancelable(false);
            mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");
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

    /**
     * 选择合成模式
     */
    private void showJoinModeDialog() {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(TCVideoJoinerPreviewActivity.this, R.style.ConfirmDialogStyle);
        normalDialog.setMessage("选择合成模式");
        normalDialog.setCancelable(true);
        normalDialog.setPositiveButton("合成模式1", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                loadSingleVideoInfo();
//                startGenerateVideo();
            }
        });
        normalDialog.setNegativeButton("合成模式2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                loadMultiVideoInfo();
//                startPictureJoin();
            }
        });
        normalDialog.show();
    }

    private void loadMultiVideoInfo() {
        mHandler.sendEmptyMessage(MSG_MULTI_VIDEO_INFO);
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
                    videoInfo = mVideoInfoReader.getVideoFileInfo(mTCVideoFileInfoList.get(0).getFilePath());
                    sendMsgToMain(MSG_SINGLE_JOIN);
                    break;
                case MSG_MULTI_VIDEO_INFO:
                    videoInfo = mVideoInfoReader.getVideoFileInfo(mTCVideoFileInfoList.get(0).getFilePath());
                    videoInfo2 = mVideoInfoReader.getVideoFileInfo(mTCVideoFileInfoList.get(1).getFilePath());
                    sendMsgToMain(MSG_MULTI_JOIN);
                    break;
            }

        }
    }

    private void sendMsgToMain(int msg) {
        if (mMainHandler != null) {
            Message mainMsg = new Message();
            mainMsg.what = msg;
            mainMsg.obj = videoInfo;
            mMainHandler.sendMessage(mainMsg);
        }
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SINGLE_JOIN:
                    if (videoInfo == null) {
                        if (mWorkLoadingProgress != null) {
                            mWorkLoadingProgress.setProgress(0);
                            mWorkLoadingProgress.dismiss();
                        }
                        mDialogUtil.showDialog(mContext, "视频合成失败", "暂不支持Android 4.3以下的系统", null);
                        mBtnDone.setClickable(true);
                        return;
                    }
                    startGenerateVideo();
                    break;
                case MSG_MULTI_JOIN:

                    if (videoInfo == null) {
                        mDialogUtil.showDialog(mContext, "视频合成失败", "暂不支持Android 4.3以下的系统", null);
                        mBtnDone.setClickable(true);
                        return;
                    }
                    startPictureJoin();
                    break;
            }
        }
    };

    private void startPictureJoin() {
        if (mCurrentState == PlayState.STATE_PLAY || mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.stopPlay();
        }
        mBtnDone.setClickable(false);
        mBtnDone.setEnabled(false);
        Toast.makeText(this, "开始视频合成", Toast.LENGTH_SHORT).show();
        mWorkLoadingProgress.setProgress(0);
        mWorkLoadingProgress.setCancelable(false);
        mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");
        try {
            String outputPath = Environment.getExternalStorageDirectory() + File.separator + TCConstants.DEFAULT_MEDIA_PACK_FOLDER;
            File outputFolder = new File(outputPath);

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            String current = String.valueOf(System.currentTimeMillis() / 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String time = sdf.format(new Date(Long.valueOf(current + "000")));
            String saveFileName = String.format("TXVideo_%s.mp4", time);
            mVideoOutputPath = outputFolder + "/" + saveFileName;
            TXCLog.d(TAG, mVideoOutputPath);
            mTXVideoJoiner.setVideoJoinerListener(this);
            mCurrentState = PlayState.STATE_GENERATE;

            //示例：以右边高度为准
            if (videoInfo != null && videoInfo2 != null) {
                TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
                rect1.x = 0;                     //第一个视频的左上角位置
                rect1.y = 0;
                rect1.width = videoInfo.width;   //第一个视频的宽高
                rect1.height = videoInfo.height;

                TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
                rect2.x = rect1.x + rect1.width; //第2个视频的左上角位置
                rect2.y = 0;
                rect2.width = videoInfo2.width;  //第2个视频的宽高
                rect2.height = videoInfo2.height;

                List<TXVideoEditConstants.TXAbsoluteRect> list = new ArrayList<>();
                list.add(rect1);
                list.add(rect2);
                mTXVideoJoiner.setSplitScreenList(list, videoInfo.width + videoInfo2.width, videoInfo2.height); //第2，3个param：两个视频合成画布的宽高
                mTXVideoJoiner.splitJoinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        mBtnDone.setClickable(true);
        mBtnDone.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setOnClickStopListener(null);
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
            mHandler.removeMessages(MSG_MULTI_VIDEO_INFO);
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    public void startGenerateVideo() {
        stopPlay(); // 停止播放
        // 处于生成状态
        mCurrentState = PlayState.STATE_GENERATE;
        // 防止
        mBtnDone.setClickable(false);
        mBtnDone.setEnabled(false);
        // 生成视频输出路径
        mVideoOutputPath = TCEditerUtil.generateVideoPath();
//        if (mWorkLoadingProgress == null) {
//            initWorkLoadingProgress();
//        }
//        mWorkLoadingProgress.setProgress(0);
//        mWorkLoadingProgress.setCancelable(false);
//        mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");

        mTXVideoJoiner.setVideoJoinerListener(this);
        mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
        Toast.makeText(this, "开始视频合成", Toast.LENGTH_SHORT).show();
    }

    private void stopGenerate() {
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setProgress(0);
            mWorkLoadingProgress.dismiss();
        }
        if (mCurrentState == PlayState.STATE_GENERATE) {
            mBtnDone.setEnabled(true);
            mBtnDone.setClickable(true);
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
//        TXCLog.d(TAG, "onPreviewProgress time = " + time);
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
        mWorkLoadingProgress.setProgress(prog);
    }

    @Override
    public void onJoinComplete(final TXVideoEditConstants.TXJoinerResult result) {
        TXCLog.d(TAG, "===onJoinComplete===");
        mWorkLoadingProgress.dismiss();
        mBtnDone.setClickable(true);
        mBtnDone.setEnabled(true);
        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            if (videoInfo != null) {
                createThumbFile(videoInfo);
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
        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_PLAY);
        intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
        intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, mTCVideoFileInfoList.get(0).getThumbPath());
        TXVideoEditConstants.TXVideoInfo txVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(mVideoOutputPath);
        if (txVideoInfo != null) {
            intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, TXVideoInfoReader.getInstance().getVideoFileInfo(mVideoOutputPath).duration);
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

