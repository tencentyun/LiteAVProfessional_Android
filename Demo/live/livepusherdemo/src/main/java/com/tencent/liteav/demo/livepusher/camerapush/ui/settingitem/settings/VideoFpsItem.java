package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings;

import android.content.Context;
import android.text.InputType;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base.AbsEditTextSendItem;

public class VideoFpsItem extends AbsEditTextSendItem {

    public VideoFpsItem(Context context, String title, String btnText) {
        super(context, true, title, btnText);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setInputText("15");
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void send(String msg) {

    }
}
