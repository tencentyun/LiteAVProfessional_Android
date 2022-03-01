package com.tencent.liteav.trtcdemo.model.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.tencent.liteav.trtcdemo.model.opengl.OpenGlUtils;
import com.tencent.liteav.trtcdemo.model.opengl.Rotation;
import com.tencent.liteav.trtcdemo.model.opengl.TXCTextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public class BaseFilter {

    public static final boolean DEBUG_MODE = false;

    public interface OnFilterListener {
        void onFilterListener(int textureID);
    }

    public static final String NO_FILTER_VERTEX_SHADER   = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying lowp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private final LinkedList<Runnable> mRunOnDraw;
    private final String               mVertexShader;
    private final String               mFragmentShader;
    protected     int                  mGLProgId;
    protected     int                  mGLAttribPosition;
    protected     int                  mGLUniformTexture;
    protected     int                  mGLAttribTextureCoordinate;
    protected     int                  mOutputWidth;
    protected     int                  mOutputHeight;
    protected     boolean              mIsInitialized;
    protected     FloatBuffer          mGLCubeBuffer;
    protected     FloatBuffer          mGLTextureBuffer;

    protected float[] mCurrentVertexMatrix;
    protected float[] mCurrentTextureMatrix;
    private   int     mTextureTransformMatrixLocation = -1;
    private   float[] mTextureTransformMatrix         = null;    // Ext 纹理旋转矩阵

    protected int mFrameBuffer        = -1;
    protected int mFrameBufferTexture = -1;

    protected            boolean mHasFrameBuffer   = false;//是否需要初始化FrameBuffer
    protected            boolean mbExtTextureModle = false;    // 是否是 Ext 纹理
    private static final String  TAG               = "TXCGPUFilter";

    public BaseFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER, false);
    }

    public BaseFilter(final String vertexShader, final String fragmentShader) {
        this(vertexShader, fragmentShader, false);
    }

    public BaseFilter(final String vertexShader, final String fragmentShader, boolean bOesModel) {
        mRunOnDraw = new LinkedList<Runnable>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        mbExtTextureModle = bOesModel;
        if (true == bOesModel) {
            Log.i(TAG, "set Oes fileter");
        }

        mGLCubeBuffer = ByteBuffer.allocateDirect(TXCTextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCurrentVertexMatrix = TXCTextureRotationUtil.CUBE;
        mGLCubeBuffer.put(mCurrentVertexMatrix).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TXCTextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCurrentTextureMatrix = TXCTextureRotationUtil.getRotation(Rotation.NORMAL, false, true);
        mGLTextureBuffer.put(mCurrentTextureMatrix).position(0);
    }

    public boolean init() {
        mGLProgId = OpenGlUtils.loadProgram(mVertexShader, mFragmentShader);
        if (mGLProgId != 0 && onInit())
            mIsInitialized = true;
        else mIsInitialized = false;
        onInitialized();
        return mIsInitialized;
    }

    public void setHasFrameBuffer(boolean hasFrameBuffer) {
        mHasFrameBuffer = hasFrameBuffer;
    }


    public boolean onInit() {
        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position");
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture");
        mTextureTransformMatrixLocation = GLES20.glGetUniformLocation(mGLProgId, "textureTransform");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId,
                "inputTextureCoordinate");

        return true;
    }

    public void onInitialized() {
    }

    public void destroy() {
        GLES20.glDeleteProgram(mGLProgId);
        onDestroy();
        mIsInitialized = false;
    }

    public void onDestroy() {
        destroyFramebuffers();
        mOutputHeight = -1;
        mOutputWidth = -1;
    }


    public void destroyFramebuffers() {
        if (mFrameBuffer != -1) {
            if (DEBUG_MODE)
                Log.e("BaseFilter", "check destroy" + mFrameBuffer + "\t" + mFrameBufferTexture);
            int[] frameBuf = new int[1];
            frameBuf[0] = mFrameBuffer;
            GLES20.glDeleteFramebuffers(1, frameBuf, 0);
            mFrameBuffer = -1;
        }
        if (mFrameBufferTexture != -1) {
            int[] frameBuf = new int[1];
            frameBuf[0] = mFrameBufferTexture;
            GLES20.glDeleteTextures(1, frameBuf, 0);
            mFrameBufferTexture = -1;
        }
    }

    public void onOutputSizeChanged(final int width, final int height) {
        if (mOutputHeight == height && mOutputWidth == width)
            return;
        mOutputWidth = width;
        mOutputHeight = height;
        if (mHasFrameBuffer) {
            if (mFrameBuffer == -1) {

            } else {
                destroyFramebuffers();
            }
            int[] frameBuffers = new int[1];
            GLES20.glGenFramebuffers(1, frameBuffers, 0);
            mFrameBuffer = frameBuffers[0];
            mFrameBufferTexture = OpenGlUtils.createTexture(width, height, GLES20.GL_RGBA, GLES20.GL_RGBA);
            if (DEBUG_MODE)
                Log.e("BaseFilter", "check" + mFrameBuffer + "\t" + mFrameBufferTexture);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTexture, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    public void onDraw(final int textureId, final FloatBuffer cubeBuffer, final FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!mIsInitialized) {
            return;
        }
        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        if (mTextureTransformMatrixLocation >= 0 && null != mTextureTransformMatrix) {
            GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);
        }
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            if (true == mbExtTextureModle) {
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            } else {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            }
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
        if (true == mbExtTextureModle) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    protected void onDrawArraysPre() {
    }

    protected void onDrawArraysAfter() {
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    public int onDrawToTexture(final int textureId, int frameBuffer, int frameBufferTexture) {
        if (!mIsInitialized)
            return OpenGlUtils.NO_TEXTURE;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        onDraw(textureId, mGLCubeBuffer, mGLTextureBuffer);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return frameBufferTexture;
    }

    public int onDrawToTexture(final int textureId) {
        return onDrawToTexture(textureId, mFrameBuffer, mFrameBufferTexture);
    }
}