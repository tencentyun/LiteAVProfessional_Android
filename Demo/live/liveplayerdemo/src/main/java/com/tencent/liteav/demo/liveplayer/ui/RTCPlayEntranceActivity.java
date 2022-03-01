package com.tencent.liteav.demo.liveplayer.ui;

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

import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.liveplayer.R;

import java.util.regex.Pattern;

public class RTCPlayEntranceActivity extends Activity {

    private static final int ACTIVITY_SCAN_REQUEST_CODE = 1;

    private Context  mContext;
    private EditText mEditInputURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.liveplayer_activity_rtc_player_entrance);
        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(Constants.INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            if (!TextUtils.isEmpty(scanURL) && (isRTCStreamId(scanURL) || isRTCURL(scanURL))) {
                startLivePlayer(scanURL);
            } else {
                Toast.makeText(mContext, R.string.liveplayer_intput_url, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViews() {
        mEditInputURL = (EditText) findViewById(R.id.liveplayer_et_input_url);
        mEditInputURL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getAction() == KeyEvent.ACTION_UP)) {
                    String url = mEditInputURL.getText().toString().trim();
                    if (!TextUtils.isEmpty(url) && (isRTCStreamId(url) || isRTCURL(url))) {
                        startLivePlayer(url);
                    } else {
                        Toast.makeText(mContext, R.string.liveplayer_intput_url, Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.liveplayer_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.liveplayer_ibtn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.liveplayer_btn_normal_url) {
            startLivePlayer(Constants.NORMAL_PLAY_URL);
        } else if (id == R.id.liveplayer_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.liveplayer_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            if (!TextUtils.isEmpty(url) && (isRTCStreamId(url) || isRTCURL(url))) {
                startLivePlayer(url);
            } else {
                Toast.makeText(mContext, R.string.liveplayer_intput_url, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLivePlayer(String url) {
        Intent intent = new Intent(mContext, RTCPlayActivity.class);
        intent.putExtra(Constants.INTENT_URL, url);
        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.LIVE_PLAYER_DOCUMENT_URL));
        IntentUtils.safeStartActivity(this, intent);
    }

    private boolean isRTCStreamId(String streamId) {
        Pattern pattern = Pattern.compile("\\d{6}");
        return pattern.matcher(streamId).matches();
    }

    private boolean isRTCURL(String url) {
        Pattern pattern = Pattern.compile("trtc://.+?");
        return pattern.matcher(url).matches();
    }
}

