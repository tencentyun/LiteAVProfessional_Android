package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base.AbsTipButtonItem;

public class SnapshotItem extends AbsTipButtonItem {

    public SnapshotItem(Context context, String title, String btnText) {
        super(context, false, title, btnText);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onClicked() {

    }
}
