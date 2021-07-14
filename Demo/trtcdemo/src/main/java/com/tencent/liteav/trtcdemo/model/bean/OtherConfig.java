package com.tencent.liteav.trtcdemo.model.bean;

import java.io.Serializable;

/**
 * 其他 Tab页 相关参数配置
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class OtherConfig implements Serializable {
    private boolean mEnableFlash        = false;
    private boolean mEnableGSensorMode  = true;

    public boolean isEnableFlash() {
        return mEnableFlash;
    }

    public void setEnableFlash(boolean enableFlash) {
        mEnableFlash = enableFlash;
    }


    public void reset() {
        mEnableFlash = false;
    }

    public boolean isEnableGSensorMode() {
        return mEnableGSensorMode;
    }

    public void setEnableGSensorMode(boolean enableGSensorMode) {
        mEnableGSensorMode = enableGSensorMode;
    }
}
