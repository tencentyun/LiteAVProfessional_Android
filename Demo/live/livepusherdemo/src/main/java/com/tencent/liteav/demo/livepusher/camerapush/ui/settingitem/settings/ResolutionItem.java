package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base.AbsSelectionItem;

public class ResolutionItem extends AbsSelectionItem {

    public ResolutionItem(Context context, String title, String... textList) {
        super(context, false, title, textList);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index, String str) {

    }
}
