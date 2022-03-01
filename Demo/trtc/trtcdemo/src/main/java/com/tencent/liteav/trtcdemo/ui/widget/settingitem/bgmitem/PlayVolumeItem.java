package com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;

public class PlayVolumeItem extends AbsSeekBarItem {

    public PlayVolumeItem(Context context, String title) {
        super(context, true, title, "");
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSeekBarChange(int index, boolean fromUser) {

    }
}
