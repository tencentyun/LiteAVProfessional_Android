package com.tencent.liteav.trtcdemo.ui.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.RemoteUserConfig;
import com.tencent.liteav.trtcdemo.model.manager.TRTCRemoteUserManager;
import com.tencent.liteav.trtcdemo.ui.adapter.RemoteUserListAdapter;
import com.tencent.liteav.trtcdemo.ui.base.BaseDialogFragment;
import com.tencent.liteav.trtcdemo.ui.fragment.RemoteUserListFragment;
import com.tencent.liteav.trtcdemo.ui.fragment.RemoteUserSettingFragment;

/**
 * 远程用户面板
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class RemoteUserPanelDialogFragment extends BaseDialogFragment
        implements RemoteUserListAdapter.ClickItemListener, RemoteUserSettingFragment.Listener {

    private RemoteUserListFragment    mRemoteUserListFragment;
    private RemoteUserSettingFragment mRemoteUserSettingFragment;
    private TRTCRemoteUserManager     mTRTCRemoteUserManager;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment();
    }

    private void initFragment() {
        if (mRemoteUserListFragment == null && mRemoteUserSettingFragment == null) {
            mRemoteUserListFragment = new RemoteUserListFragment();
            mRemoteUserSettingFragment = new RemoteUserSettingFragment();

            mRemoteUserListFragment.setClickItemListener(this);
            mRemoteUserSettingFragment.setListener(this);
        }
        if (mTRTCRemoteUserManager != null) {
            mRemoteUserSettingFragment.setTRTCRemoteUserManager(mTRTCRemoteUserManager);
        }
        getChildFragmentManager().beginTransaction()
                .add(R.id.fl_container, mRemoteUserListFragment, "RemoteUserListFragment")
                .show(mRemoteUserListFragment)
                .add(R.id.fl_container, mRemoteUserSettingFragment, "UserInfoFragment")
                .hide(mRemoteUserSettingFragment)
                .commit();
    }

    public void setTRTCRemoteUserManager(TRTCRemoteUserManager TRTCRemoteUserManager) {
        mTRTCRemoteUserManager = TRTCRemoteUserManager;
        if (mRemoteUserSettingFragment != null) {
            mRemoteUserSettingFragment.setTRTCRemoteUserManager(mTRTCRemoteUserManager);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_user_manager;
    }

    @Override
    public void onClickItem(RemoteUserConfig remoteUserConfig) {
        if (mRemoteUserSettingFragment == null) {
            return;
        }
        mRemoteUserSettingFragment.setRemoteUserConfig(remoteUserConfig);
        jumpToSettingFragment();
    }

    @Override
    public void onBackClick() {
        backToUserListFragment();
    }

    private void jumpToSettingFragment() {
        getChildFragmentManager().beginTransaction()
                .hide(mRemoteUserListFragment)
                .show(mRemoteUserSettingFragment)
                .commit();
    }

    private void backToUserListFragment() {
        getChildFragmentManager().beginTransaction()
                .hide(mRemoteUserSettingFragment)
                .show(mRemoteUserListFragment)
                .commit();
    }

    @Override
    protected int getHeight(DisplayMetrics dm) {
        return (int) (dm.heightPixels * 0.6);
    }
}
