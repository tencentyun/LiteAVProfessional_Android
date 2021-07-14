package com.tencent.liteav.trtcdemo.model.listener;

import android.opengl.GLES20;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.trtcdemo.model.opengl.GpuImageGrayscaleFilter;
import com.tencent.liteav.trtcdemo.model.opengl.OpenGlUtils;
import com.tencent.liteav.trtcdemo.model.opengl.Rotation;
import com.tencent.liteav.trtcdemo.model.customcapture.structs.FrameBuffer;
import com.tencent.trtc.TRTCCloudDef.TRTCVideoFrame;
import com.tencent.trtc.TRTCCloudListener.TRTCVideoFrameListener;

import java.nio.FloatBuffer;
import java.util.Random;

public class TRTCVideoTextureFrameListener implements TRTCVideoFrameListener {
    private static final String TAG = "TRTCVideoTextureFrameListener";

    private Random                  mRandom             = new Random();
    private FrameBuffer             mFrameBuffer;
    private GpuImageGrayscaleFilter mGrayscaleFilter;
    private FloatBuffer             mGLCubeBuffer;
    private FloatBuffer             mGLTextureBuffer;
    private int                     mFrameBufferId;

    @Override
    public void onGLContextCreated() {
        mFrameBufferId = OpenGlUtils.generateFrameBufferId();
        TXCLog.i(TAG, "onGLContextCreated, framebuffer id: %d", mFrameBufferId);
    }

    @Override
    public int onProcessVideoFrame(TRTCVideoFrame inputFrame, TRTCVideoFrame outputFrame) {
        final int width = inputFrame.width, height = inputFrame.height;
        if (mFrameBuffer == null || mFrameBuffer.getWidth() != width || mFrameBuffer.getHeight() != height) {
            if (mFrameBuffer != null) {
                mFrameBuffer.uninitialize();
            }
            mFrameBuffer = new FrameBuffer(width, height);
            mFrameBuffer.initialize();
        }
        if (mGrayscaleFilter == null) {
            mGrayscaleFilter = new GpuImageGrayscaleFilter();
            mGrayscaleFilter.init();
            mGrayscaleFilter.onOutputSizeChanged(width, height);

            mGLCubeBuffer = OpenGlUtils.createNormalCubeVerticesBuffer();
            mGLTextureBuffer = OpenGlUtils.createTextureCoordsBuffer(Rotation.NORMAL, false, false);
        }

        // 随机选择使用参数的纹理还是自己的纹理
        boolean useTextureInOutputFrame = (Math.abs(mRandom.nextInt()) % 2 == 0);
        if (useTextureInOutputFrame) {
            // 将 outputFrame 中的纹理绑定到 FrameBuffer 上，用于绘制
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                    outputFrame.texture.textureId, 0);
        } else {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer.getFrameBufferId());
        }

        GLES20.glViewport(0, 0, width, height);
        mGrayscaleFilter.onDraw(inputFrame.texture.textureId, mGLCubeBuffer, mGLTextureBuffer);

        if (useTextureInOutputFrame) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);
        } else {
            outputFrame.texture.textureId = mFrameBuffer.getTextureId();
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return 0;
    }

    @Override
    public void onGLContextDestory() {
        TXCLog.i(TAG, "onGLContextDestory framebuffer id: %d", mFrameBufferId);
        if (mFrameBuffer != null) {
            mFrameBuffer.uninitialize();
            mFrameBuffer = null;
        }
        if (mGrayscaleFilter != null) {
            mGrayscaleFilter.destroy();
            mGrayscaleFilter = null;
        }
        OpenGlUtils.deleteFrameBuffer(mFrameBufferId);
        mFrameBufferId = -1;
    }
}
