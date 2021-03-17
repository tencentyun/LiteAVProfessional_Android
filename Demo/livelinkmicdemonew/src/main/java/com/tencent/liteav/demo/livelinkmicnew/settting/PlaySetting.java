package com.tencent.liteav.demo.livelinkmicnew.settting;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

    private V2TXLivePlayer mLivePlayer;
    private ImageView mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.BaseFragmentDialogTheme);
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
        titleText.setText("设置");
        titleText.setTextSize(14);
        titleText.setGravity(Gravity.CENTER);
        titleText.setTextColor(Color.WHITE);
        llContainer.addView(titleText);

        TextView tv = new TextView(getActivity());
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText("播放音量：");
        tv.setTextColor(Color.WHITE);
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

        RadioButtonSettingItem fillMode = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText("画面填充方向","自适应", "铺满"), new RadioButtonSettingItem.SelectedListener() {
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


        RadioButtonSettingItem rotation = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText("旋转方向","0", "90", "180", "270"), new RadioButtonSettingItem.SelectedListener() {
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

        RadioButtonSettingItem audioVolumeCallback = new RadioButtonSettingItem(getActivity(), new BaseSettingItem.ItemText("音量提示","开启", "关闭"), new RadioButtonSettingItem.SelectedListener() {
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

        CustomSettingItem snapshotItem = new CustomSettingItem(getActivity(), new BaseSettingItem.ItemText("视频截图", ""), createSnapshotButton());
        snapshotItem.setAlign(CustomSettingItem.ALIGN_RIGHT);
        llContainer.addView(snapshotItem.getView());

    }

    public void setSnapshotImage(Bitmap bitmap) {
        if (mImageView != null && bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    private List<View> createSnapshotButton() {
        List<View>   views  = new ArrayList<>();
        final Button button = new Button(getActivity());
        button.setText("截图");
        mImageView = new ImageView(getActivity());
        mImageView.setLayoutParams(new Gallery.LayoutParams(80, 80));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLivePlayer != null) {
                    mLivePlayer.snapshot();
                }
            }
        });
        views.add(mImageView);
        views.add(button);
        return views;
    }

}
