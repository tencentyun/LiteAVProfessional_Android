package com.tencent.liteav.demo.livelinkmicnew.settting;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.livelinkmicnew.R;
import com.tencent.live2.V2TXLiveCode;

public class ErrorDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.BaseFragmentDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置弹窗占据屏幕的大小
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setAttributes(windowParams);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.getWindow().setLayout((int) (dm.widthPixels * 0.9), LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace();
        }
    }

    public static ErrorDialog showMsgDialog(Activity activity, String msg) {
        ErrorDialog fragment = new ErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        fragment.setArguments(bundle);
        fragment.show(activity.getFragmentManager(), System.currentTimeMillis() + "");
        return fragment;
    }

    public static ErrorDialog showMsgDialog(Context context, String msg) {
        ErrorDialog fragment = new ErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        fragment.setArguments(bundle);
        fragment.show(((Activity) context).getFragmentManager(), System.currentTimeMillis() + "");
        return fragment;
    }

    public static ErrorDialog showMsgDialog(Activity activity, int code) {
        ErrorDialog fragment = new ErrorDialog();
        Bundle bundle = new Bundle();
        String msg = "";
        switch (code) {
            case V2TXLiveCode.V2TXLIVE_OK:
                msg = "成功";
                break;
            case V2TXLiveCode.V2TXLIVE_ERROR_REFUSED:
                msg = "调用失败(调用被拒绝)";
                break;
            case V2TXLiveCode.V2TXLIVE_ERROR_INVALID_PARAMETER:
                msg = "调用失败(参数异常)";
                break;
            case V2TXLiveCode.V2TXLIVE_ERROR_NOT_SUPPORTED:
                msg = "调用未实现接口";
                break;
            case V2TXLiveCode.V2TXLIVE_ERROR_FAILED:
                msg = "调用失败";
                break;
        }
        bundle.putString("msg", msg);
        fragment.setArguments(bundle);
        if (code != V2TXLiveCode.V2TXLIVE_OK)
            fragment.show(activity.getFragmentManager(), System.currentTimeMillis() + "");
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_link_mic_new_fragment_error_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tv = (TextView) view.findViewById(R.id.live_tv_error_msg);
        tv.setText(getArguments().getString("msg", ""));

        view.findViewById(R.id.live_btn_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
