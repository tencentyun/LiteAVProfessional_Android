package com.tencent.liteav.demo.videoediter.common.widget.videotimeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import com.tencent.liteav.demo.videoediter.R;

import java.util.List;

/**
 * Created by vinsonswang on 2017/11/6.
 */

public class VideoProgressView extends FrameLayout {

    private Context mContext;
    private View mRootView;
    private RecyclerView mRecyclerView;
    private int mViewWidth;
    private int mViewHeight;
    private ThumbnailAdapter mThumbnailAdapter;

    public VideoProgressView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoProgressView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_video_progress, this);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_video_thumbnail);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setViewWidth(int viewWidth) {
        mViewWidth = viewWidth;
    }

    public int getViewWidth() {
        return mViewWidth;
    }

    public int getViewHeight() {
        return mViewHeight;
    }

    public void setViewHeight(int mViewHeight) {
        this.mViewHeight = mViewHeight;
    }

    public void setThumbnailData() {
        mThumbnailAdapter = new ThumbnailAdapter(mViewWidth);
        mRecyclerView.setAdapter(mThumbnailAdapter);
    }

    public void addThumbnail(Bitmap bitmap) {
        mThumbnailAdapter.addThumbnail(bitmap);
    }

    public void addAllThumbnail(List<Bitmap> bitmapList) {
        mThumbnailAdapter.addAllThumbnail(bitmapList);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public int getThumbnailCount() {
        return mThumbnailAdapter.getItemCount() - 2;
    }

    public float getSingleThumbnailWidth() {
        return mContext.getResources().getDimension(R.dimen.video_thumbnail_width);
    }

    public ViewGroup getParentView() {
        return (ViewGroup) mRootView;
    }

    public void clearAll() {
        mThumbnailAdapter.clearAll();
    }
}
