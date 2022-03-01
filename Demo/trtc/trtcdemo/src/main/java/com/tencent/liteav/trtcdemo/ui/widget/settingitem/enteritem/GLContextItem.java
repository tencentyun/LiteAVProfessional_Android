package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

/**
 * TRTC-视频文件推流时-是否自定义 OpenGLContext（不选则使用 SDK）
 */
public class GLContextItem extends AbsSwitchItem {
    
    public GLContextItem(Context context, boolean debug, String title) {
        super(context, debug, title);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }
    
    @Override
    public void onChecked() {
    
    }
}
