package com.tencent.liteav.liveplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.rtmp.TXLivePlayConfig;


/**
 * 直播拉流入口页面，主要用于设置PlayConfig
 */
public class V1LivePlayerEntranceActivity extends Activity {

    private Context mContext;
    private TXLivePlayConfig mPlayConfig;
    private RadioGroup mRgRenderType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.liveplayer_activity_v1_live_player_entrance);
        mPlayConfig = new TXLivePlayConfig();
        initViews();
    }

    private void initViews() {
        TextView textTitle = findViewById(R.id.tv_topBar_title);
        textTitle.setText("V1直播播放");

        findViewById(R.id.btn_topBar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_topBar_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });
        mRgRenderType = findViewById(R.id.rg_v1_entrance_renderType);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_v1_entrance_startPlay) {
            startLivePlayer();
        } else if (id == R.id.cb_v1_entrance_setAutoAdjustCacheTime) {
            CheckBox checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                mPlayConfig.setAutoAdjustCacheTime(true);
            } else {
                mPlayConfig.setAutoAdjustCacheTime(false);
            }
        } else if (id == R.id.btn_v1_entrance_setCacheTime) {
            EditText editText = findViewById(R.id.et_v1_entrance_setCacheTime);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setCacheTime(Float.valueOf(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_entrance_setMaxAutoAdjustCacheTime) {
            EditText editText;
            editText = findViewById(R.id.et_v1_entrance_setMaxAutoAdjustCacheTime);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setMaxAutoAdjustCacheTime(Float.valueOf(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_entrance_setMinAutoAdjustCacheTime) {
            EditText editText;
            editText = findViewById(R.id.et_v1_entrance_setMinAutoAdjustCacheTime);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setMinAutoAdjustCacheTime(Float.valueOf(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_entrance_setVideoBlockThreshold) {
            EditText editText;
            editText = findViewById(R.id.et_v1_entrance_setVideoBlockThreshold);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setVideoBlockThreshold(Integer.valueOf(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_entrance_setConnectRetryCount) {
            EditText editText;
            editText = findViewById(R.id.et_v1_entrance_setConnectRetryCount);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setConnectRetryCount(Integer.valueOf(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_entrance_setConnectRetryInterval) {
            EditText editText;
            editText = findViewById(R.id.et_v1_entrance_setConnectRetryInterval);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setConnectRetryInterval(Integer.valueOf(editText.getText().toString()));
            }
        } else if (id == R.id.cb_v1_entrance_setEableMessage) {
            CheckBox checkBox;
            checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                mPlayConfig.setEnableMessage(true);
            } else {
                mPlayConfig.setEnableMessage(false);
            }
        } else if (id == R.id.cb_v1_entrance_setEnableAEC) {
            CheckBox checkBox;
            checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                mPlayConfig.setEnableAEC(true);
            } else {
                mPlayConfig.setEnableAEC(false);
            }
        } else if (id == R.id.cb_v1_entrance_setEnableMetaData) {
            CheckBox checkBox;
            checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                mPlayConfig.setEnableMetaData(true);
            } else {
                mPlayConfig.setEnableMetaData(false);
            }
        } else if (id == R.id.btn_v1_entrance_setFlvSessionKey) {
            EditText editText;
            editText = findViewById(R.id.et_v1_entrance_setFlvSessionKey);
            mPlayConfig.setFlvSessionKey(editText.getText().toString());
        }
    }

    private void startLivePlayer() {
        Intent intent = new Intent(mContext, V1LivePlayerMainActivity.class);
        intent.putExtra("config", mPlayConfig);
        int renderType = 1;
        int checkedRadioButtonId = mRgRenderType.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rb_v1_entrance_TXCloudVideoView) {
            renderType = 1;
        } else if (checkedRadioButtonId == R.id.rb_v1_entrance_Surface) {
            renderType = 2;
        } else if (checkedRadioButtonId == R.id.rb_v1_entrance_customRenderTexture) {
            renderType = 3;
        } else if (checkedRadioButtonId == R.id.rb_v1_entrance_customRenderI420) {
            renderType = 4;
        }
        intent.putExtra(Constants.RENDER_TYPE, renderType);
        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.LIVE_PLAYER_DOCUMENT_URL));
        startActivity(intent);
    }
}
