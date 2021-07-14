package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.trtcdemo.R;

/**
 * 带提示的ButtonItem
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class TipButtonSettingItem extends BaseSettingItem {
    private static final String TAG = EditTextSendSettingItem.class.getName();

    private final OnClickListener mListener;

    private ItemViewHolder mItemViewHolder;

    public TipButtonSettingItem(Context context,
                                   @NonNull BaseSettingItem.ItemText itemText,
                                OnClickListener listener) {
        super(context, itemText);
        mListener = listener;
        mItemViewHolder = new ItemViewHolder(
                mInflater.inflate(R.layout.trtcdemo_item_setting_tip_button, null)
        );
    }

    @Override
    public View getView() {
        if (mItemViewHolder != null) {
            return mItemViewHolder.rootView;
        }
        return null;
    }

    public void setButtonText(final String text) {
        mItemViewHolder.mButtonSend.post(new Runnable() {
            @Override
            public void run() {
                mItemViewHolder.mButtonSend.setText(text);
            }
        });
    }

    public TipButtonSettingItem setButtonVisible(int visibility) {
        mItemViewHolder.mButtonSend.setVisibility(visibility);
        return this;
    }

    public interface OnClickListener {
        void onClick();
    }

    public class ItemViewHolder {
        public  View         rootView;
        public TextView mTextTitle;
        private Button mButtonSend;

        public ItemViewHolder(@NonNull final View itemView) {
            rootView    = itemView;
            mTextTitle  = itemView.findViewById(R.id.title);
            mButtonSend = itemView.findViewById(R.id.btn_send);
            if (mItemText == null) {
                TXCLog.e(TAG, "item text get null here");
                return;
            }

            mTextTitle.setText(mItemText.title);
            if (!CollectionUtils.isEmpty(mItemText.contentText)) {
                String text = mItemText.contentText.get(0);
                if(!TextUtils.isEmpty(text)) {
                    mButtonSend.setText(text);
                }
            }
            mButtonSend.setOnClickListener(new View.OnClickListener() {
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
