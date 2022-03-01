package com.tencent.liteav.login.ui.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.CountryCodeInfo;

import java.util.List;

public class CountryCodeListAdapter extends
        RecyclerView.Adapter<CountryCodeListAdapter.ViewHolder> {
    private static final String TAG = "CountryCodeListAdapter";

    private Context                                 mContext;
    private List<CountryCodeInfo.CountryCodeEntity> mCountryCodeList;
    private OnItemClickListener                     mOnItemClickListener;
    private int                                     mSelectPosition = -1;
    private String                                  mLanguage;

    public CountryCodeListAdapter(Context context, String language, List<CountryCodeInfo.CountryCodeEntity> list, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mLanguage = language;
        this.mCountryCodeList = list;
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.login_item_contry_code_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CountryCodeInfo.CountryCodeEntity item = mCountryCodeList.get(position);
        holder.bind(mContext, item, position, mOnItemClickListener);
    }


    @Override
    public int getItemCount() {
        return mCountryCodeList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(CountryCodeInfo.CountryCodeEntity countryCodeEntity);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTvCountryCodeDisplay;
        public TextView mTvCountryCode;

        public ViewHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }

        public void bind(final Context context,
                         final CountryCodeInfo.CountryCodeEntity countryCodeEntity,
                         final int position,
                         final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(countryCodeEntity);
                    if (mSelectPosition != position) {
                        notifyDataSetChanged();
                        mSelectPosition = position;
                    }
                }
            });
            if ("zh".equals(mLanguage)) {
                mTvCountryCodeDisplay.setText(countryCodeEntity.getZh());
            } else {
                mTvCountryCodeDisplay.setText(countryCodeEntity.getEn());
            }
            mTvCountryCode.setText("+" + countryCodeEntity.getCode());
        }

        private void initView(@NonNull final View itemView) {
            mTvCountryCodeDisplay = (TextView) itemView.findViewById(R.id.tv_country_code_display);
            mTvCountryCode = (TextView) itemView.findViewById(R.id.tv_country_code);
        }
    }
}