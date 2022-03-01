package com.tencent.liteav.trtcdemo.ui.widget.videolayout;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;

public class TRTCVideoLayoutManager extends RelativeLayout {
    private final static String                      TAG      = "TRTCVideoLayoutManager";
    private              ArrayList<TRTCLayoutHolder> mLayoutHolderList;
    private              ArrayList<LayoutParams>     mLayoutParamList;
    public static final  int                         MAX_USER = 11;
    private              String                      mSelfUserId;

    private boolean mIsLayoutParamInit = false;

    public TRTCVideoLayoutManager(Context context) {
        super(context);
        initView(context);
    }

    public TRTCVideoLayoutManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TRTCVideoLayoutManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mLayoutHolderList = new ArrayList();
        for (int i = 0; i < MAX_USER; i++) {
            TRTCVideoLayout videoLayout = new TRTCVideoLayout(context);
            videoLayout.setVisibility(View.GONE);
            videoLayout.setBackgroundColor(Color.BLACK);
            videoLayout.setMoveable(false);
            TRTCLayoutHolder entity = new TRTCLayoutHolder();
            entity.layout = videoLayout;
            entity.index = i;
            mLayoutHolderList.add(entity);
            if (i == 0) {
                entity.layout.setMoveable(false);
            } else {
                entity.layout.setMoveable(true);
            }
            addView(entity.layout, -1);
        }
    }

    /**
     * 根据 userId 和视频类型，找到已经分配的 View
     */
    public TXCloudVideoView findCloudVideoView(String userId, int streamType) {
        if (userId == null) return null;
        for (TRTCLayoutHolder layoutEntity : mLayoutHolderList) {
            if (layoutEntity.userId.equals(userId)) {
                if (layoutEntity.streamType == streamType) {
                    return layoutEntity.layout.getVideoView();
                } else if (layoutEntity.streamType != TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB && streamType != TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB) {
                    layoutEntity.streamType = streamType;
                    return layoutEntity.layout.getVideoView();
                }
            }
        }
        return null;
    }

    /**
     * 根据 userId 和 视频类型（大、小、辅路）画面分配对应的 view
     *
     * @param userId
     * @param streamType
     * @return
     */
    public TXCloudVideoView allocCloudVideoView(String userId, int streamType, int viewType) {
        if (userId == null) return null;
        for (TRTCLayoutHolder layoutEntity : mLayoutHolderList) {
            if (layoutEntity.userId.equals("")) {
                layoutEntity.userId = userId;
                layoutEntity.streamType = streamType;
                layoutEntity.layout.setVisibility(VISIBLE);
                layoutEntity.layout.updateNoVideoLayout("", View.GONE);
                layoutEntity.layout.setViewType(viewType);
                return layoutEntity.layout.getVideoView();
            }
        }
        return null;
    }

    private TRTCLayoutHolder findEntity(String userId) {
        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            if (entity.userId.equals(userId)) return entity;
        }
        return null;
    }

    public void recyclerCloudViewView(String userId, int streamType) {
        if (userId == null) return;

        TRTCLayoutHolder entity0 = mLayoutHolderList.get(0);
        if (entity0 != null) {
            if (userId.equals(entity0.userId) && entity0.streamType == streamType) {
                TRTCLayoutHolder myEntity = findEntity(mSelfUserId);
                if (myEntity != null) {
                    makeFullVideoView(myEntity.index);
                }
            }
        }

        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            if (entity.streamType == streamType && userId.equals(entity.userId)) {
                entity.layout.setVisibility(GONE);
                entity.userId = "";
                entity.streamType = -1;
                break;
            }
        }
    }

    /**
     * 隐藏所有音量的进度条
     */
    public void hideAllAudioVolumeProgressBar() {
        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            entity.layout.setAudioVolumeProgressBarVisibility(View.GONE);
        }
    }

    /**
     * 显示所有音量的进度条
     */
    public void showAllAudioVolumeProgressBar() {
        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            entity.layout.setAudioVolumeProgressBarVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置当前音量
     */
    public void updateAudioVolume(String userId, int audioVolume) {
        if (userId == null) return;
        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setAudioVolumeProgress(audioVolume);
                }
            }
        }
    }

    /**
     * 更新网络质量
     *
     * @param userId
     * @param quality
     */
    public void updateNetworkQuality(String userId, int quality) {
        if (userId == null) return;
        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.updateNetworkQuality(quality);
                }
            }
        }
    }

    /**
     * 更新当前视频状态
     */
    public void updateVideoStatus(String userId, boolean bHasVideo) {
        if (userId == null) return;
        if (TextUtils.isEmpty(mLayoutHolderList.get(0).userId)) {
            for (TRTCLayoutHolder entity : mLayoutHolderList) {
                if (!TextUtils.isEmpty(entity.userId)) {
                    makeFullVideoView(entity.index);
                }
            }
        }
        for (TRTCLayoutHolder entity : mLayoutHolderList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId) && entity.streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG) {
                    entity.layout.updateNoVideoLayout(userId, bHasVideo ? GONE : VISIBLE);
                    break;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mIsLayoutParamInit) {
            initLayoutParamList(getWidth(), getHeight());
            mIsLayoutParamInit = true;
        }
    }

    private void initLayoutParamList(int layoutWidth, int layoutHeight) {
        if (mLayoutParamList == null || mLayoutParamList.size() <= 0) {
            mLayoutParamList = new ArrayList();

            RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mLayoutParamList.add(layoutParams0);

            final int midMargin = SizeUtils.dp2px(10);
            final int lrMargin = SizeUtils.dp2px(15);
            final int bottomMargin = SizeUtils.dp2px(60);
            final int subHeight = (int) ((layoutHeight - 3 * bottomMargin) / 5f);
            final int subWidth = (int) (subHeight * 9 / 16f);

            for (int i = 0; i < 5; i++) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(subWidth, subHeight);
                layoutParams.leftMargin = layoutWidth - lrMargin - subWidth;
                layoutParams.topMargin = layoutHeight - (bottomMargin + midMargin * (i + 1) + subHeight * i) - subHeight;
                mLayoutParamList.add(layoutParams);
            }

            for (int i = 0; i < 5; i++) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(subWidth, subHeight);
                layoutParams.leftMargin = lrMargin;
                layoutParams.topMargin = layoutHeight - (bottomMargin + midMargin * (i + 1) + subHeight * i) - subHeight;
                mLayoutParamList.add(layoutParams);
            }
        }
        for (int i = 0; i < mLayoutHolderList.size(); i++) {
            TRTCLayoutHolder entity = mLayoutHolderList.get(i);
            LayoutParams layoutParams = mLayoutParamList.get(i);
            entity.layout.setLayoutParams(layoutParams);
            addFloatViewClickListener(entity.layout);
        }
    }

    /**
     * 堆叠模式下，将 index 号的 view 换到 0 号位，全屏化渲染
     *
     * @param index
     */
    private void makeFullVideoView(int index) {// 1 -> 0
        if (index <= 0 || mLayoutHolderList.size() <= index) return;
        Log.i(TAG, "makeFullVideoView: from = " + index);
        TRTCLayoutHolder indexEntity = mLayoutHolderList.get(index);
        ViewGroup.LayoutParams indexParams = indexEntity.layout.getLayoutParams();

        TRTCLayoutHolder fullEntity = mLayoutHolderList.get(0);
        ViewGroup.LayoutParams fullVideoParams = fullEntity.layout.getLayoutParams();

        indexEntity.layout.setLayoutParams(fullVideoParams);
        indexEntity.index = 0;

        fullEntity.layout.setLayoutParams(indexParams);
        fullEntity.index = index;

        indexEntity.layout.setMoveable(false);
        indexEntity.layout.setOnClickListener(null);

        fullEntity.layout.setMoveable(true);
        addFloatViewClickListener(fullEntity.layout);

        mLayoutHolderList.set(0, indexEntity); // 将 fromView 塞到 0 的位置
        mLayoutHolderList.set(index, fullEntity);

        for (int i = 0; i < mLayoutHolderList.size(); i++) {
            TRTCLayoutHolder entity = mLayoutHolderList.get(i);
            // 需要对 View 树的 zOrder 进行重排，否则在 RelativeLayout 下，存在遮挡情况
            bringChildToFront(entity.layout);
        }
    }

    private void addFloatViewClickListener(final TRTCVideoLayout view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TRTCLayoutHolder entity : mLayoutHolderList) {
                    if (entity.layout == v) {
                        makeFullVideoView(entity.index);
                        break;
                    }
                }
            }
        });
    }

    public void setMySelfUserId(String userId) {
        mSelfUserId = userId;
    }

}
