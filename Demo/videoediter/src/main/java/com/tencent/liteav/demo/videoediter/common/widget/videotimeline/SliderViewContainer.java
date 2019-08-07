package com.tencent.liteav.demo.videoediter.common.widget.videotimeline;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.tencent.liteav.demo.videoediter.R;


/**
 * Created by vinsonswang on 2017/11/10.
 */
public class SliderViewContainer extends LinearLayout {
    private static final String TAG = "RepeatSliderView";
    private Context mContext;
    private View mRootView;
    private View mSliderView;

    private long mStartTimeMs;

    private VideoProgressController mVideoProgressController;
    private ViewTouchProcess mViewTouchProcess;

    private OnStartTimeChangedListener mOnStartTimeChangedListener;

    public interface OnStartTimeChangedListener {
        void onStartTimeMsChanged(long timeMs);
    }

    public SliderViewContainer(Context context) {
        super(context);
        init(context);
    }

    public SliderViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SliderViewContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setOnStartTimeChangedListener(OnStartTimeChangedListener onStartTimeChangedListener) {
        mOnStartTimeChangedListener = onStartTimeChangedListener;
    }

    private void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_repeat_slider, this);
        mSliderView = mRootView.findViewById(R.id.iv_slider);
        mViewTouchProcess = new ViewTouchProcess(mSliderView);
        setTouchProcessListener();
    }

    private void setTouchProcessListener() {
        mViewTouchProcess.setOnPositionChangedListener(new ViewTouchProcess.OnPositionChangedListener() {
            @Override
            public void onPostionChanged(float distance) {
                long dtime = mVideoProgressController.distance2Duration(distance);

//                TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mSliderView distance = %f, dtime = %d", distance, dtime));

                if (dtime > 0 && (mVideoProgressController.getTotalDurationMs() - mStartTimeMs) - dtime < 0) {
                    dtime = mVideoProgressController.getTotalDurationMs() - mStartTimeMs;
                } else if (dtime < 0 && (mStartTimeMs + dtime < 0)) {
                    dtime = -mStartTimeMs;
                }
                if (dtime == 0) {
                    return;
                }

                mStartTimeMs = mStartTimeMs + dtime;
                changeLayoutParams();

//                TXCLog.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mSliderView layoutParams.leftMargin = %d", layoutParams.leftMargin));
            }

            @Override
            public void onChangeComplete() {
//                mVideoProgressController.setIsRangeSliderChanged(true);
//                mVideoProgressController.setCurrentTimeMs(mStartTimeMs);
                if (mOnStartTimeChangedListener != null) {
                    mOnStartTimeChangedListener.onStartTimeMsChanged(mStartTimeMs);
                }
            }

        });
    }

    public void changeLayoutParams() {
        if (mVideoProgressController != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) mSliderView.getLayoutParams();
            layoutParams.leftMargin = mVideoProgressController.calculateSliderViewPosition(SliderViewContainer.this);
            mSliderView.setLayoutParams(layoutParams);
        }
    }

    public View getSliderView() {
        return mSliderView;
    }

    public void setVideoProgressControlloer(VideoProgressController videoProgressControlloer) {
        mVideoProgressController = videoProgressControlloer;
    }

    public void setStartTimeMs(long timeMs) {
        mStartTimeMs = timeMs;
        changeLayoutParams();
    }

    public long getStartTimeMs() {
        return mStartTimeMs;
    }
}
