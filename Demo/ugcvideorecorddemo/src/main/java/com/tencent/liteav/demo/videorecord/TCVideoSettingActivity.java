package com.tencent.liteav.demo.videorecord;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.ugc.TXRecordCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * UGC小视频设置界面.
 */

public class TCVideoSettingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TCVideoSettingActivity";

    private LinearLayout mLayoutBack;
    private View         mLayoutBiterate;
    private View         mLayoutFps;
    private View         mLayoutGop;
    private EditText     mEditBitrate;
    private EditText     mEditGop;
    private EditText     mEditFps;
    private RadioGroup   mGroupVideoQuality;
    private RadioGroup   mGroupVideoResolution;
    private RadioGroup   mGroupVideoAspectRatio;
    private TextView     mTextRecommendResolution;
    private TextView     mTextRecommendBitrate;
    private TextView     mTextRecommendFps;
    private TextView     mTextRecommendGop;
    private Button       mButtonOK;
    private ImageButton  mButtonLink;
    private CheckBox     mCheckBoxTouchFocus; // true：手动对焦；false： 自动对焦
    private CheckBox     mCheckBoxEdit;
    private RadioButton  mRadioVideoAspectRatio169;

    private RadioButton mRadioVideoQualitySD, mRadioVideoQualityHD, mRadioVideoQualitySSD, mRadioVideoQulityCustom,
            mRadioVideoResolution360p, mRadioVideoResolution540p, mRadioVideoResolution720p,
            mRadioVideoAspectRatio11, mRadioVideoAspectRatio34, mRadioVideoAspectRatio916, mRadioVideoAspectRatio43;

    private int mAspectRatio;               // 视频比例
    private int mRecordResolution;          // 录制分辨率
    private int mBiteRate = 2400;           // 码率
    private int mFps      = 20;             // 帧率
    private int mGop      = 3;              // 关键帧间隔
    private int mRecommendQuality = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ugcrecord_activity_video_settings);

        initData();

        initView();

        initListener();

        initViewStatus();

        checkPermission();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initData() {
        mRecommendQuality = -1;
    }

    private void initView() {
        mLayoutBack = (LinearLayout) findViewById(R.id.back_ll);

        mLayoutBiterate = findViewById(R.id.rl_bite_rate);
        mLayoutFps = findViewById(R.id.rl_fps);
        mLayoutGop = findViewById(R.id.rl_gop);

        mEditBitrate = (EditText) findViewById(R.id.et_biterate);
        mEditFps = (EditText) findViewById(R.id.et_fps);
        mEditGop = (EditText) findViewById(R.id.et_gop);

        mGroupVideoQuality = (RadioGroup) findViewById(R.id.rg_video_quality);
        mGroupVideoResolution = (RadioGroup) findViewById(R.id.rg_video_resolution);
        mGroupVideoAspectRatio = (RadioGroup) findViewById(R.id.rg_video_aspect_ratio);

        mRadioVideoQualitySD = (RadioButton) findViewById(R.id.rb_video_quality_sd);
        mRadioVideoQualityHD = (RadioButton) findViewById(R.id.rb_video_quality_hd);
        mRadioVideoQualitySSD = (RadioButton) findViewById(R.id.rb_video_quality_super);
        mRadioVideoQulityCustom = (RadioButton) findViewById(R.id.rb_video_quality_custom);

        mRadioVideoResolution360p = (RadioButton) findViewById(R.id.rb_video_resolution_360p);
        mRadioVideoResolution540p = (RadioButton) findViewById(R.id.rb_video_resolution_540p);
        mRadioVideoResolution720p = (RadioButton) findViewById(R.id.rb_video_resolution_720p);

        mRadioVideoAspectRatio11 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_1_1);
        mRadioVideoAspectRatio34 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_3_4);
        mRadioVideoAspectRatio916 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_9_16);
        mRadioVideoAspectRatio169 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_16_9);
        mRadioVideoAspectRatio43 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_4_3);

        mTextRecommendResolution = (TextView) findViewById(R.id.tv_recommend_resolution);
        mTextRecommendBitrate = (TextView) findViewById(R.id.tv_recommend_bitrate);
        mTextRecommendFps = (TextView) findViewById(R.id.tv_recommend_fps);
        mTextRecommendGop = (TextView) findViewById(R.id.tv_recommend_gop);

        mButtonOK = (Button) findViewById(R.id.btn_ok);

        mCheckBoxTouchFocus = (CheckBox) findViewById(R.id.cb_touch_focus);
        mCheckBoxEdit = (CheckBox) findViewById(R.id.cb_edit);

        mButtonLink = (ImageButton) findViewById(R.id.webrtc_link_button);
    }

    private void initListener() {
        mLayoutBack.setOnClickListener(this);
        mButtonOK.setOnClickListener(this);
        mButtonLink.setOnClickListener(this);

        mEditBitrate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mLayoutBiterate.setBackgroundResource(R.drawable.ugckit_rect_bg_green);
                } else {
                    mLayoutBiterate.setBackgroundResource(R.drawable.ugckit_rect_bg_gray);
                }
            }
        });

        mEditFps.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mLayoutFps.setBackgroundResource(R.drawable.ugckit_rect_bg_green);
                } else {
                    mLayoutFps.setBackgroundResource(R.drawable.ugckit_rect_bg_gray);
                }
            }
        });

        mEditGop.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mLayoutGop.setBackgroundResource(R.drawable.ugckit_rect_bg_green);
                } else {
                    mLayoutGop.setBackgroundResource(R.drawable.ugckit_rect_bg_gray);
                }
            }
        });

        mGroupVideoAspectRatio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == mRadioVideoAspectRatio11.getId()) {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
                } else if (i == mRadioVideoAspectRatio34.getId()) {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
                } else if (i == mRadioVideoAspectRatio916.getId()) {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
                } else if (i == mRadioVideoAspectRatio169.getId()) {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_16_9;
                }
                else if(i == mRadioVideoAspectRatio43.getId()){
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_4_3;
                }
            }
        });

        mGroupVideoQuality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == mRadioVideoQualitySD.getId()) {
                    mRecommendQuality = TXRecordCommon.VIDEO_QUALITY_LOW;
                    showRecommendQualitySet();
                    recommendQualitySD();
                    clearCustomBg();
                } else if (i == mRadioVideoQualityHD.getId()) {
                    mRecommendQuality = TXRecordCommon.VIDEO_QUALITY_MEDIUM;
                    showRecommendQualitySet();
                    recommendQualityHD();
                    clearCustomBg();
                } else if (i == mRadioVideoQualitySSD.getId()) {
                    mRecommendQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;
                    showRecommendQualitySet();
                    recommendQualitySSD();
                    clearCustomBg();
                } else {
                    // 自定义
                    mRecommendQuality = -1;
                    showCustomQualitySet();
                }
            }
        });

        mGroupVideoResolution.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == mRadioVideoResolution360p.getId()) {
                    mRecordResolution = TXRecordCommon.VIDEO_RESOLUTION_360_640;
                } else if (i == mRadioVideoResolution540p.getId()) {
                    mRecordResolution = TXRecordCommon.VIDEO_RESOLUTION_540_960;
                } else {
                    mRecordResolution = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
                }
            }
        });
    }

    private void initViewStatus() {
        mRadioVideoResolution540p.setChecked(true);
        mRadioVideoAspectRatio916.setChecked(true);

        mRadioVideoQualityHD.setChecked(true);
    }

    private void recommendQualitySD() {
        mTextRecommendResolution.setText("360p");
        mTextRecommendBitrate.setText("2400");
        mTextRecommendFps.setText("20");
        mTextRecommendGop.setText("3");

        mRadioVideoResolution360p.setChecked(true);
    }

    private void recommendQualityHD() {
        mTextRecommendResolution.setText("540p");
        mTextRecommendBitrate.setText("6500");
        mTextRecommendFps.setText("20");
        mTextRecommendGop.setText("3");

        mRadioVideoResolution540p.setChecked(true);
    }

    private void recommendQualitySSD() {
        mTextRecommendResolution.setText("720p");
        mTextRecommendBitrate.setText("9600");
        mTextRecommendFps.setText("20");
        mTextRecommendGop.setText("3");

        mRadioVideoResolution720p.setChecked(true);
    }

    private void showCustomQualitySet() {
        mGroupVideoResolution.setVisibility(View.VISIBLE);
        mEditBitrate.setVisibility(View.VISIBLE);
        mEditFps.setVisibility(View.VISIBLE);
        mEditGop.setVisibility(View.VISIBLE);

        mTextRecommendGop.setVisibility(View.GONE);
        mTextRecommendResolution.setVisibility(View.GONE);
        mTextRecommendBitrate.setVisibility(View.GONE);
        mTextRecommendFps.setVisibility(View.GONE);
    }

    private void showRecommendQualitySet() {
        mGroupVideoResolution.setVisibility(View.GONE);
        mEditBitrate.setVisibility(View.GONE);
        mEditFps.setVisibility(View.GONE);
        mEditGop.setVisibility(View.GONE);

        mTextRecommendGop.setVisibility(View.VISIBLE);
        mTextRecommendResolution.setVisibility(View.VISIBLE);
        mTextRecommendBitrate.setVisibility(View.VISIBLE);
        mTextRecommendFps.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.back_ll) {
            finish();

        } else if (i == R.id.btn_ok) {
            getConfigData();
            startVideoRecordActivity();

        } else if (i == R.id.webrtc_link_button) {
            showCloudLink();

        }
    }

    private void showCloudLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://cloud.tencent.com/document/product/584/9369"));
        startActivity(intent);
    }

    private void clearCustomBg() {
        mLayoutBiterate.setBackgroundResource(R.drawable.ugckit_rect_bg_gray);
        mLayoutFps.setBackgroundResource(R.drawable.ugckit_rect_bg_gray);
        mLayoutGop.setBackgroundResource(R.drawable.ugckit_rect_bg_gray);
    }

    private void getConfigData() {
        // 使用提供的三挡质量设置，不需要传以下参数，sdk内部已定义
        if (mRecommendQuality != -1) {
            return;
        }

        String fps = mEditFps.getText().toString();
        String gop = mEditGop.getText().toString();
        String bitrate = mEditBitrate.getText().toString();

        if (!TextUtils.isEmpty(bitrate)) {
            try {
                mBiteRate = Integer.parseInt(bitrate);
                if (mBiteRate < 600) {
                    mBiteRate = 600;
                } else if (mBiteRate > 12000) {
                    mBiteRate = 12000;
                }
            } catch (NumberFormatException e) {
                TXCLog.e(TAG, "NumberFormatException");
            }
        } else {
            mBiteRate = 6500;
        }

        if (!TextUtils.isEmpty(fps)) {
            try {
                mFps = Integer.parseInt(fps);
                if (mFps < 15) {
                    mFps = 15;
                } else if (mFps > 30) {
                    mFps = 20;
                }
            } catch (NumberFormatException e) {
                TXCLog.e(TAG, "NumberFormatException");
            }
        } else {
            mFps = 20;
        }

        if (!TextUtils.isEmpty(gop)) {
            try {
                mGop = Integer.parseInt(gop);
                if (mGop < 1) {
                    mGop = 1;
                } else if (mGop > 10) {
                    mGop = 3;
                }
            } catch (NumberFormatException e) {
                TXCLog.e(TAG, "NumberFormatException");
            }
        } else {
            mGop = 3;
        }
    }

    private void startVideoRecordActivity() {
        Intent intent = new Intent(this, TCVideoRecordActivity.class);
        intent.putExtra(UGCKitConstants.RECORD_CONFIG_MIN_DURATION, 5 * 1000);
        intent.putExtra(UGCKitConstants.RECORD_CONFIG_MAX_DURATION, 60 * 1000);
        intent.putExtra(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, mAspectRatio);

        if (mRecommendQuality != -1) {
            // 提供的三挡设置
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY, mRecommendQuality);
        } else {
            // 自定义设置
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_RESOLUTION, mRecordResolution);
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_BITE_RATE, mBiteRate);
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_FPS, mFps);
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_GOP, mGop);
        }
        // 竖屏录制
        intent.putExtra(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
        intent.putExtra(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS, mCheckBoxTouchFocus.isChecked());
        intent.putExtra(UGCKitConstants.RECORD_CONFIG_NEED_EDITER, mCheckBoxEdit.isChecked());
        startActivity(intent);
    }

}
