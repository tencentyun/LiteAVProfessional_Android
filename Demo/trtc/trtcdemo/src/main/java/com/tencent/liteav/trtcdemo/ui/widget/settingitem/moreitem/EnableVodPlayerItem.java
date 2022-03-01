package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;

public class EnableVodPlayerItem extends AbsSwitchItem {

    private EnableVodPlayerListener mListener;

    public EnableVodPlayerItem(EnableVodPlayerListener listener, Context context, String title) {
        super(context, true, title);
        mListener = listener;
        setCheck(SettingConfigHelper.getInstance().getMoreConfig().isVodPlayerEnabled());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onChecked() {
        if (mListener != null) {
            mListener.onClicked();
        }
    }

    public interface EnableVodPlayerListener {
        void onClicked();
    }
}
