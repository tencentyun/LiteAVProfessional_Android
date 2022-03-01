package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.utils.TRTCConstants;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

public class AudioVolumeTypeItem extends AbsRadioButtonItem {

    private int mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;

    public AudioVolumeTypeItem(Context context, String title, String... textList) {
        super(context, false, title, textList);
        setSelect(0);
        onSelected(0);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        if (0 == index) {
            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
        } else if (1 == index) {
            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeMedia;
        } else if (2 == index) {
            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeVOIP;
        } else {
            mAudioVolumeType = TRTCConstants.TRTC_SYSTEM_VOLUME_TYPE_NONE;
        }
        SettingConfigHelper.getInstance().getAudioConfig().setAudioVolumeType(mAudioVolumeType);
    }

    public int getAudioVolumeType() {
        return mAudioVolumeType;
    }
}