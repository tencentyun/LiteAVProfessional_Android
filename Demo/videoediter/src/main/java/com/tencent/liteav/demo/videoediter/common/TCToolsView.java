package com.tencent.liteav.demo.videoediter.common;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.tencent.liteav.demo.videoediter.R;


/**
 * Created by hans on 2017/11/6.
 */

public class TCToolsView extends FrameLayout implements View.OnClickListener {
    private ImageButton mCutBtn, mFilterBtn, mBgmBtn, mWordBtn, mTimeEffectBtn, mPasterBtn, mMotion; // 底部的按钮
    private OnItemClickListener mListener;

    public TCToolsView(@NonNull Context context) {
        super(context);
        init();
    }

    public TCToolsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCToolsView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.layout_tools_view, this);

        mTimeEffectBtn = (ImageButton) findViewById(R.id.btn_time_effect);
        mCutBtn = (ImageButton) findViewById(R.id.btn_cut);
        mBgmBtn = (ImageButton) findViewById(R.id.btn_music);
        mFilterBtn = (ImageButton) findViewById(R.id.btn_filter);
        mWordBtn = (ImageButton) findViewById(R.id.btn_word);
        mPasterBtn = (ImageButton) findViewById(R.id.btn_paster);
        mMotion = (ImageButton) findViewById(R.id.btn_motion_filter);


        mTimeEffectBtn.setOnClickListener(this);
        mCutBtn.setOnClickListener(this);
        mFilterBtn.setOnClickListener(this);
        mBgmBtn.setOnClickListener(this);
        mWordBtn.setOnClickListener(this);
        mPasterBtn.setOnClickListener(this);
        mMotion.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_time_effect) {
            changeToTimeEffectView();
            mListener.onClickTime();
            ;

        } else if (i == R.id.btn_cut) {
            changeToCutView();
            mListener.onClickCutter();

        } else if (i == R.id.btn_filter) {
            changeToFilterView();
            mListener.onClickStaticFilter();

        } else if (i == R.id.btn_music) {
            changeToMusicView();
            mListener.onClickBGM();

        } else if (i == R.id.btn_word) {
            changeToWordView();
            mListener.onClickBubbleWord();

        } else if (i == R.id.btn_paster) {
            changeToPasterView();
            mListener.onClickPaster();

        } else if (i == R.id.btn_motion_filter) {
            changeToMotionView();
            mListener.onClickMotionFilter();

        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClickCutter();

        void onClickTime();

        void onClickStaticFilter();

        void onClickMotionFilter();

        void onClickBGM();

        void onClickPaster();

        void onClickBubbleWord();
    }


    private void changeToTimeEffectView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_pressed);
        mCutBtn.setImageResource(R.drawable.ic_cut);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful);
        mMotion.setImageResource(R.drawable.ic_motion);
        mBgmBtn.setImageResource(R.drawable.ic_music);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
    }

    private void changeToMotionView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_normal);
        mCutBtn.setImageResource(R.drawable.ic_cut);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful);
        mBgmBtn.setImageResource(R.drawable.ic_music);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
        mMotion.setImageResource(R.drawable.ic_motion_pressed);
    }

    private void changeToPasterView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_normal);
        mCutBtn.setImageResource(R.drawable.ic_cut);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful);
        mBgmBtn.setImageResource(R.drawable.ic_music);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
        mMotion.setImageResource(R.drawable.ic_motion);
    }

    private void changeToWordView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_normal);
        mCutBtn.setImageResource(R.drawable.ic_cut);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful);
        mBgmBtn.setImageResource(R.drawable.ic_music);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
        mMotion.setImageResource(R.drawable.ic_motion);
    }

    private void changeToMusicView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_normal);
        mCutBtn.setImageResource(R.drawable.ic_cut);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful);
        mBgmBtn.setImageResource(R.drawable.ic_music_pressed);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
        mMotion.setImageResource(R.drawable.ic_motion);
    }

    private void changeToFilterView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_normal);
        mCutBtn.setImageResource(R.drawable.ic_cut);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful_press);
        mBgmBtn.setImageResource(R.drawable.ic_music);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
        mMotion.setImageResource(R.drawable.ic_motion);
    }

    private void changeToCutView() {
        mTimeEffectBtn.setImageResource(R.drawable.ic_time_effect_normal);
        mCutBtn.setImageResource(R.drawable.ic_cut_press);
        mFilterBtn.setImageResource(R.drawable.ic_beautiful);
        mBgmBtn.setImageResource(R.drawable.ic_music);
        mWordBtn.setImageResource(R.drawable.ic_word);
        mPasterBtn.setImageResource(R.drawable.ic_paster);
        mMotion.setImageResource(R.drawable.ic_motion);

    }

}
