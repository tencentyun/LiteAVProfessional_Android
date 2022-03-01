package com.tencent.liteav.trtcdemo.model.bean;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.SPUtils;
import com.tencent.trtc.TRTCCloudDef;

import java.io.Serializable;

import static com.blankj.utilcode.util.GsonUtils.fromJson;
import static com.blankj.utilcode.util.GsonUtils.toJson;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;

/**
 * 视频 Tab页 相关参数配置
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class VideoConfig implements Serializable {
    final static         int     DEFAULT_BITRATE            = 600;
    final static         int     DEFAULT_FPS                = 15;
    private final static String  PER_DATA                   = "per_video_data";
    private final static String  PER_DATA_PARAM             = "per_video_param";
    private final static String  PER_SAVE_FLAG              = "per_save_flag";
    // 分辨率
    private              int     mMainStreamVideoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
    // 帧率
    private              int     mMainStreamVideoFps        = DEFAULT_FPS;
    // 码率
    private              int     mMainStreamVideoBitrate    = DEFAULT_BITRATE;
    // 辅路分辨率
    private              int     mSubStreamVideoResolution  = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
    // 辅路帧率
    private              int     mSubStreamVideoFps         = DEFAULT_FPS;
    // 辅路码率
    private              int     mSubStreamVideoBitrate     = DEFAULT_BITRATE;
    // 画质偏好
    private              int     mQosPreference             = TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_CLEAR;
    // 主路竖屏模式，true为竖屏
    private              boolean mMainStreamVideoVertical   = true;
    // 辅路竖屏模式，true为竖屏
    private              boolean mSubStreamVideoVertical    = true;
    // 画面填充方向是否为充满， true为充满
    private              boolean mVideoFillMode             = true;
    // 画面预览镜像类型
    private              int     mMirrorType                = TRTC_VIDEO_MIRROR_TYPE_AUTO;
    // 是否打开视频采集
    private              boolean mEnableVideo               = true;
    // 是否推送视频
    private              boolean mPublishVideo              = true;
    // 远端镜像
    private              boolean mRemoteMirror              = false;
    // 是否开启水印
    private              boolean mWatermark                 = false;
    // 是否开启时间水印
    private              boolean mTimeWatermark             = false;
    // 云端混流模式
    private              int     mCloudMixtureMode          = TRTCCloudDef.TRTC_TranscodingConfigMode_Manual;
    // 本地视频旋转角
    private              int     mLocalRotation             = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
    private              int     mRemoteRotation            = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
    // 自定义流Id
    private              String  mCustomLiveId;
    // 当前是否处于混流状态
    private transient    boolean mCurIsMix                  = false;
    // 是否已经暂停屏幕采集
    private              boolean mIsScreenCapturePaused     = false;
    // 图像增强
    private              boolean mEnableSharpnessEnhance    = true;
    // mute推的图片
    private              boolean mEnableMuteImage           = false;
    private transient    Bitmap  mMuteImage                 = null;
    //混合后画面的背景图
    private              String  mMixStreamBackgroundImage;
    //自定义流ID
    private              String  mMixStreamId               = "";

    // 是否默认保存到本地，第二次启动的时候，会自动从本地加载
    private transient boolean mSaveFlag = true;

    private VideoEnableListener mVideoEnableListener;

    public VideoConfig() {
        //loadCache();
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
        this.mMainStreamVideoResolution = other.mMainStreamVideoResolution;
        this.mMainStreamVideoFps = other.mMainStreamVideoFps;
        this.mMainStreamVideoBitrate = other.mSubStreamVideoBitrate;
        this.mSubStreamVideoResolution = other.mSubStreamVideoResolution;
        this.mSubStreamVideoFps = other.mSubStreamVideoFps;
        this.mSubStreamVideoBitrate = other.mMainStreamVideoBitrate;
        this.mQosPreference = other.mQosPreference;
        this.mMainStreamVideoVertical = other.mMainStreamVideoVertical;
        this.mSubStreamVideoVertical = other.mSubStreamVideoVertical;
        this.mVideoFillMode = other.mVideoFillMode;
        this.mMirrorType = other.mMirrorType;
        this.mEnableVideo = other.mEnableVideo;
        this.mPublishVideo = other.mPublishVideo;
        this.mRemoteMirror = other.mRemoteMirror;
        this.mWatermark = other.mWatermark;
        this.mCloudMixtureMode = other.mCloudMixtureMode;
        this.mLocalRotation = other.mLocalRotation;
        this.mRemoteRotation = other.mRemoteRotation;
        this.mCustomLiveId = other.mCustomLiveId;
        this.mIsScreenCapturePaused = other.mIsScreenCapturePaused;
        this.mEnableSharpnessEnhance = other.mEnableSharpnessEnhance;
        this.mSaveFlag = other.mSaveFlag;
        this.mMixStreamBackgroundImage = other.mMixStreamBackgroundImage;
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

    public int getMainStreamVideoResolution() {
        return mMainStreamVideoResolution;
    }

    public void setMainStreamVideoResolution(int videoResolution) {
        mMainStreamVideoResolution = videoResolution;
    }

    public int getMainStreamVideoFps() {
        return mMainStreamVideoFps;
    }

    public void setMainStreamVideoFps(int videoFps) {
        mMainStreamVideoFps = videoFps;
    }

    public int getMainStreamVideoBitrate() {
        return mMainStreamVideoBitrate;
    }

    public void setMainStreamVideoBitrate(int videoBitrate) {
        mMainStreamVideoBitrate = videoBitrate;
    }

    public int getSubStreamVideoResolution() {
        return mSubStreamVideoResolution;
    }

    public void setSubStreamVideoResolution(int videoResolution) {
        mSubStreamVideoResolution = videoResolution;
    }

    public int getSubStreamVideoFps() {
        return mSubStreamVideoFps;
    }

    public void setSubStreamVideoFps(int videoFps) {
        mSubStreamVideoFps = videoFps;
    }

    public int getSubStreamVideoBitrate() {
        return mSubStreamVideoBitrate;
    }

    public void setSubStreamVideoBitrate(int videoBitrate) {
        mSubStreamVideoBitrate = videoBitrate;
    }

    public boolean isMainStreamVideoVertical() {
        return mMainStreamVideoVertical;
    }

    public void setMainStreamVideoVertical(boolean videoVertical) {
        mMainStreamVideoVertical = videoVertical;
    }

    public boolean isSubStreamVideoVertical() {
        return mSubStreamVideoVertical;
    }

    public void setSubStreamVideoVertical(boolean videoVertical) {
        mSubStreamVideoVertical = videoVertical;
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
        if (mVideoEnableListener != null) {
            mVideoEnableListener.onChecked(enableVideo);
        }
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

    public boolean isTimeWatermark() {
        return mTimeWatermark;
    }

    public void setTimeWatermark(boolean enableTimeWatermark) {
        mTimeWatermark = enableTimeWatermark;
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
        mEnableSharpnessEnhance = enabled;
    }

    public boolean isSharpnessEnhancementEnabled() {
        return mEnableSharpnessEnhance;
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

    public void saveCache() {
        try {
            SPUtils.getInstance(PER_DATA).put(PER_SAVE_FLAG, mSaveFlag);
            if (mSaveFlag) {
                //bitmap 不需要序列化
                Bitmap muteImage = mMuteImage;
                mMuteImage = null;
                SPUtils.getInstance(PER_DATA).put(PER_DATA_PARAM, toJson(this));
                mMuteImage = muteImage;
            }
        } catch (Exception e) {
        }
    }

    public void loadCache() {
        try {
            String json = SPUtils.getInstance(PER_DATA).getString(PER_DATA_PARAM);
            boolean isSaveFlag = SPUtils.getInstance(PER_DATA).getBoolean(PER_SAVE_FLAG, mSaveFlag);
            VideoConfig setting = fromJson(json, VideoConfig.class);
            setting.setSaveFlag(isSaveFlag);
            copyFromSetting(setting);
        } catch (Exception e) {

        }
    }

    public void reset() {
        mMainStreamVideoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
        mMainStreamVideoFps = DEFAULT_FPS;
        mMainStreamVideoBitrate = DEFAULT_BITRATE;
        mSubStreamVideoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
        mSubStreamVideoFps = DEFAULT_FPS;
        mSubStreamVideoBitrate = DEFAULT_BITRATE;
        mQosPreference = TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_CLEAR;
        mMainStreamVideoVertical = true;
        mSubStreamVideoVertical = true;
        mVideoFillMode = true;
        mMirrorType = TRTC_VIDEO_MIRROR_TYPE_AUTO;
        mEnableVideo = true;
        mPublishVideo = true;
        mRemoteMirror = false;
        mWatermark = false;
        mTimeWatermark = false;
        mCloudMixtureMode = TRTCCloudDef.TRTC_TranscodingConfigMode_Manual;
        mLocalRotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        mRemoteRotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        mIsScreenCapturePaused = false;
        mEnableSharpnessEnhance = true;
        mEnableMuteImage = false;
        mMixStreamBackgroundImage = "";
        setVideoEnableListener(null);
    }

    public void setVideoEnableListener(VideoEnableListener listener) {
        mVideoEnableListener = listener;
    }

    public interface VideoEnableListener {
        void onChecked(boolean isEnable);
    }
}
