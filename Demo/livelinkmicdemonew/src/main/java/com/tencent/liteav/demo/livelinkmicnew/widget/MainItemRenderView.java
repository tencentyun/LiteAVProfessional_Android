package com.tencent.liteav.demo.livelinkmicnew.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.liteav.demo.livelinkmicnew.R;
import com.tencent.rtmp.ui.TXCloudVideoView;


public class MainItemRenderView extends FrameLayout {

    private ProgressBar         mPbVolume;
    private TextView            mTvLoading;
    private TXCloudVideoView    mCloudView, mSmallCloudView;
    private SurfaceView         mSurfaceView, mSmallSurfaceView;
    private TextureView         mTextureView, mSmallTextureView;
    private boolean             mIsBigView = true;
    private CheckBox            mCbFirstVideo, mCbFirstAudio;
    private ImageView           mIvSnapshot, mIvQRCode;
    private ImageView           mIconAdd;
    private TextView            mRenderTextTips;
    private LinearLayout        mControlLayout;
    private AudioEffectPanel    mAudioEffectPanel;
    private BeautyPanel mBeautyPanel;
    private TXAudioEffectManager mAudioEffectManager;

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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.live_link_mic_new_item_layout_live_render, this, true);
        mControlLayout = (LinearLayout) findViewById(R.id.ll_control);
        mTvLoading = (TextView) findViewById(R.id.render_tv_loading);
        mPbVolume = (ProgressBar) findViewById(R.id.render_pb_volume);
        mCloudView = (TXCloudVideoView) findViewById(R.id.render_cloud_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.render_surface_view);
        mTextureView = (TextureView) findViewById(R.id.render_texture_view);
        mSmallCloudView = (TXCloudVideoView) findViewById(R.id.render_cloud_view_small);
        mSmallSurfaceView = (SurfaceView) findViewById(R.id.render_surface_view_small);
        mSmallTextureView = (TextureView) findViewById(R.id.render_texture_view_small);
        mCbFirstVideo = (CheckBox) findViewById(R.id.render_cb_video);
        mCbFirstVideo.setButtonDrawable(R.drawable.live_link_mic_new_bg_checkbox);
        mCbFirstAudio = (CheckBox) findViewById(R.id.render_cb_audio);
        mCbFirstAudio.setButtonDrawable(R.drawable.live_link_mic_new_bg_checkbox);
        mIvSnapshot = (ImageView) findViewById(R.id.render_iv_snapshot);
        mIvQRCode = (ImageView) findViewById(R.id.render_iv_qrcode);
        mIconAdd = (ImageView) findViewById(R.id.render_add);
        mRenderTextTips = (TextView) findViewById(R.id.render_text_tips);
        mIvQRCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvQRCode.setVisibility(GONE);
            }
        });
        mAudioEffectPanel = (AudioEffectPanel) findViewById(R.id.livepusher_audio_panel);
        mBeautyPanel = (BeautyPanel) findViewById(R.id.livepusher_beauty_panel);
        findViewById(R.id.render_btn_switch_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View bigView = isVisiable(mCloudView) ? mCloudView : isVisiable(mSurfaceView) ? mSurfaceView : isVisiable(mTextureView) ? mTextureView : null;
                View smallView = isVisiable(mCloudView) ? mSmallCloudView : isVisiable(mSurfaceView) ? mSmallSurfaceView : isVisiable(mTextureView) ? mSmallTextureView : null;
                if (bigView != null && smallView != null) {
                    mIsBigView = !mIsBigView;
                    if (mIsBigView) {
                        smallView.setVisibility(GONE);
                        if (mCallback != null) {
                            mCallback.onSwitchView(bigView);
                        }
                    } else {
                        smallView.setVisibility(VISIBLE);
                        if (mCallback != null) {
                            mCallback.onSwitchView(smallView);
                        }
                    }
                }

            }
        });

        findViewById(R.id.render_btn_snapshot).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onClickSnapshot();
                }
            }
        });
        findViewById(R.id.render_btn_restart).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onRestart();
                }
            }
        });

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

        findViewById(R.id.iv_switch_camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onCameraChange(view);
                }
            }
        });

        findViewById(R.id.iv_beauty).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onShowBeautyPanel(view);
                }
            }
        });

        findViewById(R.id.iv_bgm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onShowBGMPanel(view);
                }
            }
        });

        findViewById(R.id.iv_fullscreen).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onFullScreenChange(view);
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
        findViewById(R.id.ic_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onClose(view);
                }
            }
        });
    }

    private boolean isVisiable(View view) {
        return view.getVisibility() == View.VISIBLE;
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

    public void recvFirstVideo(boolean isRecv) {
        mCbFirstVideo.setChecked(isRecv);
        if (!isRecv) {
            // 相当于重置窗台
            mIsBigView = true;
        }
    }

    public void showSnapshot(Bitmap bitmap) {
        if (bitmap == null) return;
        mIvSnapshot.setImageBitmap(bitmap);
    }

    public void showQRCode(Bitmap bitmap) {
        if (bitmap == null) return;
        mIvQRCode.setImageBitmap(bitmap);
    }

    public void showAddIcon() {
        mIconAdd.setVisibility(View.VISIBLE);
        mRenderTextTips.setVisibility(View.VISIBLE);
    }

    public void hideAddIcon() {
        mIconAdd.setVisibility(View.GONE);
        mRenderTextTips.setVisibility(View.GONE);
    }

    public void showControlLayout() {
        mControlLayout.setVisibility(View.VISIBLE);
    }

    public void hideControlLayout() {
        mControlLayout.setVisibility(View.GONE);
    }

    public void showOrHideAudioPanel() {
        if (mAudioEffectPanel.isShown()) {
            mAudioEffectPanel.setVisibility(View.GONE);
            mAudioEffectPanel.hideAudioPanel();
            showControlLayout();
        } else {
            mAudioEffectPanel.setVisibility(View.VISIBLE);
            mAudioEffectPanel.showAudioPanel();
            hideControlLayout();
        }
    }

    public void hideAudioEffectPanel() {
        mAudioEffectPanel.setVisibility(View.GONE);
        mAudioEffectPanel.hideAudioPanel();
        showControlLayout();
    }

    public void hideBeautyPanel() {
        mBeautyPanel.setVisibility(View.GONE);
        showControlLayout();
    }

    public void setAudioEffectManager(TXAudioEffectManager audioEffectManager) {
        mAudioEffectManager = audioEffectManager;
        mAudioEffectPanel.setAudioEffectManager(mAudioEffectManager);
        mAudioEffectPanel.setBackgroundColor(0xff13233F);
        mAudioEffectPanel.setOnAudioEffectPanelHideListener(new AudioEffectPanel.OnAudioEffectPanelHideListener() {
            @Override
            public void onClosePanel() {
                mAudioEffectPanel.setVisibility(View.GONE);
                showControlLayout();
            }
        });
    }

    public void destroyAudioEffect() {
        if (mAudioEffectPanel != null) {
            mAudioEffectPanel.reset();
            mAudioEffectPanel.unInit();
        }
    }

    public void showOrHideBeautyPanel(TXBeautyManager beautyManager) {
        mBeautyPanel.setBeautyManager(beautyManager);
        mBeautyPanel.setBackgroundColor(0xff13233F);
        mBeautyPanel.setOnAudioEffectPanelHideListener(new BeautyPanel.OnBeautyPanelHideListener() {
            @Override
            public void onClosePanel() {
                mBeautyPanel.setVisibility(View.GONE);
                showControlLayout();
            }
        });
        if (mBeautyPanel.isShown()) {
            mBeautyPanel.setVisibility(View.GONE);
            showControlLayout();
        } else {
            mBeautyPanel.setVisibility(View.VISIBLE);
            hideControlLayout();
        }
    }

    public void hideExtraInfoView() {
        findViewById(R.id.ll_setting).setVisibility(View.GONE);
        findViewById(R.id.ll_info).setVisibility(View.GONE);
        findViewById(R.id.ll_beauty).setVisibility(View.GONE);
        findViewById(R.id.ll_bgm).setVisibility(View.GONE);
        findViewById(R.id.ll_start).setVisibility(View.GONE);
    }

    public void showExtraInfoView() {
        findViewById(R.id.ll_setting).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_info).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_beauty).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_bgm).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_start).setVisibility(View.VISIBLE);
    }

    public void hidePushFeatureView() {
        findViewById(R.id.ll_switch_camera).setVisibility(View.GONE);
        findViewById(R.id.ll_bgm).setVisibility(View.GONE);
        findViewById(R.id.ll_beauty).setVisibility(View.GONE);
    }

    public void showFullScreenView() {
        findViewById(R.id.ll_fullscreen).setVisibility(View.VISIBLE);
    }

    public void showCloseButton() {
        findViewById(R.id.ic_close).setVisibility(View.VISIBLE);
    }

    public View getPlayButton() {
        return findViewById(R.id.iv_start);
    }

    public View getLogButton() {
        return findViewById(R.id.iv_info);
    }

    public View getSwitchCameraButton() {
        return findViewById(R.id.iv_switch_camera);
    }

    public View getCameraButton() {
        return findViewById(R.id.iv_camera);
    }

    public View getMicButton() {
        return findViewById(R.id.iv_mic);
    }

    public void hideCloseButton() {
        findViewById(R.id.ic_close).setVisibility(View.GONE);
    }

    public void hideFullScreenView() {
        findViewById(R.id.ll_fullscreen).setVisibility(View.GONE);
    }

    public void hideDebugView() {
        mCbFirstAudio.setVisibility(View.GONE);
        mCbFirstVideo.setVisibility(View.GONE);

        findViewById(R.id.render_btn_snapshot).setVisibility(View.GONE);
        findViewById(R.id.render_btn_restart).setVisibility(View.GONE);
        findViewById(R.id.render_btn_switch_view).setVisibility(View.GONE);
    }

    public void setRenderTextTips (String text) {
        mRenderTextTips.setText(text);
    }

    public void recvFirstAudio(boolean isRecv) {
        mCbFirstAudio.setChecked(isRecv);
    }

    private ILiveRenderViewSwitchCallback mCallback;

    public void setSwitchListener(ILiveRenderViewSwitchCallback callback) {
        mCallback = callback;
    }

    public interface ILiveRenderViewSwitchCallback {
        void onSwitchView(View view);

        void onClickSnapshot();

        void onRestart();

        void onCameraChange(View view);

        void onMuteVideo(View view);

        void onMuteAudio(View view);

        void onFullScreenChange(View view);

        void onShowSetting();

        void onShowDebugView(View view);

        void onShowBeautyPanel(View view);

        void onShowBGMPanel(View view);

        void onStart(View view);

        void onClose(View view);
    }

}
