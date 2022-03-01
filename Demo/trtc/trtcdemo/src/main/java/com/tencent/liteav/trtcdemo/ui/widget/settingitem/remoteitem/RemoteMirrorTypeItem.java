package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class RemoteMirrorTypeItem extends AbsRadioButtonItem {

    private MirrorTypeListener mListener;

    public RemoteMirrorTypeItem(MirrorTypeListener listener, Context context, String title, String... textList) {
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

    public interface MirrorTypeListener {
        void onSelected(int index);
    }
}
