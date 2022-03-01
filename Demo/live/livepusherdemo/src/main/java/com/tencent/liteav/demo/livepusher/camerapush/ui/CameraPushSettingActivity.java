package com.tencent.liteav.demo.livepusher.camerapush.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.liteav.demo.livepusher.R;
import com.tencent.liteav.demo.livepusher.camerapush.ui.model.SettingInfo;
import com.tencent.liteav.demo.livepusher.camerapush.ui.view.VariableRadioSelectorView;

public class CameraPushSettingActivity extends Activity {

    private VariableRadioSelectorView mRenderTypeRadioSelectView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livepusher_activity_live_pusher_setting);
        initViews();
    }

    private void initViews() {
        final SettingInfo settingInfo = (SettingInfo) getIntent().getSerializableExtra(Constants.INTENT_SETTING_RESULT);
        ((TextView) findViewById(R.id.livepusher_title_textview)).setText(R.string.livepusher_setting);
        findViewById(R.id.livepusher_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.livepusher_ibtn_right).setVisibility(View.GONE);

        mRenderTypeRadioSelectView = findViewById(R.id.livepusher_rsv_render_type);
        mRenderTypeRadioSelectView.setOrientation(LinearLayout.VERTICAL);
        String[] renderTypeData;
        try {
            Class.forName("com.tencent.live2.impl.V2TXLiveDefInner");
            renderTypeData = getResources().getStringArray(R.array.livepusher_render_type_with_surface);
        } catch (ClassNotFoundException e) {
            renderTypeData = getResources().getStringArray(R.array.livepusher_render_type_without_surface);
        }

        mRenderTypeRadioSelectView.setData(renderTypeData, settingInfo.renderType);
    }

    @Override
    public void finish() {
        SettingInfo settingInfo = new SettingInfo();
        settingInfo.renderType = mRenderTypeRadioSelectView.getSelectPosition();
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_SETTING_RESULT, settingInfo);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
