package com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextSendItem;

import java.io.File;

public class OtherRecordItem extends AbsEditTextSendItem {

    private TRTCCloudManager mTRTCCloudManager;

    public OtherRecordItem(TRTCCloudManager manager, Context context, String title, String btnText) {
        super(context, true, title, btnText);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
        setInputText(getRecordFileName());
    }

    @Override
    public void send(String msg) {
        if (SettingConfigHelper.getInstance().getMoreConfig().isRecording()) {
            mTRTCCloudManager.stopLocalRecording();
            SettingConfigHelper.getInstance().getMoreConfig().setRecording(false);
            setButtonText(getContext().getString(R.string.trtcdemo_start_record));
        } else {
            mTRTCCloudManager.startLocalRecording(SettingConfigHelper.getInstance().getMoreConfig().getRecordType(), msg);
            SettingConfigHelper.getInstance().getMoreConfig().setRecording(true);
            setButtonText(getContext().getString(R.string.trtcdemo_stop_record));
        }
    }

    private String getRecordFileName() {
        File sdcardDir = getContext().getExternalFilesDir(null);
        if (sdcardDir == null) {
            return "/sdcard/record.mp4";
        }
        String dirPath = sdcardDir.getAbsolutePath() + "/test/record/record.mp4";
        return dirPath;
    }
}