package com.tencent.liteav.trtcdemo.model.opengl;

public interface GLConstants {
    boolean DEBUG              = false;
    int     NO_TEXTURE         = -1;
    int     INVALID_PROGRAM_ID = -1;

    float[] CUBE_VERTICES_ARRAYS = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    float[] TEXTURE_COORDS_NO_ROTATION  = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };
    float[] TEXTURE_COORDS_ROTATE_LEFT  = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };
    float[] TEXTURE_COORDS_ROTATE_RIGHT = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
    float[] TEXTURE_COORDS_ROTATED_180  = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };

    enum GLScaleType {
        /**
         * 居中显示，不裁剪，宽或高留黑边
         */
        FIT_CENTER,

        /**
         * 居中裁剪
         */
        CENTER_CROP,
    }
}
