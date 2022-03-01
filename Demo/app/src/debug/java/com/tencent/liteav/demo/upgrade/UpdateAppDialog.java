package com.tencent.liteav.demo.upgrade;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.liteav.demo.R;

public class UpdateAppDialog extends AlertDialog {

    private OnUpdateListener mOnUpdateListener;
    private TextView         mTvContent;
    private Button           mBtnUpdate;

    public UpdateAppDialog(Context context, OnUpdateListener onUpdateListener) {
        super(context);
        setCancelable(false);
        this.mOnUpdateListener = onUpdateListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_update_dialog);
        mTvContent = findViewById(R.id.tv_app_update_content);
        mBtnUpdate = findViewById(R.id.btn_app_update_confirm);
        mBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnUpdateListener != null) {
                    mOnUpdateListener.onUpdate();
                    mBtnUpdate.setText(R.string.setting_download_title);
                }
            }
        });
    }

    /**
     * 升级信息展示
     */
    public void update(AppSetting appSetting, boolean downloading) {
        if (mTvContent != null) {
            mTvContent.setText(getContext().getString(R.string.setting_update_dialog_content,
                    appSetting.getData().getAppVersion()));
        }
        if (mBtnUpdate != null) {
            mBtnUpdate.setText(downloading ? R.string.setting_download_title : R.string.setting_update_dialog_confirm);
        }
    }

    /**
     * 升级按钮触发事件
     */
    public interface OnUpdateListener {
        void onUpdate();
    }

}
