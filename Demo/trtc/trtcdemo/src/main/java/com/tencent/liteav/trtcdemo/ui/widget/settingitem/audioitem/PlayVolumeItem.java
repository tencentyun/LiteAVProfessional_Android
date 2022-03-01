package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;

public class PlayVolumeItem extends AbsSeekBarItem {
    //为满足测试场景，音量最大支持设置150
    private static final int              AUDIO_VOLUME_MAX = 150;
    private              TRTCCloudManager mTRTCCloudManager;
    
    public PlayVolumeItem(TRTCCloudManager manager, Context context, String title) {
        super(context, false, title, "");
        mTRTCCloudManager = manager;
        setProgress(SettingConfigHelper.getInstance().getAudioConfig().getPlayoutVolume());
        setMax(AUDIO_VOLUME_MAX);
        setTip(SettingConfigHelper.getInstance().getAudioConfig().getPlayoutVolume() + "");
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }
    
    @Override
    public void onSeekBarChange(int index, boolean fromUser) {
        SettingConfigHelper.getInstance().getAudioConfig().setPlayoutVolume(index);
        mTRTCCloudManager.setPlayoutVolume(index);
        setTip(index + "");
    }
}
