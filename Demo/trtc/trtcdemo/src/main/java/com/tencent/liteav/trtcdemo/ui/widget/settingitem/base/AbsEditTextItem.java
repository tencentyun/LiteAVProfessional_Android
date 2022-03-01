package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;

public class AbsEditTextItem extends AbsBaseItem {

    private TextView mTextTitle;
    private EditText mEditText;
    private String   mTitle;
    private String   mInputText;

    public AbsEditTextItem(Context context, boolean debugFlag, String title) {
        super(context, debugFlag);
        mTitle = title;
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
    }
    
    public void setTitle(String title){
        if (!TextUtils.isEmpty(title)) {
            mTitle = title;
            mTextTitle.setText(mTitle);
        }
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
