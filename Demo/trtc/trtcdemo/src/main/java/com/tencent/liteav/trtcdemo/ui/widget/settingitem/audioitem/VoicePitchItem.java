package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;

/**
 * 音调设置，范围：[-1, 1]
 */
public class VoicePitchItem extends AbsSeekBarItem {

    private static final int AUDIO_VOICE_PITCH_MAX = 100;

    private TRTCCloudManager mTRTCCloudManager;

    public VoicePitchItem(TRTCCloudManager manager, Context context, String title) {
        super(context, false, title, "");
        mTRTCCloudManager = manager;
        setProgress(pitch2process(SettingConfigHelper.getInstance().getAudioConfig().getVoicePitch()));
        setMax(AUDIO_VOICE_PITCH_MAX);
        setTip(SettingConfigHelper.getInstance().getAudioConfig().getVoicePitch() + "");
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSeekBarChange(int index, boolean fromUser) {
        double pitch = process2pitch(index);
        SettingConfigHelper.getInstance().getAudioConfig().setVoicePitch(pitch);
        mTRTCCloudManager.getAudioEffectManager().setVoicePitch(pitch);
        setTip(pitch + "");
    }

    private double process2pitch(int process) {
        return (process - AUDIO_VOICE_PITCH_MAX / 2) / 50.0;
    }

    private int pitch2process(double pitch) {
        return (int) (pitch * 50.0 + 50);
    }
}
