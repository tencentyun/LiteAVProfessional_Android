package com.tencent.liteav.trtcdemo.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.bean.PkConfig;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.EditTextInputSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SingleButtonSettingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PK Tab Fragment页
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class PkSettingFragment extends BaseSettingFragment{
    private LinearLayout                mLLItemList;
    private List<BaseSettingItem>       mSettingItemList;
    private EditTextInputSettingItem    mEditRoomID;
    private EditTextInputSettingItem    mEditUserID;
    private SingleButtonSettingItem     mButtonEnterRoom;

    private PkConfig                    mPkConfig;
    private PkSettingListener           mPkSettingListener;

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

        mEditRoomID = new EditTextInputSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_other_roomid)), 0);
        mEditRoomID.setText("12348888");
        mSettingItemList.add(mEditRoomID);

        String userId = new Random().nextInt(100000) + 1000000 + "";
        mEditUserID = new EditTextInputSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_other_userid)), 5);
        mEditUserID.setText(userId);
        mSettingItemList.add(mEditUserID);

        mButtonEnterRoom = new SingleButtonSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_start_pk)), SizeUtils.dp2px(50), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pk();
            }
        });
        mSettingItemList.add(mButtonEnterRoom);

        updateItem();

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, item.getPaddingTop(), 0, 0);
            mLLItemList.addView(view);
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

    private void pk(){
        String roomStr  = mEditRoomID.getText().toString().trim();
        String username = mEditUserID.getText().toString();
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
