package com.tencent.liteav.v1livepusherdemo;

import com.tencent.rtmp.TXLiveConstants;

public interface V1Constants {
    String URL_FETCH_PUSH_URL         = "https://lvb.qcloud.com/weapp/utils/get_test_pushurl";
    String INTENT_SCAN_RESULT         = "SCAN_RESULT";
    String URL_PUSH                   = "url_push";       // RTMP 推流地址
    String URL_PLAY_FLV               = "url_play_flv";   // FLA  播放地址
    int    ACTIVITY_SCAN_REQUEST_CODE = 1;
    int    PUSH_RESULT_OK             = 0;
    int    SCREEN_MODE_PORTRAIT       = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;
    int    SCREEN_MODE_LANDSCAPE      = TXLiveConstants.VIDEO_ANGLE_HOME_RIGHT;

    String KEY_CUSTOM_VIDEO_PATH              = "custom_video_path";
    String KEY_IS_SCREEN_CAPTURE              = "is_screen_capture";
    String KEY_CUSTOM_VIDEO_PREPROCESS        = "custom_video_preprocess";
    String KEY_ENABLE_HIGH_CAPTURE            = "enable_high_capture";
    String KEY_PAUSE_AUDIO_ON_ACTIVITY_PAUSED = "pause_audio_on_activity_paused";
    String KEY_PROFILE_MODE                   = "profile_mode";
}
