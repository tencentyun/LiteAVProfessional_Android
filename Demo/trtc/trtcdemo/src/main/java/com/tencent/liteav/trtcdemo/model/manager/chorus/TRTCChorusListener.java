package com.tencent.liteav.trtcdemo.model.manager.chorus;

public interface TRTCChorusListener {
    /**
     * 合唱开始回调
     *
     * @param reason 合唱开始原因，参考 {@link TRTCChorusDef.ChorusStartReason}
     */
    void onChorusStart(TRTCChorusDef.ChorusStartReason reason);

    /**
     * 合唱进度回调
     *
     * @param curPtsMS   合唱音乐当前播放进度，单位：毫秒
     * @param durationMS 合唱音乐总时长，单位：毫秒
     */
    void onChorusProgress(long curPtsMS, long durationMS);

    /**
     * 合唱结束回调
     *
     * @param reason 合唱结束原因，参考 {@link TRTCChorusDef.ChorusStopReason}
     */
    void onChorusStop(TRTCChorusDef.ChorusStopReason reason);

    /**
     * 合唱 CDN 推流连接状态状态改变回调
     *
     * @param status 连接状态
     * @note 此回调透传 V2TXLivePusherObserver onPushStatusUpdate 回调
     */
    void onCdnPushStatusUpdate(TRTCChorusDef.CdnPushStatus status);

    /**
     * 合唱 CDN 播放状态改变回调
     *
     * @param status 播放状态
     * @note 此回调透传 V2TXLivePlayerObserver onAudioPlayStatusUpdate 回调
     */
    void onCdnPlayStatusUpdate(TRTCChorusDef.CdnPlayStatus status);
}
