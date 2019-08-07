package com.tencent.liteav.demo.videorecord.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.videorecord.R;

/**
 * Created by vinsonswang on 2017/9/8.
 */

public class ComposeRecordBtn extends RelativeLayout {
    private Context mContext;
    private ImageView mIvRecordRing;
    private ImageView mIvRecordStart;
    private ImageView mIvRecordPause;

    public ComposeRecordBtn(Context context) {
        super(context);
        init(context);
    }

    public ComposeRecordBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ComposeRecordBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.compose_record_btn, this);
        mIvRecordRing = (ImageView) findViewById(R.id.iv_record_ring);
        mIvRecordStart = (ImageView) findViewById(R.id.iv_record);
        mIvRecordPause = (ImageView) findViewById(R.id.iv_record_pause);
    }

    public void startRecord(){
        ObjectAnimator recordRingZoomOutXAn = ObjectAnimator.ofFloat(mIvRecordRing, "scaleX", 0.8f);
        ObjectAnimator recordRingZoomOutYAn = ObjectAnimator.ofFloat(mIvRecordRing, "scaleY", 0.8f);
        mIvRecordRing.setPivotX(mIvRecordRing.getMeasuredWidth() / 2);
        mIvRecordRing.setPivotY(mIvRecordRing.getMeasuredHeight() / 2);
        mIvRecordRing.invalidate();//显示的调用invalidate

        ObjectAnimator recordStartZoomOutXAn = ObjectAnimator.ofFloat(mIvRecordStart, "scaleX", 0.8f);
        ObjectAnimator recordStartZoomOutYAn = ObjectAnimator.ofFloat(mIvRecordStart, "scaleY", 0.8f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(recordRingZoomOutXAn).with(recordRingZoomOutYAn).with(recordStartZoomOutXAn).with(recordStartZoomOutYAn);
        animatorSet.start();

        mIvRecordPause.setVisibility(View.VISIBLE);
        mIvRecordRing.setImageResource(R.drawable.ugc_record_ring_light);
    }

    public void pauseRecord(){
        ObjectAnimator recordRingZoomInXAn = ObjectAnimator.ofFloat(mIvRecordRing, "scaleX", 1f);
        ObjectAnimator recordRingZoomIntYAn = ObjectAnimator.ofFloat(mIvRecordRing, "scaleY", 1f);
        mIvRecordRing.setPivotX(mIvRecordRing.getMeasuredWidth() / 2);
        mIvRecordRing.setPivotY(mIvRecordRing.getMeasuredHeight() / 2);
        mIvRecordRing.invalidate();//显示的调用invalidate

        ObjectAnimator recordStartZoomInXAn = ObjectAnimator.ofFloat(mIvRecordStart, "scaleX", 1f);
        ObjectAnimator recordStartZoomInYAn = ObjectAnimator.ofFloat(mIvRecordStart, "scaleY", 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(recordRingZoomInXAn).with(recordRingZoomIntYAn).with(recordStartZoomInXAn).with(recordStartZoomInYAn);
        animatorSet.start();

        mIvRecordPause.setVisibility(View.GONE);
        mIvRecordRing.setImageResource(R.drawable.ugc_record_ring_gray);
    }
}
