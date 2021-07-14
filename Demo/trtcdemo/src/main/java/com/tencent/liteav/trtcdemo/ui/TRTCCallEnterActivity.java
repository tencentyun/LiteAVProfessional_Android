package com.tencent.liteav.trtcdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.Utils;
import com.tencent.liteav.trtcdemo.model.utils.SharedPreferenceUtils;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.EditTextInputSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.RadioButtonSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.SingleButtonSettingItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_EARPIECEMODE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_QUALITY;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_AUDIO_VOLUMETYPE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_AUDIO_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_CUSTOM_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_MAIN_SCREEN_CAPTURE;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_ROOM_ID_STR;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USER_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_USE_STRING_ROOM_ID;
import static com.tencent.liteav.trtcdemo.model.bean.Constant.KEY_VIDEO_FILE_PATH;

public class TRTCCallEnterActivity  extends Activity {

    private LinearLayout                mLinearContainer;
    private List<BaseSettingItem>       mSettingItemList;
    private EditTextInputSettingItem    mEditRoomID;
    private EditTextInputSettingItem    mEditUserID;
    private RadioButtonSettingItem      mVideoInputItem;
    private RadioButtonSettingItem      mAudioInputItem;
    private RadioButtonSettingItem      mAudioQualityItem;
    private RadioButtonSettingItem      mRadioAudioOutputItem;
    private RadioButtonSettingItem      mAudioVolumeTypeItem;
    private RadioButtonSettingItem      mRoomIdTypeItem;

    private boolean mIsAudioEarpieceMode    = false;
    private int     mAudioVolumeType        = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
    private String  mVideoFile              = "";
    private int     mRoomIdType             = 0;  //0:数字  1：字符串

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trtcdemo_activity_call_enter);
        initView();
    }

    private void initView() {
        mSettingItemList = new ArrayList<>();
        mLinearContainer = (LinearLayout) findViewById(R.id.ll_content);

        mEditRoomID = new EditTextInputSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_please_input_roomid)), 0);
        mEditRoomID.setText((String)SharedPreferenceUtils.getInstance(this).getSharedPreference(SharedPreferenceUtils.KEY.LAST_INPUT_ROOMID, "12348888"));
        mSettingItemList.add(mEditRoomID);

        String userId = new Random().nextInt(100000) + 1000000 + "";
        mEditUserID = new EditTextInputSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_please_input_userid)), 5);
        mEditUserID.setText(userId);
        mEditUserID.setInputType(InputType.TYPE_CLASS_TEXT);
        mSettingItemList.add(mEditUserID);

        mVideoInputItem = new RadioButtonSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_video_input), getString(R.string.trtcdemo_camera), getString(R.string.trtcdemo_video_file), getString(R.string.trtcdemo_screen_capture)),
                new RadioButtonSettingItem.SelectedListener() {
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
                    }
                });
        mVideoInputItem.setPaddingTop(SizeUtils.dp2px(15));
        mSettingItemList.add(mVideoInputItem);

        mAudioInputItem = new RadioButtonSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_audio_input), getString(R.string.trtcdemo_sdk_capture), getString(R.string.trtcdemo_custom_capture)), null);
        mSettingItemList.add(mAudioInputItem);

        mAudioVolumeTypeItem = new RadioButtonSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_volumn_type), getString(R.string.trtcdemo_volumn_type_auto), getString(R.string.trtcdemo_volumn_type_media), getString(R.string.trtcdemo_volumn_type_voip)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        if (0 == index) {
                            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
                        } else if (1 == index) {
                            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeMedia;
                        } else if (2 == index) {
                            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeVOIP;
                        } else {
                            mAudioVolumeType = TRTCCloudDef.TRTCSystemVolumeTypeAuto;
                        }
                    }
                });
        mSettingItemList.add(mAudioVolumeTypeItem);
        mAudioVolumeTypeItem.setSelect(0);

        mRadioAudioOutputItem = new RadioButtonSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_audio_output), getString(R.string.trtcdemo_speaker), getString(R.string.trtcdemo_receiver)), new RadioButtonSettingItem.SelectedListener() {
            @Override
            public void onSelected(int index) {
                if(index == 0){
                    mIsAudioEarpieceMode = false;
                }else{
                    mIsAudioEarpieceMode = true;
                }

            }
        });
        mSettingItemList.add(mRadioAudioOutputItem);

        mAudioQualityItem = new RadioButtonSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_audio_quality), getString(R.string.trtcdemo_audio_quality_music), getString(R.string.trtcdemo_defautl), getString(R.string.trtcdemo_voice)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                    }
                });
        mAudioQualityItem.setSelect(1);
        mSettingItemList.add(mAudioQualityItem);

        mRoomIdTypeItem = new RadioButtonSettingItem(this, new BaseSettingItem.ItemText(getString(R.string.trtcdemo_roomid_type), getString(R.string.trtcdemo_number), getString(R.string.trtcdemo_string)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mRoomIdType = index;
                        if(index == 0){
                            mEditRoomID.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }else if(index == 1){
                            mEditRoomID.setInputType(InputType.TYPE_CLASS_TEXT);
                        }
                    }
                });
        mSettingItemList.add(mRoomIdTypeItem);

        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, item.getPaddingTop(), 0, 0);
            mLinearContainer.addView(view);
        }

        findViewById(R.id.btn_enter_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoInputItem.getSelected() == 1) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(intent, 1);
                    return;
                }
                startJoinRoom();
            }
        });
    }

    private void startJoinRoom() {
        String mRoomIdText = "";
        try {
            if(TextUtils.isEmpty(mEditRoomID.getText())){
                throw new Exception();
            }
            if(mRoomIdType == 0){
                Long.valueOf(mEditRoomID.getText()).intValue();
            }
            mRoomIdText = mEditRoomID.getText();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.trtcdemo_please_input_roomid), Toast.LENGTH_SHORT).show();
            return;
        }
        final String userId = mEditUserID.getText();
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, getString(R.string.trtcdemo_please_input_userid), Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferenceUtils.getInstance(this).put(SharedPreferenceUtils.KEY.LAST_INPUT_ROOMID, mRoomIdText);
        startJoinRoomInternal(mRoomIdText, userId);
    }

    private void startJoinRoomInternal(final String roomId, final String userId) {
        final Intent intent = new Intent(this, TRTCCallActivity.class);
        if(mRoomIdType == 0){
            intent.putExtra(KEY_ROOM_ID, Long.valueOf(mEditRoomID.getText()).intValue());
        }else{
            intent.putExtra(KEY_ROOM_ID_STR, roomId);
        }

        intent.putExtra(KEY_USER_ID, userId);
        if (mVideoInputItem.getSelected() == 1 && !TextUtils.isEmpty(mVideoFile)) {
            intent.putExtra(KEY_CUSTOM_CAPTURE, true);
            intent.putExtra(KEY_VIDEO_FILE_PATH, mVideoFile);
        } else if (mVideoInputItem.getSelected() == 2) {
            intent.putExtra(KEY_MAIN_SCREEN_CAPTURE, true);
        }

        int quality_index = mAudioQualityItem.getSelected();
        if (0 == quality_index) {
            intent.putExtra(KEY_AUDIO_QUALITY, TRTCCloudDef.TRTC_AUDIO_QUALITY_MUSIC);
        } else if (1 == quality_index) {
            intent.putExtra(KEY_AUDIO_QUALITY, TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT);
        } else if (2 == quality_index) {
            intent.putExtra(KEY_AUDIO_QUALITY, TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        }
        intent.putExtra(KEY_CUSTOM_AUDIO_CAPTURE, mAudioInputItem.getSelected() == 1);
        intent.putExtra(KEY_AUDIO_VOLUMETYPE, mAudioVolumeType);
        intent.putExtra(KEY_AUDIO_EARPIECEMODE, mIsAudioEarpieceMode);
        intent.putExtra(KEY_USE_STRING_ROOM_ID, mRoomIdType);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
