package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.rtmp.TXLog;

/**
 * 带checkbox的item
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class SwitchSettingItem extends BaseSettingItem {
    private static final String TAG = SwitchSettingItem.class.getName();

    private final ClickListener  mListener;
    private       ItemViewHolder mItemViewHolder;

    public SwitchSettingItem(Context context,
                             @NonNull ItemText itemText,
                             ClickListener listener) {
        super(context, itemText);
        mItemViewHolder = new ItemViewHolder(
                mInflater.inflate(R.layout.trtcdemo_item_setting_checkbox, null)
        );
        mListener = listener;
    }

    public SwitchSettingItem setCheck(final boolean check) {
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
        public  View            rootView;
        private TextView        mTitle;
        private SwitchCompat    mItemCb;

        public ItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mItemCb = (SwitchCompat) itemView.findViewById(R.id.cb_item);

            if (mItemText == null) {
                TXLog.e(TAG, "item text get null here");
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
