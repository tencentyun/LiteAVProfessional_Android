package com.tencent.liteav.trtcdemo.ui.fragment;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.bean.OtherConfig;
import com.tencent.liteav.trtcdemo.model.bean.VideoConfig;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.EditTextSendSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SwitchSettingItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 其他Tab Fragment页
 *
 * @date : 2021/5/25
 * @author : xander
 */
public class OtherSettingFragment extends BaseSettingFragment {

    private LinearLayout            mContentItem;
    private List<BaseSettingItem>   mSettingItemList;
    private SwitchSettingItem       mEnableFlashItem;
    private SwitchSettingItem       mGSensorModeItem;
    private EditTextSendSettingItem mSeiMsgItem;
    private OtherConfig             mOtherConfig;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected void initView(View itemView) {
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();
        mOtherConfig = SettingConfigHelper.getInstance().getMoreConfig();

        mGSensorModeItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_gravity_inuction), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        mOtherConfig.setEnableGSensorMode(mGSensorModeItem.getChecked());
                        onParamsChange();
                    }
                });
        mSettingItemList.add(mGSensorModeItem);

        mEnableFlashItem = new SwitchSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_open_flash), ""),
                new SwitchSettingItem.ClickListener() {
                    @Override
                    public void onClick() {
                        openFlashlight();
                    }
                });
        mSettingItemList.add(mEnableFlashItem);

        mSeiMsgItem = new EditTextSendSettingItem(getContext(), new BaseSettingItem.ItemText(getString(R.string.trtcdemo_sei_message), getString(R.string.trtcdemo_send)), new EditTextSendSettingItem.OnSendListener() {
            @Override
            public void send(String msg) {
                mTRTCCloudManager.sendSEIMsg(msg);
            }
        });
        mSettingItemList.add(mSeiMsgItem);

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(5), 0, 0);
            mContentItem.addView(view);
        }
        updateView();
    }

    private String getRecordFileName(){
        File sdcardDir = getContext().getExternalFilesDir(null);
        if (sdcardDir == null) {
            return "/sdcard/record.mp4";
        }
        String dirPath = sdcardDir.getAbsolutePath() + "/test/record/record.mp4";
        return dirPath;
    }
    private void onParamsChange() {
        if (mTRTCCloudManager != null) {
            mTRTCCloudManager.setTRTCCloudParam();
            mTRTCCloudManager.enableGSensor(mOtherConfig.isEnableGSensorMode());
        }
    }

    private void openFlashlight() {
        if (mTRTCCloudManager != null) {
            boolean openStatus = mTRTCCloudManager.openFlashlight();
            if (openStatus) {
                mEnableFlashItem.setCheck(mOtherConfig.isEnableFlash());
            } else {
                ToastUtils.showLong("打开闪光灯失败");
            }
        }
    }

    private void updateView() {
        mEnableFlashItem.setCheck(mOtherConfig.isEnableFlash());
        mGSensorModeItem.setCheck(mOtherConfig.isEnableGSensorMode());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }
}
