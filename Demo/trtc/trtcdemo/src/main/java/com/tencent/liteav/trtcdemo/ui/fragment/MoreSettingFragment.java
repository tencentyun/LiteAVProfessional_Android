package com.tencent.liteav.trtcdemo.ui.fragment;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.MoreConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.AudioMsgItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.CustomMsgItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.EnableFlashItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.EnableVodPlayerItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.GSensorModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.NetworkAudioMsgItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.OtherEnableSmallItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.OtherQosModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.OtherRecordItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.OtherRecordModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.PriorSmallItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.SEIMessageSendItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 其他Tab Fragment页
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class MoreSettingFragment extends BaseSettingFragment {
    public interface VideoSettingListener {
        void onVodPlayerVisibilityChanged(boolean show);
    }
    
    
    private LinearLayout         mContentItem;
    private List<View>           mSettingItemList;
    private OtherEnableSmallItem mEnableSmallItem;
    private PriorSmallItem       mPriorSmallItem;
    private EnableFlashItem      mEnableFlashItem;
    private GSensorModeItem      mGSensorModeItem;
    private EnableVodPlayerItem  mEnableVodPlayerItem;
    private CustomMsgItem        mCustomMsgItem;
    private SEIMessageSendItem   mSeiMsgItem;
    private AudioMsgItem         mAudioMsgItem;
    private NetworkAudioMsgItem  mNetworkAudioMsgItem;
    private OtherRecordModeItem  mRecordModeItem;
    private OtherRecordItem      mRecordItem;
    private VideoSettingListener mListener;
    private MoreConfig           mMoreConfig;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoSettingListener) {
            mListener = (VideoSettingListener) context;
        }
    }
    
    @Override
    protected void initView(View itemView) {
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mMoreConfig = SettingConfigHelper.getInstance().getMoreConfig();
        
        mEnableSmallItem = new OtherEnableSmallItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_enable_small));
        mSettingItemList.add(mEnableSmallItem);
        
        mPriorSmallItem = new PriorSmallItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_prior_small));
        mSettingItemList.add(mPriorSmallItem);
        
        mGSensorModeItem = new GSensorModeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_open_gravity_inuction));
        mSettingItemList.add(mGSensorModeItem);
        
        mEnableFlashItem = new EnableFlashItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_open_flash));
        mSettingItemList.add(mEnableFlashItem);
        
        mEnableVodPlayerItem = new EnableVodPlayerItem(new EnableVodPlayerItem.EnableVodPlayerListener() {
            @Override
            public void onClicked() {
                mMoreConfig.setVodPlayerEnabled(mEnableVodPlayerItem.getChecked());
                if (mListener != null) {
                    mListener.onVodPlayerVisibilityChanged(mEnableVodPlayerItem.getChecked());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_enable_vodplayer));
        mSettingItemList.add(mEnableVodPlayerItem);
        
        mCustomMsgItem = new CustomMsgItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_custom_message), getString(R.string.trtcdemo_send));
        mSettingItemList.add(mCustomMsgItem);
        
        mSeiMsgItem = new SEIMessageSendItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_sei_message), getString(R.string.trtcdemo_send));
        mSettingItemList.add(mSeiMsgItem);

        mAudioMsgItem = new AudioMsgItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_title_audio_msg), getString(R.string.trtcdemo_send));
        mSettingItemList.add(mAudioMsgItem);

        mNetworkAudioMsgItem = new NetworkAudioMsgItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_title_network_audio_msg), getString(R.string.trtcdemo_send));
        mSettingItemList.add(mNetworkAudioMsgItem);
        
        mRecordModeItem = new OtherRecordModeItem(getContext(), getString(R.string.trtcdemo_record_mode), getString(R.string.trtcdemo_only_audio), getString(R.string.trtcdemo_only_video), getString(R.string.trtcdemo_audio_video));
        mSettingItemList.add(mRecordModeItem);
        
        mRecordItem = new OtherRecordItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_video_file), mMoreConfig.isRecording() ? getString(R.string.trtcdemo_stop_record) : getString(R.string.trtcdemo_start_record));
        mSettingItemList.add(mRecordItem);
        
        for (View item : mSettingItemList) {
            mContentItem.addView(item);
        }
    }
    
    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }
    
}
