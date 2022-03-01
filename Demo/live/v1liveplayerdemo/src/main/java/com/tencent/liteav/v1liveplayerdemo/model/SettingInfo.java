package com.tencent.liteav.v1liveplayerdemo.model;

import java.io.Serializable;

public class SettingInfo implements Serializable {

    public static final float MIN_ADJUST_CACHETIME = 1;
    public static final float MAX_ADJUST_CACHETIME = 5;

    public static final int RENDER_TYPE_CLOUD_VIDEO_VIEW = 0;
    public static final int RENDER_TYPE_CUSTOM           = 1;
    public static final int RENDER_TYPE_SURFACE          = 2;

    public static final int CUSTOM_RENDER_TYPE_TEXTURE = 0;
    public static final int CUSTOM_RENDER_TYPE_I420    = 1;

    public static final int CONNECT_RETRY_COUNT    = 3;
    public static final int CONNECT_RETRY_INTERVAL = 3;
    public static final int VIDEO_BLOCK_THRESHOLD  = 800;

    public static final String FLV_SESSION_KEY = "X-Tlive-SpanId";


    public boolean enableSei                 = true;
    public boolean enableMetaData            = true;
    public boolean enableAutoAdjustCacheTime = true;

    public float cacheTime          = MAX_ADJUST_CACHETIME;
    public float maxAdjustCacheTime = MAX_ADJUST_CACHETIME;
    public float minAdjustCacheTime = MIN_ADJUST_CACHETIME;

    public int    renderType;
    public int    customRenderType;
    public int    connectRetryCount    = CONNECT_RETRY_COUNT;
    public int    connectRetryInterval = CONNECT_RETRY_INTERVAL;
    public int    videoBlockThreshold  = VIDEO_BLOCK_THRESHOLD;
    public String flvSessionKey        = FLV_SESSION_KEY;

}
