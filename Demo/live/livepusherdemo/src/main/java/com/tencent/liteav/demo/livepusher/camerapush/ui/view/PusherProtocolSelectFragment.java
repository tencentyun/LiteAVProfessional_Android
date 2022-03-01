package com.tencent.liteav.demo.livepusher.camerapush.ui.view;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.liteav.demo.livepusher.R;

public class PusherProtocolSelectFragment extends DialogFragment {
    private RadioButton[] mRadioButtons = new RadioButton[2];
    private Button        mButtonPush;

    private OnSelectListener mListener;
    private int              mCurrentSelected = 0;

    public void setListener(OnSelectListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.LivePusherMlvbDialogFragment);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.livepusher_fragment_push_protocol, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initData();
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

    private void initViews(View view) {
        mRadioButtons[0] = view.findViewById(R.id.livepusher_rb_flv);
        mRadioButtons[0].setText("RTMP");
        mRadioButtons[1] = view.findViewById(R.id.livepusher_rb_rtmp);
        mRadioButtons[1].setText("RTC");

        for (int i = 0; i < mRadioButtons.length; i++) {
            final int position = i;
            mRadioButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectPosition(position);
                }
            });
        }


        mButtonPush = (Button) view.findViewById(R.id.livepusher_btn_push);
        mButtonPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSelected(mCurrentSelected);
                }
                dismissAllowingStateLoss();
            }
        });
    }

    private void initData() {
        setSelectPosition(0);
    }

    private void setSelectPosition(int position) {
        mRadioButtons[mCurrentSelected].setChecked(false);
        mRadioButtons[position].setChecked(true);
        mCurrentSelected = position;
    }

    public interface OnSelectListener {
        void onSelected(int index);
    }
}

