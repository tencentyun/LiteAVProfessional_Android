package com.tencent.liteav.demo.videoediter.common.widget.layer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanszhli on 2017/6/22.
 * <p>
 * 用于统一管理{@link TCLayerOperationView}的layout
 */
public class TCLayerViewGroup extends FrameLayout implements View.OnClickListener {
    private List<TCLayerOperationView> mChilds;
    private int mLastSelectedPos = -1;

    public TCLayerViewGroup(Context context) {
        super(context);
        init();
    }

    public TCLayerViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCLayerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mChilds = new ArrayList<TCLayerOperationView>();
    }

    public void addOperationView(TCLayerOperationView view) {
        mChilds.add(view);
        selectOperationView(mChilds.size() - 1);
        addView(view);
        view.setOnClickListener(this);
    }

    public void removeOperationView(TCLayerOperationView view) {
        int viewIndex = mChilds.indexOf(view);
        mChilds.remove(view);
        mLastSelectedPos = -1;
        removeView(view);
        view.setOnClickListener(null);
    }

    public TCLayerOperationView getOperationView(int index) {
        return mChilds.get(index);
    }


    private void selectOperationView(int pos) {
        if (pos < mChilds.size() && pos >= 0) {
            if (mLastSelectedPos != -1)
                mChilds.get(mLastSelectedPos).setEditable(false);//不显示编辑的边框
            mChilds.get(pos).setEditable(true);//显示编辑的边框
            mLastSelectedPos = pos;
        }
    }

    private void unSelectOperationView(int pos) {
        if (pos < mChilds.size() && mLastSelectedPos != -1) {
            mChilds.get(mLastSelectedPos).setEditable(false);//不显示编辑的边框
            mLastSelectedPos = -1;
        }
    }

    public TCLayerOperationView getSelectedLayerOperationView() {
        if (mLastSelectedPos < 0 || mLastSelectedPos >= mChilds.size()) return null;
        return mChilds.get(mLastSelectedPos);
    }

    public int getSelectedViewIndex() {
        return mLastSelectedPos;
    }

    public int getChildCount() {
        return mChilds.size();
    }

    @Override
    public void onClick(View v) {
        TCLayerOperationView tcLayerOperationView = (TCLayerOperationView) v;
        int pos = mChilds.indexOf(tcLayerOperationView);
        int lastPos = mLastSelectedPos;
        selectOperationView(pos); //选中编辑
        if (mListener != null) {
            mListener.onLayerOperationViewItemClick(tcLayerOperationView, lastPos, pos);
        }
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onLayerOperationViewItemClick(TCLayerOperationView view, int lastSelectedPos, int currentSelectedPos);
    }

}
