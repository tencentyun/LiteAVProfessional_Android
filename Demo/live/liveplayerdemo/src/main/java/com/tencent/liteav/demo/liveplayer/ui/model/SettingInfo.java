package com.tencent.liteav.demo.liveplayer.ui.model;

import java.io.Serializable;

public class SettingInfo implements Serializable {

    public static final int RENDER_TYPE_CLOUD_VIDEO_VIEW = 0;
    public static final int RENDER_TYPE_TEXTURE_VIEW = 1;
    public static final int RENDER_TYPE_SURFACE_VIEW = 2;
    public static final int RENDER_TYPE_CUSTOM = 3;
    public static final int RENDER_TYPE_SURFACE = 4;

    public static final int CUSTOM_RENDER_TYPE_TEXTURE2D_TEXTURE = 0;
    public static final int CUSTOM_RENDER_TYPE_I420_BYTE_ARRAY = 1;
    public static final int CUSTOM_RENDER_TYPE_I420_BYTE_BUFFER = 2;

    public int renderType;
    public int customRenderType;
}
