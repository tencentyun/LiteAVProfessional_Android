package com.tencent.liteav.demo.livelinkmicnew.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.liteav.demo.livelinkmicnew.R;

public class BeautyPanel extends FrameLayout {

    private TXBeautyManager mBeautyManager;
    private SeekBar mBeautySeekBar;
    private SeekBar mRuddySeekBar;
    private SeekBar mMopiSeekBar;
    private TextView mRuddyText;
    private TextView mBeautyText;
    private TextView mMopiText;
    private TextView mTvClosePanel;

    public BeautyPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.live_link_mic_new_setting_beauty, this);
        initView();
    }

    public void setBeautyManager(TXBeautyManager beautyManager) {
        mBeautyManager = beautyManager;
        mBeautyManager.setBeautyStyle(1); //自然
        mBeautyManager.setBeautyLevel(5);
        mBeautyManager.setRuddyLevel(5);
        mBeautyManager.setWhitenessLevel(5);
    }

    private void initView() {
        mTvClosePanel = (TextView) findViewById(R.id.tv_close_panel);
        mTvClosePanel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBeautyPanelHideListener != null) {
                    mBeautyPanelHideListener.onClosePanel();
                }
            }
        });
        mBeautySeekBar = (SeekBar) findViewById(R.id.sb_beauty);
        mBeautyText = (TextView) findViewById(R.id.tv_beauty_value);
        mBeautySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBeautyText.setText(String.valueOf(progress));
                if (mBeautyManager != null) {
                    mBeautyManager.setBeautyLevel(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mRuddySeekBar = (SeekBar) findViewById(R.id.sb_ruddy);
        mRuddyText = (TextView) findViewById(R.id.tv_ruddy_value);
        mRuddySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuddyText.setText(String.valueOf(progress));
                if (mBeautyManager != null) {
                    mBeautyManager.setRuddyLevel(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMopiSeekBar = (SeekBar) findViewById(R.id.sb_mopi);
        mMopiText = (TextView) findViewById(R.id.tv_mopi_value);
        mMopiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMopiText.setText(String.valueOf(progress));
                if (mBeautyManager != null) {
                    mBeautyManager.setWhitenessLevel(progress);
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

    private OnBeautyPanelHideListener mBeautyPanelHideListener;

    public void setOnAudioEffectPanelHideListener(OnBeautyPanelHideListener listener) {
        mBeautyPanelHideListener = listener;
    }

    public interface OnBeautyPanelHideListener {
        void onClosePanel();
    }

}
