package com.tencent.liteav.trtcdemo.model.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class TimeWaterMarkFilter extends BaseFilter {
    private static final String TAG = "TimeWaterMarkFilter";

    protected int         mBlendMode          = GLES20.GL_ONE;
    public    Bitmap      mBitmapTimeWaterMark;
    public    int[]       mWaterMarkTextureId = null;
    public    FloatBuffer mVertexCoordBuffer  = null;
    public    FloatBuffer mTextureCoordBuffer = null;

    private static final float[] mVertexCoord  = {
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
    };
    private static final float[] mTextureCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public TimeWaterMarkFilter() {
        super();
        mHasFrameBuffer = true;
    }

    public void setTimeWaterMark(Bitmap bitmap, float x, float y, float width) {
        if (bitmap == null || bitmap.isRecycled()) {
            if (mWaterMarkTextureId != null) {
                GLES20.glDeleteTextures(1, mWaterMarkTextureId, 0);
                mWaterMarkTextureId = null;
            }
            mBitmapTimeWaterMark = null;
            Log.e(TAG, "time-watermark bitmap is null or recycled");
            return;
        }

        ByteBuffer tBufferWM = ByteBuffer.allocateDirect(mVertexCoord.length * 4);
        tBufferWM.order(ByteOrder.nativeOrder());
        mVertexCoordBuffer = tBufferWM.asFloatBuffer();
        mVertexCoordBuffer.put(mVertexCoord);
        mVertexCoordBuffer.position(0);

        ByteBuffer tBuffer = ByteBuffer.allocateDirect(mTextureCoord.length * 4);
        tBuffer.order(ByteOrder.nativeOrder());
        mTextureCoordBuffer = tBuffer.asFloatBuffer();
        mTextureCoordBuffer.put(mTextureCoord);
        mTextureCoordBuffer.position(0);


        if (mWaterMarkTextureId == null) {
            mWaterMarkTextureId = new int[1];
            GLES20.glGenTextures(1, mWaterMarkTextureId, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterMarkTextureId[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        if (mBitmapTimeWaterMark == null || !mBitmapTimeWaterMark.equals(bitmap)) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterMarkTextureId[0]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            mBitmapTimeWaterMark = bitmap;
        }
    }

    @Override
    protected void onDrawArraysAfter() {
        super.onDrawArraysAfter();
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(mBlendMode, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterMarkTextureId[0]);
        GLES20.glUniform1i(mGLUniformTexture, 0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexCoordBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 8, mTextureCoordBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mWaterMarkTextureId != null)
            GLES20.glDeleteTextures(1, mWaterMarkTextureId, 0);
        mWaterMarkTextureId = null;
        mBitmapTimeWaterMark = null;

    }

}
