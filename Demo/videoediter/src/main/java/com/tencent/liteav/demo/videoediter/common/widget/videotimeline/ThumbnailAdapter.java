package com.tencent.liteav.demo.videoediter.common.widget.videotimeline;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.tencent.liteav.demo.videoediter.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/11/6.
 */

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_THUMBNAIL = 3;

    private int mViewWidth;
    private int mCount;
    private List<Bitmap> mThumbnailList;

    public ThumbnailAdapter(int viewWidth) {
        mViewWidth = viewWidth;
        mThumbnailList = new ArrayList<>();
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbnailViewHolder viewHolder;
        View itemView;
        switch (viewType) {
            case TYPE_HEADER:
            case TYPE_FOOTER:
                itemView = new View(parent.getContext());
                itemView.setLayoutParams(new ViewGroup.LayoutParams(mViewWidth / 2, ViewGroup.LayoutParams.MATCH_PARENT));
                itemView.setBackgroundColor(Color.TRANSPARENT);
                viewHolder = new ThumbnailViewHolder(itemView);
                return viewHolder;

            case TYPE_THUMBNAIL:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_progress_thumbnail, null);
                viewHolder = new ThumbnailViewHolder(itemView);
                viewHolder.ivThumbnail = (ImageView) itemView.findViewById(R.id.iv_video_progress_thumbnail);
                return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        if (position != 0 && position != mCount + 1) {
            Bitmap thumbnailBitmap = mThumbnailList.get(position - 1);
            holder.ivThumbnail.setImageBitmap(thumbnailBitmap);
        }
    }

    @Override
    public int getItemCount() {
        if (mCount == 0) {
            return 0;
        }

        return mCount + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == mCount + 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_THUMBNAIL;
        }
    }

    @Override
    public void onViewRecycled(ThumbnailViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.ivThumbnail != null) {
            holder.ivThumbnail.setImageBitmap(null);
        }
    }

    public void addThumbnail(Bitmap bitmap) {
        mThumbnailList.add(bitmap);
        mCount = mThumbnailList.size();
        notifyDataSetChanged();
    }

    public void addAllThumbnail(List<Bitmap> bitmapList) {
        mThumbnailList.clear();
        mThumbnailList.addAll(bitmapList);
        mCount = mThumbnailList.size();
        notifyDataSetChanged();
    }

    public void clearAll() {
        mThumbnailList.clear();
        mCount = mThumbnailList.size();
        notifyDataSetChanged();
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
        }
    }

}
