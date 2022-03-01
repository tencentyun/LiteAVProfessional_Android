package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class MainVideoInputItem extends AbsRadioButtonItem {

    public MainVideoInputItem(Context context, String title, String... textList) {
        super(context, false, title, textList);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {

    }
}