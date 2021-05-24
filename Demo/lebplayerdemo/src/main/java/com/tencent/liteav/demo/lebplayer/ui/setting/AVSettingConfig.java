package com.tencent.liteav.demo.lebplayer.ui.setting;

import com.tencent.live2.V2TXLiveDef;

/**
 * 用来管理setting的配置项，全局维护一个实例，避免重复初始化
 */
public class AVSettingConfig {

    public V2TXLiveDef.V2TXLiveRotation rotation = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
    public V2TXLiveDef.V2TXLiveFillMode fillMode = V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill;
    public int playoutVolume = 100;
    public boolean enableVolumeCallback = true;

    private AVSettingConfig() {
    }

    public static AVSettingConfig getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        /**
         * 由JVM来保证线程安全
         */
        private static AVSettingConfig instance = new AVSettingConfig();
    }

}
