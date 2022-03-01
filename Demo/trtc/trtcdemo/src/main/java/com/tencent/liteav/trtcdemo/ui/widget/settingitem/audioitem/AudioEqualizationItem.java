package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSelectionItem;

public class AudioEqualizationItem  extends AbsSelectionItem {
    private AudioEqualizationItem.OnSelectListener mListener;

    public AudioEqualizationItem(AudioEqualizationItem.OnSelectListener listener,
                                 Context context, String title, String... textList) {
        super(context, false, title, textList);
        mListener = listener;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index, String str) {
        if (mListener != null) {
            mListener.onSelected(index, str);
        }
    }

    public interface OnSelectListener {
        void onSelected(int index, String str);
    }
}
