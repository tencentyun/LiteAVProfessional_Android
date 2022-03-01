package com.tencent.liteav.liveplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.tencent.liteav.liveplayer.custom.CustomRenderVideoFrame;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;


public class V1LivePlayerMainActivity extends Activity implements
        ITXLivePlayListener,
        OnClickListener,
        TXLivePlayer.ITXSnapshotListener,
        TXLivePlayer.ITXAudioVolumeEvaluationListener,
        TXRecordCommon.ITXVideoRecordListener,
        TXLivePlayer.ITXVideoRawDataListener,
        TXLivePlayer.ITXLivePlayVideoRenderListener,
        TXLivePlayer.ITXAudioRawDataListener {
    private static final String TAG = V1LivePlayerMainActivity.class.getSimpleName();

    public static final int SCAN_FOR_PLAY = 100;
    public static final int SCAN_FOR_SWITCH = 101;

    private static final String NORMAL_PLAY_URL = "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv";
    private static final String H265_PLAY_URL = "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid_demoH265.flv";

    /**
     * SDK player 相关
     */
    private TXLivePlayer mLivePlayer = null;
    private TXLivePlayConfig mPlayConfig;
    private TXCloudVideoView mPlayerView;
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;

    /**
     * 相关控件
     */
    private ImageView mLoadingView;
    private EditText mRtmpUrlView;
    private ProgressBar mPBVolume;
    private TextView mStatsTextView;
    private TextView mEventTextView;


    private int mPlayType;
    private int mRenderType;
    private long mStartPlayTS = 0;
    private CustomRenderVideoFrame mCustomRenderVideoFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView();
        mPlayConfig = (TXLivePlayConfig) intent.getSerializableExtra("config");
        if (mLivePlayer == null) {
            mLivePlayer = new TXLivePlayer(this);
        }
        mRenderType = getIntent().getIntExtra(Constants.RENDER_TYPE, 1);
        if (mRenderType == 1) {
            mLivePlayer.setPlayerView(mPlayerView);
            mPlayerView.setVisibility(View.VISIBLE);
        } else if (mRenderType == 2) {
            mSurfaceView.setVisibility(View.VISIBLE);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mLivePlayer.setSurface(mSurfaceView.getHolder().getSurface());
            mLivePlayer.setSurfaceSize(dm.widthPixels, dm.heightPixels);
        } else if (mRenderType == 3) {
            mLivePlayer.setVideoRenderListener(this, null);
            mTextureView.setVisibility(View.VISIBLE);
            mCustomRenderVideoFrame = new CustomRenderVideoFrame();
            mCustomRenderVideoFrame.start(mTextureView);
        } else {
            mLivePlayer.setVideoRawDataListener(this);
            mTextureView.setVisibility(View.VISIBLE);
            mCustomRenderVideoFrame = new CustomRenderVideoFrame();
            mCustomRenderVideoFrame.start(mTextureView);
        }

        mLivePlayer.setPlayListener(this);
        mLivePlayer.setAudioVolumeEvaluationListener(this);
        mLivePlayer.setAudioRawDataListener(this);
        mLivePlayer.setVideoRecordListener(this);
        mLivePlayer.enableHardwareDecode(false);
        mLivePlayer.enableAudioVolumeEvaluation(300);
        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        mLivePlayer.setConfig(mPlayConfig);
    }


    private void setContentView() {
        setContentView(R.layout.liveplayer_activity_v1_live_player_main);

        TextView titleTV = findViewById(R.id.tv_topBar_title);
        titleTV.setText("V1直播播放");

        findViewById(R.id.btn_topBar_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLivePlayer.stopPlay(true);
                finish();
            }
        });

        findViewById(R.id.btn_topBar_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Constants.LIVE_PLAYER_DOCUMENT_URL));
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_address_scan).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLivePlayer.isPlaying()) {
                    Toast.makeText(V1LivePlayerMainActivity.this, "扫码切流", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(V1LivePlayerMainActivity.this, QRCodeScanActivity.class);
                    startActivityForResult(intent, SCAN_FOR_SWITCH);
                } else {
                    Toast.makeText(V1LivePlayerMainActivity.this, "扫码播放", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(V1LivePlayerMainActivity.this, QRCodeScanActivity.class);
                    startActivityForResult(intent, SCAN_FOR_PLAY);
                }
            }
        });

        mRtmpUrlView = findViewById(R.id.et_address_url);
        mPlayerView = findViewById(R.id.vw_v1_main_video);
        mSurfaceView = findViewById(R.id.vw_v1_main_surface);
        mTextureView = findViewById(R.id.vw_v1_main_texture);
        mLoadingView = findViewById(R.id.iv_v1_main_loading);
        mPBVolume = findViewById(R.id.pb_v1_main_volume);
        mStatsTextView = findViewById(R.id.tv_v1_main_stats);
        mEventTextView = findViewById(R.id.tv_v1_main_event);

        mRtmpUrlView.setHint(" 请输入或扫二维码获取播放地址");
        mRtmpUrlView.setText(NORMAL_PLAY_URL);
        mPlayerView.getRootView().setOnClickListener(this);
        mStatsTextView.setMovementMethod(new ScrollingMovementMethod());
        mEventTextView.setMovementMethod(new ScrollingMovementMethod());

        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);

    }

    private void startLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mLoadingView.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
            ((AnimationDrawable) mLoadingView.getDrawable()).stop();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      Player 相关
    //
    /////////////////////////////////////////////////////////////////////////////////
    private boolean checkPlayUrl(final String playUrl) {
        if (TextUtils.isEmpty(playUrl) || (!playUrl.startsWith("http://") && !playUrl.startsWith("https://") && !playUrl.startsWith("rtmp://") && !playUrl.startsWith("/"))) {
            Toast.makeText(getApplicationContext(), "播放地址不合法，直播目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (playUrl.startsWith("rtmp://")) {
            mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
        } else if ((playUrl.startsWith("http://") || playUrl.startsWith("https://")) && playUrl.contains(".flv")) {
            mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
        } else {
            Toast.makeText(getApplicationContext(), "播放地址不合法，直播目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      Activity 生命周期相关
    //
    /////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        mLivePlayer.stopPlay(true);
        super.onBackPressed();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLivePlayer != null) {
            mLivePlayer.stopPlay(true);
            mLivePlayer = null;
        }
        if (mCustomRenderVideoFrame != null) {
            mCustomRenderVideoFrame.stop();
        }
        if (mPlayerView != null) {
            mPlayerView = null;
        }
        mPlayConfig = null;
        Log.d(TAG, "vrender onDestroy");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString(Constants.INTENT_SCAN_RESULT))) {
            return;
        }
        if (requestCode == SCAN_FOR_PLAY) {
            String result = data.getExtras().getString(Constants.INTENT_SCAN_RESULT);
            if (mRtmpUrlView != null) {
                mRtmpUrlView.setText(result);
            }
        } else if (requestCode == SCAN_FOR_SWITCH) {
            String result = data.getExtras().getString(Constants.INTENT_SCAN_RESULT);
            if (TextUtils.isEmpty(result)) {
                Toast.makeText(V1LivePlayerMainActivity.this, "扫码结果为空", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(V1LivePlayerMainActivity.this, "开始切流", Toast.LENGTH_SHORT).show();
            mLivePlayer.switchStream(result);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_v1_main_start) {
            if (mLivePlayer.isPlaying() == false) {
                startLoadingAnimation();
            }
            String playUrl = mRtmpUrlView.getText().toString();
            checkPlayUrl(playUrl);
            mLivePlayer.startPlay(playUrl, mPlayType);
        } else if (id == R.id.btn_v1_main_stop) {
            mLivePlayer.stopPlay(true);
        } else if (id == R.id.btn_v1_main_enableHWDecode) {
            mLivePlayer.enableHardwareDecode(true);
        } else if (id == R.id.btn_v1_main_disableHWDecode) {
            mLivePlayer.enableHardwareDecode(false);
        } else if (id == R.id.btn_v1_main_showLog) {
            mStatsTextView.setVisibility(View.VISIBLE);
            mEventTextView.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_v1_main_hideLog) {
            mStatsTextView.setVisibility(View.GONE);
            mEventTextView.setVisibility(View.GONE);
        } else if (id == R.id.btn_v1_main_rotation0) {
            mLivePlayer.setRenderRotation(0);
        } else if (id == R.id.btn_v1_main_rotation90) {
            mLivePlayer.setRenderRotation(90);
        } else if (id == R.id.btn_v1_main_rotation180) {
            mLivePlayer.setRenderRotation(180);
        } else if (id == R.id.btn_v1_main_rotation270) {
            mLivePlayer.setRenderRotation(270);
        } else if (id == R.id.btn_v1_main_fillMode) {
            mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        } else if (id == R.id.btn_v1_main_adjustMode) {
            mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        } else if (id == R.id.btn_v1_main_h265) {
            mLivePlayer.switchStream(H265_PLAY_URL);
            mRtmpUrlView.setText(H265_PLAY_URL);
        } else if (id == R.id.btn_v1_main_h264) {
            mLivePlayer.switchStream(NORMAL_PLAY_URL);
            mRtmpUrlView.setText(NORMAL_PLAY_URL);
        } else if (id == R.id.btn_v1_main_isPlaying) {
            Toast.makeText(getApplicationContext(), "isPlaying：" + Boolean.toString(mLivePlayer.isPlaying()),
                    Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_v1_main_pause) {
            mLivePlayer.pause();
        } else if (id == R.id.btn_v1_main_resume) {
            mLivePlayer.resume();
        } else if (id == R.id.btn_v1_main_getCurrentRenderPts) {
            Toast.makeText(getApplicationContext(),
                    "CurrentRenderPts：" + Long.toString(mLivePlayer.getCurrentRenderPts()),
                    Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_v1_main_mute) {
            mLivePlayer.setMute(true);
        } else if (id == R.id.btn_v1_main_noMute) {
            mLivePlayer.setMute(false);
        } else if (id == R.id.btn_v1_main_speaker) {
            mLivePlayer.setAudioRoute(TXLiveConstants.AUDIO_ROUTE_SPEAKER);
        } else if (id == R.id.btn_v1_main_receiver) {
            mLivePlayer.setAudioRoute(TXLiveConstants.AUDIO_ROUTE_RECEIVER);
        } else if (id == R.id.btn_v1_main_noAutoAdjustCacheTime) {
            mPlayConfig.setAutoAdjustCacheTime(false);
            mLivePlayer.setConfig(mPlayConfig);
        } else if (id == R.id.btn_v1_main_autoAdjustCacheTime) {
            mPlayConfig.setAutoAdjustCacheTime(true);
            mLivePlayer.setConfig(mPlayConfig);
        } else if (id == R.id.btn_v1_main_startRecord) {
            if (mLivePlayer.startRecord(TXRecordCommon.RECORD_TYPE_STREAM_SOURCE) != 0) {
                Toast.makeText(getApplicationContext(), "startRecord failed",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.btn_v1_main_stopRecord) {
            if (mLivePlayer.stopRecord() != 0) {
                Toast.makeText(getApplicationContext(), "stopRecord failed",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.btn_v1_main_snapshot) {
            mLivePlayer.snapshot(this);
        } else if (id == R.id.btn_v1_main_resumeLive) {
            mLivePlayer.resumeLive();
        } else if (id == R.id.btn_v1_main_seek) {
            EditText editText = findViewById(R.id.et_v1_main_seek);
            if (!editText.getText().toString().isEmpty()) {
                mLivePlayer.seek(Integer.parseInt(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_main_setVolume) {
            EditText editText;
            editText = findViewById(R.id.et_v1_main_setVolume);
            if (!editText.getText().toString().isEmpty()) {
                mLivePlayer.setVolume(Integer.parseInt(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_main_enableAudioVolumeEvaluation) {
            EditText editText;
            editText = findViewById(R.id.et_v1_main_enableAudioVolumeEvaluation);
            if (!editText.getText().toString().isEmpty()) {
                mLivePlayer.enableAudioVolumeEvaluation(Integer.parseInt(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v1_main_maxAutoAdjustCacheTime) {
            EditText editText;
            editText = findViewById(R.id.et_v1_main_maxAutoAdjustCacheTime);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setMaxAutoAdjustCacheTime(Integer.parseInt(editText.getText().toString()));
                mLivePlayer.setConfig(mPlayConfig);
            }
        } else if (id == R.id.btn_v1_main_minAutoAdjustCacheTime) {
            EditText editText;
            editText = findViewById(R.id.et_v1_main_minAutoAdjustCacheTime);
            if (!editText.getText().toString().isEmpty()) {
                mPlayConfig.setMinAutoAdjustCacheTime(Integer.parseInt(editText.getText().toString()));
                mLivePlayer.setConfig(mPlayConfig);
            }
        } else if (id == R.id.btn_v1_main_experimentalAPI) {
            EditText editText;
            editText = findViewById(R.id.et_v1_main_experimentalAPI);
            if (!editText.getText().toString().isEmpty()) {
                // 调用试验性接口
            }
        }
    }

    private void updateDebugView(String string) {
        mEventTextView.setText(mEventTextView.getText() + string);
        mEventTextView.post(new Runnable() {
            @Override
            public void run() {
                int offset = mEventTextView.getLineCount() * mEventTextView.getLineHeight();
                if (offset > mEventTextView.getHeight()) {
                    mEventTextView.scrollTo(0, offset - mEventTextView.getHeight());
                }
            }
        });

    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      回调函数
    //
    /////////////////////////////////////////////////////////////////////////////////
    public static String showBundleData(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "";
        for (String key : bundle.keySet()) {
            string += key + ": " + bundle.get(key) + "\n";
        }
        return string;
    }

    public static String showBundleData(Bundle bundle, int event) {
        if (bundle == null) {
            return null;
        }
        String string = "[event]: " + event + "\n";
        for (String key : bundle.keySet()) {
            string += key + ": " + bundle.get(key) + "\n";
        }
        string += "-----------------\n";
        return string;
    }

    @Override
    public void onPlayEvent(int event, Bundle param) {
        String playEventLog = showBundleData(param, event);
        Log.d(TAG, playEventLog);
        updateDebugView(playEventLog);
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            stopLoadingAnimation();
            Log.d("AutoMonitor", "PlayFirstRender,cost=" + (System.currentTimeMillis() - mStartPlayTS));
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            mLivePlayer.stopPlay(true);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING) {
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_ROTATION) {
            return;
        } else if (event == TXLiveConstants.PLAY_EVT_GET_MESSAGE) {
            if (param != null) {
                byte data[] = param.getByteArray(TXLiveConstants.EVT_GET_MSG);
                String seiMessage = "";
                if (data != null && data.length > 0) {
                    try {
                        seiMessage = new String(data, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "onPlayEvent: sei:" + seiMessage);
                Toast.makeText(getApplicationContext(), seiMessage, Toast.LENGTH_SHORT).show();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_GET_FLVSESSIONKEY) {
            String flvSessionKey = param.getString(TXLiveConstants.EVT_DESCRIPTION, "");
            Toast.makeText(getApplicationContext(), "event PLAY_EVT_GET_FLVSESSIONKEY: " + flvSessionKey, Toast.LENGTH_SHORT).show();
        }

        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNetStatus(Bundle status) {
        String str = showBundleData(status);
        mStatsTextView.setText(str);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRenderVideoFrame(TXLivePlayer.TXLiteAVTexture texture) {
        if (mCustomRenderVideoFrame != null) {
            mCustomRenderVideoFrame.onRenderVideoFrame(texture);
        }
    }


    @Override
    public void onVideoRawDataAvailable(byte[] yuvBuffer, int width, int height, int timestamp) {
        V2TXLiveDef.V2TXLiveVideoFrame v2frame = new V2TXLiveDef.V2TXLiveVideoFrame();
        v2frame.pixelFormat = V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatI420;
        v2frame.bufferType = V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeByteArray;
        v2frame.data = yuvBuffer;
        v2frame.width = width;
        v2frame.height = height;
        if (mCustomRenderVideoFrame != null) {
            mCustomRenderVideoFrame.onRenderVideoFrame(v2frame);
        }
    }


    @Override
    public void onAudioInfoChanged(int sample_rate, int channels, int bitsperchannel) {
        String str = "onAudioInfoChanged( sample_rate:" +
                sample_rate + ", channels:" +
                channels + ", bitsperchannel" +
                bitsperchannel + ")\n";
        updateDebugView(str);
    }

    @Override
    public void onPcmDataAvailable(byte[] pcm_data, long timestamp) {

    }

    @Override
    public void onAudioVolumeEvaluationNotify(int volume) {
        mPBVolume.setProgress(volume);
    }

    @Override
    public void onSnapshot(Bitmap image) {
        final Dialog dialog = new Dialog(this);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = image.getWidth();
        params.height = image.getHeight();
        dialog.getWindow().setAttributes(params);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(image);
        dialog.setContentView(imageView);
        dialog.show();
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    dialog.dismiss();
                } catch (Exception e) {

                }
            }
        }, 1000);
    }

    //    还没有实现的回调函数接口
    @Override
    public void onRecordEvent(int var1, Bundle var2) {

    }

    @Override
    public void onRecordProgress(long var1) {

    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult var1) {

    }

}