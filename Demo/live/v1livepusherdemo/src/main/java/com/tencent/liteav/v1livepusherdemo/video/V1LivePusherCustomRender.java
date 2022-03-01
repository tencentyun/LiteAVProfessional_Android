package com.tencent.liteav.v1livepusherdemo.video;


import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.TextureView;

import com.tencent.liteav.v1livepusherdemo.customcapture.structs.TextureFrame;
import com.tencent.liteav.v1livepusherdemo.customcapture.utils.Size;
import com.tencent.liteav.v1livepusherdemo.opengl.GLConstants;
import com.tencent.liteav.v1livepusherdemo.opengl.GPUImageFilter;
import com.tencent.liteav.v1livepusherdemo.opengl.OpenGlUtils;
import com.tencent.liteav.v1livepusherdemo.opengl.Rotation;
import com.tencent.liteav.v1livepusherdemo.opengl.helper.EglCore;

import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;

@TargetApi(17)
public class V1LivePusherCustomRender {
    public static final String TAG = "V1LivePusherCustomRender";

    private final HandlerThread mGLThread;
    private final GLHandler     mGLHandler;
    private       TextureView   mRenderView;

    private EglCore        mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private Size           mSurfaceSize    = new Size();
    private Size           mLastInputSize  = new Size();
    private Size           mLastOutputSize = new Size();

    private final FloatBuffer    mGLCubeBuffer;
    private final FloatBuffer    mGLTextureBuffer;
    private       GPUImageFilter mNormalFilter;

    public V1LivePusherCustomRender() {
        mGLCubeBuffer = OpenGlUtils.createNormalCubeVerticesBuffer();
        mGLTextureBuffer = OpenGlUtils.createTextureCoordsBuffer(Rotation.NORMAL, false, false);

        mGLThread = new HandlerThread(TAG);
        mGLThread.start();
        mGLHandler = new GLHandler(mGLThread.getLooper());
    }

    public void start(TextureView videoView) {
        if (videoView == null) {
            Log.w(TAG, "start error when render view is null");
            return;
        }
        Log.i(TAG, "start render");

        // 设置TextureView的SurfaceTexture生命周期回调，用于管理GLThread的创建和销毁
        mRenderView = videoView;
        if (mRenderView.getWidth() != 0 && mRenderView.getHeight() != 0) {
            mSurfaceTexture = mRenderView.getSurfaceTexture();
            mSurfaceSize.width = mRenderView.getWidth();
            mSurfaceSize.height = mRenderView.getHeight();
        }

        mRenderView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // 保存surfaceTexture，用于创建OpenGL线程
                mSurfaceTexture = surface;
                mSurfaceSize = new Size(width, height);
                Log.i(TAG, String.format("onSurfaceTextureAvailable width: %d, height: %d", width, height));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mSurfaceSize = new Size(width, height);
                Log.i(TAG, String.format("onSurfaceTextureSizeChanged width: %d, height: %d", width, height));
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                // surface释放了，需要停止渲染
                mSurfaceTexture = null;
                // 等待Runnable执行完，再返回，否则GL线程会使用一个无效的SurfaceTexture
                mGLHandler.runAndWaitDone(new Runnable() {
                    @Override
                    public void run() {
                        uninitGlComponent();
                    }
                });
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
    }

    public void stop() {
        if (mRenderView != null) {
            mRenderView.setSurfaceTextureListener(null);
        }
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                destroyInternal();
            }
        });
    }

    public void renderVideoFrame(final TextureFrame frame) {
        // 等待frame.texture的纹理绘制完成
        GLES20.glFinish();
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                renderInternal(frame);
            }
        });
    }

    private void initGlComponent(Object eglContext) {
        if (mSurfaceTexture == null) {
            return;
        }

        // 创建的时候，增加判断，防止这边创建的时候，传入的EGLContext已经被销毁了。
        try {
            if (eglContext instanceof javax.microedition.khronos.egl.EGLContext) {
                mEglCore = new EglCore((javax.microedition.khronos.egl.EGLContext) eglContext, new Surface(mSurfaceTexture));
            } else {
                mEglCore = new EglCore((android.opengl.EGLContext) eglContext, new Surface(mSurfaceTexture));
            }
        } catch (Exception e) {
            Log.e(TAG, "create EglCore failed.", e);
            return;
        }

        mEglCore.makeCurrent();
        mNormalFilter = new GPUImageFilter();
        mNormalFilter.init();
    }

    private void renderInternal(TextureFrame frame) {
        if (mEglCore == null && mSurfaceTexture != null) {
            initGlComponent(frame.eglContext);
        }

        if (mEglCore == null) {
            return;
        }

        if (mLastInputSize.width != frame.width || mLastInputSize.height != frame.height
                || mLastOutputSize.width != mSurfaceSize.width || mLastOutputSize.height != mSurfaceSize.height) {
            Pair<float[], float[]> cubeAndTextureBuffer = OpenGlUtils.calcCubeAndTextureBuffer(GLConstants.GLScaleType.FIT_CENTER,
                    Rotation.NORMAL, false, frame.width, frame.height, mSurfaceSize.width, mSurfaceSize.height);
            mGLCubeBuffer.clear();
            mGLCubeBuffer.put(cubeAndTextureBuffer.first);
            mGLTextureBuffer.clear();
            mGLTextureBuffer.put(cubeAndTextureBuffer.second);

            mLastInputSize = new Size(frame.width, frame.height);
            mLastOutputSize = new Size(mSurfaceSize.width, mSurfaceSize.height);
        }

        mEglCore.makeCurrent();
        GLES20.glViewport(0, 0, mSurfaceSize.width, mSurfaceSize.height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        mNormalFilter.onDraw(frame.textureId, mGLCubeBuffer, mGLTextureBuffer);
        mEglCore.swapBuffer();
    }

    private void uninitGlComponent() {
        if (mNormalFilter != null) {
            mNormalFilter.destroy();
            mNormalFilter = null;
        }
        if (mEglCore != null) {
            mEglCore.unmakeCurrent();
            mEglCore.destroy();
            mEglCore = null;
        }
    }

    private void destroyInternal() {
        uninitGlComponent();

        if (Build.VERSION.SDK_INT >= 18) {
            mGLHandler.getLooper().quitSafely();
        } else {
            mGLHandler.getLooper().quit();
        }
    }

    public static class GLHandler extends Handler {
        public GLHandler(Looper looper) {
            super(looper);
        }

        public void runAndWaitDone(final Runnable runnable) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
