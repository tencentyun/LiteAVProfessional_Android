package com.tencent.liteav.v1liveplayerdemo.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kyleduo.switchbutton.SwitchButton;
import com.tencent.liteav.v1liveplayerdemo.R;
import com.tencent.liteav.v1liveplayerdemo.view.VariableRadioSelectorView.RadioButton;
import com.tencent.liteav.v1liveplayerdemo.view.VariableRadioSelectorView.RadioSelectListener;

public class SettingItemView extends RelativeLayout {

    public static final int TYPE_SWITCH   = 0;
    public static final int TYPE_BUTTON   = 1;
    public static final int TYPE_PROGRESS = 2;
    public static final int TYPE_RADIO    = 3;

    public abstract static class OnEventListener {

        public void onChecked(SettingItemView itemView, boolean isChecked) {
        }

        public void onProgress(SettingItemView itemView, int progress) {
        }

        public void onButtonClick(SettingItemView itemView) {
        }

        public void onRadioChecked(SettingItemView itemView, int prePosition, int curPosition) {
        }
    }

    private Context                   mContext;
    private TextView                  mTitleView;
    private SwitchButton              mSwitchButton;
    private TextView                  mButton;
    private SeekBar                   mSeekBar;
    private TextView                  mTextViewProgress;
    private VariableRadioSelectorView mVariableRadioSelectorView;
    private int                       mType;

    private OnEventListener mListener;

    public SettingItemView(Context context) {
        super(context);
        initialize(context, TYPE_SWITCH);
    }

    public SettingItemView(Context context, int type) {
        super(context);
        initialize(context, type);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, TYPE_SWITCH);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, TYPE_SWITCH);
    }

    public void setTitleText(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    public void setTitleColor(int color) {
        if (mTitleView != null) {
            mTitleView.setTextColor(color);
        }
    }

    public void setTitleSize(float size) {
        if (mTitleView != null) {
            mTitleView.setTextSize(size);
        }
    }

    public void setButtonText(String text) {
        if (mButton != null) {
            mButton.setText(text);
        }
    }

    public void setProgressMax(int min) {
        if (mSeekBar != null) {
            mSeekBar.setMax(min);
        }
    }

    public void setProgress(int progress) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
        }
        if (mTextViewProgress != null) {
            mTextViewProgress.setText(String.valueOf(progress));
        }
    }

    public void setRadioData(@NonNull String[] data, int selectPosition) {
        if (mVariableRadioSelectorView != null) {
            mVariableRadioSelectorView.setData(data, selectPosition);
        }
    }

    public void setListener(OnEventListener listener) {
        mListener = listener;
    }

    public void setChecked(boolean checked) {
        mSwitchButton.setChecked(checked);
    }

    private void initialize(Context context, int type) {
        mContext = context;
        mType = type;
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dip2px(8), dip2px(8), dip2px(8), dip2px(8));
        setBackgroundColor(Color.WHITE);

        mTitleView = new TextView(mContext);
        mTitleView.setTextSize(14);
        mTitleView.setTextColor(Color.BLACK);
        mTitleView.setId(View.generateViewId());
        LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mTitleView.setLayoutParams(titleParams);
        titleParams.addRule(ALIGN_PARENT_LEFT);
        titleParams.addRule(CENTER_VERTICAL);
        addView(mTitleView);

        switch (mType) {
            case TYPE_SWITCH:
                initSwitch();
                break;
            case TYPE_BUTTON:
                initButton();
                break;
            case TYPE_PROGRESS:
                initProgress();
                break;
            case TYPE_RADIO:
                initRadio();
                break;
            default:
                break;
        }
    }

    private void initSwitch() {
        mSwitchButton = new SwitchButton(mContext);
        mSwitchButton.setThumbDrawableRes(R.drawable.v1liveplayer_thumb);
        mSwitchButton.setBackDrawableRes(R.drawable.v1liveplayer_switch_button_selector);
        LayoutParams switchParams = new LayoutParams(dip2px(48), dip2px(28));
        switchParams.addRule(ALIGN_PARENT_RIGHT);
        switchParams.addRule(CENTER_VERTICAL);
        mSwitchButton.setLayoutParams(switchParams);
        addView(mSwitchButton);

        mSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mListener != null) {
                    mListener.onChecked(SettingItemView.this, isChecked);
                }
            }
        });
    }

    private void initProgress() {
        mTextViewProgress = new TextView(mContext);
        mTextViewProgress.setTextSize(14);
        mTextViewProgress.setTextColor(Color.BLACK);
        mTextViewProgress.setId(View.generateViewId());
        LayoutParams progressParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        progressParams.addRule(ALIGN_PARENT_RIGHT);
        progressParams.addRule(CENTER_VERTICAL);
        mTextViewProgress.setLayoutParams(progressParams);
        addView(mTextViewProgress);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setId(View.generateViewId());
        LayoutParams seekBarParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        seekBarParams.addRule(CENTER_VERTICAL);
        seekBarParams.addRule(RIGHT_OF, mTitleView.getId());
        seekBarParams.addRule(LEFT_OF, mTextViewProgress.getId());
        mSeekBar.setLayoutParams(seekBarParams);
        addView(mSeekBar);

        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewProgress.setText(String.valueOf(progress));
                if (mListener != null) {
                    mListener.onProgress(SettingItemView.this, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initButton() {
        mButton = new TextView(mContext);
        mButton.setBackgroundResource(R.drawable.v1liveplayer_btn_bg);
        mButton.setGravity(Gravity.CENTER);
        mButton.setTextColor(Color.WHITE);
        LayoutParams buttonParams = new LayoutParams(dip2px(48), dip2px(28));
        buttonParams.addRule(ALIGN_PARENT_RIGHT);
        buttonParams.addRule(CENTER_VERTICAL);
        mButton.setLayoutParams(buttonParams);
        addView(mButton);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onButtonClick(SettingItemView.this);
                }
            }
        });
    }

    private void initRadio() {
        mVariableRadioSelectorView = new VariableRadioSelectorView(mContext);
        LayoutParams radioParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        radioParams.addRule(ALIGN_PARENT_RIGHT);
        radioParams.addRule(CENTER_VERTICAL);
        mVariableRadioSelectorView.setLayoutParams(radioParams);
        mVariableRadioSelectorView.setTextColor(getResources().getColor(R.color.v1liveplayer_text_gray), Color.BLACK);
        addView(mVariableRadioSelectorView);
        mVariableRadioSelectorView.setRadioSelectListener(new RadioSelectListener() {
            @Override
            public void onChecked(int prePosition, RadioButton preRadioButton, int curPosition,
                                  RadioButton curRadioButton) {
                if (mListener != null) {
                    mListener.onRadioChecked(SettingItemView.this, prePosition, curPosition);
                }
            }
        });
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
