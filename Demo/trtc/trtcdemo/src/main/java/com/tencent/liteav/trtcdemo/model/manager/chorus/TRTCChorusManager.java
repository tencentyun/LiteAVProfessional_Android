package com.tencent.liteav.trtcdemo.model.manager.chorus;

import android.content.Context;

import androidx.annotation.NonNull;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudListener;

public class TRTCChorusManager extends TRTCCloudListener implements TXAudioEffectManager.TXMusicPlayObserver {

    public TRTCChorusManager(@NonNull Context context, @NonNull TRTCCloud trtcCloud) {

    }

    public void setListener(TRTCChorusListener listener) {

    }

    public boolean startChorus() {
        return false;
    }

    public void stopChorus() {
    }

    public boolean isChorusOn() {
        return false;
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

    }

    /**
     * 开始合唱 CDN 推流
     *
     * @param url 推流地址
     * @return true：推流成功；false：推流失败
     */
    public boolean startCdnPush(String url) {
        return false;
    }

    /**
     * 停止合唱 CDN 推流
     */
    public void stopCdnPush() {
    }

    /**
     * 是否正在 CDN 推流中
     *
     * @return true：正在推流；false：不在推流
     */
    public boolean isCdnPushing() {
        return false;
    }

    /**
     * 开始合唱 CDN 播放
     *
     * @param url  拉流地址
     * @param view 承载视频的 view
     * @return true：拉流成功；false：拉流失败
     */
    public boolean startCdnPlay(String url, TXCloudVideoView view) {
        return false;
    }

    /**
     * 停止合唱 CDN 播放
     */
    public void stopCdnPlay() {

    }

    /**
     * 是否正在 CDN 播放中
     *
     * @return true：正在播放；false：不在播放
     */
    public boolean isCdnPlaying() {
        return false;
    }


    @Override
    public void onStart(int id, int errCode) {

    }

    @Override
    public void onPlayProgress(int id, long curPtsMS, long durationMS) {

    }

    @Override
    public void onComplete(int id, int errCode) {

    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
