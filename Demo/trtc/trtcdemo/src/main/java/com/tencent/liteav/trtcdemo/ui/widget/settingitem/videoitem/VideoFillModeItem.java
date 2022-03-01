package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class VideoFillModeItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public VideoFillModeItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setSelect(SettingConfigHelper.getInstance().getVideoConfig().isVideoFillMode() ? 0 : 1);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index) {
        SettingConfigHelper.getInstance().getVideoConfig().setVideoFillMode(index == 0);
        mTRTCCloudManager.setVideoFillMode(SettingConfigHelper.getInstance().getVideoConfig().isVideoFillMode());
    }
}
