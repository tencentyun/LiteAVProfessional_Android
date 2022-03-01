package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class ANSItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public ANSItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setSelect(SettingConfigHelper.getInstance().getAudioConfig().getANS());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        switch (index) {
            case 0:
                mTRTCCloudManager.enableANS(0);
                break;
            case 1:
                mTRTCCloudManager.enableANS(20);
                break;
            case 2:
                mTRTCCloudManager.enableANS(40);
                break;
            case 3:
                mTRTCCloudManager.enableANS(100);
                break;
            case 4:
                mTRTCCloudManager.enableANS(120);
                break;
            default:
                break;
        }
        SettingConfigHelper.getInstance().getAudioConfig().setANS(index);
    }
}