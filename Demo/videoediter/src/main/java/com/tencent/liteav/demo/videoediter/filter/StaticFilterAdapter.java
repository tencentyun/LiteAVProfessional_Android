package com.tencent.liteav.demo.videoediter.filter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.common.widget.BaseRecyclerAdapter;

import java.util.List;

/**
 * Created by hans on 2017/11/6.
 */

public class StaticFilterAdapter extends BaseRecyclerAdapter<StaticFilterAdapter.FilterViewHolder> {
    private List<Integer> mFilterList;
    private int mCurrentSelectedPos;

    public StaticFilterAdapter(List<Integer> list) {
        mFilterList = list;
    }


    public void setCurrentSelectedPos(int pos) {
        int tPos = mCurrentSelectedPos;
        mCurrentSelectedPos = pos;
        this.notifyItemChanged(tPos);
        this.notifyItemChanged(mCurrentSelectedPos);
    }

    @Override
    public void onBindVH(FilterViewHolder holder, int position) {
        //Glide.with(holder.itemView.getContext()).load(mFilterList.get(position)).into(holder.ivImage);
        holder.ivImage.setImageResource(mFilterList.get(position));
        if (mCurrentSelectedPos == position) {
            holder.ivImageTint.setVisibility(View.VISIBLE);
        } else {
            holder.ivImageTint.setVisibility(View.GONE);
        }
    }


    @Override
    public FilterViewHolder onCreateVH(ViewGroup parent, int viewType) {
        return new FilterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_layout, parent, false));
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivImageTint;

        public FilterViewHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.filter_image);
            ivImageTint = (ImageView) itemView.findViewById(R.id.filter_image_tint);
        }
    }
}
