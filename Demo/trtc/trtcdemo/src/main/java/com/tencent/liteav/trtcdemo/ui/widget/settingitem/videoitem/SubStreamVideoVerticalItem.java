package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class SubStreamVideoVerticalItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public SubStreamVideoVerticalItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, true, title, textList);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setSelect(SettingConfigHelper.getInstance().getVideoConfig().isSubStreamVideoVertical() ? 1 : 0);
    }

    @Override
    public void onSelected(int index) {
        SettingConfigHelper.getInstance().getVideoConfig().setSubStreamVideoVertical(index == 1);
        mTRTCCloudManager.setTRTCCloudParam();
    }
}
