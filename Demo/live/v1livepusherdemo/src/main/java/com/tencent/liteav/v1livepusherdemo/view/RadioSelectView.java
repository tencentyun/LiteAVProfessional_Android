package com.tencent.liteav.v1livepusherdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.tencent.liteav.v1livepusherdemo.R;

public class RadioSelectView extends FrameLayout implements View.OnClickListener {

    private Context             mContext;
    private TextView            mTextTitle;
    private LinearLayout        mLayoutRadio;
    private RadioSelectListener mRadioSelectListener;
    private int                 mSelectPosition = -1;

    public interface RadioSelectListener {
        void onClose();

        void onChecked(int prePosition, RadioButton preRadioButton, int curPosition, RadioButton curRadioButton);
    }

    public RadioSelectView(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public RadioSelectView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public RadioSelectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.v1livepusher_tv_close) {
            if (mRadioSelectListener != null) {
                mRadioSelectListener.onClose();
            }
        }
    }

    public void setTitle(String title) {
        mTextTitle.setText(title);
    }

    public void setTitle(@StringRes int resId) {
        mTextTitle.setText(getResources().getString(resId));
    }

    public void setData(@NonNull String[] data, int selectPosition) {
        mLayoutRadio.removeAllViews();
        for (int i = 0; i < data.length; i++) {
            final int position = i;
            RadioButton radioButton = new RadioButton(mContext);
            radioButton.setPadding(0, dip2px(10), 0, dip2px(10));
            radioButton.setText(data[i]);
            radioButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectPosition != position) {
                        setChecked(position);
                    }
                }
            });
            mLayoutRadio.addView(radioButton);
        }
        setChecked(selectPosition);
    }

    public void setChecked(int position) {
        if (position >= 0 && position < mLayoutRadio.getChildCount()) {
            View child = mLayoutRadio.getChildAt(position);
            if (child instanceof RadioButton) {
                RadioButton curRadioButton = (RadioButton) child;
                curRadioButton.setChecked(true);
                if (mSelectPosition != -1) {
                    RadioButton preRadioButton = (RadioButton) mLayoutRadio.getChildAt(mSelectPosition);
                    preRadioButton.setChecked(false);
                    onChecked(mSelectPosition, preRadioButton, position, curRadioButton);
                } else {
                    onChecked(-1, null, position, curRadioButton);
                }
                mSelectPosition = position;
            }
        }
    }

    public void setRadioSelectListener(RadioSelectListener radioSelectListener) {
        mRadioSelectListener = radioSelectListener;
    }

    private void initialize(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.v1livepusher_view_radio_select, this);
        initView();
    }

    private void initView() {
        mTextTitle = (TextView) findViewById(R.id.v1livepusher_tv_title);
        mLayoutRadio = (LinearLayout) findViewById(R.id.v1livepusher_rg_content);
        findViewById(R.id.v1livepusher_tv_close).setOnClickListener(this);
    }

    private void onChecked(int prePosition, RadioButton preRadioButton, int curPosition, RadioButton curRadioButton) {
        if (mRadioSelectListener != null) {
            mRadioSelectListener.onChecked(prePosition, preRadioButton, curPosition, curRadioButton);
        }
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
