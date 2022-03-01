package com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.demo.livepusher.R;
import com.tencent.liteav.demo.livepusher.camerapush.ui.custom.FlowRadioGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 封装单选框Item
 */
public abstract class AbsRadioButtonItem extends AbsBaseItem {

    public  TextView          mTextTitle;
    public  FlowRadioGroup    mRadioGroup;
    public  List<RadioButton> mRadioButtonList;
    public  List<String>      mTextList;
    public  String            mTitle;
    private int               mSelectIndex = 0;

    public AbsRadioButtonItem(Context context, boolean debug, String title, String... textList) {
        super(context, debug);
        mTitle = title;
        mTextList = Arrays.asList(textList);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.livepusher_item_setting_radio, this, true);
        mTextTitle = findViewById(R.id.title);
        mRadioGroup = findViewById(R.id.rg_item);
        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }
        if (mTextList == null) {
            return;
        }

        mRadioButtonList = new ArrayList<>();
        int index = 1;
        for (String text : mTextList) {
            RadioButton button = createRadioButton(text, mRadioGroup.hashCode() + index);
            if (index == 1) {
                button.setChecked(true);
            }
            index++;
            mRadioButtonList.add(button);
            mRadioGroup.addView(button);
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View child = group.findViewById(checkedId);
                if (!child.isPressed()) {
                    return;
                }
                int index = checkedId - mRadioGroup.hashCode() - 1;
                mSelectIndex = index;
                onSelected(index);
            }
        });
    }

    private RadioButton createRadioButton(String name, int id) {
        RadioButton radioButton = new RadioButton(getContext());
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
        radioButton.setTextColor(getContext().getResources().getColor(R.color.livepusher_white));
        radioButton.setGravity(Gravity.CENTER);
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.livepusher_bg_rb_icon_selector);
        drawable.setBounds(0, 0, SizeUtils.dp2px(21), SizeUtils.dp2px(21));
        radioButton.setCompoundDrawables(drawable, null, null, null);
        radioButton.setCompoundDrawablePadding(SizeUtils.dp2px(3));
        return radioButton;
    }

    public void setSelect(int index) {
        if (!CollectionUtils.isEmpty(mRadioButtonList)
                && index >= 0 && index < mRadioButtonList.size()) {
            mSelectIndex = index;
            final RadioButton rb = mRadioButtonList.get(index);
            rb.post(new Runnable() {
                @Override
                public void run() {
                    rb.setChecked(true);
                }
            });
        }
    }

    public int getSelect() {
        return mSelectIndex;
    }

    public abstract void onSelected(int index);
}