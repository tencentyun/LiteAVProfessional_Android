package com.tencent.liteav.liveplayer;

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_ERROR_DISCONNECTED;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.liveplayer.custom.CustomRenderVideoFrame;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLiveDef.V2TXLiveFillMode;
import com.tencent.live2.V2TXLiveDef.V2TXLiveRotation;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLiveDefInner;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class V2LivePlayerMainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = V2LivePlayerMainActivity.class.getSimpleName();

    /**
     * SDK player 相关
     */
    private V2TXLivePlayer mLivePlayer = null;
    private TXCloudVideoView mTXCloudVideoView;
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;

    /**
     * 相关控件
     */
    private ImageView mLoadingView;
    private EditText mRtmpUrlView;
    private ProgressBar mPBVolume;
    private TextView mDebugTextView;
    private TextView mDebugScrollView;

    private int mPayloadType = 0;
    private int mRenderType = 1;
    private V2TXLiveDef.V2TXLivePixelFormat mPixelFormat = V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D;
    private V2TXLiveDef.V2TXLiveBufferType mBufferType = V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeTexture;
    private CustomRenderVideoFrame mCustomRenderVideoFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        if (mLivePlayer == null) {
            mLivePlayer = new V2TXLivePlayerImpl(this);
        }
        mRenderType = getIntent().getIntExtra(Constants.RENDER_TYPE, mRenderType);
        mPayloadType = getIntent().getIntExtra(Constants.PAYLOAD_TYPE, mPayloadType);
        int pixelFormatIndex = getIntent().getIntExtra("pixel_format", 2);
        int bufferTypeIndex = getIntent().getIntExtra("buffer_type", 2);
        switch (pixelFormatIndex) {
            case 0:
                mPixelFormat = V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatUnknown;
                break;
            case 1:
                mPixelFormat = V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatI420;
                break;
            case 2:
                mPixelFormat = V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D;
                break;
        }
        switch (bufferTypeIndex) {
            case 0:
                mBufferType = V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeUnknown;
                break;
            case 1:
                mBufferType = V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeByteBuffer;
                break;
            case 2:
                mBufferType = V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeTexture;
                break;
            case 3:
                mBufferType = V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeByteArray;
                break;
        }
        switch (mRenderType) {
            case 1:
                mTXCloudVideoView.setVisibility(View.VISIBLE);
                mLivePlayer.setRenderView(mTXCloudVideoView);
                break;
            case 2:
                mTextureView.setVisibility(View.VISIBLE);
                mLivePlayer.setRenderView(mTextureView);
                break;
            case 3:
                mSurfaceView.setVisibility(View.VISIBLE);
                mLivePlayer.setRenderView(mSurfaceView);
                break;
            case 4:
                mSurfaceView.setVisibility(View.VISIBLE);
                DisplayMetrics dm = getResources().getDisplayMetrics();
                mLivePlayer.setProperty("setSurface", mSurfaceView.getHolder().getSurface());
                mLivePlayer.setProperty("setSurfaceSize", new V2TXLiveDefInner.SurfaceSize(dm.widthPixels, dm.heightPixels));
                break;
            case 5:
                mTextureView.setVisibility(View.VISIBLE);
                mLivePlayer.enableObserveVideoFrame(true, mPixelFormat, mBufferType);
                mCustomRenderVideoFrame = new CustomRenderVideoFrame();
                mCustomRenderVideoFrame.start(mTextureView);
                break;
        }
        mLivePlayer.setObserver(new MyPlayerObserver());
        mLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0);
        mLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit);
        mLivePlayer.setPlayoutVolume(100);
        mLivePlayer.enableVolumeEvaluation(300);
        if (mPayloadType != 0) {
            mLivePlayer.enableReceiveSeiMessage(true, mPayloadType);
        }
    }

    private void setContentView() {
        setContentView(R.layout.liveplayer_activity_v2_live_player_main);
        TextView titleTV = (TextView) findViewById(R.id.tv_topBar_title);
        titleTV.setText("V2直播播放");

        findViewById(R.id.btn_topBar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLivePlayer.stopPlay();
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

        findViewById(R.id.btn_address_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(V2LivePlayerMainActivity.this, "扫码播放", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(V2LivePlayerMainActivity.this, QRCodeScanActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        mRtmpUrlView = findViewById(R.id.et_address_url);
        mTXCloudVideoView = findViewById(R.id.vw_v2_main_video);
        mTextureView = findViewById(R.id.vw_v2_main_texture);
        mSurfaceView = findViewById(R.id.vw_v2_main_surface);
        mLoadingView = findViewById(R.id.iv_v2_main_loading);
        mPBVolume = findViewById(R.id.pb_v2_main_volume);
        mDebugTextView = findViewById(R.id.tv_v2_main_debug);
        mDebugScrollView = findViewById(R.id.sv_v2_main_debug);

        mRtmpUrlView.setHint(" 请输入或扫二维码获取播放地址");
        mRtmpUrlView.setText(Constants.NORMAL_PLAY_URL);
        mDebugScrollView.setMovementMethod(new ScrollingMovementMethod());

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
    //                      Activity 生命周期相关
    //
    /////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        mLivePlayer.stopPlay();
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
            mLivePlayer.stopPlay();
            mLivePlayer = null;
        }
        if (mCustomRenderVideoFrame != null) {
            mCustomRenderVideoFrame.stop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString(Constants.INTENT_SCAN_RESULT))) {
            return;
        }
        String result = data.getExtras().getString(Constants.INTENT_SCAN_RESULT);
        if (mRtmpUrlView != null) {
            mRtmpUrlView.setText(result);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_v2_main_start) {
            if (mLivePlayer.isPlaying() == 0) {
                startLoadingAnimation();
            }
            String playURL = mRtmpUrlView.getText().toString();
            mLivePlayer.startPlay(playURL);
        } else if (id == R.id.btn_v2_main_stop) {
            mLivePlayer.stopPlay();
        } else if (id == R.id.btn_v2_main_isPlaying) {
            Toast.makeText(getApplicationContext(), "isPlaying：" + Integer.toString(mLivePlayer.isPlaying()),
                    Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_v2_main_showLog) {
            mDebugTextView.setVisibility(View.VISIBLE);
            mDebugScrollView.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_v2_main_hideLog) {
            mDebugTextView.setVisibility(View.GONE);
            mDebugScrollView.setVisibility(View.GONE);
        } else if (id == R.id.btn_v2_main_rotation0) {
            mLivePlayer.setRenderRotation(V2TXLiveRotation.V2TXLiveRotation0);
        } else if (id == R.id.btn_v2_main_rotation90) {
            mLivePlayer.setRenderRotation(V2TXLiveRotation.V2TXLiveRotation90);
        } else if (id == R.id.btn_v2_main_rotation180) {
            mLivePlayer.setRenderRotation(V2TXLiveRotation.V2TXLiveRotation180);
        } else if (id == R.id.btn_v2_main_rotation270) {
            mLivePlayer.setRenderRotation(V2TXLiveRotation.V2TXLiveRotation270);
        } else if (id == R.id.btn_v2_main_fillMode) {
            mLivePlayer.setRenderFillMode(V2TXLiveFillMode.V2TXLiveFillModeFill);
        } else if (id == R.id.btn_v2_main_adjustMode) {
            mLivePlayer.setRenderFillMode(V2TXLiveFillMode.V2TXLiveFillModeFit);
        } else if (id == R.id.btn_v2_main_pause) {
            mLivePlayer.pauseVideo();
        } else if (id == R.id.btn_v2_main_resume) {
            mLivePlayer.resumeVideo();
        } else if (id == R.id.btn_v2_main_mute) {
            mLivePlayer.pauseAudio();
        } else if (id == R.id.btn_v2_main_noMute) {
            mLivePlayer.resumeAudio();
        } else if (id == R.id.btn_v2_main_snapshot) {
            mLivePlayer.snapshot();
        } else if (id == R.id.btn_v2_main_setVolume) {
            EditText editText = findViewById(R.id.et_v2_main_setVolume);
            if (!editText.getText().toString().isEmpty()) {
                mLivePlayer.setPlayoutVolume(Integer.parseInt(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v2_main_enableAudioVolumeEvaluation) {
            EditText editText;
            editText = findViewById(R.id.et_v2_main_enableAudioVolumeEvaluation);
            if (!editText.getText().toString().isEmpty()) {
                mLivePlayer.enableVolumeEvaluation(Integer.parseInt(editText.getText().toString()));
            }
        } else if (id == R.id.btn_v2_main_setCache) {
            EditText editText;
            editText = findViewById(R.id.et_v2_main_cacheMaxTime);
            int maxTime = Integer.parseInt(editText.getText().toString());

            editText = findViewById(R.id.et_v2_main_cacheMinTime);
            int minTime = Integer.parseInt(editText.getText().toString());
            if (!editText.getText().toString().isEmpty()) {
                mLivePlayer.setCacheParams(minTime, maxTime);
            }
        }
    }

    private void updateDebugView(String string) {
        mDebugScrollView.setText(mDebugScrollView.getText() + string);
        mDebugScrollView.post(new Runnable() {
            @Override
            public void run() {
                int offset = mDebugScrollView.getLineCount() * mDebugScrollView.getLineHeight();
                if (offset > mDebugScrollView.getHeight()) {
                    mDebugScrollView.scrollTo(0, offset - mDebugScrollView.getHeight());
                }
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      回调函数
    //
    /////////////////////////////////////////////////////////////////////////////////
    private class MyPlayerObserver extends V2TXLivePlayerObserver {

        @Override
        public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            String str = "[event] onWarning\n  code:" + code + " msg:" + msg + " info:" + extraInfo + "\n";
            Log.w(TAG, str);
            updateDebugView(str);
        }

        @Override
        public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            String str = "[event] onError\n  code:" + code + " msg:" + msg + " info:" + extraInfo + "\n";
            Log.e(TAG, str);
            updateDebugView(str);
            if (code == V2TXLIVE_ERROR_DISCONNECTED) {
                mLivePlayer.stopPlay();
            }
        }

        @Override
        public void onSnapshotComplete(V2TXLivePlayer player, Bitmap bitmap) {
            String str = "[event] onSnapshotComplete\n bitmap:" + bitmap + "\n";
            Log.w(TAG, str);
            final Dialog dialog = new Dialog(V2LivePlayerMainActivity.this);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = bitmap.getWidth();
            params.height = bitmap.getHeight();
            dialog.getWindow().setAttributes(params);
            ImageView imageView = new ImageView(V2LivePlayerMainActivity.this);
            imageView.setImageBitmap(bitmap);
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

        @Override
        public void onConnected(V2TXLivePlayer player, Bundle extraInfo) {
            Log.e(TAG, "[Player] onConnected");
        }

        @Override
        public void onVideoPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
            Log.d(TAG, "[Player] onVideoPlaying  firstPlay -> " + firstPlay);
            stopLoadingAnimation();
        }

        @Override
        public void onVideoLoading(V2TXLivePlayer player, Bundle extraInfo) {
            Log.d(TAG, "[Player] onVideoLoading");
            startLoadingAnimation();
        }

        @Override
        public void onAudioPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
            Log.d(TAG, "[Player] onAudioPlaying  firstPlay -> " + firstPlay);
            stopLoadingAnimation();
        }

        @Override
        public void onAudioLoading(V2TXLivePlayer player, Bundle extraInfo) {
            Log.d(TAG, "[Player] onAudioLoading");
            startLoadingAnimation();
        }

        @Override
        public void onReceiveSeiMessage(V2TXLivePlayer player, int payloadType, byte[] data) {
            String seiMessage = "";
            if (data != null && data.length > 0) {
                try {
                    seiMessage = new String(data, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String str = "[event] onReceiveSeiMessage\nseiMessage:" + seiMessage + "\n";
            Log.w(TAG, str);
            updateDebugView(str);
            Toast.makeText(getApplicationContext(), seiMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRenderVideoFrame(V2TXLivePlayer player, V2TXLiveDef.V2TXLiveVideoFrame videoFrame) {
            if (mCustomRenderVideoFrame != null) {
                mCustomRenderVideoFrame.onRenderVideoFrame(videoFrame);
            }
        }

        @Override
        public void onPlayoutVolumeUpdate(V2TXLivePlayer player, int volume) {
            mPBVolume.setProgress(volume);
        }

        @Override
        public void onStatisticsUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayerStatistics statistics) {
            String log = " cpu:" + statistics.appCpu
                    + "\n systemCpu:" + statistics.systemCpu
                    + "\n width:" + statistics.width
                    + "\n height:" + statistics.height
                    + "\n fps:" + statistics.fps
                    + "\n videoBitrate:" + statistics.videoBitrate
                    + "\n audioBitrate:" + statistics.audioBitrate;
            mDebugTextView.setText(log);
        }
    }
}
