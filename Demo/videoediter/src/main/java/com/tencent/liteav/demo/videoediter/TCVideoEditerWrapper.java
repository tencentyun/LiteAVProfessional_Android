package com.tencent.liteav.demo.videoediter;

import android.graphics.Bitmap;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/10/26.
 * <p>
 * 由于SDK提供的TXVideoEditer为非单例模式
 * 当您需要在多个Activity\Fragment 之间对同一个Video进行编辑的时候，可以在上层将其包装为一个单例
 * <p>
 * 需要注意：
 * 完成一次视频编辑后，请务必调用{@link TCVideoEditerWrapper#clear()}晴空相关的一些配置
 */
public class TCVideoEditerWrapper {
    private static final String TAG = "TCVideoEditerWrapper";
    private static TCVideoEditerWrapper INSTANCE;
    private TXVideoEditer mTXVideoEditer;
    private TXVideoEditConstants.TXVideoInfo mTXVideoInfo;
    public static final int mThumbnailCount = 10;//快速导入，默认显示10张缩略图
    /**
     * 缩略图相关
     */
    private List<ThumbnailBitmapInfo> mThumbnailList;               // 将已经加在好的Bitmap缓存起来

    /**
     * 预览相关
     * <p>
     * 由于SDK没有提供多个Listener的预览进度的回调，所以在上层包装一下
     */
    private List<TXVideoPreviewListenerWrapper> mPreviewWrapperList;
    private boolean mIsReverse;

    private long mCutterDuration;                                   // 裁剪的总时长
    private long mCutterStartTime;                                  // 裁剪开始的时间
    private long mCutterEndTime;                                    // 裁剪结束的时间

    public static TCVideoEditerWrapper getInstance() {
        if (INSTANCE == null) {
            synchronized (TCVideoEditerWrapper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TCVideoEditerWrapper();
                }
            }
        }
        return INSTANCE;
    }

    private TCVideoEditerWrapper() {
        mThumbnailList = new ArrayList<>();
        mPreviewWrapperList = new ArrayList<>();
        mIsReverse = false;
    }

    public void setTXVideoInfo(TXVideoEditConstants.TXVideoInfo info) {
        mTXVideoInfo = info;
    }

    /**
     * 获取视频的信息
     *
     * @return
     */
    public TXVideoEditConstants.TXVideoInfo getTXVideoInfo() {
        return mTXVideoInfo;
    }

    public void setEditer(TXVideoEditer editer) {
        mTXVideoEditer = editer;
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setTXVideoPreviewListener(mPreviewListener);
        }
    }

    public TXVideoEditer getEditer() {
        return mTXVideoEditer;
    }


    public void clear() {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setTXVideoPreviewListener(null);
            mTXVideoEditer = null;
        }

        mCutterDuration = 0;
        mCutterStartTime = 0;
        mCutterEndTime = 0;

        mThumbnailList.clear();

        synchronized (mPreviewWrapperList) {
            mPreviewWrapperList.clear();
        }
        mIsReverse = false;
        if (mTXVideoInfo != null) {
            if (mTXVideoInfo.coverImage != null && !mTXVideoInfo.coverImage.isRecycled()) {
                mTXVideoInfo.coverImage.recycle();
                mTXVideoInfo.coverImage = null;
            }
        }
    }

    /**
     * 裁剪后的时间
     *
     * @param newVideoDuration
     */
    public void setCutterDuration(long newVideoDuration) {
        mCutterDuration = newVideoDuration;
    }

    /**
     * 获取裁剪后的时间
     *
     * @return
     */
    public long geCutterDuration() {
        return mCutterDuration;
    }


    public void setCutterStartTime(long startTime, long endTime) {
        mCutterStartTime = startTime;
        mCutterEndTime = endTime;
        mCutterDuration = endTime - startTime;
    }

    public long getCutterStartTime() {
        return mCutterStartTime;
    }

    public long getCutterEndTime() {
        return mCutterEndTime;
    }


    /**
     * ======================================================预览相关======================================================
     */


    public void setReverse(boolean isReverse) {
        mIsReverse = isReverse;
    }

    public boolean isReverse() {
        return mIsReverse;
    }

    private TXVideoEditer.TXVideoPreviewListenerEx mPreviewListener = new TXVideoEditer.TXVideoPreviewListenerEx() {
        @Override
        public void onPreviewError(TXVideoEditConstants.TXPreviewError error) {
            synchronized (mPreviewWrapperList) {
                for (TXVideoPreviewListenerWrapper wrapper : mPreviewWrapperList) {
                    wrapper.onPreviewError(error);
                }
            }
        }

        @Override
        public void onPreviewProgress(int time) {
            int currentTimeMs = (int) (time / 1000);//转为ms值
            synchronized (mPreviewWrapperList) {
                for (TXVideoPreviewListenerWrapper wrapper : mPreviewWrapperList) {
                    wrapper.onPreviewProgressWrapper(currentTimeMs);
                }
            }
        }

        @Override
        public void onPreviewFinished() {
            TXCLog.i(TAG, "mPreviewListener, onPreviewFinished");
            synchronized (mPreviewWrapperList) {
                for (TXVideoPreviewListenerWrapper wrapper : mPreviewWrapperList) {
                    wrapper.onPreviewFinishedWrapper();
                }
            }
        }
    };

    public void addTXVideoPreviewListenerWrapper(TXVideoPreviewListenerWrapper listener) {
        synchronized (mPreviewWrapperList) {
            if (mPreviewWrapperList.contains(listener)) {
                return;
            }
            mPreviewWrapperList.add(listener);
        }
    }

    public void removeTXVideoPreviewListenerWrapper(TXVideoPreviewListenerWrapper listener) {
        synchronized (mPreviewWrapperList) {
            mPreviewWrapperList.remove(listener);
        }
    }

    /**
     * 由于SDK没有提供多个界面的预览进度的回调，所以在上层包装一下
     */
    public interface TXVideoPreviewListenerWrapper {
        /**
         * @param time
         */
        void onPreviewProgressWrapper(int time);

        void onPreviewFinishedWrapper();

        void onPreviewError(TXVideoEditConstants.TXPreviewError error);
    }

    /**
     * ======================================================缩略图相关======================================================
     */

    /**
     * 获取已经加载的缩略图
     *
     * @return
     */
    public List<Bitmap> getThumbnailList(long startPts, long endPts) {
        List<Bitmap> list = new ArrayList<>();
        for (ThumbnailBitmapInfo info : mThumbnailList) {
            if (info.ptsMs >= startPts && info.ptsMs <= endPts) {
                list.add(info.bitmap);
            }
        }
        return list;
    }

    public List<Bitmap> getAllThumbnails() {
        return getThumbnailList(0, mTXVideoInfo != null ? mTXVideoInfo.duration : 0);
    }

    public void addThumbnailBitmap(long timeMs, Bitmap bitmap) {
        mThumbnailList.add(new ThumbnailBitmapInfo(timeMs, bitmap));
    }

    public void cleaThumbnails() {
        for (ThumbnailBitmapInfo info : mThumbnailList) {
            if (info != null && info.bitmap != null && !info.bitmap.isRecycled()) {
                info.bitmap.recycle();
                info.bitmap = null;
            }
        }
        mThumbnailList.clear();
    }

    private class ThumbnailBitmapInfo {
        public long ptsMs;
        public Bitmap bitmap;

        public ThumbnailBitmapInfo(long ptsMs, Bitmap bitmap) {
            this.ptsMs = ptsMs;
            this.bitmap = bitmap;
        }
    }

}
