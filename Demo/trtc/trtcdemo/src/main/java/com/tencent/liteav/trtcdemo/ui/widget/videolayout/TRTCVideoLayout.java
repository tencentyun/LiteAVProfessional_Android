package com.tencent.liteav.trtcdemo.ui.widget.videolayout;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.Constant;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloudDef;

import java.util.HashMap;

/**
 * 此 TRTCVideoLayout 封装了{@link TXCloudVideoView} 以及业务逻辑 UI 控件
 * <p>
 * 作用：
 * 1. 实现了手势监听，配合 {@link TRTCVideoLayoutManager} 能够实现自由拖动 View。
 * 详情可见：{@link TRTCVideoLayout#initGestureListener()}
 * 实现原理：利用 RelativeLayout 的 margin 实现了能够在父容器自由定位的特性；需要注意，{@link TRTCVideoLayout} 不能增加约束规则，如 alignParentRight 等，否则无法自由定位。
 * <p>
 * 2. 对{@link TXCloudVideoView} 与逻辑 UI 进行组合，在 muteLocal、音量回调等情况，能够进行 UI 相关的变化。
 */
class TRTCVideoLayout extends RelativeLayout {

    private ViewGroup                 mVgRoot;
    private FrameLayout               mVideoViewHolder;
    private TXCloudVideoView          mVideoView;
    private OnClickListener           mClickListener;
    private GestureDetector           mSimpleOnGestureListener;
    private ProgressBar               mPbAudioVolume;
    private FrameLayout               mLlNoVideo;
    private TextView                  mTvContent;
    private ImageView                 mIvNoS;
    private boolean                   mMoveable;
    private int                       mViewType = -1;
    private HashMap<Integer, Integer> mNoSMap   = null;


    public TRTCVideoLayout(Context context) {
        this(context, null);
    }

    public TRTCVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
        initGestureListener();
        initNoS();
    }

    public int getViewType() {
        return mViewType;
    }

    public void setViewType(int type) {
        if (mViewType == type) {
            return;
        }

        mViewType = type;
        mVideoViewHolder.removeAllViews();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        if (type == Constant.TRTCViewType.TYPE_SURFACE_VIEW) {
            SurfaceView surfaceView = new SurfaceView(getContext());
            mVideoViewHolder.addView(surfaceView, params);
            mVideoView = new TXCloudVideoView(surfaceView);
        } else if (type == Constant.TRTCViewType.TYPE_TEXTURE_VIEW) {
            mVideoView = new TXCloudVideoView(getContext());
            mVideoView.addVideoView(new TextureView(getContext()));
        } else {
            mVideoView = new TXCloudVideoView(getContext());
        }
        mVideoViewHolder.addView(mVideoView, params);
    }

    private void initNoS() {
        mNoSMap = new HashMap<>();
        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Down), Integer.valueOf(R.drawable.trtcdemo_signal1));
        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Vbad), Integer.valueOf(R.drawable.trtcdemo_signal2));
        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Bad), Integer.valueOf(R.drawable.trtcdemo_signal3));
        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Poor), Integer.valueOf(R.drawable.trtcdemo_signal4));
        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Good), Integer.valueOf(R.drawable.trtcdemo_signal5));
        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Excellent), Integer.valueOf(R.drawable.trtcdemo_signal6));
    }

    public TXCloudVideoView getVideoView() {
        return mVideoView;
    }

    public void updateNoVideoLayout(String text, int visibility) {
        if (mTvContent != null) {
            mTvContent.setText(text);
        }
        if (mLlNoVideo != null) {
            mLlNoVideo.setVisibility(visibility);
        }
    }

    public void setAudioVolumeProgress(int progress) {
        if (mPbAudioVolume != null) {
            mPbAudioVolume.setProgress(progress);
        }
    }

    public void setAudioVolumeProgressBarVisibility(int visibility) {
        if (mPbAudioVolume != null) {
            mPbAudioVolume.setVisibility(visibility);
        }
    }

    private void initLayout() {
        mVgRoot = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.trtcdemo_layout_trtc_func, this, true);
        mVideoViewHolder = (FrameLayout) mVgRoot.findViewById(R.id.trtc_tc_cloud_view_holder);
        mPbAudioVolume = (ProgressBar) mVgRoot.findViewById(R.id.trtc_pb_audio);
        mLlNoVideo = (FrameLayout) mVgRoot.findViewById(R.id.trtc_fl_no_video);
        mTvContent = (TextView) mVgRoot.findViewById(R.id.trtc_tv_content);
        mIvNoS = (ImageView) mVgRoot.findViewById(R.id.trtc_iv_nos);
    }

    private void initGestureListener() {
        mSimpleOnGestureListener = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mClickListener != null) {
                    mClickListener.onClick(TRTCVideoLayout.this);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mMoveable) return false;
                ViewGroup.LayoutParams params = TRTCVideoLayout.this.getLayoutParams();
                if (params instanceof LayoutParams) {
                    LayoutParams layoutParams = (LayoutParams) TRTCVideoLayout.this.getLayoutParams();
                    int newX = (int) (layoutParams.leftMargin + (e2.getX() - e1.getX()));
                    int newY = (int) (layoutParams.topMargin + (e2.getY() - e1.getY()));

                    layoutParams.leftMargin = newX;
                    layoutParams.topMargin = newY;

                    TRTCVideoLayout.this.setLayoutParams(layoutParams);
                }
                return true;
            }
        });
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mSimpleOnGestureListener.onTouchEvent(event);
            }
        });
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mClickListener = l;
    }


    public void setMoveable(boolean enable) {
        mMoveable = enable;
    }

    public void updateNetworkQuality(int quality) {
        if (quality < TRTCCloudDef.TRTC_QUALITY_Excellent) {
            quality = TRTCCloudDef.TRTC_QUALITY_Excellent;
        }
        if (quality > TRTCCloudDef.TRTC_QUALITY_Down) {
            quality = TRTCCloudDef.TRTC_QUALITY_Down;
        }

        if (mIvNoS != null) {
            mIvNoS.setImageResource(mNoSMap.get(Integer.valueOf(quality).intValue()));
        }
    }
}
