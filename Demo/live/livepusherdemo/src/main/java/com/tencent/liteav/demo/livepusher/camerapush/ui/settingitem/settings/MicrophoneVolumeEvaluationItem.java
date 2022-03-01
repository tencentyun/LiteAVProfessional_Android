package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base.AbsSeekBarItem;

public class MicrophoneVolumeEvaluationItem extends AbsSeekBarItem {

    public MicrophoneVolumeEvaluationItem(Context context, String title, String tip) {
        super(context, true, title, tip);
        setMix(0);
        setMax(500);
        setProgress(300);
        setTip(String.valueOf(300));
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSeekBarChange(int process, boolean fromUser) {

    }
}
