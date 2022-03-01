package com.tencent.liteav.trtcdemo.ui.base;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.liteav.trtcdemo.R;


/**
 * 抽象DialogFragment基类
 * <p>
 * 一个显示View的容器，可定制功能如下：
 * - 设置宽：{@link BaseDialogFragment#getWidth(DisplayMetrics)},默认为屏幕宽的0.9
 * - 设置高：{@link BaseDialogFragment#getWidth(DisplayMetrics)},默认为屏幕高的0.9
 * - 设置渲染View {@link BaseDialogFragment#getLayoutId()}
 *
 * @author : xander
 * @date : 2021/5/25
 */
public abstract class BaseDialogFragment extends DialogFragment {
    public static final String DATA = "data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.BaseFragmentDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setAttributes(windowParams);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.getWindow().setLayout(getWidth(dm), getHeight(dm));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    /**
     * @return layout的resId
     */
    protected abstract int getLayoutId();

    /**
     * 可以通过覆盖这个函数达到改变弹窗大小的效果
     *
     * @param dm DisplayMetrics
     * @return 界面宽度
     */
    protected int getWidth(DisplayMetrics dm) {
        return (int) (dm.widthPixels * 0.9);
    }

    /**
     * 可以通过覆盖这个函数达到改变弹窗大小的效果
     *
     * @param dm DisplayMetrics
     * @return 界面高度
     */
    protected int getHeight(DisplayMetrics dm) {
        return (int) (dm.heightPixels * 0.8);
    }
}
