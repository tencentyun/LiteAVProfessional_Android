package com.tencent.liteav.trtcdemo.model.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.tencent.liteav.trtcdemo.model.opengl.helper.EglCore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class CustomGLContextUtils {
    private static final String TAG = "CustomGLContextThread";
    
    private SyncHandler mSyncHandler;
    private EglCore     mEGLCore;
    private Object      mGLContext;
    
    public synchronized void initSync() {
        Log.i(TAG, "createGLContextSync: ");
        if (mSyncHandler == null) {
            HandlerThread handlerThread = new HandlerThread("demo_glContext_thread");
            handlerThread.start();
            mSyncHandler = new SyncHandler(handlerThread.getLooper());
        }
        if (mGLContext == null) {
            mSyncHandler.runAndWaitDone(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "createGLContextSync: start create.");
                    mEGLCore = new EglCore(720, 1280);
                    mEGLCore.makeCurrent();
                    mGLContext = mEGLCore.getEglContext();
                    Log.i(TAG, "createGLContextSync: create finish. context: " + mGLContext.toString());
                }
            });
        }
    }
    
    public synchronized Object getGLContext() {
        return mGLContext;
    }
    
    public synchronized void releaseSync() {
        Log.i(TAG, "releaseSync: ");
        if (mSyncHandler != null) {
            SyncHandler syncHandler = mSyncHandler;
            mSyncHandler = null;
            final EglCore core = mEGLCore;
            mEGLCore = null;
            mGLContext = null;
            syncHandler.runAndWaitDone(new Runnable() {
                @Override
                public void run() {
                    core.destroy();
                    Log.i(TAG, "releaseSync:  release gl context finish.");
                }
            });
            syncHandler.getLooper().quit();
        }
    }
    
    public class SyncHandler extends Handler {
        public SyncHandler(Looper looper) {
            super(looper);
        }
        
        public boolean runAndWaitDone(final Runnable runnable) {
            return runAndWaitDone(runnable, -1);
        }
        
        public boolean runAndWaitDone(final Runnable runnable, final long timeoutMS) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            boolean success = post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    countDownLatch.countDown();
                }
            });
            
            if (success) {
                try {
                    if (timeoutMS > 0) {
                        countDownLatch.await(timeoutMS, TimeUnit.MILLISECONDS);
                    } else {
                        countDownLatch.await();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return success;
        }
    }
    
    
}
