package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings;

import android.content.Context;
import android.text.InputType;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base.AbsEditTextSendItem;

public class VideoBitrateItem extends AbsEditTextSendItem {

    public VideoBitrateItem(Context context, String title, String btnText) {
        super(context, true, title, btnText);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setInputText("150");
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void send(String msg) {

    }
}
