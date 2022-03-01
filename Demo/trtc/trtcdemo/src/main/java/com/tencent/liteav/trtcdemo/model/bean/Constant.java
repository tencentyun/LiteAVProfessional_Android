package com.tencent.liteav.trtcdemo.model.bean;

public class Constant {

    /**
     * ----------------------------------Intent key常量------------------------------------------
     */
    public static final String KEY_ROOM_ID                              = "room_id";
    public static final String KEY_ROOM_ID_STR                          = "room_id_str";
    public static final String KEY_USER_ID                              = "user_id";
    public static final String KEY_ROLE                                 = "role";
    public static final String KEY_CUSTOM_CAPTURE                       = "custom_capture";
    public static final String KEY_VIDEO_FILE_PATH                      = "file_path";
    public static final String KEY_USE_STRING_ROOM_ID                   = "use_string_room_id";
    public static final String KEY_MAIN_SCREEN_CAPTURE                  = "main_screen_capture";
    public static final String KEY_CUSTOM_AUDIO_CAPTURE                 = "key_custom_audio_capture";
    public static final String KEY_AUDIO_VOLUMETYPE                     = "auto_audio_volumeType";
    public static final String KEY_AUDIO_QUALITY                        = "auto_audio_quality";
    public static final String KEY_AUDIO_EARPIECEMODE                   = "earpieceMode";
    public static final String KEY_VIDEO_INPUT_TYPE                     = "video_input_type";
    public static final String KEY_AUDIO_INPUT_TYPE                     = "audio_input_type";
    public static final String KEY_NET_ENV_TYPE                         = "net_env_type";
    public static final String KEY_SUB_SCREEN_CAPTURE                   = "sub_screen_capture";
    public static final String KEY_RECEIVED_VIDEO                       = "auto_received_video";
    public static final String KEY_RECEIVED_AUDIO                       = "auto_received_audio";
    public static final String KEY_LOCAL_RENDER_VIEW_TYPE               = "local_render_view_type";
    public static final String KEY_ENCODER_265                          = "encoder_type_265";
    public static final String KEY_ENCODER_TYPE                         = "encoder_type";
    public static final String KEY_PERFORMANCE_MODE                     = "performance_mode";
    public static final String KEY_CUSTOM_VIDEO_PREPROCESS_PIXEL_FORMAT = "custom_video_preprocess_pixel_format";
    public static final String KEY_CUSTOM_VIDEO_PREPROCESS_BUFFER_TYPE  = "custom_video_preprocess_buffer_type";
    public static final String KEY_CUSTOM_RENDER_PIXEL_FORMAT           = "key_custom_render_pixel_format";
    public static final String KEY_CUSTOM_RENDER_BUFFER_TYPE            = "key_custom_render_buffer_type";
    public static final String KEY_CUSTOM_PROCESS_USE_RENDER_INTERFACE  = "key_custom_process_use_render_interface";
    public static final String KEY_USE_CUSTOM_OPEN_GL_CONTEXT           = "key_use_custom_open_gl_context";
    public static final String KEY_AUDIO_SCENE                          = "key_audio_scene";
    public static final String KEY_CHORUS_CDN_URL                       = "chorus_cdn_url";
    public static final String KEY_AUDIO_PARALLEL_MAX_COUNT             = "audio_parallel_max_count";
    public static final String KEY_USE_CUSTOM_RENDER_PREPROCESS         = "use_custom_render_preprocess";

    /** 软件编码器 */
    public static final int ENCODER_SOFTWARE = 0;
    /** 硬件编码器 */
    public static final int ENCODER_HARDWARE = 1;
    /** 根据分辨率自动选择编码器 */
    public static final int ENCODER_AUTO     = 2;

    /**
     * ----------------------------------云端环境常量------------------------------------------
     */
    public static final class NetEnv {
        public static final int TYPE_PRODUCT    = 0; // 正式环境
        public static final int TYPE_TEST       = 1; // 测试环境
        public static final int TYPE_EXPERIENCE = 2; // 体验环境，默认为正式环境
    }

    /**
     * ----------------------------------本地画面渲染类型常量------------------------------------------
     */
    public static final class TRTCViewType {
        public static final int TYPE_GLSURFACE_VIEW = 0;
        public static final int TYPE_SURFACE_VIEW   = 1;
        public static final int TYPE_TEXTURE_VIEW   = 2;
        public static final int TYPE_CUSTOM_VIEW    = 3;
    }
}
