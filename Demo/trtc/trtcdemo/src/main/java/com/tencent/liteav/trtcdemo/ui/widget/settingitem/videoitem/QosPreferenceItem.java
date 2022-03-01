package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

public class QosPreferenceItem extends AbsRadioButtonItem {

    private TRTCCloudManager mTRTCCloudManager;

    public QosPreferenceItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, false, title, textList);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setSelect(SettingConfigHelper.getInstance().getVideoConfig().getQosPreference() == TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_SMOOTH ? 0 : 1);
    }

    @Override
    public void onSelected(int index) {
        SettingConfigHelper.getInstance().getVideoConfig().setQosPreference(index == 0 ?
                TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_SMOOTH : TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_CLEAR);
        mTRTCCloudManager.setQosParam();
    }
}
