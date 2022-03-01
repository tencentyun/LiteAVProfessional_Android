package com.tencent.liteav.demo.liveplayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.tencent.liteav.demo.liveplayer.R;
import com.tencent.liteav.demo.liveplayer.ui.model.SettingInfo;
import com.tencent.liteav.demo.liveplayer.ui.view.VariableRadioSelectorView;
import com.tencent.liteav.demo.liveplayer.ui.view.VariableRadioSelectorView.RadioButton;
import com.tencent.liteav.demo.liveplayer.ui.view.VariableRadioSelectorView.RadioSelectListener;

public class LivePlayerSettingActivity extends Activity {

    private VariableRadioSelectorView mRenderTypeRadioSelectView;
    private VariableRadioSelectorView mCustomRenderTypeRadioSelectView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liveplayer_activity_live_player_setting);
        initViews();
    }

    private void initViews() {
        final SettingInfo settingInfo = (SettingInfo) getIntent().getSerializableExtra(Constants.INTENT_SETTING_RESULT);
        ((TextView) findViewById(R.id.liveplayer_title_textview)).setText(R.string.liveplayer_setting);
        findViewById(R.id.liveplayer_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.liveplayer_ibtn_right).setVisibility(View.GONE);

        mRenderTypeRadioSelectView = findViewById(R.id.liveplayer_rsv_render_type);
        mRenderTypeRadioSelectView.setOrientation(LinearLayout.VERTICAL);
        String[] renderTypeData;
        try {
            Class.forName("com.tencent.live2.impl.V2TXLiveDefInner");
            renderTypeData = getResources().getStringArray(R.array.liveplayer_render_type_with_surface);
        } catch (ClassNotFoundException e) {
            renderTypeData = getResources().getStringArray(R.array.liveplayer_render_type_without_surface);
        }

        mRenderTypeRadioSelectView.setData(renderTypeData, settingInfo.renderType);
        mRenderTypeRadioSelectView.setRadioSelectListener(new RadioSelectListener() {
            @Override
            public void onChecked(int prePosition, RadioButton preRadioButton, int curPosition,
                    RadioButton curRadioButton) {
                if (curPosition == SettingInfo.RENDER_TYPE_CUSTOM) {
                    mCustomRenderTypeRadioSelectView.setVisibility(View.VISIBLE);
                } else {
                    mCustomRenderTypeRadioSelectView.setVisibility(View.GONE);
                }
            }
        });

        mCustomRenderTypeRadioSelectView = findViewById(R.id.liveplayer_rsv_custom_render_type);
        mCustomRenderTypeRadioSelectView.setOrientation(LinearLayout.VERTICAL);
        mCustomRenderTypeRadioSelectView
                .setData(getResources().getStringArray(R.array.liveplayer_custom_render_type),
                        settingInfo.customRenderType);
        if (settingInfo.renderType == SettingInfo.RENDER_TYPE_CUSTOM) {
            mCustomRenderTypeRadioSelectView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        SettingInfo settingInfo = new SettingInfo();
        settingInfo.renderType = mRenderTypeRadioSelectView.getSelectPosition();
        settingInfo.customRenderType = mCustomRenderTypeRadioSelectView.getSelectPosition();
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_SETTING_RESULT, settingInfo);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
