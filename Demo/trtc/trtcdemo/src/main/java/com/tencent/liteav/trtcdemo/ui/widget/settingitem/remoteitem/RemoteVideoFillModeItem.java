package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class RemoteVideoFillModeItem extends AbsRadioButtonItem {

    private VideoFillModeListener mListener;

    public RemoteVideoFillModeItem(VideoFillModeListener listener, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mListener = listener;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index) {
        if (mListener != null) {
            mListener.onSelected(index);
        }
    }

    public interface VideoFillModeListener {
        void onSelected(int index);
    }
}
