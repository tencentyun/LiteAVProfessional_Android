package com.tencent.liteav.demo.videoediter.transition;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.TCVideoEditerActivity;
import com.tencent.liteav.demo.videoediter.TCVideoEditerWrapper;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.ColorfulProgress;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.RangeSliderViewContainer;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.List;

/**
 * Created by vinsonswang on 2018/5/16.
 */

public class TCTransitionFragment extends Fragment implements View.OnClickListener {
    private int mCurrentState;

    private TCVideoEditerWrapper mWrapper;
    private TXVideoEditer mTXVideoEditer;
    private TXVideoEditConstants.TXVideoInfo mTxVideoInfo;
    private Button btnTransitionLeft, btnTransitionUp, btnTransitionZoomIn, btnTransitionZoomOut, btnTransitionRotate, btnTransitionFadeInOut;
    private long lastDuration;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transition, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWrapper = TCVideoEditerWrapper.getInstance();
        mTXVideoEditer = mWrapper.getEditer();

        initView(view);

        initData();
    }

    private void initData() {
        mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING;
        btnTransitionLeft.setSelected(true);

        mTxVideoInfo = mWrapper.getTXVideoInfo();
    }

    private void initView(View view) {
        btnTransitionLeft = (Button) view.findViewById(R.id.btn_transition_left);
        btnTransitionUp = (Button) view.findViewById(R.id.btn_transition_up);
        btnTransitionZoomIn = (Button) view.findViewById(R.id.btn_transition_zoom_in);
        btnTransitionZoomOut = (Button) view.findViewById(R.id.btn_transition_zoom_out);
        btnTransitionRotate = (Button) view.findViewById(R.id.btn_transition_rotate);
        btnTransitionFadeInOut = (Button) view.findViewById(R.id.btn_transition_fade_in_out);

        btnTransitionLeft.setOnClickListener(this);
        btnTransitionUp.setOnClickListener(this);
        btnTransitionZoomIn.setOnClickListener(this);
        btnTransitionZoomOut.setOnClickListener(this);
        btnTransitionRotate.setOnClickListener(this);
        btnTransitionFadeInOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        long duration = mTxVideoInfo.duration;
        int i = v.getId();
        if (i == R.id.btn_transition_left) {
            if (mCurrentState == TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING) {
                return;
            }
            ((TCVideoEditerActivity) getActivity()).stopPlay();
            duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
            mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING;
            showLeftView();

        } else if (i == R.id.btn_transition_up) {
            if (mCurrentState == TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING) {
                return;
            }
            ((TCVideoEditerActivity) getActivity()).stopPlay();
            duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING);
            mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING;
            showUpView();

        } else if (i == R.id.btn_transition_zoom_in) {
            if (mCurrentState == TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE) {
                return;
            }
            ((TCVideoEditerActivity) getActivity()).stopPlay();
            duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE);
            mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE;
            showZoomInView();

        } else if (i == R.id.btn_transition_zoom_out) {
            if (mCurrentState == TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW) {
                return;
            }
            ((TCVideoEditerActivity) getActivity()).stopPlay();
            duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW);
            mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW;
            showZoomOutView();

        } else if (i == R.id.btn_transition_rotate) {
            if (mCurrentState == TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING) {
                return;
            }
            ((TCVideoEditerActivity) getActivity()).stopPlay();
            duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING);
            mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING;
            showRotateView();

        } else if (i == R.id.btn_transition_fade_in_out) {
            if (mCurrentState == TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT) {
                return;
            }
            ((TCVideoEditerActivity) getActivity()).stopPlay();
            duration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT);
            mCurrentState = TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT;
            showFadeInOutView();

        }
        lastDuration = mTxVideoInfo.duration;
        mTxVideoInfo.duration = duration;
        // 切换转场后，重新设置进度条的时长；恢复裁剪从0到duration；更改滤镜动效的显示范围，总长度不变，但是时间改变，因此显示范围也需要改变；
        ((TCVideoEditerActivity) getActivity()).getVideoProgressViewController().setDuration(duration);
        // 恢复裁剪从0到duration；
        RangeSliderViewContainer cutterRangeSliderView = ((TCVideoEditerActivity) getActivity()).getVideoProgressViewController().getRangeSliderView(0);
        if (cutterRangeSliderView != null) {
            cutterRangeSliderView.init(((TCVideoEditerActivity) getActivity()).getVideoProgressViewController(), 0, duration, duration);
        }
        mTXVideoEditer.setCutFromTime(0, duration);
        mWrapper.setCutterStartTime(0, duration);
        // 更改滤镜动效的显示范围，总长度不变，但是时间改变，因此显示范围也需要改变；
        ColorfulProgress colorfulProgress = ((TCVideoEditerActivity) getActivity()).getVideoProgressViewController().getColorfulProgress();
        if (colorfulProgress != null) {
            List<ColorfulProgress.MarkInfo> markInfoList = colorfulProgress.getMarkInfoList();
            if (duration == 0) {
                return;
            }
            float ratio = (float) lastDuration / duration;
            for (ColorfulProgress.MarkInfo markInfo : markInfoList) {
                markInfo.left = ratio * markInfo.left;
                markInfo.right = ratio * markInfo.right;
            }
        }
        ((TCVideoEditerActivity) getActivity()).startPlay(0, duration);
    }

    private void showLeftView() {
        btnTransitionLeft.setSelected(true);
        btnTransitionUp.setSelected(false);
        btnTransitionZoomIn.setSelected(false);
        btnTransitionZoomOut.setSelected(false);
        btnTransitionRotate.setSelected(false);
        btnTransitionFadeInOut.setSelected(false);
    }

    private void showUpView() {
        btnTransitionLeft.setSelected(false);
        btnTransitionUp.setSelected(true);
        btnTransitionZoomIn.setSelected(false);
        btnTransitionZoomOut.setSelected(false);
        btnTransitionRotate.setSelected(false);
        btnTransitionFadeInOut.setSelected(false);
    }

    private void showZoomInView() {
        btnTransitionLeft.setSelected(false);
        btnTransitionUp.setSelected(false);
        btnTransitionZoomIn.setSelected(true);
        btnTransitionZoomOut.setSelected(false);
        btnTransitionRotate.setSelected(false);
        btnTransitionFadeInOut.setSelected(false);
    }

    private void showZoomOutView() {
        btnTransitionLeft.setSelected(false);
        btnTransitionUp.setSelected(false);
        btnTransitionZoomIn.setSelected(false);
        btnTransitionZoomOut.setSelected(true);
        btnTransitionRotate.setSelected(false);
        btnTransitionFadeInOut.setSelected(false);
    }

    private void showRotateView() {
        btnTransitionLeft.setSelected(false);
        btnTransitionUp.setSelected(false);
        btnTransitionZoomIn.setSelected(false);
        btnTransitionZoomOut.setSelected(false);
        btnTransitionRotate.setSelected(true);
        btnTransitionFadeInOut.setSelected(false);
    }

    private void showFadeInOutView() {
        btnTransitionLeft.setSelected(false);
        btnTransitionUp.setSelected(false);
        btnTransitionZoomIn.setSelected(false);
        btnTransitionZoomOut.setSelected(false);
        btnTransitionRotate.setSelected(false);
        btnTransitionFadeInOut.setSelected(true);
    }
}
