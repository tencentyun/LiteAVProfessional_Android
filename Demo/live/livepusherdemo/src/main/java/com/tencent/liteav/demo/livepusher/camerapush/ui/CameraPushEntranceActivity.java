package com.tencent.liteav.demo.livepusher.camerapush.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.livepusher.R;
import com.tencent.liteav.demo.livepusher.camerapush.ui.model.SettingInfo;
import com.tencent.liteav.demo.livepusher.camerapush.ui.view.LoadingFragment;
import com.tencent.liteav.demo.livepusher.camerapush.ui.view.PusherProtocolSelectFragment;
import com.tencent.liteav.demo.livepusher.camerapush.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 摄像头推流入口页面，主要用于生成推流地址
 */
public class CameraPushEntranceActivity extends Activity {

    private static final int    ACTIVITY_SCAN_REQUEST_CODE    = 1;
    private static final int    ACTIVITY_SETTING_REQUEST_CODE = 2;
    private static final int    REQUEST_CODE                  = 100;
    private static final String PUSHER_PROTOCOL_FRAGMENT      = "push_protocol_fragment";

    private Context                      mContext;
    private EditText                     mEditInputURL;
    private PusherProtocolSelectFragment mFragmentProtocol;
    private SettingInfo                  mSettingInfo = new SettingInfo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.livepusher_activity_live_pusher_entrance);
        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(Constants.INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            startLivePusher(scanURL);
        } else if (requestCode == ACTIVITY_SETTING_REQUEST_CODE && resultCode == RESULT_OK) {
            mSettingInfo = (SettingInfo) data.getSerializableExtra(Constants.INTENT_SETTING_RESULT);
        }
    }

    private void initViews() {
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
        findViewById(R.id.livepusher_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.livepusher_ibtn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });

        if (mFragmentProtocol == null) {
            mFragmentProtocol = new PusherProtocolSelectFragment();
            mFragmentProtocol.setListener(new PusherProtocolSelectFragment.OnSelectListener() {
                @Override
                public void onSelected(int index) {
                    if (index == 0) {
                        fetchPusherURL();
                    } else {
                        generateRTCURL();
                    }
                }
            });
        }

        if (AppRuntime.get().isDebug()) {
            findViewById(R.id.livepusher_btn_setting).setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.livepusher_btn_normal_url) {
            if (mFragmentProtocol != null) {
                mFragmentProtocol.toggle(getFragmentManager(), PUSHER_PROTOCOL_FRAGMENT);
            }
        } else if (id == R.id.livepusher_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.livepusher_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            startLivePusher(url);
        } else if (id == R.id.livepusher_btn_setting) {
            Intent intent = new Intent(mContext, CameraPushSettingActivity.class);
            intent.putExtra(Constants.INTENT_SETTING_RESULT, mSettingInfo);
            startActivityForResult(intent, ACTIVITY_SETTING_REQUEST_CODE);
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
                        fragment.dismissAllowingStateLoss();
                        startLivePusher(pusherURLDefault, rtmpPlayURL, flvPlayURL, hlsPlayURL, "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLivePusher(String pushURL) {
        if (TextUtils.isEmpty(pushURL)) {
            Toast.makeText(mContext, getString(R.string.livepusher_input_push_url), Toast.LENGTH_LONG).show();
        } else {
            Pattern pattern = Pattern.compile("[1-9]\\d{5}");
            boolean isRTC = pattern.matcher(pushURL).matches();
            if (isRTC) {
                String userId = String.valueOf(new Random().nextInt(899999) + 100000);
                String streamId = pushURL;
                String pushUrl = "trtc://cloud.tencent.com/push/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID + "&userid=" + userId + "&usersig=" + GenerateTestUserSig.genTestUserSig(userId);
                String rtmpPlayURL = "rtmp://3891.liveplay.myqcloud.com/live/" + pushURL;
                String flvPlayURL = "http://3891.liveplay.myqcloud.com/live/" + pushURL + ".flv";
                String hlsPlayURL = "http://3891.liveplay.myqcloud.com/live/" + pushURL + ".m3u8";
                startLivePusher(pushUrl, rtmpPlayURL, flvPlayURL, hlsPlayURL, streamId);

            } else {
                String streamId = getStreamIdByPushUrl(pushURL);
                if (TextUtils.isEmpty(streamId)) {
                    startLivePusher(pushURL, "", "", "", "");
                } else {
                    String rtmpPlayURL = "rtmp://3891.liveplay.myqcloud.com/live/" + streamId;
                    String flvPlayURL = "http://3891.liveplay.myqcloud.com/live/" + streamId + ".flv";
                    String hlsPlayURL = "http://3891.liveplay.myqcloud.com/live/" + streamId + ".m3u8";
                    startLivePusher(pushURL, rtmpPlayURL, flvPlayURL, hlsPlayURL, "");
                }
            }
        }
    }

    private String getStreamIdByPushUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String[] array = url.split("/");
        if (array.length < 5) {
            return "";
        }
        if (TextUtils.isEmpty(array[4])) {
            return "";
        }

        if (array[4].contains("?")) {
            return array[4].substring(0, array[4].indexOf("?"));
        } else {
            return array[4];
        }
    }

    private void startLivePusher(String pushURL, String rtmpPlayURL, String flvPlayURL, String hlsPlayURL, String rtcPlayURL) {
        if (!isPushUrlLegal(pushURL)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.livepusher_push_url_illegal), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        Intent intent = new Intent(mContext, CameraPushMainActivity.class);
        intent.putExtra(Constants.INTENT_URL_PUSH, pushURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_RTMP, rtmpPlayURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_FLV, flvPlayURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_HLS, hlsPlayURL);
        intent.putExtra(Constants.INTENT_URL_PLAY_RTC, rtcPlayURL);
        intent.putExtra(Constants.INTENT_SETTING_RESULT, mSettingInfo);
        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.URL_PRODUCT_DOCUMENT));
        IntentUtils.safeStartActivity(this, intent);
    }

    private void generateRTCURL() {
        String pushUserId = String.valueOf(new Random().nextInt(899999) + 100000);
        String streamId = String.valueOf(new Random().nextInt(899999) + 100000);
        String pushUrl = "trtc://cloud.tencent.com/push/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID
                + "&userid=" + pushUserId + "&usersig=" + GenerateTestUserSig.genTestUserSig(pushUserId);
        String rtmpPlayURL = "rtmp://3891.liveplay.myqcloud.com/live/" + streamId;
        String flvPlayURL = "http://3891.liveplay.myqcloud.com/live/" + streamId + ".flv";
        String hlsPlayURL = "http://3891.liveplay.myqcloud.com/live/" + streamId + ".m3u8";
        String playUserId = String.valueOf(new Random().nextInt(899999) + 100000);
        String rtcPlayURL = "trtc://cloud.tencent.com/play/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID
                + "&userid" + "=" + playUserId + "&usersig=" + GenerateTestUserSig.genTestUserSig(playUserId);

        startLivePusher(pushUrl, rtmpPlayURL, flvPlayURL, hlsPlayURL, rtcPlayURL);
    }

    private boolean isPushUrlLegal(String pushUrl) {
        if (TextUtils.isEmpty(pushUrl)) {
            return false;
        }
        String[] urls = pushUrl.split("###");
        if (urls.length <= 0) {
            return false;
        }
        return Utils.checkLegalForPushUrl(urls[0]);
    }
}