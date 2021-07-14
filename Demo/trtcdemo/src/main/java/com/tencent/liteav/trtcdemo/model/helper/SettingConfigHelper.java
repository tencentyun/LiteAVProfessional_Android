package com.tencent.liteav.trtcdemo.model.helper;

import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig;
import com.tencent.liteav.trtcdemo.model.bean.OtherConfig;
import com.tencent.liteav.trtcdemo.model.bean.PkConfig;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;

/**
 * 用来管理setting的配置项，全局维护一个实例，避免重复初始化
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class SettingConfigHelper {
    // 视频相关设置项
    private VideoConfig mVideoConfig;
    // 音频相关设置项
    private AudioConfig mAudioConfig;
    // 连麦相关设置项
    private PkConfig mPkConfig;
    // 其他的设置项
    private OtherConfig mOtherConfig;
    // CDN播放设置项
    private CdnPlayerConfig mCdnPlayerConfig;

    private SettingConfigHelper() {
    }

    public static SettingConfigHelper getInstance() {
        return SingletonHolder.instance;
    }

    public CdnPlayerConfig getCdnPlayerConfig() {
        if (mCdnPlayerConfig == null) {
            mCdnPlayerConfig = new CdnPlayerConfig();
        }
        return mCdnPlayerConfig;
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

    public PkConfig getPkConfig() {
        if (mPkConfig == null) {
            mPkConfig = new PkConfig();
        }
        return mPkConfig;
    }

    public OtherConfig getMoreConfig() {
        if (mOtherConfig == null) {
            mOtherConfig = new OtherConfig();
        }
        return mOtherConfig;
    }

    private static class SingletonHolder {
        /**
         * 由JVM来保证线程安全
         */
        private static SettingConfigHelper instance = new SettingConfigHelper();
    }

}
