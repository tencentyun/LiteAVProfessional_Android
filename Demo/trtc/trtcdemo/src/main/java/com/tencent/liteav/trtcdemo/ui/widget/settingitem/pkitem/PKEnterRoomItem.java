package com.tencent.liteav.trtcdemo.ui.widget.settingitem.pkitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsButtonItem;

public class PKEnterRoomItem extends AbsButtonItem {
    private Listener mListener;

    public PKEnterRoomItem(Listener listener, Context context, String btnText) {
        super(context, false, btnText);
        mListener = listener;
        setPadding(0, SizeUtils.dp2px(50), 0, 0);
    }

    @Override
    public void onClicked() {
        if (mListener != null) {
            mListener.onClick();
        }
    }

    public interface Listener {
        void onClick();
    }
}
