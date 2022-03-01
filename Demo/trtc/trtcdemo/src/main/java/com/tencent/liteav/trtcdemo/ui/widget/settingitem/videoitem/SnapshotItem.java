package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsTipButtonItem;

public class SnapshotItem extends AbsTipButtonItem {
    private TRTCCloudManager mTRTCCloudManager;

    public SnapshotItem(TRTCCloudManager manager, Context context, String title, String btnText) {
        super(context, false, title, btnText);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onClicked() {
        mTRTCCloudManager.snapshotLocalView();
    }
}
