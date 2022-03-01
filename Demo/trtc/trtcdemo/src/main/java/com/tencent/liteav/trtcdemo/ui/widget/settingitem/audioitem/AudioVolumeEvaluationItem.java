package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class AudioVolumeEvaluationItem extends AbsSwitchItem {
    private TRTCCloudManager mTRTCCloudManager;

    public AudioVolumeEvaluationItem(TRTCCloudManager manager, Context context, String title) {
        super(context, false, title);
        mTRTCCloudManager = manager;
        setCheck(SettingConfigHelper.getInstance().getAudioConfig().isAudioVolumeEvaluation());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        boolean bChecked = getChecked();
        SettingConfigHelper.getInstance().getAudioConfig().setAudioVolumeEvaluation(bChecked);
        mTRTCCloudManager.enableAudioVolumeEvaluation(bChecked);
    }
}

