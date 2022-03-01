package com.tencent.liteav.demo.livelinkmicnew.settting;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.liteav.demo.livelinkmicnew.R;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.BaseSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.CustomSettingItem;
import com.tencent.liteav.demo.livelinkmicnew.settting.customitem.RadioButtonSettingItem;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation180;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation270;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation90;

public class PlaySetting extends DialogFragment {

    private static final int SEI_PAY_LOAD_TYPE_5   = 5;
    private static final int SEI_PAY_LOAD_TYPE_242 = 242;
    private V2TXLivePlayer mLivePlayer;
    private ImageView      mImageView;
    private Context        mAppContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.BaseFragmentDialogTheme);
        mAppContext = getActivity().getApplicationContext();
    }

    public void setLivePlayer(V2TXLivePlayer player) {
        mLivePlayer = player;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_link_mic_new_fragment_live_config, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout llContainer = (LinearLayout) view.findViewById(R.id.live_ll_container);

        TextView titleText = new TextView(getActivity());
        titleText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titleText.setText(getString(R.string.livelinkmicnew_tv_setting));
        titleText.setTextSize(14);
        titleText.setGravity(Gravity.CENTER);
        titleText.setTextColor(Color.WHITE);
        llContainer.addView(titleText);

        TextView tv = new TextView(getActivity());
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(getString(R.string.livelinkmicnew_tv_play_volume));
        tv.setTextColor(Color.WHITE);
        titleText.setTextSize(12);
        llContainer.addView(tv);
        SeekBar seekBar = new SeekBar(getActivity());
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        seekBar.setLayoutParams(seekBarParams);
        seekBar.setMax(100);
        seekBar.setProgress(AVSettingConfig.getInstance().playoutVolume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AVSettingConfig.getInstance().playoutVolume = progress;
                if (mLivePlayer != null) {
                    int result = mLivePlayer.setPlayoutVolume(AVSettingConfig.getInstance().playoutVolume);
                    ErrorDialog.showMsgDialog(getActivity(), result);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        llContainer.addView(seekBar);

        RadioButtonSettingItem fillMode = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText(
                getString(R.string.livelinkmicnew_tv_screen_filling_direction),
                getString(R.string.livelinkmicnew_tv_screen_auto),
                getString(R.string.livelinkmicnew_tv_screen_fill)), new RadioButtonSettingItem.SelectedListener() {
            @Override
            public void onSelected(int index) {
                switch (index) {
                    case 0:
                        AVSettingConfig.getInstance().fillMode = V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit;
                        break;
                    case 1:
                        AVSettingConfig.getInstance().fillMode = V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill;
                        break;
                }
                if (mLivePlayer != null) {
                    int result = mLivePlayer.setRenderFillMode(AVSettingConfig.getInstance().fillMode);
                    ErrorDialog.showMsgDialog(getActivity(), result);
                }
            }
        });
        switch (AVSettingConfig.getInstance().fillMode) {
            case V2TXLiveFillModeFit:
                fillMode.setSelect(0);
                break;
            case V2TXLiveFillModeFill:
                fillMode.setSelect(1);
                break;
        }
        llContainer.addView(fillMode.getView());


        RadioButtonSettingItem rotation = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText(
                getString(R.string.livelinkmicnew_tv_direction_of_rotation),
                getString(R.string.livelinkmicnew_tv_rotation0),
                getString(R.string.livelinkmicnew_tv_rotation90),
                getString(R.string.livelinkmicnew_tv_rotation180),
                getString(R.string.livelinkmicnew_tv_rotation270)), new RadioButtonSettingItem.SelectedListener() {
            @Override
            public void onSelected(int index) {
                switch (index) {
                    case 0:
                        AVSettingConfig.getInstance().rotation = V2TXLiveRotation0;
                        break;
                    case 1:
                        AVSettingConfig.getInstance().rotation = V2TXLiveRotation90;
                        break;
                    case 2:
                        AVSettingConfig.getInstance().rotation = V2TXLiveRotation180;
                        break;
                    case 3:
                        AVSettingConfig.getInstance().rotation = V2TXLiveRotation270;
                        break;
                }
                if (mLivePlayer != null) {
                    int result = mLivePlayer.setRenderRotation(AVSettingConfig.getInstance().rotation);
                    ErrorDialog.showMsgDialog(getActivity(), result);
                }
            }
        });
        switch (AVSettingConfig.getInstance().rotation) {
            case V2TXLiveRotation0:
                rotation.setSelect(0);
                break;
            case V2TXLiveRotation90:
                rotation.setSelect(1);
                break;
            case V2TXLiveRotation180:
                rotation.setSelect(2);
                break;
            case V2TXLiveRotation270:
                rotation.setSelect(3);
                break;
        }
        llContainer.addView(rotation.getView());

        RadioButtonSettingItem audioVolumeCallback = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText(
                getString(R.string.livelinkmicnew_tv_volume_tips),
                getString(R.string.livelinkmicnew_tv_enable_volume_tips),
                getString(R.string.livelinkmicnew_tv_disable_volume_tips)), new RadioButtonSettingItem.SelectedListener() {
            @Override
            public void onSelected(int index) {
                AVSettingConfig.getInstance().enableVolumeCallback = index == 0;
                if (mLivePlayer != null) {
                    int result = mLivePlayer.enableVolumeEvaluation(AVSettingConfig.getInstance().enableVolumeCallback ? 300 : 0);
                    ErrorDialog.showMsgDialog(getActivity(), result);
                }
            }
        });
        audioVolumeCallback.setSelect(AVSettingConfig.getInstance().enableVolumeCallback ? 0 : 1);
        llContainer.addView(audioVolumeCallback.getView());

        CustomSettingItem snapshotItem = new CustomSettingItem(getActivity(), new BaseSettingItem.ItemText(
                getString(R.string.livelinkmicnew_tv_screen_snapshot), ""), createSnapshotButton());
        snapshotItem.setAlign(CustomSettingItem.ALIGN_RIGHT);
        llContainer.addView(snapshotItem.getView());

        RadioButtonSettingItem payloadType = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText(
                getString(R.string.livelinkmicnew_tv_payload_type),
                getString(R.string.livelinkmicnew_tv_payload_type5),
                getString(R.string.livelinkmicnew_tv_payload_type242)), new RadioButtonSettingItem.SelectedListener() {
            @Override
            public void onSelected(int index) {
                switch (index) {
                    case 0:
                        AVSettingConfig.getInstance().payloadType = SEI_PAY_LOAD_TYPE_5;
                        break;
                    case 1:
                        AVSettingConfig.getInstance().payloadType = SEI_PAY_LOAD_TYPE_242;
                        break;
                }
                if (mLivePlayer != null) {
                    int result = mLivePlayer.enableReceiveSeiMessage(true, AVSettingConfig.getInstance().payloadType);
                    ErrorDialog.showMsgDialog(getActivity(), result);
                }
            }
        });
        switch (AVSettingConfig.getInstance().payloadType) {
            case SEI_PAY_LOAD_TYPE_5:
                payloadType.setSelect(0);
                break;
            case SEI_PAY_LOAD_TYPE_242:
                payloadType.setSelect(1);
                break;
        }
        llContainer.addView(payloadType.getView());
    }

    public void setSnapshotImage(Bitmap bitmap) {
        if (mImageView != null && bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    private List<View> createSnapshotButton() {
        List<View> views = new ArrayList<>();
        final Button button = new Button(getActivity());
        button.setText(getString(R.string.livelinkmicnew_tv_snapshot));
        mImageView = new ImageView(getActivity());
        mImageView.setLayoutParams(new Gallery.LayoutParams(80, 80));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapshotWithPermission();
            }
        });
        views.add(mImageView);
        views.add(button);
        return views;
    }

    private void snapshotWithPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                if (mLivePlayer != null) {
                    mLivePlayer.snapshot();
                }
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                Toast.makeText(mAppContext,
                        mAppContext.getString(R.string.livelinkmicnew_toast_snapshot_permission_failure),
                        Toast.LENGTH_LONG).show();
            }
        }).request();
    }
}
