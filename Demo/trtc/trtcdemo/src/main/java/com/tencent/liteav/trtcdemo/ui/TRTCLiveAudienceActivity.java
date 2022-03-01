package com.tencent.liteav.trtcdemo.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.liteav.audiosettingkit.AudioEffectPanel;
import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.demo.beauty.view.BeautyPanel;
import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.demo.common.view.ContentLoadingDialog;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.bean.Constant;
import com.tencent.liteav.trtcdemo.model.bean.MoreConfig;
import com.tencent.liteav.trtcdemo.model.bean.PkConfig;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.customcapture.AudioFrameReader;
import com.tencent.liteav.trtcdemo.model.customcapture.CustomCaptureVideo;
import com.tencent.liteav.trtcdemo.model.customcapture.CustomRenderVideo;
import com.tencent.liteav.trtcdemo.model.customcapture.VideoFrameReader;
import com.tencent.liteav.trtcdemo.model.customcapture.structs.TextureFrame;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.CustomAudioCapturor;
import com.tencent.liteav.trtcdemo.model.helper.RemoteUserConfigHelper;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.listener.TRTCCloudManagerListener;
import com.tencent.liteav.trtcdemo.model.listener.TRTCVideoBytesFrameListener;
import com.tencent.liteav.trtcdemo.model.listener.TRTCVideoTextureFrameListener;
import com.tencent.liteav.trtcdemo.model.manager.CdnPlayManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCRemoteUserManager;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusListener;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusManager;
import com.tencent.liteav.trtcdemo.ui.dialog.CdnPanelDialogFragment;
import com.tencent.liteav.trtcdemo.ui.dialog.RemoteUserPanelDialogFragment;
import com.tencent.liteav.trtcdemo.ui.dialog.SettingPanelDialogFragment;
import com.tencent.liteav.trtcdemo.ui.widget.lrcview.LrcView;
import com.tencent.liteav.trtcdemo.ui.widget.videolayout.TRTCVideoLayoutManager;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudDef.TRTCVideoFrame;
import com.tencent.trtc.TRTCStatistics;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_EARPIECEMODE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_QUALITY;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_SCENE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_VOLUMETYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CHORUS_CDN_URL;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_AUDIO_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_RENDER_BUFFER_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_RENDER_PIXEL_FORMAT;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_VIDEO_PREPROCESS_BUFFER_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_VIDEO_PREPROCESS_PIXEL_FORMAT;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ENCODER_265;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ENCODER_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_LOCAL_RENDER_VIEW_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_MAIN_SCREEN_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_NET_ENV_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_PERFORMANCE_MODE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_RECEIVED_AUDIO;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_RECEIVED_VIDEO;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROOM_ID_STR;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_SUB_SCREEN_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USER_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USE_STRING_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_VIDEO_FILE_PATH;
import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_ERROR_DISCONNECTED;

public class TRTCLiveAudienceActivity extends AppCompatActivity implements View.OnClickListener,
        TRTCCloudManagerListener,
        TRTCCloudManager.IView,
        TRTCRemoteUserManager.IView,
        CustomAudioCapturor.TXICustomAudioCapturorListener {

    private static final String TAG         = TRTCLiveAudienceActivity.class.getSimpleName();
    private static final int    SAMPLE_RATE = 48000;
    private static final int    CHANNELS    = 1;

    private TRTCCloudManager              mTRTCCloudManager;
    private TRTCRemoteUserManager         mTRTCRemoteUserManager;
    private CdnPlayManager                mCdnPlayManager;
    private TRTCVideoLayoutManager        mTRTCVideoLayout;
    private TextView                      mTextRoomId;
    private ImageView                     mImageSwitchRole;
    private SettingPanelDialogFragment    mSettingPanelFragmentDialog;
    private RemoteUserPanelDialogFragment mRemoteUserManagerFragmentDialog;
    private BeautyPanel                   mPanelBeautyControl;
    private AudioEffectPanel              mPanelAudioControl;
    private CdnPanelDialogFragment        mCdnPanelFragmentDialog;
    private ImageView                     mImageSwitchCamera;
    private ImageView                     mImageEnableCamera;
    private ImageView                     mImageEnableAudio;
    private ImageView                     mImageMoreTrtc;
    private ImageView                     getmImageAudienceSwitchRole;
    private Group                         mCdnPlayViewGroup;
    private Group                         mRoleAudienceGroup;
    private Group                         mRoleAnchorGroup;
    private TXCloudVideoView              mCdnPlayView;
    private Button                        mButtonSwitchCdn;
    private ProgressDialog                mLoadingDialog;
    private Handler                       mMainHandler;
    private boolean                       mIsCustomCapture       = false;
    private boolean                       mIsCustomAudioCapture  = false;
    private String                        mVideoFilePath;
    private CustomCaptureVideo            mCustomCapture;
    private CustomRenderVideo             mCustomRender;
    private CustomAudioCapturor           mCustomAudioCapturor;
    private int                           mLogLevel              = 0;
    private boolean                       mIsCdnPlay             = false;
    private boolean                       mIsNeedSwitchCdn       = false;
    private String                        mMainUserId            = "";
    private int                           mVolumeType            = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
    private int                           mAudioQuality          = TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT;
    private int                           mAppScene              = TRTCCloudDef.TRTC_APP_SCENE_LIVE;
    private boolean                       mIsAudioEarpieceMode   = false;
    private boolean                       mIsScreenCapture       = false;
    private int                           mRoomIdType            = 0; //0:数字   1：字符串
    private int                           mRoomId;
    private String                        mRoomIdStr;
    private String                        mUserId;
    private int                           mRole                  = TRTCCloudDef.TRTCRoleAudience;
    private int                           mNetEnvType            = Constant.NetEnv.TYPE_PRODUCT;
    private boolean                       mSubScreenCapture      = false;
    private boolean                       mReceivedVideo         = true;
    private boolean                       mReceivedAudio         = true;
    private boolean                       mUse265                = true;
    private int                           mEncoderType           = Constant.ENCODER_AUTO;
    private boolean                       mEnablePerformanceMode = false;
    private boolean                       mIsCustomLocalRender   = false;
    private int                           mLocalRenderViewType   = Constant.TRTCViewType.TYPE_GLSURFACE_VIEW;
    private int                           mBufferType            = 0;
    private int                           mPixelFormat           = 0;
    private boolean                       mIsChorusMode          = false;
    private boolean                       mIsExited              = false;
    private String                        mChorusCdnUrl;
    private TRTCChorusManager             mChorusManager;
    private LrcView                       mLrcView;
    private ImageView                     mIvChorus;
    private ImageButton                   mImageCDN;

    private Runnable mLoadingTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            dismissLoading();
        }
    };


    private final V2TXLivePlayerObserver mObserver = new V2TXLivePlayerObserver() {

        @Override
        public void onReceiveSeiMessage(V2TXLivePlayer player, int payloadType, byte[] data) {
            if (data != null) {
                String seiMessage = "";
                if (data != null && data.length > 0) {
                    try {
                        seiMessage = new String(data, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ToastUtils.showLong(seiMessage);
            }
        }

        @Override
        public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extraInfo);
            dismissLoading();
            ToastUtils.showLong(getString(R.string.trtcdemo_play_fail) + msg);
            if (code == V2TXLIVE_ERROR_DISCONNECTED && null != player) {
                player.stopPlay();
            }
        }

        @Override
        public void onVideoPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
            Log.d(TAG, "[Player] onVideoPlaying: firstPlay -> " + firstPlay);
            dismissLoading();
            ToastUtils.showLong(getString(R.string.trtcdemo_play_success));
        }

        @Override
        public void onAudioPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
            Log.d(TAG, "[Player] onAudioPlaying: firstPlay -> " + firstPlay);
            dismissLoading();
            ToastUtils.showLong(getString(R.string.trtcdemo_play_success));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.BeautyTheme);
        initIntentData();
        setContentView(R.layout.trtcdemo_activity_live_audience_room);
        initTRTCSDK();
        initViews();
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE, PermissionConstants.STORAGE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                if (!(mRole == TRTCCloudDef.TRTCRoleAudience && mIsChorusMode)) {
                    // 合唱模式下，观众不进房
                    enterRoom();
                }
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtils.showShort(R.string.trtcdemo_permission_tips);
                exitTRTCLiveAudience();
            }
        }).request();
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void initIntentData() {
        Intent intent = getIntent();
        mUserId = intent.getStringExtra(KEY_USER_ID);
        mRoomIdType = intent.getIntExtra(KEY_USE_STRING_ROOM_ID, 0);
        mIsCustomCapture = intent.getBooleanExtra(KEY_CUSTOM_CAPTURE, false);
        mIsCustomAudioCapture = intent.getBooleanExtra(KEY_CUSTOM_AUDIO_CAPTURE, false);
        mIsScreenCapture = intent.getBooleanExtra(KEY_MAIN_SCREEN_CAPTURE, false);
        mVolumeType = intent.getIntExtra(KEY_AUDIO_VOLUMETYPE, TRTCCloudDef.TRTCSystemVolumeTypeAuto);
        mAudioQuality = intent.getIntExtra(KEY_AUDIO_QUALITY, TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT);
        mIsAudioEarpieceMode = intent.getBooleanExtra(KEY_AUDIO_EARPIECEMODE, false);
        mVideoFilePath = intent.getStringExtra(KEY_VIDEO_FILE_PATH);
        if (isEnableDebugMode()) {
            mNetEnvType = intent.getIntExtra(KEY_NET_ENV_TYPE, Constant.NetEnv.TYPE_PRODUCT);
            mSubScreenCapture = intent.getBooleanExtra(KEY_SUB_SCREEN_CAPTURE, false);
            mReceivedVideo = intent.getBooleanExtra(KEY_RECEIVED_VIDEO, true);
            mReceivedAudio = intent.getBooleanExtra(KEY_RECEIVED_AUDIO, true);
            mUse265 = intent.getBooleanExtra(KEY_ENCODER_265, false);
            mEncoderType = intent.getIntExtra(KEY_ENCODER_TYPE, Constant.ENCODER_AUTO);
            mEnablePerformanceMode = intent.getBooleanExtra(KEY_PERFORMANCE_MODE, false);
            mLocalRenderViewType = intent.getIntExtra(KEY_LOCAL_RENDER_VIEW_TYPE, Constant.TRTCViewType.TYPE_GLSURFACE_VIEW);
            mBufferType = intent.getIntExtra(KEY_CUSTOM_VIDEO_PREPROCESS_BUFFER_TYPE, 0);
            mPixelFormat = intent.getIntExtra(KEY_CUSTOM_VIDEO_PREPROCESS_PIXEL_FORMAT, 0);
            mIsCustomLocalRender = Constant.TRTCViewType.TYPE_CUSTOM_VIEW == mLocalRenderViewType;
            mIsChorusMode = intent.getBooleanExtra(KEY_AUDIO_SCENE, false);
            mChorusCdnUrl = getIntent().getStringExtra(KEY_CHORUS_CDN_URL);
        }
        if (mRoomIdType == 1) {
            mRoomIdStr = intent.getStringExtra(KEY_ROOM_ID_STR);
        } else {
            mRoomId = intent.getIntExtra(KEY_ROOM_ID, 0);
        }
        if (mIsCustomCapture) {
            mIsCustomLocalRender = true;
            mIsCustomAudioCapture = false;
        }
    }

    private void enterRoom() {
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        if (mRole == TRTCCloudDef.TRTCRoleAnchor) {
            startLocalPreview();
            videoConfig.setEnableVideo(true);
            videoConfig.setPublishVideo(true);
            Bitmap bitmap = decodeResource(getResources(), R.drawable.trtcdemo_mute_image);
            videoConfig.setMuteImage(bitmap);
            mImageEnableCamera.setImageResource(videoConfig.isEnableVideo()
                    ? R.drawable.trtcdemo_remote_video_enable : R.drawable.trtcdemo_remote_video_disable);
        } else {
            videoConfig.setEnableVideo(false);
            videoConfig.setPublishVideo(false);
        }

        mTRTCCloudManager.enterRoom();

        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        if (mRole == TRTCCloudDef.TRTCRoleAnchor) {
            if (mIsCustomAudioCapture) {
                mCustomAudioCapturor.start(SAMPLE_RATE, CHANNELS);
            } else {
                mTRTCCloudManager.startLocalAudio();
            }
            audioConfig.setAudioCapturingStarted(true);
            audioConfig.setLocalAudioMuted(false);
        } else {
            audioConfig.setAudioCapturingStarted(false);
        }
        mImageEnableAudio.setImageResource(audioConfig.isLocalAudioMuted() ? R.drawable.trtcdemo_mic_disable : R.drawable.trtcdemo_mic_enable);
    }

    private void exitRoom() {
        stopLocalPreview();
        if (mCustomAudioCapturor != null) {
            mCustomAudioCapturor.stop();
        }
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        if (audioConfig.isRecording()) {
            mTRTCCloudManager.stopRecord();
        }
        audioConfig.setRecording(false);
        MoreConfig moreConfig = SettingConfigHelper.getInstance().getMoreConfig();
        if (moreConfig.isRecording()) {
            mTRTCCloudManager.stopLocalRecording();
        }
        moreConfig.setRecording(false);
        mTRTCCloudManager.exitRoom();
    }

    private void initViews() {
        mTextRoomId = findViewById(R.id.trtc_tv_room_id);
        mImageSwitchCamera = findViewById(R.id.trtc_iv_camera);
        mImageEnableCamera = findViewById(R.id.iv_camera_on_off);
        setVideoEnableListener();
        mImageEnableAudio = findViewById(R.id.trtc_iv_mic);
        getmImageAudienceSwitchRole = findViewById(R.id.trtc_iv_switch_role_audience);
        mImageCDN = findViewById(R.id.trtc_ib_cdn);
        mIvChorus = findViewById(R.id.trtc_iv_chorus);
        mIvChorus.setOnClickListener(this);
        findViewById(R.id.trtc_iv_beauty).setOnClickListener(this);
        findViewById(R.id.trtc_iv_log).setOnClickListener(this);
        findViewById(R.id.trtc_iv_setting).setOnClickListener(this);
        findViewById(R.id.trtc_ib_back).setOnClickListener(this);
        findViewById(R.id.trtc_iv_music).setOnClickListener(this);
        findViewById(R.id.trtc_iv_log_audience).setOnClickListener(this);
        findViewById(R.id.trtc_iv_more_audience).setOnClickListener(this);
        findViewById(R.id.trtc_iv_switch_role_audience).setOnClickListener(this);
        mImageEnableAudio.setOnClickListener(this);
        mImageSwitchCamera.setOnClickListener(this);
        mImageEnableCamera.setOnClickListener(this);

        if (mRoomIdType == 0) {
            mTextRoomId.setText(mRoomId + "");
        } else {
            mTextRoomId.setText(mRoomIdStr);
        }

        mCdnPlayView = findViewById(R.id.trtc_cdn_play_view);
        mCdnPlayViewGroup = (Group) findViewById(R.id.trtc_cdn_view_group);
        mCdnPlayViewGroup.setVisibility(View.GONE);
        mButtonSwitchCdn = (Button) findViewById(R.id.btn_switch_cdn);
        mRoleAudienceGroup = (Group) findViewById(R.id.group_role_audience);
        mRoleAnchorGroup = findViewById(R.id.group_role_anchor);

        mImageMoreTrtc = (ImageView) findViewById(R.id.trtc_iv_more);
        mImageMoreTrtc.setOnClickListener(this);

        mPanelAudioControl = findViewById(R.id.anchor_audio_panel);
        mPanelAudioControl.setAudioEffectManager(mTRTCCloudManager.getAudioEffectManager());
        mPanelAudioControl.initPanelDefaultBackground();

        mPanelBeautyControl = (BeautyPanel) findViewById(R.id.trtc_beauty_panel);
        mPanelBeautyControl.setBeautyManager(mTRTCCloudManager.getBeautyManager());

        mRemoteUserManagerFragmentDialog = new RemoteUserPanelDialogFragment();
        mRemoteUserManagerFragmentDialog.setTRTCRemoteUserManager(mTRTCRemoteUserManager);
        RemoteUserConfigHelper.getInstance().clear();

        mTRTCVideoLayout = (TRTCVideoLayoutManager) findViewById(R.id.trtc_video_view_layout);

        mSettingPanelFragmentDialog = new SettingPanelDialogFragment();
        mSettingPanelFragmentDialog.setTRTCCloudManager(mTRTCCloudManager, mTRTCRemoteUserManager, mTRTCVideoLayout);

        mImageSwitchRole = (ImageView) findViewById(R.id.trtc_iv_switch_role);
        mImageSwitchRole.setOnClickListener(this);
        mButtonSwitchCdn.setOnClickListener(this);

        if (mRole == TRTCCloudDef.TRTCRoleAnchor) {
            mRoleAudienceGroup.setVisibility(View.VISIBLE);
            mRoleAnchorGroup.setVisibility(View.GONE);
            mImageSwitchRole.setVisibility(View.GONE);
            mButtonSwitchCdn.setVisibility(View.GONE);
        } else {
            mRoleAudienceGroup.setVisibility(View.GONE);
            mRoleAnchorGroup.setVisibility(View.VISIBLE);
            mImageSwitchRole.setVisibility(View.VISIBLE);
            mButtonSwitchCdn.setVisibility(View.VISIBLE);
        }
        mImageSwitchCamera.setImageResource(mTRTCCloudManager.isFontCamera() ? R.drawable.trtcdemo_ic_camera_back : R.drawable.trtcdemo_ic_camera_front);

        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setMessage(getString(R.string.trtcdemo_switching));
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mMainHandler = new Handler();
        mLrcView = (LrcView) findViewById(R.id.lrc_view);
        String mainLrcText = getLrcText("153307-yc.lrc");
        mLrcView.loadLrc(mainLrcText, null);
        if (!mIsChorusMode) {
            mLrcView.setVisibility(View.GONE);
            mIvChorus.setVisibility(View.GONE);
        } else {
            getmImageAudienceSwitchRole.setVisibility(View.GONE);
            mButtonSwitchCdn.setVisibility(View.GONE);
            mImageSwitchRole.setVisibility(View.GONE);
            mCdnPlayViewGroup.setVisibility(View.VISIBLE);
            mTRTCVideoLayout.setVisibility(View.GONE);
        }
        mImageCDN.setVisibility(View.GONE);
    }

    private void setVideoEnableListener() {
        SettingConfigHelper.getInstance().getVideoConfig()
                .setVideoEnableListener(new VideoConfig.VideoEnableListener() {
                    @Override
                    public void onChecked(boolean isEnable) {
                        if (isEnable) {
                            mImageEnableCamera.setImageResource(R.drawable.trtcdemo_remote_video_enable);
                        } else {
                            mImageEnableCamera.setImageResource(R.drawable.trtcdemo_remote_video_disable);
                        }
                    }
                });
    }

    private String getLrcText(String fileName) {
        String lrcText = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            lrcText = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrcText;
    }

    private void initTRTCSDK() {
        TRTCCloudDef.TRTCParams mTRTCParams = new TRTCCloudDef.TRTCParams();
        mTRTCParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        mTRTCParams.userId = mUserId;
        String userSig = UserModelManager.getInstance().getUserModel().userNameSig;
        if (TextUtils.isEmpty(userSig)) {
            userSig = GenerateTestUserSig.genTestUserSig(mUserId);
            UserModelManager.getInstance().getUserModel().userNameSig = userSig;
        }
        mTRTCParams.userSig = userSig;
        if (mRoomIdType == 0) {
            mTRTCParams.roomId = mRoomId;
        } else {
            mTRTCParams.strRoomId = mRoomIdStr;
        }
        mTRTCParams.role = mRole;
        mTRTCCloudManager = new TRTCCloudManager(this, mTRTCParams, mAppScene);
        mTRTCCloudManager.setViewListener(this);
        mTRTCCloudManager.setTRTCListener(this);
        SettingConfigHelper.getInstance().getAudioConfig().setAudioQulity(mAudioQuality);
        mTRTCCloudManager.setAudioQuality(mAudioQuality);
        if (isEnableDebugMode()) {
            mTRTCCloudManager.mUse265Encode = mUse265;
            mTRTCCloudManager.mEncoderType = mEncoderType;
        }
        mTRTCCloudManager.initTRTCManager(mIsCustomCapture, mReceivedAudio, mReceivedVideo, mEnablePerformanceMode);
        mTRTCCloudManager.setSystemVolumeType(mVolumeType);
        mTRTCCloudManager.enableAudioHandFree(mIsAudioEarpieceMode);
        SettingConfigHelper.getInstance().getAudioConfig().setAudioEarpieceMode(mIsAudioEarpieceMode);

        mTRTCRemoteUserManager = new TRTCRemoteUserManager(this, this, mIsCustomCapture,
                getIntent().getIntExtra(KEY_CUSTOM_RENDER_PIXEL_FORMAT, -1),
                getIntent().getIntExtra(KEY_CUSTOM_RENDER_BUFFER_TYPE, -1),
                false);
        mTRTCRemoteUserManager.setMixUserId(mUserId);

        if (mIsCustomCapture) {
            mCustomCapture = new CustomCaptureVideo(this, mVideoFilePath, true);
            mCustomRender = new CustomRenderVideo(mUserId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                    false);
        }

        if (mIsCustomAudioCapture) {
            mCustomAudioCapturor = CustomAudioCapturor.getInstance();
            mCustomAudioCapturor.setCustomAudioCaptureListener(this);
            mTRTCCloudManager.enableCustomAudioCapture(true);
        }
        if (isEnableDebugMode()) {
            // 设置云端环境
            mTRTCCloudManager.setNetEnv(mNetEnvType);
            mTRTCCloudManager.mUse265Encode = mUse265;
            if (mBufferType == TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE) {
                mTRTCCloudManager.setLocalVideoProcessListener(TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D,
                        TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE, new TRTCVideoTextureFrameListener());
            } else if (mBufferType != TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_UNKNOWN) {
                mTRTCCloudManager.setLocalVideoProcessListener(mPixelFormat, mBufferType, new TRTCVideoBytesFrameListener());
            } else {
                mTRTCCloudManager.setLocalVideoProcessListener(TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_NV21,
                        TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_BUFFER, null);
            }
        }

        if (mIsChorusMode) {
            mChorusManager = new TRTCChorusManager(this, TRTCCloud.sharedInstance(this));
            try {
                Class<?> clazz = getClassLoader().loadClass("com.tencent.liteav.trtcdemo.model.manager.chorus"
                        + ".TRTCChorusManagerImpl");
                Constructor constructor = clazz.getConstructor(Context.class, TRTCCloud.class);
                mChorusManager = (TRTCChorusManager) constructor.newInstance(this, TRTCCloud.sharedInstance(this));
            } catch (Exception e) {
                Log.e(TAG, "create TRTCChorusManagerImpl new instance failed.", e);
            }

            mChorusManager.setListener(new TRTCChorusListener() {
                @Override
                public void onChorusStart(TRTCChorusDef.ChorusStartReason reason) {
                    mIvChorus.setImageResource(R.drawable.trtcdemo_ic_chorus_stop);
                }

                @Override
                public void onChorusProgress(long curPtsMS, long durationMS) {
                    mLrcView.updateTime(curPtsMS);
                }

                @Override
                public void onChorusStop(TRTCChorusDef.ChorusStopReason reason) {
                    if (reason == TRTCChorusDef.ChorusStopReason.MusicPlayFailed) {
                        ToastUtils.showShort(getString(R.string.trtcdemo_chorus_playback_failed));
                    }
                    mIvChorus.setImageResource(R.drawable.trtcdemo_ic_chorus_start);
                    mLrcView.updateTime(0);
                }

                @Override
                public void onCdnPushStatusUpdate(TRTCChorusDef.CdnPushStatus status) {

                }

                @Override
                public void onCdnPlayStatusUpdate(TRTCChorusDef.CdnPlayStatus status) {
                    switch (status) {
                        case Loading:
                            showLoading();
                            break;
                        case Playing:
                            dismissLoading();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.trtc_ib_back) {
            handleBackPressed();
        } else if (id == R.id.trtc_iv_switch_role || id == R.id.trtc_iv_switch_role_audience) {
            switchRole();
        } else if (id == R.id.trtc_iv_beauty) {
            if (mPanelBeautyControl.isShown()) {
                mPanelBeautyControl.setVisibility(View.GONE);
            } else {
                mPanelBeautyControl.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.trtc_iv_camera) {
            mTRTCCloudManager.switchCamera();
            ((ImageView) v).setImageResource(mTRTCCloudManager.isFontCamera() ? R.drawable.trtcdemo_ic_camera_back : R.drawable.trtcdemo_ic_camera_front);
        } else if (id == R.id.iv_camera_on_off) {
            if (SettingConfigHelper.getInstance().getVideoConfig().isEnableVideo()) {
                SettingConfigHelper.getInstance().getVideoConfig().setEnableVideo(false);
                mTRTCCloudManager.stopLocalPreview();
            } else {
                SettingConfigHelper.getInstance().getVideoConfig().setEnableVideo(true);
                mTRTCCloudManager.startLocalPreview();
            }
            ((ImageView) v).setImageResource(SettingConfigHelper.getInstance().getVideoConfig().isEnableVideo() ? R.drawable.trtcdemo_remote_video_enable : R.drawable.trtcdemo_remote_video_disable);
        } else if (id == R.id.trtc_iv_mic) {
            AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
            audioConfig.setLocalAudioMuted(!audioConfig.isLocalAudioMuted());
            mTRTCCloudManager.muteLocalAudio(audioConfig.isLocalAudioMuted());
            ((ImageView) v).setImageResource(audioConfig.isLocalAudioMuted() ? R.drawable.trtcdemo_mic_disable : R.drawable.trtcdemo_mic_enable);
        } else if (id == R.id.trtc_iv_log || id == R.id.trtc_iv_log_audience) {
            if (mIsCdnPlay) {
                mLogLevel = (mLogLevel + 1) % 2;
                ((ImageView) v).setImageResource((0 == mLogLevel) ? R.drawable.trtcdemo_log_hidden : R.drawable.trtcdemo_log_show);
                mCdnPlayManager.setDebug(1 == mLogLevel);
            } else {
                mLogLevel = (mLogLevel + 1) % 3;
                ((ImageView) v).setImageResource((0 == mLogLevel) ? R.drawable.trtcdemo_log_hidden : R.drawable.trtcdemo_log_show);
                mTRTCCloudManager.showDebugView(mLogLevel);
            }
        } else if (id == R.id.trtc_iv_setting) {
            showDialogFragment(mSettingPanelFragmentDialog, "FeatureSettingFragmentDialog");
        } else if (id == R.id.trtc_iv_more || id == R.id.trtc_iv_more_audience) {
            if (mIsCdnPlay) {
                if (mCdnPanelFragmentDialog == null) {
                    // cdn播放设置
                    mCdnPanelFragmentDialog = new CdnPanelDialogFragment();
                    if (mCdnPlayManager == null) {
                        mCdnPlayManager = new CdnPlayManager(mCdnPlayView, mObserver);
                    }
                    mCdnPanelFragmentDialog.setCdnPlayManager(mCdnPlayManager);
                }
                showDialogFragment(mCdnPanelFragmentDialog, "CdnPlayerSettingFragmentDialog");
            } else {
                showDialogFragment(mRemoteUserManagerFragmentDialog, "RemoteUserManagerFragmentDialog");
            }
        } else if (id == R.id.trtc_iv_music) {
            mPanelAudioControl.setVisibility(View.VISIBLE);
            mPanelAudioControl.showAudioPanel();
        } else if (id == R.id.btn_switch_cdn) {
            toggleCdnPlay();
        } else if (id == R.id.trtc_iv_chorus) {
            toggleChorus();
        }
    }

    private void toggleCdnPlay() {
        if (mCdnPlayManager == null) {
            mCdnPlayManager = new CdnPlayManager(mCdnPlayView, mObserver);
        }
        if (mIsCdnPlay) {
            //cdn播放的情况下，需要切换成正常模式
            showLoading();
            mIsCdnPlay = false;
            mTRTCVideoLayout.setVisibility(View.VISIBLE);
            mCdnPlayViewGroup.setVisibility(View.GONE);
            mCdnPlayManager.stopPlay();
            enterRoom();
            mButtonSwitchCdn.setText(R.string.trtcdemo_switch_cdn_lay);
        } else {
            showLoading();
            exitRoom();
            mIsNeedSwitchCdn = true;
        }
    }

    private void toggleChorus() {
        if (mRole == TRTCCloudDef.TRTCRoleAnchor) {
            if (mChorusManager.isChorusOn()) {
                mChorusManager.stopChorus();
            } else {
                mChorusManager.startChorus();
            }
        } else {
            if (mChorusManager.isCdnPlaying()) {
                mChorusManager.stopCdnPlay();
            } else {
                if (!mChorusManager.startCdnPlay(mChorusCdnUrl, mCdnPlayView)) {
                    ToastUtils.showShort(getString(R.string.trtcdemo_chorus_playback_failed));
                } else {
                    showLoading();
                }
            }
            mIvChorus.setImageResource(mChorusManager.isCdnPlaying() ? R.drawable.trtcdemo_ic_chorus_stop : R.drawable.trtcdemo_ic_chorus_start);
            mImageCDN.setVisibility(mChorusManager.isCdnPlaying() ? View.VISIBLE : View.GONE);
        }
    }

    private void showLoading() {
        mLoadingDialog.show();
        mMainHandler.removeCallbacks(mLoadingTimeoutRunnable);
        mMainHandler.postDelayed(mLoadingTimeoutRunnable, 6000);
    }

    private void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    private void actuallyCdnPlay() {
        mIsCdnPlay = true;
        mCdnPlayViewGroup.setVisibility(View.VISIBLE);
        mTRTCVideoLayout.setVisibility(View.GONE);
        if (mRoomIdType == 0) {
            mCdnPlayManager.initPlayUrl(mRoomId + "", mMainUserId);
        } else {
            mCdnPlayManager.initPlayUrl(mRoomIdStr, mMainUserId);
        }

        mCdnPlayManager.startPlay();
        mButtonSwitchCdn.setText(R.string.trtcdemo_switch_udp_play);

    }

    private void showDialogFragment(DialogFragment dialogFragment, String tag) {
        if (dialogFragment == null) {
            return;
        }
        if (dialogFragment.isVisible()) {
            dialogFragment.dismissAllowingStateLoss();
            return;
        }
        if (dialogFragment.isAdded()) {
            return;
        }
        // 快速双击时，第二击 tag 不为空，isAdded 有一定的滞后性。
        if (dialogFragment.getTag() != null) {
            return;
        }
        dialogFragment.show(getSupportFragmentManager(), tag);
    }

    protected void releaseTRTCLiveAudience() {
        exitRoom();
        mTRTCCloudManager.destroy();
        mTRTCRemoteUserManager.destroy();
        if (mCdnPlayManager != null) {
            mCdnPlayManager.destroy();
        }
        TRTCCloud.destroySharedInstance();
        mMainHandler.removeCallbacks(mLoadingTimeoutRunnable);
        if (mPanelAudioControl != null) {
            mPanelAudioControl.reset();
            mPanelAudioControl.unInit();
            mPanelAudioControl = null;
        }
        SettingConfigHelper.getInstance().getVideoConfig().saveCache();
        mSettingPanelFragmentDialog.destroyAllSubRooms();
        if (mChorusManager != null) {
            mChorusManager.stopChorus();
            mChorusManager.stopCdnPlay();
            mChorusManager.stopCdnPush();
        }
    }


    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    private void handleBackPressed() {
        if (mPanelAudioControl.isShown()) {
            mPanelAudioControl.closeAudioEffectPanel();
        } else {
            exitTRTCLiveAudience();
        }
    }

    private void exitTRTCLiveAudience() {
        if (mIsExited) {
            return;
        }
        mIsExited = true;
        new ExitTask().execute();
    }

    private class ExitTask extends AsyncTask {
        ContentLoadingDialog mDialog = new ContentLoadingDialog(TRTCLiveAudienceActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.showDialog(getString(R.string.trtcdemo_ending_live));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            releaseTRTCLiveAudience();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mDialog.hideDialog();
            TRTCLiveAudienceActivity.this.finish();
        }
    }


    private void switchRole() {
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        // 目标的切换角色
        int targetRole = mTRTCCloudManager.switchRole();
        mRole = targetRole;
        // 如果当前角色是主播
        if (targetRole == TRTCCloudDef.TRTCRoleAnchor) {
            mImageSwitchRole.setImageResource(R.drawable.trtcdemo_linkmic);
            mButtonSwitchCdn.setVisibility(View.GONE);
            mRoleAudienceGroup.setVisibility(View.VISIBLE);
            mRoleAnchorGroup.setVisibility(View.GONE);
            // cdn播放中，需要先停止cdn播放，cdn播放里面已经包含了进房的逻辑，所以不用再执行startLocalPreview
            if (mIsCdnPlay) {
                toggleCdnPlay();
                mCdnPlayViewGroup.setVisibility(View.GONE);
            } else {
                // 开启本地预览
                startLocalPreview();
                videoConfig.setEnableVideo(true);
                videoConfig.setPublishVideo(true);
                // 开启本地声音
                if (mIsCustomAudioCapture) {
                    mCustomAudioCapturor.start(SAMPLE_RATE, CHANNELS);
                } else {
                    mTRTCCloudManager.startLocalAudio();
                }
                mTRTCCloudManager.muteLocalAudio(false);
                audioConfig.setLocalAudioMuted(false);
                audioConfig.setAudioCapturingStarted(true);
            }
        } else {
            // 关闭本地预览
            stopLocalPreview();
            videoConfig.setEnableVideo(false);
            videoConfig.setPublishVideo(false);
            // 关闭音频采集
            mTRTCCloudManager.stopLocalAudio();
            audioConfig.setAudioCapturingStarted(false);
            mImageSwitchRole.setImageResource(R.drawable.trtcdemo_linkmic2);
            mButtonSwitchCdn.setVisibility(View.VISIBLE);
            mRoleAudienceGroup.setVisibility(View.GONE);
            mRoleAnchorGroup.setVisibility(View.VISIBLE);
            if (mPanelAudioControl != null) {
                // 未退出当前Activity如果置位null将无法重新初始化，所以这里只需要reset
                mPanelAudioControl.reset();
            }
        }
        mImageSwitchCamera.setImageResource(mTRTCCloudManager.isFontCamera()
                ? R.drawable.trtcdemo_ic_camera_back : R.drawable.trtcdemo_ic_camera_front);
        mImageEnableCamera.setImageResource(videoConfig.isEnableVideo()
                ? R.drawable.trtcdemo_remote_video_enable : R.drawable.trtcdemo_remote_video_disable);
        mImageEnableAudio.setImageResource(audioConfig.isLocalAudioMuted()
                ? R.drawable.trtcdemo_mic_disable : R.drawable.trtcdemo_mic_enable);
    }

    private void startLocalPreview() {
        if (mIsScreenCapture) {
            mTRTCCloudManager.startScreenCapture(TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
        } else if (mSubScreenCapture) {
            mTRTCCloudManager.startScreenCapture(TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
            startCameraPreview();
        } else {
            startCameraPreview();
        }
    }

    private TXCloudVideoView getCloudVideoView() {
        TXCloudVideoView renderView = mTRTCVideoLayout.findCloudVideoView(mUserId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
        if (renderView == null) {
            renderView = mTRTCVideoLayout.allocCloudVideoView(mUserId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, mLocalRenderViewType);
        }
        return renderView;
    }

    private void startCameraPreview() {
        TXCloudVideoView localVideoView = getCloudVideoView();
        // 设置 TRTC SDK 的状态为本地自定义渲染，视频格式为纹理
        if (mIsCustomLocalRender && mCustomRender != null) {
            mTRTCCloudManager.setLocalVideoRenderListener(
                    getIntent().getIntExtra(KEY_CUSTOM_RENDER_PIXEL_FORMAT, -1),
                    getIntent().getIntExtra(KEY_CUSTOM_RENDER_BUFFER_TYPE, -1),
                    mCustomRender);
            TextureView textureView = new TextureView(this);
            localVideoView.addVideoView(textureView);
            mCustomRender.start(textureView);
        }
        if (mIsCustomCapture) {
            if (mCustomCapture != null) {
                mCustomCapture.start(mAudioFrameReadListener, mVideoFrameReadListener);
            }
        } else {
            // 开启本地预览
            mTRTCCloudManager.setLocalPreviewView(localVideoView);
            mTRTCCloudManager.startLocalPreview();
        }
    }

    private final AudioFrameReader.AudioFrameReadListener mAudioFrameReadListener = new AudioFrameReader.AudioFrameReadListener() {
        @Override
        public void onFrameAvailable(byte[] data, int sampleRate, int channel, long timestamp) {
            TRTCCloudDef.TRTCAudioFrame trtcAudioFrame = new TRTCCloudDef.TRTCAudioFrame();
            trtcAudioFrame.data = data;
            trtcAudioFrame.sampleRate = sampleRate;
            trtcAudioFrame.channel = channel;
            trtcAudioFrame.timestamp = mTRTCCloudManager.generateCustomPTS();

            mTRTCCloudManager.sendCustomAudioData(trtcAudioFrame);
        }
    };

    private final VideoFrameReader.VideoFrameReadListener mVideoFrameReadListener = new VideoFrameReader.VideoFrameReadListener() {
        @Override
        public void onFrameAvailable(TextureFrame frame) {
            // 将视频帧通过纹理方式塞给SDK
            TRTCVideoFrame videoFrame = new TRTCVideoFrame();
            videoFrame.texture = new TRTCCloudDef.TRTCTexture();
            videoFrame.texture.textureId = frame.textureId;
            videoFrame.texture.eglContext14 = frame.eglContext;
            videoFrame.width = frame.width;
            videoFrame.height = frame.height;
            videoFrame.pixelFormat = TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D;
            videoFrame.bufferType = TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE;
            videoFrame.timestamp = mTRTCCloudManager.generateCustomPTS();

            mTRTCCloudManager.sendCustomVideoData(videoFrame);
        }
    };

    private void stopLocalPreview() {
        if (mIsScreenCapture || mSubScreenCapture) {
            mTRTCCloudManager.stopScreenCapture();
        }
        if (!mIsCustomCapture) {
            mTRTCCloudManager.stopLocalPreview();
        } else {
            if (mCustomCapture != null) {
                mCustomCapture.stop();
            }
        }
        if (mCustomRender != null) {
            mCustomRender.stop();
        }
        if (!mIsExited) {
            mTRTCVideoLayout.recyclerCloudViewView(mUserId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
        }
    }

    private void startLinkMicLoading() {
        FrameLayout layout = (FrameLayout) findViewById(R.id.trtc_fl_link_loading);
        layout.setVisibility(View.VISIBLE);

        ImageView imageView = (ImageView) findViewById(R.id.trtc_iv_link_loading);
        imageView.setImageResource(R.drawable.trtcdemo_linkmic_loading);
        AnimationDrawable animation = (AnimationDrawable) imageView.getDrawable();
        if (animation != null) {
            animation.start();
        }
    }

    private void stopLinkMicLoading() {
        FrameLayout layout = (FrameLayout) findViewById(R.id.trtc_fl_link_loading);
        layout.setVisibility(View.GONE);

        ImageView imageView = (ImageView) findViewById(R.id.trtc_iv_link_loading);
        AnimationDrawable animation = (AnimationDrawable) imageView.getDrawable();
        if (animation != null) {
            animation.stop();
        }
    }


    private void onVideoChange(String userId, int streamType, boolean available) {
        if (mIsChorusMode) {
            return;
        }
        if (available) {
            // 首先需要在界面中分配对应的TXCloudVideoView
            TXCloudVideoView renderView = mTRTCVideoLayout.findCloudVideoView(userId, streamType);
            if (renderView == null) {
                renderView = mTRTCVideoLayout.allocCloudVideoView(userId, streamType, mLocalRenderViewType);
            }
            // 启动远程画面的解码和显示逻辑
            if (renderView != null) {
                mTRTCRemoteUserManager.remoteUserVideoAvailable(userId, streamType, renderView);
            }
            if (!userId.equals(mMainUserId)) {
                mMainUserId = userId;
            }
        } else {
            mTRTCRemoteUserManager.remoteUserVideoUnavailable(userId, streamType);
            if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB) {
                // 辅路直接移除画面，不会更新状态。主流需要更新状态，所以保留
                mTRTCVideoLayout.recyclerCloudViewView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                mTRTCRemoteUserManager.removeRemoteUser(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
            }
        }
        if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG) {
            // 根据当前视频流的状态，展示相关的 UI 逻辑。
            mTRTCVideoLayout.updateVideoStatus(userId, available);
        }
        if (SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode() == TRTCCloudDef.TRTC_TranscodingConfigMode_Manual) {
            mTRTCRemoteUserManager.updateCloudMixtureParams();
        }
    }

    @Override
    public void onEnterRoom(long elapsed) {
        dismissLoading();
        if (elapsed >= 0) {
            Toast.makeText(this, getString(R.string.trtcdemo_ener_room_success_tips) + elapsed + getString(R.string.trtcdemo_mills), Toast.LENGTH_SHORT).show();
            // 发起云端混流
            mTRTCRemoteUserManager.updateCloudMixtureParams();
        } else {
            Toast.makeText(this, getString(R.string.trtcdemo_enter_room_fail_tips), Toast.LENGTH_SHORT).show();
            exitRoom();
        }
    }

    @Override
    public void onExitRoom(int reason) {
        if (mIsNeedSwitchCdn && mRole == TRTCCloudDef.TRTCRoleAudience) {
            actuallyCdnPlay();
            mIsNeedSwitchCdn = false;
        }
    }

    @Override
    public void onError(int errCode, String errMsg, Bundle extraInfo) {
        Toast.makeText(this, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_LONG).show();
        Log.e(TAG, "onError: " + errMsg + "[" + errCode + "], exitRoom...");
        if (errCode != TXLiteAVCode.ERR_SERVER_CENTER_ANOTHER_USER_PUSH_SUB_VIDEO) {
            exitTRTCLiveAudience();
        }
    }

    @Override
    public void onRemoteUserEnterRoom(String userId) {
    }

    @Override
    public void onRemoteUserLeaveRoom(String userId, int reason) {
        mTRTCRemoteUserManager.removeRemoteUser(userId);
        mTRTCVideoLayout.recyclerCloudViewView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
        mTRTCVideoLayout.recyclerCloudViewView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
        if (SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode() == TRTCCloudDef.TRTC_TranscodingConfigMode_Manual) {
            mTRTCRemoteUserManager.updateCloudMixtureParams();
        }
    }

    @Override
    public void onUserVideoAvailable(String userId, boolean available) {
        onVideoChange(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, available);
    }

    @Override
    public void onUserSubStreamAvailable(final String userId, boolean available) {
        onVideoChange(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB, available);
    }

    @Override
    public void onUserAudioAvailable(String userId, boolean available) {
        if (mIsChorusMode) {
            return;
        }
    }

    @Override
    public void onFirstVideoFrame(String userId, int streamType, int width, int height) {
        Log.i(TAG, "onFirstVideoFrame: userId = " + userId + " streamType = " + streamType + " width = " + width + " height = " + height);
    }

    @Override
    public void onCameraDidReady() {
        MoreConfig config = SettingConfigHelper.getInstance().getMoreConfig();
        if (config.isEnableFlash()) {
            mTRTCCloudManager.getDeviceManager().enableCameraTorch(config.isEnableFlash());
        }
    }

    @Override
    public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
        for (int i = 0; i < userVolumes.size(); ++i) {
            mTRTCVideoLayout.updateAudioVolume(userVolumes.get(i).userId, userVolumes.get(i).volume);
        }
    }

    @Override
    public void onStatistics(TRTCStatistics statics) {
    }

    @Override
    public void onConnectOtherRoom(final String userID, final int err, final String errMsg) {
        PkConfig pkConfig = SettingConfigHelper.getInstance().getPkConfig();
        stopLinkMicLoading();
        if (err == 0) {
            pkConfig.setConnected(true);
            Toast.makeText(this, getString(R.string.trtcdemo_pk_success_tips), Toast.LENGTH_LONG).show();
        } else {
            pkConfig.setConnected(false);
            Toast.makeText(this, getString(R.string.trtcdemo_pk_fail_tips), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDisConnectOtherRoom(final int err, final String errMsg) {
        PkConfig pkConfig = SettingConfigHelper.getInstance().getPkConfig();
        pkConfig.reset();
    }

    @Override
    public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
        mTRTCVideoLayout.updateNetworkQuality(localQuality.userId, localQuality.quality);
        for (TRTCCloudDef.TRTCQuality qualityInfo : remoteQuality) {
            mTRTCVideoLayout.updateNetworkQuality(qualityInfo.userId, qualityInfo.quality);
        }
    }

    @Override
    public void onAudioEffectFinished(int effectId, int code) {
        Toast.makeText(this, "effect id = " + effectId + getString(R.string.trtcdemo_play_end) + " code = " + code, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecvCustomCmdMsg(String userId, int cmdID, int seq, byte[] message) {
        String msg = "";
        if (message != null && message.length > 0) {
            try {
                msg = new String(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (!mIsChorusMode) {
                ToastUtils.showLong(getString(R.string.trtcdemo_receive) + userId + getString(R.string.trtcdemo_message_end) + msg);
            }
        }
        if (mChorusManager != null) {
            mChorusManager.onRecvCustomCmdMsg(userId, cmdID, seq, message);
        }
    }

    @Override
    public void onRecvSEIMsg(String userId, byte[] data) {
        String msg = "";
        if (data != null && data.length > 0) {
            try {
                msg = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ToastUtils.showLong(getString(R.string.trtcdemo_receive) + userId + getString(R.string.trtcdemo_message_end) + msg);
        }
    }

    @Override
    public void onAudioVolumeEvaluationChange(boolean enable) {
        if (enable) {
            mTRTCVideoLayout.showAllAudioVolumeProgressBar();
        } else {
            mTRTCVideoLayout.hideAllAudioVolumeProgressBar();
        }
    }

    @Override
    public void onStartLinkMic() {
        startLinkMicLoading();
    }

    @Override
    public void onMuteLocalVideo(boolean isMute) {
        mTRTCVideoLayout.updateVideoStatus(mUserId, !isMute);
    }

    @Override
    public void onMuteLocalAudio(boolean isMute) {
        mImageEnableAudio.setImageResource(!isMute ? R.drawable.trtcdemo_mic_enable : R.drawable.trtcdemo_mic_disable);
    }

    @Override
    public void onSnapshotLocalView(final Bitmap bmp) {
        showSnapshotImage(bmp);
    }

    @Override
    public TXCloudVideoView getRemoteUserViewById(String roomId, String userId, int steamType) {
        TXCloudVideoView view = mTRTCVideoLayout.findCloudVideoView(userId, steamType);
        if (view == null) {
            view = mTRTCVideoLayout.allocCloudVideoView(userId, steamType, mLocalRenderViewType);
        }
        return view;
    }

    @Override
    public void onRemoteViewStatusUpdate(String roomId, String userId, boolean enableVideo) {
        mTRTCVideoLayout.updateVideoStatus(userId, enableVideo);
    }

    @Override
    public void onSnapshotRemoteView(Bitmap bm) {
        showSnapshotImage(bm);
    }

    private void showSnapshotImage(final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bmp == null) {
                    ToastUtils.showLong(getString(R.string.trtcdemo_capture_picture_fail));
                } else {
                    ImageView imageView = new ImageView(TRTCLiveAudienceActivity.this);
                    imageView.setImageBitmap(bmp);
                    AlertDialog dialog = new AlertDialog.Builder(TRTCLiveAudienceActivity.this)
                            .setView(imageView)
                            .setPositiveButton(getString(R.string.trtcdemo_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();

                    dialog.show();

                    final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                    positiveButtonLL.gravity = Gravity.CENTER;
                    positiveButton.setLayoutParams(positiveButtonLL);
                }
            }
        });
    }

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    @Override
    public void onAudioCapturePcm(byte[] data, int sampleRate, int channels, long timestampMs) {
        TRTCCloudDef.TRTCAudioFrame trtcAudioFrame = new TRTCCloudDef.TRTCAudioFrame();
        trtcAudioFrame.data = data;
        trtcAudioFrame.sampleRate = sampleRate;
        trtcAudioFrame.channel = channels;

        mTRTCCloudManager.sendCustomAudioData(trtcAudioFrame);
    }

    private boolean isEnableDebugMode() {
        return AppRuntime.get().isDebug();
    }
}
