package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;
import android.text.InputType;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextItem;

public class RoomPasswordItem extends AbsEditTextItem {

    public RoomPasswordItem(Context context, String title) {
        super(context, true, title);
        setInputType(InputType.TYPE_CLASS_TEXT);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

}
