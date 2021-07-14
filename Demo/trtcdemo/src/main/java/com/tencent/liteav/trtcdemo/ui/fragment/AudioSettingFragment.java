package com.tencent.liteav.trtcdemo.ui.fragment;

import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.RadioButtonSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SeekBarSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SwitchSettingItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频Tab Fragment页
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class AudioSettingFragment extends BaseSettingFragment {
    private LinearLayout            mLLItemList;
    private List<BaseSettingItem>   mSettingItemList;

    private RadioButtonSettingItem  mAudioVolumeTypeItem;
    private SwitchSettingItem       mAGCItem;
    private RadioButtonSettingItem  mANSItem;
    private RadioButtonSettingItem  mAECItem;
    private SwitchSettingItem       mAudioEarMonitoringItem;
    private SwitchSettingItem       mAudioEarpieceModeItem;
    private SwitchSettingItem       mAudioVolumeEvaluationItem;
    private SeekBarSettingItem      mCaptureVolumeItem;
    private SeekBarSettingItem      mPlayVolumeItem;
    private AudioConfig             mAudioConfig;
    private static final int        AUDIO_VOLUME_MAX = 100;

    @Override
    protected void initView(View itemView) {
        mLLItemList = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mAudioConfig = SettingConfigHelper.getInstance().getAudioConfig();

        mAudioVolumeTypeItem = new RadioButtonSettingItem(getContext(),
                new BaseSettingItem.ItemText(getString(R.string.trtcdemo_volumn_type),
                        getString(R.string.trtcdemo_volumn_type_auto),
                        getString(R.string.trtcdemo_volumn_type_media),
                        getString(R.string.trtcdemo_volumn_type_voip)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        int vIndex = mAudioVolumeTypeItem.getSelected();
                        int volumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
                        if(0 == vIndex){
                            volumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
                        }else if(1 == vIndex){
                            volumeType = TRTCCloudDef.TRTCSystemVolumeTypeMedia;
                        }else if(2 == vIndex){
                            volumeType = TRTCCloudDef.TRTCSystemVolumeTypeVOIP;
                        }
                        mAudioConfig.setAudioVolumeType(volumeType);
                        mTRTCCloudManager.setSystemVolumeType(mAudioConfig.getAudioVolumeType());
                    }
                });
        mSettingItemList.add(mAudioVolumeTypeItem);

        mANSItem = new RadioButtonSettingItem(getContext(),
                new BaseSettingItem.ItemText(getString(R.string.trtcdemo_ans), getString(R.string.trtcdemo_close),
                        getString(R.string.trtcdemo_low), getString(R.string.trtcdemo_middle), getString(R.string.trtcdemo_high)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        switch (index) {
                            case 0:
                                mTRTCCloudManager.enableANS(0);
                                break;
                            case 1:
                                mTRTCCloudManager.enableANS(20);
                                break;
                            case 2:
                                mTRTCCloudManager.enableANS(40);
                                break;
                            case 3:
                                mTRTCCloudManager.enableANS(100);
                                break;
                            default:
                                break;
                        }
                        mAudioConfig.setANS(index);
                    }
                });
        mSettingItemList.add(mANSItem);

        mAECItem = new RadioButtonSettingItem(getContext(),
                new BaseSettingItem.ItemText(getString(R.string.trtcdemo_aec), getString(R.string.trtcdemo_close),
                        getString(R.string.trtcdemo_middle), getString(R.string.trtcdemo_high)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        switch (index) {
                            case 0:
                                mTRTCCloudManager.enableAEC(0);
                                break;
                            case 1:
                                mTRTCCloudManager.enableAEC(60);
                                break;
                            case 2:
                                mTRTCCloudManager.enableAEC(100);
                                break;
                            default:
                                break;
                        }
                        mAudioConfig.setAEC(index);
                    }
                });
        mSettingItemList.add(mAECItem);

        mCaptureVolumeItem = new SeekBarSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_capture_volume), ""), new SeekBarSettingItem.Listener() {
            @Override
            public void onSeekBarChange(int progress, boolean fromUser) {
                mAudioConfig.setRecordVolume(progress);
                mTRTCCloudManager.setRecordVolume(progress);
                mCaptureVolumeItem.setTips(progress + "");
            }
        }).setProgress(mAudioConfig.getRecordVolume()).setMax(AUDIO_VOLUME_MAX).setTips(mAudioConfig.getRecordVolume() + "");
        mSettingItemList.add(mCaptureVolumeItem);

        mPlayVolumeItem = new SeekBarSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_play_volume), ""), new SeekBarSettingItem.Listener() {
            @Override
            public void onSeekBarChange(int progress, boolean fromUser) {
                mAudioConfig.setPlayoutVolume(progress);
                mTRTCCloudManager.setPlayoutVolume(progress);
                mPlayVolumeItem.setTips(progress + "");
            }
        }).setProgress(mAudioConfig.getPlayoutVolume()).setMax(AUDIO_VOLUME_MAX).setTips(mAudioConfig.getPlayoutVolume() + "");
        mSettingItemList.add(mPlayVolumeItem);

        //耳返设置入口
        mAudioEarMonitoringItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_auricular_regurgitation), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mAudioConfig.setEnableEarMonitoring(mAudioEarMonitoringItem.getChecked());
                        mTRTCCloudManager.enableEarMonitoring(mAudioEarMonitoringItem.getChecked());
                    }
                });
        mSettingItemList.add(mAudioEarMonitoringItem);

        mAudioEarpieceModeItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_receiver_mode), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mAudioConfig.setAudioEarpieceMode(mAudioEarpieceModeItem.getChecked());
                        if (mAudioConfig.isAudioEarpieceMode()) {
                            mTRTCCloudManager.enableAudioHandFree(true);
                        } else {
                            mTRTCCloudManager.enableAudioHandFree(false);
                        }
                    }
                });
        mSettingItemList.add(mAudioEarpieceModeItem);

        mAGCItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_automatic_gain), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mAudioConfig.setAGC(mAGCItem.getChecked());
                        mTRTCCloudManager.enableAGC(mAudioConfig.isAGC() ? 100 : 0);
                    }
                });
        mSettingItemList.add(mAGCItem);

        mAudioVolumeEvaluationItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_volume_tips), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mAudioConfig.setAudioVolumeEvaluation(mAudioVolumeEvaluationItem.getChecked());
                        mTRTCCloudManager.enableAudioVolumeEvaluation(mAudioConfig.isAudioVolumeEvaluation());
                    }
                });
        mSettingItemList.add(mAudioVolumeEvaluationItem);

        updateItem();

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(8), 0, 0);
            mLLItemList.addView(view);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAudioConfig.saveCache();
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
