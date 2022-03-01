package com.tencent.liteav.trtcdemo.model.bean;

import com.blankj.utilcode.util.SPUtils;
import com.tencent.liteav.trtcdemo.model.utils.TRTCConstants;
import com.tencent.trtc.TRTCCloudDef;

import java.io.Serializable;
import java.util.ArrayList;

import static com.blankj.utilcode.util.GsonUtils.fromJson;
import static com.blankj.utilcode.util.GsonUtils.toJson;

/**
 * 音频 Tab页 相关参数配置
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class AudioConfig implements Serializable {
    private final static String PER_DATA       = "per_audio_data";
    private final static String PER_DATA_PARAM = "per_audio_param";
    private final static String PER_SAVE_FLAG  = "per_save_flag";

    private           boolean mEnable16KSampleRate       = false;    // 是否打开16k采样率，true 16k false 48k
    private           int     mAudioVolumeType           = TRTCCloudDef.TRTCSystemVolumeTypeAuto;    // 系统音量类型
    private           int     mAudioQulity               = TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT;
    private           boolean mAGC                       = false;    // 自动增益
    private           int     mANS                       = 0;   // 噪音消除
    private           int     mAEC                       = 0;    // 回声消除
    private           boolean mIsAudioCapturingStarted   = true;    // 是否打开音频采集
    private           boolean mIsLocalAudioMuted         = false;    // 是否将本地采集静音
    private           boolean mEnableEarMonitoring       = false;    // 耳返开关
    private           boolean mAudioEarpieceMode         = false;   // 听筒模式 true 听筒 false 免提
    private           boolean mAudioVolumeEvaluation     = false;    // 是否打开音量提示，界面上表示为底下的音量条
    private           int     mRecordVolume              = 100;    // 采集音量
    private           int     mPlayoutVolume             = 100;    // 播放音量
    private transient boolean mSaveFlag                  = true;    //是否默认保存到本地，第二次启动的时候，会自动从本地加载
    private transient boolean mRecording                 = false;    // 录音状态， true 正在录音 false 结束录音
    private           int     mRecordType                = TRTCCloudDef.TRTC_AudioRecordingContent_All;
    private           int     mAudioBitrate              = 50;      //音频码率
    private           double  mVoicePitch                = 0.0;    // 声调
    private           int     mAudioParallelMaxCount     = TRTCConstants.TRTC_SDK_AUDIO_PARALLEL_MAX_COUNT; //最大并发播放数
    private ArrayList<String> mAudioParallelIncludeUsers = new ArrayList<>();      // 音频必定播放用户

    public AudioConfig() {
        //loadCache();
    }

    public void copyFromSetting(AudioConfig other) {
        this.mEnable16KSampleRate = other.mEnable16KSampleRate;
        this.mAudioVolumeType = other.mAudioVolumeType;
        this.mAGC = other.mAGC;
        this.mANS = other.mANS;
        this.mIsAudioCapturingStarted = other.mIsAudioCapturingStarted;
        this.mAudioEarpieceMode = other.mAudioEarpieceMode;
        this.mAudioVolumeEvaluation = other.mAudioVolumeEvaluation;
        this.mSaveFlag = other.mSaveFlag;
        this.mRecording = other.mRecording;
        this.mEnableEarMonitoring = other.mEnableEarMonitoring;
        this.mRecordVolume = other.mRecordVolume;
        this.mPlayoutVolume = other.mPlayoutVolume;
        this.mAudioQulity = other.mAudioQulity;
        this.mRecordType = other.mRecordType;
        this.mVoicePitch = other.mVoicePitch;
        this.mAudioParallelMaxCount = other.mAudioParallelMaxCount;
        this.mAudioParallelIncludeUsers = other.mAudioParallelIncludeUsers;
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

    public int getANS() {
        return mANS;
    }

    public void setANS(int ANS) {
        mANS = ANS;
    }

    public int getAEC() {
        return mAEC;
    }

    public void setAEC(int AEC) {
        mAEC = AEC;
    }

    public boolean isAudioCapturingStarted() {
        return mIsAudioCapturingStarted;
    }

    public void setAudioCapturingStarted(boolean isStarted) {
        mIsAudioCapturingStarted = isStarted;
    }

    public boolean isLocalAudioMuted() {
        return mIsLocalAudioMuted;
    }

    public void setLocalAudioMuted(boolean mute) {
        mIsLocalAudioMuted = mute;
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
                SPUtils.getInstance(PER_DATA).put(PER_DATA_PARAM, toJson(this));
            }
        } catch (Exception e) {
        }
    }

    public void loadCache() {
        try {
            String json = SPUtils.getInstance(PER_DATA).getString(PER_DATA_PARAM);
            boolean isSaveFlag = SPUtils.getInstance(PER_DATA).getBoolean(PER_SAVE_FLAG, mSaveFlag);
            AudioConfig setting = fromJson(json, AudioConfig.class);
            setting.setSaveFlag(isSaveFlag);
            copyFromSetting(setting);
        } catch (Exception e) {
        }
    }

    public int getAudioQulity() {
        return mAudioQulity;
    }

    public void setAudioQulity(int audioQulity) {
        this.mAudioQulity = audioQulity;
    }

    public boolean isRecording() {
        return mRecording;
    }

    public void setRecording(boolean recording) {
        mRecording = recording;
    }

    public int getRecordType() {
        return mRecordType;
    }

    public void setRecordType(int type) {
        mRecordType = type;
    }

    public int getAudioBitrate() {
        return mAudioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.mAudioBitrate = audioBitrate;
    }

    public void setVoicePitch(double voicePitch) {
        this.mVoicePitch = voicePitch;
    }

    public double getVoicePitch() {
        return mVoicePitch;
    }

    public int getAudioParallelMaxCount() {
        return mAudioParallelMaxCount;
    }

    public void setAudioParallelMaxCount(int audioParallelMaxCount) {
        mAudioParallelMaxCount = audioParallelMaxCount;
    }

    public boolean setAudioParallelIncludeUsers(String userName, boolean isAdd) {
        if (mAudioParallelMaxCount == 0 || (isAdd && mAudioParallelMaxCount - mAudioParallelIncludeUsers.size() <= 1)) {
            return false;
        }

        if (isAdd) {
            mAudioParallelIncludeUsers.add(userName);
        } else {
            mAudioParallelIncludeUsers.remove(userName);
        }

        return true;
    }

    public ArrayList<String> getAudioParallelIncludeUsers() {
        return mAudioParallelIncludeUsers;
    }

    public void removeAudioParallelIncludeUsers() {
        mAudioParallelIncludeUsers.clear();
    }

    public void reset() {
        mAudioEarpieceMode = false;
        mAEC = 0;
        mAGC = false;
        mANS = 0;
        mAudioVolumeEvaluation = false;
        mRecordVolume = 100;
        mPlayoutVolume = 100;
        mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
        mAudioQulity = TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT;
        mEnableEarMonitoring = false;
        mRecordType = TRTCCloudDef.TRTC_AudioRecordingContent_All;
        mAudioBitrate = 50;
        mVoicePitch = 0.0;
    }
}
