package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class RemoteEnableVideoItem extends AbsSwitchItem {

    private EnableViewListener mListener;

    public RemoteEnableVideoItem(EnableViewListener listener, Context context, String title) {
        super(context, false, title);
        this.mListener = listener;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        if (mListener != null) {
            mListener.onClicked();
        }
    }

    public interface EnableViewListener {
        void onClicked();
    }
}
