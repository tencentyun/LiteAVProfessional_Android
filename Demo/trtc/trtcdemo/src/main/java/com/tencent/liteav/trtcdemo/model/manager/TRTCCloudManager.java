package com.tencent.liteav.trtcdemo.model.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.AudioConfig;
import com.tencent.liteav.trtcdemo.model.bean.MoreConfig;
import com.tencent.liteav.trtcdemo.model.bean.PkConfig;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.model.filter.TimeWaterMarkFilter;
import com.tencent.liteav.trtcdemo.model.helper.RemoteUserConfigHelper;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.listener.TRTCCloudListenerImpl;
import com.tencent.liteav.trtcdemo.model.listener.TRTCCloudManagerListener;
import com.tencent.liteav.trtcdemo.model.opengl.OpenGlUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 封装了 TRTCCloud 的基本功能，方便直接调用
 * 1. 通过 {@link IView} 接口和界面进行联系，如果您编写的界面需要监听sdk的一些变化，可以通过 {@link TRTCCloudManager#setViewListener}
 * 设置 listener
 * 2. 很多设置参数通过 {@link SettingConfigHelper} 获取，您可以改变 {@link SettingConfigHelper} 中的每个设置选项来达到修改预设参数的
 * 目的
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class TRTCCloudManager {
    private static final String TAG = "TRTCCloudManager";

    public  String                        mTestRecordAACPath;
    private Context                       mContext;
    private TRTCCloud                     mTRTCCloud;                 // SDK 核心类
    private TRTCCloudDef.TRTCParams       mTRTCParams;                // 进房参数
    private int                           mAppScene;                  // 推流模式
    private TRTCCloudDef.TRTCRenderParams mTRTCRenderParams;
    private boolean                       mIsFontCamera      = true;       // 是否用前置摄像头
    private int                           mMsgCmdIndex       = 0;
    private IView                         mIView;                    //界面回调相关
    public  int                           mVolumeType        = -1;
    public  boolean                       mUse265Encode      = false;
    public  int                           mEncoderType       = 2;
    // 本地预览窗口
    private TXCloudVideoView              mLocalPreviewView;
    public  String                        mBindAudioFrameMsg = null;

    public TRTCCloudManager(Context context, TRTCCloudDef.TRTCParams trtcParams, int appScene) {
        this(context, TRTCCloud.sharedInstance(context), trtcParams, appScene);
    }

    public TRTCCloudManager(Context context, TRTCCloud cloud, TRTCCloudDef.TRTCParams trtcParams, int appScene) {
        mContext = context;
        mTRTCCloud = cloud;
        mTRTCParams = trtcParams;
        mAppScene = appScene;
        mTRTCRenderParams = new TRTCCloudDef.TRTCRenderParams();
        mTestRecordAACPath = createFilePath();
    }

    private String createFilePath() {
        try {
            File sdcardDir = mContext.getExternalFilesDir(null);
            if (sdcardDir == null) {
                return null;
            }

            String dirPath = sdcardDir.getAbsolutePath() + "/test/record/";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "record.aac");

            file.delete();
            file.createNewFile();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String createMediaFilePath() {
        File sdcardDir = mContext.getExternalFilesDir(null);
        if (sdcardDir == null) {
            return null;
        }

        String dirPath = sdcardDir.getAbsolutePath() + "/test/record/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "record.mp4");

        file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public void destroy() {
        mTRTCCloud.setListener(null);
        SettingConfigHelper.getInstance().getPkConfig().reset();
        SettingConfigHelper.getInstance().getAudioConfig().reset();
        SettingConfigHelper.getInstance().getMoreConfig().reset();
        SettingConfigHelper.getInstance().getVideoConfig().reset();
    }

    public void setViewListener(IView IView) {
        mIView = IView;
    }

    public void setTRTCListener(TRTCCloudManagerListener trtcCloudListener) {
        mTRTCCloud.setListener(new TRTCCloudListenerImpl(trtcCloudListener));
    }

    /**
     * 对进房的设置进行初始化
     *
     * @param isCustomCapture       是否为自采集，开启该模式，SDK 只保留编码和发送能力
     * @param isReceivedAudio       是否自动接收音频
     * @param isReceivedVideo       是否自动接收视频
     * @param enablePerformanceMode 是否开启性能模式
     */
    public void initTRTCManager(boolean isCustomCapture, boolean isReceivedAudio, boolean isReceivedVideo,
                                boolean enablePerformanceMode) {
        // 是否为自采集，请在调用 SDK 相关配置前优先设置好，避免时序导致的异常问题。
        if (isCustomCapture) {
            mTRTCCloud.enableCustomVideoCapture(isCustomCapture);
            mTRTCCloud.enableCustomAudioCapture(isCustomCapture);
        }

        // 对接收模式进行操作
        mTRTCCloud.setDefaultStreamRecvMode(isReceivedAudio, isReceivedVideo);

        // 设置美颜参数
        mTRTCCloud.setBeautyStyle(TRTCCloudDef.TRTC_BEAUTY_STYLE_SMOOTH, 5, 5, 5);

        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        // 设置视频渲染模式
        setVideoFillMode(videoConfig.isVideoFillMode());

        // 设置视频旋转角
        setLocalVideoRotation(videoConfig.getLocalRotation());

        MoreConfig moreConfig = SettingConfigHelper.getInstance().getMoreConfig();
        // 是否开启重力感应
        enableGSensor(moreConfig.isEnableGSensorMode());

        // 是否开启推流画面镜像
        enableVideoEncMirror(videoConfig.isRemoteMirror());

        // 设置本地画面是否镜像预览
        setLocalViewMirror(videoConfig.getMirrorType());

        // 是否开启水印
        enableWatermark(videoConfig.isWatermark());

        // 【关键】设置 TRTC 推流参数
        setTRTCCloudParam();

        setVideoEncoderRotation(videoConfig.getRemoteRotation());

        mTRTCCloud.callExperimentalAPI(String.format(Locale.ENGLISH, "{\"api\":\"setPerformanceMode\","
                        + "\"params\": {\"mode\":%d}}",
                enablePerformanceMode ? 1 : 0));
        mTRTCCloud.callExperimentalAPI("{\"api\":\"setSystemAudioKitEnabled\"}");
    }

    public boolean isFontCamera() {
        return mIsFontCamera;
    }

    public void enterRoom() {
        setSystemVolumeType(mVolumeType);
        mTRTCCloud.callExperimentalAPI(String.format(Locale.ENGLISH, "{\"api\""
                        + ":\"setEncodedDataProcessingListener\", \"params\": {\"listener\":%d}}",
                TRTCCustomerCrypt.sharedInstance().getEncodedDataProcessingListener()));
        mTRTCCloud.callExperimentalAPI(String.format(Locale.ENGLISH, "{\"api\""
                        + ":\"setAudioPacketExtraDataListener\", \"params\": {\"listener\":%d}}",
                TRTCAudioPkgListener.getInstance().getCustomerAudioPkgListener()));
        mTRTCCloud.enterRoom(mTRTCParams, mAppScene);
        mTRTCCloud.setAudioFrameListener(new TRTCCloudListener.TRTCAudioFrameListener() {
            @Override
            public void onCapturedRawAudioFrame(TRTCCloudDef.TRTCAudioFrame frame) {

            }

            @Override
            public void onLocalProcessedAudioFrame(TRTCCloudDef.TRTCAudioFrame frame) {
                synchronized (TRTCCloudManager.this) {
                    if (!TextUtils.isEmpty(mBindAudioFrameMsg)) {
                        frame.extraData = mBindAudioFrameMsg.getBytes();
                        mBindAudioFrameMsg = null;
                    }
                }
            }

            @Override
            public void onRemoteUserAudioFrame(TRTCCloudDef.TRTCAudioFrame frame, String userId) {
                byte[] data = frame.extraData;
                String msg = "";
                if (data != null && data.length > 0) {
                    try {
                        msg = new String(data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    ToastUtils.showLong("收到" + userId + "的音频消息：" + msg);
                }
            }

            @Override
            public void onMixedPlayAudioFrame(TRTCCloudDef.TRTCAudioFrame frame) {

            }

            @Override
            public void onMixedAllAudioFrame(TRTCCloudDef.TRTCAudioFrame frame) {

            }
        });
    }

    /**
     * 设置 TRTC 推流参数
     */
    public void setTRTCCloudParam() {
        setBigSteam();
        setQosParam();
        setSmallSteam();
        setSubSteam();
        setVideoEncodeParamEx();
    }

    public void setBigSteam() {
        // 大画面的编码器参数设置
        // 设置视频编码参数，包括分辨率、帧率、码率等等，这些编码参数来自于 videoConfig 的设置
        // 注意（1）：不要在码率很低的情况下设置很高的分辨率，会出现较大的马赛克
        // 注意（2）：不要设置超过25FPS以上的帧率，因为电影才使用24FPS，我们一般推荐15FPS，这样能将更多的码率分配给画质
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = videoConfig.getMainStreamVideoResolution();
        encParam.videoFps = videoConfig.getMainStreamVideoFps();
        encParam.videoBitrate = videoConfig.getMainStreamVideoBitrate();
        encParam.videoResolutionMode = videoConfig.isMainStreamVideoVertical()
                ? TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT : TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;
        if (mAppScene == TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL) {
            encParam.enableAdjustRes = true;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "enableHevcEncode");
            JSONObject paramsObject = new JSONObject();
            jsonObject.put("params", paramsObject);
            paramsObject.put("enable", mUse265Encode ? 1 : 0);
        } catch (JSONException e) {
            Log.e(TAG, "json exception: " + e.getMessage());
        }
        mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        mTRTCCloud.setVideoEncoderParam(encParam);
    }

    public void setSubSteam() {
        // 大画面的编码器参数设置
        // 设置视频编码参数，包括分辨率、帧率、码率等等，这些编码参数来自于 videoConfig 的设置
        // 注意（1）：不要在码率很低的情况下设置很高的分辨率，会出现较大的马赛克
        // 注意（2）：不要设置超过25FPS以上的帧率，因为电影才使用24FPS，我们一般推荐15FPS，这样能将更多的码率分配给画质
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = videoConfig.getSubStreamVideoResolution();
        encParam.videoFps = videoConfig.getSubStreamVideoFps();
        encParam.videoBitrate = videoConfig.getSubStreamVideoBitrate();
        encParam.videoResolutionMode = videoConfig.isSubStreamVideoVertical()
                ? TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT : TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;
        if (mAppScene == TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL) {
            encParam.enableAdjustRes = true;
        }
        mTRTCCloud.setSubStreamEncoderParam(encParam);
    }

    public void setQosParam() {
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        MoreConfig moreConfig = SettingConfigHelper.getInstance().getMoreConfig();
        TRTCCloudDef.TRTCNetworkQosParam qosParam = new TRTCCloudDef.TRTCNetworkQosParam();
        qosParam.controlMode = moreConfig.getQosMode();
        qosParam.preference = videoConfig.getQosPreference();
        mTRTCCloud.setNetworkQosParam(qosParam);
    }

    public void setSmallSteam() {
        //小画面的编码器参数设置
        //TRTC SDK 支持大小两路画面的同时编码和传输，这样网速不理想的用户可以选择观看小画面
        //注意：iPhone & Android 不要开启大小双路画面，非常浪费流量，大小路画面适合 Windows 和 MAC 这样的有线网络环境
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();

        TRTCCloudDef.TRTCVideoEncParam smallParam = new TRTCCloudDef.TRTCVideoEncParam();
        smallParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_160_90;
        smallParam.videoFps = videoConfig.getMainStreamVideoFps();
        smallParam.videoBitrate = 100;
        smallParam.videoResolutionMode = videoConfig.isMainStreamVideoVertical()
                ? TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT : TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;

        MoreConfig moreConfig = SettingConfigHelper.getInstance().getMoreConfig();
        mTRTCCloud.enableEncSmallVideoStream(moreConfig.isEnableSmall(), smallParam);
        mTRTCCloud.setPriorRemoteVideoStreamType(moreConfig.isPriorSmall()
                ? TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SMALL : TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
    }

    public void setAudioQualityEx(int bitrate) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "setAudioQualityEx");
            JSONObject params = new JSONObject();
            params.put("bitrate", bitrate);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setVideoEncodeParamEx() {
        // 设置编码器类型，软件编码或者硬件编码
        JSONObject encoderJson = new JSONObject();
        try {
            encoderJson.put("api", "setVideoEncodeParamEx");
            JSONObject paramsObject = new JSONObject();
            encoderJson.put("params", paramsObject);
            if (mEncoderType == 1 || mEncoderType == 0) {
                paramsObject.put("codecType", mEncoderType);
            }
            mTRTCCloud.callExperimentalAPI(encoderJson.toString());
        } catch (JSONException e) {
            Log.e(TAG, "encoderJson json exception");
        }
    }

    public void exitRoom() {
        if (mTRTCCloud != null) {
            mTRTCCloud.setAudioFrameListener(null);
            mTRTCCloud.exitRoom();
        }
    }

    /**
     * 系统音量类型
     *
     * @param type {@link TRTCCloudDef#TRTCSystemVolumeTypeAuto}
     */
    public void setSystemVolumeType(int type) {
        if (type < TRTCCloudDef.TRTCSystemVolumeTypeAuto) {
            return;
        }
        mVolumeType = type;
        mTRTCCloud.setSystemVolumeType(type);
    }

    /**
     * 音频质量
     *
     * @param quality {@link TRTCCloudDef#TRTC_AUDIO_QUALITY_DEFAULT}
     */
    public void setAudioQuality(int quality) {
        if (quality < TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH) {
            return;
        }
        mTRTCCloud.setAudioQuality(quality);
    }

    /**
     * 设置本地渲染模式：全屏铺满\自适应
     *
     * @param bFillMode
     */
    public void setVideoFillMode(boolean bFillMode) {
        mTRTCRenderParams.fillMode = bFillMode ? TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL
                : TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
        mTRTCCloud.setLocalRenderParams(mTRTCRenderParams);
    }

    /**
     * 设置旋转角
     */
    public void setLocalVideoRotation(int rotation) {
        mTRTCRenderParams.rotation = rotation;
        mTRTCCloud.setLocalRenderParams(mTRTCRenderParams);
    }

    /**
     * 是否开启免提
     *
     * @param bEnable
     */
    public void enableAudioHandFree(boolean bEnable) {
        if (bEnable) {
            // 听筒
            mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE);
        } else {
            // 扬声器
            mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        }
    }

    /**
     * 是否开启画面镜像推流
     * 开启后，画面将会进行左右镜像，推到远端
     *
     * @param bMirror
     */
    public void enableVideoEncMirror(boolean bMirror) {
        mTRTCCloud.setVideoEncoderMirror(bMirror);
    }

    public void setVideoEncoderRotation(int rotation) {
        mTRTCCloud.setVideoEncoderRotation(rotation);
    }

    /**
     * 是否开启本地画面镜像
     */
    public void setLocalViewMirror(int mode) {
        mTRTCRenderParams.mirrorType = mode;
        mTRTCCloud.setLocalRenderParams(mTRTCRenderParams);
    }

    /**
     * 是否开启重力该应
     *
     * @param bEnable
     */
    public void enableGSensor(boolean bEnable) {
        if (bEnable) {
            mTRTCCloud.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_UIFIXLAYOUT);
        } else {
            mTRTCCloud.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_DISABLE);
        }
    }

    /**
     * 是否开启音量回调
     *
     * @param bEnable
     */
    public void enableAudioVolumeEvaluation(boolean bEnable) {
        if (bEnable) {
            mTRTCCloud.enableAudioVolumeEvaluation(300);
        } else {
            mTRTCCloud.enableAudioVolumeEvaluation(0);
        }
        if (mIView != null) {
            mIView.onAudioVolumeEvaluationChange(bEnable);
        }
    }

    /**
     * 开始本地预览
     */
    public void startLocalPreview() {
        if (null != mLocalPreviewView) {
            mTRTCCloud.startLocalPreview(mIsFontCamera, mLocalPreviewView);
        }
    }

    public void setLocalPreviewView(TXCloudVideoView localPreviewView) {
        mLocalPreviewView = localPreviewView;
    }

    /**
     * 开启屏幕采集
     */
    public void startScreenCapture(int streamType) {
        VideoConfig videoConfig = SettingConfigHelper.getInstance().getVideoConfig();
        TRTCCloudDef.TRTCVideoEncParam encParams = new TRTCCloudDef.TRTCVideoEncParam();

        if (TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB == streamType) {
            encParams.videoResolution = videoConfig.getSubStreamVideoResolution();
            if (videoConfig.isSubStreamVideoVertical()) {
                encParams.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
            } else {
                encParams.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;
            }
            encParams.videoFps = videoConfig.getSubStreamVideoFps();
            encParams.enableAdjustRes = false;
            encParams.videoBitrate = videoConfig.getSubStreamVideoBitrate();
        } else {
            encParams.videoResolution = videoConfig.getMainStreamVideoResolution();
            if (videoConfig.isMainStreamVideoVertical()) {
                encParams.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
            } else {
                encParams.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;
            }
            encParams.videoFps = videoConfig.getMainStreamVideoFps();
            encParams.enableAdjustRes = false;
            encParams.videoBitrate = videoConfig.getMainStreamVideoBitrate();
        }
        TRTCCloudDef.TRTCScreenShareParams params = new TRTCCloudDef.TRTCScreenShareParams();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        params.floatingView = inflater.inflate(R.layout.trtcdemo_screen_capture_floating_window,
                null, false);

        mTRTCCloud.startScreenCapture(streamType, encParams, params);
    }

    public void setLocalVideoProcessListener(int pixelFormat, int bufferType,
                                             TRTCCloudListener.TRTCVideoFrameListener listener) {
        mTRTCCloud.setLocalVideoProcessListener(pixelFormat, bufferType, listener);
    }

    /**
     * 恢复屏幕采集
     */
    public void resumeScreenCapture() {
        mTRTCCloud.resumeScreenCapture();
    }

    public void switchCamera() {
        mIsFontCamera = !mIsFontCamera;
        mTRTCCloud.getDeviceManager().switchCamera(mIsFontCamera);
    }

    /**
     * 设置图像增强
     */
    public void setSharpnessEnhancementEnabled(boolean enabled) {
        mTRTCCloud.getBeautyManager().enableSharpnessEnhancement(enabled);
    }

    public void setMuteImageEnabled(boolean enabled) {
        if (enabled) {
            mTRTCCloud.setVideoMuteImage(SettingConfigHelper.getInstance().getVideoConfig().getMuteImage(), 10);
        } else {
            mTRTCCloud.setVideoMuteImage(null, 10);
        }
    }

    /**
     * 打开自定义声音采集
     *
     * @param bEnable
     */
    public void enableCustomAudioCapture(boolean bEnable) {
        mTRTCCloud.enableCustomAudioCapture(bEnable);
    }

    public long generateCustomPTS() {
        return mTRTCCloud.generateCustomPTS();
    }

    public void sendCustomAudioData(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {
        mTRTCCloud.sendCustomAudioData(trtcAudioFrame);
    }

    public void sendCustomVideoData(TRTCCloudDef.TRTCVideoFrame trtcVideoFrame) {
        mTRTCCloud.sendCustomVideoData(trtcVideoFrame);
    }

    public TXDeviceManager getDeviceManager() {
        return mTRTCCloud.getDeviceManager();
    }

    /**
     * 是否开启自动增益补偿功能, 可以自动调麦克风的收音量到一定的音量水平
     *
     * @param level
     */
    public void enableAGC(int level) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "enableAudioAGC");
            JSONObject params = new JSONObject();
            params.put("enable", level > 0 ? 1 : 0);
            params.put("level", level);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回声消除器，可以消除各种延迟的回声
     *
     * @param level
     */
    public void enableAEC(int level) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "enableAudioAEC");
            JSONObject params = new JSONObject();
            params.put("enable", level > 0 ? 1 : 0);
            params.put("level", level);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 背景噪音抑制功能，可探测出背景固定频率的杂音并消除背景噪音
     *
     * @param level
     */
    public void enableANS(int level) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "enableAudioANS");
            JSONObject params = new JSONObject();
            params.put("enable", level > 0 ? 1 : 0);
            params.put("level", level);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换角色，用在直播的场景
     *
     * @return 切换后的角色
     */
    public int switchRole() {
        // 目标的切换角色
        int targetRole = mTRTCParams.role == TRTCCloudDef.TRTCRoleAnchor ? TRTCCloudDef.TRTCRoleAudience
                : TRTCCloudDef.TRTCRoleAnchor;
        if (mTRTCCloud != null) {
            mTRTCCloud.switchRole(targetRole);
        }
        mTRTCParams.role = targetRole;
        return mTRTCParams.role;
    }

    /**
     * 启动麦克风采集，并将音频数据传输给房间里的其他用户
     */
    public void startLocalAudio() {
        mTRTCCloud.startLocalAudio();
    }

    /**
     * 关闭麦克风采集，其他用户会受到 onUserAudioAvailable(false)
     */
    public void stopLocalAudio() {
        mTRTCCloud.stopLocalAudio();
    }

    /**
     * 停止本地视频采集及预览
     */
    public void stopLocalPreview() {
        mTRTCCloud.stopLocalPreview();
    }

    /**
     * 停止屏幕采集
     */
    public void stopScreenCapture() {
        mTRTCCloud.stopScreenCapture();
    }

    /**
     * 暂停屏幕采集
     */
    public void pauseScreenCapture() {
        mTRTCCloud.pauseScreenCapture();
    }

    /**
     * 函数会停止向其他用户发送视频数据
     * 当屏蔽本地视频后，房间里的其它成员将会收到 onUserVideoAvailable 回调通知
     *
     * @param mute true 屏蔽本地视频  false 开启本地视频
     */
    public void muteLocalVideo(boolean mute) {
        mTRTCCloud.muteLocalVideo(mute);
    }

    /**
     * 见 {@link TRTCCloud#muteLocalAudio(boolean)}
     *
     * @param mute
     */
    public void muteLocalAudio(boolean mute) {
        mTRTCCloud.muteLocalAudio(mute);
    }

    /**
     * 开始跨房连麦
     *
     * @param roomId   房间id
     * @param username 用户id
     */
    public void startLinkMic(String roomId, String username) {
        PkConfig pkConfig = SettingConfigHelper.getInstance().getPkConfig();
        pkConfig.setConnectRoomId(roomId);
        pkConfig.setConnectUserName(username);
        // 根据userId，以及roomid 发起跨房连接
        mTRTCCloud.ConnectOtherRoom(String.format("{\"roomId\":%s,\"userId\":\"%s\"}", roomId, username));
        if (mIView != null) {
            mIView.onStartLinkMic();
        }
    }

    /**
     * 停止连麦
     */
    public void stopLinkMic() {
        mTRTCCloud.DisconnectOtherRoom();
    }

    /**
     * 开启耳返
     *
     * @param enable
     */
    public void enableEarMonitoring(boolean enable) {
        mTRTCCloud.enableAudioEarMonitoring(enable);
    }

    /**
     * 是否展示debug信息在界面中
     *
     * @param logLevel
     */
    public void showDebugView(int logLevel) {
        if (logLevel == 0) {
            mTRTCCloud.showDebugView(logLevel);
        } else {
            setDebugViewMargin(mTRTCParams.userId);
            mTRTCCloud.showDebugView(logLevel);
        }
    }

    public void setDebugViewMargin(String userId) {
        mTRTCCloud.setDebugViewMargin(userId, new TRTCCloud.TRTCViewMargin(
                0.0f, 0.0f, 0.52f, 0.0f));
    }

    /**
     * 开/关 闪光灯，根据 {@link MoreConfig#isEnableFlash } 的状态来操作
     *
     * @return true 成功打开或者关闭 false 打开或者关闭失败
     */
    public boolean openFlashlight() {
        if (!mTRTCCloud.isCameraTorchSupported()) {
            ToastUtils.showShort("设备不支持闪光灯");
            return false;
        }
        MoreConfig config = SettingConfigHelper.getInstance().getMoreConfig();
        boolean openStatus = mTRTCCloud.getDeviceManager().enableCameraTorch(!config.isEnableFlash());
        if (openStatus) {
            config.setEnableFlash(!config.isEnableFlash());
        }
        return openStatus;
    }

    /**
     * 打开水印，这里默认打开 R.drawable.watermark 这个文件
     * 如果您需要打开其他的水印，可以参考 {@link TRTCCloud#setWatermark(Bitmap, int, float, float, float)}
     *
     * @param watermark 是否打开水印
     */
    public void enableWatermark(boolean watermark) {
        if (watermark) {
            Bitmap bitmap = ImageUtils.getBitmap(R.drawable.trtcdemo_watermark);
            mTRTCCloud.setWatermark(bitmap, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, 0.1f, 0.1f, 0.2f);
        } else {
            mTRTCCloud.setWatermark(null, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, 0.1f, 0.1f, 0.2f);
        }
    }


    /**
     * 打开时间水印：通过三方美颜接口，在视频帧中绘制时间信息，格式:yyyy-MM-dd HH:mm:ss:SSS (方便开发&测试同学在定位问题)；
     * 如果您需要打开其他的水印，可以参考 {@link TRTCCloud#setWatermark(Bitmap, int, float, float, float)}
     *
     * @param enable 是否打开时间水印
     */
    public void enableTimeWatermark(boolean enable) {
        mTRTCCloud.setLocalVideoProcessListener(TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D,
                TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE, enable ? new TRTCCloudListener.TRTCVideoFrameListener() {
            TimeWaterMarkFilter waterMarkFilter;
            int mVideoFrameWidth = 0;
            int mVideoFrameHeight = 0;

            @Override
            public void onGLContextCreated() {
                if (waterMarkFilter == null) {
                    waterMarkFilter = new TimeWaterMarkFilter();
                }

                if (!waterMarkFilter.init()) {
                    waterMarkFilter.destroy();
                    waterMarkFilter = null;
                } else {
                    waterMarkFilter.setHasFrameBuffer(true);
                }
            }

            @Override
            public int onProcessVideoFrame(TRTCCloudDef.TRTCVideoFrame srcVideoFrame,
                                           TRTCCloudDef.TRTCVideoFrame dstVideoFrame) {
                if (mVideoFrameWidth == 0 || mVideoFrameHeight == 0) {
                    mVideoFrameWidth = srcVideoFrame.width;
                    mVideoFrameHeight = srcVideoFrame.height;
                    if (waterMarkFilter != null) {
                        waterMarkFilter.onOutputSizeChanged(mVideoFrameWidth, mVideoFrameHeight);
                    }
                } else if (mVideoFrameWidth != srcVideoFrame.width || mVideoFrameHeight != srcVideoFrame.height) {
                    mVideoFrameWidth = srcVideoFrame.width;
                    mVideoFrameHeight = srcVideoFrame.height;
                    dstVideoFrame.width = srcVideoFrame.width;
                    dstVideoFrame.height = srcVideoFrame.height;
                    if (waterMarkFilter != null) {
                        waterMarkFilter.onOutputSizeChanged(mVideoFrameWidth, mVideoFrameHeight);
                    }
                }

                if (waterMarkFilter != null) {
                    Bitmap bitmap = OpenGlUtils.createTimeBitmap(System.currentTimeMillis(),
                            srcVideoFrame.width, srcVideoFrame.height);
                    waterMarkFilter.setTimeWaterMark(bitmap, 0.1f, 0.1f, 0.5f);
                    dstVideoFrame.texture.textureId = waterMarkFilter.onDrawToTexture(srcVideoFrame.texture.textureId);
                    bitmap.recycle();
                }
                return 0;
            }

            @Override
            public void onGLContextDestory() {
                if (null != waterMarkFilter) {
                    waterMarkFilter.destroy();
                    waterMarkFilter = null;
                    mVideoFrameWidth = 0;
                    mVideoFrameHeight = 0;
                }
            }
        } : null);
    }


    /**
     * 开始录音，默认保存到 /app私有目录/test/record/record.aac 中
     * 您可以参考 {@link TRTCCloud#startAudioRecording(com.tencent.trtc.TRTCCloudDef.TRTCAudioRecordingParams)}
     * 保存到其他文件中
     *
     * @return
     */
    public boolean startRecord(int type) {
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        TRTCCloudDef.TRTCAudioRecordingParams params = new TRTCCloudDef.TRTCAudioRecordingParams();
        params.filePath = mTestRecordAACPath;
        params.recordingContent = type;

        if (TextUtils.isEmpty(params.filePath)) {
            return false;
        }
        int res = mTRTCCloud.startAudioRecording(params);
        if (res == 0) {
            audioConfig.setRecording(true);
            ToastUtils.showLong("开始录制" + mTestRecordAACPath);
            return true;
        } else if (res == -1) {
            audioConfig.setRecording(true);
            ToastUtils.showLong("正在录制中");
            return true;
        } else {
            audioConfig.setRecording(false);
            ToastUtils.showLong("录制失败");
            return false;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        audioConfig.setRecording(false);
        mTRTCCloud.stopAudioRecording();
        ToastUtils.showLong("录制成功，文件保存在" + mTestRecordAACPath);
    }


    public void startLocalRecording(int mode, String path) {
        TRTCCloudDef.TRTCLocalRecordingParams params = new TRTCCloudDef.TRTCLocalRecordingParams();
        params.filePath = path;
        params.recordType = mode;
        params.interval = 2000;

        if (TextUtils.isEmpty(params.filePath)) {
            return;
        }
        mTRTCCloud.startLocalRecording(params);
    }

    /**
     * 停止录音
     */
    public void stopLocalRecording() {
        mTRTCCloud.stopLocalRecording();
    }

    /**
     * 并发选路配置
     */
    public void setRemoteAudioParallelParams(int maxCount) {
        // 清空之前用户配置，避免冲突
        ArrayList<String> includeUsers =
                SettingConfigHelper.getInstance().getAudioConfig().getAudioParallelIncludeUsers();
        for (String user : includeUsers) {
            RemoteUserConfigHelper.getInstance().setRemoteUserAudioParallelMustPlay(user, false);
        }
        SettingConfigHelper.getInstance().getAudioConfig().removeAudioParallelIncludeUsers();

        TRTCCloudDef.TRTCAudioParallelParams params = new TRTCCloudDef.TRTCAudioParallelParams();
        params.maxCount = maxCount;
        params.includeUsers = new ArrayList<>();

        mTRTCCloud.setRemoteAudioParallelParams(params);
        SettingConfigHelper.getInstance().getAudioConfig().setAudioParallelMaxCount(maxCount);
    }

    /**
     * 开启自定义渲染
     */
    public void enableCustomAudioRendering(boolean enable) {
        mTRTCCloud.enableCustomAudioRendering(enable);
    }

    /**
     * 获取可播放的音频数据
     */
    public void getCustomAudioRenderingFrame(TRTCCloudDef.TRTCAudioFrame audioFrame) {
        mTRTCCloud.getCustomAudioRenderingFrame(audioFrame);
    }

    /**
     * 截取本地预览画面
     */
    public void snapshotLocalView() {
        AudioConfig audioConfig = SettingConfigHelper.getInstance().getAudioConfig();
        audioConfig.setRecording(false);
        mTRTCCloud.snapshotVideo(null, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                new TRTCCloudListener.TRTCSnapshotListener() {
            @Override
            public void onSnapshotComplete(Bitmap bmp) {
                if (bmp == null) {
                    ToastUtils.showLong("截图失败");
                } else {
                    if (mIView != null) {
                        mIView.onSnapshotLocalView(bmp);
                    }
                }
            }
        });

    }

    /**
     * 发送自定义消息，见 {@link TRTCCloud#sendCustomCmdMsg }
     *
     * @param msg
     */
    public void sendCustomMsg(String msg) {
        int index = mMsgCmdIndex % 10 + 1;
        boolean sendStatus = mTRTCCloud.sendCustomCmdMsg(index, msg.getBytes(), true, true);
        if (sendStatus) {
            ToastUtils.showLong("发送自定义消息成功");
            mMsgCmdIndex++;
        }
    }

    /**
     * 发送sei消息，见 {@link TRTCCloud#sendSEIMsg }
     *
     * @param msg
     */
    public void sendSEIMsg(String msg) {
        mTRTCCloud.sendSEIMsg(msg.getBytes(), 1);
    }

    /**
     * 设置推送纯黑视频帧
     *
     * @param enable 开启或关闭
     */
    public void enableBlackStream(boolean enable) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "enableBlackStream");
            JSONObject params = new JSONObject();
            params.put("enable", enable);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public TXAudioEffectManager getAudioEffectManager() {
        if (mTRTCCloud == null) {
            mTRTCCloud = TRTCCloud.sharedInstance(mContext);
        }
        return mTRTCCloud.getAudioEffectManager();
    }

    public TXBeautyManager getBeautyManager() {
        if (mTRTCCloud == null) {
            mTRTCCloud = TRTCCloud.sharedInstance(mContext);
        }
        return mTRTCCloud.getBeautyManager();
    }

    public void setLocalVideoRenderListener(int pixelFormat, int bufferType,
                                            TRTCCloudListener.TRTCVideoRenderListener listener) {
        mTRTCCloud.setLocalVideoRenderListener(pixelFormat, bufferType, listener);
    }

    public void setRecordVolume(int volume) {
        mTRTCCloud.setAudioCaptureVolume(volume);
    }

    public void setPlayoutVolume(int volume) {
        mTRTCCloud.setAudioPlayoutVolume(volume);
    }

    public String getDefaultPlayUrl() {
        String roomId = TextUtils.isEmpty(mTRTCParams.strRoomId) ? String.valueOf(mTRTCParams.roomId)
                : mTRTCParams.strRoomId;
        String streamId = mTRTCParams.sdkAppId + "_" + roomId + "_" + mTRTCParams.userId + "_main";
        try {
            streamId = URLEncoder.encode(streamId, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return streamId;
    }

    /**
     * 设置后台环境（正式环境，测试环境）
     *
     * @param env 服务器环境 0：正式环境；1：测试环境；2：体验环境，默认为正式环境
     * @note 仅适用于腾讯内部研发团队使用，测试环境服务器和正式环境是隔离的，而且有大量的不确定性问题，请勿使用。
     */
    public void setNetEnv(int env) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "setNetEnv");
            JSONObject params = new JSONObject();
            params.put("env", env);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCustomRenderMode(int mode) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "setCustomRenderMode");
            JSONObject params = new JSONObject();
            params.put("mode", mode);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getAppScene() {
        return mAppScene;
    }

    public TRTCCloudDef.TRTCParams getParams() {
        return mTRTCParams;
    }

    public TRTCCloud createSubCloud() {
        return TRTCCloud.sharedInstance(mContext).createSubCloud();
    }

    public void destroySubCloud() {
        TRTCCloud.sharedInstance(mContext).destroySubCloud(mTRTCCloud);
    }

    public void startPublishLocalStreams() {
        mTRTCCloud.switchRole(TRTCCloudDef.TRTCRoleAnchor);
        mTRTCCloud.muteLocalVideo(false);
        mTRTCCloud.muteLocalAudio(false);
    }

    public void stopPublishLocalStreams() {
        mTRTCCloud.switchRole(TRTCCloudDef.TRTCRoleAudience);
        mTRTCCloud.muteLocalVideo(true);
        mTRTCCloud.muteLocalAudio(true);
    }

    public void bindAudioMsg(String msg) {
        synchronized (this) {
            mBindAudioFrameMsg = msg;
        }
    }

    public void sendMsgToAudioPkg(String msg) {
        TRTCAudioPkgListener.getInstance().bindMsgToAudioPkg(msg);
    }

    public void startPublishCDNStream(String url, String streamId) {
        TRTCCloudDef.TRTCPublishCDNParam param = new TRTCCloudDef.TRTCPublishCDNParam();
        param.appId = GenerateTestUserSig.APPID;
        param.bizId = GenerateTestUserSig.BIZID;
        param.url = url;
        param.streamId = streamId;
        mTRTCCloud.startPublishCDNStream(param);
    }

    public void stopPublishCDNStream() {
        mTRTCCloud.stopPublishCDNStream();
    }



    /**
     * 可能会与界面相关的回调
     */
    public interface IView {
        /**
         * 是否开启音量提示条，界面可以显示/隐藏音量提示条
         *
         * @param enable
         */
        void onAudioVolumeEvaluationChange(boolean enable);

        /**
         * 开始连麦回调，界面可以展示loading状态
         */
        void onStartLinkMic();

        /**
         * 屏蔽本地视频回调，界面可以更新对应按钮状态
         *
         * @param isMute
         */
        void onMuteLocalVideo(boolean isMute);

        /**
         * 屏蔽本地音频回调，界面可以更新对应按钮状态
         *
         * @param isMute
         */
        void onMuteLocalAudio(boolean isMute);

        /**
         * 视频截图回调
         *
         * @param bmp
         */
        void onSnapshotLocalView(Bitmap bmp);
    }
}
