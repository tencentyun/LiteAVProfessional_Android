package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class OpenVideoCaptureItem extends AbsSwitchItem {
    private TRTCCloudManager mTRTCCloudManager;

    public OpenVideoCaptureItem(TRTCCloudManager manager, Context context, String title) {
        super(context, false, title);
        mTRTCCloudManager = manager;
        setCheck(SettingConfigHelper.getInstance().getVideoConfig().isEnableVideo());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        final boolean isChecked = getChecked();
        SettingConfigHelper.getInstance().getVideoConfig().setEnableVideo(isChecked);
        if (isChecked) {
            mTRTCCloudManager.startLocalPreview();
        } else {
            mTRTCCloudManager.stopLocalPreview();
        }
    }
}
