package com.tencent.liteav.demo.liveplayer.ui.model;

import com.tencent.liteav.demo.liveplayer.ui.view.SettingItemView;
import com.tencent.liteav.demo.liveplayer.ui.view.SettingItemView.OnEventListener;

public class SettingItem {

    private int mType;
    private String mTitle;
    private String mButtonText;
    private boolean mChecked;
    private int mProgressMax;
    private int mProgress;
    private String[] mRadioData;
    private int mRadioDefaultIndex;
    private SettingItemView.OnEventListener mListener;

    public SettingItem(String title, boolean checked, SettingItemView.OnEventListener listener) {
        this.mTitle = title;
        this.mChecked = checked;
        this.mListener = listener;
        mType = SettingItemView.TYPE_SWITCH;
    }

    public SettingItem(String title, String buttonText, SettingItemView.OnEventListener listener) {
        this.mTitle = title;
        this.mButtonText = buttonText;
        this.mListener = listener;
        mType = SettingItemView.TYPE_BUTTON;
    }

    public SettingItem(String title, int progressMax, int progress, SettingItemView.OnEventListener listener) {
        this.mTitle = title;
        this.mProgressMax = progressMax;
        this.mProgress = progress;
        this.mListener = listener;
        mType = SettingItemView.TYPE_PROGRESS;
    }

    public SettingItem(String title, String[] radioData, int radioDefaultIndex,
            SettingItemView.OnEventListener listener) {
        mTitle = title;
        mRadioData = radioData;
        mRadioDefaultIndex = radioDefaultIndex;
        this.mListener = listener;
        mType = SettingItemView.TYPE_RADIO;
    }

    public int getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getButtonText() {
        return mButtonText;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public int getProgressMax() {
        return mProgressMax;
    }

    public int getProgress() {
        return mProgress;
    }

    public String[] getRadioData() {
        return mRadioData;
    }

    public int getRadioDefaultIndex() {
        return mRadioDefaultIndex;
    }

    public OnEventListener getListener() {
        return mListener;
    }
}
