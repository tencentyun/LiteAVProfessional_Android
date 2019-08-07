package com.tencent.liteav.demo.videoediter.motion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.TCVideoEditerActivity;
import com.tencent.liteav.demo.videoediter.TCVideoEditerWrapper;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.ColorfulProgress;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressController;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

/**
 * Created by hans on 2017/11/7.
 * <p>
 * 动态滤镜特效的设置Fragment
 */
public class TCMotionFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "TCMotionFragment";

    private Button mBtnSpirit;
    private Button mBtnSplit;
    private Button mBtnLightWave;
    private Button mBtnDark;
    private Button mBtnWinShaddow;
    private Button mBtnGhostShaddow;
    private Button mBtnPhantom;
    private Button mBtnGhost;
    private Button mBtnLightning;
    private Button mBtnMirror;
    private Button mBtnIllusion;
    private RelativeLayout mRlDelete;


    private boolean mIsOnTouch; // 是否已经有按下的
    private TXVideoEditer mTXVideoEditer;
    private long mVideoDuration;

    private ColorfulProgress mColorfulProgress;
    private VideoProgressController mActivityVideoProgressController;
    private boolean mStartMark;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_motion, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TCVideoEditerWrapper wrapper = TCVideoEditerWrapper.getInstance();
        mTXVideoEditer = wrapper.getEditer();
        if (mTXVideoEditer != null) {
            mVideoDuration = wrapper.getTXVideoInfo().duration;
        }
        mActivityVideoProgressController = ((TCVideoEditerActivity) getActivity()).getVideoProgressViewController();
        initViews(view);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mColorfulProgress != null) {
            mColorfulProgress.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }
    }

    private void initViews(View view) {
        mBtnSpirit = (Button) view.findViewById(R.id.btn_soul);
        mBtnSplit = (Button) view.findViewById(R.id.btn_split);
        mBtnLightWave = (Button) view.findViewById(R.id.btn_light_wave);
        mBtnDark = (Button) view.findViewById(R.id.btn_black);
        mBtnWinShaddow = (Button) view.findViewById(R.id.btn_win_shaddow);
        mBtnGhostShaddow = (Button) view.findViewById(R.id.btn_ghost_shaddow);
        mBtnPhantom = (Button) view.findViewById(R.id.btn_phantom);
        mBtnGhost = (Button) view.findViewById(R.id.btn_ghost);
        mBtnLightning = (Button) view.findViewById(R.id.btn_lightning);
        mBtnMirror = (Button) view.findViewById(R.id.btn_mirror);
        mBtnIllusion = (Button) view.findViewById(R.id.btn_illusion);

        mBtnSpirit.setOnTouchListener(this);
        mBtnSplit.setOnTouchListener(this);
        mBtnLightWave.setOnTouchListener(this);
        mBtnDark.setOnTouchListener(this);
        mBtnWinShaddow.setOnTouchListener(this);
        mBtnGhostShaddow.setOnTouchListener(this);
        mBtnPhantom.setOnTouchListener(this);
        mBtnGhost.setOnTouchListener(this);
        mBtnLightning.setOnTouchListener(this);
        mBtnMirror.setOnTouchListener(this);
        mBtnIllusion.setOnTouchListener(this);

        mRlDelete = (RelativeLayout) view.findViewById(R.id.motion_rl_delete);
        mRlDelete.setOnClickListener(this);

        mColorfulProgress = new ColorfulProgress(getContext());
        mColorfulProgress.setWidthHeight(mActivityVideoProgressController.mThumbnailPicListDisplayWidth, getResources().getDimensionPixelOffset(R.dimen.video_progress_height));
        mActivityVideoProgressController.addColorfulProgress(mColorfulProgress);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.motion_rl_delete) {
            deleteLastMotion();

        }
    }


    private void deleteLastMotion() {
        ColorfulProgress.MarkInfo markInfo = mColorfulProgress.deleteLastMark();
        if (markInfo != null) {
            mActivityVideoProgressController.setCurrentTimeMs(markInfo.startTimeMs);
            TCVideoEditerActivity parentActivity = (TCVideoEditerActivity) getActivity();
            parentActivity.previewAtTime(markInfo.startTimeMs);
        }

        mTXVideoEditer.deleteLastEffect();
        if (mColorfulProgress.getMarkListSize() > 0) {
            showDeleteBtn();
        } else {
            hideDeleteBtn();
        }
    }

    public void showDeleteBtn() {
        if (mColorfulProgress.getMarkListSize() > 0) {
            mRlDelete.setVisibility(View.VISIBLE);
        }
    }

    public void hideDeleteBtn() {
        if (mColorfulProgress.getMarkListSize() == 0) {
            mRlDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (mIsOnTouch && action == MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (view.getId() == R.id.btn_soul) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_SOUL_OUT);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_SOUL_OUT);
                mIsOnTouch = false;
            }
            return false;
        }

        if (view.getId() == R.id.btn_split) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_SPLIT_SCREEN);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_SPLIT_SCREEN);
                mIsOnTouch = false;
            }
            return false;
        }

        if (view.getId() == R.id.btn_light_wave) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_ROCK_LIGHT);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_ROCK_LIGHT);
                mIsOnTouch = false;
            }
            return false;
        }

        if (view.getId() == R.id.btn_black) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_DARK_DRAEM);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_DARK_DRAEM);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_win_shaddow) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_WIN_SHADDOW);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_WIN_SHADDOW);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_ghost_shaddow) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_GHOST_SHADDOW);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_GHOST_SHADDOW);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_phantom) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_PHANTOM_SHADDOW);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_PHANTOM_SHADDOW);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_ghost) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_GHOST);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_GHOST);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_lightning) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_LIGHTNING);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_LIGHTNING);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_mirror) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_MIRROR);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_MIRROR);
                mIsOnTouch = false;
            }
            return false;
        }
        if (view.getId() == R.id.btn_illusion) {
            if (action == MotionEvent.ACTION_DOWN) {
                pressMotion(TXVideoEditConstants.TXEffectType_ILLUSION);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                upMotion(TXVideoEditConstants.TXEffectType_ILLUSION);
                mIsOnTouch = false;
            }
            return false;
        }
        return false;
    }

    private void pressMotion(int type) {
        // 未开始播放 则开始播放
        long currentTime = mActivityVideoProgressController.getCurrentTimeMs();

        if (((TCVideoEditerActivity) getActivity()).isPreviewFinish) {
            TXCLog.i(TAG, "pressMotion, preview finished, ignore");
            mStartMark = false;
            return;
        }
        mStartMark = true;
        ((TCVideoEditerActivity) getActivity()).playVideo(true);
        mTXVideoEditer.startEffect(type, currentTime);

        switch (type) {
            case TXVideoEditConstants.TXEffectType_SOUL_OUT:
                mBtnSpirit.setBackgroundResource(R.drawable.shape_motion_spirit_press);
                // 进度条开始变颜色
                mColorfulProgress.startMark(getResources().getColor(R.color.spirit_out_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_SPLIT_SCREEN:
                mBtnSplit.setBackgroundResource(R.drawable.shape_motion_split_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.screen_split_press));
                break;
            case TXVideoEditConstants.TXEffectType_ROCK_LIGHT:
                mBtnLightWave.setBackgroundResource(R.drawable.shape_motion_light_wave_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.light_wave_press));
                break;
            case TXVideoEditConstants.TXEffectType_DARK_DRAEM:
                mBtnDark.setBackgroundResource(R.drawable.shape_motion_dark_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.dark_illusion_press));
                break;
            case TXVideoEditConstants.TXEffectType_WIN_SHADDOW:
                mBtnWinShaddow.setBackgroundResource(R.drawable.shape_motion_window_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.win_shaddow_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_GHOST_SHADDOW:
                mBtnGhostShaddow.setBackgroundResource(R.drawable.shape_motion_ghost_shaddow_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.ghost_shaddow_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_PHANTOM_SHADDOW:
                mBtnPhantom.setBackgroundResource(R.drawable.shape_motion_phantom_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.phantom_shaddow_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_GHOST:
                mBtnGhost.setBackgroundResource(R.drawable.shape_motion_ghost_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.ghost_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_LIGHTNING:
                mBtnLightning.setBackgroundResource(R.drawable.shape_motion_lightning_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.lightning_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_MIRROR:
                mBtnMirror.setBackgroundResource(R.drawable.shape_motion_mirror_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.mirror_color_press));
                break;
            case TXVideoEditConstants.TXEffectType_ILLUSION:
                mBtnIllusion.setBackgroundResource(R.drawable.shape_motion_illusion_press);

                mColorfulProgress.startMark(getResources().getColor(R.color.illusion_color_press));
                break;
        }
    }

    private void upMotion(int type) {
        if (!mStartMark) {
            return;
        }
        switch (type) {
            case TXVideoEditConstants.TXEffectType_SOUL_OUT:
                mBtnSpirit.setBackgroundResource(R.drawable.shape_motion_spirit);
                break;
            case TXVideoEditConstants.TXEffectType_SPLIT_SCREEN:
                mBtnSplit.setBackgroundResource(R.drawable.shape_motion_split);
                break;
            case TXVideoEditConstants.TXEffectType_ROCK_LIGHT:
                mBtnLightWave.setBackgroundResource(R.drawable.shape_motion_light_wave);
                break;
            case TXVideoEditConstants.TXEffectType_DARK_DRAEM:
                mBtnDark.setBackgroundResource(R.drawable.shape_motion_dark);
                break;
            case TXVideoEditConstants.TXEffectType_WIN_SHADDOW:
                mBtnWinShaddow.setBackgroundResource(R.drawable.shape_motion_window);
                break;
            case TXVideoEditConstants.TXEffectType_GHOST_SHADDOW:
                mBtnGhostShaddow.setBackgroundResource(R.drawable.shape_motion_ghost_shaddow);
                break;
            case TXVideoEditConstants.TXEffectType_PHANTOM_SHADDOW:
                mBtnPhantom.setBackgroundResource(R.drawable.shape_motion_phantom);
                break;
            case TXVideoEditConstants.TXEffectType_GHOST:
                mBtnGhost.setBackgroundResource(R.drawable.shape_motion_ghost);
                break;
            case TXVideoEditConstants.TXEffectType_LIGHTNING:
                mBtnLightning.setBackgroundResource(R.drawable.shape_motion_lightning);
                break;
            case TXVideoEditConstants.TXEffectType_MIRROR:
                mBtnMirror.setBackgroundResource(R.drawable.shape_motion_mirror);
                break;
            case TXVideoEditConstants.TXEffectType_ILLUSION:
                mBtnIllusion.setBackgroundResource(R.drawable.shape_motion_illusion);
                break;
        }

        // 暂停播放
        ((TCVideoEditerActivity) getActivity()).pausePlay();
        // 进度条结束标记
        mColorfulProgress.endMark();

        // 特效结束时间
        long currentTime = mActivityVideoProgressController.getCurrentTimeMs();
        mTXVideoEditer.stopEffect(type, currentTime);
        // 显示撤销的按钮
        showDeleteBtn();
    }
}
