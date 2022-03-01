package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class GSensorModeItem extends AbsSwitchItem {
    private TRTCCloudManager mTRTCCloudManager;

    public GSensorModeItem(TRTCCloudManager manager, Context context, String title) {
        super(context, false, title);
        mTRTCCloudManager = manager;
        setCheck(SettingConfigHelper.getInstance().getMoreConfig().isEnableGSensorMode());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        SettingConfigHelper.getInstance().getMoreConfig().setEnableGSensorMode(getChecked());
        if (mTRTCCloudManager != null) {
            mTRTCCloudManager.setTRTCCloudParam();
            mTRTCCloudManager.enableGSensor(SettingConfigHelper.getInstance().getMoreConfig().isEnableGSensorMode());
        }
    }
}


