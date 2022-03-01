package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSelectionItem;

/**
 * TRTC-视频文件推流时-设置自定义渲染格式
 */
public class CustomRenderConfigItem extends AbsSelectionItem {
    
    public CustomRenderConfigItem(Context context, boolean debug, String title, String... textList) {
        super(context, debug, title, textList);
        setSelect(0);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }
    
    @Override
    public void onSelected(int index, String str) {
    
    }
}
