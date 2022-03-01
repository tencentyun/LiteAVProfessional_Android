package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_180;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_270;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_90;

public class RemoteRotationItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public RemoteRotationItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setSelect(indexOfRotation(SettingConfigHelper.getInstance().getVideoConfig().getRemoteRotation()));
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index) {
        if (SettingConfigHelper.getInstance().getMoreConfig().isEnableGSensorMode()) {
            SettingConfigHelper.getInstance().getMoreConfig().setEnableGSensorMode(false);
            mTRTCCloudManager.enableGSensor(false);
            ToastUtils.showShort(R.string.trtcdemo_close_gravity_inuction);
        }
        int rotation = TRTC_VIDEO_ROTATION_0;
        if (index == 1) {
            rotation = TRTC_VIDEO_ROTATION_90;
        } else if (index == 2) {
            rotation = TRTC_VIDEO_ROTATION_180;
        } else if (index == 3) {
            rotation = TRTC_VIDEO_ROTATION_270;
        }
        SettingConfigHelper.getInstance().getVideoConfig().setRemoteRotation(rotation);
        mTRTCCloudManager.setVideoEncoderRotation(rotation);
    }

    private int indexOfRotation(int rotation) {
        switch (rotation) {
            case TRTC_VIDEO_ROTATION_0:
                return 0;
            case TRTC_VIDEO_ROTATION_90:
                return 1;
            case TRTC_VIDEO_ROTATION_180:
                return 2;
            case TRTC_VIDEO_ROTATION_270:
                return 3;
        }
        return 0;
    }
}
