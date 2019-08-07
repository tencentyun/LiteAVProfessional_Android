package com.tencent.liteav.demo.videoediter.common.widget.videotimeline;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videoediter.R;


import java.util.Locale;

/**
 * Created by vinsonswang on 2017/11/7.
 */

public class RangeSliderViewContainer extends LinearLayout {
    private final String TAG = "RangeSliderView";

    private Context mContext;
    private View mRootView;
    private View mStartView;        // 左边拖动控件
    private View mEndView;          // 右边拖动控件
    private View mMiddleView;       // 中间裁剪区域
    private long mStartTimeMs;        // 起始时间us
    private long mDurationMs;         // 最终的时长us
    private long mEndTimeMs;          // 结束时间us
    private long mMaxDuration;      // 允许设置的最大时长
    private int mDistance;          // 中间裁剪区域距离

    private ViewTouchProcess mStartViewTouchProcess;
    private ViewTouchProcess mEndViewTouchProcess;

    private VideoProgressController mVideoProgressController;

    private OnDurationChangeListener mOnDurationChangeListener;

    public interface OnDurationChangeListener {
        void onDurationChange(long startTimeMs, long endTimeMs);
    }

    public void setDurationChangeListener(OnDurationChangeListener onDurationChangeListener) {
        mOnDurationChangeListener = onDurationChangeListener;
    }

    public RangeSliderViewContainer(Context context) {
        super(context);
        initView(context);
    }

    public RangeSliderViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RangeSliderViewContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_range_slider, this);
        mStartView = mRootView.findViewById(R.id.iv_start_view);
        mEndView = mRootView.findViewById(R.id.iv_end_view);
        mMiddleView = mRootView.findViewById(R.id.middle_view);

        mStartViewTouchProcess = new ViewTouchProcess(mStartView);
        mEndViewTouchProcess = new ViewTouchProcess(mEndView);
    }

    public void init(VideoProgressController videoProgressController, long startTimeMs, long durationMs, long maxDurationMs) {
        mVideoProgressController = videoProgressController;
        mStartTimeMs = startTimeMs;
        mDurationMs = durationMs;
        mMaxDuration = maxDurationMs;
        mEndTimeMs = mStartTimeMs + mDurationMs;

        mDistance = videoProgressController.duration2Distance(mDurationMs);

//        TXCLog.i(TAG, "init, mDistance = " + mDistance);

        ViewGroup.LayoutParams layoutParams = mMiddleView.getLayoutParams();
        layoutParams.width = mDistance;
        mMiddleView.setLayoutParams(layoutParams);
        setMiddleRangeColor(mContext.getResources()
                .getColor(R.color.colorAccentTransparent30));

        setTouchProcessListener();
    }

    /**
     * 设置中间范围颜色
     *
     * @param color
     */
    public void setMiddleRangeColor(int color) {
        mMiddleView.setBackgroundColor(color);
    }

    private void setTouchProcessListener() {
        mStartViewTouchProcess.setOnPositionChangedListener(new ViewTouchProcess.OnPositionChangedListener() {
            @Override
            public void onPostionChanged(float distance) {
                long dtime = mVideoProgressController.distance2Duration(distance);

                TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mStartView distance = %f, dtime = %d", distance, dtime));

                if (dtime > 0 && mDurationMs - dtime < 0) {
                    dtime = mDurationMs;
                } else if (dtime < 0 && (mStartTimeMs + dtime < 0)) {
                    dtime = -mStartTimeMs;
//                    TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mStartView dtime < 0 and < start time, dtime = %d", dtime));
                }
                if (dtime == 0) {
                    return;
                }

                mDurationMs -= dtime;
                mStartTimeMs = mStartTimeMs + dtime;
                MarginLayoutParams layoutParams = (MarginLayoutParams) mStartView.getLayoutParams();
                int dx = layoutParams.leftMargin;

                TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mStartView layoutParams.leftMargin = %d", layoutParams.leftMargin));

                changeStartViewLayoutParams();
                dx = layoutParams.leftMargin - dx;
//                mStartView.setLayoutParams(layoutParams);

                layoutParams = (MarginLayoutParams) mMiddleView.getLayoutParams();
                layoutParams.width -= dx;
//                mMiddleView.setLayoutParams(layoutParams);
            }

            @Override
            public void onChangeComplete() {
                mVideoProgressController.setIsRangeSliderChanged(true);
                mVideoProgressController.setCurrentTimeMs(mStartTimeMs);
                if (mOnDurationChangeListener != null) {
                    mOnDurationChangeListener.onDurationChange(mStartTimeMs, mEndTimeMs);
                }
            }
        });


        mEndViewTouchProcess.setOnPositionChangedListener(new ViewTouchProcess.OnPositionChangedListener() {
            @Override
            public void onPostionChanged(float distance) {
                long dtime = mVideoProgressController.distance2Duration(distance);
//                TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mEndView distance = %f, dtime = %d", distance, dtime));

                if (dtime < 0 && (mEndTimeMs + dtime - mStartTimeMs) < 0) {
                    dtime = mStartTimeMs - mEndTimeMs;
                } else if (dtime > 0 && mEndTimeMs + dtime > mMaxDuration) {
                    dtime = mMaxDuration - mEndTimeMs;
                }
                if (dtime == 0) {
                    return;
                }
                mDurationMs += dtime;

                ViewGroup.LayoutParams layoutParams = mMiddleView.getLayoutParams();
                layoutParams.width = mVideoProgressController.duration2Distance(mDurationMs);

//                TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mEndView dtime = %d, layoutParams.width = %d", dtime,  layoutParams.width));

                mEndTimeMs = mEndTimeMs + dtime;
                mMiddleView.setLayoutParams(layoutParams);
            }

            @Override
            public void onChangeComplete() {
                mVideoProgressController.setIsRangeSliderChanged(true);
                mVideoProgressController.setCurrentTimeMs(mEndTimeMs);
                if (mOnDurationChangeListener != null) {
                    mOnDurationChangeListener.onDurationChange(mStartTimeMs, mEndTimeMs);
                }
            }
        });
    }

    public void changeStartViewLayoutParams() {
        MarginLayoutParams layoutParams = (MarginLayoutParams) mStartView.getLayoutParams();
        layoutParams.leftMargin = mVideoProgressController.calculateStartViewPosition(this);

//        TXCLog.i(TAG, String.format(Locale.getDefault(), "changeStartViewLayoutParams, layoutParams.leftMargin = %d", layoutParams.leftMargin));

        mStartView.setLayoutParams(layoutParams);
    }

    public void setEditComplete() {
        mStartView.setVisibility(View.INVISIBLE);
        mEndView.setVisibility(View.INVISIBLE);
    }

    public void showEdit() {
        mStartView.setVisibility(View.VISIBLE);
        mEndView.setVisibility(View.VISIBLE);
    }

    public ViewGroup getContainer() {
        return (ViewGroup) mRootView;
    }

    public View getStartView() {
        return mStartView;
    }

    public View getEndView() {
        return mEndView;
    }

    public long getStartTimeUs() {
        return mStartTimeMs;
    }

    public long getDuration() {
        return mDurationMs;
    }
}
