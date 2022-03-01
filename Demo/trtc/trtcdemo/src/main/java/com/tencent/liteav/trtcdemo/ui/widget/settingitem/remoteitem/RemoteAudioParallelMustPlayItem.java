package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class RemoteAudioParallelMustPlayItem extends AbsSwitchItem {

    private ParallelMustPlayListener mListener;

    public RemoteAudioParallelMustPlayItem(ParallelMustPlayListener listener, Context context, String title) {
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

    public interface ParallelMustPlayListener {
        void onClicked();
    }
}