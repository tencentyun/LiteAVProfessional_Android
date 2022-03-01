package com.tencent.liteav.v1livepusherdemo.itemview;

import android.content.Context;
import androidx.annotation.NonNull;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.v1livepusherdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 带RadioButton的item
 *
 * @author guanyifeng
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
                mInflater.inflate(R.layout.v1livepusher_item_setting_radio, null)
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
        public static final int               MIN_SIZE = 2;
        public              View              rootView;
        public              TextView          mTitle;
        public              RadioGroup        mItemRg;
        public              List<RadioButton> mRadioButtonList;
        public              int               mSelectedIndex;

        public RadioItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.v1livepusher_title);
            mItemRg = (RadioGroup) itemView.findViewById(R.id.v1livepusher_rg_item);

            if (mItemText == null) {
                Log.e(TAG, "item text get null here");
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
            if (mItemText.contentText.size() >= 3) {
                mItemRg.setOrientation(LinearLayout.VERTICAL);
            }

            mItemRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    View child = group.findViewById(checkedId);
                    if (!child.isPressed()) {
                        return;
                    }
                    mSelectedIndex = checkedId - mItemRg.hashCode() - 1;
                    Log.d(TAG, mTitle.getText() + " select " + mSelectedIndex);
                    if (mSelectedListener != null) {
                        mSelectedListener.onSelected(mSelectedIndex);
                    }
                }
            });
        }

        private RadioButton createRadioButton(String name, int id) {
            RadioButton radioButton = new RadioButton(mContext);
            RadioGroup.LayoutParams mLayoutParams = new RadioGroup.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mLayoutParams.rightMargin = SizeUtils.dp2px(10);
            radioButton.setLayoutParams(mLayoutParams);
            radioButton.setClickable(true);
            radioButton.setButtonDrawable(R.drawable.v1livepusher_live_pusher_checkbox_bg);
            radioButton.setId(id);
            radioButton.setText(name);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            radioButton.setTextColor(mContext.getResources().getColor(R.color.v1livepusher_colorRadioText));
            radioButton.setGravity(Gravity.CENTER);
            return radioButton;
        }
    }
}

