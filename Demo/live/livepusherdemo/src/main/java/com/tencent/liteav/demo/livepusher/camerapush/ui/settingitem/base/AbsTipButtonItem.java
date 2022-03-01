package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.liteav.demo.livepusher.R;


/**
 * 带提示的Button Item
 */
public abstract class AbsTipButtonItem extends AbsBaseItem {

    public  TextView mTextTitle;
    private Button   mButton;
    public  String   mButtonText;
    public  String   mTitle;

    public AbsTipButtonItem(Context context, boolean debug, String title, String btnText) {
        super(context, debug);
        mTitle = title;
        mButtonText = btnText;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.livepusher_item_setting_tip_button, this, true);
        mTextTitle = findViewById(R.id.title);
        mButton = findViewById(R.id.btn_send);
        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }
        if (!TextUtils.isEmpty(mButtonText)) {
            mButton.setText(mButtonText);
        }
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClicked();
            }
        });
    }

    public void setButtonText(final String text) {
        mButton.post(new Runnable() {
            @Override
            public void run() {
                mButton.setText(text);
            }
        });
    }

    public abstract void onClicked();
}
