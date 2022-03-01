package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

public class CustomVideoPreprocessPixelFormatItem extends AbsRadioButtonItem {

    private static final int[] pixelFormats = new int[]{
            TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_I420,
            TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_NV21,
    };

    public CustomVideoPreprocessPixelFormatItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(0);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {

    }

    public int getCustomVideoPreprocessPixelFormat() {
        return pixelFormats[getSelect()];
    }

    @Override
    public void setVisibility(int visibility) {
        if (!AppRuntime.get().isDebug() && View.VISIBLE == visibility) {
            return;
        }
        super.setVisibility(visibility);
    }
}