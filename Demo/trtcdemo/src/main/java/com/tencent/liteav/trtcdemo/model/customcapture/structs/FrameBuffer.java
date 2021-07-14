package com.tencent.liteav.trtcdemo.model.customcapture.structs;

import android.opengl.GLES20;
import android.util.Log;

import com.tencent.liteav.trtcdemo.model.opengl.OpenGlUtils;

import static com.tencent.liteav.trtcdemo.model.opengl.GLConstants.NO_TEXTURE;

public class FrameBuffer {
    private static final String TAG = "FrameBuffer";

    private final int mWidth;
    private final int mHeight;
    private int mTextureId;
    private int mFrameBufferId;

    public FrameBuffer(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void initialize() {
        mTextureId = OpenGlUtils.loadTexture(GLES20.GL_RGBA, null, mWidth, mHeight, NO_TEXTURE);
        mFrameBufferId = OpenGlUtils.generateFrameBufferId();
        Log.i(TAG, String.format("create frameBufferId: %d, textureId: %d", mFrameBufferId, mTextureId));

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mTextureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int getTextureId() {
        return mTextureId;
    }

    public int getFrameBufferId() {
        return mFrameBufferId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void uninitialize() {
        Log.i(TAG, String.format("destroy frameBufferId: %d, textureId: %d", mFrameBufferId, mTextureId));
        OpenGlUtils.deleteTexture(mTextureId);
        mTextureId = NO_TEXTURE;
        OpenGlUtils.deleteFrameBuffer(mFrameBufferId);
        mFrameBufferId = NO_TEXTURE;
    }
}
