package com.tencent.liteav.demo.videoediter.paster.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.paster.TCPasterInfo;

import java.util.List;

/**
 * Created by vinsonswang on 2017/10/27.
 */

public class TCPasterSelectView extends LinearLayout implements View.OnClickListener {

    private Context mContext;
    public static int TAB_ANIMATED_PASTER = 1;
    public static int TAB_PASTER = 2;

    public int getCurrentTab() {
        return currentTab;
    }

    private int currentTab;

    private TextView mTvPaster, mTvAnimatedPaster;

    private RecyclerView mRecyclerView;
    private PasterAdapter mPasterAdapter;
    private Button mBtnSure;
    private OnTabChangedListener mOnTabChangedListener;
    private OnAddClickListener mOnAddClickListener;
    private PasterAdapter.OnItemClickListener mOnItemClickListener;

    public interface OnAddClickListener {
        void onAdd();
    }

    public interface OnTabChangedListener {
        void onTabChanged(int currentTab);
    }

    public TCPasterSelectView(Context context) {
        super(context);
        init(context);
    }

    public TCPasterSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TCPasterSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_paster_select, this);
        mTvPaster = (TextView) findViewById(R.id.tv_paster);
        mTvPaster.setOnClickListener(this);

        mTvAnimatedPaster = (TextView) findViewById(R.id.tv_animated_paster);
        mTvAnimatedPaster.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.paster_recycler_view);
        mBtnSure = (Button) findViewById(R.id.paster_btn_done);
        mBtnSure.setOnClickListener(this);

        currentTab = TAB_ANIMATED_PASTER;
        mTvAnimatedPaster.setTextColor(context.getResources().getColor(R.color.white));
        mTvPaster.setTextColor(context.getResources().getColor(R.color.colorGray4));
    }

    public void setPasterInfoList(List<TCPasterInfo> pasterInfoList) {
        mPasterAdapter = new PasterAdapter(pasterInfoList);
        mPasterAdapter.setOnItemClickListener(mOnItemClickListener);

        GridLayoutManager manager = new GridLayoutManager(mContext, 1, GridLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mPasterAdapter);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_animated_paster) {
            if (currentTab == TAB_ANIMATED_PASTER) {
                return;
            }
            currentTab = TAB_ANIMATED_PASTER;
            mTvAnimatedPaster.setTextColor(mContext.getResources().getColor(R.color.white));
            mTvPaster.setTextColor(mContext.getResources().getColor(R.color.colorGray4));
            if (mOnTabChangedListener != null) {
                mOnTabChangedListener.onTabChanged(currentTab);
            }

        } else if (i == R.id.tv_paster) {
            if (currentTab == TAB_PASTER) {
                return;
            }
            currentTab = TAB_PASTER;
            mTvAnimatedPaster.setTextColor(mContext.getResources().getColor(R.color.colorGray4));
            mTvPaster.setTextColor(mContext.getResources().getColor(R.color.white));
            if (mOnTabChangedListener != null) {
                mOnTabChangedListener.onTabChanged(currentTab);
            }

        } else if (i == R.id.paster_btn_done) {
            exitAnimator();
            if (mOnAddClickListener != null) {
                mOnAddClickListener.onAdd();
            }

        }
    }

    public void show() {
        this.post(new Runnable() {
            @Override
            public void run() {
                enterAnimator();
            }
        });
    }

    public void dismiss() {
        this.post(new Runnable() {
            @Override
            public void run() {
                exitAnimator();
            }
        });
    }

    private void enterAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", this.getHeight(), 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        set.play(translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                TCPasterSelectView.this.setVisibility(VISIBLE);
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

    private void exitAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", 0,
                this.getHeight());
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
                TCPasterSelectView.this.setVisibility(GONE);
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

    public void setOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        mOnTabChangedListener = onTabChangedListener;
    }

    public void setOnItemClickListener(PasterAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        mOnAddClickListener = listener;
    }
}
