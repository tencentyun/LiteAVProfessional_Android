package com.tencent.liteav.v1livepusherdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.v1livepusherdemo.itemview.BaseSettingItem;
import com.tencent.liteav.v1livepusherdemo.itemview.CheckBoxSettingItem;
import com.tencent.liteav.v1livepusherdemo.itemview.RadioButtonSettingItem;
import com.tencent.liteav.v1livepusherdemo.utils.V1Utils;
import com.tencent.liteav.v1livepusherdemo.customcapture.utils.MediaUtils;

import java.util.ArrayList;
import java.util.List;

public class V1VideoPusherEntranceActivity extends Activity {
    private static final String TAG = "V1VideoPusherEntrance";
    private static final int REQ_CHOOSE_VIDEO_FILE = 101;

    private final List<BaseSettingItem>  mSettingItemList = new ArrayList<>();
    private LinearLayout           mContainer;
    private RadioButtonSettingItem mInputSourceItem;
    private RadioButtonSettingItem mProfileItem;
    private CheckBoxSettingItem    mCustomVideoPreprocessItem;
    private CheckBoxSettingItem    mEnableHighCaptureItem;
    private CheckBoxSettingItem         mPauseAudioOnActivityPausedItem;

    private String mVideoFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1livepusher_video_entrance_activity);
        mContainer = (LinearLayout) findViewById(R.id.ll_container);
        TextView enterView = (TextView) findViewById(R.id.v1livepusher_tv_enter);
        enterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputSourceItem.getSelected() == 1) {
                    startSelectVideoActivity();
                } else {
                    mVideoFilePath = null;
                    startPushActivity();
                }
            }
        });

        BaseSettingItem.ItemText itemText = new BaseSettingItem.ItemText("视频输入", "前摄像头", "视频文件", "录屏");
        mInputSourceItem = new RadioButtonSettingItem(this, itemText, new RadioButtonSettingItem.SelectedListener() {
            @Override
            public void onSelected(int index) {
                // 自定义采集 不支持 自定义预处理
                if (index == 1) {
                    mCustomVideoPreprocessItem.setCheck(false);
                    mCustomVideoPreprocessItem.getView().setVisibility(View.GONE);
                } else {
                    mCustomVideoPreprocessItem.getView().setVisibility(View.VISIBLE);
                }
            }
        });
        mSettingItemList.add(mInputSourceItem);

        itemText = new BaseSettingItem.ItemText("Profile", "默认(RTC为Base、其他为High)", "High", "Baseline");
        mProfileItem = new RadioButtonSettingItem(this, itemText, null);
        mSettingItemList.add(mProfileItem);

        itemText = new BaseSettingItem.ItemText("自定义预处理", "");
        mCustomVideoPreprocessItem = new CheckBoxSettingItem(this, itemText, null);
        mSettingItemList.add(mCustomVideoPreprocessItem);

        itemText = new BaseSettingItem.ItemText("高分辨率采集", "");
        mEnableHighCaptureItem = new CheckBoxSettingItem(this, itemText, null);
        mSettingItemList.add(mEnableHighCaptureItem);

        itemText = new BaseSettingItem.ItemText("退后台后推静音数据", "");
        mPauseAudioOnActivityPausedItem = new CheckBoxSettingItem(this, itemText, null);
        mSettingItemList.add(mPauseAudioOnActivityPausedItem);

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(5), 0, 0);
            mContainer.addView(view);
        }

        // 强制设置为 视频推流
        mInputSourceItem.setSelect(1);
        mInputSourceItem.getView().setVisibility(View.GONE);
        mCustomVideoPreprocessItem.setCheck(false);
        mCustomVideoPreprocessItem.getView().setVisibility(View.GONE);

        checkPublishPermission();  // 检查权限
    }

    private void startSelectVideoActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQ_CHOOSE_VIDEO_FILE);
    }

    private void startPushActivity() {
        Intent intent = new Intent(this, V1VideoPusherMainActivity.class);
        intent.putExtra(V1Constants.KEY_CUSTOM_VIDEO_PATH, mVideoFilePath);
        intent.putExtra(V1Constants.KEY_IS_SCREEN_CAPTURE, mInputSourceItem.getSelected() == 2);
        intent.putExtra(V1Constants.KEY_CUSTOM_VIDEO_PREPROCESS, mCustomVideoPreprocessItem.getChecked());
        intent.putExtra(V1Constants.KEY_ENABLE_HIGH_CAPTURE, mEnableHighCaptureItem.getChecked());
        intent.putExtra(V1Constants.KEY_PAUSE_AUDIO_ON_ACTIVITY_PAUSED, mPauseAudioOnActivityPausedItem.getChecked());
        intent.putExtra(V1Constants.KEY_PROFILE_MODE, mProfileItem.getSelected());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CHOOSE_VIDEO_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                mVideoFilePath = uri.getPath();
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    mVideoFilePath = V1Utils.getPath(this, uri);
                } else {//4.4以下下系统调用方法
                    mVideoFilePath = V1Utils.getRealPathFromURI(this, uri);
                }
            }

            try {
                MediaFormat mediaFormat  = MediaUtils.retriveMediaFormat(mVideoFilePath, false);
                int         sampleRate   = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int         channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                if (sampleRate != 48000 || channelCount != 1) {
                    Toast.makeText(this, "音频仅支持采样率48000、单声道，请重新选择！", Toast.LENGTH_SHORT).show();
                    mVideoFilePath = null;
                } else {
                    startPushActivity();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to open file " + mVideoFilePath);
                Toast.makeText(this, "打开文件失败!", Toast.LENGTH_LONG).show();
                mVideoFilePath = null;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      权限相关回调接口
    //
    /////////////////////////////////////////////////////////////////////////////////
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

    private boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
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
}

