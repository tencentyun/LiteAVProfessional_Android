package com.tencent.liteav.demo.videojoiner.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.videojoiner.R;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.SwipeMenuAdapter;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.DateTimeUtil;

import java.util.List;

public class MenuAdapter extends SwipeMenuAdapter<MenuAdapter.DefaultViewHolder> {

    private Context mContext;
    private List<TCVideoFileInfo> mTCVideoFileInfoList;
    private TCVideoJoinerActivity.OnDeleteListener mOnDeleteListener;

    public MenuAdapter(Context context, List<TCVideoFileInfo> fileInfos) {
        mContext = context;
        this.mTCVideoFileInfoList = fileInfos;
    }

    public void removeIndex(int position) {
        this.mTCVideoFileInfoList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mTCVideoFileInfoList.size());
    }

    public void setOnItemDeleteListener(TCVideoJoinerActivity.OnDeleteListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    @Override
    public int getItemCount() {
        return mTCVideoFileInfoList == null ? 0 : mTCVideoFileInfoList.size();
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.ugcjoin_swipe_menu_item, parent, false);
    }

    @Override
    public MenuAdapter.DefaultViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new DefaultViewHolder(realContentView);
    }

    @Override
    public void onBindViewHolder(MenuAdapter.DefaultViewHolder holder, int position) {
        TCVideoFileInfo fileInfo = mTCVideoFileInfoList.get(position);
        holder.setTitle(fileInfo.getFileName());
        holder.setDuration(DateTimeUtil.duration(fileInfo.getDuration()));
        holder.setOnDeleteListener(mOnDeleteListener);
        Glide.with(mContext).load(fileInfo.getFileUri()).into(holder.ivThumb);
    }

    static class DefaultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivThumb;
        TextView tvDuration;
        LinearLayout ivDelete;
        TextView tvTitle;
        TCVideoJoinerActivity.OnDeleteListener mOnDeleteListener;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            ivDelete = (LinearLayout) itemView.findViewById(R.id.ly_delete);
            ivDelete.setOnClickListener(this);
        }

        public void setOnDeleteListener(TCVideoJoinerActivity.OnDeleteListener onDeleteListener) {
            this.mOnDeleteListener = onDeleteListener;
        }

        public void setTitle(String title) {
            this.tvTitle.setText(title);
        }

        public void setDuration(String duration) {
            this.tvDuration.setText(duration);
        }

        @Override
        public void onClick(View v) {
            if (mOnDeleteListener != null) {
                mOnDeleteListener.onDelete(getAdapterPosition());
            }
        }
    }

}
