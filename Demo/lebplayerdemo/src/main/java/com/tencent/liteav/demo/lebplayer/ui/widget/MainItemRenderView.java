package com.tencent.liteav.demo.lebplayer.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.liteav.demo.lebplayer.R;
import com.tencent.rtmp.ui.TXCloudVideoView;


public class MainItemRenderView extends FrameLayout {

    private ProgressBar         mPbVolume;
    private TextView            mTvLoading;
    private TXCloudVideoView    mCloudView;
    private SurfaceView         mSurfaceView;
    private TextureView         mTextureView;

    private boolean mIsShowDebugView;

    public MainItemRenderView(@NonNull Context context) {
        super(context);
        init();
    }

    public MainItemRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainItemRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.lebplayer_render, this, true);
        mTvLoading = (TextView) findViewById(R.id.render_tv_loading);
        mPbVolume = (ProgressBar) findViewById(R.id.render_pb_volume);
        mCloudView = (TXCloudVideoView) findViewById(R.id.render_cloud_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.render_surface_view);
        mTextureView = (TextureView) findViewById(R.id.render_texture_view);

        findViewById(R.id.iv_mic).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onMuteAudio(view);
                }
            }
        });

        findViewById(R.id.iv_camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onMuteVideo(view);
                }
            }
        });

        findViewById(R.id.iv_setting).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onShowSetting();
                }
            }
        });

        findViewById(R.id.iv_info).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsShowDebugView) {
                    mIsShowDebugView = false;
                    ((ImageView) view).setImageResource(R.drawable.lebplayer_ic_bottom_log_hidden);
                } else {
                    mIsShowDebugView = true;
                    ((ImageView) view).setImageResource(R.drawable.lebplayer_ic_bottom_log_show);
                }
                if (mCallback != null) {
                    mCallback.onShowDebugView(view);
                }
            }
        });
        findViewById(R.id.iv_start).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onStart(view);
                }
            }
        });
    }

    public TXCloudVideoView getCloudView() {
        mSurfaceView.setVisibility(GONE);
        mTextureView.setVisibility(GONE);
        mCloudView.setVisibility(VISIBLE);
        return mCloudView;
    }

    public SurfaceView getSurfaceView() {
        mCloudView.setVisibility(GONE);
        mTextureView.setVisibility(GONE);
        mSurfaceView.setVisibility(VISIBLE);
        return mSurfaceView;
    }

    public TextureView getTextureView() {
        mCloudView.setVisibility(GONE);
        mSurfaceView.setVisibility(GONE);
        mTextureView.setVisibility(VISIBLE);
        return mTextureView;
    }

    public void showLoading() {
        mTvLoading.setVisibility(VISIBLE);
    }

    public void dismissLoading() {
        mTvLoading.setVisibility(GONE);
    }

    public void setVolumeProgress(int volume) {
        mPbVolume.setProgress(volume);
    }

    public void updatePlayerStatus(boolean playing) {
        if (playing) {
            ((ImageView)findViewById(R.id.iv_start)).setImageResource(R.drawable.lebplayer_ic_bottom_stop);
        } else {
            ((ImageView)findViewById(R.id.iv_start)).setImageResource(R.drawable.lebplayer_ic_bottom_start);
        }
    }

    public void updateMuteVideoStatus(boolean mute) {
        if (mute) {
            ((ImageView) findViewById(R.id.iv_camera)).setImageResource(R.drawable.lebplayer_ic_remote_video_off);
        } else {
            ((ImageView) findViewById(R.id.iv_camera)).setImageResource(R.drawable.lebplayer_ic_remote_video_on);
        }
    }

    public void updateMuteAudioStatus(boolean mute) {
        if (mute) {
            ((ImageView) findViewById(R.id.iv_mic)).setImageResource(R.drawable.lebplayer_new_ic_bottom_mic_off);
        } else {
            ((ImageView) findViewById(R.id.iv_mic)).setImageResource(R.drawable.lebplayer_ic_bottom_mic_on);
        }
    }

    private ILiveRenderViewSwitchCallback mCallback;

    public void setSwitchListener(ILiveRenderViewSwitchCallback callback) {
        mCallback = callback;
    }

    public interface ILiveRenderViewSwitchCallback {

        void onMuteVideo(View view);

        void onMuteAudio(View view);

        void onShowSetting();

        void onShowDebugView(View view);

        void onStart(View view);
    }

}
