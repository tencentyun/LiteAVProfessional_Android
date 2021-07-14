package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.ui.widget.custom.FlowRadioGroup;
import com.tencent.rtmp.TXLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 带RadioButton的item
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class RadioButtonSettingItem extends BaseSettingItem {
    private static final String TAG = RadioButtonSettingItem.class.getName();

    private RadioItemViewHolder mRadioItemViewHolder;
    private SelectedListener    mSelectedListener;

    public RadioButtonSettingItem(Context context,
                                  @NonNull ItemText itemText,
                                  SelectedListener listener) {
        super(context, itemText);
        mSelectedListener = listener;
        mRadioItemViewHolder = new RadioItemViewHolder(
                mInflater.inflate(R.layout.trtcdemo_item_setting_radio, null)
        );
    }

    public RadioButtonSettingItem setSelect(int index) {
        if (!CollectionUtils.isEmpty(mRadioItemViewHolder.mRadioButtonList)
                && index >= 0 && index < mRadioItemViewHolder.mRadioButtonList.size()) {
            mRadioItemViewHolder.mSelectedIndex = index;
            final RadioButton rb = mRadioItemViewHolder.mRadioButtonList.get(index);
            rb.post(new Runnable() {
                @Override
                public void run() {
                    rb.setChecked(true);
                }
            });
        }
        return this;
    }

    @Override
    public View getView() {
        if (mRadioItemViewHolder != null) {
            return mRadioItemViewHolder.rootView;
        }
        return null;
    }

    public int getSelected() {
        return mRadioItemViewHolder.mSelectedIndex;
    }

    public interface SelectedListener {
        void onSelected(int index);
    }

    public class RadioItemViewHolder {
        public              View              rootView;
        public              TextView          mTitle;
        public              FlowRadioGroup    mItemRg;
        public              List<RadioButton> mRadioButtonList;
        public              int               mSelectedIndex;

        public RadioItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mTitle   = itemView.findViewById(R.id.title);
            mItemRg  = itemView.findViewById(R.id.rg_item);

            if (mItemText == null) {
                TXLog.e(TAG, "item text get null here");
                return;
            }

            mTitle.setText(mItemText.title);
            mRadioButtonList = new ArrayList<>();
            int index = 1;
            for (String text : mItemText.contentText) {
                RadioButton button = createRadioButton(text, mItemRg.hashCode() + index);
                if (index == 1) {
                    button.setChecked(true);
                }
                index++;
                mRadioButtonList.add(button);
                mItemRg.addView(button);
            }

            mItemRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    View child = group.findViewById(checkedId);
                    if (!child.isPressed()) {
                        return;
                    }
                    mSelectedIndex = checkedId - mItemRg.hashCode() - 1;
                    TXLog.d(TAG, mTitle.getText() + " select " + mSelectedIndex);
                    if (mSelectedListener != null) {
                        mSelectedListener.onSelected(mSelectedIndex);
                    }
                }
            });
        }

        private RadioButton createRadioButton(String name, int id) {
            RadioButton radioButton = new RadioButton(mContext);
            RadioGroup.LayoutParams mLayoutParams = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    SizeUtils.dp2px(21));
            mLayoutParams.rightMargin = SizeUtils.dp2px(10);
            radioButton.setLayoutParams(mLayoutParams);
            radioButton.setClickable(true);
            radioButton.setButtonDrawable(null);
            radioButton.setId(id);
            radioButton.setText(name);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            radioButton.setTextColor(mContext.getResources().getColor(R.color.trtcvoiceroom_text_color_second));
            radioButton.setGravity(Gravity.CENTER);
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.trtcdemo_bg_rb_icon_selector);
            drawable.setBounds(0, 0, SizeUtils.dp2px(21), SizeUtils.dp2px(21));
            radioButton.setCompoundDrawables(drawable, null, null, null);
            radioButton.setCompoundDrawablePadding(SizeUtils.dp2px(3));
            return radioButton;
        }
    }
}
