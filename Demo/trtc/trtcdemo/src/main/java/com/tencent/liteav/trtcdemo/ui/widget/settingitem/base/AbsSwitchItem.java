package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;

/**
 * 封装开关Item
 */
public abstract class AbsSwitchItem extends AbsBaseItem {

    private TextView     mTextTitle;
    private SwitchCompat mSwitchCompat;
    private String       mTitle;

    public AbsSwitchItem(Context context, boolean debug, String title) {
        super(context, debug);
        mTitle = title;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_switch, this, true);
        mTextTitle = findViewById(R.id.title);
        mSwitchCompat = findViewById(R.id.cb_item);
        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }
        mSwitchCompat.setSaveEnabled(false);
        mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChecked();
            }
        });
    }

    public AbsSwitchItem setCheck(final boolean check) {
        mSwitchCompat.setChecked(check);
        return this;
    }

    public boolean getChecked() {
        return mSwitchCompat.isChecked();
    }

    public abstract void onChecked();
}
