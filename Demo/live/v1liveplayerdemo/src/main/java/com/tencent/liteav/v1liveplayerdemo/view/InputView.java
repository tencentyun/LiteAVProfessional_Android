package com.tencent.liteav.v1liveplayerdemo.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InputView extends LinearLayout {

    private Context  mContext;
    private TextView mTitleView;
    private EditText mEditView;

    public InputView(Context context, String title, String input) {
        super(context);
        mContext = context;
        mTitleView = new TextView(mContext);
        mTitleView.setText(title);
        mEditView = new EditText(mContext);
        mEditView.setText(input);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
        mTitleView.setTextColor(Color.WHITE);
        mTitleView.setTextSize(16);
        LinearLayout.LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        addView(mTitleView, params);
        mEditView.setGravity(Gravity.CENTER);
        mEditView.setMaxLines(1);
        addView(mEditView, params);
    }

    public void setInputType(int type) {
        mEditView.setInputType(type);
    }

    public String getInput() {
        return mEditView.getText().toString().trim();
    }

}
