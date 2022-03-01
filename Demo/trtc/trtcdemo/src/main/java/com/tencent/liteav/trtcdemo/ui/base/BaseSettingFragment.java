package com.tencent.liteav.trtcdemo.ui.base;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCRemoteUserManager;
import com.tencent.liteav.trtcdemo.ui.widget.videolayout.TRTCVideoLayoutManager;

/**
 * 抽象fragment的基类
 * <p>
 * - 设置布局文件{@link BaseSettingFragment#getLayoutId()}
 * - 初始化布局View{@link BaseSettingFragment#getView()}
 *
 * @author : xander
 * @date : 2021/5/25
 */
public abstract class BaseSettingFragment extends Fragment {
    protected Handler                mHandler = new Handler();
    protected TRTCCloudManager       mTRTCCloudManager;
    protected TRTCRemoteUserManager  mTRTCRemoteUserManager;
    protected TRTCVideoLayoutManager mTRTCVideoLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    public TRTCCloudManager getTRTCCloudManager() {
        return mTRTCCloudManager;
    }

    public void setTRTCCloudManager(TRTCCloudManager trtcCloudManager) {
        mTRTCCloudManager = trtcCloudManager;
    }

    public TRTCRemoteUserManager getTRTCRemoteUserManager() {
        return mTRTCRemoteUserManager;
    }

    public void setTRTCRemoteUserManager(TRTCRemoteUserManager TRTCRemoteUserManager) {
        mTRTCRemoteUserManager = TRTCRemoteUserManager;
    }

    public void setmTRTCVideoLayoutManager(TRTCVideoLayoutManager layoutManager) {
        mTRTCVideoLayout = layoutManager;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    protected abstract void initView(View view);

    protected abstract int getLayoutId();
}
