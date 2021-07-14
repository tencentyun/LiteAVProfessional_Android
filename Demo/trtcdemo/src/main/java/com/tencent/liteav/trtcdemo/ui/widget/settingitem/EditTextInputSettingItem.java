package com.tencent.liteav.trtcdemo.ui.widget.settingitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.trtcdemo.R;

/**
 * EditText纯输入Item
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class EditTextInputSettingItem extends BaseSettingItem {
    private static final String TAG = EditTextInputSettingItem.class.getName();


    private ItemViewHolder  mItemViewHolder;
    private int             mInputMaxValue  = Integer.MAX_VALUE;
    private int             mInputType      = InputType.TYPE_CLASS_NUMBER;

    public EditTextInputSettingItem(Context context, ItemText itemText, int paddingTop) {
        super(context, itemText);
        mItemViewHolder = new ItemViewHolder(
                mInflater.inflate(R.layout.trtcdemo_item_setting_edittext_input, null)
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

    public EditText getEditText(){
        return mItemViewHolder.mMessageEt;
    }

    public void setInputType(int type){
        mItemViewHolder.mMessageEt.setInputType(type);
        mInputType  = type;
    }

    public void setText(final String text) {
        mItemViewHolder.mMessageEt.post(new Runnable() {
            @Override
            public void run() {
                mItemViewHolder.mMessageEt.setText(text);
            }
        });
    }

    public String getText() {
        return mItemViewHolder.mMessageEt.getText().toString();
    }

    public class ItemViewHolder {
        public  View            rootView;
        public  TextView        mTitle;
        private EditText        mMessageEt;

        public ItemViewHolder(@NonNull final View itemView) {
            rootView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mMessageEt = (EditText) itemView.findViewById(R.id.et_input);
            if (mItemText == null) {
                TXCLog.e(TAG, "item text get null here");
                return;
            }

            mTitle.setText(mItemText.title);
            mMessageEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(mInputType == InputType.TYPE_CLASS_NUMBER){
                        try {
                            long input = Long.valueOf(s.toString().trim());
                            if(input > mInputMaxValue){
                                s.delete(s.toString().trim().length() - 1, s.toString().trim().length());
                                mMessageEt.setText(s);
                                mMessageEt.setSelection(s.toString().length());
                            }
                        }catch (Exception e){

                        }
                    }
                }
            });
        }
    }
}
