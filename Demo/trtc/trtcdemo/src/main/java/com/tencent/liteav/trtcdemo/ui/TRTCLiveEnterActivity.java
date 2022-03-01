package com.tencent.liteav.trtcdemo.ui;

import static com.tencent.liteav.device.TXDeviceManager.TXCameraCaptureMode.TXCameraCaptureManual;
import static com.tencent.liteav.device.TXDeviceManager.TXCameraCaptureMode.TXCameraResolutionStrategyAuto;
import static com.tencent.liteav.device.TXDeviceManager.TXCameraCaptureMode.TXCameraResolutionStrategyHighQuality;
import static com.tencent.liteav.device.TXDeviceManager.TXCameraCaptureMode.TXCameraResolutionStrategyPerformance;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_EARPIECEMODE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_INPUT_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_QUALITY;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_SCENE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_VOLUMETYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CHORUS_CDN_URL;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_AUDIO_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_PROCESS_USE_RENDER_INTERFACE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_RENDER_BUFFER_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_RENDER_PIXEL_FORMAT;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_VIDEO_PREPROCESS_BUFFER_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_VIDEO_PREPROCESS_PIXEL_FORMAT;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ENCODER_265;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ENCODER_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_LOCAL_RENDER_VIEW_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_MAIN_SCREEN_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_NET_ENV_TYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_PERFORMANCE_MODE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_RECEIVED_AUDIO;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_RECEIVED_VIDEO;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROLE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROOM_ID_STR;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_SUB_SCREEN_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USER_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USE_CUSTOM_OPEN_GL_CONTEXT;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USE_STRING_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_VIDEO_FILE_PATH;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_VIDEO_INPUT_TYPE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.common.UserModel;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.demo.common.listener.OnSingleClickListener;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.Constant;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.Utils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCustomerCrypt;
import com.tencent.liteav.trtcdemo.model.utils.SharedPreferenceUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsEditTextItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSelectionItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSwitchItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.common.EditTextInputItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.AudioInputItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.AudioOutputItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.AudioQualityItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.AudioReceivedItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.AudioSceneItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.AudioVolumeTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.CDNAddressItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.CameraCaptureItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.CameraCaptureResolutionItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.CustomRenderConfigItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.CustomVideoPreprocessBufferTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.CustomVideoPreprocessPixelFormatItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.EncoderItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.EncoderTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.GLContextItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.LocalRenderViewTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.MainVideoInputItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.NetEnvItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.PerformanceModeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.RoleItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.RoomIdTypeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.RoomPasswordItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.SubVideoInputItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem.VideoReceivedItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.moreitem.OtherQosModeItem;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TRTCLiveEnterActivity extends Activity {
    private static final String TAG = "TRTCLiveEnterActivity";

    private LinearLayout                mLinearContainer;
    private List<View>                  mSettingItemList;
    private CameraCaptureItem           mCameraCaptureItem;
    private CameraCaptureResolutionItem mCameraCaptureResolutionItem;
    private AbsRadioButtonItem          mRoleItem;
    private EditTextInputItem           mEditRoomID;
    private AbsEditTextItem             mEditRoomPassword;
    private AbsRadioButtonItem          mNetEnvItem;
    private AbsRadioButtonItem          mMainVideoInputItem;
    private AbsRadioButtonItem          mSubVideoInputItem;
    private AbsRadioButtonItem          mAudioInputItem;
    private AbsRadioButtonItem          mVideoReceivedItem;
    private AbsRadioButtonItem          mAudioReceivedItem;
    private AbsRadioButtonItem          mAudioQualityItem;
    private AbsRadioButtonItem          mRadioAudioOutputItem;
    private AbsRadioButtonItem          mAudioVolumeTypeItem;
    private AbsRadioButtonItem          mLocalRenderViewTypeItem;
    private AbsRadioButtonItem          mEncoderTypeItem;
    private AbsRadioButtonItem          mEncoderItem;
    private AbsSwitchItem               mPerformanceModeItem;
    private AbsRadioButtonItem          mCustomVideoPreprocessBufferTypeItem;
    private AbsRadioButtonItem          mCustomVideoPreprocessPixelFormatItem;
    private AbsRadioButtonItem          mRoomIdTypeItem;
    private AbsSelectionItem            mCustomRenderConfigItem;
    private AbsSwitchItem               mGLContextItem;
    private AbsSwitchItem               mCustomVideoPreprocessUseRenderItem;
    private OtherQosModeItem            mQosModeItem;
    private AbsRadioButtonItem          mAudioSceneItem;
    private AbsEditTextItem             mCDNAddressItem;

    private String mVideoFile = "";

    private static final FrameConfig[] CUSTOM_RENDER_SUPPORT_CONFIGS = {
            new FrameConfig("RGBA - ByteArray",
                    TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_RGBA, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY),
            new FrameConfig("RGBA - ByteBuffer",
                    TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_RGBA, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_BUFFER),
            new FrameConfig("I420 - ByteArray",
                    TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_I420, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY),
            new FrameConfig("I420 - ByteBuffer",
                    TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_I420, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_BUFFER),
            new FrameConfig("Texture",
                    TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trtcdemo_activity_live_enter);
        initView();
    }

    private void initView() {
        mSettingItemList = new ArrayList<>();
        mLinearContainer = (LinearLayout) findViewById(R.id.ll_content);

        // 请输入房间号
        mEditRoomID = new EditTextInputItem(this, getString(R.string.trtcdemo_please_input_roomid), "");
        mEditRoomID.setText((String) SharedPreferenceUtils.getInstance(this).getSharedPreference(
                SharedPreferenceUtils.KEY.LAST_INPUT_ROOMID, "12348888"));
        mSettingItemList.add(mEditRoomID);

        // 房间密码
        mEditRoomPassword = new RoomPasswordItem(this, getString(R.string.trtcdemo_room_password));
        mSettingItemList.add(mEditRoomPassword);

        // 云端环境
        mNetEnvItem = new NetEnvItem(this,
                getString(R.string.trtcdemo_net_env),
                getString(R.string.trtcdemo_product),
                getString(R.string.trtcdemo_test),
                getString(R.string.trtcdemo_experience));
        mSettingItemList.add(mNetEnvItem);

        // 音频场景
        mAudioSceneItem = new AudioSceneItem(this, new AudioSceneItem.OnSelectListener() {
            @Override
            public void onSelected(int index) {
                mCDNAddressItem.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
            }
        },
                getString(R.string.trtcdemo_audio_scene),
                getString(R.string.trtcdemo_audio_scene_chorus),
                getString(R.string.trtcdemo_audio_scene_normal));
        mSettingItemList.add(mAudioSceneItem);

        // 音频场景下，输入CDN地址
        mCDNAddressItem = new CDNAddressItem(this,
                getString(R.string.trtcdemo_please_input_cdn_push_url));
        mCDNAddressItem.setVisibility(View.GONE);
        mSettingItemList.add(mCDNAddressItem);

        // 角色选择
        mRoleItem = new RoleItem(this, new RoleItem.OnSelectListener() {
            @Override
            public void onSelected(int index) {
                mCDNAddressItem.setTitle(index == 0
                        ? getString(R.string.trtcdemo_please_input_cdn_push_url)
                        : getString(R.string.trtcdemo_please_input_cdn_pull_url));
            }
        },
                getString(R.string.trtcdemo_role_select),
                getString(R.string.trtcdemo_anchor),
                getString(R.string.trtcdemo_audience));
        mSettingItemList.add(mRoleItem);

        // 主路视频输入
        mMainVideoInputItem = createMainVideoInputItem();
        mSettingItemList.add(mMainVideoInputItem);

        // 辅路视频输入
        mSubVideoInputItem = new SubVideoInputItem(this,
                getString(R.string.trtcdemo_sub_video_input),
                getString(R.string.trtcdemo_null),
                getString(R.string.trtcdemo_screen_capture)) {
            @Override
            public void onSelected(int index) {
                if (index == 1) {
                    // 选择辅路录屏后，清除主路录屏设置
                    if (mMainVideoInputItem.getSelect() == 2) {
                        mMainVideoInputItem.setSelect(0);
                    }
                }
            }
        };
        mSettingItemList.add(mSubVideoInputItem);

        // 摄像头采集
        mCameraCaptureResolutionItem = new CameraCaptureResolutionItem(this);
        mCameraCaptureItem = new CameraCaptureItem(this,
                getString(R.string.trtcdemo_camera_capture),
                getString(R.string.trtcdemo_camera_capture_auto),
                getString(R.string.trtcdemo_camera_capture_performance),
                getString(R.string.trtcdemo_camera_capture_high),
                getString(R.string.trtcdemo_camera_capture_custom)) {
            @Override
            public void onSelected(int index) {
                if (index == 3) {
                    mCameraCaptureResolutionItem.setVisibility(View.VISIBLE);
                } else {
                    mCameraCaptureResolutionItem.setVisibility(View.GONE);
                }
            }
        };
        mCameraCaptureItem.setSelect(3);
        mSettingItemList.add(mCameraCaptureItem);
        mSettingItemList.add(mCameraCaptureResolutionItem);

        // 音频输入
        mAudioInputItem = new AudioInputItem(this,
                getString(R.string.trtcdemo_audio_input),
                getString(R.string.trtcdemo_sdk_capture),
                getString(R.string.trtcdemo_custom_capture),
                getString(R.string.trtcdemo_null));
        mSettingItemList.add(mAudioInputItem);

        mCustomRenderConfigItem = new CustomRenderConfigItem(this, true,
                getString(R.string.trtcdemo_custom_render_type),
                getResources().getStringArray(R.array.trtcdemo_video_render_type));
        mSettingItemList.add(mCustomRenderConfigItem);
        mCustomRenderConfigItem.setVisibility(View.GONE);

        mGLContextItem = new GLContextItem(this, true,
                getString(R.string.trtcdemo_custom_render_glcontext));
        mSettingItemList.add(mGLContextItem);
        mGLContextItem.setVisibility(View.GONE);

        mCustomVideoPreprocessUseRenderItem = new GLContextItem(this, true,
                getString(R.string.trtcdemo_custom_render_use_api));
        mSettingItemList.add(mCustomVideoPreprocessUseRenderItem);
        mCustomVideoPreprocessUseRenderItem.setVisibility(View.GONE);

        // 视频接收
        mVideoReceivedItem = new VideoReceivedItem(this,
                getString(R.string.trtcdemo_video_receive),
                getString(R.string.trtcdemo_auto),
                getString(R.string.trtcdemo_manual));
        mSettingItemList.add(mVideoReceivedItem);

        // 音频接收
        mAudioReceivedItem = new AudioReceivedItem(this,
                getString(R.string.trtcdemo_audio_receive),
                getString(R.string.trtcdemo_auto),
                getString(R.string.trtcdemo_manual));
        mSettingItemList.add(mAudioReceivedItem);

        //流控方案
        mQosModeItem = new OtherQosModeItem(this,
                getString(R.string.trtcdemo_qos_mode),
                getString(R.string.trtcdemo_control_client),
                getString(R.string.trtcdemo_control_server));
        mSettingItemList.add(mQosModeItem);

        // 音量类型
        mAudioVolumeTypeItem = new AudioVolumeTypeItem(this,
                getString(R.string.trtcdemo_volumn_type),
                getString(R.string.trtcdemo_volumn_type_auto),
                getString(R.string.trtcdemo_volumn_type_media),
                getString(R.string.trtcdemo_volumn_type_voip),
                getString(R.string.trtcdemo_no_choice));
        mSettingItemList.add(mAudioVolumeTypeItem);

        // 本地画面渲染类型
        mLocalRenderViewTypeItem = new LocalRenderViewTypeItem(this,
                getString(R.string.trtcdemo_local_render_view_type),
                getString(R.string.trtcdemo_render_view_default),
                getString(R.string.trtcdemo_render_view_surfaceview),
                getString(R.string.trtcdemo_render_view_textureview),
                getString(R.string.trtcdemo_render_view_custom));
        mSettingItemList.add(mLocalRenderViewTypeItem);

        // 编码类型
        mEncoderTypeItem = new EncoderTypeItem(this,
                getString(R.string.trtcdemo_encoder_type),
                getString(R.string.trtcdemo_encoder_h265),
                getString(R.string.trtcdemo_encoder_h264));
        mSettingItemList.add(mEncoderTypeItem);

        // 软硬编码设置
        mEncoderItem = new EncoderItem(this,
                getString(R.string.trtcdemo_encoder_soft_hard_setting),
                getString(R.string.trtcdemo_encoder_software),
                getString(R.string.trtcdemo_encoder_hardware),
                getString(R.string.trtcdemo_encoder_auto));
        mSettingItemList.add(mEncoderItem);
        mEncoderItem.setSelect(Constant.ENCODER_AUTO);

        // 性能模式
        mPerformanceModeItem = new PerformanceModeItem(this, getString(R.string.trtcdemo_performance_mode));
        mSettingItemList.add(mPerformanceModeItem);

        // 自定义预处理存储格式
        mCustomVideoPreprocessBufferTypeItem = new CustomVideoPreprocessBufferTypeItem(this,
                getString(R.string.trtcdemo_custom_video_preprocess_buffer_type),
                getString(R.string.trtcdemo_custom_video_preprocess_close),
                getString(R.string.trtcdemo_custom_video_preprocess_texture),
                getString(R.string.trtcdemo_custom_video_preprocess_byte_array),
                getString(R.string.trtcdemo_custom_video_preprocess_byte_buffer)) {
            @Override
            public void onSelected(int index) {
                if (index == 0 || index == 1) {
                    mCustomVideoPreprocessPixelFormatItem.setSelect(0);
                    mCustomVideoPreprocessPixelFormatItem.setVisibility(View.GONE);
                } else {
                    mCustomVideoPreprocessPixelFormatItem.setVisibility(View.VISIBLE);
                }
            }
        };
        mSettingItemList.add(mCustomVideoPreprocessBufferTypeItem);
        mCustomVideoPreprocessBufferTypeItem.setSelect(0);

        // 自定义预处理数据格式
        mCustomVideoPreprocessPixelFormatItem = new CustomVideoPreprocessPixelFormatItem(this,
                getString(R.string.trtcdemo_custom_video_preprocess_pixel_format),
                getString(R.string.trtcdemo_custom_video_preprocess_i420),
                getString(R.string.trtcdemo_custom_video_preprocess_nv21));
        mSettingItemList.add(mCustomVideoPreprocessPixelFormatItem);
        mCustomVideoPreprocessPixelFormatItem.setVisibility(View.GONE);

        // 音频输出
        mRadioAudioOutputItem = new AudioOutputItem(this,
                getString(R.string.trtcdemo_audio_output),
                getString(R.string.trtcdemo_speaker),
                getString(R.string.trtcdemo_receiver));
        mSettingItemList.add(mRadioAudioOutputItem);

        // 音频质量
        mAudioQualityItem = new AudioQualityItem(this,
                getString(R.string.trtcdemo_audio_quality),
                getString(R.string.trtcdemo_audio_quality_music),
                getString(R.string.trtcdemo_default),
                getString(R.string.trtcdemo_voice),
                getString(R.string.trtcdemo_no_choice));
        mSettingItemList.add(mAudioQualityItem);

        // 房间号类型
        mRoomIdTypeItem = new RoomIdTypeItem(this,
                getString(R.string.trtcdemo_roomid_type),
                getString(R.string.trtcdemo_number),
                getString(R.string.trtcdemo_string)) {
            @Override
            public void onSelected(int index) {
                if (index == 0) {
                    mEditRoomID.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else if (index == 1) {
                    mEditRoomID.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        };
        mSettingItemList.add(mRoomIdTypeItem);

        for (View view : mSettingItemList) {
            mLinearContainer.addView(view);
        }

        findViewById(R.id.btn_enter_room).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mMainVideoInputItem.getSelect() == 1) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(intent, 1);
                    return;
                }
                startJoinRoom();
            }
        });

        findViewById(R.id.trtc_ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private MainVideoInputItem createMainVideoInputItem() {
        return new MainVideoInputItem(this,
                getString(R.string.trtcdemo_main_video_input),
                getString(R.string.trtcdemo_camera),
                getString(R.string.trtcdemo_video_file),
                getString(R.string.trtcdemo_screen_capture),
                getString(R.string.trtcdemo_null)) {
            @Override
            public void onSelected(int index) {
                if (mAudioVolumeTypeItem != null) {
                    // 选择视频文件的时候音量类型默认使用媒体音量
                    if (index == 1) {
                        mAudioVolumeTypeItem.setSelect(1);
                    } else {
                        mAudioVolumeTypeItem.setSelect(0);
                    }
                }
                // 只有摄像头才支持选择本地渲染的类型
                if (index == 0) {
                    mLocalRenderViewTypeItem.setSelect(0);
                    mLocalRenderViewTypeItem.setVisibility(View.VISIBLE);
                    mAudioSceneItem.setVisibility(View.VISIBLE);
                } else {
                    mLocalRenderViewTypeItem.setVisibility(View.GONE);
                    mAudioSceneItem.setVisibility(GONE);
                }

                // 自定义采集 不支持 自定义预处理
                if (index == 1 || index == 3) {
                    mCustomVideoPreprocessBufferTypeItem.setSelect(0);
                    mCustomVideoPreprocessBufferTypeItem.setVisibility(View.GONE);
                    mCustomVideoPreprocessPixelFormatItem.setSelect(0);
                    mCustomVideoPreprocessPixelFormatItem.setVisibility(View.GONE);
                } else {
                    mCustomVideoPreprocessBufferTypeItem.setSelect(0);
                    mCustomVideoPreprocessBufferTypeItem.setVisibility(View.VISIBLE);
                    mCustomVideoPreprocessPixelFormatItem.setVisibility(View.GONE);
                }
                // 选择主路录屏后，清除辅路录屏设置
                if (index == 2) {
                    if (mSubVideoInputItem.getSelect() == 1) {
                        mSubVideoInputItem.setSelect(0);
                    }
                }
                // 自定义采集 不支持 自定义预处理
                if (isEnableDebugMode()) {
                    if (index == 1) {
                        mCustomVideoPreprocessBufferTypeItem.setSelect(0);
                        mCustomVideoPreprocessBufferTypeItem.setVisibility(View.GONE);
                        mCustomVideoPreprocessPixelFormatItem.setSelect(0);
                        mCustomVideoPreprocessPixelFormatItem.setVisibility(View.GONE);
                        mCustomRenderConfigItem.setVisibility(View.VISIBLE);
                        mCustomVideoPreprocessUseRenderItem.setVisibility(View.VISIBLE);
                        mGLContextItem.setVisibility(View.VISIBLE);
                    } else {
                        mCustomVideoPreprocessBufferTypeItem.setVisibility(View.VISIBLE);
                        mCustomVideoPreprocessPixelFormatItem.setVisibility(View.VISIBLE);
                        mCustomRenderConfigItem.setVisibility(View.GONE);
                        mCustomVideoPreprocessUseRenderItem.setVisibility(View.GONE);
                        mGLContextItem.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

    private void startJoinRoom() {
        String mRoomIdText = "";
        try {
            if (TextUtils.isEmpty(mEditRoomID.getText())) {
                throw new Exception();
            }
            if (mRoomIdTypeItem.getSelect() == 0) {
                Long.valueOf(mEditRoomID.getText()).intValue();
            }
            mRoomIdText = mEditRoomID.getText();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.trtcdemo_please_input_roomid), Toast.LENGTH_SHORT).show();
            return;
        }
        TRTCCustomerCrypt.sharedInstance().encryptKey = mEditRoomPassword.getText();
        UserModel userModel = UserModelManager.getInstance().getUserModel();
        final String userId = userModel.userName;
        SharedPreferenceUtils.getInstance(this).put(SharedPreferenceUtils.KEY.LAST_INPUT_ROOMID, mRoomIdText);

        TXDeviceManager deviceManager = TRTCCloud.sharedInstance(this.getApplicationContext()).getDeviceManager();
        TXDeviceManager.TXCameraCaptureParam params = new TXDeviceManager.TXCameraCaptureParam();
        // 性能模式开启时，以性能模式为主，强制设置摄像头采集模式为性能模式
        // 并且此时摄像头采集的模式设置无效
        if (mPerformanceModeItem.getChecked()) {
            params.mode = TXCameraResolutionStrategyPerformance;
        } else {
            switch (mCameraCaptureItem.getSelect()) {
                case 1:
                    params.mode = TXCameraResolutionStrategyPerformance;
                    break;
                case 2:
                    params.mode = TXCameraResolutionStrategyHighQuality;
                    break;
                case 3:
                    params.mode = TXCameraCaptureManual;
                    params.width = mCameraCaptureResolutionItem.getXResolution();
                    params.height = mCameraCaptureResolutionItem.getYResolution();
                    break;
                default:
                    params.mode = TXCameraResolutionStrategyAuto;
                    break;
            }
        }
        deviceManager.setCameraCapturerParam(params);

        startJoinRoomInternal(mRoomIdText, userId);
    }

    private void startJoinRoomInternal(final String roomId, final String userId) {
        final Intent intent;
        if (mRoleItem.getSelect() == 0) {
            intent = new Intent(this, TRTCLiveAnchorActivity.class);
        } else {
            intent = new Intent(this, TRTCLiveAudienceActivity.class);
        }
        if (mRoomIdTypeItem.getSelect() == 0) {
            intent.putExtra(KEY_ROOM_ID, Long.valueOf(mEditRoomID.getText()).intValue());
        } else {
            intent.putExtra(KEY_ROOM_ID_STR, roomId);
        }

        intent.putExtra(KEY_USER_ID, userId);
        intent.putExtra(KEY_ROLE,
                mRoleItem.getSelect() == 0 ? TRTCCloudDef.TRTCRoleAnchor : TRTCCloudDef.TRTCRoleAudience);
        if (mMainVideoInputItem.getSelect() == 1 && !TextUtils.isEmpty(mVideoFile)) {
            intent.putExtra(KEY_CUSTOM_CAPTURE, true);
            intent.putExtra(KEY_VIDEO_FILE_PATH, mVideoFile);
        } else if (mMainVideoInputItem.getSelect() == 2) {
            intent.putExtra(KEY_MAIN_SCREEN_CAPTURE, true);
        } else if (mMainVideoInputItem.getSelect() == 3) {
            SettingConfigHelper.getInstance().getVideoConfig().setEnableVideo(false);
        }

        intent.putExtra(KEY_AUDIO_QUALITY, ((AudioQualityItem) mAudioQualityItem).getType());
        intent.putExtra(KEY_CUSTOM_AUDIO_CAPTURE, mAudioInputItem.getSelect() == 1);
        intent.putExtra(KEY_AUDIO_VOLUMETYPE, ((AudioVolumeTypeItem) mAudioVolumeTypeItem).getAudioVolumeType());
        intent.putExtra(KEY_AUDIO_EARPIECEMODE, mRadioAudioOutputItem.getSelect() != 0);
        intent.putExtra(KEY_USE_STRING_ROOM_ID, mRoomIdTypeItem.getSelect());
        intent.putExtra(KEY_VIDEO_INPUT_TYPE, mMainVideoInputItem.getSelect());
        intent.putExtra(KEY_AUDIO_INPUT_TYPE, mAudioInputItem.getSelect());
        if (isEnableDebugMode()) {
            intent.putExtra(KEY_NET_ENV_TYPE, mNetEnvItem.getSelect());
            intent.putExtra(KEY_SUB_SCREEN_CAPTURE, mSubVideoInputItem.getSelect() == 1);
            intent.putExtra(KEY_RECEIVED_VIDEO, mVideoReceivedItem.getSelect() == 0);
            intent.putExtra(KEY_RECEIVED_AUDIO, mAudioReceivedItem.getSelect() == 0);
            intent.putExtra(KEY_LOCAL_RENDER_VIEW_TYPE,
                    ((LocalRenderViewTypeItem) mLocalRenderViewTypeItem).getLocalRenderViewType());
            intent.putExtra(KEY_ENCODER_265, mEncoderTypeItem.getSelect() == 0);
            intent.putExtra(KEY_ENCODER_TYPE, getEncoderType(mEncoderItem.getSelect()));
            intent.putExtra(KEY_PERFORMANCE_MODE, mPerformanceModeItem.getChecked());
            intent.putExtra(KEY_CUSTOM_VIDEO_PREPROCESS_PIXEL_FORMAT,
                    ((CustomVideoPreprocessPixelFormatItem) mCustomVideoPreprocessPixelFormatItem)
                            .getCustomVideoPreprocessPixelFormat());
            intent.putExtra(KEY_CUSTOM_VIDEO_PREPROCESS_BUFFER_TYPE,
                    ((CustomVideoPreprocessBufferTypeItem) mCustomVideoPreprocessBufferTypeItem)
                            .getCustomVideoPreprocessBufferType());

            FrameConfig customRenderConfig = CUSTOM_RENDER_SUPPORT_CONFIGS[mCustomRenderConfigItem.getSelected()];
            intent.putExtra(KEY_CUSTOM_RENDER_PIXEL_FORMAT, customRenderConfig.pixelFormat);
            intent.putExtra(KEY_CUSTOM_RENDER_BUFFER_TYPE, customRenderConfig.bufferType);
            intent.putExtra(KEY_CUSTOM_PROCESS_USE_RENDER_INTERFACE, mCustomVideoPreprocessUseRenderItem.getChecked());
            intent.putExtra(KEY_USE_CUSTOM_OPEN_GL_CONTEXT, mGLContextItem.getChecked());
            intent.putExtra(KEY_AUDIO_SCENE, mAudioSceneItem.getSelect() == 0);
            intent.putExtra(KEY_CHORUS_CDN_URL, mCDNAddressItem.getText());
            if (mAudioSceneItem.getSelect() == 0) {
                TRTCCloud.sharedInstance(TRTCLiveEnterActivity.this).callExperimentalAPI(
                        String.format(Locale.ENGLISH, "{\"api"
                                + "\":\"enableRealtimeChorus\", \"params\": {\"enable\":%d}}", 1));
            } else {
                TRTCCloud.sharedInstance(TRTCLiveEnterActivity.this).callExperimentalAPI(
                        String.format(Locale.ENGLISH, "{\"api"
                                + "\":\"enableRealtimeChorus\", \"params\": {\"enable\":%d}}", 0));
            }
        } else {
            intent.putExtra(KEY_CUSTOM_RENDER_PIXEL_FORMAT, TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_RGBA);
            intent.putExtra(KEY_CUSTOM_RENDER_BUFFER_TYPE, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY);
        }
        startActivity(intent);
    }

    private int getEncoderType(int index) {
        int type;
        switch (index) {
            case 0:
                type = Constant.ENCODER_SOFTWARE;
                break;
            case 1:
                type = Constant.ENCODER_HARDWARE;
                break;
            case 2:
                type = Constant.ENCODER_AUTO;
                break;
            default:
                Log.e(TAG, "getEncoderType : wrong index");
                type = Constant.ENCODER_AUTO;
                break;
        }
        return type;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                mVideoFile = uri.getPath();
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    mVideoFile = Utils.getPath(this, uri);
                } else {
                    mVideoFile = Utils.getRealPathFromURI(this, uri);
                }
            }
        }
        startJoinRoom();
    }

    private boolean isEnableDebugMode() {
        return AppRuntime.get().isDebug();
    }

    private static class FrameConfig {
        public String name;
        public int    pixelFormat;
        public int    bufferType;

        public FrameConfig(String name, int pixelFormat, int bufferType) {
            this.name = name;
            this.pixelFormat = pixelFormat;
            this.bufferType = bufferType;
        }
    }
}
