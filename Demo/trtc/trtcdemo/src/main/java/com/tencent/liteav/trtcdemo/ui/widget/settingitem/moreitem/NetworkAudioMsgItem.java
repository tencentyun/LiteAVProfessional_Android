package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextSendItem;

public class NetworkAudioMsgItem extends AbsEditTextSendItem {

    private TRTCCloudManager mTRTCCloudManager;

    public NetworkAudioMsgItem(TRTCCloudManager manager, Context context, String title, String btnText) {
        super(context, true, title, btnText);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void send(String msg) {
        mTRTCCloudManager.sendMsgToAudioPkg(msg);
    }
}

