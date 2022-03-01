package com.tencent.liteav.v1livepusherdemo.itemview;

import android.content.Context;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tencent.liteav.v1livepusherdemo.R;

/**
 * 带checkbox的item
 *
 * @author guanyifeng
 */
public class CheckBoxSettingItem extends BaseSettingItem {
    private static final String TAG = "CheckBoxSettingItem";

    private final ClickListener  mListener;
    private       ItemViewHolder mItemViewHolder;

    public CheckBoxSettingItem(Context context,
                               @NonNull ItemText itemText,
                               ClickListener listener) {
        super(context, itemText);
        mItemViewHolder = new ItemViewHolder(
                mInflater.inflate(R.layout.v1livepusher_live_pusher_item_setting_checkbox, null)
        );
        mListener = listener;
    }

    public CheckBoxSettingItem setCheck(final boolean check) {
        if (mItemViewHolder != null) {
            mItemViewHolder.mItemCb.post(new Runnable() {
                @Override
                public void run() {
                    mItemViewHolder.mItemCb.setChecked(check);
                }
            });
        }
        return this;
    }

    @Override
    public View getView() {
        if (mItemViewHolder != null) {
            return mItemViewHolder.rootView;
        }
        return null;
    }

    public boolean getChecked() {
        return mItemViewHolder.mItemCb.isChecked();
    }

    public interface ClickListener {
        void onClick();
    }

    public class ItemViewHolder {
        public  View     rootView;
        private TextView mTitle;
        private CheckBox mItemCb;

        public ItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.v1livepusher_title);
            mItemCb = (CheckBox) itemView.findViewById(R.id.v1livepusher_cb_item);

            if (mItemText == null) {
                Log.e(TAG, "item text get null here");
                return;
            }

            mTitle.setText(mItemText.title);
            mItemCb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick();
                    }
                }
            });
        }
    }
}

