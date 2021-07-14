package com.tencent.liteav.trtcdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.RadioButtonSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SeekBarSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SelectionSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SwitchSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.TipButtonSettingItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_180;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_270;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_90;

/**
 * 视频Tab Fragment页
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class VideoSettingFragment extends BaseSettingFragment{
    private static final String TAG = VideoSettingFragment.class.getName();

    private LinearLayout            mLinearItemList;
    private List<BaseSettingItem>   mSettingItemList;
    private SeekBarSettingItem      mMainStreamBitrateItem;
    private SelectionSettingItem    mMainStreamResolutionItem;
    private SelectionSettingItem    mMainStreamVideoFpsItem;
    private RadioButtonSettingItem  mQosPreferenceItem;
    private RadioButtonSettingItem  mMainStreamVideoVerticalItem;
    private RadioButtonSettingItem  mVideoFillModeItem;
    private RadioButtonSettingItem  mMirrorTypeItem;
    private RadioButtonSettingItem  mRotationItem;
    private RadioButtonSettingItem  mRemoteRotationItem;
    private SwitchSettingItem       mRemoteMirrorItem;
    private SwitchSettingItem       mWatermark;
    private SwitchSettingItem       mTimeWatermark;
    private SwitchSettingItem       mPauseScreenCaptureItem;
    private SwitchSettingItem       mEnableSharpnessEnhancementItem;
    private SwitchSettingItem       mEnableMuteImageItem;

    private VideoConfig                         mVideoConfig;
    private ArrayList<TRTCSettingBitrateTable>  mParamArray;
    private int                                 mAppScene = TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL;
    private int                                 mCurrentSolutionIndex;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        boolean isLive = (mAppScene == TRTCCloudDef.TRTC_APP_SCENE_LIVE);
        mParamArray = new ArrayList<>();
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_160_160, isLive ? 220 : 150, 40, 300, 10));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_180, isLive ? 350 : 250, 80, 350, 10));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_240, isLive ? 400 : 300, 100, 400, 10));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_480_480, isLive ? 600 : 400, 200, 1000, 10));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360, isLive ? 750 : 500, 200, 1000, 10));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_480, isLive ? 900 : 600, 250, 1000, 50));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_960_540, isLive ? 1200 : 800, 400, 1600, 50));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720, isLive ? 1750 : 1150, 500, 2000, 50));
        mParamArray.add(new TRTCSettingBitrateTable(TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1920_1080, isLive ? 1900 : 1900, 800, 3000, 50));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected void initView(View itemView) {
        mLinearItemList = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mVideoConfig = SettingConfigHelper.getInstance().getVideoConfig();

        /**
         * 界面中的码率会和选择的分辨率\帧率相关，在选择对应的分辨率之后，要设置对应的码率
         * 所以要在一开始先初始化码率的item，不然会出现null的情况
         */
        mCurrentSolutionIndex = getResolutionPos(mVideoConfig.getMainStreamVideoResolution());
        mMainStreamResolutionItem = new SelectionSettingItem(getContext(),  new BaseSettingItem.ItemText(getString(R.string.trtcdemo_solution), getResources().getStringArray(R.array.trtcdemo_video_solution)),
                new SelectionSettingItem.Listener() {
                    @Override
                    public void onItemSelected(int position, String text) {
                        mCurrentSolutionIndex = position;
                        updateSolution(mMainStreamBitrateItem, mCurrentSolutionIndex);
                        int resolution = getResolution(mMainStreamResolutionItem.getSelected());
                        if (resolution != mVideoConfig.getMainStreamVideoResolution()) {
                            mVideoConfig.setMainStreamVideoResolution(resolution);
                            mTRTCCloudManager.setTRTCCloudParam();
                        }
                    }
                }
        ).setSelect(mCurrentSolutionIndex);
        mSettingItemList.add(mMainStreamResolutionItem);

        mMainStreamVideoFpsItem = new SelectionSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_frame_bits), getResources().getStringArray(R.array.trtc_video_fps)),
                new SelectionSettingItem.Listener() {
                    @Override
                    public void onItemSelected(int position, String text) {
                        int fps = getFps(position);
                        if (fps != mVideoConfig.getMainStreamVideoFps()) {
                            mVideoConfig.setMainStreamVideoFps(fps);
                            mTRTCCloudManager.setTRTCCloudParam();
                        }
                    }
                }
        ).setSelect(getFpsPos(mVideoConfig.getMainStreamVideoFps()));
        mSettingItemList.add(mMainStreamVideoFpsItem);

        mMainStreamBitrateItem = new SeekBarSettingItem(getContext(),  new BaseSettingItem.ItemText(getString(R.string.trtcdemo_code_bits), ""), new SeekBarSettingItem.Listener() {
            @Override
            public void onSeekBarChange(int progress, boolean fromUser) {
                int bitrate = getBitrate(progress, mCurrentSolutionIndex);
                mMainStreamBitrateItem.setTips(bitrate + "kbps");
                if (bitrate != mVideoConfig.getMainStreamVideoBitrate()) {
                    mVideoConfig.setMainStreamVideoBitrate(bitrate);
                    mTRTCCloudManager.setTRTCCloudParam();
                }
            }
        });
        updateSolution(mMainStreamBitrateItem, mCurrentSolutionIndex);
        mMainStreamBitrateItem.setProgress(getBitrateProgress(mVideoConfig.getMainStreamVideoBitrate(), mCurrentSolutionIndex));
        mMainStreamBitrateItem.setTips(getBitrate(mVideoConfig.getMainStreamVideoBitrate(), mCurrentSolutionIndex) + "kbps");
        mSettingItemList.add(mMainStreamBitrateItem);

        mQosPreferenceItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_picture_quality), getString(R.string.trtcdemo_priority_flow), getString(R.string.trtcdemo_priority_vivid)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mVideoConfig.setQosPreference(index == 0 ?
                                TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_SMOOTH : TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_CLEAR);
                        mTRTCCloudManager.setQosParam();
                    }
                });
        mSettingItemList.add(mQosPreferenceItem);

        mMainStreamVideoVerticalItem = new RadioButtonSettingItem(getContext(),  new BaseSettingItem.ItemText(getString(R.string.trtcdemo_picture_orientation), getString(R.string.trtcdemo_horizontal_mode), getString(R.string.trtcdemo_vertical_mode)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mVideoConfig.setMainStreamVideoVertical(index == 1);
                        mTRTCCloudManager.setTRTCCloudParam();

                    }
                });
        mSettingItemList.add(mMainStreamVideoVerticalItem);

        mVideoFillModeItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_frame_fill_mode), getString(R.string.trtcdemo_frame_fill_mode_fill), getString(R.string.trtcdemo_frame_fill_mode_fit)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mVideoConfig.setVideoFillMode(index == 0);
                        mTRTCCloudManager.setVideoFillMode(mVideoConfig.isVideoFillMode());
                    }
                });
        mSettingItemList.add(mVideoFillModeItem);

        mRotationItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_local_rotate_direction), "0", "90", "180", "270"),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        int rotation = TRTC_VIDEO_ROTATION_0;
                        if (index == 1) {
                            rotation = TRTC_VIDEO_ROTATION_90;
                        } else if (index == 2) {
                            rotation = TRTC_VIDEO_ROTATION_180;
                        } else if (index == 3){
                            rotation = TRTC_VIDEO_ROTATION_270;
                        }
                        mVideoConfig.setLocalRotation(rotation);
                        mTRTCCloudManager.setLocalVideoRotation(mVideoConfig.getLocalRotation());
                    }
                });
        mSettingItemList.add(mRotationItem);

        mRemoteRotationItem = new RadioButtonSettingItem(getContext(),  new BaseSettingItem.ItemText(getString(R.string.trtcdemo_code_output_picture_rotate), "0", "90", "180", "270"),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        if(SettingConfigHelper.getInstance().getMoreConfig().isEnableGSensorMode()){
                            SettingConfigHelper.getInstance().getMoreConfig().setEnableGSensorMode(false);
                            mTRTCCloudManager.enableGSensor(false);
                            ToastUtils.showShort(R.string.trtcdemo_close_gravity_inuction);
                        }
                        int rotation = TRTC_VIDEO_ROTATION_0;
                        if (index == 1) {
                            rotation = TRTC_VIDEO_ROTATION_90;
                        }else if(index == 2){
                            rotation = TRTC_VIDEO_ROTATION_180;
                        }else if(index == 3){
                            rotation = TRTC_VIDEO_ROTATION_270;
                        }
                        mVideoConfig.setRemoteRotation(rotation);
                        mTRTCCloudManager.setVideoEncoderRotation(rotation);
                    }
                });
        mSettingItemList.add(mRemoteRotationItem);

        mMirrorTypeItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_local_mirror), getString(R.string.trtcdemo_local_front_camera_open), getString(R.string.trtcdemo_mirror_all_open), getString(R.string.trtcdemo_mirror_all_close)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        int type;
                        if (index == 0) {
                            type = TRTC_VIDEO_MIRROR_TYPE_AUTO;
                        } else if (mMirrorTypeItem.getSelected() == 1) {
                            type = TRTC_VIDEO_MIRROR_TYPE_ENABLE;
                        } else {
                            type = TRTC_VIDEO_MIRROR_TYPE_DISABLE;
                        }
                        mVideoConfig.setMirrorType(type);
                        mTRTCCloudManager.setLocalViewMirror(mVideoConfig.getMirrorType());
                    }
                });
        mSettingItemList.add(mMirrorTypeItem);

        mRemoteMirrorItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_remote_mioor), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setRemoteMirror(mRemoteMirrorItem.getChecked());
                        mTRTCCloudManager.enableVideoEncMirror(mVideoConfig.isRemoteMirror());
                    }
                });
        mSettingItemList.add(mRemoteMirrorItem);

        mWatermark = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_watermark), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setWatermark(mWatermark.getChecked());
                        mTRTCCloudManager.enableWatermark(mVideoConfig.isWatermark());
                    }
                });
        mSettingItemList.add(mWatermark);

        mPauseScreenCaptureItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_pause_video_capture), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setScreenCapturePaused(mPauseScreenCaptureItem.getChecked());
                        mTRTCCloudManager.muteLocalVideo(mPauseScreenCaptureItem.getChecked());
                    }
                });
        mSettingItemList.add(mPauseScreenCaptureItem);

        mEnableSharpnessEnhancementItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_picture_enhance), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setSharpnessEnhancementEnabled(mEnableSharpnessEnhancementItem.getChecked());
                        mTRTCCloudManager.setSharpnessEnhancementEnabled(mEnableSharpnessEnhancementItem.getChecked());
                    }
                });
        mSettingItemList.add(mEnableSharpnessEnhancementItem);


        mEnableMuteImageItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_set_shim), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setMuteImageEnabled(mEnableMuteImageItem.getChecked());
                        mTRTCCloudManager.setMuteImageEnabled(mEnableMuteImageItem.getChecked());
                    }
                });
        mSettingItemList.add(mEnableMuteImageItem);

        mTimeWatermark = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_time_watermark), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setTimeWatermark(mTimeWatermark.getChecked());
                        mTRTCCloudManager.enableTimeWatermark(mVideoConfig.isTimeWatermark());
                    }
                });
        mSettingItemList.add(mTimeWatermark);

        TipButtonSettingItem snapshotItem = new TipButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_lcoal_video_capture), getString(R.string.trtcdemo_capture_image)), new TipButtonSettingItem.OnClickListener() {
            @Override
            public void onClick() {
                mTRTCCloudManager.snapshotLocalView();
            }
        });
        mSettingItemList.add(snapshotItem);

        updateItem();

        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(8), 0, 0);
            mLinearItemList.addView(view);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateItem() {
        mQosPreferenceItem.setSelect(
                mVideoConfig.getQosPreference() == TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_SMOOTH ? 0 : 1);
        mMainStreamVideoVerticalItem.setSelect(mVideoConfig.isMainStreamVideoVertical() ? 1 : 0);
        mVideoFillModeItem.setSelect(mVideoConfig.isVideoFillMode() ? 0 : 1);
        int index = 0;
        int type  = mVideoConfig.getMirrorType();
        if (TRTC_VIDEO_MIRROR_TYPE_AUTO == type) {
            index = 0;
        } else if (TRTC_VIDEO_MIRROR_TYPE_ENABLE == type) {
            index = 1;
        } else {
            index = 2;
        }
        mMirrorTypeItem.setSelect(index);
        int rotation = mVideoConfig.getLocalRotation();
        if (TRTC_VIDEO_ROTATION_0 == rotation) {
            index = 0;
        } else if (TRTC_VIDEO_ROTATION_90 == rotation) {
            index = 1;
        } else {
            index = 2;
        }
        mRotationItem.setSelect(index);
        mRemoteMirrorItem.setCheck(mVideoConfig.isRemoteMirror());
        mWatermark.setCheck(mVideoConfig.isWatermark());
        mPauseScreenCaptureItem.setCheck(mVideoConfig.isScreenCapturePaused());
        mEnableSharpnessEnhancementItem.setCheck(mVideoConfig.isSharpnessEnhancementEnabled());
        mTRTCCloudManager.setSharpnessEnhancementEnabled(mVideoConfig.isSharpnessEnhancementEnabled());
        mEnableMuteImageItem.setCheck(mVideoConfig.isMuteImageEnabled());
        mVideoFillModeItem.setSelect(mVideoConfig.isVideoFillMode() ? 0 : 1);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }


    private void updateSolution(SeekBarSettingItem item, int pos) {
        int minBitrate = getMinBitrate(pos);
        int maxBitrate = getMaxBitrate(pos);

        int stepBitrate = getStepBitrate(pos);
        int max         = (maxBitrate - minBitrate) / stepBitrate;
        if (item.getMax() != max) {    // 有变更时设置默认值
            item.setMax(max);
            int defBitrate = getDefBitrate(pos);
            item.setProgress(getBitrateProgress(defBitrate, pos));
        } else {
            item.setMax(max);
        }
    }

    private int getResolutionPos(int resolution) {
        for (int i = 0; i < mParamArray.size(); i++) {
            if (resolution == (mParamArray.get(i).resolution)) {
                return i;
            }
        }
        return 4;
    }

    private int getResolution(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).resolution;
        }
        return TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
    }

    private int getFpsPos(int fps) {
        switch (fps) {
            case 10:
                return 0;
            case 15:
                return 1;
            case 20:
                return 2;
            default:
                return 0;
        }
    }

    private int getFps(int pos) {
        switch (pos) {
            case 0:
                return 10;
            case 1:
                return 15;
            case 2:
                return 20;
            default:
                return 15;
        }
    }

    private int getMinBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).minBitrate;
        }
        return 300;
    }

    private int getMaxBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).maxBitrate;
        }
        return 1000;
    }

    private int getDefBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).defaultBitrate;
        }
        return 400;
    }

    /**
     * 获取当前精度
     */
    private int getStepBitrate(int pos) {
        if (pos >= 0 && pos < mParamArray.size()) {
            return mParamArray.get(pos).step;
        }
        return 10;
    }

    private int getBitrateProgress(int bitrate, int pos) {
        int minBitrate  = getMinBitrate(pos);
        int stepBitrate = getStepBitrate(pos);

        int progress = (bitrate - minBitrate) / stepBitrate;
        Log.i(TAG, "getBitrateProgress->progress: " + progress + ", min: " + minBitrate + ", stepBitrate: " + stepBitrate + "/" + bitrate);
        return progress;
    }

    private int getBitrate(int progress, int pos) {
        int minBitrate  = getMinBitrate(pos);
        int maxBitrate  = getMaxBitrate(pos);
        int stepBitrate = getStepBitrate(pos);
        int bit         = (progress * stepBitrate) + minBitrate;
        Log.i(TAG, "getBitrate->bit: " + bit + ", min: " + minBitrate + ", max: " + maxBitrate);
        return bit;
    }

    static class TRTCSettingBitrateTable {
        public int resolution;
        public int defaultBitrate;
        public int minBitrate;
        public int maxBitrate;
        public int step;

        public TRTCSettingBitrateTable(int resolution, int defaultBitrate, int minBitrate, int maxBitrate, int step) {
            this.resolution = resolution;
            this.defaultBitrate = defaultBitrate;
            this.minBitrate = minBitrate;
            this.maxBitrate = maxBitrate;
            this.step = step;
        }
    }
}
