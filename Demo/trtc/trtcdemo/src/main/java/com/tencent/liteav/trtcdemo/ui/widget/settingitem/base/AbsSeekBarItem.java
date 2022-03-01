package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;

/**
 * 封装拖动条Item
 */
public abstract class AbsSeekBarItem extends AbsBaseItem {

    private TextView mTextTitle;
    public  SeekBar  mSeekbar;
    public  TextView mTextTip;
    public  String   mTip;
    public  String   mTitle;

    public AbsSeekBarItem(Context context, boolean debug, String title, String tip) {
        super(context, debug);
        mTitle = title;
        mTip = tip;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_seekbar, this, true);
        mTextTitle = findViewById(R.id.title);
        mSeekbar = findViewById(R.id.sb_item);
        mTextTip = findViewById(R.id.tv_tips);
        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }
        if (!TextUtils.isEmpty(mTip)) {
            mTextTip.setText(mTip);
        }

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onSeekBarChange(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public int getMax() {
        return mSeekbar.getMax();
    }

    public AbsSeekBarItem setMax(final int max) {
        mSeekbar.post(new Runnable() {
            @Override
            public void run() {
                mSeekbar.setMax(max);
            }
        });
        return this;
    }

    public AbsSeekBarItem setMix(final int min) {
        mSeekbar.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mSeekbar.setMin(min);
                }
            }
        });
        return this;
    }

    public int getProgress() {
        return mSeekbar.getProgress();
    }

    public AbsSeekBarItem setProgress(final int progress) {
        mSeekbar.post(new Runnable() {
            @Override
            public void run() {
                mSeekbar.setProgress(progress);
            }
        });
        return this;
    }

    public AbsSeekBarItem setTip(final String tips) {
        mTextTip.setText(tips);
        return this;
    }

    public abstract void onSeekBarChange(int index, boolean fromUser);
}
