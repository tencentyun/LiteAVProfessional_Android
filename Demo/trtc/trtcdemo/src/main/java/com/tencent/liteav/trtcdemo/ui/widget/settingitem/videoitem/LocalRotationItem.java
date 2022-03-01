package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_180;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_270;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_90;

public class LocalRotationItem extends AbsRadioButtonItem {

    private TRTCCloudManager mTRTCCloudManager;

    public LocalRotationItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setSelect(SettingConfigHelper.getInstance().getVideoConfig().getLocalRotation());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index) {
        int rotation = TRTC_VIDEO_ROTATION_0;
        if (index == 1) {
            rotation = TRTC_VIDEO_ROTATION_90;
        } else if (index == 2) {
            rotation = TRTC_VIDEO_ROTATION_180;
        } else if (index == 3) {
            rotation = TRTC_VIDEO_ROTATION_270;
        }
        SettingConfigHelper.getInstance().getVideoConfig().setLocalRotation(rotation);
        mTRTCCloudManager.setLocalVideoRotation(SettingConfigHelper.getInstance().getVideoConfig().getLocalRotation());
    }
}
