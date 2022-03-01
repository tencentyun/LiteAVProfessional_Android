package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsTipButtonItem;

public class RemoteSnapshotItem extends AbsTipButtonItem {

    private SnapshotListener mListener;

    public RemoteSnapshotItem(SnapshotListener listener, Context context, String title, String btnText) {
        super(context, false, title, btnText);
        mListener = listener;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onClicked() {
        if (mListener != null) {
            mListener.onClick();
        }
    }

    public interface SnapshotListener {
        void onClick();
    }
}
