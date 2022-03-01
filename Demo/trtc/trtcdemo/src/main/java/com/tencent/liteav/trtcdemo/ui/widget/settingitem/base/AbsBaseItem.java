package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import android.widget.LinearLayout;

import com.tencent.liteav.demo.common.AppRuntime;

public abstract class AbsBaseItem extends LinearLayout {
    public boolean mDebugFlag;

    public AbsBaseItem(Context context, boolean debugFlag) {
        super(context);
        mDebugFlag = debugFlag;

        if (!AppRuntime.get().isDebug() && mDebugFlag) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }
}
