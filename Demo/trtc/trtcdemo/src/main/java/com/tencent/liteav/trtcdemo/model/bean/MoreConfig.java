package com.tencent.liteav.trtcdemo.model.bean;

import com.tencent.trtc.TRTCCloudDef;

import java.io.Serializable;

/**
 * 其他 Tab页 相关参数配置
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class MoreConfig implements Serializable {
    private boolean mEnableFlash       = false;
    private boolean mEnableGSensorMode = true;
    private boolean mIsRecording       = false;
    private int     mRecordType        = TRTCCloudDef.TRTC_RECORD_TYPE_BOTH;
    // VOD播放器
    private boolean mEnableVodPlayer   = false;
    // 是否开启默认小流
    private boolean mPriorSmall        = false;
    // 双路编码:是否开启小流
    private boolean mEnableSmall       = false;
    // 流控模式：默认云端流控
    private int     mQosMode           = TRTCCloudDef.VIDEO_QOS_CONTROL_SERVER;

    public boolean isEnableFlash() {
        return mEnableFlash;
    }

    public void setEnableFlash(boolean enableFlash) {
        mEnableFlash = enableFlash;
    }

    public void reset() {
        mEnableFlash = false;
        mEnableGSensorMode = true;
        mIsRecording = false;
        mRecordType = TRTCCloudDef.TRTC_RECORD_TYPE_BOTH;
        mEnableVodPlayer = false;
        mPriorSmall = false;
        mEnableSmall = false;
        mQosMode = TRTCCloudDef.VIDEO_QOS_CONTROL_SERVER;
    }

    public boolean isEnableGSensorMode() {
        return mEnableGSensorMode;
    }

    public void setEnableGSensorMode(boolean enableGSensorMode) {
        mEnableGSensorMode = enableGSensorMode;
    }

    public int getRecordType() {
        return mRecordType;
    }

    public void setRecordType(int type) {
        mRecordType = type;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setRecording(boolean recording) {
        mIsRecording = recording;
    }

    public void setVodPlayerEnabled(boolean enabled) {
        mEnableVodPlayer = enabled;
    }

    public boolean isVodPlayerEnabled() {
        return mEnableVodPlayer;
    }

    public boolean isPriorSmall() {
        return mPriorSmall;
    }

    public void setPriorSmall(boolean priorSmall) {
        mPriorSmall = priorSmall;
    }

    public boolean isEnableSmall() {
        return mEnableSmall;
    }

    public void setEnableSmall(boolean enableSmall) {
        mEnableSmall = enableSmall;
    }

    public int getQosMode() {
        return mQosMode;
    }

    public void setQosMode(int qosMode) {
        mQosMode = qosMode;
    }

}
