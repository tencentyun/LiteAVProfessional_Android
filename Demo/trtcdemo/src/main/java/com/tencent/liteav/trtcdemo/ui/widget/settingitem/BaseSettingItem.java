package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 抽象所有SettingItem的基类
 *
 * - 返回Item的View对象{@link BaseSettingItem#getView()}
 */
public abstract class BaseSettingItem {
    protected Context        mContext;
    protected LayoutInflater mInflater;
    protected ItemText       mItemText;
    protected int            mPaddingTop;

    public BaseSettingItem(Context context,
                           @NonNull ItemText itemText) {
        mContext = context;
        mItemText = itemText;
        mInflater = LayoutInflater.from(context);
        mPaddingTop = SizeUtils.dp2px(10);
    }

    public abstract View getView();

    public static class ItemText {
        public String       title;
        public List<String> contentText;

        public ItemText(String title, String... textList) {
            this.title = title;
            this.contentText = Arrays.asList(textList);
        }

        public ItemText(String title, List<String> textList) {
            this.title = title;
            this.contentText = textList;
        }
    }

    public int getPaddingTop() {
        return mPaddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.mPaddingTop = paddingTop;
    }
}
