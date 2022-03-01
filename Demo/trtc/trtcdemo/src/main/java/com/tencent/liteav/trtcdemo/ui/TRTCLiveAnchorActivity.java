package com.tencent.liteav.trtcdemo.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.tencent.liteav.trtcdemo.model.manager.TRTCBgmManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCMixAudioManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCRemoteUserManager;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusListener;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusManager;
import com.tencent.liteav.trtcdemo.model.utils.CustomGLContextUtils;
import com.tencent.liteav.trtcdemo.model.utils.TRTCConstants;
import com.tencent.liteav.trtcdemo.ui.dialog.RemoteUserPanelDialogFragment;
import com.tencent.liteav.trtcdemo.ui.dialog.SettingPanelDialogFragment;
import com.tencent.liteav.trtcdemo.ui.fragment.MoreSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.bgm.BgmSettingFragmentDialog;
import com.tencent.liteav.trtcdemo.ui.widget.lrcview.LrcView;
import com.tencent.liteav.trtcdemo.ui.widget.videolayout.TRTCVideoLayoutManager;
import com.tencent.rtmp.ITXLivePlayListener;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_EARPIECEMODE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_INPUT_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_QUALITY;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_SCENE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_VOLUMETYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CHORUS_CDN_URL;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_AUDIO_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_PROCESS_USE_RENDER_INTERFACE;
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
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USE_CUSTOM_OPEN_GL_CONTEXT;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USE_STRING_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_VIDEO_FILE_PATH;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_VIDEO_INPUT_TYPE;

public class TRTCLiveAnchorActivity extends AppCompatActivity implements View.OnClickListener,
        TRTCCloudManagerListener,
        TRTCCloudManager.IView,
        TRTCRemoteUserManager.IView,
        ITXLivePlayListener,
        MoreSettingFragment.VideoSettingListener,
        CustomAudioCapturor.TXICustomAudioCapturorListener {
    
    private static final String TAG = "TRTCLiveAnchorActivity";
    
    private TRTCCloud                     mTRTCCloud;
    private TRTCCloudManager              mTRTCCloudManager;
    private TRTCRemoteUserManager         mTRTCRemoteUserManager;
    private TRTCBgmManager                mBgmManager;
    private TRTCMixAudioManager           mMixAudioManager;
    private CustomGLContextUtils          mCustomGLContextUtils;
    private TRTCVideoLayoutManager        mTRTCVideoLayout;
    private TextView                      mTextRoomId;
    private SettingPanelDialogFragment    mSettingPanelFragmentDialog;
    private RemoteUserPanelDialogFragment mRemoteUserManagerFragmentDialog;
    private BeautyPanel                   mPanelBeautyControl;
    private AudioEffectPanel              mPanelAudioControl;
    private BgmSettingFragmentDialog      mBgmSettingFragmentDialog;
    private ImageView                     mImageSwitchCamera;
    private ImageView                     mImageEnableAudio;
    private ImageView                     mImageMoreTrtc;
    private ImageView                     mCameraToggleView;
    private Fragment                      mVodPlayerFragment;
    private boolean                       mIsCustomCapture       = false;
    private boolean                       mIsCustomAudioCapture  = false;
    private String                        mVideoFilePath;
    private CustomCaptureVideo            mCustomCapture;
    private CustomRenderVideo             mCustomRender;
    private CustomAudioCapturor           mCustomAudioCapturor;
    private int                           mLogLevel              = 0;
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
    private int                           mRole                  = TRTCCloudDef.TRTCRoleAnchor;
    private int                           mVideoInputType        = 0;
    private int                           mAudioInputType        = 0;
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
    private boolean                       mIsUseCustomGLContext  = false;
    private boolean                       mIsChorusMode          = false;
    private boolean                       mIsExited              = false;
    private TRTCChorusManager             mChorusManager;
    private ImageView                     mImageChorus;
    private LrcView                       mLrcView;
    private String                        mChorusCdnUrl;
    private ProgressDialog                mLoadingDialog;
    private Handler                       mMainHandler;
    
    private Runnable mLoadingTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            dismissLoading();
        }
    };
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.BeautyTheme);
        initIntentData();
        setContentView(R.layout.trtcdemo_activity_live_anchor_room);
        initTRTCSDK();
        initViews();
        updateVodPlayer();
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE, PermissionConstants.STORAGE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                enterRoom();
            }
            
            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtils.showShort(R.string.trtcdemo_permission_tips);
                exitTRTCLiveAnchor();
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
        mAudioQuality = intent.getIntExtra(KEY_AUDIO_QUALITY, TRTCConstants.TRTC_AUDIO_QUALITY_NONE);
        mIsAudioEarpieceMode = intent.getBooleanExtra(KEY_AUDIO_EARPIECEMODE, false);
        mVideoInputType = intent.getIntExtra(KEY_VIDEO_INPUT_TYPE, 0);
        mAudioInputType = intent.getIntExtra(KEY_AUDIO_INPUT_TYPE, 0);
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
            mIsUseCustomGLContext = intent.getBooleanExtra(KEY_USE_CUSTOM_OPEN_GL_CONTEXT, false);
            if (mIsUseCustomGLContext) {
                // 在 demo 层同步创建好一个 glContext
                mCustomGLContextUtils = new CustomGLContextUtils();
                mCustomGLContextUtils.initSync();
            }
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
        if (mVideoInputType != 3 && !mIsChorusMode) {
            startLocalPreview();
            videoConfig.setEnableVideo(true);
            videoConfig.setPublishVideo(true);
            Bitmap bitmap = decodeResource(getResources(), R.drawable.trtcdemo_mute_image);
            videoConfig.setMuteImage(bitmap);
        }
        if (mIsChorusMode) {
            videoConfig.setEnableVideo(true);
            videoConfig.setPublishVideo(true);
        }
        
        mTRTCCloudManager.enterRoom();
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        if (mAudioInputType != 2) {
            if (mIsCustomAudioCapture) {
                mCustomAudioCapturor.start(48000, 1);
            } else {
                mTRTCCloudManager.startLocalAudio();
            }
            audioConfig.setAudioCapturingStarted(true);
        }
        mTRTCCloudManager.enableEarMonitoring(audioConfig.isEnableEarMonitoring());
        
        if (mIsChorusMode) {
            if (!mChorusManager.startCdnPush(mChorusCdnUrl)) {
                ToastUtils.showShort(R.string.trtcdemo_chorus_push_fail);
            }
        }
        if (!mIsCustomCapture && !mIsChorusMode) {
            mTRTCCloudManager.setLocalPreviewView(getLocalVideoView());
        }
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
        mImageEnableAudio = findViewById(R.id.trtc_iv_mic);
        mImageMoreTrtc = findViewById(R.id.trtc_iv_more);
        findViewById(R.id.trtc_iv_bgm).setVisibility(AppRuntime.get().isDebug() ? View.VISIBLE : View.GONE);
        
        findViewById(R.id.trtc_iv_beauty).setOnClickListener(this);
        findViewById(R.id.trtc_iv_log).setOnClickListener(this);
        findViewById(R.id.trtc_iv_setting).setOnClickListener(this);
        findViewById(R.id.trtc_ib_back).setOnClickListener(this);
        findViewById(R.id.trtc_iv_music).setOnClickListener(this);
        findViewById(R.id.trtc_iv_bgm).setOnClickListener(this);
        mImageEnableAudio.setOnClickListener(this);
        mImageSwitchCamera.setOnClickListener(this);
        mImageMoreTrtc.setOnClickListener(this);

        mCameraToggleView = findViewById(R.id.iv_camera_on_off);
        mCameraToggleView.setOnClickListener(this);
        mCameraToggleView.setVisibility(mIsScreenCapture ? View.GONE : View.VISIBLE);
        setVideoEnableListener();

        if (mRoomIdType == 0) {
            mTextRoomId.setText(mRoomId + "");
        } else {
            mTextRoomId.setText(mRoomIdStr);
        }
        
        mPanelAudioControl = findViewById(R.id.anchor_audio_panel);
        mPanelAudioControl.setAudioEffectManager(mTRTCCloudManager.getAudioEffectManager());
        mPanelAudioControl.initPanelDefaultBackground();
        
        mPanelBeautyControl = (BeautyPanel) findViewById(R.id.trtc_beauty_panel);
        mPanelBeautyControl.setBeautyManager(mTRTCCloudManager.getBeautyManager());
        
        // BGM设置面板
        mBgmSettingFragmentDialog = new BgmSettingFragmentDialog();
        mBgmSettingFragmentDialog.setTRTCBgmManager(mBgmManager);
        mBgmSettingFragmentDialog.setTRTCMixAudioManager(mMixAudioManager);
        
        mRemoteUserManagerFragmentDialog = new RemoteUserPanelDialogFragment();
        mRemoteUserManagerFragmentDialog.setTRTCRemoteUserManager(mTRTCRemoteUserManager);
        RemoteUserConfigHelper.getInstance().clear();
        
        mTRTCVideoLayout = (TRTCVideoLayoutManager) findViewById(R.id.trtc_video_view_layout);
        
        mSettingPanelFragmentDialog = new SettingPanelDialogFragment();
        mSettingPanelFragmentDialog.setTRTCCloudManager(mTRTCCloudManager, mTRTCRemoteUserManager, mTRTCVideoLayout);
        
        mImageSwitchCamera.setImageResource(mTRTCCloudManager.isFontCamera() ? R.drawable.trtcdemo_ic_camera_back : R.drawable.trtcdemo_ic_camera_front);
        // loading初始化
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setMessage(getString(R.string.trtcdemo_switching));
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mMainHandler = new Handler();
        
        // Vod播放器
        try {
            Class<?> clazz = getClassLoader().loadClass("com.tencent.liteav.trtcdemo.ui.fragment.VodPlayerFragment");
            mVodPlayerFragment = (Fragment) clazz.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "create vod player fragment failed.", e);
        }
        mImageChorus = findViewById(R.id.trtc_iv_chorus);
        mImageChorus.setOnClickListener(this);
        mLrcView = (LrcView) findViewById(R.id.lrc_view);
        String mainLrcText = getLrcText("153307-yc.lrc");
        mLrcView.loadLrc(mainLrcText, null);
        if (mIsChorusMode) {
            mImageChorus.setVisibility(View.VISIBLE);
            mLrcView.setVisibility(View.VISIBLE);
        } else {
            mImageChorus.setVisibility(View.GONE);
            mLrcView.setVisibility(View.GONE);
        }
    }

    private void setVideoEnableListener() {
        SettingConfigHelper.getInstance().getVideoConfig()
                .setVideoEnableListener(new VideoConfig.VideoEnableListener() {
            @Override
            public void onChecked(boolean isEnable) {
                if (isEnable) {
                    mCameraToggleView.setImageResource(R.drawable.trtcdemo_remote_video_enable);
                } else {
                    mCameraToggleView.setImageResource(R.drawable.trtcdemo_remote_video_disable);
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
        mTRTCCloud = TRTCCloud.sharedInstance(this);
        mBgmManager = new TRTCBgmManager(mTRTCCloud, mTRTCParams);
        mMixAudioManager = new TRTCMixAudioManager(mTRTCCloud);
        mTRTCCloudManager = new TRTCCloudManager(this, mTRTCCloud, mTRTCParams, mAppScene);
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
                getIntent().getBooleanExtra(KEY_CUSTOM_PROCESS_USE_RENDER_INTERFACE, false));
        mTRTCRemoteUserManager.setMixUserId(mUserId);
        
        if (mIsCustomCapture) {
            mCustomCapture = new CustomCaptureVideo(this, mVideoFilePath, true);
            mCustomRender = new CustomRenderVideo(mUserId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                    false);
        }
        
        if (mIsCustomLocalRender) {
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
            mTRTCCloudManager.setTRTCCloudParam();
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
            mChorusManager = new TRTCChorusManager(this, mTRTCCloud);
            try {
                Class<?> clazz = getClassLoader().loadClass("com.tencent.liteav.trtcdemo.model.manager.chorus"
                        + ".TRTCChorusManagerImpl");
                Constructor constructor = clazz.getConstructor(Context.class, TRTCCloud.class);
                mChorusManager = (TRTCChorusManager) constructor.newInstance(this, mTRTCCloud);
            } catch (Exception e) {
                Log.e(TAG, "create TRTCChorusManagerImpl new instance failed.", e);
            }
            mChorusManager.setListener(new TRTCChorusListener() {
                @Override
                public void onChorusStart(TRTCChorusDef.ChorusStartReason reason) {
                    mImageChorus.setImageResource(R.drawable.trtcdemo_ic_chorus_stop);
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
                    mImageChorus.setImageResource(R.drawable.trtcdemo_ic_chorus_start);
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
    
    private void showLoading() {
        Log.d(TAG, "showLoading");
        mLoadingDialog.show();
        mMainHandler.removeCallbacks(mLoadingTimeoutRunnable);
        mMainHandler.postDelayed(mLoadingTimeoutRunnable, 6000);
    }
    
    private void dismissLoading() {
        Log.d(TAG, "dismissLoading");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.trtc_ib_back) {
            handleBackPressed();
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
            mTRTCCloudManager.muteLocalAudio(!audioConfig.isLocalAudioMuted());
            ((ImageView) v).setImageResource(audioConfig.isLocalAudioMuted() ? R.drawable.trtcdemo_mic_enable : R.drawable.trtcdemo_mic_disable);
        } else if (id == R.id.trtc_iv_log) {
            mLogLevel = (mLogLevel + 1) % 3;
            ((ImageView) v).setImageResource((0 == mLogLevel) ? R.drawable.trtcdemo_log_hidden : R.drawable.trtcdemo_log_show);
            mTRTCCloudManager.showDebugView(mLogLevel);
        } else if (id == R.id.trtc_iv_setting) {
            showDialogFragment(mSettingPanelFragmentDialog, "FeatureSettingFragmentDialog");
        } else if (id == R.id.trtc_iv_more) {
            showDialogFragment(mRemoteUserManagerFragmentDialog, "RemoteUserManagerFragmentDialog");
        } else if (id == R.id.trtc_iv_music) {
            mPanelAudioControl.setVisibility(View.VISIBLE);
            mPanelAudioControl.showAudioPanel();
        } else if (id == R.id.trtc_iv_bgm) {
            showDialogFragment(mBgmSettingFragmentDialog, "BgmSettingFragmentDialog");
        } else if (id == R.id.trtc_iv_chorus) {
            toggleChorus();
        }
    }
    
    private void toggleChorus() {
        if (mChorusManager.isChorusOn()) {
            mChorusManager.stopChorus();
        } else {
            mChorusManager.startChorus();
        }
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

    private void releaseTRTCLiveAnchor() {
        exitRoom();
        mBgmManager.destroy();
        mTRTCCloudManager.destroy();
        mTRTCRemoteUserManager.destroy();
        TRTCCloud.destroySharedInstance();
        if (mPanelAudioControl != null) {
            mPanelAudioControl.reset();
            mPanelAudioControl.unInit();
            mPanelAudioControl = null;
        }
        mMainHandler.removeCallbacks(mLoadingTimeoutRunnable);
        SettingConfigHelper.getInstance().getVideoConfig().saveCache();
        mSettingPanelFragmentDialog.destroyAllSubRooms();
        if (isEnableDebugMode()) {
            if (mIsUseCustomGLContext && mCustomGLContextUtils != null) {
                mCustomGLContextUtils.releaseSync();
            }
        }
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
            exitTRTCLiveAnchor();
        }
    }

    private void exitTRTCLiveAnchor() {
        if (mIsExited) {
            return;
        }
        mIsExited = true;
        new ExitTask().execute();
    }

    private class ExitTask extends AsyncTask {
        ContentLoadingDialog mDialog = new ContentLoadingDialog(TRTCLiveAnchorActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.showDialog(getString(R.string.trtcdemo_ending_live));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            releaseTRTCLiveAnchor();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mDialog.hideDialog();
            TRTCLiveAnchorActivity.this.finish();
        }
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

    private TXCloudVideoView getLocalVideoView() {
        TXCloudVideoView localVideoView = getCloudVideoView();
        // 设置 TRTC SDK 的状态为本地自定义渲染，视频格式为纹理
        if (mIsCustomLocalRender && mCustomRender != null) {
            Intent intent = getIntent();
            mTRTCCloudManager.setLocalVideoRenderListener(
                    intent.getIntExtra(KEY_CUSTOM_RENDER_PIXEL_FORMAT, -1),
                    intent.getIntExtra(KEY_CUSTOM_RENDER_BUFFER_TYPE, -1),
                    mCustomRender);
            TextureView textureView = new TextureView(this);
            localVideoView.addVideoView(textureView);
            mCustomRender.start(textureView);
        }
        return localVideoView;
    }
    
    private void startCameraPreview() {
        if (mIsCustomCapture) {
            if (mCustomCapture != null) {
                mCustomCapture.start(mAudioFrameReadListener, mVideoFrameReadListener);
            }
        } else {
            // 开启本地预览
            mTRTCCloudManager.setLocalPreviewView(getLocalVideoView());
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
    
    }
    
    @Override
    public void onError(int errCode, String errMsg, Bundle extraInfo) {
        Toast.makeText(this, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_LONG).show();
        Log.e(TAG, "onError: " + errMsg + "[" + errCode + "], exitRoom...");
        if (errCode != TXLiteAVCode.ERR_SERVER_CENTER_ANOTHER_USER_PUSH_SUB_VIDEO) {
            exitTRTCLiveAnchor();
        } else {
            if (mVodPlayerFragment != null) {
                try {
                    Class<?> clazz = mVodPlayerFragment.getClass();
                    Method method = clazz.getDeclaredMethod("unPublish");
                    method.invoke(mVodPlayerFragment);
                } catch (Exception e) {
                    Log.e(TAG, "call unpublish error " + e);
                }
            }
        }
    }
    
    @Override
    public void onRemoteUserEnterRoom(String userId) {
        mTRTCRemoteUserManager.addRemoteUserToList(userId);
    }
    
    @Override
    public void onRemoteUserLeaveRoom(String userId, int reason) {
        mTRTCRemoteUserManager.removeRemoteUserFromList(userId);
        mTRTCRemoteUserManager.removeRemoteUser(userId);
        mTRTCVideoLayout.recyclerCloudViewView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
        mTRTCVideoLayout.recyclerCloudViewView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
        if (SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode() == TRTCCloudDef.TRTC_TranscodingConfigMode_Manual) {
            mTRTCRemoteUserManager.updateCloudMixtureParams();
        }
    }
    
    @Override
    public void onUserVideoAvailable(String userId, boolean available) {
        if (available) {
            mTRTCCloudManager.setDebugViewMargin(userId);
        }
        onVideoChange(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, available);
    }
    
    @Override
    public void onUserSubStreamAvailable(final String userId, boolean available) {
        if (available) {
            mTRTCCloudManager.setDebugViewMargin(userId);
        }
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
    
    }
    
    @Override
    public void onAudioEffectFinished(int effectId, int code) {
        Toast.makeText(this, "effect id = " + effectId + getString(R.string.trtcdemo_play_end) + " code = " + code, Toast.LENGTH_SHORT).show();
        mBgmSettingFragmentDialog.onAudioEffectFinished(effectId, code);
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
    
    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            ToastUtils.showLong(getString(R.string.trtcdemo_play_success) + event);
        } else if (event == TXLiveConstants.PLAY_EVT_GET_MESSAGE) {
            if (param != null) {
                byte[] data = param.getByteArray(TXLiveConstants.EVT_GET_MSG);
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
        } else if (event < 0) {
            ToastUtils.showLong(getString(R.string.trtcdemo_play_fail) + event);
        }
    }
    
    @Override
    public void onNetStatus(Bundle status) {
    
    }
    
    private void showSnapshotImage(final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bmp == null) {
                    ToastUtils.showLong(getString(R.string.trtcdemo_capture_picture_fail));
                } else {
                    ImageView imageView = new ImageView(TRTCLiveAnchorActivity.this);
                    imageView.setImageBitmap(bmp);
                    AlertDialog dialog = new AlertDialog.Builder(TRTCLiveAnchorActivity.this)
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
    
    @Override
    public void onVodPlayerVisibilityChanged(boolean show) {
        updateVodPlayer();
    }
    
    private void updateVodPlayer() {
        if (mVodPlayerFragment == null) {
            return;
        }
        
        boolean enable = SettingConfigHelper.getInstance().getMoreConfig().isVodPlayerEnabled();
        findViewById(R.id.fl_vod_player_holder).setVisibility(enable ? View.VISIBLE : View.GONE);
        
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (enable) {
            if (getFragmentManager().findFragmentById(R.id.fl_vod_player_holder) == null) {
                transaction.add(R.id.fl_vod_player_holder, mVodPlayerFragment);
            }
            VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
            TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
            encParam.videoResolution = videoConfig.getMainStreamVideoResolution();
            encParam.videoFps = videoConfig.getMainStreamVideoFps();
            encParam.videoBitrate = videoConfig.getMainStreamVideoBitrate();
            encParam.videoResolutionMode = videoConfig.isMainStreamVideoVertical() ? TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT : TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;
            if (mAppScene == TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL) {
                encParam.enableAdjustRes = true;
            }
            mTRTCCloud.setSubStreamEncoderParam(encParam);
            try {
                Class<?> clazz = mVodPlayerFragment.getClass();
                Field field = clazz.getDeclaredField("mTrtcCloud");
                field.set(mVodPlayerFragment, mTRTCCloud);
            } catch (Exception e) {
                Log.e(TAG, "set mTrtcCloud error " + e);
            }
        } else {
            transaction.remove(mVodPlayerFragment);
        }
        transaction.commit();
    }
}
