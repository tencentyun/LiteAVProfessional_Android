package com.tencent.liteav.demo.livepusher.camerapush.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.livepusher.R;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.AudioQualityItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.MicrophoneVolumeEvaluationItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.RenderMirrorItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.RenderRotationItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.ResolutionItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.SnapshotItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.VideoBitrateItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.VideoFpsItem;
import com.tencent.liteav.demo.livepusher.camerapush.ui.settingitem.settings.VideoMinBitrateItem;
import com.tencent.live2.V2TXLiveDef;

/**
 * 设置面板，包括耳返、静音、横屏推流等开关设置
 */
public class PusherSettingFragment extends DialogFragment {

    private static final String TAG                            = "PusherSettingFragment";
    private static final int    POSITION_ADJUST_BITRATE        = 0;
    private static final int    POSITION_EAR_MONITORING_ENABLE = 1;
    private static final int    POSITION_MUTE_AUDIO            = 2;
    private static final int    POSITION_LANDSCAPE             = 3;
    private static final int    POSITION_WATER_MARK_ENABLE     = 4;
    private static final int    POSITION_MIRROR_ENABLE         = 5;
    private static final int    POSITION_FLASH_ENABLE          = 6;
    private static final int    POSITION_FOCUS_ENABLE          = 7;
    private static final int    POSITION_PRIVACY_MODEL_ENABLE  = 8;

    private static final String SP_NAME                 = "sp_pusher_setting";
    private static final String SP_KEY_PRIVACY_MODEL    = "sp_key_privacy_model";
    private static final String SP_KEY_ADJUST_BITRATE   = "sp_key_adjust_bitrate";
    private static final String SP_KEY_EAR_MONITORING   = "sp_key_ear_monitoring";
    private static final String SP_KEY_MUTE_AUDIO       = "sp_key_mute_audio";
    private static final String SP_KEY_LANDSCAPE        = "sp_key_portrait";
    private static final String SP_KEY_WATER_MARK       = "sp_key_water_mark";
    private static final String SP_KEY_MIRROR           = "sp_key_mirror";
    private static final String SP_KEY_FOCUS            = "sp_key_focus";
    private static final String SP_KEY_AUDIO_QUALITY    = "sp_key_audio_quality";
    private static final String SP_KEY_RENDER_ROTATION  = "sp_key_render_rotation";
    private static final String SP_KEY_RENDER_MIRROR    = "sp_key_render_mirror";
    private static final String SP_KEY_VIDEO_RESOLUTION = "sp_key_video_Resolution";

    private static final int AUDIO_SPEECH  = 0;    // 语音(speech)
    private static final int AUDIO_DEFAULT = 1;    // 标准(default)
    private static final int AUDIO_MUSIC   = 2;    // 音乐(music)

    private AudioQualityItem        mAudioQualityItem;
    private RenderRotationItem      mRenderRotationItem;
    private RenderMirrorItem        mRenderMirrorItem;
    private OnSettingChangeListener mOnSettingChangeListener;
    private Dialog                  mDialog;
    private CheckSelectView         mCheckSelectView;
    private boolean[]               mEnables              = new boolean[9];
    private int                     mAudioQualityIndex    = 1;
    private int                     mRenderRotationIndex  = 0;
    private int                     mRenderMirrorIndex    = 0;
    private int                     mVideoResolutionIndex = 9;
    private int                     mVideoBitrate         = 150;
    private int                     mVideoMinBitrate      = 100;

    private EditText mSEIPayloadTypeET;
    private EditText mSEIDataET;
    private Button   mSEISendBtn;

    public PusherSettingFragment() {
        initialize();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mDialog == null) {
            mDialog = super.onCreateDialog(savedInstanceState);
            View inflate = View.inflate(getActivity(), R.layout.livepusher_fragment_setting, null);
            initViews(inflate);
            mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    int height = getResources().getDisplayMetrics().heightPixels;
                    WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
                    params.height = height / 2;
                    params.gravity = Gravity.BOTTOM;
                    mDialog.getWindow().setAttributes(params);
                }
            });
            mDialog.setContentView(inflate);
            mDialog.show();
        }
        return mDialog;
    }

    @Override
    public int getTheme() {
        return android.R.style.Theme_Black_NoTitleBar;
    }

    private void initViews(View view) {

        final LinearLayout topRoot = view.findViewById(R.id.livepusher_ll_top);

        // 音质选择
        mAudioQualityItem = new AudioQualityItem(getContext(),
                getString(R.string.livepusher_audio_quality),
                getResources().getStringArray(R.array.livepusher_voice_channel)) {
            @Override
            public void onSelected(int index) {
                mAudioQualityIndex = index;
                if (null != mOnSettingChangeListener) {
                    mOnSettingChangeListener.onAudioQualityChange(getAudioQuality());
                }
            }
        };
        mAudioQualityItem.setSelect(mAudioQualityIndex);
        topRoot.addView(mAudioQualityItem);

        // 渲染方向
        mRenderRotationItem = new RenderRotationItem(getContext(),
                getString(R.string.livepusher_render_rotation),
                getResources().getStringArray(R.array.livepusher_render_rotation)) {
            @Override
            public void onSelected(int index) {
                mRenderRotationIndex = index;
                if (null != mOnSettingChangeListener) {
                    mOnSettingChangeListener.onRenderRotation(getRotationType());
                }
            }
        };
        mRenderRotationItem.setSelect(mRenderRotationIndex);
        topRoot.addView(mRenderRotationItem);

        // 渲染镜像
        mRenderMirrorItem = new RenderMirrorItem(getContext(),
                getString(R.string.livepusher_render_mirror),
                getResources().getStringArray(R.array.livepusher_render_mirror)) {
            @Override
            public void onSelected(int index) {
                mRenderMirrorIndex = index;
                if (null != mOnSettingChangeListener) {
                    mOnSettingChangeListener.onRenderMirror(getMirrorType());
                }
            }
        };
        mRenderMirrorItem.setSelect(mRenderMirrorIndex);
        topRoot.addView(mRenderMirrorItem);

        view.findViewById(R.id.livepusher_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        initCheckSelectView(view);

        final LinearLayout bottomRoot = view.findViewById(R.id.livepusher_ll_bottom);
        SnapshotItem snapshotItem = new SnapshotItem(getContext(),
                getString(R.string.livepusher_local_snapshot),
                getString(R.string.livepusher_snapshot)) {
            @Override
            public void onClicked() {
                if (mOnSettingChangeListener != null) {
                    mOnSettingChangeListener.onClickSnapshot();
                }
            }
        };
        bottomRoot.addView(snapshotItem);

        // 视频分辨率
        ResolutionItem resolutionItem = new ResolutionItem(getContext(),
                getString(R.string.livepusher_resolution),
                getResources().getStringArray(R.array.livepusher_video_solution)) {
            @Override
            public void onSelected(int index, String str) {
                mRenderRotationIndex = index;
                if (null != mOnSettingChangeListener) {
                    mOnSettingChangeListener.onResolutionChange(getVideoResolution());
                }
            }
        };
        resolutionItem.setSelect(mVideoResolutionIndex);
        bottomRoot.addView(resolutionItem);

        // 视频帧率
        VideoFpsItem videoFpsItem = new VideoFpsItem(getContext(),
                getString(R.string.livepusher_fps), getString(R.string.livepusher_comfirm)) {
            @Override
            public void send(String msg) {
                if (mOnSettingChangeListener != null) {
                    mOnSettingChangeListener.onVideoFpsChange(convertToInt(msg));
                }
            }
        };
        bottomRoot.addView(videoFpsItem);


        // 视频码率
        VideoBitrateItem videoBitrateItem = new VideoBitrateItem(getContext(),
                getString(R.string.livepusher_video_bitrate), getString(R.string.livepusher_comfirm)) {
            @Override
            public void send(String msg) {
                if (mOnSettingChangeListener != null) {
                    mOnSettingChangeListener.onVideoBitrateChange(convertToInt(msg));
                }
            }
        };
        videoBitrateItem.setInputText(String.valueOf(mVideoBitrate));
        bottomRoot.addView(videoBitrateItem);

        // 视频最低码率
        VideoMinBitrateItem videoMinBitrateItem = new VideoMinBitrateItem(getContext(),
                getString(R.string.livepusher_min_video_bitrate), getString(R.string.livepusher_comfirm)) {
            @Override
            public void send(String msg) {
                if (mOnSettingChangeListener != null) {
                    mOnSettingChangeListener.onVideoBitrateChange(convertToInt(msg));
                }
            }
        };
        videoMinBitrateItem.setInputText(String.valueOf(mVideoMinBitrate));
        bottomRoot.addView(videoMinBitrateItem);

        // 音量提示间隔
        MicrophoneVolumeEvaluationItem volumeEvaluationItem = new MicrophoneVolumeEvaluationItem(
                getContext(), getString(R.string.livepusher_volume_tips), "") {
            @Override
            public void onSeekBarChange(int process, boolean fromUser) {
                if (fromUser) {
                    setTip(String.valueOf(process));
                    if (null != mOnSettingChangeListener) {
                        mOnSettingChangeListener.onMicrophoneVolumeEvaluation(process);
                    }
                }
            }
        };
        bottomRoot.addView(volumeEvaluationItem);

        // SEI
        if (AppRuntime.get().isDebug()) {
            view.findViewById(R.id.livepusher_ll_sei).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.livepusher_ll_sei).setVisibility(View.GONE);
        }
        mSEIPayloadTypeET = view.findViewById(R.id.livepusher_et_sei_payload_type);
        mSEIPayloadTypeET.setText("5");
        mSEIDataET = view.findViewById(R.id.livepusher_et_sei_data);
        mSEISendBtn = view.findViewById(R.id.livepusher_btn_sei_send);
        mSEISendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSettingChangeListener == null) {
                    return;
                }
                if (TextUtils.isEmpty(mSEIPayloadTypeET.getText())) {
                    Toast.makeText(getContext(), getString(R.string.livepusher_sei_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                int type = 0;
                try {
                    type = Integer.parseInt(mSEIPayloadTypeET.getText().toString());
                    if (!(type == 5 || type == 242)) {
                        Toast.makeText(getContext(), getString(R.string.livepusher_sei_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "invalid sei payload type ", e);
                    Toast.makeText(getContext(), getString(R.string.livepusher_sei_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mSEIDataET.getText() == null || TextUtils.isEmpty(mSEIDataET.getText().toString())) {
                    Log.w(TAG, "invalid sei data");
                    Toast.makeText(getContext(), getString(R.string.livepusher_sei_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] data = mSEIDataET.getText().toString().getBytes();
                mOnSettingChangeListener.onSEISendButtonClick(type, data);
            }
        });
    }

    private void initCheckSelectView(View rootView) {
        mCheckSelectView = (CheckSelectView) rootView.findViewById(R.id.livepusher_ctv_setting_check);
        mCheckSelectView.setData(getResources().getStringArray(R.array.livepusher_setting), mEnables);
        mCheckSelectView.setCheckSelectListener(new CheckSelectView.CheckSelectListener() {
            @Override
            public void onChecked(int position, boolean enable) {
                Log.i(TAG, "onChecked: position -> " + position + ", enable -> " + enable);
                mEnables[position] = enable;
                if (mOnSettingChangeListener == null) {
                    return;
                }
                switch (position) {
                    case POSITION_ADJUST_BITRATE:
                        mOnSettingChangeListener.onAdjustBitrateChange(enable);
                        if (enable) {
                            mOnSettingChangeListener.onVideoBitrateChange(mVideoBitrate);
                            mOnSettingChangeListener.onVideoMinBitrateChange(mVideoMinBitrate);
                        }
                        break;
                    case POSITION_EAR_MONITORING_ENABLE:
                        mOnSettingChangeListener.onEnableAudioEarMonitoringChange(enable);
                        break;
                    case POSITION_MUTE_AUDIO:
                        mOnSettingChangeListener.onMuteChange(enable);
                        break;
                    case POSITION_LANDSCAPE:
                        mOnSettingChangeListener.onHomeOrientationChange(enable);
                        break;
                    case POSITION_WATER_MARK_ENABLE:
                        mOnSettingChangeListener.onWatermarkChange(enable);
                        break;
                    case POSITION_MIRROR_ENABLE:
                        mOnSettingChangeListener.onMirrorChange(enable);
                        break;
                    case POSITION_FLASH_ENABLE:
                        mOnSettingChangeListener.onFlashLightChange(enable);
                        break;
                    case POSITION_FOCUS_ENABLE:
                        mOnSettingChangeListener.onTouchFocusChange(enable);
                        break;
                    case POSITION_PRIVACY_MODEL_ENABLE:
                        mOnSettingChangeListener.onPrivacyModelChange(enable);
                        break;
                    default:
                }
            }
        });
    }

    public void openFlashResult(boolean result) {
        if (mCheckSelectView != null) {
            mEnables[POSITION_FLASH_ENABLE] = result;
            mCheckSelectView.setCheckedNoEvent(POSITION_FLASH_ENABLE, result);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCheckSelectView.setChecked(POSITION_LANDSCAPE, mEnables[POSITION_LANDSCAPE]);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveConfigIntoSp();
    }

    private void initialize() {
        mEnables[POSITION_ADJUST_BITRATE] = true;
        mEnables[POSITION_WATER_MARK_ENABLE] = true;
        mEnables[POSITION_FOCUS_ENABLE] = true;
        mEnables[POSITION_PRIVACY_MODEL_ENABLE] = false;
    }

    private int convertToInt(String text) {
        int value = 0;
        try {
            value = Integer.parseInt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 保存配置到 SharePreferences
     */
    private void saveConfigIntoSp() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(SP_KEY_ADJUST_BITRATE, mEnables[POSITION_ADJUST_BITRATE])
                    .putBoolean(SP_KEY_EAR_MONITORING, mEnables[POSITION_EAR_MONITORING_ENABLE])
                    .putBoolean(SP_KEY_MUTE_AUDIO, mEnables[POSITION_MUTE_AUDIO])
                    .putBoolean(SP_KEY_LANDSCAPE, mEnables[POSITION_LANDSCAPE])
                    .putBoolean(SP_KEY_WATER_MARK, mEnables[POSITION_WATER_MARK_ENABLE])
                    .putBoolean(SP_KEY_MIRROR, mEnables[POSITION_MIRROR_ENABLE])
                    .putBoolean(SP_KEY_FOCUS, mEnables[POSITION_FOCUS_ENABLE])
                    .putBoolean(SP_KEY_PRIVACY_MODEL, mEnables[POSITION_PRIVACY_MODEL_ENABLE])
                    .putInt(SP_KEY_AUDIO_QUALITY, mAudioQualityIndex)
                    .putInt(SP_KEY_RENDER_ROTATION, mRenderRotationIndex)
                    .putInt(SP_KEY_RENDER_MIRROR, mRenderMirrorIndex)
                    .putInt(SP_KEY_VIDEO_RESOLUTION, mVideoResolutionIndex)
                    .apply();
        }
    }

    /**
     * 清理不需要保存的配置
     */
    public void clearConfig(Context context) {
        if (context != null) {
            context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(SP_KEY_PRIVACY_MODEL, false)
                    .apply();
        }
    }

    public void loadConfig(Context context) {
        SharedPreferences s = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mEnables[POSITION_ADJUST_BITRATE] = s.getBoolean(SP_KEY_ADJUST_BITRATE, mEnables[POSITION_ADJUST_BITRATE]);
        mEnables[POSITION_EAR_MONITORING_ENABLE] = s.getBoolean(SP_KEY_EAR_MONITORING, mEnables[POSITION_EAR_MONITORING_ENABLE]);
        mEnables[POSITION_MUTE_AUDIO] = s.getBoolean(SP_KEY_MUTE_AUDIO, mEnables[POSITION_MUTE_AUDIO]);
        mEnables[POSITION_LANDSCAPE] = s.getBoolean(SP_KEY_LANDSCAPE, mEnables[POSITION_LANDSCAPE]);
        mEnables[POSITION_WATER_MARK_ENABLE] = s.getBoolean(SP_KEY_WATER_MARK, mEnables[POSITION_WATER_MARK_ENABLE]);
        mEnables[POSITION_MIRROR_ENABLE] = s.getBoolean(SP_KEY_MIRROR, mEnables[POSITION_MIRROR_ENABLE]);
        mEnables[POSITION_FOCUS_ENABLE] = s.getBoolean(SP_KEY_FOCUS, mEnables[POSITION_FOCUS_ENABLE]);
        mEnables[POSITION_PRIVACY_MODEL_ENABLE] = s.getBoolean(SP_KEY_PRIVACY_MODEL, mEnables[POSITION_PRIVACY_MODEL_ENABLE]);
        mAudioQualityIndex = s.getInt(SP_KEY_AUDIO_QUALITY, mAudioQualityIndex);
        mRenderRotationIndex = s.getInt(SP_KEY_RENDER_ROTATION, mRenderRotationIndex);
        mRenderMirrorIndex = s.getInt(SP_KEY_RENDER_MIRROR, mRenderMirrorIndex);
        mVideoResolutionIndex = s.getInt(SP_KEY_VIDEO_RESOLUTION, mVideoResolutionIndex);
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggle(FragmentManager manager, String tag) {
        if (isVisible()) {
            dismissAllowingStateLoss();
        } else {
            show(manager, tag);
        }
    }

    public void setOnSettingChangeListener(OnSettingChangeListener onSettingChangeListener) {
        mOnSettingChangeListener = onSettingChangeListener;
    }

    public boolean isAdjustBitrate() {
        return mEnables[POSITION_ADJUST_BITRATE];
    }

    public boolean enableAudioEarMonitoring() {
        return mEnables[POSITION_EAR_MONITORING_ENABLE];
    }

    public boolean isMute() {
        return mEnables[POSITION_MUTE_AUDIO];
    }

    public boolean isLandscape() {
        return mEnables[POSITION_LANDSCAPE];
    }

    public boolean isWatermark() {
        return mEnables[POSITION_WATER_MARK_ENABLE];
    }

    public boolean isMirror() {
        return mEnables[POSITION_MIRROR_ENABLE];
    }

    public boolean isFlashEnable() {
        return mEnables[POSITION_FLASH_ENABLE];
    }

    public boolean isTouchFocus() {
        return mEnables[POSITION_FOCUS_ENABLE];
    }

    /**
     * 声音质量
     *
     * @return
     */
    public V2TXLiveDef.V2TXLiveAudioQuality getAudioQuality() {
        V2TXLiveDef.V2TXLiveAudioQuality audioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault;
        switch (mAudioQualityIndex) {
            case AUDIO_SPEECH:  // 语音
                audioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualitySpeech;
                break;
            case AUDIO_DEFAULT: // 标准
                audioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault;
                break;
            case AUDIO_MUSIC:   // 音乐
                audioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityMusic;
                break;
        }
        return audioQuality;
    }

    public V2TXLiveDef.V2TXLiveMirrorType getMirrorType() {
        switch (mRenderMirrorIndex) {
            case 1:
                return V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeDisable;
            case 2:
                return V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeEnable;
            case 0:
            default:
                return V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeAuto;
        }
    }

    public V2TXLiveDef.V2TXLiveRotation getRotationType() {
        switch (mRenderRotationIndex) {
            case 1:
                return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation90;
            case 2:
                return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation180;
            case 3:
                return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation270;
            case 0:
            default:
                return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
        }
    }

    public V2TXLiveDef.V2TXLiveVideoResolution getVideoResolution() {
        switch (mRenderRotationIndex) {
            case 0:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution160x160;
            case 1:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution270x270;
            case 2:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x480;
            case 3:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution320x240;
            case 4:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x360;
            case 5:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x480;
            case 6:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution320x180;
            case 7:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x270;
            case 8:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360;
            case 9:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
            case 10:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720;
            case 11:
                return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1920x1080;
            default:
        }
        return V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
    }

    public interface OnSettingChangeListener {
        /**
         * 音质选择（声道设置）
         * 语音(speech)：16000，单声道
         * 标准(default)：48000，单声道
         * 音乐(music)：48000，双声道
         *
         * @param audioQuality
         */
        void onAudioQualityChange(V2TXLiveDef.V2TXLiveAudioQuality audioQuality);

        /**
         * 码率自适应
         *
         * @param enable
         */
        void onAdjustBitrateChange(boolean enable);

        /**
         * 耳返开关
         *
         * @param enable
         */
        void onEnableAudioEarMonitoringChange(boolean enable);

        /**
         * 是否开启静音推流
         *
         * @param enable
         */
        void onMuteChange(boolean enable);

        /**
         * 横竖屏推流
         *
         * @param isLandscape
         */
        void onHomeOrientationChange(boolean isLandscape);

        /**
         * 开启或关闭观众端镜像
         *
         * @param enable
         */
        void onMirrorChange(boolean enable);

        /**
         * 开启或关闭后置摄像头闪光灯
         *
         * @param enable
         */
        void onFlashLightChange(boolean enable);

        /**
         * 开启或关闭水印
         *
         * @param enable
         */
        void onWatermarkChange(boolean enable);

        /**
         * 开启或关闭手动对焦
         *
         * @param enable
         */
        void onTouchFocusChange(boolean enable);

        /**
         * 点击截图
         */
        void onClickSnapshot();

        /**
         * 隐私模式开关
         *
         * @param enable
         */
        void onPrivacyModelChange(boolean enable);

        /**
         * 发送 SEI 消息
         */
        void onSEISendButtonClick(int payloadType, byte[] data);

        /**
         * 渲染方向
         */
        void onRenderRotation(V2TXLiveDef.V2TXLiveRotation rotation);

        /**
         * 渲染镜像
         */
        void onRenderMirror(V2TXLiveDef.V2TXLiveMirrorType type);

        /**
         * 更新视频分辨率
         *
         * @param resolution
         */
        void onResolutionChange(V2TXLiveDef.V2TXLiveVideoResolution resolution);

        /**
         * 更新视频帧率
         *
         * @param fps
         */
        void onVideoFpsChange(int fps);

        /**
         * 更新视频码率
         *
         * @param bitrate
         */
        void onVideoBitrateChange(int bitrate);

        /**
         * 更新视频最低码率
         *
         * @param bitrate
         */
        void onVideoMinBitrateChange(int bitrate);

        /**
         * 更新音频提示间隔
         * @param evaluation
         */
        void onMicrophoneVolumeEvaluation(int evaluation);
    }
}
