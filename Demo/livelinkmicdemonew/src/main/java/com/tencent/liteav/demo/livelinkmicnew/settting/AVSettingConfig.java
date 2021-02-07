package com.tencent.liteav.demo.livelinkmicnew.settting;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.tencent.liteav.demo.livelinkmicnew.widget.MainItemRenderView;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.trtc.TRTCCloudDef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;

/**
 * 用来管理setting的配置项，全局维护一个实例，避免重复初始化
 */
public class AVSettingConfig {

    public V2TXLivePusher pusherInstance;
    public Map<String, V2TXLivePlayer> playerMap = new HashMap<>();
    // 二维码扫码播放地址判重用
    public Map<String, MainItemRenderView> playerViewScanMap = new HashMap<>();
    public List<String> playerURLList = new ArrayList<>();
    public static final String INTENT_SCAN_RESULT = "SCAN_RESULT";
    public V2TXLiveDef.V2TXLiveRotation rotation = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
    public V2TXLiveDef.V2TXLiveFillMode fillMode = V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill;
    public int playoutVolume = 100;
    public boolean enableVolumeCallback = false;
    public boolean enableSpeaker = true;
    public String roomPushURL;

    // 视频相关设置项
    private VideoConfig mVideoConfig;
    // 音频相关设置项
    private AudioConfig mAudioConfig;

    private AVSettingConfig() {
    }

    public static AVSettingConfig getInstance() {
        return SingletonHolder.instance;
    }

    public VideoConfig getVideoConfig() {
        if (mVideoConfig == null) {
            mVideoConfig = new VideoConfig();
            mVideoConfig.loadCache();
        }
        return mVideoConfig;
    }

    public AudioConfig getAudioConfig() {
        if (mAudioConfig == null) {
            mAudioConfig = new AudioConfig();
            mAudioConfig.loadCache();
        }
        return mAudioConfig;
    }

    private static class SingletonHolder {
        /**
         * 由JVM来保证线程安全
         */
        private static AVSettingConfig instance = new AVSettingConfig();
    }

    // 音频设置项，通过json的形式保存到本地，在初始化时取出

    class AudioConfig implements Serializable {
        private final static String PER_DATA = "per_audio_data";
        private final static String PER_DATA_PARAM = "per_audio_param";
        private final static String PER_SAVE_FLAG = "per_save_flag";

        // 是否打开16k采样率，true 16k false 48k
        private boolean mEnable16KSampleRate = false;
        // 系统音量类型
        private int mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
        // 自动增益
        private boolean mAGC = false;
        // 噪音消除
        private boolean mANS = false;
        // 是否打开音频采集
        private boolean mEnableAudio = true;
        // 耳返开关
        private boolean mEnableEarMonitoring = false;
        // 听筒模式 true 听筒 false 免提
        private boolean mAudioEarpieceMode = false;
        // 是否打开音量提示，界面上表示为底下的音量条
        private boolean mAudioVolumeEvaluation = false;
        // 采集音量
        private int mRecordVolume = 100;
        // 播放音量
        private int mPlayoutVolume = 100;

        /**
         * 是否默认保存到本地，第二次启动的时候，会自动从本地加载
         * transient 表示这个参数不会序列化
         */
        private transient boolean mSaveFlag = true;
        // 录音状态， true 正在录音 false 结束录音
        private transient boolean mRecording = false;

        public AudioConfig() {
            //loadCache();
        }

        public void copyFromSetting(AudioConfig other) {
            this.mEnable16KSampleRate = other.mEnable16KSampleRate;
            this.mAudioVolumeType = other.mAudioVolumeType;
            this.mAGC = other.mAGC;
            this.mANS = other.mANS;
            this.mEnableAudio = other.mEnableAudio;
            this.mAudioEarpieceMode = other.mAudioEarpieceMode;
            this.mAudioVolumeEvaluation = other.mAudioVolumeEvaluation;
            this.mSaveFlag = other.mSaveFlag;
            this.mRecording = other.mRecording;
            this.mEnableEarMonitoring = other.mEnableEarMonitoring;
            this.mRecordVolume = other.mRecordVolume;
            this.mPlayoutVolume = other.mPlayoutVolume;
        }

        public boolean isEnable16KSampleRate() {
            return mEnable16KSampleRate;
        }

        public void setEnable16KSampleRate(boolean enable16KSampleRate) {
            mEnable16KSampleRate = enable16KSampleRate;
        }

        public int getAudioVolumeType() {
            return mAudioVolumeType;
        }

        public void setAudioVolumeType(int audioVolumeType) {
            mAudioVolumeType = audioVolumeType;
        }

        public boolean isAGC() {
            return mAGC;
        }

        public void setAGC(boolean AGC) {
            mAGC = AGC;
        }

        public boolean isANS() {
            return mANS;
        }

        public void setANS(boolean ANS) {
            mANS = ANS;
        }

        public boolean isEnableAudio() {
            return mEnableAudio;
        }

        public void setEnableAudio(boolean enableAudio) {
            mEnableAudio = enableAudio;
        }

        public void setEnableEarMonitoring(boolean enable) {
            mEnableEarMonitoring = enable;
        }

        public boolean isEnableEarMonitoring() {
            return mEnableEarMonitoring;
        }

        public boolean isAudioEarpieceMode() {
            return mAudioEarpieceMode;
        }

        public void setAudioEarpieceMode(boolean audioEarpieceMode) {
            mAudioEarpieceMode = audioEarpieceMode;
        }

        public boolean isAudioVolumeEvaluation() {
            return mAudioVolumeEvaluation;
        }

        public void setAudioVolumeEvaluation(boolean audioVolumeEvaluation) {
            mAudioVolumeEvaluation = audioVolumeEvaluation;
        }

        public int getRecordVolume() {
            return mRecordVolume;
        }

        public void setRecordVolume(int recordVolume) {
            mRecordVolume = recordVolume;
        }

        public int getPlayoutVolume() {
            return mPlayoutVolume;
        }

        public void setPlayoutVolume(int playoutVolume) {
            mPlayoutVolume = playoutVolume;
        }

        public boolean isSaveFlag() {
            return mSaveFlag;
        }

        public void setSaveFlag(boolean saveFlag) {
            mSaveFlag = saveFlag;
        }

        public void saveCache() {
            try {
                SPUtils.getInstance(PER_DATA).put(PER_SAVE_FLAG, mSaveFlag);
                if (mSaveFlag) {
                    SPUtils.getInstance(PER_DATA).put(PER_DATA_PARAM, GsonUtils.toJson(this));
                }
            } catch (Exception e) {
            }
        }

        public void loadCache() {
            try {
                String json = SPUtils.getInstance(PER_DATA).getString(PER_DATA_PARAM);
                boolean isSaveFlag = SPUtils.getInstance(PER_DATA).getBoolean(PER_SAVE_FLAG, mSaveFlag);
                AudioConfig setting = GsonUtils.fromJson(json, AudioConfig.class);
                setting.setSaveFlag(isSaveFlag);
                copyFromSetting(setting);
            } catch (Exception e) {
            }
        }

        public boolean isRecording() {
            return mRecording;
        }

        public void setRecording(boolean recording) {
            mRecording = recording;
        }
    }

    // 推流视频设置项，通过json的形式保存到本地，在初始化时取出

    class VideoConfig implements Serializable {
        final static int DEFAULT_BITRATE = 600;
        final static int DEFAULT_FPS = 15;
        private final static String PER_DATA = "per_video_data";
        private final static String PER_DATA_PARAM = "per_video_param";
        private final static String PER_SAVE_FLAG = "per_save_flag";
        // 分辨率
        private V2TXLiveDef.V2TXLiveVideoResolution mVideoResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360;
        // 帧率
        private int mVideoFps = DEFAULT_FPS;
        // 码率
        private int mVideoBitrate = DEFAULT_BITRATE;
        // 流控模式：默认云端流控
        private int mQosMode = TRTCCloudDef.VIDEO_QOS_CONTROL_SERVER;
        // 画质偏好
        private int mQosPreference = TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_CLEAR;
        // 竖屏模式，true为竖屏
        private boolean mVideoVertical = true;
        // 画面填充方向是否为充满， true为充满
        private boolean mVideoFillMode = false;
        // 画面预览镜像类型
        private int mMirrorType = TRTC_VIDEO_MIRROR_TYPE_AUTO;
        // 是否打开视频采集
        private boolean mEnableVideo = true;
        // 是否推送视频
        private boolean mPublishVideo = true;
        // 远端镜像
        private boolean mRemoteMirror = false;
        // 是否开启水印
        private boolean mWatermark = false;
        // 双路编码:是否开启小流
        private boolean mEnableSmall = false;
        // 是否开启默认小流
        private boolean mPriorSmall = false;
        // 重力感应
        private boolean mEnableGSensorMode = false;
        // 云端混流模式
        private int mCloudMixtureMode = TRTCCloudDef.TRTC_TranscodingConfigMode_Manual;
        // 本地视频旋转角
        private int mLocalRotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        private int mRemoteRotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        // 自定义流Id
        private String mCustomLiveId;
        // 当前是否处于混流状态
        private transient boolean mCurIsMix = false;
        // 是否已经暂停屏幕采集
        private boolean mIsScreenCapturePaused = false;
        // 图像增强
        private boolean mEnableSharpnessEnhancement = true;
        // mute推的图片
        private boolean mEnableMuteImage = false;
        private Bitmap mMuteImage = null;
        //混合后画面的背景图
        private String mMixStreamBackgroundImage;
        //自定义流ID
        private String mMixStreamId = "";
        // VOD播放器
        private boolean mEnableVodPlayer = false;
        /**
         * 是否默认保存到本地，第二次启动的时候，会自动从本地加载
         * transient 表示这个参数不会序列化
         */
        private transient boolean mSaveFlag = true;
        private int mCurrentResolutionPosition = 4; // 640*360
        private int mCurrentFPSPosition = 1; // 15fps

        public VideoConfig() {
            //loadCache();
        }

        public void setCurrentResolutionPosition(int currentPosition) {
            mCurrentResolutionPosition = currentPosition;
        }

        public int getCurrentResolutionPosition() {
            return mCurrentResolutionPosition;
        }

        public void setCurrentFPSPosition(int currentPosition) {
            mCurrentFPSPosition = currentPosition;
        }

        public int getCurrentFPSPosition() {
            return mCurrentFPSPosition;
        }

        public boolean isCurIsMix() {
            return mCurIsMix;
        }

        public void setCurIsMix(boolean curIsMix) {
            mCurIsMix = curIsMix;
        }

        public boolean isPublishVideo() {
            return mPublishVideo;
        }

        public void setPublishVideo(boolean publishVideo) {
            mPublishVideo = publishVideo;
        }

        public int getCloudMixtureMode() {
            return mCloudMixtureMode;
        }

        public void setCloudMixtureMode(int mode) {
            mCloudMixtureMode = mode;
        }

        public void copyFromSetting(VideoConfig other) {
            this.mVideoResolution = other.mVideoResolution;
            this.mVideoFps = other.mVideoFps;
            this.mVideoBitrate = other.mVideoBitrate;
            this.mQosPreference = other.mQosPreference;
            this.mVideoVertical = other.mVideoVertical;
            this.mVideoFillMode = other.mVideoFillMode;
            this.mMirrorType = other.mMirrorType;
            this.mEnableVideo = other.mEnableVideo;
            this.mPublishVideo = other.mPublishVideo;
            this.mRemoteMirror = other.mRemoteMirror;
            this.mWatermark = other.mWatermark;
            this.mQosMode = other.mQosMode;
            this.mEnableSmall = other.mEnableSmall;
            this.mPriorSmall = other.mPriorSmall;
            this.mEnableGSensorMode = other.mEnableGSensorMode;
            this.mCloudMixtureMode = other.mCloudMixtureMode;
            this.mLocalRotation = other.mLocalRotation;
            this.mRemoteRotation = other.mRemoteRotation;
            this.mCustomLiveId = other.mCustomLiveId;
            this.mIsScreenCapturePaused = other.mIsScreenCapturePaused;
            this.mEnableSharpnessEnhancement = other.mEnableSharpnessEnhancement;
            this.mSaveFlag = other.mSaveFlag;
            this.mCurrentResolutionPosition = other.mCurrentResolutionPosition;
            this.mCurrentFPSPosition = other.mCurrentFPSPosition;
        }

        public int getRemoteRotation() {
            return mRemoteRotation;
        }

        public void setRemoteRotation(int remoteRotation) {
            mRemoteRotation = remoteRotation;
        }

        public int getLocalRotation() {
            return mLocalRotation;
        }

        public void setLocalRotation(int localRotation) {
            mLocalRotation = localRotation;
        }

        public boolean isEnableGSensorMode() {
            return mEnableGSensorMode;
        }

        public void setEnableGSensorMode(boolean enableGSensorMode) {
            mEnableGSensorMode = enableGSensorMode;
        }

        public int getQosMode() {
            return mQosMode;
        }

        public void setQosMode(int qosMode) {
            mQosMode = qosMode;
        }

        public boolean isEnableSmall() {
            return mEnableSmall;
        }

        public void setEnableSmall(boolean enableSmall) {
            mEnableSmall = enableSmall;
        }

        public boolean isPriorSmall() {
            return mPriorSmall;
        }

        public void setPriorSmall(boolean priorSmall) {
            mPriorSmall = priorSmall;
        }

        public V2TXLiveDef.V2TXLiveVideoResolution getVideoResolution() {
            return mVideoResolution;
        }

        public void setVideoResolution(V2TXLiveDef.V2TXLiveVideoResolution videoResolution) {
            mVideoResolution = videoResolution;
        }

        public int getVideoFps() {
            return mVideoFps;
        }

        public void setVideoFps(int videoFps) {
            mVideoFps = videoFps;
        }

        public int getVideoBitrate() {
            return mVideoBitrate;
        }

        public void setVideoBitrate(int videoBitrate) {
            mVideoBitrate = videoBitrate;
        }

        public boolean isVideoVertical() {
            return mVideoVertical;
        }

        public void setVideoVertical(boolean videoVertical) {
            mVideoVertical = videoVertical;
        }

        public int getQosPreference() {
            return mQosPreference;
        }

        public void setQosPreference(int qosPreference) {
            mQosPreference = qosPreference;
        }

        public boolean isVideoFillMode() {
            return mVideoFillMode;
        }

        public void setVideoFillMode(boolean videoFillMode) {
            mVideoFillMode = videoFillMode;
        }

        public int getMirrorType() {
            return mMirrorType;
        }

        public void setMirrorType(int mirrorType) {
            mMirrorType = mirrorType;
        }

        public boolean isEnableVideo() {
            return mEnableVideo;
        }

        public void setEnableVideo(boolean enableVideo) {
            mEnableVideo = enableVideo;
        }

        public boolean isRemoteMirror() {
            return mRemoteMirror;
        }

        public void setRemoteMirror(boolean remoteMirror) {
            mRemoteMirror = remoteMirror;
        }

        public boolean isWatermark() {
            return mWatermark;
        }

        public void setWatermark(boolean watermark) {
            mWatermark = watermark;
        }

        public boolean isSaveFlag() {
            return mSaveFlag;
        }

        public String getCustomLiveId() {
            return mCustomLiveId;
        }

        public void setCustomLiveId(String customLiveId) {
            mCustomLiveId = customLiveId;
        }

        public void setSaveFlag(boolean saveFlag) {
            mSaveFlag = saveFlag;
        }

        public void setScreenCapturePaused(boolean paused) {
            mIsScreenCapturePaused = paused;
        }

        public boolean isScreenCapturePaused() {
            return mIsScreenCapturePaused;
        }

        public void setMixStreamBackgroundImage(String backgroundImage) {
            mMixStreamBackgroundImage = backgroundImage;
        }

        public String getMixStreamBackgroundImage() {
            return mMixStreamBackgroundImage;
        }

        public void setMixStreamId(String streamId) {
            mMixStreamId = streamId;
        }

        public String getMixStreamId() {
            return mMixStreamId;
        }

        public void setSharpnessEnhancementEnabled(boolean enabled) {
            mEnableSharpnessEnhancement = enabled;
        }

        public boolean isSharpnessEnhancementEnabled() {
            return mEnableSharpnessEnhancement;
        }

        public void setMuteImageEnabled(boolean enabled) {
            mEnableMuteImage = enabled;
        }

        public boolean isMuteImageEnabled() {
            return mEnableMuteImage;
        }

        public void setMuteImage(Bitmap bitmap) {
            mMuteImage = bitmap;
        }

        public Bitmap getMuteImage() {
            return mMuteImage;
        }

        public void setVodPlayerEnabled(boolean enabled) {
            mEnableVodPlayer = enabled;
        }

        public boolean isVodPlayerEnabled() {
            return mEnableVodPlayer;
        }

        public void saveCache() {
            try {
                SPUtils.getInstance(PER_DATA).put(PER_SAVE_FLAG, mSaveFlag);
                if (mSaveFlag) {
                    SPUtils.getInstance(PER_DATA).put(PER_DATA_PARAM, GsonUtils.toJson(this));
                }
            } catch (Exception e) {
            }
        }

        public void loadCache() {
            try {
                String json = SPUtils.getInstance(PER_DATA).getString(PER_DATA_PARAM);
                boolean isSaveFlag = SPUtils.getInstance(PER_DATA).getBoolean(PER_SAVE_FLAG, mSaveFlag);
                VideoConfig setting = GsonUtils.fromJson(json, VideoConfig.class);
                setting.setSaveFlag(isSaveFlag);
                copyFromSetting(setting);
            } catch (Exception e) {

            }
        }
    }
}
