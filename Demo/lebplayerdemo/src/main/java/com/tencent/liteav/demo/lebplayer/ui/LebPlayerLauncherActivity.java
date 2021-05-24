package com.tencent.liteav.demo.lebplayer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.lebplayer.R;

/**
 * 快直播拉流入口页面，主要用于获取拉流地址
 */
public class LebPlayerLauncherActivity extends Activity {

    private static final int ACTIVITY_SCAN_REQUEST_CODE = 1;

    private Context  mContext;
    private EditText mEditInputURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.lebplayer_launcher);
        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String scanURL = data.getStringExtra(Constants.INTENT_SCAN_RESULT);
            mEditInputURL.setText(scanURL);
            startLEBPlayer(scanURL);
        }
    }

    private void initViews() {
        mEditInputURL = (EditText) findViewById(R.id.lebplayer_et_input_url);
        mEditInputURL.setText(Constants.NORMAL_PLAY_URL);
        mEditInputURL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getAction() == KeyEvent.ACTION_UP)) {
                    String url = mEditInputURL.getText().toString().trim();
                    if (TextUtils.isEmpty(url) || !url.startsWith("webrtc://")) {
                        Toast.makeText(mContext, R.string.lebplayer_intput_leb_url, Toast.LENGTH_LONG).show();
                    } else {
                        startLEBPlayer(url);
                    }
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.lebplayer_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.lebplayer_ibtn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.lebplayer_btn_qr_code_scan) {
            Intent intent = new Intent(mContext, QRCodeScanActivity.class);
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE);
        } else if (id == R.id.lebplayer_btn_play) {
            String url = mEditInputURL.getText().toString().trim();
            if (TextUtils.isEmpty(url) || !url.startsWith("webrtc://")) {
                Toast.makeText(mContext, R.string.lebplayer_intput_leb_url, Toast.LENGTH_LONG).show();
            } else {
                startLEBPlayer(url);
            }
        }
    }

    private void startLEBPlayer(String url) {
        Intent intent = new Intent(mContext, LebPlayerActivity.class);
        intent.putExtra(Constants.INTENT_URL, url);
        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.LEB_PLAYER_DOCUMENT_URL));
        startActivity(intent);
    }
}
