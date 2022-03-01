package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSelectionItem;

/**
 * 主路分辨率设置Item,需要外部Item会依赖此变化，所以以外部回调方式实现
 */
public class SubStreamResolutionItem extends AbsSelectionItem {
    private OnSelectListener mListener;

    public SubStreamResolutionItem(OnSelectListener listener, Context context, String title, String... textList) {
        super(context, true, title, textList);
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
