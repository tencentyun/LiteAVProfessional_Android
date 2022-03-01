package com.tencent.liteav.v1livepusherdemo.fragment;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.liteav.v1livepusherdemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class V1VisibleLogListAdapter extends RecyclerView.Adapter<V1VisibleLogListAdapter.ViewHolder> {
    private final       Context           mContext;
    private             ArrayList<String> data       = new ArrayList<String>();
    private             int               mType;
    public static final int               TYPE_CATON = 1;
    public static final int               TYPE_DROP  = 2;

    public V1VisibleLogListAdapter(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.v1livepusher_visible_log_layout, null);
        return new V1VisibleLogListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String catonNum = data.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String date = sdf.format(System.currentTimeMillis());
        String string;
        if (mType == TYPE_CATON) {
            string = String.format("卡顿警告:出现卡顿%sms[%s]", catonNum, date);
        } else {
            string = String.format("丢帧警告:网络状况不佳[%s]", date);
        }
        holder.warning.setText(string);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(String catonNum) {
        try {
            this.data.add(catonNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.data.clear();
        notifyDataSetChanged();
    }

    public void setType(int type) {
        mType = type;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView warning;

        public ViewHolder(final View itemView) {
            super(itemView);
            warning = (TextView) itemView.findViewById(R.id.v1livepusher_tv_warning);
        }
    }
}
