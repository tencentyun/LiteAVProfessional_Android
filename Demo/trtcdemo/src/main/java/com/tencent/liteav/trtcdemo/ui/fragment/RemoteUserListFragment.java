package com.tencent.liteav.trtcdemo.ui.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.helper.RemoteUserConfigHelper;
import com.tencent.liteav.trtcdemo.ui.adapter.RemoteUserListAdapter;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;

/**
 * 远程用户列表页
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class RemoteUserListFragment extends BaseSettingFragment {
    private static final String TAG = RemoteUserListFragment.class.getName();

    private RecyclerView                            mRecyclerUserList;
    private RemoteUserListAdapter                   mAdapter;
    private RemoteUserListAdapter.ClickItemListener mListener;

    @Override
    protected void initView(View view) {
        mRecyclerUserList = view.findViewById(R.id.rv_user_list);
        mRecyclerUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RemoteUserListAdapter(getContext());
        if (mListener != null) {
            mAdapter.setClickItemListener(mListener);
        }
        mRecyclerUserList.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setUserInfoList(RemoteUserConfigHelper.getInstance().getRemoteUserConfigList());
        Log.d(TAG, "onResume user list size: " + RemoteUserConfigHelper.getInstance().getRemoteUserConfigList().size());
    }

    public void setClickItemListener(RemoteUserListAdapter.ClickItemListener clickItemListener) {
        mListener = clickItemListener;
        if (mAdapter != null) {
            mAdapter.setClickItemListener(mListener);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_remote_user_list;
    }
}
