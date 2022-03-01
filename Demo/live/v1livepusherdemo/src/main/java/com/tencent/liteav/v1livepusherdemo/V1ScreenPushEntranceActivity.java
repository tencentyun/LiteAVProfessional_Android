package com.tencent.liteav.v1livepusherdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class V1ScreenPushEntranceActivity extends Activity {
    private static final String  TAG                          = "V1ScreenPushActivity";
    private static final String  PUSHER_PLAY_QR_CODE_FRAGMENT = "v1_push_play_qr_code_fragment";
    private static       String  sQRCodePusherURL;
    private static       String  sPushURL;
    private static       int     sResolution                  = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;
    private static       int     sResolutionMode              = V1Constants.SCREEN_MODE_PORTRAIT;
    private static       boolean sHasInitPusher               = false;

    private Context                mContext;
    private EditText               mEditInputURL;
    private RadioButton            mLandScape;
    private RadioButton            mPortrait;
    private RadioButton            mVideoSuper;
    private RadioButton            mVideoStand;
    private RadioButton            mVideoHigh;
    private QRCodeGenerateFragment mPusherPlayQRCodeFragment;
    private TXLivePusher           sLivePusher;
    private TXLivePushConfig       mTXLivePushConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1livepusher_screen_entrance_activity);
        mContext = this;

        mPortrait = findViewById(R.id.v1livepusher_rb_portrait);
        mLandScape = findViewById(R.id.v1livepusher_rb_landscape);
        mVideoSuper = findViewById(R.id.v1livepusher_rb_video_super);
        mVideoStand = findViewById(R.id.v1livepusher_rb_video_stand);
        mVideoHigh = findViewById(R.id.v1livepusher_rb_video_high);

        mLandScape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    sResolutionMode = V1Constants.SCREEN_MODE_LANDSCAPE;
                    mTXLivePushConfig.setHomeOrientation(sResolutionMode);
                    if (sLivePusher != null) {
                        sLivePusher.setConfig(mTXLivePushConfig);
                    }
                }
            }
        });
        mPortrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    sResolutionMode = V1Constants.SCREEN_MODE_PORTRAIT;
                    mTXLivePushConfig.setHomeOrientation(sResolutionMode);
                    if (sLivePusher != null) {
                        sLivePusher.setConfig(mTXLivePushConfig);
                    }
                }
            }
        });
        mVideoStand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    sResolution = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;
                    if (sLivePusher != null) {
                        sLivePusher.setVideoQuality(sResolution, true, false);
                    }
                }
            }
        });
        mVideoHigh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    sResolution = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;
                    if (sLivePusher != null) {
                        sLivePusher.setVideoQuality(sResolution, true, false);
                    }

                }
            }
        });
        mVideoSuper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    sResolution = TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION;
                    if (sLivePusher != null) {
                        sLivePusher.setVideoQuality(sResolution, true, false);
                    }
                }
            }
        });

        setStyle(mPortrait);
        setStyle(mLandScape);
        setStyle(mVideoSuper);
        setStyle(mVideoStand);
        setStyle(mVideoHigh);

        initViews();
        checkPusherStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sHasInitPusher) {
            stopLivePusher();
        }
    }

    private void checkPusherStatus() {
        if (sHasInitPusher) {
            ((Button) findViewById(R.id.v1livepusher_btn_play))
                    .setText(getString(R.string.v1livepusher_screen_push_tip));
            ((Button) findViewById(R.id.v1livepusher_btn_stop)).setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(sPushURL)) {
                mEditInputURL.setText(sPushURL);
            }


            if (sResolutionMode == V1Constants.SCREEN_MODE_PORTRAIT) {
                mPortrait.setChecked(true);
            } else if (sResolutionMode == V1Constants.SCREEN_MODE_LANDSCAPE) {
                mLandScape.setChecked(true);
            }

            if (sResolution == TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION) {
                mVideoStand.setChecked(true);
            } else if (sResolution == TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION) {
                mVideoHigh.setChecked(true);
            } else if (sResolution == TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION) {
                mVideoSuper.setChecked(true);
            }

        }
    }

    private void setStyle(RadioButton rb) {
        Drawable drawable = getResources().getDrawable(R.drawable.v1livepusher_screen_rb_icon_selector);
        //定义底部标签图片大小和位置
        drawable.setBounds(0, 0, 45, 45);
        //设置图片在文字的哪个方向
        rb.setCompoundDrawables(drawable, null, null, null);
        rb.setCompoundDrawablePadding(20);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == V1Constants.ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(V1Constants.INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            startLivePusher(scanURL);
        }
    }

    private void initViews() {
        if (mPusherPlayQRCodeFragment == null) {
            mPusherPlayQRCodeFragment = new QRCodeGenerateFragment();
        }
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
        findViewById(R.id.v1livepusher_ibtn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.v1livepusher_btn_normal_url) {
            fetchPusherURL();
        } else if (id == R.id.v1livepusher_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, V1Constants.ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.v1livepusher_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            if (!sHasInitPusher) {
                startLivePusher(url);
            }
        } else if (id == R.id.v1livepusher_btn_stop) {
            if (sHasInitPusher) {
                stopLivePusher();
            }
        } else if (id == R.id.v1livepusher_ibtn_qrcode) {
            if (sHasInitPusher) {
                mPusherPlayQRCodeFragment.setQRCodeURL(sQRCodePusherURL);
                mPusherPlayQRCodeFragment.toggle(getFragmentManager(), PUSHER_PLAY_QR_CODE_FRAGMENT);
            } else {
                Toast.makeText(this, getString(R.string.v1livepusher_screen_toast_please_start_up_push),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchPusherURL() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(V1Constants.URL_FETCH_PUSH_URL)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonRsp = new JSONObject(response.body().string());
                        final String pusherURLDefault = jsonRsp.optString(V1Constants.URL_PUSH);
                        String flvPlayURL = jsonRsp.optString(V1Constants.URL_PLAY_FLV);
                        sQRCodePusherURL = flvPlayURL;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mEditInputURL.setText(pusherURLDefault);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLivePusher(final String pushURL) {
        if (mLandScape.isChecked()) {
            sResolutionMode = V1Constants.SCREEN_MODE_LANDSCAPE;
        } else if (mPortrait.isChecked()) {
            sResolutionMode = V1Constants.SCREEN_MODE_PORTRAIT;
        }

        if (mVideoStand.isChecked()) {
            sResolution = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;
        } else if (mVideoHigh.isChecked()) {
            sResolution = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;
        } else {
            sResolution = TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION;
        }

        if (TextUtils.isEmpty(pushURL)) {
            Toast.makeText(mContext, getString(R.string.v1livepusher_screen_input_push_url), Toast.LENGTH_LONG).show();
        } else {
            PermissionUtils.permission(PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                    startPush(pushURL);
                }

                @Override
                public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                    ToastUtils.showShort(R.string.v1livepusher_screen_camera_storage_mic);
                    finish();
                }
            }).request();
        }
    }

    private class MyTXLivePushListener implements ITXLivePushListener {
        @Override
        public void onPushEvent(int i, Bundle bundle) {
            if (i == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
                sHasInitPusher = false;
                resetConfig();
                Toast.makeText(V1ScreenPushEntranceActivity.this, getString(R.string.v1livepusher_screen_cancel),
                        Toast.LENGTH_LONG).show();
                stopLivePusher();
            }
        }

        @Override
        public void onNetStatus(Bundle bundle) {
            Log.d(TAG, "onNetStatus : " + bundle.toString());
        }
    }

    private void resetConfig() {
        sQRCodePusherURL = "";
        sResolution = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;
        sPushURL = "";
        sResolutionMode = V1Constants.SCREEN_MODE_PORTRAIT;
    }

    private void startPush(String pushURL) {
        if (sLivePusher != null) {
            sLivePusher.setMute(true);
            sLivePusher.stopScreenCapture();
            sLivePusher.stopPusher();
            sLivePusher = null;
        }
        mTXLivePushConfig = new TXLivePushConfig();
        sLivePusher = new TXLivePusher(mContext);
        // 一般情况下不需要修改 config 的默认配置
        sLivePusher.setConfig(mTXLivePushConfig);


        sLivePusher.setPushListener(new MyTXLivePushListener());
        sLivePusher.setMute(false);
        sLivePusher.startScreenCapture();
        sLivePusher.setVideoQuality(sResolution, true, false);
        sPushURL = pushURL;
        int result = sLivePusher.startPusher(pushURL);
        if (result == V1Constants.PUSH_RESULT_OK) {
            sHasInitPusher = true;
            Toast.makeText(V1ScreenPushEntranceActivity.this, getString(R.string.v1livepusher_screen_push),
                    Toast.LENGTH_LONG).show();
            ((Button) findViewById(R.id.v1livepusher_btn_play))
                    .setText(getString(R.string.v1livepusher_screen_push_tip));
            ((Button) findViewById(R.id.v1livepusher_btn_stop)).setVisibility(View.VISIBLE);
        } else {
            resetConfig();
            sHasInitPusher = false;
        }
        if (TextUtils.isEmpty(sQRCodePusherURL)) {
            sQRCodePusherURL = getFlvUrl(pushURL);
        }
    }

    private void stopLivePusher() {
        if (sLivePusher != null) {
            sLivePusher.setMute(true);
            sLivePusher.stopScreenCapture();
            sLivePusher.stopPusher();
        }
        sHasInitPusher = false;
        resetConfig();
        mEditInputURL.setText("");
        Toast.makeText(V1ScreenPushEntranceActivity.this, getString(R.string.v1livepusher_screen_cancel),
                Toast.LENGTH_LONG).show();
        ((Button) findViewById(R.id.v1livepusher_btn_play))
                .setText(getString(R.string.v1livepusher_screen_start_screen_push));
        ((Button) findViewById(R.id.v1livepusher_btn_stop)).setVisibility(View.GONE);
    }

    private String getFlvUrl(String pushUrl) {
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
        String bseURL = array2[0].replace("livepush", "liveplay");
        return "http://" + bseURL + ".flv";
    }
}
