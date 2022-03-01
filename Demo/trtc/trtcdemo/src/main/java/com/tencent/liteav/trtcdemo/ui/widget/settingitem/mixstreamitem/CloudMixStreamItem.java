package com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

public class CloudMixStreamItem extends AbsRadioButtonItem {

    private static final int[] MIXTURE_MODE = {
            TRTCCloudDef.TRTC_TranscodingConfigMode_Unknown,
            TRTCCloudDef.TRTC_TranscodingConfigMode_Manual,
            TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PureAudio,
            TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PresetLayout
    };

    public CloudMixStreamItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(indexOfValue(SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode()));
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        videoConfig.setCloudMixtureMode(MIXTURE_MODE[index]);
    }

    private int indexOfValue(int value) {
        for (int i = 0; i < MIXTURE_MODE.length; i++) {
            if (MIXTURE_MODE[i] == value) {
                return i;
            }
        }
        return 0;
    }
}