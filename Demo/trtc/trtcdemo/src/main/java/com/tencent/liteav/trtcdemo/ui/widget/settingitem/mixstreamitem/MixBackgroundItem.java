package com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class MixBackgroundItem extends AbsRadioButtonItem {

    /**
     * 混合后画面的背景图ID列表，""表示无背景图。
     * 【特别说明】背景图需要您事先在 “控制台 => 应用管理 => 功能配置 => 素材管理” 中上传，上传成功后可以获得对应的"图片ID"。
     */
    private static final String[] BACKGROUND_IMAGE = {"", "51", "52"};

    public MixBackgroundItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(indexOfValue(SettingConfigHelper.getInstance().getVideoConfig().getMixStreamBackgroundImage()));
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        videoConfig.setMixStreamBackgroundImage(BACKGROUND_IMAGE[index]);
    }

    private int indexOfValue(String value) {
        for (int i = 0; i < BACKGROUND_IMAGE.length; i++) {
            if (BACKGROUND_IMAGE[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }
}