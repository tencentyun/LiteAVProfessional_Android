package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

import static com.tencent.trtc.TRTCCloudDef.VIDEO_QOS_CONTROL_CLIENT;
import static com.tencent.trtc.TRTCCloudDef.VIDEO_QOS_CONTROL_SERVER;

public class OtherQosModeItem extends AbsRadioButtonItem {
    
    public OtherQosModeItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(SettingConfigHelper.getInstance().getMoreConfig().getQosMode() == VIDEO_QOS_CONTROL_SERVER ? 1 : 0);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }
    
    @Override
    public void onSelected(int index) {
        final int mode = index == 0 ? VIDEO_QOS_CONTROL_CLIENT : VIDEO_QOS_CONTROL_SERVER;
        SettingConfigHelper.getInstance().getMoreConfig().setQosMode(mode);
    }
}
