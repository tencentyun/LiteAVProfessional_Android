package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class EncoderTypeItem extends AbsRadioButtonItem {

    private TRTCCloudManager mTRTCCloudManager;

    public EncoderTypeItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, true, title, textList);
        mTRTCCloudManager = manager;
        setSelect(mTRTCCloudManager.mUse265Encode ? 0 : 1);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        mTRTCCloudManager.mUse265Encode = (index == 0);
        mTRTCCloudManager.setTRTCCloudParam();
    }
}