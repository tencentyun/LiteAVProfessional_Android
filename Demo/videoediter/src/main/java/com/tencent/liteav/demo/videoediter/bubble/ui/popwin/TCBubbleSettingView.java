package com.tencent.liteav.demo.videoediter.bubble.ui.popwin;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.bubble.utils.TCBubbleInfo;

import java.util.List;

/**
 * Created by hans on 2017/10/23.
 * <p>
 * 配置气泡字幕样式、以及字体颜色的控件
 */
public class TCBubbleSettingView extends FrameLayout implements BubbleAdapter.OnItemClickListener, View.OnClickListener, TCColorView.OnSelectColorListener {
    private View mContentView;
    private View mBtnDone;
    private RecyclerView mRvBubbles;
    private BubbleAdapter mBubbleAdapter;
    private List<TCBubbleInfo> mBubbles;
    private ImageView mIvBubble, mIvColor;
    private TCColorView mColorView;
    private TCCircleView mCvColor;
    private LinearLayout mLlColor;

    private TCWordParamsInfo mTCWordInfo;
    private OnAddClickListener mOnAddClickListener;

    public TCBubbleSettingView(@NonNull Context context) {
        super(context);
        init();
    }

    public TCBubbleSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCBubbleSettingView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mTCWordInfo = new TCWordParamsInfo();

        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bubble_win, this, true);

        initViews(mContentView);
    }

    private void initViews(View contentView) {
        mBtnDone = contentView.findViewById(R.id.bubble_btn_done);
        mBtnDone.setOnClickListener(this);
        mRvBubbles = (RecyclerView) contentView.findViewById(R.id.bubble_rv_style);
        mIvBubble = (ImageView) contentView.findViewById(R.id.bubble_iv_bubble);
        mIvBubble.setOnClickListener(this);
        mIvColor = (ImageView) contentView.findViewById(R.id.bubble_iv_color);
        mIvColor.setOnClickListener(this);
        mLlColor = (LinearLayout) contentView.findViewById(R.id.bubble_ll_color);
        mCvColor = (TCCircleView) contentView.findViewById(R.id.bubble_cv_color);
        mCvColor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mColorView = (TCColorView) contentView.findViewById(R.id.bubble_color_view);
        mColorView.setOnSelectColorListener(this);
        mIvBubble.setSelected(true);
        mRvBubbles.setVisibility(View.VISIBLE);

    }

    private void enterAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mContentView, "translationY", mContentView.getHeight(), 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        set.play(translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                TCBubbleSettingView.this.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    private void resetInfo() {
        mTCWordInfo.setBubblePos(0);
        // 创建一个默认的
        TCBubbleInfo info = new TCBubbleInfo();
        info.setHeight(0);
        info.setWidth(0);
        info.setDefaultSize(40);
        info.setBubblePath(null);
        info.setIconPath(null);
        info.setRect(0, 0, 0, 0);
        mTCWordInfo.setBubbleInfo(info);
    }

    public void setBubbles(List<TCBubbleInfo> list) {
        mBubbles = list;
        mRvBubbles.setVisibility(View.VISIBLE);
        mBubbleAdapter = new BubbleAdapter(list);
        mBubbleAdapter.setOnItemClickListener(this);
        GridLayoutManager manager = new GridLayoutManager(mContentView.getContext(), 4, GridLayoutManager.VERTICAL, false);
        mRvBubbles.setLayoutManager(manager);
        mRvBubbles.setAdapter(mBubbleAdapter);
    }

    public void show(TCWordParamsInfo info) {
        if (info == null) {
            resetInfo();
        } else {
            mTCWordInfo = info;
        }
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                enterAnimator();
            }
        });
    }

    public void dismiss() {
        exitAnimator();
    }

    private void exitAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mContentView, "translationY", 0,
                mContentView.getHeight());
        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);
        set.play(translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                TCBubbleSettingView.super.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.start();
    }

    @Override
    public void onItemClick(View view, int position) {
        mTCWordInfo.setBubblePos(position);
        mTCWordInfo.setBubbleInfo(mBubbles.get(position));
        callback();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bubble_iv_bubble) {
            mLlColor.setVisibility(View.GONE);
            mIvColor.setSelected(false);

            mIvBubble.setSelected(true);
            mRvBubbles.setVisibility(View.VISIBLE);

        } else if (i == R.id.bubble_iv_color) {
            mLlColor.setVisibility(View.VISIBLE);
            mIvColor.setSelected(true);

            mIvBubble.setSelected(false);
            mRvBubbles.setVisibility(View.GONE);

        } else if (i == R.id.bubble_btn_done) {
            dismiss();
            if (mOnAddClickListener != null) {
                mOnAddClickListener.onAdd();
            }

        }
    }

    @Override
    public void onFinishColor(@ColorInt int color) {
        mTCWordInfo.setTextColor(color);
        callback();
    }

    @Override
    public void onProgressColor(@ColorInt int color) {
        mCvColor.setColor(color);
    }


    private void callback() {
        if (mCallback != null) {
            TCWordParamsInfo info = new TCWordParamsInfo();
            info.setBubblePos(mTCWordInfo.getBubblePos());
            info.setBubbleInfo(mTCWordInfo.getBubbleInfo());
            info.setTextColor(mTCWordInfo.getTextColor());
            mCallback.onWordInfoCallback(info);
        }
    }

    private OnWordInfoCallback mCallback;

    public void setOnWordInfoCallback(OnWordInfoCallback callback) {
        mCallback = callback;
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        mOnAddClickListener = listener;
    }

    public interface OnWordInfoCallback {
        void onWordInfoCallback(TCWordParamsInfo info);
    }

    public interface OnAddClickListener {
        void onAdd();
    }
}
