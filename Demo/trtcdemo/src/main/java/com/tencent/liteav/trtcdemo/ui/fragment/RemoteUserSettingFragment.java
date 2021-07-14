package com.tencent.liteav.trtcdemo.ui.fragment;


import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.RemoteUserConfig;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.RadioButtonSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SeekBarSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SwitchSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.TipButtonSettingItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_180;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_270;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_90;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB;

/**
 * 用户列表管理界面
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class RemoteUserSettingFragment extends BaseSettingFragment implements View.OnClickListener {
    public static final  String                 DATA = "data";
    private static final String                 TAG  = RemoteUserSettingFragment.class.getName();

    private FrameLayout             mFrameBack;
    private TextView                mTextTitle;
    private LinearLayout            mLinearContent;
    private List<BaseSettingItem>   mSettingItemList;
    private SwitchSettingItem       mEnableVideoItem;
    private SwitchSettingItem       mEnableAudioItem;
    private RadioButtonSettingItem  mVideoFillModeItem;
    private RadioButtonSettingItem  mMirrorTypeItem;
    private RadioButtonSettingItem  mRotationItem;
    private TipButtonSettingItem    mSnapshotItem;
    private SeekBarSettingItem      mVolumeItem;

    private RemoteUserConfig        mRemoteUserConfig = null;
    private Listener                mListener;


    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    protected void initView(@NonNull final View itemView) {
        mFrameBack = (FrameLayout) itemView.findViewById(R.id.fl_back);
        mFrameBack.setOnClickListener(this);
        mTextTitle = (TextView) itemView.findViewById(R.id.title_tv);
        mLinearContent = (LinearLayout) itemView.findViewById(R.id.item_content);

        mSettingItemList = new ArrayList<>();
        mEnableVideoItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_video), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        if (mRemoteUserConfig != null) {
                            mRemoteUserConfig.setEnableVideo(mEnableVideoItem.getChecked());
                            mTRTCRemoteUserManager.muteRemoteVideo(mRemoteUserConfig.getUserName(),
                                    mRemoteUserConfig.getStreamType(), !mRemoteUserConfig.isEnableVideo());

                            if(mEnableVideoItem.getChecked()){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mVideoFillModeItem.setSelect(mRemoteUserConfig.isFillMode() ? 0 : 1);
                                        mMirrorTypeItem.setSelect(mRemoteUserConfig.getMirrorType() == TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE ? 0 : 1);
                                        int index = 0;
                                        switch (mRemoteUserConfig.getRotation()) {
                                            case TRTC_VIDEO_ROTATION_0:
                                                index = 0;
                                                break;
                                            case TRTC_VIDEO_ROTATION_90:
                                                index = 1;
                                                break;
                                            case TRTC_VIDEO_ROTATION_180:
                                                index = 2;
                                                break;
                                            case TRTC_VIDEO_ROTATION_270:
                                                index = 3;
                                                break;
                                            default:
                                                break;
                                        }
                                        mRotationItem.setSelect(index);
                                        mVolumeItem.setProgress(mRemoteUserConfig.getVolume());
                                    }
                                });
                            }else{
                                mRemoteUserConfig.setFillMode(true);
                                mRemoteUserConfig.setRotation(0);
                                mRemoteUserConfig.setVolume(50);
                                mRemoteUserConfig.setMirrorType(TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE);
                            }
                        }
                    }
                });
        mSettingItemList.add(mEnableVideoItem);

        mEnableAudioItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_audio), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        if (mRemoteUserConfig != null) {
                            mRemoteUserConfig.setEnableAudio(mEnableAudioItem.getChecked());
                            mTRTCRemoteUserManager.muteRemoteAudio(mRemoteUserConfig.getUserName(),
                                    !mRemoteUserConfig.isEnableAudio());
                        }
                    }
                });
        mSettingItemList.add(mEnableAudioItem);

        mVideoFillModeItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_frame_fill_mode), getString(R.string.trtcdemo_frame_fill_mode_fill), getString(R.string.trtcdemo_frame_fill_mode_fit)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mRemoteUserConfig.setFillMode(index == 0);
                        mTRTCRemoteUserManager.setRemoteFillMode(mRemoteUserConfig.getUserName(),
                                mRemoteUserConfig.getStreamType(), mRemoteUserConfig.isFillMode());
                    }
                });
        mSettingItemList.add(mVideoFillModeItem);

        mMirrorTypeItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_remote_mirror), getString(R.string.trtcdemo_open), getString(R.string.trtcdemo_close)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mRemoteUserConfig.setMirrorType(index == 0 ? TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE : TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE);
                        mTRTCRemoteUserManager.setMirrorType(mRemoteUserConfig.getUserName(),
                                mRemoteUserConfig.getStreamType(), mRemoteUserConfig.getMirrorType());
                    }
                });
        mSettingItemList.add(mMirrorTypeItem);

        mRotationItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_frame_rotate), "0", "90", "180", "270"),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        if (mRemoteUserConfig == null) {
                            return;
                        }
                        int rotation = TRTC_VIDEO_ROTATION_0;
                        if (index == 1) {
                            rotation = TRTC_VIDEO_ROTATION_90;
                        } else if (index == 2) {
                            rotation = TRTC_VIDEO_ROTATION_180;
                        } else if (index == 3) {
                            rotation = TRTC_VIDEO_ROTATION_270;
                        }
                        mRemoteUserConfig.setRotation(rotation);
                        mTRTCRemoteUserManager.setRemoteRotation(mRemoteUserConfig.getUserName(),
                                mRemoteUserConfig.getStreamType(), rotation);
                    }
                });
        mSettingItemList.add(mRotationItem);

        mVolumeItem = new SeekBarSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_volume_set), ""), new SeekBarSettingItem.Listener() {
            @Override
            public void onSeekBarChange(int progress, boolean fromUser) {
                if (mRemoteUserConfig == null || !fromUser) {
                    return;
                }
                mRemoteUserConfig.setVolume(progress);
                mVolumeItem.setTips(progress + "");
                mTRTCRemoteUserManager.setRemoteVolume(mRemoteUserConfig.getUserName(), mRemoteUserConfig.getStreamType(), mRemoteUserConfig.getVolume());
            }
        }).setMix(0).setMax(100).setProgress(100).setTips("100");
        mSettingItemList.add(mVolumeItem);

        mSnapshotItem = new TipButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_video_capture), ""), new TipButtonSettingItem.OnClickListener() {
            @Override
            public void onClick() {
                if (mRemoteUserConfig != null) {
                    mTRTCRemoteUserManager.snapshotRemoteView(mRemoteUserConfig.getUserName(), mRemoteUserConfig.getStreamType());
                }
            }
        });
        mSnapshotItem.setButtonText(getString(R.string.trtcdemo_capture_image));
        mSettingItemList.add(mSnapshotItem);

        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(8), 0, 0);
            mLinearContent.addView(view);
        }

        updateView();
    }

    private void updateView() {
        if (mRemoteUserConfig == null) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mRemoteUserConfig.getStreamType() == TRTC_VIDEO_STREAM_TYPE_SUB){
                    mTextTitle.setText(mRemoteUserConfig.getUserName() + "-sub");
                }else{
                    mTextTitle.setText(mRemoteUserConfig.getUserName());
                }
                mEnableVideoItem.setCheck(mRemoteUserConfig.isEnableVideo());
                mEnableAudioItem.setCheck(mRemoteUserConfig.isEnableAudio());
                mVideoFillModeItem.setSelect(mRemoteUserConfig.isFillMode() ? 0 : 1);
                mMirrorTypeItem.setSelect(mRemoteUserConfig.getMirrorType() == TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE ? 0 : 1);
                int index = 0;
                switch (mRemoteUserConfig.getRotation()) {
                    case TRTC_VIDEO_ROTATION_0:
                        index = 0;
                        break;
                    case TRTC_VIDEO_ROTATION_90:
                        index = 1;
                        break;
                    case TRTC_VIDEO_ROTATION_180:
                        index = 2;
                        break;
                    case TRTC_VIDEO_ROTATION_270:
                        index = 3;
                        break;
                    default:
                        break;
                }
                mRotationItem.setSelect(index);
                mVolumeItem.setProgress(mRemoteUserConfig.getVolume());

                int visibility = mRemoteUserConfig.isCustomRender() ? View.GONE : View.VISIBLE;
                mVideoFillModeItem.getView().setVisibility(visibility);
                mMirrorTypeItem.getView().setVisibility(visibility);
                mRotationItem.getView().setVisibility(visibility);
                mSnapshotItem.getView().setVisibility(visibility);
            }
        });
    }

    public void setRemoteUserConfig(RemoteUserConfig remoteUserConfig) {
        mRemoteUserConfig = remoteUserConfig;
        updateView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_remote_user_setting;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fl_back) {
            if (mListener != null) {
                mListener.onBackClick();
            }
        }
    }

    public interface Listener {
        void onBackClick();
    }
}
