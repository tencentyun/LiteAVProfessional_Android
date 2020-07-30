package com.tencent.liteav.demo.videoediter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoEdit;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.editer.IVideoEditKit;
import com.tencent.qcloud.ugckit.module.editer.UGCKitEditConfig;
import com.tencent.ugc.TXVideoEditConstants;

public class TCVideoEditerActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "TCVideoEditerActivity";

    private UGCKitVideoEdit mUGCKitVideoEdit;
    private TextView        mTextBgm;              // 背景音
    private TextView        mTextMotion;           // 动态滤镜
    private TextView        mTextSpeed;            // 时间特效
    private TextView        mTextFilter;           // 静态滤镜
    private TextView        mTextPaster;           // 贴纸
    private TextView        mTextSubtitle;         // 字幕

    private String mVideoPath;              // 视频路径
    private int    mVideoResolution = -1;   // 视频分辨率[从录制跳转的视频才有此参数，生成视频时保持与录制设置同样的分辨率]
    private int    mCustomBitrate;          // 视频自定义码率


    private IVideoEditKit.OnEditListener mOnEditListener = new IVideoEditKit.OnEditListener() {
        @Override
        public void onEditCompleted(UGCKitResult ugcKitResult) {
            startPreviewActivity(ugcKitResult);
        }

        @Override
        public void onEditCanceled() {
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcedit_activity_video_editer);

        initData();

        mUGCKitVideoEdit = (UGCKitVideoEdit) findViewById(R.id.video_edit);
        Log.d(TAG, "mVideoPath:" + mVideoPath);
        if (!TextUtils.isEmpty(mVideoPath)) {
            mUGCKitVideoEdit.setVideoPath(mVideoPath);
        }
        mUGCKitVideoEdit.initPlayer();
        UGCKitEditConfig config = new UGCKitEditConfig();
        config.isPublish = false;
        if (mCustomBitrate != 0) {
            config.videoBitrate = mCustomBitrate;
        }
        if (mVideoResolution != -1) {
            config.resolution = mVideoResolution;
        }
        config.isCoverGenerate = true;
        config.isSaveToDCIM = true;
        mUGCKitVideoEdit.setConfig(config);

        mTextBgm = (TextView) findViewById(R.id.tv_bgm);
        mTextMotion = (TextView) findViewById(R.id.tv_motion);
        mTextSpeed = (TextView) findViewById(R.id.tv_speed);
        mTextFilter = (TextView) findViewById(R.id.tv_filter);
        mTextPaster = (TextView) findViewById(R.id.tv_paster);
        mTextSubtitle = (TextView) findViewById(R.id.tv_subtitle);

        mTextBgm.setOnClickListener(this);
        mTextMotion.setOnClickListener(this);
        mTextSpeed.setOnClickListener(this);
        mTextFilter.setOnClickListener(this);
        mTextPaster.setOnClickListener(this);
        mTextSubtitle.setOnClickListener(this);
    }

    private void startPreviewActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(getApplicationContext(), TCEditPreviewActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        intent.putExtra(UGCKitConstants.VIDEO_COVERPATH, ugcKitResult.coverPath);
        startActivity(intent);
        finish();
    }

    private void initData() {
        mVideoResolution = getIntent().getIntExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXVideoEditConstants.VIDEO_COMPRESSED_720P);
        mCustomBitrate = getIntent().getIntExtra(UGCKitConstants.RECORD_CONFIG_BITE_RATE, 0);
        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUGCKitVideoEdit.initPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitVideoEdit.setOnVideoEditListener(mOnEditListener);
        mUGCKitVideoEdit.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoEdit.stop();
        mUGCKitVideoEdit.setOnVideoEditListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoEdit.release();
    }

    @Override
    public void onBackPressed() {
        mUGCKitVideoEdit.backPressed();
    }

    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        if (id == R.id.tv_bgm) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_BGM);
        } else if (id == R.id.tv_motion) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_MOTION);
        } else if (id == R.id.tv_speed) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_SPEED);
        } else if (id == R.id.tv_filter) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_FILTER);
        } else if (id == R.id.tv_paster) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_PASTER);
        } else if (id == R.id.tv_subtitle) {
            startEffectActivity(UGCKitConstants.TYPE_EDITER_SUBTITLE);
        }
    }

    /**
     * 跳转到视频特效编辑界面
     *
     * @param effectType {@link UGCKitConstants#TYPE_EDITER_BGM} 添加背景音</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_MOTION} 添加动态滤镜</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_SPEED} 添加时间特效</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_FILTER} 添加静态滤镜</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_PASTER} 添加贴纸</p>
     *                   {@link UGCKitConstants#TYPE_EDITER_SUBTITLE} 添加字幕</p>
     */
    private void startEffectActivity(int effectType) {
        Intent intent = new Intent(this, TCVideoEffectActivity.class);
        intent.putExtra(UGCKitConstants.KEY_FRAGMENT, effectType);
        startActivityForResult(intent, UGCKitConstants.ACTIVITY_OTHER_REQUEST_CODE);
    }
}
