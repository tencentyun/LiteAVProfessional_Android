package com.tencent.liteav.demo.videoediter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoCut;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.cut.IVideoCutKit;
import com.tencent.ugc.TXVideoEditConstants;

/**
 * 腾讯云"视频裁剪" Demo
 */
public class TCVideoCutActivity extends FragmentActivity {
    private static final String TAG = "TCVideoCutActivity";

    private UGCKitVideoCut mUGCKitVideoCut;

    private String mVideoPath;                // 视频路径
    private String mVideoUri;                // 视频路径Uri
    private int    mVideoResolution = -1;    // 视频分辨率[从录制跳转的视频才有此参数，生成视频时保持与录制设置同样的分辨率]
    private int    mCustomBitrate;           //视频自定义码率

    private IVideoCutKit.OnCutListener mOnCutListener = new IVideoCutKit.OnCutListener() {
        /**
         * 视频裁剪进度条执行完成后调用
         */
        @Override
        public void onCutterCompleted(UGCKitResult ugcKitResult) {
            Log.i(TAG, "onCutterCompleted");
            if (ugcKitResult.errorCode == 0) {
                startEditActivity();
            }
        }

        /**
         * 点击视频裁剪进度叉号，取消裁剪时被调用
         */
        @Override
        public void onCutterCanceled() {
            Log.i(TAG, "onCutterCanceled");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcedit_activity_video_cut);
        mUGCKitVideoCut = (UGCKitVideoCut) findViewById(R.id.video_cutter_layout);

        mVideoResolution = getIntent().getIntExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXVideoEditConstants.VIDEO_COMPRESSED_720P);
        mCustomBitrate = getIntent().getIntExtra(UGCKitConstants.RECORD_CONFIG_BITE_RATE, 0);
        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
        mVideoUri = getIntent().getStringExtra(UGCKitConstants.VIDEO_URI);

        String path = null;
        if (Build.VERSION.SDK_INT >= 29) {
            path = mVideoUri;
        } else {
            path = mVideoPath;
        }
        mUGCKitVideoCut.setVideoPath(path);
        mUGCKitVideoCut.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitVideoCut.setOnCutListener(mOnCutListener);
        mUGCKitVideoCut.startPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoCut.stopPlay();
        //FIXBUG:生成进度条使用的同一个，不要在onStop中清空Listener，【onStop在下个界面onStart之后调用，会被清空】
        mUGCKitVideoCut.setOnCutListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoCut.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startEditActivity() {
        Intent intent = new Intent(this, TCVideoEditerActivity.class);
        if (mVideoResolution != -1) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, mVideoResolution);
        }
        startActivity(intent);
        finish();
    }

}
