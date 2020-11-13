package com.tencent.liteav.demo.videouploader.ui.utils;


public class Constants {
    /**
     * UGC 编辑的的参数
     */
    public static final String VIDEO_EDITER_PATH = "key_video_editer_path"; // 路径的key
    public static final String VIDEO_SOURCE_PATH = "key_video_source_path"; // 未压缩前视频源路径的key
    public static final String VIDEO_EDITER_URI = "key_video_editer_uri_path";

    public static final String DEFAULT_MEDIA_PACK_FOLDER = "txrtmp";      // UGC编辑器输出目录

    /**
     * 上传常量
     */
    public static final String PLAYER_DEFAULT_VIDEO = "play_default_video";
    public static final String PLAYER_VIDEO_ID = "video_id";
    public static final String PLAYER_VIDEO_NAME = "video_name";

    /**
     * 点播的信息
     */
    public static final int VOD_APPID = 1256468886;
    public static final String VOD_APPKEY = "1973fcc2b70445af8b51053d4f9022bb";

    public static final String PREFIX_MEDIA_URI = "content://";

    /**
     * 业务服务器交互路由
     * */
    public static final String SERVER_IP = "http://demo.vod2.myqcloud.com/shortvideo";
    public static final String ADDRESS_SIG = SERVER_IP + "/api/v1/misc/upload/signature";
    public static final String ADDRESS_VIDEO_LIST = SERVER_IP + "/api/v1/resource/videos";
    public static final String ADDRESS_VIDEO_INFO = SERVER_IP + "/api/v1/resource/videos/#";
    public static final String ADDRESS_VIDEO_REPORT = SERVER_IP + "/api/v1/resource/videos/"; // /api/v1/resource/videos/#file_id

    /**
     * 业务服务器返回结果码
     * */
   public static class RetCode {
        // 服务器返回码
        public static final int CODE_SUCCESS = 0;               // 接口请求成功
        public static final int CODE_PARAMS_ERR = 1001;         // 请求参数错误
        public static final int CODE_AUTH_ERR = 1002;           // 鉴权错误
        public static final int CODE_RES_ERR = 1003;            // 资源不存在
        public static final int CODE_REQ_TOO_FAST_ERR = 1004;   // 请求频率过快
        public static final int CODE_SERVER_ERR = 1000;         // 服务器错误
        // 客户端处理码
        public static final int CODE_REQUEST_ERR = 1;           // 请求错误
        public static final int CODE_PARSE_ERR = 2;             // 解析json错误

    }
}
