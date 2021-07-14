package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.trtcdemo.R;

/**
 * 只有一个Button的Item
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class SingleButtonSettingItem extends BaseSettingItem {
    private static final String TAG = SingleButtonSettingItem.class.getName();

    private ItemViewHolder  mItemViewHolder;
    private View.OnClickListener mListener;

    public SingleButtonSettingItem(Context context, ItemText itemText, int paddingTop, View.OnClickListener listener) {
        super(context, itemText);
        this.mListener = listener;
        mItemViewHolder = new ItemViewHolder(
                mInflater.inflate(R.layout.trtcdemo_item_setting_button, null)
        );
        mPaddingTop = SizeUtils.dp2px(paddingTop);
    }

    @Override
    public View getView() {
        if (mItemViewHolder != null) {
            return mItemViewHolder.rootView;
        }
        return null;
    }

    public void setText(final String text){
        mItemViewHolder.mButton.post(new Runnable() {
            @Override
            public void run() {
                mItemViewHolder.mButton.setText(text);
            }
        });
    }

    public class ItemViewHolder {
        public View rootView;
        public Button mButton;

        public ItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mButton = (Button) itemView.findViewById(R.id.btn_click);
            if (mItemText == null) {
                TXCLog.e(TAG, "item text get null here");
                return;
            }
            mButton.setText(mItemText.title);
            mButton.setOnClickListener(mListener);
        }
    }
}
