package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

/**
 * 远程用户-观看类型设置（大流、小流）
 */
public class RemoteStreamTypeItem extends AbsRadioButtonItem {
    
    private StreamTypeItemListener mListener;
    
    public RemoteStreamTypeItem(StreamTypeItemListener listener, Context context, String title, String... textList) {
        super(context, true, title, textList);
        mListener = listener;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }
    
    @Override
    public void onSelected(int index) {
        if (mListener != null) {
            mListener.onSelected(index);
        }
    }
    
    public interface StreamTypeItemListener {
        void onSelected(int index);
    }
}
