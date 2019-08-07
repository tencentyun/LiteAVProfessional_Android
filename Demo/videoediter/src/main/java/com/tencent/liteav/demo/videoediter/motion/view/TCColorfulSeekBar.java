package com.tencent.liteav.demo.videoediter.motion.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.tencent.liteav.demo.videoediter.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hans on 2017/11/3.
 */

public class TCColorfulSeekBar extends View {
    private static final String TAG = "TCColorfulSeekBar";
    private int mViewWidth, mViewHeight;
    private float mAnchorWidth, mAnchorHeight, mAnchorLeft, mAnchorRound;
    private float mSeekBarLeft, mSeekBarRight, mSeekBarTop, mSeekBarBottom, mSbRound;
    private RectF mDrawRectF, mCurrentAnchorRectF;
    private Paint mPaint;

    private int mAnchorColor;// 锚的颜色
    private int mSeekBarColor;
    private float mDownX, mDownY;

    private long mMax;
    private long mProgress;
    private WeakReference<OnSeekBarListener> mWefListener;


    private List<MarkInfo> mMarkInfoList;

    public TCColorfulSeekBar(Context context) {
        super(context);
        init();
    }

    public TCColorfulSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCColorfulSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mSeekBarColor = Color.WHITE;
        mAnchorColor = getResources().getColor(R.color.colorAccent);

        mDrawRectF = new RectF();
        mCurrentAnchorRectF = new RectF();

        mMarkInfoList = new ArrayList<>();
    }

    public void setMax(long max) {
        mMax = max;
    }

    /**
     * 开始标记
     *
     * @param color
     */
    public void startMark(int color) {
        MarkInfo info = new MarkInfo();
        info.startTime = mProgress;
        info.left = mCurrentAnchorRectF.centerX();
        info.color = color;
        mMarkInfoList.add(info);
    }

    /**
     * 结束标记
     */
    public void endMark() {
        MarkInfo info = mMarkInfoList.get(mMarkInfoList.size() - 1);
        info.right = mCurrentAnchorRectF.centerX();
        info.endTime = mProgress;
        invalidate();
    }


    public int getMarkListSize() {
        return mMarkInfoList.size();
    }

    public void setProgress(final long progress) {
        this.post(new Runnable() {
            @Override
            public void run() {
                mProgress = progress;
                mAnchorLeft = calculateAnchorLeft(progress);
                invalidate();
            }
        });
    }

    public long getProgress() {
        return mProgress;
    }

    public void setOnSeekBarListener(OnSeekBarListener listener) {
        mWefListener = new WeakReference<OnSeekBarListener>(listener);
    }

    public MarkInfo deleteLastMark() {
        MarkInfo info = null;
        if (mMarkInfoList != null && mMarkInfoList.size() != 0) {
            info = mMarkInfoList.remove(mMarkInfoList.size() - 1);
            invalidate();
        }
        return info;
    }


    public interface OnSeekBarListener {
        void onStopTrackingTouch(long progress);

        void onStartTrackingTouch();

        void onProgressChanged(long progress);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        mAnchorHeight = h;
        mAnchorWidth = w / 30;
        mAnchorRound = mAnchorHeight / 2;

        mSeekBarLeft = mAnchorWidth / 2;
        mSeekBarRight = w - mSeekBarLeft;
        mSeekBarTop = 0.38f * mViewHeight;
        mSeekBarBottom = 0.62f * mViewHeight;
        mSbRound = (mSeekBarBottom - mSeekBarTop) / 2;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x >= mCurrentAnchorRectF.left - 40 && x <= mCurrentAnchorRectF.right + 40) {
                    mDownX = x;
                    mDownY = y;
                    if (mWefListener.get() != null && mWefListener.get() != null) {
                        mWefListener.get().onStartTrackingTouch();
                    }
                    return true;//拦截事件
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dis = x - mDownX;
                mAnchorLeft += dis;
                if (mAnchorLeft < 0) {
                    mAnchorLeft = 0;
                } else if (mAnchorLeft + mAnchorWidth > mViewWidth) {
                    mAnchorLeft = mViewWidth - mAnchorWidth;
                }
                invalidate();
                mDownX = x;
                mDownY = y;
                if (mWefListener != null && mWefListener.get() != null) {
                    mProgress = calculateProgress();
                    mWefListener.get().onProgressChanged(mProgress);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (mWefListener != null && mWefListener.get() != null) {
                    mProgress = calculateProgress();
                    mWefListener.get().onStopTrackingTouch(mProgress);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }


    private long calculateProgress() {
        float anchorCenterX = mAnchorLeft + mAnchorWidth / 2;
        float anchorDis = anchorCenterX - mSeekBarLeft;
        float progress = anchorDis / (mSeekBarRight - mSeekBarLeft);
        return (long) (progress * mMax);
    }

    private float calculateAnchorLeft(long progress) {
        float anchorDis = (progress * 1.0f / mMax) * (mSeekBarRight - mSeekBarLeft);
        float anchorCenterX = anchorDis + mSeekBarLeft;
        float anchorLeft = anchorCenterX - mAnchorWidth / 2;
        if (anchorLeft < 0) {
            anchorLeft = 0;
        } else if (anchorLeft + mAnchorWidth >= mViewWidth) {
            anchorLeft = mViewWidth - mAnchorWidth;
        }
        return anchorLeft;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSeekBar(canvas);

        drawMarkInfo(canvas);

        drawAnchor(canvas);
    }

    private void drawMarkInfo(Canvas canvas) {
        for (MarkInfo info : mMarkInfoList) {
            mPaint.setColor(info.color);
            mDrawRectF.left = info.left;
            mDrawRectF.top = mSeekBarTop;
            mDrawRectF.bottom = mSeekBarBottom;
            mDrawRectF.right = info.right == -1 ? mAnchorLeft : info.right;

            if (mDrawRectF.left > mDrawRectF.right) {
                float tmp = mDrawRectF.left;
                mDrawRectF.left = mDrawRectF.right;
                mDrawRectF.right = tmp;
            }
            canvas.drawRoundRect(mDrawRectF, mSbRound, mSbRound, mPaint);
            Log.i(TAG, "drawMarkInfo: " + info.toString());
        }
    }

    private void drawAnchor(Canvas canvas) {
        mPaint.setColor(mAnchorColor);
        mDrawRectF.left = mAnchorLeft;
        mDrawRectF.top = 0;
        mDrawRectF.bottom = mViewHeight;
        mDrawRectF.right = mAnchorLeft + mAnchorWidth;
        // 记录当前的锚的一个位置
        mCurrentAnchorRectF.set(mDrawRectF);
        canvas.drawRoundRect(mDrawRectF, mAnchorRound, mAnchorRound, mPaint);
    }

    private void drawSeekBar(Canvas canvas) {
        mPaint.setColor(mSeekBarColor);
        mDrawRectF.left = mSeekBarLeft;
        mDrawRectF.top = mSeekBarTop;
        mDrawRectF.bottom = mSeekBarBottom;
        mDrawRectF.right = mSeekBarRight;
        canvas.drawRoundRect(mDrawRectF, mSbRound, mSbRound, mPaint);
    }


    public static class MarkInfo {
        public int color;
        public long startTime;
        public long endTime;
        private float left = -1;
        private float right = -1;

        @Override
        public String toString() {
            return "MarkInfo{" +
                    "color=" + color +
                    ", startTimeMs=" + startTime +
                    ", endTime=" + endTime +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }
    }
}
