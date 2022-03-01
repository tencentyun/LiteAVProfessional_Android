package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;

public abstract class AbsButtonItem extends AbsBaseItem {

    private Button mButton;
    public  String mButtonText;

    public AbsButtonItem(Context context, boolean debug, String btnText) {
        super(context, debug);
        mButtonText = btnText;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_button, this, true);

        mButton = findViewById(R.id.btn_click);
        if (!TextUtils.isEmpty(mButtonText)) {
            mButton.setText(mButtonText);
        }
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClicked();
            }
        });
    }

    public void setText(final String text) {
        mButton.post(new Runnable() {
            @Override
            public void run() {
                mButton.setText(text);
            }
        });
    }

    public abstract void onClicked();
}
