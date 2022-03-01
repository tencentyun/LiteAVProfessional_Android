package com.tencent.liteav.trtcdemo.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.PkConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.common.EditTextInputItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.pkitem.PKEnterRoomItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PK Tab Fragment页
 */
public class PkSettingFragment extends BaseSettingFragment {
    private LinearLayout      mLLItemList;
    private List<View>        mSettingItemList;
    private EditTextInputItem mEditRoomID;
    private EditTextInputItem mEditUserID;
    private PKEnterRoomItem   mButtonEnterRoom;
    private PkConfig          mPkConfig;
    private PkSettingListener mPkSettingListener;

    public void setPkSettingListener(PkSettingListener pkSettingListener) {
        mPkSettingListener = pkSettingListener;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void initView(View itemView) {
        mLLItemList = itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mPkConfig = SettingConfigHelper.getInstance().getPkConfig();

        mEditRoomID = new EditTextInputItem(getContext(), getString(R.string.trtcdemo_other_roomid), "12348888");
        mSettingItemList.add(mEditRoomID);

        String userId = new Random().nextInt(100000) + 1000000 + "";
        mEditUserID = new EditTextInputItem(getContext(), getString(R.string.trtcdemo_other_userid), userId);
        mSettingItemList.add(mEditUserID);

        mButtonEnterRoom = new PKEnterRoomItem(new PKEnterRoomItem.Listener() {
            @Override
            public void onClick() {
                pk();
            }
        }, getContext(), getString(R.string.trtcdemo_start_pk));
        mSettingItemList.add(mButtonEnterRoom);

        updateItem();

        for (View item : mSettingItemList) {
            mLLItemList.addView(item);
        }
    }

    private void updateItem() {
        mEditRoomID.setText(mPkConfig.getConnectRoomId());
        mEditUserID.setText(mPkConfig.getConnectUserName());
        if (mPkConfig.isConnected()) {
            mButtonEnterRoom.setText(getString(R.string.trtcdemo_stop_pk));
        } else {
            mButtonEnterRoom.setText(getString(R.string.trtcdemo_start_pk));
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }

    private void pk() {
        String roomStr = mEditRoomID.getText().trim();
        String username = mEditUserID.getText();
        if (TextUtils.isEmpty(roomStr)) {
            ToastUtils.showLong(R.string.trtcdemo_please_input_roomid);
            return;
        }
        if (TextUtils.isEmpty(username)) {
            ToastUtils.showLong(R.string.trtcdemo_please_input_userid);
            return;
        }

        if (mTRTCCloudManager != null) {
            if (!mPkConfig.isConnected()) {
                mTRTCCloudManager.startLinkMic(roomStr, username);
            } else {
                mTRTCCloudManager.stopLinkMic();
            }
        }
        if (mPkSettingListener != null) {
            mPkSettingListener.onPkSettingComplete();
        }
    }

    public interface PkSettingListener {
        void onPkSettingComplete();
    }
}
