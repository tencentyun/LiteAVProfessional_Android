package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.trtc.TRTCCloudDef;

public class RecordContentItem extends AbsRadioButtonItem {

    private static final int RECORD_CONTENT[] = {
            TRTCCloudDef.TRTC_AudioRecordingContent_All,
            TRTCCloudDef.TRTC_AudioRecordingContent_Local,
            TRTCCloudDef.TRTC_AudioRecordingContent_Remote
    };

    public RecordContentItem(Context context, String title, String... textList) {
        super(context, true, title, textList);
        setSelect(SettingConfigHelper.getInstance().getAudioConfig().getRecordType());
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void onSelected(final int index) {
        SettingConfigHelper.getInstance().getAudioConfig().setRecordType(index);
    }

    public int getRecordContent() {
        return RECORD_CONTENT[getSelect()];
    }
}