package com.tencent.liteav.v1livepusherdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.v1livepusherdemo.itemview.BaseSettingItem;
import com.tencent.liteav.v1livepusherdemo.itemview.CheckBoxSettingItem;
import com.tencent.liteav.v1livepusherdemo.itemview.RadioButtonSettingItem;
import com.tencent.liteav.v1livepusherdemo.view.LoadingFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 摄像头推流入口页面，主要用于生成推流地址
 */
public class V1CameraPushEntranceActivity extends Activity {

    private static final int    ACTIVITY_SCAN_REQUEST_CODE = 1;
    private static final int    REQUEST_CODE               = 100;
    private static final int    REQ_CHOOSE_VIDEO_FILE      = 200;
    private static final String EMPTY                      = "";

    private       Context                mContext;
    private       EditText               mEditInputURL;
    private final List<BaseSettingItem>  mSettingItemList = new ArrayList<>();
    private       LinearLayout           mContainer;
    private       RadioButtonSettingItem mInputSourceItem;
    private       RadioButtonSettingItem mProfileItem;
    private       CheckBoxSettingItem    mCustomVideoPreprocessItem;
    private       CheckBoxSettingItem    mEnableHighCaptureItem;
    private       CheckBoxSettingItem    mPauseAudioOnActivityPausedItem;
    private       String                 mVideoFilePath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.v1livepusher_live_pusher_entrance_activity);
        initViews();
        checkPublishPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(V1Constants.INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            startLivePusher(scanURL);
        }
    }

    private void initViews() {
        mEditInputURL = findViewById(R.id.v1livepusher_et_input_url);
        mEditInputURL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getAction() == KeyEvent.ACTION_UP)) {
                    String url = mEditInputURL.getText().toString().trim();
                    startLivePusher(url);
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.v1livepusher_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.v1livepusher_ibtn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });

        initSettingView();
    }

    private void initSettingView() {
        mContainer = (LinearLayout) findViewById(R.id.ll_container);
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

        // 强制设置为Camera采集
        mInputSourceItem.setSelect(0);
        mInputSourceItem.getView().setVisibility(View.GONE);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.v1livepusher_btn_normal_url) {
            fetchPusherURL();
        } else if (id == R.id.v1livepusher_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.v1livepusher_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            startLivePusher(url);
        }
    }

    private void fetchPusherURL() {
        final LoadingFragment fragment = new LoadingFragment();
        fragment.show(getFragmentManager(), "LOADING");
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(Constants.URL_FETCH_PUSH_URL)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fragment.dismissAllowingStateLoss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonRsp = new JSONObject(response.body().string());
                        String pusherURLDefault = jsonRsp.optString(Constants.URL_PUSH);
                        String rtmpPlayURL = jsonRsp.optString(Constants.URL_PLAY_RTMP);
                        String flvPlayURL = jsonRsp.optString(Constants.URL_PLAY_FLV);
                        String hlsPlayURL = jsonRsp.optString(Constants.URL_PLAY_HLS);
                        String realtimePlayURL = jsonRsp.optString(Constants.URL_PLAY_ACC);
                        fragment.dismissAllowingStateLoss();
                        startLivePusher(pusherURLDefault, rtmpPlayURL, flvPlayURL, hlsPlayURL, realtimePlayURL);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.CAMERA,
                    PermissionConstants.MICROPHONE, PermissionConstants.PHONE)
                    .callback(new PermissionUtils.FullCallback() {
                        @Override
                        public void onGranted(List<String> permissionsGranted) {
                        }

                        @Override
                        public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                            if (permissionsDenied.contains(Manifest.permission.CAMERA) || permissionsDenied
                                    .contains(Manifest.permission.RECORD_AUDIO)) {
                                ToastUtils.showShort(R.string.v1livepusher_permissions_camera_mic);
                                finish();
                            }
                        }
                    }).request();
        }
    }

    private void startLivePusher(String pushURL) {
        if (TextUtils.isEmpty(pushURL)) {
            Toast.makeText(mContext, getString(R.string.v1livepusher_input_push_url), Toast.LENGTH_LONG).show();
            return;
        }
        String baseUrl = getBaseUrl(pushURL);
        if (TextUtils.isEmpty(baseUrl)) {
            startLivePusher(pushURL, EMPTY, EMPTY, EMPTY, EMPTY);
            return;
        }
        String rtmpPlayURL = "rtmp://" + baseUrl;
        String flvPlayURL = "http://" + baseUrl + ".flv";
        String hlsPlayURL = "http://" + baseUrl + ".m3u8";
        String aacPlayURL = pushURL.replace("livepush", "liveplay");
        startLivePusher(pushURL, rtmpPlayURL, flvPlayURL, hlsPlayURL, aacPlayURL);
    }

    private String getBaseUrl(String pushUrl) {
        if (TextUtils.isEmpty(pushUrl)) {
            return null;
        }
        if (!pushUrl.startsWith("rtmp")) {
            return null;
        }
        String[] array1 = pushUrl.split("//");
        if (array1.length < 2) {
            return null;
        }
        String[] array2 = array1[1].split("\\?");
        if (array2.length < 2) {
            return null;
        }
        if (!array2[0].contains("livepush")) {
            return null;
        }
        return array2[0].replace("livepush", "liveplay");
    }

    private void startLivePusher(String pushURL, String rtmpPlayURL, String flvPlayURL, String hlsPlayURL, String realtimePlayURL) {
        Intent intent = new Intent(mContext, V1CameraPushMainActivity.class);
        intent.putExtra(Constants.INTENT_URL_PUSH, pushURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_RTMP, rtmpPlayURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_FLV, flvPlayURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_HLS, hlsPlayURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_ACC, realtimePlayURL);
        intent.putExtra(V1Constants.KEY_CUSTOM_VIDEO_PATH, mVideoFilePath);
        intent.putExtra(V1Constants.KEY_IS_SCREEN_CAPTURE, mInputSourceItem.getSelected() == 2);
        intent.putExtra(V1Constants.KEY_CUSTOM_VIDEO_PREPROCESS, mCustomVideoPreprocessItem.getChecked());
        intent.putExtra(V1Constants.KEY_ENABLE_HIGH_CAPTURE, mEnableHighCaptureItem.getChecked());
        intent.putExtra(V1Constants.KEY_PAUSE_AUDIO_ON_ACTIVITY_PAUSED, mPauseAudioOnActivityPausedItem.getChecked());
        intent.putExtra(V1Constants.KEY_PROFILE_MODE, mProfileItem.getSelected());
        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.URL_PRODUCT_DOCUMENT));
        IntentUtils.safeStartActivity(this, intent);
    }
}