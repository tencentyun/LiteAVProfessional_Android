package com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;

public abstract class MixCustomIdItem extends LinearLayout {
    private EditText mMixCustomIdEt;
    private Button   mMixCustomIdBtn;

    public MixCustomIdItem(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_mix_custom_id_layout, this);
        mMixCustomIdEt = findViewById(R.id.trtcdemo_mix_custom_id_et);
        mMixCustomIdBtn = findViewById(R.id.trtcdemo_mix_custom_id_btn);
        mMixCustomIdBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String mixId = mMixCustomIdEt.getText().toString().trim();
                VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
                videoConfig.setMixStreamId(mixId);
                onClickSet(mixId);
            }
        });
    }

    public abstract void onClickSet(String mixId);
}
