package com.tencent.liteav.trtcdemo.ui.fragment;

import android.view.View;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AECItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AGCItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.ANSItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioBitrateItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioCaptureItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioCustomRenderItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioEarMonitoringItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioEarpieceModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioVolumeEvaluationItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioVolumeTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.CaptureVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.PlayVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.RecordContentItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.RecordItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.VoicePitchItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioParallelMaxCountItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsTipButtonItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频Tab Fragment页
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class AudioSettingFragment extends BaseSettingFragment {
    private LinearLayout mLLItemList;
    private List<View>   mSettingItemList;

    private AbsRadioButtonItem        mAudioVolumeTypeItem;
    private AbsSwitchItem             mAGCItem;
    private AbsRadioButtonItem        mANSItem;
    private AbsRadioButtonItem        mAECItem;
    private AbsSwitchItem             mAudioCaptureItem;
    private AbsSwitchItem             mAudioEarMonitoringItem;
    private AbsSwitchItem             mAudioEarpieceModeItem;
    private AbsSwitchItem             mAudioVolumeEvaluationItem;
    private AbsRadioButtonItem        mRecordContentItem;
    private AbsTipButtonItem          mRecordItem;
    private AbsSeekBarItem            mCaptureVolumeItem;
    private AbsSeekBarItem            mPlayVolumeItem;
    private AbsSeekBarItem            mVoicePitchItem;
    private AudioConfig               mAudioConfig;
    private AudioBitrateItem          mAudioBitrateItem;
    private AudioParallelMaxCountItem mAudioParallelMaxCountItem;
    private AudioCustomRenderItem     mAudioCustomRenderItem;

    @Override
    protected void initView(View itemView) {
        mLLItemList = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mAudioConfig = SettingConfigHelper.getInstance().getAudioConfig();

        mAudioVolumeTypeItem = new AudioVolumeTypeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_volumn_type),
                getString(R.string.trtcdemo_volumn_type_auto),
                getString(R.string.trtcdemo_volumn_type_media),
                getString(R.string.trtcdemo_volumn_type_voip),
                getString(R.string.trtcdemo_no_choice));
        mSettingItemList.add(mAudioVolumeTypeItem);

        mANSItem = new ANSItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_ans),
                getString(R.string.trtcdemo_close),
                getString(R.string.trtcdemo_low),
                getString(R.string.trtcdemo_middle),
                getString(R.string.trtcdemo_high),
                getString(R.string.trtcdemo_ai));
        mSettingItemList.add(mANSItem);

        mAECItem = new AECItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_aec),
                getString(R.string.trtcdemo_close),
                getString(R.string.trtcdemo_middle),
                getString(R.string.trtcdemo_high));
        mSettingItemList.add(mAECItem);

        // 声音采集
        mAudioCaptureItem = new AudioCaptureItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_audio_capture));
        mSettingItemList.add(mAudioCaptureItem);

        mCaptureVolumeItem = new CaptureVolumeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_capture_volume));
        mSettingItemList.add(mCaptureVolumeItem);

        mPlayVolumeItem = new PlayVolumeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_play_volume));
        mSettingItemList.add(mPlayVolumeItem);

        mVoicePitchItem = new VoicePitchItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_voice_pitch));
        mSettingItemList.add(mVoicePitchItem);

        //耳返设置入口
        mAudioEarMonitoringItem = new AudioEarMonitoringItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_auricular_regurgitation));
        mSettingItemList.add(mAudioEarMonitoringItem);

        mAudioEarpieceModeItem = new AudioEarpieceModeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_receiver_mode));
        mSettingItemList.add(mAudioEarpieceModeItem);

        mAGCItem = new AGCItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_automatic_gain));
        mSettingItemList.add(mAGCItem);

        mAudioVolumeEvaluationItem = new AudioVolumeEvaluationItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_volume_tips));
        mSettingItemList.add(mAudioVolumeEvaluationItem);

        mRecordContentItem = new RecordContentItem(getContext(), getString(R.string.trtcdemo_record_content), getString(R.string.trtcdemo_record_all), getString(R.string.trtcdemo_record_local), getString(R.string.trtcdemo_record_remote));
        mSettingItemList.add(mRecordContentItem);

        // 音频录制
        mRecordItem = new RecordItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_audio_record), getString(R.string.trtcdemo_start_record), getString(R.string.trtcdemo_stop_record));
        mSettingItemList.add(mRecordItem);

        mAudioBitrateItem = new AudioBitrateItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_audio_bits), getResources().getStringArray(R.array.trtcdemo_audio_bits));
        mSettingItemList.add(mAudioBitrateItem);

        mAudioParallelMaxCountItem = new AudioParallelMaxCountItem(mTRTCCloudManager, getContext(),
                getString(R.string.trtcdemo_audio_parallel_max_count), getString(R.string.trtcdemo_set));
        mSettingItemList.add(mAudioParallelMaxCountItem);

        mAudioCustomRenderItem = new AudioCustomRenderItem(mTRTCCloudManager, getContext(),
                getString(R.string.trtcdemo_audio_custom_render_title));
        mSettingItemList.add(mAudioCustomRenderItem);

        updateItem();

        // 将这些view添加到对应的容器中
        for (View view : mSettingItemList) {
            mLLItemList.addView(view);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAudioConfig.saveCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAudioCustomRenderItem != null) {
            mAudioCustomRenderItem.destroy();
        }
    }

    private void updateItem() {
        mAudioVolumeTypeItem.setSelect(mTRTCCloudManager.mVolumeType);
        mAGCItem.setCheck(mAudioConfig.isAGC());
        mANSItem.setSelect(mAudioConfig.getANS());
        mAECItem.setSelect(mAudioConfig.getAEC());
        mAudioEarMonitoringItem.setCheck(mAudioConfig.isEnableEarMonitoring());
        mAudioEarpieceModeItem.setCheck(mAudioConfig.isAudioEarpieceMode());
        mAudioVolumeEvaluationItem.setCheck(mAudioConfig.isAudioVolumeEvaluation());
        mTRTCCloudManager.enableAudioVolumeEvaluation(mAudioConfig.isAudioVolumeEvaluation());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }
}
