package com.tencent.liteav.demo.videoediter.bgm;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.tencent.liteav.demo.videoediter.common.utils.TCBGMInfo;
import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.common.widget.BaseRecyclerAdapter;

import java.util.List;

/**
 * Created by hanszhli on 2017/6/15.
 */

public class TCMusicAdapter extends BaseRecyclerAdapter<TCMusicAdapter.LinearMusicViewHolder> implements View.OnClickListener {
    private List<TCBGMInfo> mBGMList;

    public TCMusicAdapter(List<TCBGMInfo> list) {
        mBGMList = list;
    }


    @Override
    public LinearMusicViewHolder onCreateVH(ViewGroup parent, int viewType) {
        return new LinearMusicViewHolder(View.inflate(parent.getContext(), R.layout.item_editer_bgm, null));
    }


    @Override
    public void onBindVH(LinearMusicViewHolder holder, int position) {
        TCBGMInfo info = mBGMList.get(position);
        holder.tvName.setText(info.getSongName() + "  —  " + info.getSingerName());
        holder.tvDuration.setText(info.getFormatDuration());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mBGMList.size();
    }


    public static class LinearMusicViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDuration;

        public LinearMusicViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.bgm_tv_name);
            tvDuration = (TextView) itemView.findViewById(R.id.bgm_tv_duration);
        }
    }


}
