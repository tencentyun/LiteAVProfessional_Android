package com.tencent.liteav.demo.livelinkmicnew.settting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.SizeUtils;

import android.support.v4.app.Fragment;

import com.tencent.liteav.demo.livelinkmicnew.R;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.BaseSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.CheckBoxSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.CustomSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.RadioButtonSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.SelectionSettingItem;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeDisable;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeEnable;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModeLandscape;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE;

/**
 * 视频设置页
 *
 */
public class PushVideoSettingFragment extends Fragment {
    private static final String                 TAG = PushVideoSettingFragment.class.getName();

    /**
     * 界面相关
     */
    private              LinearLayout           mContentItem;
    private              List<BaseSettingItem>  mSettingItemList;
    private              SelectionSettingItem   mResolutionItem;
    private              RadioButtonSettingItem mMirrorTypeItem;
    private              CheckBoxSettingItem    mRemoteMirrorItem;
    private              CheckBoxSettingItem    mWatermark;

    private AVSettingConfig.VideoConfig         mVideoConfig;
    private ArrayList<TRTCSettingBitrateTable>  paramArray;
    private int                                 mAppScene = TRTCCloudDef.TRTC_APP_SCENE_LIVE;
    private int                                 mCurRes;
    private V2TXLivePusher                      mLivePusher;
    private ImageView                           mImageView;

    public void setLivePusher(V2TXLivePusher pusher) {
        mLivePusher = pusher;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        boolean isVideoCall = mAppScene == TRTCCloudDef.TRTC_APP_SCENE_LIVE;
        paramArray = new ArrayList<>();
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution160x160, isVideoCall ? 250 : 300, 40, 300, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution320x180, isVideoCall ? 350 : 350, 80, 350, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution320x240, isVideoCall ? 400 : 400, 100, 400, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x480, isVideoCall ? 500 : 750, 200, 1000, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360, isVideoCall ? 600 : 900, 200, 1000, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x480, isVideoCall ? 700 : 1000, 250, 1000, 50));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540, isVideoCall ? 900 : 1350, 400, 1600, 50));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720, isVideoCall ? 1250 : 1850, 500, 2000, 50));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1920x1080, isVideoCall ? 1900 : 1900, 800, 3000, 50));
    }

    @Override
    public void onViewCreated(View itemView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(itemView, savedInstanceState);
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mVideoConfig = AVSettingConfig.getInstance().getVideoConfig();
        mCurRes = mVideoConfig.getCurrentResolutionPosition(); // 默认设置

        // 分辨率相关
        BaseSettingItem.ItemText itemText =
                new BaseSettingItem.ItemText("分辨率", getResources().getStringArray(R.array.live_link_mic_new_solution));
        mResolutionItem = new SelectionSettingItem(getContext(), itemText,
                new SelectionSettingItem.Listener() {
                    @Override
                    public void onItemSelected(int position, String text) {
                        mCurRes = position;
                        mVideoConfig.setCurrentResolutionPosition(mCurRes);
                        V2TXLiveDef.V2TXLiveVideoResolution resolution = getResolution(mResolutionItem.getSelected());
                        if (resolution != mVideoConfig.getVideoResolution()) {
                            mVideoConfig.setVideoResolution(resolution);
                            if (mLivePusher != null) {
                                mLivePusher.setVideoQuality(resolution, mVideoConfig.isVideoVertical() ? V2TXLiveVideoResolutionModePortrait : V2TXLiveVideoResolutionModeLandscape);
                            }
                        }
                    }
                }
        ).setSelect(mCurRes);
        mSettingItemList.add(mResolutionItem);

        itemText =
                new BaseSettingItem.ItemText("本地预览镜像", "auto", "开启", "关闭");
        mMirrorTypeItem = new RadioButtonSettingItem(getContext(), itemText,
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        V2TXLiveDef.V2TXLiveMirrorType type;
                        if (index == 0) {
                            //自动
                            type = V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeAuto;
                        } else if (mMirrorTypeItem.getSelected() == 1) {
                            type = V2TXLiveMirrorTypeEnable;
                        } else {
                            type = V2TXLiveMirrorTypeDisable;
                        }
                        if (mLivePusher != null) {
                            mLivePusher.setRenderMirror(type);
                        }
                    }
                });
        mSettingItemList.add(mMirrorTypeItem);

        itemText =
                new BaseSettingItem.ItemText("开启远程镜像", "");
        mRemoteMirrorItem = new CheckBoxSettingItem(getContext(), itemText,
                new CheckBoxSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setRemoteMirror(mRemoteMirrorItem.getChecked());
                        if (mLivePusher != null) {
                            mLivePusher.setEncoderMirror(mRemoteMirrorItem.getChecked());
                        }
                    }
                });
        mSettingItemList.add(mRemoteMirrorItem);

        itemText =
                new BaseSettingItem.ItemText("开启视频水印", "");
        mWatermark = new CheckBoxSettingItem(getContext(), itemText,
                new CheckBoxSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setWatermark(mWatermark.getChecked());
                        if (mLivePusher != null) {
                            Bitmap bitmap = ImageUtils.getBitmap(R.drawable.live_link_mic_new_watermark);
                            mLivePusher.setWatermark(mWatermark.getChecked() ? bitmap : null, 0.1f, 0.1f, 0.2f);
                        }
                    }
                });
        mSettingItemList.add(mWatermark);

        itemText =
                new BaseSettingItem.ItemText("视频截图", "");
        CustomSettingItem snapshotItem = new CustomSettingItem(getContext(), itemText, createSnapshotButton());
        snapshotItem.setAlign(CustomSettingItem.ALIGN_RIGHT);
        mSettingItemList.add(snapshotItem);

        updateItem();

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(8), 0, 0);
            mContentItem.addView(view);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoConfig.saveCache();
    }

    public void setSnapshotImage(Bitmap bitmap) {
        if (mImageView != null && bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    private List<View> createSnapshotButton() {
        List<View>   views  = new ArrayList<>();
        final Button button = new Button(getContext());
        button.setText("截图");
        mImageView = new ImageView(getContext());
        mImageView.setLayoutParams(new Gallery.LayoutParams(80, 80));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLivePusher.snapshot();
            }
        });
        views.add(mImageView);
        views.add(button);
        return views;
    }

    private void updateItem() {
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
        mRemoteMirrorItem.setCheck(mVideoConfig.isRemoteMirror());
        mWatermark.setCheck(mVideoConfig.isWatermark());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_link_mic_new_setting_common, container, false);
    }

    private V2TXLiveDef.V2TXLiveVideoResolution getResolution(int pos) {
        if (pos >= 0 && pos < paramArray.size()) {
            return paramArray.get(pos).resolution;
        }
        return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360;
    }

    static class TRTCSettingBitrateTable {
        public V2TXLiveDef.V2TXLiveVideoResolution resolution;
        public int defaultBitrate;
        public int minBitrate;
        public int maxBitrate;
        public int step;

        public TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution resolution, int defaultBitrate, int minBitrate, int maxBitrate, int step) {
            this.resolution = resolution;
            this.defaultBitrate = defaultBitrate;
            this.minBitrate = minBitrate;
            this.maxBitrate = maxBitrate;
            this.step = step;
        }
    }
}
