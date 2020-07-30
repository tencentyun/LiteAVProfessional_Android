package com.tencent.liteav.demo.videoediter.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.videoediter.R;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.DateTimeUtil;

import java.util.ArrayList;

public class TCVideoEditerListAdapter extends RecyclerView.Adapter<TCVideoEditerListAdapter.ViewHolder> {

    private Context                    mContext;
    private ArrayList<TCVideoFileInfo> mData = new ArrayList<>();
    private ArrayList<TCVideoFileInfo> mInOrderFileInfoList;
    private boolean                    mMultiplePick;
    private int                        mLastSelected = -1;

    public TCVideoEditerListAdapter(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.ugcedit_item_videoedit_video, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TCVideoFileInfo fileInfo = mData.get(position);
        holder.ivSelected.setVisibility(fileInfo.isSelected() ? View.VISIBLE : View.GONE);
        if (fileInfo.getFileType() == TCVideoFileInfo.FILE_TYPE_PICTURE) {
            holder.duration.setText("");
        } else {
            holder.duration.setText(DateTimeUtil.formattedTime(fileInfo.getDuration() / 1000));
        }
        Glide.with(mContext).load(fileInfo.getFileUri()).dontAnimate().into(holder.thumb);
        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMultiplePick) {
                    changeMultiSelection(position);
                } else {
                    changeSingleSelection(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setMultiplePick(boolean multiplePick) {
        mMultiplePick = multiplePick;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumb;
        private final TextView duration;
        private final ImageView ivSelected;

        public ViewHolder(final View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
            duration = (TextView) itemView.findViewById(R.id.tv_duration);
            ivSelected = (ImageView) itemView.findViewById(R.id.iv_selected);
        }
    }

    public ArrayList<TCVideoFileInfo> getMultiSelected() {
        ArrayList<TCVideoFileInfo> infos = new ArrayList<TCVideoFileInfo>();

        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isSelected()) {
                infos.add(mData.get(i));
            }
        }
        return infos;
    }

    public ArrayList<TCVideoFileInfo> getInOrderMultiSelected() {
        return mInOrderFileInfoList;
    }

    public TCVideoFileInfo getSingleSelected() {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isSelected()) {
                return mData.get(i);
            }
        }
        return null;
    }

    public void addAll(ArrayList<TCVideoFileInfo> files) {
        try {
            this.mData.clear();
            this.mData.addAll(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void changeSingleSelection(int position) {
        if (mLastSelected != -1) {
            mData.get(mLastSelected).setSelected(false);
        }
        notifyItemChanged(mLastSelected);

        TCVideoFileInfo info = mData.get(position);
        info.setSelected(true);
        notifyItemChanged(position);

        mLastSelected = position;
    }

    public void changeMultiSelection(int position) {
        if (mInOrderFileInfoList == null) {
            mInOrderFileInfoList = new ArrayList<>();
        }

        TCVideoFileInfo fileInfo = mData.get(position);

        if (fileInfo.isSelected()) {
            fileInfo.setSelected(false);
            for (int i = 0; i < mInOrderFileInfoList.size(); i++) {
                TCVideoFileInfo tcVideoFileInfo = mInOrderFileInfoList.get(i);
                if (tcVideoFileInfo.getFilePath().equals(fileInfo.getFilePath())) {
                    mInOrderFileInfoList.remove(i);
                    break;
                }
            }
        } else {
            fileInfo.setSelected(true);
            mInOrderFileInfoList.add(fileInfo);
        }
        notifyItemChanged(position);
    }

}
