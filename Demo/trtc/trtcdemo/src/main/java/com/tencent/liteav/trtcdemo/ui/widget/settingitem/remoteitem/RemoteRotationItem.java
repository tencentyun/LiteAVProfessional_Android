package com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_180;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_270;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_90;

public class RemoteRotationItem extends AbsRadioButtonItem {

    private RotationListener mListener;

    public RemoteRotationItem(RotationListener listener, Context context, String title, String... textList) {
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

    public static int getRotationByIndex(int index) {
        int rotation = TRTC_VIDEO_ROTATION_0;
        if (index == 1) {
            rotation = TRTC_VIDEO_ROTATION_90;
        } else if (index == 2) {
            rotation = TRTC_VIDEO_ROTATION_180;
        } else if (index == 3) {
            rotation = TRTC_VIDEO_ROTATION_270;
        }
        return rotation;
    }

    public interface RotationListener {
        void onSelected(int index);
    }
}
