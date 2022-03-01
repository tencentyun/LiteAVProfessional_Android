package com.tencent.liteav.demo.livelinkmicnew.settting;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeDisable;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeEnable;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModeLandscape;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livelinkmicnew.R;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.BaseSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.CheckBoxSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.CustomSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.RadioButtonSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.SelectionSettingItem;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频设置页
 */
public class PushVideoSettingFragment extends Fragment {
    private static final String                             TAG       = "PushVideoSettingFragment";
    private              LinearLayout                       mContentItem;
    private              List<BaseSettingItem>              mSettingItemList;
    private              SelectionSettingItem               mResolutionItem;
    private              RadioButtonSettingItem             mMirrorTypeItem;
    private              CheckBoxSettingItem                mRemoteMirrorItem;
    private              CheckBoxSettingItem                mWatermark;
    private              CheckBoxSettingItem                mVirtualCamera;
    private              CheckBoxSettingItem                mMuteVideo;
    private              AVSettingConfig.VideoConfig        mVideoConfig;
    private              ArrayList<TRTCSettingBitrateTable> paramArray;
    private              int                                mCurRes;
    private              V2TXLivePusher                     mLivePusher;
    private              ImageView                          mImageView;
    private              EditText                           mPayLoadTypeEdit;
    private              Context                            mAppContext;

    public void setLivePusher(V2TXLivePusher pusher) {
        mLivePusher = pusher;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppContext = getActivity().getApplicationContext();
        initData();
    }

    private void initData() {
        paramArray = new ArrayList<>();
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution160x160,
                250, 40, 300, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution320x180,
                350, 80, 350, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution320x240,
                400, 100, 400, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x480,
                500, 200, 1000, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360,
                600, 200, 1000, 10));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x480,
                700, 250, 1000, 50));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540,
                900, 400, 1600, 50));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720,
                1250, 500, 2000, 50));
        paramArray.add(new TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1920x1080,
                1900, 800, 3000, 50));
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
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_resolution), getResources().getStringArray(R.array.live_link_mic_new_solution));
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
                                V2TXLiveDef.V2TXLiveVideoEncoderParam param = new V2TXLiveDef.V2TXLiveVideoEncoderParam(resolution);
                                param.videoResolutionMode = mVideoConfig.isVideoVertical() ? V2TXLiveVideoResolutionModePortrait : V2TXLiveVideoResolutionModeLandscape;
                                mLivePusher.setVideoQuality(param);
                            }
                        }
                    }
                }
        ).setSelect(mCurRes);
        mSettingItemList.add(mResolutionItem);

        itemText =
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_local_preview_mirror),
                        getString(R.string.livelinkmicnew_tv_mirror_type_auto),
                        getString(R.string.livelinkmicnew_tv_mirror_type_enable),
                        getString(R.string.livelinkmicnew_tv_mirror_type_disable));
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
                        mVideoConfig.setMirrorType(type);
                        if (mLivePusher != null) {
                            mLivePusher.setRenderMirror(type);
                        }
                    }
                });
        mSettingItemList.add(mMirrorTypeItem);

        itemText =
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_enable_remote_mirror), "");
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
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_enable_video_watermark), "");
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
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_enable_virtual_camera), "");
        mVirtualCamera = new CheckBoxSettingItem(getContext(), itemText,
                new CheckBoxSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setMuteImageEnabled(mVirtualCamera.getChecked());
                        if (mLivePusher != null) {
                            boolean isChecked = mVirtualCamera.getChecked();
                            if (isChecked) {
                                mLivePusher.startVirtualCamera(BitmapFactory.decodeResource(getResources(), R.drawable.live_link_mic_pause_publish));
                            } else {
                                mLivePusher.stopVirtualCamera();
                            }
                        }
                    }
                });
        mSettingItemList.add(mVirtualCamera);

        itemText =
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_enable_mute_video), "");
        mMuteVideo = new CheckBoxSettingItem(getContext(), itemText,
                new CheckBoxSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mVideoConfig.setEnableVideo(mMuteVideo.getChecked());
                        if (mLivePusher != null) {
                            boolean isChecked = mMuteVideo.getChecked();
                            if (isChecked) {
                                mLivePusher.resumeVideo();
                            } else {
                                mLivePusher.pauseVideo();
                            }
                        }
                    }
                });
        mSettingItemList.add(mMuteVideo);

        itemText =
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_screen_snapshot), "");
        CustomSettingItem snapshotItem = new CustomSettingItem(getContext(), itemText, createSnapshotButton());
        snapshotItem.setAlign(CustomSettingItem.ALIGN_RIGHT);
        mSettingItemList.add(snapshotItem);

        itemText =
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_payload_type), "");
        CustomSettingItem payLoadTypeItem = new CustomSettingItem(getContext(), itemText, setSEIPayloadType());
        payLoadTypeItem.setAlign(CustomSettingItem.ALIGN_RIGHT);
        mSettingItemList.add(payLoadTypeItem);

        itemText =
                new BaseSettingItem.ItemText(getString(R.string.livelinkmicnew_tv_sei), "");
        CustomSettingItem seiItem = new CustomSettingItem(getContext(), itemText, createSEIButton());
        seiItem.setAlign(CustomSettingItem.ALIGN_RIGHT);
        mSettingItemList.add(seiItem);

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
        List<View> views = new ArrayList<>();
        final Button button = new Button(getContext());
        button.setText(getString(R.string.livelinkmicnew_tv_snapshot));
        mImageView = new ImageView(getContext());
        mImageView.setLayoutParams(new Gallery.LayoutParams(80, 80));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapshotWithPermission();
            }
        });
        views.add(mImageView);
        views.add(button);
        return views;
    }

    private void snapshotWithPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                mLivePusher.snapshot();
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                Toast.makeText(mAppContext, getString(R.string.livelinkmicnew_toast_snapshot_permission_failure),
                        Toast.LENGTH_LONG).show();
            }
        }).request();
    }

    private List<View> setSEIPayloadType() {
        List<View>   views  = new ArrayList<>();

        mPayLoadTypeEdit = new EditText(getContext());
        mPayLoadTypeEdit.setTextSize(12);
        mPayLoadTypeEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        mPayLoadTypeEdit.setTextColor(getResources().getColor(R.color.white_alpha));
        mPayLoadTypeEdit.setLayoutParams( new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT));
        views.add(mPayLoadTypeEdit);
        return views;
    }

    private List<View> createSEIButton() {
        List<View>   views  = new ArrayList<>();
        final Button button = new Button(getContext());
        button.setText(getString(R.string.livelinkmicnew_tv_sei));

        final EditText editText = new EditText(getContext());
        editText.setTextSize(12);
        editText.setTextColor(getResources().getColor(R.color.white_alpha));
        editText.setLayoutParams( new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mPayLoadTypeEdit.getText().toString())) {
                    Toast.makeText(getActivity(), getString(R.string.livelinkmicnew_tv_payload_empty_tips), Toast.LENGTH_LONG).show();
                    return;
                }
                if (mPayLoadTypeEdit.getText().toString().length() > 4) {
                    Toast.makeText(getActivity(), getString(R.string.livelinkmicnew_tv_payload_error_tips), Toast.LENGTH_LONG).show();
                    return;
                }
                mLivePusher.sendSeiMessage(Integer.parseInt(mPayLoadTypeEdit.getText().toString()), editText.getText().toString().getBytes());
            }
        });
        views.add(editText);
        views.add(button);
        return views;
    }

    private void updateItem() {
        int index;
        V2TXLiveDef.V2TXLiveMirrorType type = mVideoConfig.getMirrorType();
        if (V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeAuto == type) {
            index = 0;
        } else if (V2TXLiveMirrorTypeEnable == type) {
            index = 1;
        } else {
            index = 2;
        }
        mMirrorTypeItem.setSelect(index);
        mRemoteMirrorItem.setCheck(mVideoConfig.isRemoteMirror());
        mWatermark.setCheck(mVideoConfig.isWatermark());
        mVirtualCamera.setCheck(mVideoConfig.isMuteImageEnabled());
        mMuteVideo.setCheck(mVideoConfig.isEnableVideo());
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
        public int                                 defaultBitrate;
        public int                                 minBitrate;
        public int                                 maxBitrate;
        public int                                 step;

        public TRTCSettingBitrateTable(V2TXLiveDef.V2TXLiveVideoResolution resolution, int defaultBitrate, int minBitrate, int maxBitrate, int step) {
            this.resolution = resolution;
            this.defaultBitrate = defaultBitrate;
            this.minBitrate = minBitrate;
            this.maxBitrate = maxBitrate;
            this.step = step;
        }
    }
}
