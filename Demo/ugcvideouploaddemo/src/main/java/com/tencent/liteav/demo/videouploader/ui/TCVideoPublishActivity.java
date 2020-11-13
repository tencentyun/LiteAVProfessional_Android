package com.tencent.liteav.demo.videouploader.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.liteav.demo.player.demo.SuperPlayerActivity;
import com.tencent.liteav.demo.videouploader.R;
import com.tencent.liteav.demo.videouploader.ui.utils.Constants;
import com.tencent.liteav.demo.videouploader.ui.view.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videouploader.ui.utils.VideoServerManager.PublishSigListener;
import com.tencent.liteav.demo.videouploader.ui.utils.VideoServerManager;
import com.tencent.liteav.demo.videouploader.model.TXUGCPublish;
import com.tencent.liteav.demo.videouploader.model.TXUGCPublishTypeDef;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TCVideoPublishActivity extends FragmentActivity implements View.OnClickListener, ITXVodPlayListener {
    private final String TAG = "TCVideoPublishActivity";

    private LinearLayout              mLayoutBack;
    private EditText                  mEditVideoTitle;
    private ImageView                 mImageCover;
    private Button                    mButtonPublish;
    private TXCloudVideoView          mTXCloudVideoView;
    private VideoWorkProgressFragment mWorkLoadingProgress;
    private TXUGCPublish              mTXUGCPublish;
    private TXVodPlayer               mTXVodPlayer;
    private TXVodPlayConfig           mTXPlayConfig = null;

    private String  mTitleStr;
    private String  mSignature;
    private String  mVideoPath;
    private String  mCoverImagePath;
    private boolean mIsCancelPublish;

    private PublishSigListener      mPublishSigListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcupload_activity_video_publish);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlay(false);
    }

    private void startPlay() {
        mTXVodPlayer.setPlayerView(mTXCloudVideoView);
        mTXVodPlayer.enableHardwareDecode(false);
        mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mTXVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        mTXVodPlayer.setConfig(mTXPlayConfig);
        mTXVodPlayer.setVodListener(this);
        mTXVodPlayer.setLoop(true);

        mTXVodPlayer.startPlay(mVideoPath);
    }

    private void initData() {
        mVideoPath = getIntent().getStringExtra(Constants.VIDEO_EDITER_PATH);
        String sourceVideoPath = getIntent().getStringExtra(Constants.VIDEO_SOURCE_PATH);
        File sdcardDir = getExternalFilesDir(null);
        if (sdcardDir != null) {
            mCoverImagePath = sdcardDir.getAbsolutePath() + "/cover.jpg";
        }
        final Bitmap coverBitmap = TXVideoInfoReader.getInstance(this).getSampleImage(0, sourceVideoPath);
        if(coverBitmap != null){
            mImageCover.setImageBitmap(coverBitmap);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveBitmap(coverBitmap, mCoverImagePath);
                }
            }).start();
        }

        mTXVodPlayer = new TXVodPlayer(this);
        mTXPlayConfig = new TXVodPlayConfig();

        mTXUGCPublish = new TXUGCPublish(this.getApplicationContext(), "customID");

        initListener();
    }

    private void initListener() {
        mPublishSigListener = new PublishSigListener() {
            @Override
            public void onSuccess(String signatureStr) {
                mSignature = signatureStr;
                publish();
            }

            @Override
            public void onFail(final int errCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWorkLoadingProgress != null && mWorkLoadingProgress.isAdded()) {
                            mWorkLoadingProgress.dismiss();
                        }
                        Toast.makeText(TCVideoPublishActivity.this, "err code = " + errCode, Toast.LENGTH_SHORT).show();
                        startPlay();
                    }
                });
            }
        };
        VideoServerManager.getInstance().setPublishSigListener(mPublishSigListener);
    }

    private void initView() {
        mButtonPublish = (Button) findViewById(R.id.btn_publish);
        mEditVideoTitle = (EditText) findViewById(R.id.et_video_title);
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mTXCloudVideoView.disableLog(true);
        mImageCover = (ImageView) findViewById(R.id.iv_video_cover);
        mLayoutBack = (LinearLayout) findViewById(R.id.back_ll);

        mButtonPublish.setOnClickListener(this);
        mLayoutBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_publish) {
            publishVideo();
        } else if (id == R.id.back_ll) {
            finish();
        }
    }

    private void publishVideo() {
        if (!isNetworkAvailable(this)){
            Toast.makeText(this, R.string.ugcupload_toast_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        stopPlay(false);
        if (mWorkLoadingProgress == null) {
            initWorkLoadingProgress();
        }
        mWorkLoadingProgress.setProgress(0);
        mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");

        VideoServerManager.getInstance().getPublishSig();

        mIsCancelPublish = false;
    }

    private void publish() {
        mTXUGCPublish.setListener(new TXUGCPublishTypeDef.ITXVideoPublishListener() {
            @Override
            public void onPublishProgress(long uploadBytes, long totalBytes) {
                TXLog.d(TAG, "onPublishProgress [" + uploadBytes + "/" + totalBytes + "]");
                if(mIsCancelPublish){
                    return;
                }
                mWorkLoadingProgress.setProgress((int) ((uploadBytes * 100) / totalBytes));
            }

            @Override
            public void onPublishComplete(TXUGCPublishTypeDef.TXPublishResult result) {
                TXLog.d(TAG, "onPublishComplete [" + result.retCode + "/" + (result.retCode == 0 ? result.videoURL : result.descMsg) + "]");
                if (mWorkLoadingProgress != null && mWorkLoadingProgress.isAdded()) {
                    mWorkLoadingProgress.dismiss();
                }

                if(mIsCancelPublish){
                    return;
                }

                // 这里可以把上传返回的视频信息以及自定义的视频信息上报到自己的业务服务器
                reportVideoInfo(result);

                // 注意：如果取消发送时，是取消的剩余未上传的分片发送，如果视频比较小，分片已经进入任务队列了是无法取消的。此时不跳转到下一个界面。
                if (result.retCode == TXUGCPublishTypeDef.PUBLISH_RESULT_OK) {
                    Toast.makeText(TCVideoPublishActivity.this, R.string.ugcupload_toast_publish_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TCVideoPublishActivity.this, SuperPlayerActivity.class);
                    intent.putExtra(Constants.PLAYER_DEFAULT_VIDEO, false);
                    intent.putExtra(Constants.PLAYER_VIDEO_ID, result.videoId);
                    intent.putExtra(Constants.PLAYER_VIDEO_NAME, mTitleStr);
                    startActivity(intent);
                } else {
                    if (result.descMsg.contains("java.net.UnknownHostException") || result.descMsg.contains("java.net.ConnectException")) {
                        Toast.makeText(TCVideoPublishActivity.this, getString(R.string.ugcupload_error_publish_without_network, result.descMsg), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TCVideoPublishActivity.this, getString(R.string.ugcupload_message_publish_fail,result.retCode, result.descMsg), Toast.LENGTH_SHORT).show();
                    }
                    startPlay();
                }
            }
        });

        TXUGCPublishTypeDef.TXPublishParam param = new TXUGCPublishTypeDef.TXPublishParam();
        // signature计算规则可参考 https://www.qcloud.com/document/product/266/9221
        param.signature = mSignature;
        param.videoPath = mVideoPath;
        param.coverPath = mCoverImagePath;
        mTitleStr = mEditVideoTitle.getText().toString();
        if(TextUtils.isEmpty(mTitleStr)){
            mTitleStr = "测试";
        }
        param.fileName = mTitleStr;
        mTXUGCPublish.publishVideo(param);
    }

    private void reportVideoInfo(TXUGCPublishTypeDef.TXPublishResult result) {
        VideoServerManager.getInstance().reportVideoInfo(result.videoId, "腾讯云");
    }

    private void initWorkLoadingProgress() {
        if (mWorkLoadingProgress == null) {
            mWorkLoadingProgress = VideoWorkProgressFragment.newInstance("发布中...");
            mWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTXUGCPublish != null) {
                        mTXUGCPublish.canclePublish();
                        mIsCancelPublish = true;
                        mWorkLoadingProgress.setProgress(0);
                        mWorkLoadingProgress.dismiss();
                        startPlay();
                    }
                }
            });
        }
        mWorkLoadingProgress.setProgress(0);
    }

    protected void stopPlay(boolean clearLastFrame) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setVodListener(null);
            mTXVodPlayer.stopPlay(clearLastFrame);
        }
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {

    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }

    public static void saveBitmap(Bitmap bitmap, String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        File parent = f.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try {
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoServerManager.getInstance().setPublishSigListener(null);
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setOnClickStopListener(null);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }
}
