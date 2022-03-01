package com.tencent.liteav.v1liveplayerdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.v1liveplayerdemo.model.SettingInfo;

/**
 * 直播拉流入口页面，主要用于获取拉流地址
 */
public class V1LivePlayerEntranceActivity extends Activity {

    private static final int ACTIVITY_SCAN_REQUEST_CODE = 1;
    private static final int ACTIVITY_SETTING_REQUEST_CODE = 2;

    private Context  mContext;
    private EditText mEditInputURL;
    private SettingInfo mSettingInfo = new SettingInfo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.v1liveplayer_entrance_activity);
        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(Constants.INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            startLivePlayer(scanURL);
        } else if (requestCode == ACTIVITY_SETTING_REQUEST_CODE && resultCode == RESULT_OK) {
            mSettingInfo = (SettingInfo) data.getSerializableExtra(Constants.INTENT_SETTING_RESULT);
        }
    }

    private void initViews() {
        mEditInputURL = (EditText) findViewById(R.id.v1liveplayer_et_input_url);
        mEditInputURL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getAction() == KeyEvent.ACTION_UP)) {
                    String url = mEditInputURL.getText().toString().trim();
                    if (TextUtils.isEmpty(url)) {
                        Toast.makeText(mContext, R.string.v1liveplayer_intput_url, Toast.LENGTH_LONG).show();
                    } else {
                        startLivePlayer(url);
                    }
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.v1liveplayer_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.v1liveplayer_ibtn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });
        if (AppRuntime.get().isDebug()) {
            findViewById(R.id.v1liveplayer_btn_setting).setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.v1liveplayer_btn_normal_url) {
            startLivePlayer(Constants.NORMAL_PLAY_URL);
        } else if (id == R.id.v1liveplayer_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.v1liveplayer_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(mContext, R.string.v1liveplayer_intput_url, Toast.LENGTH_LONG).show();
            } else {
                startLivePlayer(url);
            }
        } else if (id == R.id.v1liveplayer_btn_realtime_play) {
            String url = mEditInputURL.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(mContext, R.string.v1liveplayer_intput_url, Toast.LENGTH_LONG).show();
            } else {
                boolean isLiveRTMP = url.startsWith(Constants.URL_PREFIX_RTMP);
                boolean hasBizid = url.contains(Constants.URL_BIZID);
                // "rtmp://" 开头且包含关键字 "bizid" 才认为是低延迟拉流地址
                if (isLiveRTMP && hasBizid) {
                    startLivePlayer(url, true);
                } else {
                    Toast.makeText(mContext, R.string.v1liveplayer_not_realtime_url, Toast.LENGTH_LONG).show();
                }
            }
        } else if (id == R.id.v1liveplayer_btn_setting) {
            Intent intent = new Intent(mContext, V1LivePlayerSettingActivity.class);
            intent.putExtra(Constants.INTENT_SETTING_RESULT, mSettingInfo);
            startActivityForResult(intent, ACTIVITY_SETTING_REQUEST_CODE);
        }
    }

    private void startLivePlayer(String url) {
        startLivePlayer(url, false);
    }

    private void startLivePlayer(String url, boolean isRealtime) {
        Intent intent = new Intent(mContext, V1LivePlayerMainActivity.class);
        if (isRealtime) {
            intent.putExtra(Constants.INTENT_ACTIVITY_TYPE, Constants.ACTIVITY_TYPE_REALTIME_PLAY);
        }
        intent.putExtra(Constants.INTENT_SETTING_RESULT, mSettingInfo);
        intent.putExtra(Constants.INTENT_URL, url);
        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.LIVE_PLAYER_DOCUMENT_URL));
        IntentUtils.safeStartActivity(this, intent);
    }
}
