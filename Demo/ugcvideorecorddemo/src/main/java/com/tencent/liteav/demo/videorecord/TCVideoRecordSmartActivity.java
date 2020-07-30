package com.tencent.liteav.demo.videorecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoRecord;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.effect.bgm.TCMusicActivity;
import com.tencent.qcloud.ugckit.module.record.MusicInfo;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.interfaces.IVideoRecordKit;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.ugc.TXRecordCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * UGC小视频录制界面
 */
public class TCVideoRecordSmartActivity extends FragmentActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "TCVideoRecordSmartActivity";
    private UGCKitVideoRecord mUGCKitVideoRecord;

    private int     mMinDuration;
    private int     mMaxDuration;
    private int     mAspectRatio;
    private int     mRecommendQuality;
    private int     mVideoBitrate;
    private int     mResolution;
    private int     mFps;
    private int     mGop;
    private int     mOrientation;
    private boolean mTouchFocus;
    private boolean mNeedEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initWindowParam();
        setContentView(R.layout.ugcrecord_activity_video_record);

        mUGCKitVideoRecord = (UGCKitVideoRecord) findViewById(R.id.video_record_layout);

        UGCKitRecordConfig ugcKitRecordConfig = UGCKitRecordConfig.getInstance();
        ugcKitRecordConfig.mMinDuration = mMinDuration;
        ugcKitRecordConfig.mMaxDuration = mMaxDuration;
        ugcKitRecordConfig.mAspectRatio = mAspectRatio;
        ugcKitRecordConfig.mQuality = mRecommendQuality;
        ugcKitRecordConfig.mVideoBitrate = mVideoBitrate;
        ugcKitRecordConfig.mResolution = mResolution;
        ugcKitRecordConfig.mFPS = mFps;
        ugcKitRecordConfig.mGOP = mGop;
        ugcKitRecordConfig.mHomeOrientation = mOrientation;
        ugcKitRecordConfig.mTouchFocus = mTouchFocus;
        ugcKitRecordConfig.mIsNeedEdit = mNeedEdit;

        mUGCKitVideoRecord.setConfig(ugcKitRecordConfig);
        mUGCKitVideoRecord.disableTakePhoto();
        mUGCKitVideoRecord.disableLongPressRecord();
        mUGCKitVideoRecord.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUGCKitVideoRecord.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
            @Override
            public void onRecordCanceled() {
                finish();
            }

            @Override
            public void onRecordCompleted(UGCKitResult result) {
                if (mNeedEdit) {
                    startEditActivity(result);
                } else {
                    startPreviewActivity(result);
                }
            }
        });
        mUGCKitVideoRecord.setOnMusicChooseListener(new IVideoRecordKit.OnMusicChooseListener() {
            @Override
            public void onChooseMusic(int position) {
                Intent bgmIntent = new Intent(TCVideoRecordSmartActivity.this, TCMusicActivity.class);
                bgmIntent.putExtra(UGCKitConstants.MUSIC_POSITION, position);
                startActivityForResult(bgmIntent, UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE);
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mMinDuration = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_MIN_DURATION, 5 * 1000);
            mMaxDuration = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_MAX_DURATION, 60 * 1000);
            mAspectRatio = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
            mRecommendQuality = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY, -1);
            mResolution = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_540_960);
            mVideoBitrate = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_BITE_RATE, 6500);
            mFps = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_FPS, 20);
            mGop = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_GOP, 3);
            mOrientation = intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
            mTouchFocus = intent.getBooleanExtra(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS, true);
            mNeedEdit = intent.getBooleanExtra(UGCKitConstants.RECORD_CONFIG_NEED_EDITER, true);
        }
    }

    private void initWindowParam() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void startEditActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent();
        intent.setAction("com.tencent.liteav.demo.videoediter");
        if (mRecommendQuality == TXRecordCommon.VIDEO_QUALITY_LOW) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_360_640);
        } else if (mRecommendQuality == TXRecordCommon.VIDEO_QUALITY_MEDIUM) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_540_960);
        } else if (mRecommendQuality == TXRecordCommon.VIDEO_QUALITY_HIGH) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_720_1280);
        } else {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, mResolution);
        }
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        startActivity(intent);
        finish();
    }

    private void startPreviewActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(getApplicationContext(), TCRecordPreviewActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        intent.putExtra(UGCKitConstants.VIDEO_COVERPATH, ugcKitResult.coverPath);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermission()) {
            mUGCKitVideoRecord.start();
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUGCKitVideoRecord.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitVideoRecord.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUGCKitVideoRecord.screenOrientationChange();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE) {
            return;
        }
        if (data == null) {
            return;
        }
        MusicInfo musicInfo = new MusicInfo();

        musicInfo.path = data.getStringExtra(UGCKitConstants.MUSIC_PATH);
        musicInfo.name = data.getStringExtra(UGCKitConstants.MUSIC_NAME);
        musicInfo.position = data.getIntExtra(UGCKitConstants.MUSIC_POSITION, -1);

        mUGCKitVideoRecord.setRecordMusicInfo(musicInfo);
    }

    @Override
    public void onBackPressed() {
        mUGCKitVideoRecord.backPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults == null || permissions == null || permissions.length == 0 || grantResults.length == 0) {
            return;
        }

        for (int ret : grantResults) {
            if (ret != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mUGCKitVideoRecord.start();
    }
}
