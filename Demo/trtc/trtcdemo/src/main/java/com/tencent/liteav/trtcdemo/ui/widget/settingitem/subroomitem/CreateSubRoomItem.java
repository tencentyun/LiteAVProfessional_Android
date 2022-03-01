package com.tencent.liteav.trtcdemo.ui.widget.settingitem.subroomitem;

import android.content.Context;
import android.text.InputType;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextSendItem;

public class CreateSubRoomItem extends AbsEditTextSendItem {

    public CreateSubRoomItem(Context context, String title, String btnText) {
        super(context, true, title, btnText);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void send(String msg) {

    }
}

