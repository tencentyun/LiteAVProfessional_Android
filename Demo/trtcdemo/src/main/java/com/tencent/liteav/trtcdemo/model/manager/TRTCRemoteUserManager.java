package com.tencent.liteav.trtcdemo.model.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.TextureView;

import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.helper.RemoteUserConfigHelper;
import com.tencent.liteav.trtcdemo.model.bean.PkConfig;
import com.tencent.liteav.trtcdemo.model.bean.RemoteUserConfig;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.customcapture.CustomRenderVideo;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * TRTC 用户管理相关，集合了对远程用户的管理
 * 1. 通过 {@link IView} 与界面进行交互
 * 2. 通过 {@link RemoteUserConfig} 获取远程用户的配置选项并操作
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class TRTCRemoteUserManager {
    private static final String TAG = TRTCRemoteUserManager.class.getName();

    private final IView                             mIView;
    private final boolean                           mIsCustomRender;
    private final boolean                           mCustomProcessUseRenderInterface;
    private TRTCCloud                               mTRTCCloud;
    private HashMap<String, CustomRenderVideo>      mCustomRemoteRenderMap;
    private ArrayList<TRTCVideoStream>              mTRTCVideoStreams;
    private String                                  mMixUserId;
    private String                                  mRoomId = "";
    private int                                     mCustomRenderPixelFormat;
    private int                                     mCustomRenderBufferType;
    private Context                                 mContext;

    public TRTCRemoteUserManager(Context context,
                                 IView iView,
                                 boolean isCustomRender,
                                 int customRenderPixelFormat,
                                 int customRenderBufferType,
                                 boolean enableCustomProcess) {
        this.mContext = context;
        mTRTCCloud = TRTCCloud.sharedInstance(context);
        mIView = iView;
        mIsCustomRender = isCustomRender;
        mTRTCVideoStreams = new ArrayList<>();
        mCustomRemoteRenderMap = new HashMap<>();
        mCustomRenderPixelFormat = customRenderPixelFormat;
        mCustomRenderBufferType = customRenderBufferType;
        mCustomProcessUseRenderInterface = enableCustomProcess;
    }

    /**
     * 设置主播id，用在混流的时候，把主播的画面放在主画面上
     *
     * @param mixUserId
     */
    public void setMixUserId(String mixUserId) {
        mMixUserId = mixUserId;
    }

    /**
     * 设置房间 id
     *
     * @param roomId
     */
    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    /**
     * 屏蔽某个远程用户的视频
     *
     * @param userId
     * @param streamType
     * @param isMute
     */
    public void muteRemoteVideo(String userId, int streamType, boolean isMute) {
        if (isMute) {
            // mute 直接停止拉流
            mTRTCCloud.stopRemoteView(userId, streamType);
        } else {
            // 不mute重新拉流
            TXCloudVideoView view = mIView.getRemoteUserViewById(mRoomId, userId, streamType);
            // 当有辅路上行(屏幕分享）或者没打开自定义渲染的时候需要调用
            if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB || !mIsCustomRender) {
                startSDKRender(userId, streamType, view);
            } else {
                startCustomRender(userId, view);
            }
        }
    }

    /**
     * 静音某一个用户的声音
     *
     * @param userId
     * @param isMute
     */
    public void muteRemoteAudio(String userId, boolean isMute) {
        mTRTCCloud.muteRemoteAudio(userId, isMute);
    }

    public void setRemoteVideoStreamType(String userId, int type) {
        mTRTCCloud.setRemoteVideoStreamType(userId, type);
    }

    /**
     * 根据用户id和主辅流设置远端图像的渲染模式
     *
     * @param userId     用户id
     * @param streamType 主辅流
     * @param enableFill true:填充（画面可能会被拉伸裁剪） false: 适应（画面可能会有黑边）
     */
    public void setRemoteFillMode(String userId, int streamType, boolean enableFill) {
        RemoteUserConfig config = RemoteUserConfigHelper.getInstance().getRemoteUser(userId);
        config.setFillMode(enableFill);
        TRTCCloudDef.TRTCRenderParams params = new TRTCCloudDef.TRTCRenderParams();
        params.fillMode = config.isFillMode() ? TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL : TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
        params.rotation =  config.getRotation();
        params.mirrorType = config.getMirrorType();
        mTRTCCloud.setRemoteRenderParams(userId, streamType, params);
    }

    /**
     * 设置远端图像的顺时针旋转角度 {@link TRTCCloud#setRemoteViewRotation }
     */
    public void setRemoteRotation(String userId, int streamType, int rotation) {
        RemoteUserConfig config = RemoteUserConfigHelper.getInstance().getRemoteUser(userId);
        config.setRotation(rotation);
        TRTCCloudDef.TRTCRenderParams params = new TRTCCloudDef.TRTCRenderParams();
        params.fillMode = config.isFillMode() ? TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL : TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
        params.rotation =  rotation;
        params.mirrorType = config.getMirrorType();
        mTRTCCloud.setRemoteRenderParams(userId, streamType, params);
    }

    public void setMirrorType(String userId, int streamType, int mirrorType) {
        RemoteUserConfig config = RemoteUserConfigHelper.getInstance().getRemoteUser(userId);
        config.setMirrorType(mirrorType);
        TRTCCloudDef.TRTCRenderParams params = new TRTCCloudDef.TRTCRenderParams();
        params.fillMode = config.isFillMode() ? TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL : TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
        params.rotation =  config.getRotation();
        params.mirrorType = config.getMirrorType();
        mTRTCCloud.setRemoteRenderParams(userId, streamType, params);
    }

    /**
     * 截取远端视频画面
     *
     * @param userId
     */
    public void snapshotRemoteView(String userId, int streamType) {
        mTRTCCloud.snapshotVideo(userId, streamType, new TRTCCloudListener.TRTCSnapshotListener() {
            @Override
            public void onSnapshotComplete(Bitmap bmp) {
                if (mIView != null) {
                    mIView.onSnapshotRemoteView(bmp);
                }
            }
        });
    }

    /**
     * 设置某个远程用户的播放音量
     * {@link TRTCCloud#setRemoteAudioVolume }
     */
    public void setRemoteVolume(String userId, int streamType, int volume) {
        mTRTCCloud.setRemoteAudioVolume(userId, volume);
    }

    /**
     * 界面退出时调用
     */
    public void destroy() {
        if (mCustomRemoteRenderMap != null) {
            for (CustomRenderVideo render : mCustomRemoteRenderMap.values()) {
                if (render != null) {
                    render.stop();
                }
            }
            mCustomRemoteRenderMap.clear();
        }
    }

    private boolean isContainVideoStream(String userId, int streamType) {
        for (TRTCVideoStream stream : mTRTCVideoStreams) {
            if (stream != null && stream.userId != null && stream.userId.equals(userId) && stream.streamType == streamType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移除一条视频流
     */
    private void removeVideoStream(String userId, int streamType) {
        Iterator<TRTCVideoStream> streamIterator = mTRTCVideoStreams.iterator();
        while (streamIterator.hasNext()) {
            TRTCVideoStream stream = streamIterator.next();
            if (stream.userId != null && stream.userId.equals(userId) && stream.streamType == streamType) {
                streamIterator.remove();
                TXLog.i(TAG, "removeVideoStream " + userId + ", stream " + streamType + ", size " + mTRTCVideoStreams.size());
                break;
            }
        }
    }

    /**
     * 远程用户关闭了视频流，停止界面的渲染，并移除一条视频流
     */
    public void remoteUserVideoUnavailable(String userId, int streamType) {
        if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB || !mIsCustomRender) {
            stopSDKRender(userId, streamType);
        } else {
            stopCustomRender(userId);
        }
        removeVideoStream(userId, streamType);
    }

    /**
     * 远程用户打开了视频流，开始界面的渲染，并添加一条视频流
     */
    public void remoteUserVideoAvailable(String userId, int streamType, TXCloudVideoView renderView) {
        TRTCVideoStream stream = new TRTCVideoStream();
        stream.userId = userId;
        stream.streamType = streamType;

        // 增加成员
        RemoteUserConfig remoteUserConfig = RemoteUserConfigHelper.getInstance().getRemoteUser(userId, streamType);
        if (remoteUserConfig == null) {
            remoteUserConfig = new RemoteUserConfig(userId, streamType, mIsCustomRender);
            if(streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB){
                remoteUserConfig.setFillMode(true);
                remoteUserConfig.setRotation(0);
                remoteUserConfig.setVolume(50);
                remoteUserConfig.setMirrorType(TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE);
            }
            RemoteUserConfigHelper.getInstance().addRemoteUser(remoteUserConfig);
        }else{
            if(streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB){
                remoteUserConfig.setFillMode(true);
                remoteUserConfig.setRotation(0);
                remoteUserConfig.setVolume(50);
                remoteUserConfig.setMirrorType(TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_DISABLE);
            }
        }

        // 如果用户勾选了打开远端视频
        if (remoteUserConfig.isEnableVideo()) {
            if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB || !mIsCustomRender) {
                startSDKRender(userId, streamType, renderView);
            } else {
                startCustomRender(userId, renderView);
            }
        }

        // 记录当前流的类型，以供设置混流参数时使用：
        if (!isContainVideoStream(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG)) {
            mTRTCVideoStreams.add(stream);
            TXLog.i(TAG, "remoteUserVideoAvailable " + stream.userId +
                    ", stream " + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG +
                    ", size " + mTRTCVideoStreams.size());
        }
    }

    /**
     * 有人退出了直播间，需要停止渲染并移除所有相关的界面
     *
     * @param userId
     */
    public void removeRemoteUser(String userId) {
        RemoteUserConfigHelper.getInstance().removeRemoteUser(userId);
        stopCustomRender(userId);
        mTRTCCloud.stopRemoteView(userId);
        mTRTCCloud.stopRemoteSubStreamView(userId);
        // 连麦主播退出房间，需要将相关参数置为空；否则会导致混流异常
        PkConfig pkConfig = SettingConfigHelper.getInstance().getPkConfig();
        if (userId.equals(pkConfig.getConnectUserName())) {
            pkConfig.reset();
        }
    }

    /**
     * 有人退出了直播间，需要停止渲染并移除所有相关的界面
     *
     * @param userId
     */
    public void removeRemoteUser(String userId, int streamType) {
        RemoteUserConfigHelper.getInstance().removeRemoteUser(userId, streamType);
        stopCustomRender(userId);
        mTRTCCloud.stopRemoteView(userId, streamType);
        mTRTCCloud.stopRemoteSubStreamView(userId);
        // 连麦主播退出房间，需要将相关参数置为空；否则会导致混流异常
        PkConfig pkConfig = SettingConfigHelper.getInstance().getPkConfig();
        if (userId.equals(pkConfig.getConnectUserName())) {
            pkConfig.reset();
        }
    }

    /**
     * 启动渲染
     *
     * @param userId
     * @param streamType
     * @param renderView
     */
    private void startSDKRender(String userId, int streamType, TXCloudVideoView renderView) {
        // 启动远程画面的解码和显示逻辑
        if (renderView != null) {
            TRTCCloudDef.TRTCRenderParams params = new TRTCCloudDef.TRTCRenderParams();
            params.fillMode = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
            mTRTCCloud.setRemoteRenderParams(userId, streamType, params);
            mTRTCCloud.startRemoteView(userId, streamType, renderView);
        }
    }

    /**
     * 停止sdk渲染
     *
     * @param userId
     * @param streamType
     */
    private void stopSDKRender(String userId, int streamType) {
        // 停止渲染
        if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG) {
            mTRTCCloud.stopRemoteView(userId);
        } else if (streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB) {
            mTRTCCloud.stopRemoteSubStreamView(userId);
        }
    }

    /**
     * 开始自定义渲染
     *
     * @param userId
     * @param renderView
     */
    private void startCustomRender(String userId, TXCloudVideoView renderView) {
        // 创建自定义渲染类
        CustomRenderVideo customRender = new CustomRenderVideo(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                mCustomProcessUseRenderInterface);
        // 创建渲染的View
        TextureView textureView = new TextureView(renderView.getContext());
        // 添加到父布局
        renderView.addVideoView(textureView);
        // 同调 SDK api：绑定 userId 与自定义渲染器 customRender
        mTRTCCloud.setRemoteVideoRenderListener(userId, mCustomRenderPixelFormat, mCustomRenderBufferType, customRender);
        // 自定义渲染器与渲染的 View 绑定
        customRender.start(textureView);
        // 存储记录
        mCustomRemoteRenderMap.put(userId, customRender);
        // 调用 SDK api 开始渲染（view 传 null）
        mTRTCCloud.startRemoteView(userId, null);
    }

    /**
     * 停止自定义渲染
     *
     * @param userId
     */
    private void stopCustomRender(String userId) {
        // 停止自定义渲染
        CustomRenderVideo render = mCustomRemoteRenderMap.remove(userId);
        if (render != null) {
            render.stop();
        }
        // 移除自定义渲染的 View
        mTRTCCloud.stopRemoteSubStreamView(userId);
    }

    public void muteInSpeaderAudio(String userId, boolean isMute) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "muteRemoteAudioInSpeaker");
            JSONObject params = new JSONObject();
            params.put("userID", userId);
            params.put("mute", isMute ? 1 : 0);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopCloudMixture() {
        SettingConfigHelper.getInstance().getVideoConfig().setCurIsMix(false);
        mTRTCCloud.setMixTranscodingConfig(null);
    }

    /**
     * 更新混流参数
     */
    public void updateCloudMixtureParams() {
        int mode = SettingConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode();
        switch (mode) {
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Unknown:
                SettingConfigHelper.getInstance().getVideoConfig().setCurIsMix(false);
                mTRTCCloud.setMixTranscodingConfig(null);
                break;
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Manual:
                updateCloudMixtureManual();
                SettingConfigHelper.getInstance().getVideoConfig().setCurIsMix(true);
                break;
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PureAudio:
                updateCloudMixturePureAudio();
                SettingConfigHelper.getInstance().getVideoConfig().setCurIsMix(true);
                break;
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PresetLayout:
                updateCloudMixturePresetLayout();
                SettingConfigHelper.getInstance().getVideoConfig().setCurIsMix(true);
                break;
        }
    }

    /**
     * 界面相关回调
     */
    public interface IView {
        TXCloudVideoView getRemoteUserViewById(String roomId, String userId, int steamType);

        void onRemoteViewStatusUpdate(String roomId, String userId, boolean enable);

        void onSnapshotRemoteView(Bitmap bm);
    }

    /**
     * 混流相关参数
     */
    private static class TRTCVideoStream {
        public String userId;
        public int    streamType;
    }

    private void updateCloudMixtureManual() {
        ArrayList<TRTCVideoStream> local = new ArrayList<>();
        TRTCVideoStream user = new TRTCVideoStream();
        user.userId = mMixUserId;
        user.streamType = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
        local.add(user);
        updateCloudMixture(TRTCCloudDef.TRTC_TranscodingConfigMode_Manual, local, mTRTCVideoStreams);
    }

    private void updateCloudMixturePureAudio() {
        TRTCCloudDef.TRTCTranscodingConfig config = new TRTCCloudDef.TRTCTranscodingConfig();
        config.mode = TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PureAudio;
        ///【字段含义】腾讯云直播 AppID
        ///【推荐取值】请在 [实时音视频控制台](https://console.cloud.tencent.com/rav) 选择已经创建的应用，单击【帐号信息】后，在“直播信息”中获取
        config.appId = GenerateTestUserSig.APPID;
        ///【字段含义】腾讯云直播 bizid
        ///【推荐取值】请在 [实时音视频控制台](https://console.cloud.tencent.com/rav) 选择已经创建的应用，单击【帐号信息】后，在“直播信息”中获取
        config.bizId = GenerateTestUserSig.BIZID;
        config.audioSampleRate = 48000;
        config.audioBitrate = 64;
        config.audioChannels = 1;
        config.streamId = SettingConfigHelper.getInstance().getVideoConfig().getMixStreamId();

        mTRTCCloud.setMixTranscodingConfig(config);
    }

    private void updateCloudMixturePresetLayout() {
        ArrayList<TRTCVideoStream> local = new ArrayList<>();
        TRTCVideoStream user = new TRTCVideoStream();
        user.userId = "$PLACE_HOLDER_LOCAL_MAIN$";
        user.streamType = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
        local.add(user);

        ArrayList<TRTCVideoStream> remote = new ArrayList<>();

        user = new TRTCVideoStream();
        user.userId = "$PLACE_HOLDER_LOCAL_SUB$";
        user.streamType = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB;
        remote.add(user);

        for (int index=0;index < 6;++index) {
            TRTCVideoStream stream = new TRTCVideoStream();
            stream.userId = "$PLACE_HOLDER_REMOTE$";
            stream.streamType = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
            remote.add(stream);
        }
        updateCloudMixture(TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PresetLayout, local, remote);
    }

    private void updateCloudMixture(int mode, ArrayList<TRTCVideoStream> localList, ArrayList<TRTCVideoStream> remoteList) {
        // 背景大画面宽高
        int videoWidth  = 720;
        int videoHeight = 1280;

        // 小画面宽高
        int subWidth  = 180;
        int subHeight = 320;

        int offsetX = 5;
        int offsetY = 50;

        int bitrate = 200;
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        int resolution = videoConfig.getMainStreamVideoResolution();
        switch (resolution) {
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_160_160: {
                videoWidth = 160;
                videoHeight = 160;
                subWidth = 32;
                subHeight = 48;
                offsetY = 10;
                bitrate = 200;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_180: {
                videoWidth = 192;
                videoHeight = 336;
                subWidth = 54;
                subHeight = 96;
                offsetY = 30;
                bitrate = 400;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_240: {
                videoWidth = 240;
                videoHeight = 320;
                subWidth = 54;
                subHeight = 96;
                offsetY = 30;
                bitrate = 400;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_480_480: {
                videoWidth = 480;
                videoHeight = 480;
                subWidth = 72;
                subHeight = 128;
                bitrate = 600;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360: {
                videoWidth = 368;
                videoHeight = 640;
                subWidth = 90;
                subHeight = 160;
                bitrate = 800;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_480: {
                videoWidth = 480;
                videoHeight = 640;
                subWidth = 90;
                subHeight = 160;
                bitrate = 800;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_960_540: {
                videoWidth = 544;
                videoHeight = 960;
                subWidth = 160;
                subHeight = 288;
                bitrate = 1000;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720: {
                videoWidth = 720;
                videoHeight = 1280;
                subWidth = 192;
                subHeight = 336;
                bitrate = 1500;
                break;
            }
        }

        TRTCCloudDef.TRTCTranscodingConfig config = new TRTCCloudDef.TRTCTranscodingConfig();
        config.mode = mode;
        ///【字段含义】腾讯云直播 AppID
        ///【推荐取值】请在 [实时音视频控制台](https://console.cloud.tencent.com/rav) 选择已经创建的应用，单击【帐号信息】后，在“直播信息”中获取
        config.appId = GenerateTestUserSig.APPID;
        ///【字段含义】腾讯云直播 bizid
        ///【推荐取值】请在 [实时音视频控制台](https://console.cloud.tencent.com/rav) 选择已经创建的应用，单击【帐号信息】后，在“直播信息”中获取
        config.bizId = GenerateTestUserSig.BIZID;
        config.videoWidth = videoWidth;
        config.videoHeight = videoHeight;
        config.videoGOP = 1;
        config.videoFramerate = 15;
        config.videoBitrate = bitrate;
        config.audioSampleRate = 48000;
        config.audioBitrate = 64;
        config.audioChannels = 1;
        config.backgroundImage = videoConfig.getMixStreamBackgroundImage();
        config.streamId = videoConfig.getMixStreamId();

        config.mixUsers = new ArrayList<>();

        for (TRTCVideoStream userStream : localList) {
            // 设置混流后主播的画面位置
            TRTCCloudDef.TRTCMixUser mixUser = new TRTCCloudDef.TRTCMixUser();
            mixUser.userId = userStream.userId;
            mixUser.streamType = userStream.streamType;
            mixUser.zOrder = 1;
            mixUser.x = 0;
            mixUser.y = 0;
            if (TextUtils.isEmpty(config.backgroundImage)) {
                mixUser.width = videoWidth;
                mixUser.height = videoHeight;
            } else {
                mixUser.width = videoWidth / 2;
                mixUser.height = videoHeight /2;
            }
            config.mixUsers.add(mixUser);
        }


        // 设置混流后各个小画面的位置
        int index = 0;
        TXLog.i(TAG, "updateCloudMixtureParams " + remoteList.size());
        for (TRTCVideoStream userStream : remoteList) {
            TRTCCloudDef.TRTCMixUser _mixUser = new TRTCCloudDef.TRTCMixUser();
            PkConfig                 pkConfig = SettingConfigHelper.getInstance().getPkConfig();
            if (pkConfig.isConnected() && userStream.userId.equalsIgnoreCase(pkConfig.getConnectUserName())) {
                _mixUser.roomId = pkConfig.getConnectRoomId();
            }

            _mixUser.userId = userStream.userId;
            _mixUser.streamType = userStream.streamType;
            _mixUser.zOrder = 2 + index;
            if (index < 3) {
                // 前三个小画面靠右从下往上铺
                _mixUser.x = videoWidth - offsetX - subWidth;
                _mixUser.y = videoHeight - offsetY - index * subHeight - subHeight;
                _mixUser.width = subWidth;
                _mixUser.height = subHeight;
            } else if (index < 6) {
                // 后三个小画面靠左从下往上铺
                _mixUser.x = offsetX;
                _mixUser.y = videoHeight - offsetY - (index - 3) * subHeight - subHeight;
                _mixUser.width = subWidth;
                _mixUser.height = subHeight;
            } else {
                // 最多只叠加六个小画面
            }
            TXLog.i(TAG, "updateCloudMixtureParams userId " + _mixUser.userId);
            config.mixUsers.add(_mixUser);
            ++index;
        }

        mTRTCCloud.setMixTranscodingConfig(config);
    }
}
