package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base.AbsRadioButtonItem;

public class RenderRotationItem extends AbsRadioButtonItem {

    public RenderRotationItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(0);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {

    }
}