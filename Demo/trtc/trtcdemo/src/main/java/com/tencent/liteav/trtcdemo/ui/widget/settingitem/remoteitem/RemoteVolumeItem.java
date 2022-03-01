package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;

public class RemoteVolumeItem extends AbsSeekBarItem {

    private VolumeItemListener mListener;

    public RemoteVolumeItem(VolumeItemListener listener, Context context, String title, String tip) {
        super(context, false, title, tip);
        mListener = listener;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSeekBarChange(int index, boolean fromUser) {
        if (mListener != null) {
            mListener.onSeekChange(index, fromUser);
        }
    }

    public interface VolumeItemListener {
        void onSeekChange(int index, boolean fromUser);
    }
}
