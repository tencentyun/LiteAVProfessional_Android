package com.tencent.liteav.v1liveplayerdemo.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tencent.liteav.v1liveplayerdemo.R;
import com.tencent.liteav.v1liveplayerdemo.model.SettingItem;

import java.util.List;

public class PlayerSettingFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private Context             mContext;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog   mBottomSheetDialog;
    private View                mView;
    private LinearLayout        mContentView;

    public PlayerSettingFragment(Context context) {
        mContext = context;
        mView = View.inflate(context, R.layout.v1liveplayer_fragment_live_player_setting, null);
        mContentView = mView.findViewById(R.id.v1liveplayer_ll_content);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
            initViews(mView);
            mBottomSheetDialog.setContentView(mView);
            mBottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet)
                    .setBackgroundResource(android.R.color.transparent);
            mBehavior = BottomSheetBehavior.from((View) mView.getParent());

            View parent = (View) mView.getParent();
            BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
            mView.measure(0, 0);
            behavior.setPeekHeight(mView.getMeasuredHeight());
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            parent.setLayoutParams(params);
            mBottomSheetDialog.show();
        }
        return mBottomSheetDialog;
    }

    /**
     * set data.
     *
     * @param data
     */
    public void setData(List<SettingItem> data) {
        if (data == null || data.size() == 0) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            SettingItem item = data.get(i);
            int type = item.getType();
            SettingItemView view = new SettingItemView(mContext, type);
            view.setTitleText(item.getTitle());
            view.setListener(item.getListener());
            fillData(item, view);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            layoutParams.bottomMargin = dip2px(8);
            view.setLayoutParams(layoutParams);
            mContentView.addView(view);
        }
    }

    private void fillData(SettingItem item, SettingItemView view) {
        int type = item.getType();
        if (type == SettingItemView.TYPE_SWITCH) {
            view.setChecked(item.isChecked());
        } else if (type == SettingItemView.TYPE_BUTTON) {
            view.setButtonText(item.getButtonText());
        } else if (type == SettingItemView.TYPE_PROGRESS) {
            view.setProgressMax(item.getProgressMax());
            view.setProgress(item.getProgress());
        } else if (type == SettingItemView.TYPE_RADIO) {
            view.setRadioData(item.getRadioData(), item.getRadioDefaultIndex());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggle(FragmentManager manager, String tag) {
        if (isVisible()) {
            dismissAllowingStateLoss();
        } else {
            show(manager, tag);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.v1liveplayer_btn_close) {
            dismissAllowingStateLoss();
        }
    }

    private void initViews(View view) {
        view.findViewById(R.id.v1liveplayer_btn_close).setOnClickListener(this);
    }
}
