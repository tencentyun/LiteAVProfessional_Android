package com.tencent.liteav.demo.livelinkmicnew;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.demo.livelinkmicnew.settting.AVSettingConfig;
import com.tencent.liteav.demo.livelinkmicnew.settting.ErrorDialog;
import com.tencent.liteav.demo.livelinkmicnew.settting.PlaySetting;
import com.tencent.liteav.demo.livelinkmicnew.settting.PushSetting;
import com.tencent.liteav.demo.livelinkmicnew.widget.MainItemRenderView;
import com.tencent.liteav.demo.livelinkmicnew.widget.QRCodeGenerateFragment;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.model.UserModel;
import com.tencent.live2.V2TXLiveCode;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.live2.trtc.TXLiveUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;

public class V2MainActivity extends AppCompatActivity {

    private static final String TAG = "V2MainActivity";

    private static final int ACTIVITY_PUSHER_REQUEST_CODE = 0x0010;
    private static final int ACTIVITY_PLAYER_REQUEST_CODE = 0x0020;
    private static final int REQ_PERMISSION_CODE = 0x1000;
    private static final int PLAY_ERROR_TIMEOUT  = 5000;

    private static final String PUSHER_PLAY_QR_CODE_FRAGMENT = "push_play_qr_code_fragment";
    private static final String NORMAL_PLAY_URL              = "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv";
    private static final String URL_FETCH_PUSH_URL           = "https://lvb.qcloud.com/weapp/utils/get_test_pushurl";

    private Handler mHandler = new Handler(Looper.getMainLooper());

    // pusher
    private MainItemRenderView mPushRenderView;
    private V2TXLivePusher mLivePusher;
    private PlayerViewContainer mCurrentPlayViewContainer;
    private MainProtocolSelectDialog mPlayChooseTypeFragmentDialog;

    private String mPusherRoomId;
    private String mPusherUserId;
    private String mQRCodePusherURL;
    private String mScanPlayURL;
    private String mScanPushURL;

    private boolean mHasInitPusher = false;
    private boolean mHasStopPusher = false;
    private boolean mIsPusherStart = false;
    private boolean mIsFrontCamera = true;
    private boolean mIsMuteVideo = false;
    private boolean mIsMuteAudio = false;
    private boolean mIsPushFullScreen = false;
    private boolean mIsShowPusherDebugView = false;
    private boolean mIsRoomProtocol = true;
    private boolean mIsPlayFullScreen = false;

    private EditText mPushCDNEditTextUrl;
    private EditText mPushROOMRoomId;
    private EditText mPushROOMUserId;
    private EditText mPushTRTCStreamId;
    private EditText mPlayTRTCStreamId;
    private EditText mPlayROOMRoomId;
    private EditText mPlayROOMUserId;
    private EditText mPlayCNDURL;

    private PushSetting mPushSettingFragmentDialog;
    private MainProtocolSelectDialog mPushChooseTypeFragmentDialog;
    private QRCodeGenerateFragment mPusherPlayQRCodeFragment;
    private V2VideoSource mVideoSource = V2VideoSource.CAMERA;
    private PlaySetting mPlaySetting;

    // player
    private final List<PlayerViewContainer> mRemoteRenderViewList = new ArrayList<>();
    private long[] mHits = new long[2]; //存储时间的数组

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_link_mic_new_activity_main);
        initView();
        checkPermission();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(V2MainActivity.this,
                        (String[]) permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "[Player] onActivityResult requestCode " + requestCode + ", resultCode " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_PLAYER_REQUEST_CODE) {
                mScanPlayURL = data.getStringExtra(AVSettingConfig.INTENT_SCAN_RESULT);
                int position = data.getIntExtra("position", 0);
                Log.i(TAG, "[Player] scanURL " + mScanPlayURL + ", position " + position);
                if (AVSettingConfig.getInstance().playerViewScanMap.get(mScanPlayURL) != null) {
                    Toast.makeText(V2MainActivity.this, "该播放地址正在播放，请换一个播放地址", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "[Player] scanURL url is same");
                    return;
                }

                AVSettingConfig.getInstance().playerViewScanMap.put(mScanPlayURL, mRemoteRenderViewList.get(position).playerView);
                Toast.makeText(this, "url: " + mScanPlayURL, Toast.LENGTH_LONG).show();
                String playUrl = mScanPlayURL;
                if (mScanPlayURL.startsWith("room://") || mScanPlayURL.startsWith("trtc://")) {
                    UserModel userModel = ProfileManager.getInstance().getUserModel();
                    String userId = userModel.userId;
                    playUrl = mScanPlayURL + "&sdkappid=" + GenerateTestUserSig.SDKAPPID + "&userId=" + userId + "&usersig=" + userModel.userSig;
                }

                startPlay(playUrl, mRemoteRenderViewList.get(position));
            } else if (requestCode == ACTIVITY_PUSHER_REQUEST_CODE) {
                mScanPlayURL = data.getStringExtra(AVSettingConfig.INTENT_SCAN_RESULT);

                if (mPushChooseTypeFragmentDialog != null) {
                    mPushChooseTypeFragmentDialog.setCDNPusherURL(mScanPlayURL);
                }

                // 清理其他
                mPushROOMRoomId = null;
                mPushROOMUserId = null;
                mPushTRTCStreamId = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasStopPusher && mLivePusher != null) {
            mLivePusher.startCamera(mIsFrontCamera);
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsPushFullScreen) {
            adjustPushViewLayout();
        } else if (mIsPlayFullScreen) {
            adjustPlayViewLayout();
        } else {
            finish();
        }
    }

    private void adjustPushViewLayout() {
        findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
        for (PlayerViewContainer container : mRemoteRenderViewList) {
            container.playerView.setVisibility(View.VISIBLE);
        }
        ((TextView) findViewById(R.id.livepusher_title_textview)).setText("V2推拉流");
        findViewById(R.id.livepusher_ibtn_qrcode).setVisibility(View.GONE);
        mPushRenderView.hideExtraInfoView();
        mIsPushFullScreen = false;
        mPushRenderView.hideAudioEffectPanel();
        mPushRenderView.hideBeautyPanel();
        mPushRenderView.showFullScreenView();
        if (mLivePusher != null) {
            mLivePusher.showDebugView(false);
        }
        if (mIsPusherStart) {
            mPushRenderView.showCloseButton();
        } else {
            mPushRenderView.hideControlLayout();
        }
    }

    private void adjustPlayViewLayout() {
        int position = 0;
        for (int i = 0; i < mRemoteRenderViewList.size(); i++) {
            if (mRemoteRenderViewList.get(i).equals(mCurrentPlayViewContainer)) {
                position = i;
            }
        }
        Log.d(TAG, "[Player] onFullScreenChange position " + position + ", mIsPlayFullScreen " + mIsPlayFullScreen);
        if (position == 0) {
            mPushRenderView.setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
        } else if (position == 1) {
            findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
            findViewById(R.id.live_render_user_4).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
        } else if (position == 2) {
            findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
            findViewById(R.id.live_render_user_3).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
        } else if (position == 3) {
            findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
            findViewById(R.id.live_render_user_6).setVisibility(View.VISIBLE);
        } else if (position == 4) {
            findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
            findViewById(R.id.live_render_user_5).setVisibility(View.VISIBLE);
        }
        ((TextView) findViewById(R.id.livepusher_title_textview)).setText("V2推拉流");
        findViewById(R.id.livepusher_ibtn_qrcode).setVisibility(View.GONE);
        mCurrentPlayViewContainer.playerView.hideExtraInfoView();

        if (mCurrentPlayViewContainer.isPlaying) {
            mCurrentPlayViewContainer.playerView.showFullScreenView();
            mCurrentPlayViewContainer.playerView.showCloseButton();
        } else {
            mCurrentPlayViewContainer.playerView.hideFullScreenView();
            mCurrentPlayViewContainer.playerView.hideCloseButton();
            mCurrentPlayViewContainer.playerView.hideControlLayout();
        }
        if (mCurrentPlayViewContainer.livePlayer != null) {
            mCurrentPlayViewContainer.livePlayer.showDebugView(false);
        }
        mIsPlayFullScreen = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPushRenderView != null) {
            mPushRenderView.destroyAudioEffect();
        }
        resetRenderView(mPushRenderView);
        for (PlayerViewContainer container : mRemoteRenderViewList) {
            resetRenderView(container.playerView);
            container.isPlaying = false;
        }

        resetPusher(mLivePusher, mPushRenderView);
        mHasStopPusher = false;
        for (V2TXLivePlayer player : AVSettingConfig.getInstance().playerMap.values()) {
            player.stopPlay();
        }
        // 销毁 Pusher 实例
        AVSettingConfig.getInstance().pusherInstance = null;
        AVSettingConfig.getInstance().playerURLList.clear();
        AVSettingConfig.getInstance().playerMap.clear();
        AVSettingConfig.getInstance().playerViewScanMap.clear();
        mHandler.removeCallbacksAndMessages(null);
        Log.i(TAG, "onDestroy ");
    }

    private void initView() {
        if (mPusherPlayQRCodeFragment == null) {
            mPusherPlayQRCodeFragment = new QRCodeGenerateFragment();
        }
        mPushRenderView = (MainItemRenderView) findViewById(R.id.live_render_user_1);
        PlayerViewContainer container1 = new PlayerViewContainer();
        container1.playerView = (MainItemRenderView) findViewById(R.id.live_render_user_2);
        PlayerViewContainer container2 = new PlayerViewContainer();
        container2.playerView = (MainItemRenderView) findViewById(R.id.live_render_user_3);
        PlayerViewContainer container3 = new PlayerViewContainer();
        container3.playerView = (MainItemRenderView) findViewById(R.id.live_render_user_4);
        PlayerViewContainer container4 = new PlayerViewContainer();
        container4.playerView = (MainItemRenderView) findViewById(R.id.live_render_user_5);
        PlayerViewContainer container5 = new PlayerViewContainer();
        container5.playerView = (MainItemRenderView) findViewById(R.id.live_render_user_6);

        mRemoteRenderViewList.add(container1);
        mRemoteRenderViewList.add(container2);
        mRemoteRenderViewList.add(container3);
        mRemoteRenderViewList.add(container4);
        mRemoteRenderViewList.add(container5);

        for (PlayerViewContainer container : mRemoteRenderViewList) {
            container.playerView.setTag("");
            container.playerView.setRenderTextTips("Player");
            container.playerView.hideDebugView();
            container.playerView.hideControlLayout();
        }
        mPushRenderView.hideControlLayout();
        mPushRenderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //实现数组的移位操作，点击一次，左移一位，末尾补上当前开机时间（cpu的时间）
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                //双击事件的时间间隔500ms
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    handlePusherFullScreenChange();
                    return;
                }
                if (mHasInitPusher) {
                    return;
                }

                if (mPushChooseTypeFragmentDialog != null) {
                    mPushChooseTypeFragmentDialog.dismissAllowingStateLoss();
                }
                mPushChooseTypeFragmentDialog = new MainProtocolSelectDialog();
                mPushChooseTypeFragmentDialog.setOnDismissListener(new MainProtocolSelectDialog.OnFragmentClickListener() {
                    @Override
                    public void onDismiss(boolean isCancel) {
                        showDialogFragment(mPushChooseTypeFragmentDialog, "ProtocolTypeEntrance");
                    }

                    @Override
                    public void onCDNEditTextChange(EditText editText) {
                        mPushCDNEditTextUrl = editText;

                        // 清理其他
                        mPushROOMRoomId = null;
                        mPushROOMUserId = null;
                        mPushTRTCStreamId = null;
                    }

                    @Override
                    public void onROOMEditTextChange(EditText editRoomId, EditText editUserId) {
                        mPushROOMRoomId = editRoomId;
                        mPushROOMUserId = editUserId;

                        // 清理其他
                        mPushTRTCStreamId = null;
                        mPushCDNEditTextUrl = null;
                    }

                    @Override
                    public void onTRTCEditTextChange(EditText editText) {
                        mPushTRTCStreamId = editText;

                        // 清理其他
                        mPushROOMRoomId = null;
                        mPushROOMUserId = null;
                        mPushCDNEditTextUrl = null;
                    }

                    @Override
                    public void onStart(boolean isLinkMic) {
                        Log.i(TAG, "[Pusher] onStart");
                        mVideoSource = isLinkMic ? V2VideoSource.SCREEN : V2VideoSource.CAMERA;
                        if (mPushCDNEditTextUrl != null) {
                            String pushURL = mPushCDNEditTextUrl.getText().toString().trim();
                            Log.i(TAG, "[Pusher]  onStart CDNPushURL url: " + pushURL);
                            AVSettingConfig.getInstance().roomPushURL = pushURL;
                        }
                        UserModel userModel = ProfileManager.getInstance().getUserModel();
                        // TRTC push
                        if (mPushTRTCStreamId != null) {
                            String streamId = mPushTRTCStreamId.getText().toString().trim();
                            mPusherRoomId = streamId;
                            if (TextUtils.isEmpty(streamId)) {
                                Toast.makeText(V2MainActivity.this, "请输入一个streamId。", Toast.LENGTH_LONG).show();
                                mPushTRTCStreamId = null;
                                return;
                            }
                            mPusherUserId = userModel.userId;
                            // 拼装 TRTC 下 push 协议
                            String trtcPushURL = "trtc://cloud.tencent.com/push/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID + "&userId=" + mPusherUserId + "&usersig=" + userModel.userSig;
                            Log.i(TAG, "[Pusher] onStart parse trtcPushURL url: " + trtcPushURL);

                            // 设置二维码 URL
                            mQRCodePusherURL = "trtc://cloud.tencent.com/play/" + streamId;
                            AVSettingConfig.getInstance().roomPushURL = trtcPushURL;
                            mIsRoomProtocol = false;
                        }
                        // ROOM push
                        if (mPushROOMRoomId != null && mPushROOMUserId != null) {
                            if (TextUtils.isEmpty(mPushROOMRoomId.getText().toString()) || TextUtils.isEmpty(mPushROOMUserId.getText().toString())) {
                                Toast.makeText(V2MainActivity.this, "roomId, userId不能为空。", Toast.LENGTH_LONG).show();
                                mPushROOMRoomId = null;
                                mPushROOMUserId = null;
                                return;
                            }
                            mPusherRoomId = mPushROOMRoomId.getText().toString().trim();
                            mPusherUserId = userModel.userId;
                            // 拼装 ROOM 下 push 协议
                            String roomPushURL = "room://cloud.tencent.com/rtc?sdkappid=" + GenerateTestUserSig.SDKAPPID + "&strroomid=" + mPusherRoomId + "&userId=" + mPusherUserId + "&usersig=" + userModel.userSig;

                            // 设置二维码 URL
                            mQRCodePusherURL = "room://cloud.tencent.com/rtc?strroomid=" + mPusherRoomId + "&remoteuserid=" + mPusherUserId;
                            AVSettingConfig.getInstance().roomPushURL = roomPushURL;
                            mIsRoomProtocol = true;
                        }
                        mPushRenderView.hideAddIcon();
                        startPush();
                        showDialogFragment(mPushChooseTypeFragmentDialog, "ProtocolTypeEntrance");
                    }

                    @Override
                    public void onAutoFetchUrl() {
                        if (mPushCDNEditTextUrl != null) {
                            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(10, TimeUnit.SECONDS)
                                    .build();
                            Request request = new Request.Builder()
                                    .url(URL_FETCH_PUSH_URL)
                                    .addHeader("Content-Type", "application/json; charset=utf-8")
                                    .build();
                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        try {
                                            JSONObject jsonRsp = new JSONObject(response.body().string());
                                            final String pusherURLDefault = jsonRsp.optString("url_push");
                                            // 二维码 URL
                                            mQRCodePusherURL = jsonRsp.optString("url_play_flv");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mPushCDNEditTextUrl.setText(pusherURLDefault);
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onScanQRCode() {
                        if (mLivePusher != null) {
                            mLivePusher.stopCamera();
                        }
                        mHasStopPusher = true;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(V2MainActivity.this, QRCodeScanActivity.class);
                                startActivityForResult(intent, ACTIVITY_PUSHER_REQUEST_CODE);
                            }
                        }, 200);
                    }
                });
                mPushChooseTypeFragmentDialog.setIsPlay(false);
                showDialogFragment(mPushChooseTypeFragmentDialog, "ProtocolTypeEntrance");
            }
        });
        mPushRenderView.hideDebugView();
        mPushRenderView.hideExtraInfoView();
        mPushRenderView.setRenderTextTips("Pusher");
        mPushRenderView.setSwitchListener(new PushViewCallback());
    }

    private void startPlayChooseProtocolType(final int positionView) {
        //实现数组的移位操作，点击一次，左移一位，末尾补上当前开机时间（cpu的时间）
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        //双击事件的时间间隔500ms
        if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
            handlePlayerFullScreenChange(mRemoteRenderViewList.get(positionView));
            return;
        }
        final PlayerViewContainer playerViewContainer = mRemoteRenderViewList.get(positionView);
        final MainItemRenderView renderView = mRemoteRenderViewList.get(positionView).playerView;
        if (renderView == null || !TextUtils.isEmpty((String) renderView.getTag())) {
            Log.w(TAG, "[Player] renderView.getTag " + renderView.getTag());
            return;
        }
        Log.w(TAG, "[Player] startPlayChooseProtocolType: " + positionView);
        if (mPlayChooseTypeFragmentDialog != null) {
            mPlayChooseTypeFragmentDialog.dismissAllowingStateLoss();
        }
        mPlayChooseTypeFragmentDialog = new MainProtocolSelectDialog();
        mPlayChooseTypeFragmentDialog.setOnDismissListener(new MainProtocolSelectDialog.OnFragmentClickListener() {
            @Override
            public void onDismiss(boolean isCancel) {
                if (!isCancel) {
                    if (mLivePusher != null) {
                        mLivePusher.stopCamera();
                    }
                    mHasStopPusher = true;
                }
                showDialogFragment(mPlayChooseTypeFragmentDialog, "ProtocolTypePlayEntrance");
            }

            @Override
            public void onCDNEditTextChange(EditText editText) {
                mPlayCNDURL = editText;
                // 清理其他
                mPlayROOMRoomId = null;
                mPlayROOMUserId = null;
                mPlayTRTCStreamId = null;
            }

            @Override
            public void onROOMEditTextChange(EditText editRoomId, EditText editUserId) {
                mPlayROOMRoomId = editRoomId;
                mPlayROOMUserId = editUserId;
                // 清理其他
                mPlayCNDURL = null;
                mPlayTRTCStreamId = null;
            }

            @Override
            public void onTRTCEditTextChange(EditText editText) {
                mPlayTRTCStreamId = editText;
                // 清理其他
                mPlayCNDURL = null;
                mPlayROOMRoomId = null;
                mPlayROOMUserId = null;
            }

            @Override
            public void onStart(boolean isLinkMic) {
                Log.i(TAG, "[Player] onStart");
                if (mPlayCNDURL != null) {
                    String url = mPlayCNDURL.getText().toString().trim();
                    Log.i(TAG, "[Player] onStart CDNPlayURL url: " + url);
                    startPlay(url, playerViewContainer);
                }
                UserModel userModel = ProfileManager.getInstance().getUserModel();
                // TRTC streamid
                if (mPlayTRTCStreamId != null) {
                    String streamId = mPlayTRTCStreamId.getText().toString().trim();
                    if (TextUtils.isEmpty(streamId)) {
                        Toast.makeText(V2MainActivity.this, "请输入一个streamId。", Toast.LENGTH_LONG).show();
                        mPlayTRTCStreamId = null;
                        return;
                    }

                    String simpleURL = "trtc://cloud.tencent.com/play/" + streamId;
                    if (AVSettingConfig.getInstance().playerViewScanMap.get(simpleURL) != null) {
                        Toast.makeText(V2MainActivity.this, "重复的streamId，请换一个streamId。", Toast.LENGTH_LONG).show();
                        mPlayTRTCStreamId = null;
                        return;
                    }
                    mScanPlayURL = simpleURL;
                    String userId = userModel.userId;
                    // 拼装 TRTC 下 play 协议
                    String trtcPlayURL = "trtc://cloud.tencent.com/play/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID + "&userId=" + userId + "&usersig=" + userModel.userSig;
                    Log.i(TAG, "[Player] onStart url: " + trtcPlayURL);
                    startPlay(trtcPlayURL, playerViewContainer);
                }
                // ROOM roomid/userid
                if (mPlayROOMRoomId != null && mPlayROOMUserId != null) {
                    String roomId = mPlayROOMRoomId.getText().toString().trim();
                    String remoteUserId = mPlayROOMUserId.getText().toString().trim();
                    if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(remoteUserId)) {
                        Toast.makeText(V2MainActivity.this, "roomId, userId不能为空。", Toast.LENGTH_LONG).show();
                        mPlayROOMRoomId = null;
                        mPlayROOMUserId = null;
                        return;
                    }

                    String simpleURL = "room://cloud.tencent.com/rtc?strroomid=" + roomId + "&remoteuserid=" + remoteUserId;
                    if (AVSettingConfig.getInstance().playerViewScanMap.get(simpleURL) != null) {
                        Toast.makeText(V2MainActivity.this, "roomId, userId重复。", Toast.LENGTH_LONG).show();
                        mPlayROOMRoomId = null;
                        mPlayROOMUserId = null;
                        return;
                    }
                    mScanPlayURL = simpleURL;

                    String userId = userModel.userId;
                    // 拼装 ROOM 下 play 协议
                    String roomPushURL = "room://cloud.tencent.com/rtc?sdkappid=" + GenerateTestUserSig.SDKAPPID + "&strroomid=" + roomId + "&remoteuserid=" + remoteUserId + "&userId=" + userId + "&usersig=" + userModel.userSig;
                    Log.i(TAG, "[Player] onStart roomPlayURL url: " + roomPushURL);
                    startPlay(roomPushURL, playerViewContainer);
                }
                showDialogFragment(mPlayChooseTypeFragmentDialog, "ProtocolTypePlayEntrance");
            }

            @Override
            public void onAutoFetchUrl() {
                if (mPlayCNDURL != null) {
                    mPlayCNDURL.setText(NORMAL_PLAY_URL);
                }
            }

            @Override
            public void onScanQRCode() {
                if (mLivePusher != null) {
                    mLivePusher.stopCamera();
                }
                mHasStopPusher = true;
                showDialogFragment(mPlayChooseTypeFragmentDialog, "ProtocolTypePlayEntrance");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(V2MainActivity.this, QRCodeScanActivity.class);
                        intent.putExtra("position", positionView);
                        startActivityForResult(intent, ACTIVITY_PLAYER_REQUEST_CODE);
                    }
                }, 200);
            }
        });
        mPlayChooseTypeFragmentDialog.setIsPlay(true);
        showDialogFragment(mPlayChooseTypeFragmentDialog, "ProtocolTypePlayEntrance");
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.livepusher_ibtn_back) {
            if (mIsPushFullScreen) {
                adjustPushViewLayout();
            } else if (mIsPlayFullScreen) {
                adjustPlayViewLayout();
            } else {
                finish();
            }
        } else if (id == R.id.livepusher_ibtn_qrcode) {
            if (mHasInitPusher) {
                mPusherPlayQRCodeFragment.setQRCodeURL(mQRCodePusherURL);
                mPusherPlayQRCodeFragment.toggle(getFragmentManager(), PUSHER_PLAY_QR_CODE_FRAGMENT);
            } else {
                Toast.makeText(this, "推流尚未启动！请启动推流！", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.live_render_user_2) {
            startPlayChooseProtocolType(0);
        } else if (id == R.id.live_render_user_3) {
            startPlayChooseProtocolType(1);
        } else if (id == R.id.live_render_user_4) {
            startPlayChooseProtocolType(2);
        } else if (id == R.id.live_render_user_5) {
            startPlayChooseProtocolType(3);
        } else if (id == R.id.live_render_user_6) {
            startPlayChooseProtocolType(4);
        }
    }

    /**
     * 展示dialog界面
     */
    private void showDialogFragment(DialogFragment dialogFragment, String tag) {
        if (dialogFragment != null) {
            if (dialogFragment.isVisible()) {
                try {
                    dialogFragment.dismissAllowingStateLoss();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                dialogFragment.show(getSupportFragmentManager(), tag);
            }
        }
    }

    private void startPush() {
        String pushURL = AVSettingConfig.getInstance().roomPushURL;
        if (TextUtils.isEmpty(pushURL)) {
            Toast.makeText(this, "推流失败！", Toast.LENGTH_LONG).show();
            resetPusher(null, mPushRenderView);
            Log.w(TAG, "[Pusher] startPush failed, push url is empty! ");
            return;
        }
        mIsPusherStart = false;
        mLivePusher = new V2TXLivePusherImpl(this, TXLiveUtils.parseLiveMode(pushURL));

        mLivePusher.setObserver(new MyPusherObserver());
        AVSettingConfig.getInstance().pusherInstance = mLivePusher;

        if (mVideoSource == V2VideoSource.CAMERA) {
            // 设置本地预览View
            mLivePusher.setRenderView(mPushRenderView.getCloudView());
            mLivePusher.startCamera(true);
            mLivePusher.getDeviceManager().switchCamera(true);
            mLivePusher.getDeviceManager().enableCameraAutoFocus(true);
            mLivePusher.getDeviceManager().setCameraZoomRatio(1);
            mLivePusher.getDeviceManager().enableCameraTorch(false);
        } else {
            mLivePusher.startScreenCapture();
        }

        // 音频相关
        mLivePusher.setRenderMirror(V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeAuto);
        mLivePusher.setVideoQuality(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540, V2TXLiveVideoResolutionModePortrait);
        mLivePusher.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0);
        mLivePusher.setEncoderMirror(false);

        // 音频相关
        mLivePusher.setAudioQuality(V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault);

        mLivePusher.getDeviceManager().setSystemVolumeType(TXDeviceManager.TXSystemVolumeType.TXSystemVolumeTypeAuto);
        mLivePusher.getDeviceManager().setAudioRoute(TXDeviceManager.TXAudioRoute.TXAudioRouteSpeakerphone);

        mLivePusher.getAudioEffectManager().setVoiceCaptureVolume(100);
        mLivePusher.startMicrophone();
        final V2TXLivePusher pusher = mLivePusher;
        // 开始推流
        int result = mLivePusher.startPush(AVSettingConfig.getInstance().roomPushURL);
        if (result != 0) {
            if (result == V2TXLiveCode.V2TXLIVE_ERROR_REFUSED) {
                Toast.makeText(V2MainActivity.this, "推流失败：抱歉，RTC暂不支持同一台设备使用相同streamid同时推拉流", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(V2MainActivity.this, "推流失败！", Toast.LENGTH_LONG).show();
            }
            Log.w(TAG, "[Pusher] startPush failed, result " + result);
            resetPusher(pusher, mPushRenderView);
            return;
        }

        mPushRenderView.setAudioEffectManager(mLivePusher.getAudioEffectManager());
        mPushRenderView.showControlLayout();
        mPushRenderView.showCloseButton();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsPusherStart) {
                    Toast.makeText(V2MainActivity.this, "推流失败！", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "[Pusher] pusher failed, timeout to receive local first video");
                    resetPusher(pusher, mPushRenderView);
                }
            }
        }, PLAY_ERROR_TIMEOUT); // 5s内没有收到本地首帧视频采集或者音频采集到，认为推流异常
        mHasInitPusher = true;
    }

    private void stopPush() {
        Log.i(TAG, "[Pusher] stopPush " + mLivePusher);
        if (mLivePusher != null) {
            mLivePusher.showDebugView(false);
            // 释放资源
            mLivePusher.stopMicrophone();
            mLivePusher.stopCamera();
            mLivePusher.stopScreenCapture();

            mLivePusher.stopPush();
            mLivePusher = null;
        }
        mHasInitPusher = false;
        mIsPusherStart = false;
        mIsMuteAudio = false;
        mIsMuteVideo = false;
        mIsShowPusherDebugView = false;
        mIsFrontCamera = true;
        AVSettingConfig.getInstance().roomPushURL = null;
    }

    private void startPlay(String url, final PlayerViewContainer container) {
        final MainItemRenderView playerView = container.playerView;
        if (TextUtils.isEmpty(url) || playerView == null) {
            Toast.makeText(V2MainActivity.this, "URL为空！", Toast.LENGTH_SHORT).show();
            resetPlayer(container);
            container.isPlaying = false;
            container.isShowDebugView = false;
            return;
        }
        boolean isURLInvalid = url.startsWith("room://") || url.startsWith("trtc://") || url.startsWith("http://") || url.startsWith("rtmp://") || url.startsWith("https://");
        if (!isURLInvalid) {
            Toast.makeText(V2MainActivity.this, "无效的URL！", Toast.LENGTH_SHORT).show();
            resetPlayer(container);
            container.isPlaying = false;
            container.isShowDebugView = false;
            return;
        }
        if (url.startsWith("trtc://")) {
            String[] strArrays = url.split("[?&]");
            if (strArrays.length > 2) {
                String[] subItem = strArrays[0].split("/");
                String streamId = subItem[subItem.length - 1];
                container.textTitle = streamId;
            }
        } else {
            String textTitle = "";
            String[] strArrays = url.split("[?&]");
            for (String strItem : strArrays) {
                if (strItem.indexOf("=") != -1) {
                    String[] array = strItem.split("[=]");
                    if (array.length == 2) {
                        String key = array[0];
                        String val = array[1];
                        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(val)) {
                            if (key.equalsIgnoreCase("strroomid")) {
                                textTitle += val;
                            } else if (key.equalsIgnoreCase("remoteuserid")) {
                                textTitle += "_" + val;
                            }
                        }
                    }
                }
            }
            container.textTitle = textTitle;
        }

        Log.i(TAG, "[Player] startPlay url " + url);
        final V2TXLivePlayer player = new V2TXLivePlayerImpl(V2MainActivity.this);

        playerView.hideAddIcon();
        playerView.showControlLayout();
        playerView.hideExtraInfoView();
        playerView.hidePushFeatureView();
        playerView.setSwitchListener(new PlayerViewCallback(container));

        player.setRenderView(playerView.getCloudView());
        player.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0);
        player.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);
        player.setPlayoutVolume(100);
        AVSettingConfig.getInstance().playerMap.put(url, player);
        player.setObserver(new MyPlayerObserver(container));
        final int result = player.startPlay(url);
        if (result != 0) {
            if (result == V2TXLiveCode.V2TXLIVE_ERROR_REFUSED) {
                Toast.makeText(V2MainActivity.this, "拉流失败：抱歉，RTC暂不支持同一台设备使用相同streamid同时推拉流", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(V2MainActivity.this, "拉流失败！", Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "[Player] startPlay failed, result " + result);
            resetPlayer(container);
            return;
        }
        AVSettingConfig.getInstance().playerURLList.add(url);
        AVSettingConfig.getInstance().playerViewScanMap.put(mScanPlayURL, playerView);
        container.isPlaying = false;
        container.playURL = url;
        container.livePlayer = player;

        playerView.setTag(url);
        playerView.showCloseButton();
        startPlayerLoading(container, player);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!container.isPlaying) {
                    Toast.makeText(V2MainActivity.this, "拉流失败！", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "[Player] play error, timeout to receive first video");
                    resetPlayer(container);
                    container.isPlaying = false;
                    container.isShowDebugView = false;
                }
            }
        }, PLAY_ERROR_TIMEOUT); // 5s内没有收到首帧视频或者音频，认为播放异常
    }

    private class MyPusherObserver extends V2TXLivePusherObserver {
        @Override
        public void onWarning(int code, String msg, Bundle extraInfo) {
            Log.w(TAG, "[Pusher] onWarning errorCode: " + code + ", msg " + msg);
        }

        @Override
        public void onError(int code, String msg, Bundle extraInfo) {
            Log.e(TAG, "[Pusher] onError: " + msg + ", extraInfo " + extraInfo);
            mIsPusherStart = false;
            ErrorDialog.showMsgDialog(V2MainActivity.this, "onError errorCode: " + code);
        }

        @Override
        public void onCaptureFirstAudioFrame() {
            Log.i(TAG, "[Pusher] onCaptureFirstAudioFrame");
            mIsPusherStart = true;
            mPushRenderView.recvFirstAudio(true);
        }

        @Override
        public void onCaptureFirstVideoFrame() {
            Log.i(TAG, "[Pusher] onCaptureFirstVideoFrame");
            mIsPusherStart = true;
            mPushRenderView.recvFirstVideo(true);
        }

        @Override
        public void onMicrophoneVolumeUpdate(int volume) {
            mPushRenderView.setVolumeProgress(volume);
        }

        @Override
        public void onPushStatusUpdate(V2TXLiveDef.V2TXLivePushStatus status, String msg, Bundle bundle) {
            if (status == V2TXLiveDef.V2TXLivePushStatus.V2TXLivePushStatusConnecting || status == V2TXLiveDef.V2TXLivePushStatus.V2TXLivePushStatusReconnecting) {
                mPushRenderView.showLoading();
            } else {
                mPushRenderView.dismissLoading();
            }
        }

        @Override
        public void onSnapshotComplete(Bitmap bitmap) {
            mPushSettingFragmentDialog.setSnapshotImage(bitmap);
        }

        @Override
        public void onStatisticsUpdate(V2TXLiveDef.V2TXLivePusherStatistics statistics) {
//            Log.i(TAG, "[Pusher] onStatisticsUpdate: statistics cpu-" + statistics.appCpu
//                    + " syscpu-" + statistics.systemCpu
//                    + " width-" + statistics.width
//                    + " height-" + statistics.height
//                    + " fps-" + statistics.fps
//                    + " video bitrate-" + statistics.videoBitrate
//                    + " audio bitrate-" + statistics.audioBitrate
//            );
        }
    }

    // player
    private class MyPlayerObserver extends V2TXLivePlayerObserver {

        private PlayerViewContainer mPlayerContainer;

        public MyPlayerObserver(PlayerViewContainer view) {
            mPlayerContainer = view;
        }

        @Override
        public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            Log.w(TAG, "[Player] onWarning: player-" + player + " code-" + code + " msg-" + msg + " info-" + extraInfo);
        }

        @Override
        public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extraInfo);
            ErrorDialog.showMsgDialog(V2MainActivity.this, "onError errorCode: " + code);
        }

        @Override
        public void onSnapshotComplete(V2TXLivePlayer v2TXLivePlayer, Bitmap bitmap) {
            if (mPlaySetting != null) {
                mPlaySetting.setSnapshotImage(bitmap);
            }
        }

        @Override
        public void onVideoPlayStatusUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayStatus status, V2TXLiveDef.V2TXLiveStatusChangeReason reason, Bundle bundle) {
            Log.i(TAG, "[Player] onVideoPlayStatusUpdate: player-" + player + ", status-" + status + ", reason-" + reason);
            switch (status) {
                case V2TXLivePlayStatusPlaying:
                    mPlayerContainer.isPlaying = true;
                    stopPlayerLoading(mPlayerContainer, player);
                    if(reason == V2TXLiveDef.V2TXLiveStatusChangeReason.V2TXLiveStatusChangeReasonLocalStarted) {
                        MainItemRenderView renderView = mPlayerContainer.playerView;
                        for (String url : AVSettingConfig.getInstance().playerMap.keySet()) {
                            if (AVSettingConfig.getInstance().playerMap.get(url) == player) {
                                if (renderView != null) {
                                    renderView.recvFirstVideo(true);
                                }
                                return;
                            }
                        }
                    }
                    break;
                case V2TXLivePlayStatusLoading:
                    mPlayerContainer.isPlaying = false;
                    startPlayerLoading(mPlayerContainer, player);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onAudioPlayStatusUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayStatus status, V2TXLiveDef.V2TXLiveStatusChangeReason reason, Bundle bundle) {
            Log.i(TAG, "[Player] onAudioPlayStatusUpdate: player-" + player + ", status-" + status + ", reason-" + reason);
            switch (status) {
                case V2TXLivePlayStatusPlaying:
                    mPlayerContainer.isPlaying = true;
                    stopPlayerLoading(mPlayerContainer, player);
                    if (reason == V2TXLiveDef.V2TXLiveStatusChangeReason.V2TXLiveStatusChangeReasonLocalStarted) {
                        MainItemRenderView renderView = mPlayerContainer.playerView;
                        for (String url : AVSettingConfig.getInstance().playerMap.keySet()) {
                            if (AVSettingConfig.getInstance().playerMap.get(url) == player) {
                                if (renderView != null) {
                                    renderView.recvFirstAudio(true);
                                }
                                return;
                            }
                        }
                    }
                    break;
                case V2TXLivePlayStatusLoading:
                    mPlayerContainer.isPlaying = false;
                    startPlayerLoading(mPlayerContainer, player);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPlayoutVolumeUpdate(V2TXLivePlayer player, int volume) {
//            Log.i(TAG, "onPlayoutVolumeUpdate: player-" + player +  ", volume-" + volume);
            MainItemRenderView renderView = mPlayerContainer.playerView;
            for (String url : AVSettingConfig.getInstance().playerMap.keySet()) {
                if (AVSettingConfig.getInstance().playerMap.get(url) == player) {
                    if (renderView != null) {
                        renderView.setVolumeProgress(volume);
                    }
                    return;
                }
            }
        }

        @Override
        public void onStatisticsUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayerStatistics statistics) {
            if (!AVSettingConfig.getInstance().enableVolumeCallback) {
                if (mPlayerContainer != null) {
                    mPlayerContainer.playerView.setVolumeProgress(0);
                }
            }
//            Log.i(TAG, "[Player] onStatisticsUpdate: statistics cpu-" + statistics.appCpu
//                    + " syscpu-" + statistics.systemCpu
//                    + " width-" + statistics.width
//                    + " height-" + statistics.height
//                    + " fps-" + statistics.fps
//                    + " video bitrate-" + statistics.videoBitrate
//                    + " audio bitrate-" + statistics.audioBitrate
//            );
        }

    }

    private void startPlayerLoading(PlayerViewContainer playerViewContainer, V2TXLivePlayer player) {
        MainItemRenderView view = null;
        for (String url : AVSettingConfig.getInstance().playerMap.keySet()) {
            if (AVSettingConfig.getInstance().playerMap.get(url) == player) {
                view = playerViewContainer.playerView;
                break;
            }
        }
        if (view != null) {
            view.showLoading();
        }
    }

    private void stopPlayerLoading(PlayerViewContainer playerViewContainer, V2TXLivePlayer player) {
        MainItemRenderView view = null;
        for (String url : AVSettingConfig.getInstance().playerMap.keySet()) {
            if (AVSettingConfig.getInstance().playerMap.get(url) == player) {
                view = playerViewContainer.playerView;
                break;
            }
        }
        if (view != null) {
            view.dismissLoading();
        }
    }

    private class PlayerViewCallback implements MainItemRenderView.ILiveRenderViewSwitchCallback {
        private PlayerViewContainer mPlayViewContainer;

        public PlayerViewCallback(PlayerViewContainer playViewContainer) {
            mPlayViewContainer = playViewContainer;
        }

        @Override
        public void onSwitchView(View view) {
        }

        @Override
        public void onClickSnapshot() {
        }

        @Override
        public void onRestart() {
        }

        @Override
        public void onCameraChange(View view) {
        }

        @Override
        public void onMuteVideo(View view) {
            if (!mPlayViewContainer.isMuteVideo) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_off);
                mPlayViewContainer.isMuteVideo = true;
                mPlayViewContainer.livePlayer.pauseVideo();
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_on);
                mPlayViewContainer.isMuteVideo = false;
                mPlayViewContainer.livePlayer.resumeVideo();
            }
        }

        @Override
        public void onMuteAudio(View view) {
            if (!mPlayViewContainer.isMuteAudio) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_off);
                mPlayViewContainer.isMuteAudio = true;
                mPlayViewContainer.livePlayer.pauseAudio();
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_on);
                mPlayViewContainer.isMuteAudio = false;
                mPlayViewContainer.livePlayer.resumeAudio();
            }
        }

        @Override
        public void onFullScreenChange(View view) {
            handlePlayerFullScreenChange(mPlayViewContainer);
        }

        @Override
        public void onShowSetting() {
            mPlaySetting = new PlaySetting();
            mPlaySetting.setLivePlayer(mPlayViewContainer.livePlayer);
            mPlaySetting.show(getFragmentManager(), "remote_config_fragment");
        }

        @Override
        public void onShowDebugView(View view) {
            if (!mPlayViewContainer.isShowDebugView) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_show);
                mPlayViewContainer.livePlayer.showDebugView(true);
                mPlayViewContainer.isShowDebugView = true;
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_hidden);
                mPlayViewContainer.livePlayer.showDebugView(false);
                mPlayViewContainer.isShowDebugView = false;
            }
        }

        @Override
        public void onShowBeautyPanel(View view) {
            // undo
        }

        @Override
        public void onShowBGMPanel(View view) {
            // undo
        }

        @Override
        public void onStart(View view) {
            if (!mPlayViewContainer.isPlaying) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_stop);
                mPlayViewContainer.livePlayer.startPlay(mPlayViewContainer.playURL);
                mPlayViewContainer.isPlaying = true;
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_start);
                handlePlayerFullScreenChange(mPlayViewContainer);
                resetPlayer(mPlayViewContainer);
                mPlayViewContainer.isPlaying = false;
                mPlayViewContainer.isShowDebugView = false;
            }
        }

        @Override
        public void onClose(View view) {
            if (mPlayViewContainer.isPlaying) {
                resetPlayer(mPlayViewContainer);
                mPlayViewContainer.isPlaying = false;
                mPlayViewContainer.isShowDebugView = false;
            }
        }
    }

    private class PushViewCallback implements MainItemRenderView.ILiveRenderViewSwitchCallback {

        @Override
        public void onSwitchView(View view) {
        }

        @Override
        public void onClickSnapshot() {

        }

        @Override
        public void onRestart() {
        }

        @Override
        public void onCameraChange(View view) {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            mIsFrontCamera = !mIsFrontCamera;
            ((ImageView) view).setImageResource(mIsFrontCamera ? R.drawable.live_link_mic_new_ic_bottom_camera_back : R.drawable.live_link_mic_new_ic_bottom_camera_front);
            mLivePusher.getDeviceManager().switchCamera(mIsFrontCamera);
        }

        @Override
        public void onMuteVideo(View view) {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            if (!mIsMuteVideo) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_off);
                mLivePusher.showDebugView(false);// debugview和stopCamera有绑定关系
                mLivePusher.stopCamera();
                mIsMuteVideo = true;
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_on);
                mLivePusher.startCamera(mIsFrontCamera);
                if (mIsShowPusherDebugView) {
                    mLivePusher.showDebugView(true);
                }
                mIsMuteVideo = false;
            }
        }

        @Override
        public void onMuteAudio(View view) {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            if (!mIsMuteAudio) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_off);
                mLivePusher.stopMicrophone();
                mIsMuteAudio = true;
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_on);
                mLivePusher.startMicrophone();
                mIsMuteAudio = false;
            }
        }

        @Override
        public void onFullScreenChange(View view) {
            handlePusherFullScreenChange();
        }

        @Override
        public void onShowSetting() {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            if (mPushSettingFragmentDialog != null) {
                mPushSettingFragmentDialog.dismissAllowingStateLoss();
            }
            mPushSettingFragmentDialog = new PushSetting();
            mPushSettingFragmentDialog.setLivePusher(mLivePusher);
            showDialogFragment(mPushSettingFragmentDialog, "PushSettingDialog");
        }

        @Override
        public void onShowDebugView(View view) {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            if (!mIsShowPusherDebugView) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_show);
                mLivePusher.showDebugView(true);
                mIsShowPusherDebugView = true;
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_hidden);
                mLivePusher.showDebugView(false);
                mIsShowPusherDebugView = false;
            }
        }

        @Override
        public void onShowBeautyPanel(View view) {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            mPushRenderView.showOrHideBeautyPanel(mLivePusher.getBeautyManager());
        }

        @Override
        public void onShowBGMPanel(View view) {
            if (mLivePusher == null) {
                Toast.makeText(V2MainActivity.this, "推流尚未开始！", Toast.LENGTH_LONG).show();
                return;
            }
            mPushRenderView.showOrHideAudioPanel();
        }

        @Override
        public void onStart(View view) {
            if (!mIsPusherStart) {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_stop);
                startPush();
            } else {
                ((ImageView) view).setImageResource(R.drawable.live_link_mic_new_ic_bottom_start);
                handlePusherFullScreenChange();
                resetPusher(mLivePusher, mPushRenderView);
            }
        }

        @Override
        public void onClose(View view) {
            if (mIsPusherStart) {
                resetPusher(mLivePusher, mPushRenderView);
            }
        }
    }

    private void handlePusherFullScreenChange() {
        Log.d(TAG, "[Pusher] onFullScreenChange mIsPushFullScreen " + mIsPushFullScreen);
        if (!mHasInitPusher) {
            Toast.makeText(V2MainActivity.this, "推流尚未启动!请启动推流！", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mIsPushFullScreen) {
            findViewById(R.id.ll_layout2).setVisibility(View.GONE);
            findViewById(R.id.ll_layout3).setVisibility(View.GONE);
            for (PlayerViewContainer container : mRemoteRenderViewList) {
                container.playerView.setVisibility(View.GONE);
            }
            if (TextUtils.isEmpty(mPusherRoomId)) {
                mPusherRoomId = "";
            }
            if (TextUtils.isEmpty(mPusherUserId)) {
                mPusherUserId = "";
            }
            String text = mIsRoomProtocol ? "V2推流(" + mPusherRoomId + "_" + mPusherUserId + ")" : "V2推流(" + mPusherRoomId + ")";
            ((TextView) findViewById(R.id.livepusher_title_textview)).setText(text);
            mPushRenderView.showExtraInfoView();
            findViewById(R.id.livepusher_ibtn_qrcode).setVisibility(View.VISIBLE);
            mPushRenderView.hideFullScreenView();
            mPushRenderView.hideCloseButton();
            if (mLivePusher != null && mIsShowPusherDebugView) {
                mLivePusher.showDebugView(true);
            }
            mIsPushFullScreen = true;
        } else {
            findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
            for (PlayerViewContainer container : mRemoteRenderViewList) {
                container.playerView.setVisibility(View.VISIBLE);
            }
            ((TextView) findViewById(R.id.livepusher_title_textview)).setText("V2推拉流");
            findViewById(R.id.livepusher_ibtn_qrcode).setVisibility(View.GONE);
            mPushRenderView.hideExtraInfoView();
            mPushRenderView.hideBeautyPanel();
            mPushRenderView.hideAudioEffectPanel();
            mPushRenderView.showFullScreenView();
            if (mIsPusherStart) {
                mPushRenderView.showCloseButton();
            } else {
                mPushRenderView.hideControlLayout();
            }
            if (mLivePusher != null) {
                mLivePusher.showDebugView(false);
            }
            mIsPushFullScreen = false;
        }
    }

    private void handlePlayerFullScreenChange(PlayerViewContainer playerViewContainer) {
        if (!playerViewContainer.isPlaying) {
            Toast.makeText(V2MainActivity.this, "拉流尚未启动!请启动拉流！", Toast.LENGTH_LONG).show();
            return;
        }
        int position = 0;
        if (!mIsPlayFullScreen) {
            for (int i = 0; i < mRemoteRenderViewList.size(); i++) {
                if (mRemoteRenderViewList.get(i).equals(playerViewContainer)) {
                    position = i;
                }
            }
            Log.d(TAG, "[Player] onFullScreenChange position " + position + ", mIsPlayFullScreen " + mIsPlayFullScreen);
            if (position == 0) {
                mPushRenderView.setVisibility(View.GONE);
                findViewById(R.id.ll_layout2).setVisibility(View.GONE);
                findViewById(R.id.ll_layout3).setVisibility(View.GONE);
            } else if (position == 1) {
                findViewById(R.id.ll_layout1).setVisibility(View.GONE);
                findViewById(R.id.live_render_user_4).setVisibility(View.GONE);
                findViewById(R.id.ll_layout3).setVisibility(View.GONE);
            } else if (position == 2) {
                findViewById(R.id.ll_layout1).setVisibility(View.GONE);
                findViewById(R.id.live_render_user_3).setVisibility(View.GONE);
                findViewById(R.id.ll_layout3).setVisibility(View.GONE);
            } else if (position == 3) {
                findViewById(R.id.ll_layout1).setVisibility(View.GONE);
                findViewById(R.id.ll_layout2).setVisibility(View.GONE);
                findViewById(R.id.live_render_user_6).setVisibility(View.GONE);
            } else if (position == 4) {
                findViewById(R.id.ll_layout1).setVisibility(View.GONE);
                findViewById(R.id.ll_layout2).setVisibility(View.GONE);
                findViewById(R.id.live_render_user_5).setVisibility(View.GONE);
            }
            String title = "V2拉流";
            if (!TextUtils.isEmpty(playerViewContainer.textTitle)) {
                title += "(" + playerViewContainer.textTitle + ")";
            }
            ((TextView) findViewById(R.id.livepusher_title_textview)).setText(title);
            playerViewContainer.playerView.showExtraInfoView();
            playerViewContainer.playerView.hidePushFeatureView();
            playerViewContainer.playerView.hideFullScreenView();
            playerViewContainer.playerView.hideCloseButton();
            mCurrentPlayViewContainer = playerViewContainer;
            if (playerViewContainer.isShowDebugView) {
                playerViewContainer.livePlayer.showDebugView(true);
            }
            mIsPlayFullScreen = true;
        } else {
            for (int i = 0; i < mRemoteRenderViewList.size(); i++) {
                if (mRemoteRenderViewList.get(i).equals(playerViewContainer)) {
                    position = i;
                }
            }
            Log.d(TAG, "[Player] onFullScreenChange position " + position + ", mIsPlayFullScreen " + mIsPlayFullScreen);
            if (position == 0) {
                mPushRenderView.setVisibility(View.VISIBLE);
                findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
            } else if (position == 1) {
                findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
                findViewById(R.id.live_render_user_4).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
            } else if (position == 2) {
                findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
                findViewById(R.id.live_render_user_3).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_layout3).setVisibility(View.VISIBLE);
            } else if (position == 3) {
                findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
                findViewById(R.id.live_render_user_6).setVisibility(View.VISIBLE);
            } else if (position == 4) {
                findViewById(R.id.ll_layout1).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_layout2).setVisibility(View.VISIBLE);
                findViewById(R.id.live_render_user_5).setVisibility(View.VISIBLE);
            }
            ((TextView) findViewById(R.id.livepusher_title_textview)).setText("V2推拉流");
            playerViewContainer.playerView.hideExtraInfoView();
            playerViewContainer.playerView.showFullScreenView();
            if (mCurrentPlayViewContainer.isPlaying) {
                mCurrentPlayViewContainer.playerView.showCloseButton();
            } else {
                mCurrentPlayViewContainer.playerView.hideCloseButton();
            }
            if (playerViewContainer.livePlayer != null) {
                playerViewContainer.livePlayer.showDebugView(false);
            }
            mIsPlayFullScreen = false;
        }
    }

    private void resetPlayer(PlayerViewContainer container) {
        Log.i(TAG, "[Player] resetPlayer: player-" + container);
        if (mScanPlayURL != null) {
            AVSettingConfig.getInstance().playerViewScanMap.remove(mScanPlayURL);
        }
        if (container == null) {
            Log.i(TAG, "[Player] resetPlayer: playerViewContainer is null");
            return;
        }
        if (container.livePlayer != null) {
            container.livePlayer.showDebugView(false);
            container.livePlayer.stopPlay();
            container.isPlaying = false;
            container.isMuteAudio = false;
            container.isMuteVideo = false;
            container.isShowDebugView = false;
        }
        if (container.playerView == null) {
            Log.i(TAG, "[Player] resetPlayer: playerView is null");
            return;
        }
        MainItemRenderView playerView = container.playerView;
        if (!container.isPlaying) {
            ((ImageView) playerView.getPlayButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_stop);
        } else {
            ((ImageView) playerView.getPlayButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_start);
        }
        if (!container.isShowDebugView) {
            ((ImageView) playerView.getLogButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_hidden);
        } else {
            ((ImageView) playerView.getLogButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_show);
        }
        if (!container.isMuteAudio) {
            ((ImageView) playerView.getMicButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_on);
        } else {
            ((ImageView) playerView.getMicButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_off);
        }
        if (!container.isMuteVideo) {
            ((ImageView) playerView.getCameraButton()).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_on);
        } else {
            ((ImageView) playerView.getCameraButton()).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_off);
        }

        playerView.showAddIcon();
        playerView.setTag("");
        playerView.hideControlLayout();
        playerView.setVolumeProgress(0);
        playerView.hideCloseButton();
        playerView.dismissLoading();
        AVSettingConfig.getInstance().playerURLList.remove(container.playURL);
        AVSettingConfig.getInstance().enableSpeaker = true;
        AVSettingConfig.getInstance().enableVolumeCallback = false;
        AVSettingConfig.getInstance().playoutVolume = 100;
        AVSettingConfig.getInstance().rotation = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
        AVSettingConfig.getInstance().fillMode = V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill;
    }

    private void resetPusher(V2TXLivePusher pusher, MainItemRenderView pusherView) {
        Log.i(TAG, "[Pusher] resetPusher: pusher-" + pusher + ", pusherView-" + pusherView);
        if (pusher != null) {
            stopPush();
        }
        if (!mIsPusherStart) {
            ((ImageView) pusherView.getPlayButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_stop);
        } else {
            ((ImageView) pusherView.getPlayButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_start);
        }
        if (!mIsShowPusherDebugView) {
            ((ImageView) pusherView.getLogButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_hidden);
        } else {
            ((ImageView) pusherView.getLogButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_log_show);
        }
        if (!mIsMuteAudio) {
            ((ImageView) pusherView.getMicButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_on);
        } else {
            ((ImageView) pusherView.getMicButton()).setImageResource(R.drawable.live_link_mic_new_ic_bottom_mic_off);
        }
        if (!mIsMuteVideo) {
            ((ImageView) pusherView.getCameraButton()).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_on);
        } else {
            ((ImageView) pusherView.getCameraButton()).setImageResource(R.drawable.live_link_mic_new_ic_remote_video_off);
        }
        ((ImageView) pusherView.getSwitchCameraButton()).setImageResource(mIsFrontCamera ? R.drawable.live_link_mic_new_ic_bottom_camera_back : R.drawable.live_link_mic_new_ic_bottom_camera_front);
        pusherView.showAddIcon();
        pusherView.setTag("");
        pusherView.hideControlLayout();
        pusherView.setVolumeProgress(0);
        pusherView.hideCloseButton();
        if (pusherView.getCloudView() != null) {
            pusherView.getCloudView().clearLastFrame(true);
        }
        if (pusherView != null) {
            pusherView.destroyAudioEffect();
        }
    }

    private void resetRenderView(MainItemRenderView view) {
        Log.i(TAG, "resetRenderView: view-" + view);
        if (view != null) {
            view.setTag("");
            view.setVolumeProgress(0);
            view.recvFirstAudio(false);
            view.recvFirstVideo(false);
        }
    }

    private enum V2VideoSource {
        CAMERA,
        SCREEN
    }

    private class PlayerViewContainer {
        private MainItemRenderView playerView;
        private boolean isPlaying;
        private String playURL;
        private V2TXLivePlayer livePlayer;
        private boolean isShowDebugView;
        private boolean isMuteVideo;
        private boolean isMuteAudio;
        private String textTitle;
    }

}
