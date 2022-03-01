package com.tencent.liteav.v1livepusherdemo.view;

import android.app.DialogFragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.liteav.v1livepusherdemo.R;

public class LoadingFragment extends DialogFragment {

    private ImageView mImageLoading;
    private TextView  mTextTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.v1livepusher_dialog_fragment);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v1livepusher_fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        mImageLoading = (ImageView) view.findViewById(R.id.v1livepusher_iv_fragment_loading);
        mTextTitle = (TextView) view.findViewById(R.id.v1livepusher_tv_fragment_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoadingAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLoadingAnimation();
    }

    @Override
    public void dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLoadingAnimation() {
        if (mImageLoading != null) {
            mImageLoading.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mImageLoading.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mImageLoading != null) {
            mImageLoading.setVisibility(View.GONE);
            ((AnimationDrawable) mImageLoading.getDrawable()).stop();
        }
    }
}
