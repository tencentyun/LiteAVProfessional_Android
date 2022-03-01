package com.tencent.liteav.trtcdemo.model.manager.chorus;

import static com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef.ChorusStopReason.LocalStop;
import static com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef.ChorusStopReason.MusicPlayFailed;
import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_ERROR_DISCONNECTED;
import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef.CdnPlayStatus;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef.CdnPushStatus;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef.ChorusStartReason;
import com.tencent.liteav.trtcdemo.model.manager.chorus.TRTCChorusDef.ChorusStopReason;
import com.tencent.liteav.trtcdemo.model.utils.FileUtils;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;

public class TRTCChorusManagerImpl extends TRTCChorusManager {

    private static final String TAG = "TRTCChorusManager";

    private static final String MUSIC_FILE_NAME         = "153307-yc.mp3";
    private static final String KEY_CMD                 = "cmd";
    private static final String KEY_START_PLAY_MUSIC_TS = "startPlayMusicTS";
    private static final String KEY_REQUEST_STOP_TS     = "requestStopTS";
    private static final String KEY_MUSIC_CURRENT_TS    = "musicCurrentTS";
    private static final String MSG_START_CHORUS        = "startChorus";
    private static final String MSG_STOP_CHORUS         = "stopChorus";
    private static final int    MUSIC_START_DELAY       = 1000;
    private static final int    MUSIC_PRELOAD_DELAY     = 400;
    private static final int    MESSAGE_SEND_INTERVAL   = 1000;
    private static final int    MUSIC_ID                = 999;
    private static final float  CACHE_TIME_SMOOTH       = 5.0f;

    private final Context            mContext;
    private final TRTCCloud          mTRTCCloud;
    private       Timer              mTimer;
    private final HandlerThread      mWorkThread;
    private final Handler            mWorkHandler;
    private       TRTCChorusListener mListener;

    /**
     * 合唱音乐相关
     */
    private          String  mMusicPath;
    private volatile long    mMusicDuration;
    private volatile boolean mIsChorusOn;
    private          long    mRevStartPlayMusicTs;
    private volatile long    mStartPlayMusicTs;
    private          long    mRequestStopPlayMusicTs;

    /**
     * 合唱 cdn 相关
     */
    private              V2TXLivePusher mPusher;
    private              V2TXLivePlayer mPlayer;
    private static final int            SEI_PAYLOAD_TYPE = 242;

    public TRTCChorusManagerImpl(@NonNull Context context, @NonNull TRTCCloud trtcCloud) {
        super(context, trtcCloud);
        mContext = context;
        mTRTCCloud = trtcCloud;
        mWorkThread = new HandlerThread("TRTCChorusManagerWorkThread");
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
        initMusicFile();
    }

    /**
     * 设置合唱回调
     *
     * @param listener 合唱回调
     */
    public void setListener(TRTCChorusListener listener) {
        mListener = listener;
    }

    /**
     * 开始合唱
     *
     * @return true：合唱启动成功；false：合唱启动失败
     */
    public boolean startChorus() {
        Log.i(TAG, "startChorus");
        return startPlayMusic(ChorusStartReason.LocalStart, MUSIC_START_DELAY);
    }

    /**
     * 停止合唱
     */
    public void stopChorus() {
        Log.i(TAG, "stopChorus");
        stopPlayMusic(LocalStop);
    }

    /**
     * 当前是否正在合唱
     *
     * @return true：当前正在合唱中；false：当前不在合唱
     */
    public boolean isChorusOn() {
        return mIsChorusOn;
    }

    /**
     * TRTC 自定义消息回调，用于接收房间内其他用户发送的自定义消息，用于解析处理合唱相关消息
     *
     * @param userId  用户标识
     * @param cmdID   命令 ID
     * @param seq     消息序号
     * @param message 消息数据
     */
    @Override
    public void onRecvCustomCmdMsg(String userId, int cmdID, int seq, byte[] message) {
        if (!isNtpReady() || message == null || message.length <= 0) {
            return;
        }
        try {
            JSONObject json = new JSONObject(new String(message, "UTF-8"));
            if (!json.has(KEY_CMD)) {
                return;
            }
            switch (json.getString(KEY_CMD)) {
                case MSG_START_CHORUS:
                    mRevStartPlayMusicTs = json.getLong(KEY_START_PLAY_MUSIC_TS);
                    Log.i(TAG, "receive start chorus message. startTs:" + mRevStartPlayMusicTs);
                    if (mRevStartPlayMusicTs < mRequestStopPlayMusicTs) {
                        // 当前收到的命令是在请求停止合唱之前发出的，需要忽略掉，否则会导致请求停止后又开启了合唱
                        return;
                    }
                    int startDelayMs = (int) (mRevStartPlayMusicTs - getNtpTime());
                    startPlayMusic(ChorusStartReason.RemoteStart, startDelayMs);
                    break;
                case MSG_STOP_CHORUS:
                    mRequestStopPlayMusicTs = json.getLong(KEY_REQUEST_STOP_TS);
                    Log.i(TAG, "receive stop chorus message. stopTs:" + mRequestStopPlayMusicTs);
                    stopPlayMusic(ChorusStopReason.RemoteStop);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "parse custom message failed. " + e);
        }
    }

    /**
     * 开始合唱 CDN 推流
     *
     * @param url 推流地址
     * @return true：推流成功；false：推流失败
     */
    public boolean startCdnPush(String url) {
        Log.i(TAG, "startCdnPush url:" + url);
        initPusher();
        if (mPusher.isPushing() == 1) {
            return false;
        }
        return mPusher.startPush(url) == V2TXLIVE_OK;
    }

    /**
     * 停止合唱 CDN 推流
     */
    public void stopCdnPush() {
        Log.i(TAG, "stopCdnPush");
        initPusher();
        if (mPusher.isPushing() == 1) {
            mPusher.stopPush();
        }
    }

    /**
     * 是否正在 CDN 推流中
     *
     * @return true：正在推流；false：不在推流
     */
    public boolean isCdnPushing() {
        initPusher();
        return mPusher.isPushing() == 1;
    }

    /**
     * 开始合唱 CDN 播放
     *
     * @param url  拉流地址
     * @param view 承载视频的 view
     * @return true：拉流成功；false：拉流失败
     */
    public boolean startCdnPlay(String url, TXCloudVideoView view) {
        Log.i(TAG, "startCdnPlay url:" + url);
        initPlayer();
        if (mPlayer.isPlaying() == 1) {
            return false;
        }
        mPlayer.setRenderView(view);
        mPlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);
        mPlayer.setCacheParams(CACHE_TIME_SMOOTH, CACHE_TIME_SMOOTH);
        return mPlayer.startPlay(url) == V2TXLIVE_OK;
    }

    /**
     * 停止合唱 CDN 播放
     */
    public void stopCdnPlay() {
        Log.i(TAG, "stopCdnPlay");
        initPlayer();
        if (mPlayer.isPlaying() == 1) {
            mPlayer.stopPlay();
        }
    }

    /**
     * 是否正在 CDN 播放中
     *
     * @return true：正在播放；false：不在播放
     */
    public boolean isCdnPlaying() {
        initPlayer();
        return mPlayer.isPlaying() == 1;
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                    私有方法
    //
    /////////////////////////////////////////////////////////////////////////////////

    private void initMusicFile() {
        File sdcardDir = mContext.getApplicationContext().getExternalFilesDir(null);
        if (sdcardDir != null) {
            mMusicPath = sdcardDir.getAbsolutePath() + "/" + MUSIC_FILE_NAME;
        }
        if (TextUtils.isEmpty(mMusicPath)) {
            return;
        }
        File file = new File(mMusicPath);
        if (file.exists()) {
            mMusicDuration = mTRTCCloud.getAudioEffectManager().getMusicDurationInMS(mMusicPath);
            return;
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FileUtils.copyFilesFromAssets(mContext, MUSIC_FILE_NAME, mMusicPath);
                mMusicDuration = mTRTCCloud.getAudioEffectManager().getMusicDurationInMS(mMusicPath);
            }
        });
    }

    private void preloadMusic(int startTimeMS) {
        Log.i(TAG, "preloadMusic currentNtp:" + getNtpTime());
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("api", "preLoadMusic");
            JSONObject paramJsonObject = new JSONObject();
            paramJsonObject.put("musicId", MUSIC_ID);
            paramJsonObject.put("path", mMusicPath);
            paramJsonObject.put("startTimeMS", startTimeMS);
            jsonObject.put("params", paramJsonObject);
            body = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTRTCCloud.callExperimentalAPI(body);
    }

    private boolean isNtpReady() {
        return TXLiveBase.getNetworkTimestamp() > 0;
    }

    private long getNtpTime() {
        return TXLiveBase.getNetworkTimestamp();
    }

    private boolean startPlayMusic(ChorusStartReason reason, int delayMs) {
        if (!isNtpReady() || mMusicDuration <= 0) {
            Log.e(TAG,
                    "startPlayMusic failed. isNtpReady:" + isNtpReady() + " isMusicFileReady:" + (mMusicDuration > 0));
            return false;
        }
        if (delayMs <= -mMusicDuration) {
            //若 delayMs 为负数，代表约定的合唱开始时间在当前时刻之前
            //进一步，若 delayMs 为负，并且绝对值大于 BGM 时长，证明此时合唱已经结束了，应当忽略此次消息
            return false;
        }
        if (mIsChorusOn) {
            return false;
        }
        mIsChorusOn = true;
        Log.i(TAG, "startPlayMusic delayMs:" + delayMs + " currentNtp:" + getNtpTime());

        startTimer(reason == ChorusStartReason.LocalStart ? (getNtpTime() + MUSIC_START_DELAY) : mRevStartPlayMusicTs);
        final TXAudioEffectManager.AudioMusicParam audioMusicParam =
                new TXAudioEffectManager.AudioMusicParam(MUSIC_ID, mMusicPath);
        mTRTCCloud.getAudioEffectManager().setMusicObserver(MUSIC_ID, this);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!mIsChorusOn) {
                    // 若达到预期播放时间时，合唱已被停止，则跳过此次播放
                    return;
                }
                Log.i(TAG, "calling startPlayMusic currentNtp:" + getNtpTime());
                mTRTCCloud.getAudioEffectManager().startPlayMusic(audioMusicParam);
                mTRTCCloud.getAudioEffectManager().setMusicPlayoutVolume(audioMusicParam.id, 30);
            }
        };

        if (delayMs > 0) {
            preloadMusic(0);
            mWorkHandler.postDelayed(runnable, delayMs);
        } else {
            preloadMusic(Math.abs(delayMs) + MUSIC_PRELOAD_DELAY);
            mWorkHandler.postDelayed(runnable, MUSIC_PRELOAD_DELAY);
        }
        if (mListener != null) {
            mListener.onChorusStart(reason);
        }
        return true;
    }

    private void startTimer(final long startTs) {
        Log.i(TAG, "startTimer startTs:" + startTs);
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendStartMusicMsg(startTs);
                    checkMusicProgress();
                }
            }, 0, MESSAGE_SEND_INTERVAL);
            mStartPlayMusicTs = startTs;
        }
    }

    private void sendStartMusicMsg(long startTs) {
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_CMD, MSG_START_CHORUS);
            jsonObject.put(KEY_START_PLAY_MUSIC_TS, startTs);
            body = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTRTCCloud.sendCustomCmdMsg(0, body.getBytes(), false, false);
    }

    private void stopPlayMusic(ChorusStopReason reason) {
        if (!mIsChorusOn) {
            return;
        }
        mIsChorusOn = false;
        Log.i(TAG, "stopPlayMusic currentNtp:" + getNtpTime());
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTRTCCloud.getAudioEffectManager().setMusicObserver(MUSIC_ID, null);
        mTRTCCloud.getAudioEffectManager().stopPlayMusic(MUSIC_ID);
        sendStopBgmMsg();
        if (reason == LocalStop) {
            mRequestStopPlayMusicTs = getNtpTime();
        }
        if (mListener != null) {
            mListener.onChorusStop(reason);
        }
    }

    private void sendStopBgmMsg() {
        long stopTs = getNtpTime();
        Log.i(TAG, "sendStopBgmMsg stopTs:" + stopTs);
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_CMD, MSG_STOP_CHORUS);
            jsonObject.put(KEY_REQUEST_STOP_TS, stopTs);
            body = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTRTCCloud.sendCustomCmdMsg(0, body.getBytes(), true, true);
        mRequestStopPlayMusicTs = stopTs;
    }

    private void checkMusicProgress() {
        long currentProgress = mTRTCCloud.getAudioEffectManager().getMusicCurrentPosInMS(MUSIC_ID);
        long estimatedProgress = getNtpTime() - mStartPlayMusicTs;
        if (estimatedProgress >= 0 && Math.abs(currentProgress - estimatedProgress) > 60) {
            Log.i(TAG,
                    "checkMusicProgress currentProgress:" + currentProgress + " estimatedProgress:" + estimatedProgress);
            mTRTCCloud.getAudioEffectManager().seekMusicToPosInMS(MUSIC_ID, (int) estimatedProgress);
        }
    }

    @Override
    public void onStart(int id, int errCode) {
        Log.i(TAG, "onStart currentNtp:" + getNtpTime());
        if (errCode < 0) {
            Log.e(TAG, "start play music failed. errCode:" + errCode);
            stopPlayMusic(ChorusStopReason.MusicPlayFailed);
        }
    }

    @Override
    public void onPlayProgress(int id, long curPtsMS, long durationMS) {
        if (mListener != null) {
            mListener.onChorusProgress(curPtsMS, durationMS);
        }
        sendMusicPositionMsg(curPtsMS);
    }

    @Override
    public void onComplete(int id, int errCode) {
        Log.i(TAG, "onComplete currentNtp:" + getNtpTime());
        if (errCode < 0) {
            Log.e(TAG, "music play error. errCode:" + errCode);
            stopPlayMusic(MusicPlayFailed);
        } else {
            stopPlayMusic(ChorusStopReason.MusicPlayFinished);
        }
    }

    private void initPusher() {
        if (mPusher != null) {
            return;
        }
        Log.i(TAG, "initPusher");
        try {
            Class<?> clazz = Class.forName("com.tencent.live2.impl.V2TXLivePusherImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Context.class, int.class);
            constructor.setAccessible(true);
            mPusher = (V2TXLivePusher) constructor.newInstance(mContext, 101);
        } catch (Exception e) {
            Log.e(TAG, "create pusher failed. " + e);
            return;
        }
        V2TXLiveDef.V2TXLiveVideoEncoderParam param =
                new V2TXLiveDef.V2TXLiveVideoEncoderParam(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540);
        mPusher.setVideoQuality(param);
        mPusher.setAudioQuality(V2TXLiveAudioQualityDefault);
        mPusher.startVirtualCamera(null);
        mPusher.setObserver(new V2TXLivePusherObserver() {
            @Override
            public void onPushStatusUpdate(V2TXLiveDef.V2TXLivePushStatus status, String msg, Bundle extraInfo) {
                if (mListener == null) {
                    return;
                }
                CdnPushStatus pushStatus = CdnPushStatus.Disconnected;
                switch (status) {
                    case V2TXLivePushStatusDisconnected:
                        pushStatus = CdnPushStatus.Disconnected;
                        break;
                    case V2TXLivePushStatusConnecting:
                        pushStatus = CdnPushStatus.Connecting;
                        break;
                    case V2TXLivePushStatusReconnecting:
                        pushStatus = CdnPushStatus.Reconnecting;
                        break;
                    case V2TXLivePushStatusConnectSuccess:
                        pushStatus = CdnPushStatus.ConnectSuccess;
                        break;
                }
                mListener.onCdnPushStatusUpdate(pushStatus);
            }
        });
    }

    private void initPlayer() {
        if (mPlayer != null) {
            return;
        }
        Log.i(TAG, "initPlayer");
        mPlayer = new V2TXLivePlayerImpl(mContext);
        mPlayer.enableReceiveSeiMessage(true, SEI_PAYLOAD_TYPE);
        mPlayer.setObserver(new V2TXLivePlayerObserver() {
            @Override
            public void onAudioLoading(V2TXLivePlayer player, Bundle extraInfo) {
                if (mListener != null) {
                    mListener.onCdnPlayStatusUpdate(CdnPlayStatus.Loading);
                }
            }

            @Override
            public void onAudioPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
                if (mListener != null) {
                    mListener.onCdnPlayStatusUpdate(CdnPlayStatus.Playing);
                }
            }

            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                if (code == V2TXLIVE_ERROR_DISCONNECTED && mListener != null) {
                    mListener.onCdnPlayStatusUpdate(CdnPlayStatus.Stopped);
                }
            }

            @Override
            public void onReceiveSeiMessage(V2TXLivePlayer player, int payloadType, byte[] data) {
                if (data == null || data.length <= 0) {
                    return;
                }
                try {
                    JSONObject json = new JSONObject(new String(data, "UTF-8"));
                    if (!json.has(KEY_MUSIC_CURRENT_TS)) {
                        return;
                    }
                    long position = json.getLong(KEY_MUSIC_CURRENT_TS);
                    if (mListener != null) {
                        mListener.onChorusProgress(position, mMusicDuration);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "parse sei message failed. " + e);
                }
            }
        });
    }

    private void sendMusicPositionMsg(long position) {
        if (mPusher.isPushing() != 1) {
            return;
        }
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_MUSIC_CURRENT_TS, position);
            body = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPusher.sendSeiMessage(SEI_PAYLOAD_TYPE, body.getBytes());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mWorkThread.quit();
    }
}
