package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;
import android.text.InputType;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextItem;

/**
 * TRTC -》 Live 场景 -》 合唱场景时，输入CDN地址
 */
public class CDNAddressItem extends AbsEditTextItem {
    
    public CDNAddressItem(Context context, String title) {
        super(context, true, title);
        setInputType(InputType.TYPE_CLASS_TEXT);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }
}
