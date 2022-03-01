package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class TimeWaterMarkItem extends AbsSwitchItem {
    private TRTCCloudManager mTRTCCloudManager;

    public TimeWaterMarkItem(TRTCCloudManager manager, Context context, String title) {
        super(context, true, title);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        SettingConfigHelper.getInstance().getVideoConfig().setTimeWatermark(getChecked());
        mTRTCCloudManager.enableTimeWatermark(SettingConfigHelper.getInstance().getVideoConfig().isTimeWatermark());
    }
}
