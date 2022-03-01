package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class AECItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public AECItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setSelect(SettingConfigHelper.getInstance().getAudioConfig().getAEC());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        switch (index) {
            case 0:
                mTRTCCloudManager.enableAEC(0);
                break;
            case 1:
                mTRTCCloudManager.enableAEC(60);
                break;
            case 2:
                mTRTCCloudManager.enableAEC(100);
                break;
            default:
                break;
        }
        SettingConfigHelper.getInstance().getAudioConfig().setAEC(index);
    }
}