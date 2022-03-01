package com.tencent.liteav.trtcdemo.ui.widget.settingitem.subroomitem;

import android.content.Context;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.listener.TRTCCloudManagerListener;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsBaseItem;


public class SubRoomSettingItem extends AbsBaseItem {

    public interface SubRoomListener {
        void onStartPublishLocalStreams(SubRoomSettingItem item);

        void onStopPublishLocalStreams(SubRoomSettingItem item);

        void onExitRoom(SubRoomSettingItem item);
    }

    private View             mRootView        = null;
    private TextView         mTitleTV         = null;
    private SwitchCompat     mPushCB          = null;
    private Button           mExitRoomBtn     = null;
    private TRTCCloudManager mCloudManager    = null;
    private SubRoomListener  mSubRoomListener = null;

    public SubRoomSettingItem(Context context, String title,
                              TRTCCloudManager trtcCloudManager,
                              TRTCCloudManagerListener cloudManagerListener,
                              SubRoomListener listener) {
        super(context, true);
        mRootView = LayoutInflater.from(context).inflate(R.layout.trtcdemo_sub_room_control, this, true);
        mTitleTV = (TextView) mRootView.findViewById(R.id.title_tv);
        mPushCB = (SwitchCompat) mRootView.findViewById(R.id.push_cb);
        mExitRoomBtn = (Button) mRootView.findViewById(R.id.exitroom_btn);
        mCloudManager = trtcCloudManager;
        mTitleTV.setText(title);
        mPushCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPushCB.isChecked()) {
                    mCloudManager.startPublishLocalStreams();
                    if (mSubRoomListener != null) {
                        mSubRoomListener.onStartPublishLocalStreams(SubRoomSettingItem.this);
                    }
                } else {
                    mCloudManager.stopPublishLocalStreams();
                    if (mSubRoomListener != null) {
                        mSubRoomListener.onStopPublishLocalStreams(SubRoomSettingItem.this);
                    }
                }
            }
        });
        mExitRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloudManager.exitRoom();
                mCloudManager.destroySubCloud();
                if (mSubRoomListener != null) {
                    mSubRoomListener.onExitRoom(SubRoomSettingItem.this);
                }
            }
        });
        mSubRoomListener = listener;
    }

    public void setPublishChecked(boolean checked) {
        mPushCB.setChecked(checked);
    }

    public boolean isPublishChecked() {
        return mPushCB.isChecked();
    }

    public void hideExitRoomButton(boolean hide) {
        if (hide) {
            mExitRoomBtn.setVisibility(View.INVISIBLE);
        } else {
            mExitRoomBtn.setVisibility(View.VISIBLE);
        }
    }

    public void destroyRoom() {
        mCloudManager.exitRoom();
        mCloudManager.destroySubCloud();
    }

    public String getRoomId() {
        return mCloudManager.getParams().roomId + "";
    }
}
