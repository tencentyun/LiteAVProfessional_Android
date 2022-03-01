package com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.fragment.VideoSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;

import java.util.ArrayList;

public class MainStreamBitrateItem extends AbsSeekBarItem {

    private static final int DEFAULT_MIN_BIT_RATE  = 300;
    private static final int DEFAULT_MAX_BIT_RATE  = 1000;
    private static final int DEFAULT_BIT_RATE      = 400;
    private static final int DEFAULT_BIT_RATE_STEP = 10;

    private TRTCCloudManager                                        mTRTCCloudManager;
    private int                                                     mCurrentSolutionIndex;
    private ArrayList<VideoSettingFragment.TRTCSettingBitrateTable> mParamArray;

    public MainStreamBitrateItem(int index, TRTCCloudManager manager, ArrayList<VideoSettingFragment.TRTCSettingBitrateTable> array, Context context, String title, String tip) {
        super(context, false, title, tip);
        mCurrentSolutionIndex = index;
        mTRTCCloudManager = manager;
        mParamArray = array;
        updateSolution(mCurrentSolutionIndex);
        setProgress(getBitrateProgress(SettingConfigHelper.getInstance().getVideoConfig().getMainStreamVideoBitrate(), mCurrentSolutionIndex));
        setTip(getBitrate(SettingConfigHelper.getInstance().getVideoConfig().getMainStreamVideoBitrate(), mCurrentSolutionIndex) + "kbps");
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSeekBarChange(int index, boolean fromUser) {
        int bitrate = getBitrate(index, mCurrentSolutionIndex);
        setTip(bitrate + "kbps");
        if (bitrate != SettingConfigHelper.getInstance().getVideoConfig().getMainStreamVideoBitrate()) {
            SettingConfigHelper.getInstance().getVideoConfig().setMainStreamVideoBitrate(bitrate);
            mTRTCCloudManager.setTRTCCloudParam();
        }
    }

    public int getBitrate(int progress, int pos) {
        int minBitrate = getMinBitrate(pos);
        int maxBitrate = getMaxBitrate(pos);
        int stepBitrate = getStepBitrate(pos);
        int bit = (progress * stepBitrate) + minBitrate;
        return bit;
    }

    public int getMinBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).minBitrate;
        }
        return DEFAULT_MIN_BIT_RATE;
    }

    public int getMaxBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).maxBitrate;
        }
        return DEFAULT_MAX_BIT_RATE;
    }

    public int getDefBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).defaultBitrate;
        }
        return DEFAULT_BIT_RATE;
    }

    private int getStepBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).step;
        }
        return DEFAULT_BIT_RATE_STEP;
    }

    public void updateSolution(int pos) {
        int minBitrate = getMinBitrate(pos);
        int maxBitrate = getMaxBitrate(pos);

        int stepBitrate = getStepBitrate(pos);
        int max = (maxBitrate - minBitrate) / stepBitrate;
        if (getMax() != max) {    // 有变更时设置默认值
            setMax(max);
            int defBitrate = getDefBitrate(pos);
            setProgress(getBitrateProgress(defBitrate, pos));
        } else {
            setMax(max);
        }
    }

    private int getBitrateProgress(int bitrate, int pos) {
        int minBitrate = getMinBitrate(pos);
        int stepBitrate = getStepBitrate(pos);

        int progress = (bitrate - minBitrate) / stepBitrate;
        return progress;
    }

    public void setCurrentSolutionIndex(int currentSolutionIndex) {
        this.mCurrentSolutionIndex = currentSolutionIndex;
    }
}
