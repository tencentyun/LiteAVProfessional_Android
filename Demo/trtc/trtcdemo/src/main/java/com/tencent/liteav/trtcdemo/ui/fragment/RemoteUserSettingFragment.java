package com.tencent.liteav.trtcdemo.ui.fragment;


import androidx.annotation.NonNull;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.RemoteUserConfig;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteEnableAudioItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteAudioParallelMustPlayItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteEnableVideoItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteMirrorTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteRotationItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteSnapshotItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteStreamTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteVideoFillModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.remoteitem.RemoteVolumeItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_180;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_270;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_ROTATION_90;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB;

/**
 * 用户列表管理界面
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class RemoteUserSettingFragment extends BaseSettingFragment implements View.OnClickListener {

    private FrameLayout                     mFrameBack;
    private TextView                        mTextTitle;
    private LinearLayout                    mLinearContent;
    private List<View>                      mSettingItemList;
    private RemoteEnableVideoItem           mEnableVideoItem;
    private RemoteAudioParallelMustPlayItem mAudioParallelMustPlayItem;
    private RemoteEnableAudioItem           mEnableAudioItem;
    private RemoteStreamTypeItem            mStreamTypeItem;
    private RemoteVideoFillModeItem         mVideoFillModeItem;
    private RemoteMirrorTypeItem            mMirrorTypeItem;
    private RemoteRotationItem              mRotationItem;
    private RemoteVolumeItem                mVolumeItem;
    private RemoteSnapshotItem              mSnapshotItem;
    private RemoteUserConfig                mRemoteUserConfig = null;
    private Listener                        mListener;


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
        mEnableVideoItem = new RemoteEnableVideoItem(new RemoteEnableVideoItem.EnableViewListener() {
            @Override
            public void onClicked() {
                if (mRemoteUserConfig != null) {
                    mRemoteUserConfig.setEnableVideo(mEnableVideoItem.getChecked());
                    mTRTCRemoteUserManager.muteRemoteVideo(mRemoteUserConfig.getUserName(),
                            mRemoteUserConfig.getStreamType(), !mRemoteUserConfig.isEnableVideo());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_open_video));
        mSettingItemList.add(mEnableVideoItem);

        mAudioParallelMustPlayItem = getAudioParallelMustPlayItem();
        mSettingItemList.add(mAudioParallelMustPlayItem);

        mEnableAudioItem = new RemoteEnableAudioItem(new RemoteEnableAudioItem.EnableViewListener() {
            @Override
            public void onClicked() {
                if (mRemoteUserConfig != null) {
                    mRemoteUserConfig.setEnableAudio(mEnableAudioItem.getChecked());
                    mTRTCRemoteUserManager.muteRemoteAudio(mRemoteUserConfig.getUserName(),
                            !mRemoteUserConfig.isEnableAudio());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_open_audio));
        mSettingItemList.add(mEnableAudioItem);
    
    
        mStreamTypeItem = new RemoteStreamTypeItem(new RemoteStreamTypeItem.StreamTypeItemListener() {
            @Override
            public void onSelected(int index) {
                if (mRemoteUserConfig != null) {
                    mRemoteUserConfig.setStreamType(index == 0 ?
                            TRTC_VIDEO_STREAM_TYPE_BIG : TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SMALL);
                    mTRTCRemoteUserManager.setRemoteVideoStreamType(mRemoteUserConfig.getUserName(), mRemoteUserConfig.getStreamType());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_see_type), getString(R.string.trtcdemo_big_stream), getString(R.string.trtcdemo_small_stream));
        mSettingItemList.add(mStreamTypeItem);

        mVideoFillModeItem = new RemoteVideoFillModeItem(new RemoteVideoFillModeItem.VideoFillModeListener() {
            @Override
            public void onSelected(int index) {
                if (mRemoteUserConfig != null) {
                    mRemoteUserConfig.setFillMode(index == 0);
                    mTRTCRemoteUserManager.setRemoteFillMode(mRemoteUserConfig.getUserName(),
                            mRemoteUserConfig.getStreamType(), mRemoteUserConfig.isFillMode());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_frame_fill_mode), getString(R.string.trtcdemo_frame_fill_mode_fill), getString(R.string.trtcdemo_frame_fill_mode_fit));
        mSettingItemList.add(mVideoFillModeItem);

        mMirrorTypeItem = new RemoteMirrorTypeItem(new RemoteMirrorTypeItem.MirrorTypeListener() {
            @Override
            public void onSelected(int index) {
                if (mRemoteUserConfig != null) {
                    mRemoteUserConfig.setMirrorType(index == 0 ? TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_ENABLE : TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE);
                    mTRTCRemoteUserManager.setMirrorType(mRemoteUserConfig.getUserName(),
                            mRemoteUserConfig.getStreamType(), mRemoteUserConfig.getMirrorType());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_remote_mirror), getString(R.string.trtcdemo_open), getString(R.string.trtcdemo_close));
        mSettingItemList.add(mMirrorTypeItem);

        mRotationItem = new RemoteRotationItem(new RemoteRotationItem.RotationListener() {
            @Override
            public void onSelected(int index) {
                if (mRemoteUserConfig != null) {
                    int rotation = RemoteRotationItem.getRotationByIndex(index);
                    mRemoteUserConfig.setRotation(rotation);
                    mTRTCRemoteUserManager.setRemoteRotation(mRemoteUserConfig.getUserName(),
                            mRemoteUserConfig.getStreamType(), rotation);
                }
            }
        }, getContext(), getString(R.string.trtcdemo_frame_rotate), "0", "90", "180", "270");
        mSettingItemList.add(mRotationItem);

        mVolumeItem = new RemoteVolumeItem(new RemoteVolumeItem.VolumeItemListener() {
            @Override
            public void onSeekChange(int progress, boolean fromUser) {
                if (mRemoteUserConfig == null || !fromUser) {
                    return;
                }
                mRemoteUserConfig.setVolume(progress);
                mVolumeItem.setTip(progress + "");
                mTRTCRemoteUserManager.setRemoteVolume(mRemoteUserConfig.getUserName(), mRemoteUserConfig.getStreamType(), mRemoteUserConfig.getVolume());

            }
        }, getContext(), getString(R.string.trtcdemo_volume_set), "100");
        mVolumeItem.setMix(0).setMax(100).setProgress(100);
        mSettingItemList.add(mVolumeItem);

        mSnapshotItem = new RemoteSnapshotItem(new RemoteSnapshotItem.SnapshotListener() {
            @Override
            public void onClick() {
                if (mRemoteUserConfig != null) {
                    mTRTCRemoteUserManager.snapshotRemoteView(mRemoteUserConfig.getUserName(), mRemoteUserConfig.getStreamType());
                }
            }
        }, getContext(), getString(R.string.trtcdemo_video_capture), getString(R.string.trtcdemo_capture_image));
        mSettingItemList.add(mSnapshotItem);

        for (View item : mSettingItemList) {
            mLinearContent.addView(item);
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
                if (mRemoteUserConfig.getStreamType() == TRTC_VIDEO_STREAM_TYPE_SUB) {
                    mTextTitle.setText(mRemoteUserConfig.getUserName() + "-sub");
                } else if (mRemoteUserConfig.getStreamType() == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SMALL) {
                    mTextTitle.setText(mRemoteUserConfig.getUserName() + "-small");
                } else {
                    mTextTitle.setText(mRemoteUserConfig.getUserName());
                }
                mEnableVideoItem.setCheck(mRemoteUserConfig.isEnableVideo());
                mAudioParallelMustPlayItem.setCheck(mRemoteUserConfig.isAudioParallelMustPlay());
                mEnableAudioItem.setCheck(mRemoteUserConfig.isEnableAudio());
                mStreamTypeItem.setSelect(mRemoteUserConfig.getStreamType() == TRTC_VIDEO_STREAM_TYPE_BIG ? 0 : 1);
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
                mVolumeItem.setTip(mRemoteUserConfig.getVolume() + "");
                int visibility = mRemoteUserConfig.isCustomRender() ? View.GONE : View.VISIBLE;
                mVideoFillModeItem.setVisibility(visibility);
                mMirrorTypeItem.setVisibility(visibility);
                mRotationItem.setVisibility(visibility);
                mSnapshotItem.setVisibility(visibility);
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

    private RemoteAudioParallelMustPlayItem getAudioParallelMustPlayItem() {
        return new RemoteAudioParallelMustPlayItem(
                new RemoteAudioParallelMustPlayItem.ParallelMustPlayListener() {
                    @Override
                    public void onClicked() {
                        if (mRemoteUserConfig != null) {
                            boolean isChecked = mAudioParallelMustPlayItem.getChecked();
                            boolean isSuccess = mTRTCRemoteUserManager.setRemoteAudioParallelParams(
                                    mRemoteUserConfig.getUserName(), isChecked);
                            mRemoteUserConfig.setAudioParallelMustPlay(isSuccess ? isChecked : !isChecked);

                            mAudioParallelMustPlayItem.setCheck(mRemoteUserConfig.isAudioParallelMustPlay());
                        }
                    }
                }, getContext(), getString(R.string.trtcdemo_audio_parallel_must_play));
    }
}
