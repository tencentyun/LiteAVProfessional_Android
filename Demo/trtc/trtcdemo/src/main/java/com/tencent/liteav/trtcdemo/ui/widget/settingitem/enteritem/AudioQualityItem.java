package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.utils.TRTCConstants;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_QUALITY;

public class AudioQualityItem extends AbsRadioButtonItem {

    public AudioQualityItem(Context context, String title, String... textList) {
        super(context, false, title, textList);
        setSelect(1);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {

    }

    public int getType() {
        int index = getSelect();
        if (0 == index) {
            return TRTCCloudDef.TRTC_AUDIO_QUALITY_MUSIC;
        } else if (1 == index) {
            return TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT;
        } else if (2 == index) {
            return TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH;
        } else {
            return TRTCConstants.TRTC_AUDIO_QUALITY_NONE;
        }
    }
}