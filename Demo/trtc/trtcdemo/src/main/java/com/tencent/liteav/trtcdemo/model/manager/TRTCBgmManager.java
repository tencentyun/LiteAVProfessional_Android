package com.tencent.liteav.trtcdemo.model.manager;

import android.util.Log;

import com.tencent.liteav.trtcdemo.model.bean.AudioEqualizationConfig;
import com.tencent.liteav.trtcdemo.ui.widget.bgm.EffectItemView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BGM的控制类
 *
 * @author guanyifeng
 */
public class TRTCBgmManager {
    private static final String TAG = "TRTCBgmManager";

    private TRTCCloud               mTRTCCloud;                 // SDK 核心类
    private TRTCCloudDef.TRTCParams mTRTCParams;                // 进房参数
    private Map<Integer,Integer> mEffectNextStatus = new ConcurrentHashMap<>();
    enum EffectAction {
        PLAY,
        PAUSE,
        RESUME,
        STOP
    }
    public TRTCBgmManager(TRTCCloud trtcCloud, TRTCCloudDef.TRTCParams trtcParams) {
        mTRTCCloud = trtcCloud;
        mTRTCParams = trtcParams;
    }

    public void destroy() {
        stopBGM();
        stopAllAudioEffects();
    }

    /**
     * ==================================音效面板控制==================================
     */
    public void playAudioEffect(int effectId, String path, int count, boolean publish, double volume) {
        setEffectNextStatus(effectId, EffectAction.PLAY);
        if (mTRTCCloud != null) {
            TRTCCloudDef.TRTCAudioEffectParam effect = new TRTCCloudDef.TRTCAudioEffectParam(effectId, path);
            effect.loopCount = count;
            effect.publish = publish;
            effect.effectId = effectId;
            mTRTCCloud.playAudioEffect(effect);
            mTRTCCloud.setAudioEffectVolume(effect.effectId, (int)volume);
        }
    }

    public void pauseAudioEffect(int effectId) {
        setEffectNextStatus(effectId, EffectAction.PAUSE);
        if (mTRTCCloud != null) {
            mTRTCCloud.pauseAudioEffect(effectId);
        }
    }


    public void resumeAudioEffect(int effectId) {
        setEffectNextStatus(effectId, EffectAction.RESUME);
        if (mTRTCCloud != null) {
            mTRTCCloud.resumeAudioEffect(effectId);
        }
    }

    public void stopAudioEffect(int effectId) {
        setEffectNextStatus(effectId, EffectAction.STOP);
        if (mTRTCCloud != null) {
            mTRTCCloud.stopAudioEffect(effectId);
        }
    }

    public void setAudioEffectVolume(int effectId, int gain) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setAudioEffectVolume(effectId, gain);
        }
    }

    public void setEqualizationParam(int type, AudioEqualizationConfig customValue) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "setEqualizationParam");
            JSONObject params = new JSONObject();
            params.put("equalization_type", type);
            params.put("gain", customValue.getJSONArray());
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopAllAudioEffects() {
        setAllEffectNextStatus(EffectAction.STOP);
        if (mTRTCCloud != null) {
            mTRTCCloud.stopAllAudioEffects();
        }
    }

    public void onAudioEffectFinished(int effectId) {
        setEffectNextStatus(effectId, EffectAction.STOP);
    }

    private void setAllEffectNextStatus(EffectAction effectAction) {
        for(Integer effectId : mEffectNextStatus.keySet()) {
            mEffectNextStatus.put(effectId, getNextStatus(effectAction));
        }
    }

    private int getNextStatus(EffectAction effectAction) {
        int nextStatus = EffectItemView.STATUS_IDLE;
        switch (effectAction) {
            case PLAY:
            case RESUME:
                nextStatus = EffectItemView.STATUS_PAUSE;
                break;
            case PAUSE:
                nextStatus = EffectItemView.STATUS_RESUME;
                break;
            case STOP:
                nextStatus = EffectItemView.STATUS_IDLE;
                break;
        }
        return nextStatus;
    }

    public int getNextStatus(int effectId) {
        if (!mEffectNextStatus.containsKey(effectId)) {
            return EffectItemView.STATUS_IDLE;
        }
        return mEffectNextStatus.get(effectId);
    }

    private void setEffectNextStatus(int effectId, EffectAction effectAction) {
        mEffectNextStatus.put(effectId, getNextStatus(effectAction));
    }

    public void setAllAudioEffectsVolume(int gain) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setAllAudioEffectsVolume(gain);
        }
    }

    /**
     * ==================================BGM控制==================================
     */
    public void playBGM(String url, int loopTimes, int bgmVol, int micVol, TRTCCloud.BGMNotify notify) {
        if (mTRTCCloud != null) {
            mTRTCCloud.playBGM(url, notify);
            mTRTCCloud.setBGMVolume(bgmVol);
            mTRTCCloud.setMicVolumeOnMixing(micVol);
        }
    }

    public void resumeBGM() {
        if (mTRTCCloud != null) {
            mTRTCCloud.resumeBGM();
        }
    }

    public void pauseBGM() {
        if (mTRTCCloud != null) {
            mTRTCCloud.pauseBGM();
        }
    }

    public void stopBGM() {
        if (mTRTCCloud != null) {
            mTRTCCloud.stopBGM();
        }
    }

    public int getBGMDuration(String url) {
        if (mTRTCCloud == null) {
            return 0;
        }
        return mTRTCCloud.getBGMDuration(url);
    }

    public void setBGMPosition(int position) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setBGMPosition(position);
        }
    }

    public void setBGMVolume(int volume) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setBGMVolume(volume);
        }
    }

    public void setMicVolumeOnMixing(int volume) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setMicVolumeOnMixing(volume);
        }
    }

    public void setEarMonitorVolume(int volume) {
        if (mTRTCCloud != null) {
            mTRTCCloud.getAudioEffectManager().setVoiceEarMonitorVolume(volume);
        }
    }

    public void setPlayoutVolume(int volume) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setBGMPlayoutVolume(volume);
        }
    }

    public void setPublishVolume(int volume) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setBGMPublishVolume(volume);
        }
    }

    public void setReverbType(int type) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setReverbType(type);
        }
    }

    public void setVoiceChangerType(int type) {
        if (mTRTCCloud != null) {
            mTRTCCloud.setVoiceChangerType(type);
        }
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
