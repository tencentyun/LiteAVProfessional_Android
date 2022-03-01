package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;

/**
 * 封装 带编辑框 和 按钮的 Item，实例如SEI消息发送
 */
public abstract class AbsEditTextSendItem extends AbsBaseItem {

    public  TextView     mTextTitle;
    public  LinearLayout mLinearItem;
    private EditText     mEditMessage;
    private Button       mButtonSend;
    private String       mTitle;
    private String       mButtonText;

    public AbsEditTextSendItem(Context context, boolean debug, String title, String btnText) {
        super(context, debug);
        mTitle = title;
        mButtonText = btnText;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_edittext, this, true);
        mTextTitle = findViewById(R.id.title);
        mLinearItem = findViewById(R.id.ll_item);
        mEditMessage = findViewById(R.id.et_message);
        mButtonSend = findViewById(R.id.btn_send);
        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }

        if (!TextUtils.isEmpty(mButtonText)) {
            mButtonSend.setText(mButtonText);
        }
        mEditMessage.setSaveEnabled(false);
        mButtonSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditMessage.getText().toString();
                send(message);
            }
        });
    }

    public void setInputText(String text) {
        mEditMessage.setText(text);
    }

    public void setButtonText(String text) {
        mButtonSend.setText(text);
    }

    public void setInputType(int type) {
        mEditMessage.setInputType(type);
    }

    public abstract void send(String msg);
}