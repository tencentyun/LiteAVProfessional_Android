package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextSendItem;

public class AudioParallelMaxCountItem extends AbsEditTextSendItem {

    private TRTCCloudManager mTRTCCloudManager;

    public AudioParallelMaxCountItem(TRTCCloudManager manager, Context context, String title, String btnText) {
        super(context, true, title, btnText);
        mTRTCCloudManager = manager;
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    @Override
    public void send(String msg) {
        try {
            mTRTCCloudManager.setRemoteAudioParallelParams(Integer.parseInt(msg));
            ToastUtils.showLong(R.string.trtcdemo_audio_parallel_max_count_success_tips);
        } catch (Exception e) {
            ToastUtils.showLong(R.string.trtcdemo_audio_parallel_max_count_fail_tips);
        }
    }

}
