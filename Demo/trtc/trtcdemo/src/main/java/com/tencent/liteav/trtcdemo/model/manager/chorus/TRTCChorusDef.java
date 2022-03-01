package com.tencent.liteav.trtcdemo.model.manager.chorus;

public class TRTCChorusDef {
    /**
     * 合唱开始原因
     */
    public enum ChorusStartReason {
        // 本地用户发起合唱
        LocalStart,
        // 远端某个用户发起合唱
        RemoteStart
    }


    /**
     * 合唱结束原因
     */
    public enum ChorusStopReason {
        // 合唱歌曲播放完毕，自动停止
        MusicPlayFinished,
        // 合唱音乐起播失败，被迫终止
        MusicPlayFailed,
        // 本地用户停止合唱
        LocalStop,
        // 远端某个用户请求停止合唱
        RemoteStop
    }


    /**
     * CDN 推流连接状态
     */
    public enum CdnPushStatus {
        // 与服务器断开连接
        Disconnected,
        // 正在连接服务器
        Connecting,
        // 连接服务器成功
        ConnectSuccess,
        // 重连服务器中
        Reconnecting
    }


    /**
     * CDN 播放状态。
     */
    public enum CdnPlayStatus {
        // 播放停止
        Stopped,
        // 正在播放
        Playing,
        // 正在缓冲
        Loading
    }

}
