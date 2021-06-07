package com.tencent.liteav.demo.livepusher.screenpush;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePusherImpl;

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

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK;
import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_WARNING_SCREEN_CAPTURE_START_FAILED;

public class ScreenPushEntranceActivity extends Activity {

    public static final String URL_FETCH_PUSH_URL = "https://lvb.qcloud.com/weapp/utils/get_test_pushurl";

    /**
     * QRCodeScanActivity完成扫描后，传递过来的结果的KEY
     */
    public static final String INTENT_SCAN_RESULT   = "SCAN_RESULT";

    public static final String URL_PUSH        = "url_push";       // RTMP 推流地址
    public static final String URL_PLAY_RTMP   = "url_play_rtmp";  // RTMP 播放地址
    public static final String URL_PLAY_FLV    = "url_play_flv";   // FLA  播放地址
    public static final String URL_PLAY_HLS    = "url_play_hls";   // HLS  播放地址
    public static final String URL_PLAY_ACC    = "url_play_acc";   // RTMP 加速流地址

    private static final int ACTIVITY_SCAN_REQUEST_CODE = 1;
    private static final int REQUEST_CODE = 100;
    private Context mContext;
    private EditText mEditInputURL;

    private RadioButton mLandScape;
    private RadioButton mPortrait;
    private RadioButton mVideoSuper;
    private RadioButton mVideoStand;
    private RadioButton mVideoHigh;

    private QRCodeGenerateFragment mPusherPlayQRCodeFragment;
    private boolean mHasInitPusher = false;
    private String mQRCodePusherURL;
    private static final String PUSHER_PLAY_QR_CODE_FRAGMENT = "push_play_qr_code_fragment";
    private V2TXLiveDef.V2TXLiveVideoResolution mResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
    private V2TXLiveDef.V2TXLiveVideoResolutionMode mResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
    private V2TXLivePusher mLivePusher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livepusherscreen_activity_entrance);
        mContext = this;

        mPortrait = (RadioButton) findViewById(R.id.rb_portrait);
        mLandScape = (RadioButton) findViewById(R.id.rb_landscape);
        mVideoSuper = findViewById(R.id.rb_video_super);
        mVideoStand = findViewById(R.id.rb_video_stand);
        mVideoHigh = findViewById(R.id.rb_video_high);

        setStyle(mPortrait);
        setStyle(mLandScape);
        setStyle(mVideoSuper);
        setStyle(mVideoStand);
        setStyle(mVideoHigh);

        initViews();
        checkPublishPermission();
    }

    private void setStyle(RadioButton rb) {
        Drawable drawable = getResources().getDrawable(R.drawable.livepusher_screen_rb_icon_selector);
        //定义底部标签图片大小和位置
        drawable.setBounds(0, 0, 45, 45);
        //设置图片在文字的哪个方向
        rb.setCompoundDrawables(drawable, null, null, null);
        rb.setCompoundDrawablePadding(20);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            startLivePusher(scanURL);
        }
    }

    private void initViews() {
        if (mPusherPlayQRCodeFragment == null) {
            mPusherPlayQRCodeFragment = new QRCodeGenerateFragment();
        }
        mEditInputURL = findViewById(R.id.livepusher_et_input_url);
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
        findViewById(R.id.livepusher_ibtn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.livepusher_btn_normal_url) {
            fetchPusherURL();
        } else if (id == R.id.livepusher_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.livepusher_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            if (!mHasInitPusher) {
                startLivePusher(url);
            }
        } else if (id == R.id.livepusher_btn_stop) {
            if (mHasInitPusher) {
                stopLivePusher();
            }
        } else if (id == R.id.livepusher_ibtn_qrcode) {
            if (mHasInitPusher) {
                mPusherPlayQRCodeFragment.setQRCodeURL(mQRCodePusherURL);
                mPusherPlayQRCodeFragment.toggle(getFragmentManager(), PUSHER_PLAY_QR_CODE_FRAGMENT);
            } else {
                Toast.makeText(this, getString(R.string.livepusher_screen_toast_please_start_up_push), Toast.LENGTH_LONG).show();
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
                .url(URL_FETCH_PUSH_URL)
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
                        String pusherURLDefault = jsonRsp.optString(URL_PUSH);
                        String flvPlayURL = jsonRsp.optString(URL_PLAY_FLV);
                        mQRCodePusherURL = flvPlayURL;
                        mEditInputURL.setText(pusherURLDefault);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLivePusher(String pushURL) {
        if (mLandScape.isChecked()) {
            mResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModeLandscape;
        } else if (mPortrait.isChecked()) {
            mResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
        }

        if (mVideoSuper.isChecked()) {
            mResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x480;
        } else if (mVideoStand.isChecked()) {
            mResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
        } else {
            mResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720;
        }

        if (TextUtils.isEmpty(pushURL)) {
            Toast.makeText(mContext, getString(R.string.livepusher_screen_input_push_url), Toast.LENGTH_LONG).show();
        } else {
            startLivePusher(pushURL, "", "", "", "");
        }
    }

    private void startLivePusher(String pushURL, String rtmpPlayURL, String flvPlayURL, String hlsPlayURL, String realtimePlayURL) {
        mLivePusher = new V2TXLivePusherImpl(mContext, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP);
        mLivePusher.setObserver(new MyTXLivePusherObserver());
        mLivePusher.startMicrophone();
        mLivePusher.startScreenCapture();
        mLivePusher.setVideoQuality(mResolution, mResolutionMode);
        int result = mLivePusher.startPush(pushURL);
        if (result == V2TXLIVE_OK) {
            mHasInitPusher = true;
            ((Button)findViewById(R.id.livepusher_btn_play)).setText(getString(R.string.livepusher_screen_push_tip));
            ((Button)findViewById(R.id.livepusher_btn_stop)).setVisibility(View.VISIBLE);
        } else {
            mHasInitPusher = false;
        }
    }

    private class MyTXLivePusherObserver extends V2TXLivePusherObserver {

        @Override
        public void onWarning(int code, String msg, Bundle extraInfo) {
            if (code == V2TXLIVE_WARNING_SCREEN_CAPTURE_START_FAILED) {
                mHasInitPusher = false;
                Toast.makeText(ScreenPushEntranceActivity.this, getString(R.string.livepusher_screen_cancel), Toast.LENGTH_LONG).show();
                stopLivePusher();
            }
        }
    }

    private void stopLivePusher() {
        if (mLivePusher != null) {
            mLivePusher.stopMicrophone();
            mLivePusher.stopScreenCapture();
            mLivePusher.stopPush();
            mHasInitPusher = false;
            ((Button)findViewById(R.id.livepusher_btn_play)).setText(getString(R.string.livepusher_screen_start_screen_push));
            ((Button)findViewById(R.id.livepusher_btn_stop)).setVisibility(View.GONE);
        }
    }

    private boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_CODE);
                return false;
            }
        }
        return true;
    }
}
