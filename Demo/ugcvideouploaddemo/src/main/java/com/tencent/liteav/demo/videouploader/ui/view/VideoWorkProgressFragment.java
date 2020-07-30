package com.tencent.liteav.demo.videouploader.ui.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.liteav.demo.videouploader.R;

/**
 * Created by hanszhli on 2017/6/5.
 */

public class VideoWorkProgressFragment extends DialogFragment {
    private static final String KEY_TITLE = "key_title";

    private View                 mContentView;
    private ImageView            mImageStop;
    private NumberProgressBar    mProgressLoading;
    private TextView             mTextTips;
    private View.OnClickListener mListener;

    private int     mProgress;
    private boolean mCanCancel = true;


    public static VideoWorkProgressFragment newInstance(String title) {
        VideoWorkProgressFragment fragment = new VideoWorkProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(R.style.UGCUploadConfirmDialogStyle, R.style.UGCUploadDialogFragmentStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.ugcupload_layout_uploader_progress, null);
        mTextTips = (TextView) mContentView.findViewById(R.id.uploader_tv_msg);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String msg = bundle.getString(KEY_TITLE);
            if (!TextUtils.isEmpty(msg)) {
                mTextTips.setText(msg);
            }
        }
        mImageStop = (ImageView) mContentView.findViewById(R.id.uploader_iv_stop);
        mProgressLoading = (NumberProgressBar) mContentView.findViewById(R.id.uploader_pb_loading);
        mProgressLoading.setMax(100);
        mProgressLoading.setProgress(mProgress);
        mImageStop.setOnClickListener(mListener);
        if (mCanCancel) {
            mImageStop.setVisibility(View.VISIBLE);
        } else {
            mImageStop.setVisibility(View.INVISIBLE);
        }
        return mContentView;
    }


    /**
     * 设置停止按钮的监听
     *
     * @param listener
     */
    public void setOnClickStopListener(View.OnClickListener listener) {
        mListener = listener;
        if (mImageStop != null) {
            mImageStop.setOnClickListener(listener);
        }
    }

    /**
     * 设置进度条
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (mProgressLoading == null) {
            mProgress = progress;
            return;
        }
        mProgressLoading.setProgress(progress);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        //此处不使用用.show(...)的方式加载dialogfragment，避免IllegalStateException
        try {
            if (isAdded()) {
                dismiss();
            }
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                manager.beginTransaction().remove(this).add(this, tag).commitAllowingStateLoss();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void dismiss() {
        // 和show对应
        if (getFragmentManager() != null && isAdded()) {
            getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
        if (mProgressLoading != null) {
            mProgressLoading.setProgress(0);
        }
    }

    public void setCanCancel(boolean canCancel) {
        mCanCancel = canCancel;
        if (mImageStop == null) {
        } else {
            if (canCancel) {
                mImageStop.setVisibility(View.VISIBLE);
            } else {
                mImageStop.setVisibility(View.INVISIBLE);
            }
        }
    }
}
