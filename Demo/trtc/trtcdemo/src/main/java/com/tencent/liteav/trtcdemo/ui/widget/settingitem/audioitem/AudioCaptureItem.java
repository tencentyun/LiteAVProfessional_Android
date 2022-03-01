package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class AudioCaptureItem extends AbsSwitchItem {
    private TRTCCloudManager mTRTCCloudManager;

    public AudioCaptureItem(TRTCCloudManager manager, Context context, String title) {
        super(context, true, title);
        mTRTCCloudManager = manager;
        setCheck(SettingConfigHelper.getInstance().getAudioConfig().isAudioCapturingStarted());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        boolean bChecked = getChecked();
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        audioConfig.setAudioCapturingStarted(bChecked);
        if (bChecked) {
            mTRTCCloudManager.startLocalAudio();
            mTRTCCloudManager.enableEarMonitoring(audioConfig.isEnableEarMonitoring());
        } else {
            mTRTCCloudManager.stopLocalAudio();
        }
    }
}

