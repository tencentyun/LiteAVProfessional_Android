package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class EnableSharpnessEnhancementItem extends AbsSwitchItem {
    private TRTCCloudManager mTRTCCloudManager;

    public EnableSharpnessEnhancementItem(TRTCCloudManager manager, Context context, String title) {
        super(context, false, title);
        mTRTCCloudManager = manager;
        setCheck(SettingConfigHelper.getInstance().getVideoConfig().isSharpnessEnhancementEnabled());
        mTRTCCloudManager.setSharpnessEnhancementEnabled(SettingConfigHelper.getInstance().getVideoConfig().isSharpnessEnhancementEnabled());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        SettingConfigHelper.getInstance().getVideoConfig().setSharpnessEnhancementEnabled(getChecked());
        mTRTCCloudManager.setSharpnessEnhancementEnabled(getChecked());
    }
}
