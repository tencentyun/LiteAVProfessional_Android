package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.rtmp.TXLog;

/**
 * 带seekbar的item
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class SeekBarSettingItem extends BaseSettingItem {
    private static final String TAG = RadioButtonSettingItem.class.getName();

    private final Listener       mListener;
    private       ItemViewHolder mItemViewHolder;

    public SeekBarSettingItem(Context context,
                              @NonNull ItemText itemText,
                              Listener listener) {
        super(context, itemText);
        mItemViewHolder = new ItemViewHolder(
                mInflater.inflate(R.layout.trtcdemo_item_setting_seekbar, null)
        );
        mListener = listener;
    }

    public int getMax() {
        return mItemViewHolder.mItemSb.getMax();
    }

    public SeekBarSettingItem setMax(final int max) {
        mItemViewHolder.mItemSb.post(new Runnable() {
            @Override
            public void run() {
                mItemViewHolder.mItemSb.setMax(max);
            }
        });
        return this;
    }

    public SeekBarSettingItem setMix(final int min) {
        mItemViewHolder.mItemSb.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mItemViewHolder.mItemSb.setMin(min);
                }
            }
        });
        return this;
    }

    public int getProgress() {
        return mItemViewHolder.mItemSb.getProgress();
    }

    public SeekBarSettingItem setProgress(final int progress) {
        mItemViewHolder.mItemSb.post(new Runnable() {
            @Override
            public void run() {
                mItemViewHolder.mItemSb.setProgress(progress);
            }
        });
        return this;
    }

    public SeekBarSettingItem setTips(final String tips) {
        mItemViewHolder.mTipsTv.setText(tips);
        return this;
    }

    @Override
    public View getView() {
        if (mItemViewHolder != null) {
            return mItemViewHolder.rootView;
        }
        return null;
    }

    public interface Listener {
        void onSeekBarChange(int progress, boolean fromUser);
    }

    public class ItemViewHolder {
        public View     rootView;
        public TextView mTitle;
        public SeekBar  mItemSb;
        public TextView mTipsTv;

        public ItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mItemSb = (SeekBar) itemView.findViewById(R.id.sb_item);
            mTipsTv = (TextView) itemView.findViewById(R.id.tv_tips);
            if (mItemText == null) {
                TXLog.e(TAG, "item text get null here");
                return;
            }
            mTitle.setText(mItemText.title);
            mItemSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mListener != null) {
                        mListener.onSeekBarChange(progress, fromUser);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }
}
