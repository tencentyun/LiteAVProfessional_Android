package com.tencent.liteav.trtcdemo.ui.widget.settingitem.common;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;

public class EditTextInputItem extends LinearLayout {

    private TextView mTextTitle;
    private EditText mEditText;
    private String   mTitle;
    private String   mInputText;

    public EditTextInputItem(Context context, String title, String inputText) {
        super(context);
        mTitle = title;
        mInputText = inputText;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_edittext_input, this, true);
        mTextTitle = findViewById(R.id.title);
        mEditText = findViewById(R.id.et_input);
        mTextTitle.setText(mTitle);
        if (!TextUtils.isEmpty(mInputText)) {
            mEditText.setText(mInputText);
        }
        mEditText.setSaveEnabled(false);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    public void setText(String text) {
        mInputText = text;
        if (!TextUtils.isEmpty(mInputText)) {
            mEditText.setText(mInputText);
        }
    }

    public String getText() {
        return mEditText.getText().toString();
    }

    public void setInputType(int type) {
        mEditText.setInputType(type);
    }
}
