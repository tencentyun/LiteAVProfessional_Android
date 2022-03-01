package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.model.utils.TRTCConstants;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

public class AudioVolumeTypeItem extends AbsRadioButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public AudioVolumeTypeItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        int type = SettingConfigHelper.getInstance().getAudioConfig().getAudioVolumeType();
        setSelect(-1 == type ? 3 : type);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        int volumeType = TRTCConstants.TRTC_SYSTEM_VOLUME_TYPE_NONE;
        if (0 == index) {
            volumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
        } else if (1 == index) {
            volumeType = TRTCCloudDef.TRTCSystemVolumeTypeMedia;
        } else if (2 == index) {
            volumeType = TRTCCloudDef.TRTCSystemVolumeTypeVOIP;
        }
        SettingConfigHelper.getInstance().getAudioConfig().setAudioVolumeType(volumeType);
        mTRTCCloudManager.setSystemVolumeType(volumeType);
    }
}