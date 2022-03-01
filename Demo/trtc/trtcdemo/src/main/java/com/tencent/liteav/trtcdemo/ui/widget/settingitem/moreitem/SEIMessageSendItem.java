package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextSendItem;

/**
 * 发送SEI消息Item
 */
public class SEIMessageSendItem extends AbsEditTextSendItem {

    private TRTCCloudManager mTRTCCloudManager;

    public SEIMessageSendItem(TRTCCloudManager manager, Context context, String title, String btnText) {
        super(context, false, title, btnText);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void send(String msg) {
        mTRTCCloudManager.sendSEIMsg(msg);
    }
}
