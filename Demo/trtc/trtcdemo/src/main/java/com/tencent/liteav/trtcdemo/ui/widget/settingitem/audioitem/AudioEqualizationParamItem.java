package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;

public class AudioEqualizationParamItem extends AbsSeekBarItem {
    private static final int AUDIO_EQUALIZATION_GAIN_MAX = 15;
    private static final int AUDIO_EQUALIZATION_GAIN_MIN = -15;

    public AudioEqualizationParamItem(Context context, String title) {
        super(context, true, title, "");
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setMax(AUDIO_EQUALIZATION_GAIN_MAX);
        setMix(AUDIO_EQUALIZATION_GAIN_MIN);
    }

    @Override
    public void onSeekBarChange(int index, boolean fromUser) {
        setTip(String.valueOf(index));
    }
}
