package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.trtcdemo.model.bean.Constant;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;

public class LocalRenderViewTypeItem extends AbsRadioButtonItem {

    private static final int[] types = new int[]{
            Constant.TRTCViewType.TYPE_GLSURFACE_VIEW,
            Constant.TRTCViewType.TYPE_SURFACE_VIEW,
            Constant.TRTCViewType.TYPE_TEXTURE_VIEW,
            Constant.TRTCViewType.TYPE_CUSTOM_VIEW
    };

    public LocalRenderViewTypeItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(0);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {

    }

    public int getLocalRenderViewType() {
        return types[getSelect()];
    }

    @Override
    public void setVisibility(int visibility) {
        if (!AppRuntime.get().isDebug() && View.VISIBLE == visibility) {
            return;
        }
        super.setVisibility(visibility);
    }
}