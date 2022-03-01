package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class OtherRecordModeItem extends AbsRadioButtonItem {

    public OtherRecordModeItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(SettingConfigHelper.getInstance().getMoreConfig().getRecordType());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index) {
        SettingConfigHelper.getInstance().getMoreConfig().setRecordType(index);
    }
}
