package com.tencent.liteav.trtcdemo.model.manager;

import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;

/**
 * 混音模块控制类
 */
public class TRTCMixAudioManager {

    private TRTCCloud mTRTCCloud;

    public TRTCMixAudioManager(TRTCCloud trtcCloud) {
        mTRTCCloud = trtcCloud;
    }

    public void enableMixExternalAudioFrame(boolean enablePublish, boolean enablePlayout) {
        if (mTRTCCloud != null) {
            mTRTCCloud.enableMixExternalAudioFrame(enablePublish, enablePlayout);
        }
    }

    public int mixExternalAudioFrame(TRTCCloudDef.TRTCAudioFrame frame) {
        if (mTRTCCloud != null) {
            return mTRTCCloud.mixExternalAudioFrame(frame);
        }
        return 0;
    }

    public void setMixExternalAudioVolume(int publishVolume, int playoutVolume) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setMixExternalAudioVolume(publishVolume, playoutVolume);
        }
    }

}
