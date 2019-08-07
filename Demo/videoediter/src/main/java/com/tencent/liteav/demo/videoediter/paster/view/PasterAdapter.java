package com.tencent.liteav.demo.videoediter.paster.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.paster.TCPasterInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/10/27.
 */

public class PasterAdapter extends RecyclerView.Adapter<PasterAdapter.PasterViewHolder> implements View.OnClickListener {

    private List<TCPasterInfo> mPasterInfoList;
    private WeakReference<RecyclerView> mRecyclerView;
    private OnItemClickListener mOnItemClickListener;

    public PasterAdapter(List<TCPasterInfo> pasterInfoList) {
        if (pasterInfoList == null) {
            mPasterInfoList = new ArrayList<TCPasterInfo>();
        } else {
            mPasterInfoList = pasterInfoList;
        }
    }

    @Override
    public PasterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mRecyclerView == null) {
            mRecyclerView = new WeakReference<RecyclerView>((RecyclerView) parent);
        }
        return new PasterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_paster_view, null));
    }

    @Override
    public void onBindViewHolder(PasterViewHolder holder, int position) {
        holder.itemView.setOnClickListener(this);
        Glide.with(holder.itemView.getContext()).load(mPasterInfoList.get(position).getIconPath()).into(holder.ivPaster);
    }

    @Override
    public int getItemCount() {
        return mPasterInfoList.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener == null) {
            return;
        }
        RecyclerView recyclerView = mRecyclerView.get();
        if (recyclerView != null) {
            int position = recyclerView.getChildAdapterPosition(view);
            mOnItemClickListener.onItemClick(mPasterInfoList.get(position), position);
        }
    }

    class PasterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPaster;

        public PasterViewHolder(View itemView) {
            super(itemView);
            ivPaster = (ImageView) itemView.findViewById(R.id.iv_paster);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(TCPasterInfo tcPasterInfo, int position);
    }
}
