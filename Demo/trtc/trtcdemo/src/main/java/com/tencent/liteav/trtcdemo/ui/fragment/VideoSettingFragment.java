package com.tencent.liteav.trtcdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.EncoderTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.EnableMuteImageItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.EnableSharpnessEnhancementItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.LocalMirrorItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.LocalRotationItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.MainStreamBitrateItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.MainStreamResolutionItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.MainStreamVideoFpsItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.MainStreamVideoVerticalItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.OpenVideoCaptureItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.PauseScreenCaptureItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.PublishVideoItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.QosPreferenceItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.RemoteMirrorItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.RemoteRotationItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.SnapshotItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.SubStreamBitrateItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.SubStreamResolutionItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.SubStreamVideoFpsItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.SubStreamVideoVerticalItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.TimeWaterMarkItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.VideoFillModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.videoitem.WaterMarkItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频Tab Fragment页
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class VideoSettingFragment extends BaseSettingFragment {

    private LinearLayout                       mLinearItemList;
    private List<View>                         mSettingItemList;
    private MainStreamBitrateItem              mMainStreamBitrateItem;
    private MainStreamResolutionItem           mMainStreamResolutionItem;
    private MainStreamVideoFpsItem             mMainStreamVideoFpsItem;
    private SubStreamBitrateItem               mSubStreamBitrateItem;
    private SubStreamResolutionItem            mSubStreamResolutionItem;
    private SubStreamVideoFpsItem              mSubStreamVideoFpsItem;
    private QosPreferenceItem                  mQosPreferenceItem;
    private AbsRadioButtonItem                 mEncoderTypeItem;
    private MainStreamVideoVerticalItem        mMainStreamVideoVerticalItem;
    private SubStreamVideoVerticalItem         mSubStreamVideoVerticalItem;
    private VideoFillModeItem                  mVideoFillModeItem;
    private OpenVideoCaptureItem               mOpenVideoCaptureItem;
    private PublishVideoItem                   mPublishVideoItem;
    private LocalMirrorItem                    mLocalMirrorItem;
    private LocalRotationItem                  mLocalRotationItem;
    private RemoteRotationItem                 mRemoteRotationItem;
    private RemoteMirrorItem                   mRemoteMirrorItem;
    private WaterMarkItem                      mWaterMarkItem;
    private TimeWaterMarkItem                  mTimeWaterMarkItem;
    private PauseScreenCaptureItem             mPauseScreenCaptureItem;
    private EnableSharpnessEnhancementItem     mEnableSharpnessEnhancementItem;
    private EnableMuteImageItem                mEnableMuteImageItem;
    private SnapshotItem                       mSnapshotItem;
    private VideoConfig                        mVideoConfig;
    private ArrayList<TRTCSettingBitrateTable> mParamArray;
    private int                                mAppScene = TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL;
    private int                                mCurrentSolutionIndex;
    private int                                mCurrentSubSolutionIndex;


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
        mMainStreamResolutionItem = new MainStreamResolutionItem(new MainStreamResolutionItem.OnSelectListener() {
            @Override
            public void onSelected(int index, String str) {
                mCurrentSolutionIndex = index;
                mMainStreamBitrateItem.setCurrentSolutionIndex(mCurrentSolutionIndex);
                mMainStreamBitrateItem.updateSolution(mCurrentSolutionIndex);
                int resolution = getResolution(mMainStreamResolutionItem.getSelected());
                if (resolution != mVideoConfig.getMainStreamVideoResolution()) {
                    mVideoConfig.setMainStreamVideoResolution(resolution);
                    mTRTCCloudManager.setTRTCCloudParam();
                }
            }
        }, getContext(), getString(R.string.trtcdemo_main_solution), getResources().getStringArray(R.array.trtcdemo_video_solution));
        mMainStreamResolutionItem.setSelect(mCurrentSolutionIndex);
        mSettingItemList.add(mMainStreamResolutionItem);

        mMainStreamVideoFpsItem = new MainStreamVideoFpsItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_main_frame_bits), getResources().getStringArray(R.array.trtcdemo_video_fps));
        mMainStreamVideoFpsItem.setSelect(MainStreamVideoFpsItem.getFpsPos(mVideoConfig.getMainStreamVideoFps()));
        mSettingItemList.add(mMainStreamVideoFpsItem);

        mMainStreamBitrateItem = new MainStreamBitrateItem(mCurrentSolutionIndex, mTRTCCloudManager, mParamArray, getContext(), getString(R.string.trtcdemo_main_code_bits), "");
        mSettingItemList.add(mMainStreamBitrateItem);

        // 辅路
        mCurrentSubSolutionIndex = getResolutionPos(mVideoConfig.getSubStreamVideoResolution());
        mSubStreamResolutionItem = new SubStreamResolutionItem(new SubStreamResolutionItem.OnSelectListener() {
            @Override
            public void onSelected(int index, String str) {
                mCurrentSubSolutionIndex = index;
                mSubStreamBitrateItem.setCurrentSolutionIndex(mCurrentSubSolutionIndex);
                mSubStreamBitrateItem.updateSolution(mCurrentSubSolutionIndex);
                int resolution = getResolution(mSubStreamResolutionItem.getSelected());
                if (resolution != mVideoConfig.getSubStreamVideoResolution()) {
                    mVideoConfig.setSubStreamVideoResolution(resolution);
                    mTRTCCloudManager.setTRTCCloudParam();
                }
            }
        }, getContext(), getString(R.string.trtcdemo_sub_solution), getResources().getStringArray(R.array.trtcdemo_video_solution));
        mSubStreamResolutionItem.setSelect(mCurrentSubSolutionIndex);
        mSettingItemList.add(mSubStreamResolutionItem);

        mSubStreamVideoFpsItem = new SubStreamVideoFpsItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_sub_frame_bits), getResources().getStringArray(R.array.trtcdemo_video_fps));
        mSubStreamVideoFpsItem.setSelect(SubStreamVideoFpsItem.getFpsPos(mVideoConfig.getSubStreamVideoFps()));
        mSettingItemList.add(mSubStreamVideoFpsItem);

        mSubStreamBitrateItem = new SubStreamBitrateItem(mCurrentSubSolutionIndex, mTRTCCloudManager, mParamArray, getContext(), getString(R.string.trtcdemo_sub_code_bits), "");
        mSettingItemList.add(mSubStreamBitrateItem);

        mQosPreferenceItem = new QosPreferenceItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_picture_quality), getString(R.string.trtcdemo_priority_flow), getString(R.string.trtcdemo_priority_vivid));
        mSettingItemList.add(mQosPreferenceItem);

        mEncoderTypeItem = new EncoderTypeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_encoder_type), getString(R.string.trtcdemo_encoder_h265), getString(R.string.trtcdemo_encoder_h264));
        mSettingItemList.add(mEncoderTypeItem);

        mMainStreamVideoVerticalItem = new MainStreamVideoVerticalItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_main_picture_orientation), getString(R.string.trtcdemo_horizontal_mode), getString(R.string.trtcdemo_vertical_mode));
        mSettingItemList.add(mMainStreamVideoVerticalItem);

        mSubStreamVideoVerticalItem = new SubStreamVideoVerticalItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_sub_picture_orientation), getString(R.string.trtcdemo_horizontal_mode), getString(R.string.trtcdemo_vertical_mode));
        mSettingItemList.add(mSubStreamVideoVerticalItem);

        mVideoFillModeItem = new VideoFillModeItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_frame_fill_mode), getString(R.string.trtcdemo_frame_fill_mode_fill), getString(R.string.trtcdemo_frame_fill_mode_fit));
        mSettingItemList.add(mVideoFillModeItem);

        mLocalRotationItem = new LocalRotationItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_local_rotate_direction), "0", "90", "180", "270");
        mSettingItemList.add(mLocalRotationItem);

        mRemoteRotationItem = new RemoteRotationItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_code_output_picture_rotate), "0", "90", "180", "270");
        mSettingItemList.add(mRemoteRotationItem);

        mOpenVideoCaptureItem = new OpenVideoCaptureItem(mTRTCCloudManager, getContext(),
                getString(R.string.trtcdemo_open_video_capture));
        mSettingItemList.add(mOpenVideoCaptureItem);

        mPublishVideoItem = new PublishVideoItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_publish_video));
        mSettingItemList.add(mPublishVideoItem);

        mLocalMirrorItem = new LocalMirrorItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_local_mirror), getString(R.string.trtcdemo_local_front_camera_open), getString(R.string.trtcdemo_mirror_all_open), getString(R.string.trtcdemo_mirror_all_close));
        mSettingItemList.add(mLocalMirrorItem);

        mRemoteMirrorItem = new RemoteMirrorItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_open_remote_mioor));
        mSettingItemList.add(mRemoteMirrorItem);

        mWaterMarkItem = new WaterMarkItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_open_watermark));
        mSettingItemList.add(mWaterMarkItem);

        mPauseScreenCaptureItem = new PauseScreenCaptureItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_pause_screen_capture));
        mSettingItemList.add(mPauseScreenCaptureItem);

        mEnableSharpnessEnhancementItem = new EnableSharpnessEnhancementItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_picture_enhance));
        mSettingItemList.add(mEnableSharpnessEnhancementItem);

        mEnableMuteImageItem = new EnableMuteImageItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_set_shim));
        mSettingItemList.add(mEnableMuteImageItem);

        mTimeWaterMarkItem = new TimeWaterMarkItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_open_time_watermark));
        mSettingItemList.add(mTimeWaterMarkItem);
        mTimeWaterMarkItem.setCheck(SettingConfigHelper.getInstance().getVideoConfig().isTimeWatermark());

        mSnapshotItem = new SnapshotItem(mTRTCCloudManager, getContext(), getString(R.string.trtcdemo_lcoal_video_capture), getString(R.string.trtcdemo_capture_image));
        mSettingItemList.add(mSnapshotItem);

        for (View view : mSettingItemList) {
            mLinearItemList.addView(view);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
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

    public static class TRTCSettingBitrateTable {
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
