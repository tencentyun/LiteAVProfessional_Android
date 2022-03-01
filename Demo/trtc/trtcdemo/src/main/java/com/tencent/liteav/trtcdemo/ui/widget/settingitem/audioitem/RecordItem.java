package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsTipButtonItem;

/**
 * 开始/结束录制
 */
public class RecordItem extends AbsTipButtonItem {
    private TRTCCloudManager mTRTCCloudManager;
    private String           mStartText;
    private String           mStopText;

    public RecordItem(TRTCCloudManager manager, Context context, String title, String startText, String stopText) {
        super(context, true, title, startText);
        mTRTCCloudManager = manager;
        mStartText = startText;
        mStopText = stopText;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        if (SettingConfigHelper.getInstance().getAudioConfig().isRecording()) {
            setButtonText(mStopText);
        }
    }

    @Override
    public void onClicked() {
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        // 这里开始录制进行操作
        if (audioConfig.isRecording()) {
            mTRTCCloudManager.stopRecord();
            setButtonText(mStartText);
        } else {
            int type = audioConfig.getRecordType();
            if (mTRTCCloudManager.startRecord(type)) {
                setButtonText(mStopText);
            }
        }
    }
}
