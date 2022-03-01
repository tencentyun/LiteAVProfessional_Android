package com.tencent.liteav.demo.liveroom.roomutil.commondef;

public class MLVBCommonDef {
    public enum CustomFieldOp {
        SET/*设置*/, INC/*加计数*/, DEC/*减计数*/
    }

    //客户端错误码
    public interface LiveRoomErrorCode {
        int ERROR_NOT_LOGIN          = -1; // 未登录
        int ERROR_NOT_IN_ROOM        = -2; // 未进直播房间
        int ERROR_PUSH               = -3; // 推流错误
        int ERROR_PARAMETERS_INVALID = -4; // 参数错误
        int ERROR_LICENSE_INVALID    = -5; // license 校验失败
        int ERROR_PLAY               = -6; // 播放错误
        int ERROR_IM_FORCE_OFFLINE   = -7; // IM 被强制下线（例如：多端登录）
    }

    //日志显示状态码
    public interface LogShowMode {
        int LOG_STATUS_COUNT    = 3;  //日志状态数量
        int LOG_SHOW_NONE       = 0;  //不显示日志
        int LOG_SHOW_GLOBAL     = 1;  //显示全局的日志
        int LOG_SHOW_VIDEO_VIEW = 2;  //在TRTCCloudVideoView上显示日志
    }
}
