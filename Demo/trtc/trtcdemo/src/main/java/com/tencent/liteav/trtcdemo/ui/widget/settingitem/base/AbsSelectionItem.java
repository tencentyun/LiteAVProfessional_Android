package com.tencent.liteav.trtcdemo.ui.widget.settingitem.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;

import java.util.Arrays;
import java.util.List;

/**
 * 封装下拉选择框Item
 */
public abstract class AbsSelectionItem extends AbsBaseItem {

    private TextView             mTextTitle;
    private Spinner              mSpinner;
    public  ArrayAdapter<String> mAdapter;
    public  List<String>         mTextList;
    public  String               mTitle;
    private SpinnerListener      mSpinnerListener;

    public AbsSelectionItem(Context context, boolean debug, String title, String... textList) {
        super(context, debug);
        mTitle = title;
        mTextList = Arrays.asList(textList);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_selection, this, true);
        mTextTitle = findViewById(R.id.title);
        mSpinner = findViewById(R.id.sp_item);

        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }
        if (mTextList == null) {
            return;
        }
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.trtcdemo_item_setting_selection_textview, mTextList);
        mSpinner.setAdapter(mAdapter);
        mSpinnerListener = new SpinnerListener();
        mSpinner.setOnTouchListener(mSpinnerListener);
        mSpinner.setOnItemSelectedListener(mSpinnerListener);

    }

    public class SpinnerListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        private boolean fromUser = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            fromUser = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (fromUser) {
                fromUser = false;
                onSelected(position, mTextList.get(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void setSelect(final int index) {
        mSpinner.post(new Runnable() {
            @Override
            public void run() {
                mSpinner.setSelection(index);
            }
        });
    }

    public int getSelected() {
        return mSpinner.getSelectedItemPosition();
    }

    public abstract void onSelected(int index, String str);
}