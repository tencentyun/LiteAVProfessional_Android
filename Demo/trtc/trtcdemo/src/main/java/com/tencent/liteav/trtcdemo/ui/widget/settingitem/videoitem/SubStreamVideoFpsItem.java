package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSelectionItem;

/**
 * 主路帧率设置Item
 */
public class SubStreamVideoFpsItem extends AbsSelectionItem {
    private TRTCCloudManager mTRTCCloudManager;

    public SubStreamVideoFpsItem(TRTCCloudManager manager, Context context, String title, String... textList) {
        super(context, true, title, textList);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(int index, String str) {
        int fps = getFps(index);
        if (fps != SettingConfigHelper.getInstance().getVideoConfig().getSubStreamVideoFps()) {
            SettingConfigHelper.getInstance().getVideoConfig().setSubStreamVideoFps(fps);
            mTRTCCloudManager.setTRTCCloudParam();
        }
    }

    public static int getFps(int pos) {
        switch (pos) {
            case 0:
                return 10;
            case 1:
                return 15;
            case 2:
                return 20;
            default:
                return 15;
        }
    }

    public static int getFpsPos(int fps) {
        switch (fps) {
            case 10:
                return 0;
            case 15:
                return 1;
            case 20:
                return 2;
            default:
                return 0;
        }
    }
}
