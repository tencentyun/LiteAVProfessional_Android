package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE;

public class LocalMirrorItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public LocalMirrorItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setSelect(SettingConfigHelper.getInstance().getVideoConfig().getMirrorType());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index) {
        int type;
        if (index == 0) {
            type = TRTC_VIDEO_MIRROR_TYPE_AUTO;
        } else if (index == 1) {
            type = TRTC_VIDEO_MIRROR_TYPE_ENABLE;
        } else {
            type = TRTC_VIDEO_MIRROR_TYPE_DISABLE;
        }
        SettingConfigHelper.getInstance().getVideoConfig().setMirrorType(type);
        mTRTCCloudManager.setLocalViewMirror(SettingConfigHelper.getInstance().getVideoConfig().getMirrorType());
    }
}