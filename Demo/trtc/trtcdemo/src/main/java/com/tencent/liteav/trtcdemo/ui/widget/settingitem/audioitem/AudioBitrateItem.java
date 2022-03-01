package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSelectionItem;

public class AudioBitrateItem extends AbsSelectionItem {
    private TRTCCloudManager mTRTCCloudManager;

    public AudioBitrateItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, true, title, textList);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        int bitrate = SettingConfigHelper.getInstance().getAudioConfig().getAudioBitrate();
        setSelect(getBitratePos(bitrate));
    }

    @Override
    public void onSelected(int index, String str) {
        int bitrate = getBitrate(index);
        mTRTCCloudManager.setAudioQualityEx(bitrate);
        SettingConfigHelper.getInstance().getAudioConfig().setAudioBitrate(bitrate);
    }

    public static int getBitrate(int pos) {
        switch (pos) {
            case 0:
                return 16;
            case 1:
                return 20;
            case 2:
                return 32;
            case 3:
                return 50;
            case 4:
                return 64;
            case 5:
                return 96;
            case 6:
                return 128;
            default:
                return 50;
        }
    }

    public static int getBitratePos(int bitrate) {
        switch (bitrate) {
            case 16:
                return 0;
            case 20:
                return 1;
            case 32:
                return 2;
            case 50:
                return 3;
            case 64:
                return 4;
            case 96:
                return 5;
            case 128:
                return 6;
            default:
                return 3;
        }
    }
}
