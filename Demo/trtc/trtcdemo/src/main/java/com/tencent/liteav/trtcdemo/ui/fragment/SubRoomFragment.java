package com.tencent.liteav.trtcdemo.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.Constant;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.listener.TRTCCloudManagerListener;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCRemoteUserManager;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.subroomitem.CreateSubRoomItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.subroomitem.SubRoomSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.videolayout.TRTCVideoLayoutManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCStatistics;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class SubRoomFragment extends BaseSettingFragment implements TRTCRemoteUserManager.IView {
    private final String                             TAG          = "SubRoomFragment";
    private       LinearLayout                       mContentItem;
    private       ArrayList<SubRoomSettingItem>      subRoomItems = new ArrayList<>();
    private       SubRoomSettingItem                 mMainSubRoomItem;
    private       HashMap<String, ArrayList<String>> mUserIds     = new HashMap<>();
    private       SubRoomSettingItem                 mCurrentPublishSubRoomItem;

    private static final int ROOM_ID_LENGTH_MAX = 9;

    @Override
    protected void initView(final View view) {
        mContentItem = (LinearLayout) view.findViewById(R.id.item_content);
        CreateSubRoomItem editMsgItem = new CreateSubRoomItem(getContext(), getString(R.string.trtcdemo_new_room), getString(R.string.trtcdemo_create)) {
            @Override
            public void send(String msg) {
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                if (msg.length() > ROOM_ID_LENGTH_MAX) {
                    msg = msg.substring(0, ROOM_ID_LENGTH_MAX);
                }
                Context context = view.getContext().getApplicationContext();
                for (SubRoomSettingItem item : subRoomItems) {
                    if (msg.equals(item.getRoomId())) {
                        Toast.makeText(context, getString(R.string.trtcdemo_room_exists), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                TRTCCloud subCloud = mTRTCCloudManager.createSubCloud();
                // 子房间会单独创建一个TRTCSubCloudManager，重构目前没有实现子房间逻辑所以会出现crash
                if (subCloud != null) {
                    createAndEnterRoom(context, subCloud, msg, false);
                } else {
                    Toast.makeText(context, getString(R.string.trtcdemo_create_sub_room_failed), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };
        mContentItem.addView(editMsgItem);

        if (subRoomItems.size() > 0) {
            mMainSubRoomItem = subRoomItems.get(0);
            for (SubRoomSettingItem item : subRoomItems) {
                mContentItem.addView(item);
                item.setPublishChecked(false);
            }
        } else {
            mMainSubRoomItem = new SubRoomSettingItem(view.getContext().getApplicationContext(),
                    mTRTCCloudManager.getParams().roomId + "", mTRTCCloudManager, null,
                    new SubRoomSettingItem.SubRoomListener() {
                        @Override
                        public void onStartPublishLocalStreams(SubRoomSettingItem item) {
                            for (SubRoomSettingItem subRoomItem : subRoomItems) {
                                if (subRoomItem != item) {
                                    subRoomItem.setPublishChecked(false);
                                }
                            }
                        }

                        @Override
                        public void onStopPublishLocalStreams(SubRoomSettingItem item) {
                        }

                        @Override
                        public void onExitRoom(SubRoomSettingItem item) {
                        }
                    });
            mMainSubRoomItem.hideExitRoomButton(true);
            mContentItem.addView(mMainSubRoomItem);
            subRoomItems.add(mMainSubRoomItem);
            mCurrentPublishSubRoomItem = mMainSubRoomItem;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }

    private void createAndEnterRoom(Context context, TRTCCloud cloud, String roomId,
                                    boolean hideExitRoomButton) {
        TRTCCloudDef.TRTCParams mainParams = mTRTCCloudManager.getParams();
        TRTCCloudDef.TRTCParams subCloudParams = new TRTCCloudDef.TRTCParams(mainParams.sdkAppId,
                mainParams.userId, mainParams.userSig, Integer.valueOf(roomId), "", "");
        subCloudParams.role = TRTCCloudDef.TRTCRoleAudience;
        TRTCCloudManager subCloudManger = new TRTCCloudManager(context, cloud, subCloudParams,
                mTRTCCloudManager.getAppScene());

        TRTCRemoteUserManager subRemoteUserManager = new TRTCRemoteUserManager(cloud, SubRoomFragment.this, false,
                TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_I420, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY,
                false);
        subRemoteUserManager.setMixUserId(mainParams.userId);
        subRemoteUserManager.setRoomId(roomId);

        SubRoomCloudManagerListenerImpl listener = new SubRoomCloudManagerListenerImpl(context,
                subCloudManger, subRemoteUserManager, mTRTCVideoLayout);
        subCloudManger.setTRTCListener(listener);

        SubRoomSettingItem item = new SubRoomSettingItem(context, roomId, subCloudManger,
                listener, new SubRoomSettingItem.SubRoomListener() {
            @Override
            public void onStartPublishLocalStreams(SubRoomSettingItem item) {
                for (SubRoomSettingItem subRoomItem : subRoomItems) {
                    if (subRoomItem != item) {
                        subRoomItem.setPublishChecked(false);
                    }
                }
            }

            @Override
            public void onStopPublishLocalStreams(SubRoomSettingItem item) {
            }

            @Override
            public void onExitRoom(SubRoomSettingItem item) {
                mContentItem.removeView(item);
                subRoomItems.remove(item);
                ArrayList<String> userIds = SubRoomFragment.this.mUserIds.get(item.getRoomId());
                if (userIds != null) {
                    for (String userId : userIds) {
                        String rendId = SubRoomFragment.getRendId(item.getRoomId(), userId);
                        mTRTCVideoLayout.recyclerCloudViewView(rendId,
                                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                        mTRTCVideoLayout.recyclerCloudViewView(rendId,
                                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                    }
                    SubRoomFragment.this.mUserIds.remove(item.getRoomId());
                }
            }
        });
        item.hideExitRoomButton(hideExitRoomButton);
        mContentItem.addView(item);
        subRoomItems.add(item);
        subCloudManger.enterRoom();
    }

    @Override
    public TXCloudVideoView getRemoteUserViewById(String roomId, String userId, int steamType) {
        String rendId = SubRoomFragment.getRendId(roomId, userId);
        TXCloudVideoView view = mTRTCVideoLayout.findCloudVideoView(rendId, steamType);
        if (view == null) {
            view = mTRTCVideoLayout.allocCloudVideoView(rendId, steamType, Constant.TRTCViewType.TYPE_SURFACE_VIEW);
        }
        return view;
    }

    @Override
    public void onRemoteViewStatusUpdate(String roomId, String userId, boolean enableVideo) {
        String rendId = SubRoomFragment.getRendId(roomId, userId);
        mTRTCVideoLayout.updateVideoStatus(rendId, enableVideo);
    }

    @Override
    public void onSnapshotRemoteView(Bitmap bm) {

    }

    @Override
    public void onStart() {
        super.onStart();
        for (SubRoomSettingItem item : subRoomItems) {
            if (mCurrentPublishSubRoomItem == item) {
                item.setPublishChecked(true);
            } else {
                item.setPublishChecked(false);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mCurrentPublishSubRoomItem = null;
        for (SubRoomSettingItem item : subRoomItems) {
            if (item.isPublishChecked()) {
                mCurrentPublishSubRoomItem = item;
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        for (SubRoomSettingItem item : subRoomItems) {
            mContentItem.removeView(item);
        }
        super.onDestroyView();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void destroyAllSubRooms() {
        for (SubRoomSettingItem subRoomItem : subRoomItems) {
            if (subRoomItem != mMainSubRoomItem) {
                subRoomItem.destroyRoom();
            }
        }
        if (mMainSubRoomItem != null && !mMainSubRoomItem.isPublishChecked()) {
            // 如果当前不是主房间推流，切回主房间推流
            mTRTCCloudManager.startPublishLocalStreams();
        }
        subRoomItems.clear();
        for (HashMap.Entry<String, ArrayList<String>> room : mUserIds.entrySet()) {
            for (String userId : room.getValue()) {
                String rendId = SubRoomFragment.getRendId(room.getKey(), userId);
                mTRTCVideoLayout.recyclerCloudViewView(rendId,
                        TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                mTRTCVideoLayout.recyclerCloudViewView(rendId,
                        TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
            }
        }
        mUserIds.clear();
    }

    private static String getRendId(String roomId, String userId) {
        return roomId + userId;
    }

    public class SubRoomCloudManagerListenerImpl implements TRTCCloudManagerListener {
        private Context                mContext               = null;
        private TRTCCloudManager       mTRTCCloudManager      = null;
        private TRTCRemoteUserManager  mTRTCRemoteUserManager = null;
        private TRTCVideoLayoutManager mTRTCVideoLayout       = null;

        public SubRoomCloudManagerListenerImpl(Context context, TRTCCloudManager cloudManager,
                                               TRTCRemoteUserManager remoteUserManager,
                                               TRTCVideoLayoutManager layoutManager) {
            mContext = context;
            mTRTCCloudManager = cloudManager;
            mTRTCRemoteUserManager = remoteUserManager;
            mTRTCVideoLayout = layoutManager;
        }

        @Override
        public void onEnterRoom(long elapsed) {
            if (elapsed >= 0) {
                Toast.makeText(mContext, getString(R.string.trtcdemo_ener_room_success_tips) + elapsed + getString(R.string.trtcdemo_mills), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, getString(R.string.trtcdemo_enter_room_fail_tips), Toast.LENGTH_SHORT).show();
                mTRTCCloudManager.exitRoom();
            }
        }

        @Override
        public void onExitRoom(int reason) {

        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Toast.makeText(mContext, "onError: " + errMsg + "[" + errCode + "]",
                    Toast.LENGTH_SHORT).show();
            mTRTCCloudManager.exitRoom();
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {

        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            mTRTCRemoteUserManager.removeRemoteUser(userId);
            // 回收分配的渲染的View
            String rendId = SubRoomFragment.getRendId(mTRTCCloudManager.getParams().roomId + "", userId);
            mTRTCVideoLayout.recyclerCloudViewView(rendId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            mTRTCVideoLayout.recyclerCloudViewView(rendId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
            ArrayList<String> userIds = mUserIds.get(mTRTCCloudManager.getParams().roomId);
            if (userIds != null) {
                userIds.remove(userId);
            }
            // 更新混流参数
            if (SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode()
                    == TRTCCloudDef.TRTC_TranscodingConfigMode_Manual) {
                mTRTCRemoteUserManager.updateCloudMixtureParams();
            }
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            onVideoChange(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, available);
        }

        @Override
        public void onUserSubStreamAvailable(String userId, boolean available) {
            onVideoChange(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB, available);
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean available) {

        }

        @Override
        public void onFirstVideoFrame(String userId, int streamType, int width, int height) {
            Log.i(TAG, "onFirstVideoFrame: userId = " + userId + " streamType = " +
                    streamType + " width = " + width + " height = " + height);
        }

        @Override
        public void onCameraDidReady() {

        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes,
                                      int totalVolume) {

        }

        @Override
        public void onStatistics(TRTCStatistics statics) {

        }

        @Override
        public void onConnectOtherRoom(String userID, int err, String errMsg) {

        }

        @Override
        public void onDisConnectOtherRoom(int err, String errMsg) {

        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality,
                                     ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
            String rendId = SubRoomFragment.getRendId(mTRTCCloudManager.getParams().roomId + "", localQuality.userId);
            mTRTCVideoLayout.updateNetworkQuality(rendId, localQuality.quality);
            for (TRTCCloudDef.TRTCQuality qualityInfo : remoteQuality) {
                mTRTCVideoLayout.updateNetworkQuality(rendId, qualityInfo.quality);
            }
        }

        @Override
        public void onAudioEffectFinished(int effectId, int code) {

        }

        @Override
        public void onRecvCustomCmdMsg(String userId, int cmdID, int seq, byte[] message) {
            String msg = "";
            if (message != null && message.length > 0) {
                try {
                    msg = new String(message, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ToastUtils.showLong(getString(R.string.trtcdemo_receive) + userId + getString(R.string.trtcdemo_message_end) + msg);
            }
        }

        @Override
        public void onRecvSEIMsg(String userId, byte[] data) {
            String msg = "";
            if (data != null && data.length > 0) {
                try {
                    msg = new String(data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ToastUtils.showLong(getString(R.string.trtcdemo_receive) + userId + getString(R.string.trtcdemo_message_end) + msg);
            }
        }

        private void onVideoChange(String userId, int streamType, boolean available) {
            String rendId = SubRoomFragment.getRendId(mTRTCCloudManager.getParams().roomId + "", userId);
            if (available) {
                // 首先需要在界面中分配对应的TXCloudVideoView
                TXCloudVideoView renderView = mTRTCVideoLayout.findCloudVideoView(rendId,
                        streamType);
                if (renderView == null) {
                    renderView = mTRTCVideoLayout.allocCloudVideoView(rendId, streamType, Constant.TRTCViewType.TYPE_SURFACE_VIEW);
                    ArrayList<String> userIds = mUserIds.get(mTRTCCloudManager.getParams().roomId);
                    if (userIds == null) {
                        userIds = new ArrayList<>();
                        mUserIds.put(mTRTCCloudManager.getParams().roomId + "", userIds);
                    }
                    userIds.add(userId);
                }
                // 启动远程画面的解码和显示逻辑
                if (renderView != null) {
                    mTRTCRemoteUserManager.remoteUserVideoAvailable(userId, streamType, renderView);
                }
            } else {
                mTRTCRemoteUserManager.remoteUserVideoUnavailable(userId, streamType);
                if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB) {
                    // 辅路直接移除画面，不会更新状态。主流需要更新状态，所以保留
                    mTRTCVideoLayout.recyclerCloudViewView(rendId,
                            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                }
            }
            if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG) {
                // 根据当前视频流的状态，展示相关的 UI 逻辑。
                mTRTCVideoLayout.updateVideoStatus(rendId, available);
            }
            if (SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode()
                    == TRTCCloudDef.TRTC_TranscodingConfigMode_Manual) {
                mTRTCRemoteUserManager.updateCloudMixtureParams();
            }
        }
    }
}
