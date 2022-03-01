package com.tencent.liteav.trtcdemo.ui.fragment;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.CommonApplication;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class VodPlayerFragment extends Fragment implements ITXVodPlayListener {
    private static final String TAG = "VodPlayerFragment";

    private TXVodPlayer      mVodPlayer = null;
    private TXCloudVideoView mPlayerView;
    private ImageView        mLoadingView;
    private boolean          mHWDecode  = false;
    private LinearLayout     mRootView;

    private Button   mBtnLog;
    private Button   mBtnPlay;
    private Button   mBtnRenderRotation;
    private Button   mBtnRenderMode;
    private Button   mBtnHWDecode;
    private SeekBar  mSeekBar;
    private TextView mTextDuration;
    private TextView mTextStart;
    private SeekBar  mVolumeSeekBar;

    private Button mBtnStop;
    private Button mBtnCache;
    private Button mBtnSpd;

    private int mCurrentRenderMode;
    private int mCurrentRenderRotation;

    private boolean         mStartSeek    = false;
    private boolean         mVideoPause   = false;
    private boolean         mVideoPublish = false;
    private boolean         mAudioPublish = false;
    private TXVodPlayConfig mPlayConfig;
    private long            mStartPlayTS  = 0;

    private boolean mEnableCache;
    private boolean mVideoPlay;
    private boolean mIsLogShow = false;
    private float   mPlayRate  = 1.0f;

    public Object mTrtcCloud = null;

    private static final String DOCUMENT_URL = "https://cloud.tencent.com/document/product/454/12148";
    private static final String PLAY_URL      = "http://1252463788.vod2.myqcloud.com/95576ef5vodtransgzp1252463788/e1ab85305285890781763144364/v.f20.mp4";

    private SeekBar.OnSeekBarChangeListener mOnSeekBarVolumeChangedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mVodPlayer != null) {
                mVodPlayer.setAudioPlayoutVolume(seekBar.getProgress());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trtcdemo_fragment_vod_player, container, false);

        mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;

        mPlayConfig = new TXVodPlayConfig();

        setContentView(rootView);

        TextView titleTV = (TextView) rootView.findViewById(R.id.title_tv);
        titleTV.setText(getString(R.string.trtcdemo_title_vodplayer));
        mVolumeSeekBar = (SeekBar) rootView.findViewById(R.id.volume_seekbar);
        mVolumeSeekBar.setOnSeekBarChangeListener(mOnSeekBarVolumeChangedListener);

        registerForContextMenu(rootView.findViewById(R.id.btnPlay));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        stopPlayVod();
        super.onDestroyView();
        if (mVodPlayer != null) {
            mVodPlayer.detachTRTC();
        }
    }

    public void setContentView(View rootView) {
        mRootView = (LinearLayout) rootView.findViewById(R.id.root);
        if (mVodPlayer == null) {
            mVodPlayer = new TXVodPlayer(getActivity());
        }
        if (mTrtcCloud != null) {
            mVodPlayer.attachTRTC(mTrtcCloud);
            mVodPlayer.setLoop(true);
        }
        mPhoneListener = new TXPhoneStateListener(getActivity(), mVodPlayer);
        mPhoneListener.startListen();

        mPlayerView = (TXCloudVideoView) rootView.findViewById(R.id.video_view);
        mPlayerView.showLog(false);
        mPlayerView.setLogMargin(12, 12, 110, 60);
        mLoadingView = (ImageView) rootView.findViewById(R.id.loadingImageView);

        mVideoPlay = false;

        mBtnPlay = (Button) rootView.findViewById(R.id.btnPlay);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click playbtn isplay:" + mVideoPlay + " ispause:" + mVideoPause);
                if (mVideoPlay) {
                    if (mVideoPause) {
                        mVodPlayer.resume();
                        mBtnPlay.setBackgroundResource(R.drawable.trtcdemo_play_pause);
                        mRootView.setBackgroundColor(0xff000000);
                    } else {
                        mVodPlayer.pause();
                        mBtnPlay.setBackgroundResource(R.drawable.trtcdemo_play_start);
                    }
                    mVideoPause = !mVideoPause;
                } else {
                    mVideoPlay = startPlayVod();
                }
            }
        });

        final Button btnVideo = (Button) rootView.findViewById(R.id.btnVideo);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPublish) {
                    mVodPlayer.unpublishVideo();
                    btnVideo.setBackgroundResource(R.drawable.trtcdemo_play_start);
                } else {
                    mVodPlayer.publishVideo();
                    btnVideo.setBackgroundResource(R.drawable.trtcdemo_play_pause);
                }
                mVideoPublish = !mVideoPublish;
            }
        });

        final Button btnAudio = (Button) rootView.findViewById(R.id.btnAudio);
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioPublish) {
                    mVodPlayer.unpublishAudio();
                    btnAudio.setBackgroundResource(R.drawable.trtcdemo_play_start);
                } else {
                    mVodPlayer.publishAudio();
                    btnAudio.setBackgroundResource(R.drawable.trtcdemo_play_pause);
                }
                mAudioPublish = !mAudioPublish;
            }
        });
        //停止按钮
        mBtnStop = (Button) rootView.findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayVod();
                mVideoPlay = false;
                mVideoPause = false;
                if (mTextStart != null) {
                    mTextStart.setText("00:00");
                }
                if (mSeekBar != null) {
                    mSeekBar.setProgress(0);
                    mSeekBar.setSecondaryProgress(0);
                }
            }
        });

        mBtnLog = (Button) rootView.findViewById(R.id.btnLog);
        mBtnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLogShow) {
                    mIsLogShow = false;
                    mBtnLog.setBackgroundResource(R.drawable.trtcdemo_log_show);
                    mPlayerView.showLog(false);
                } else {
                    mIsLogShow = true;
                    mBtnLog.setBackgroundResource(R.drawable.trtcdemo_log_hidden);
                    mPlayerView.showLog(true);
                }
            }
        });

        //横屏|竖屏
        mBtnRenderRotation = (Button) rootView.findViewById(R.id.btnOrientation);
        mBtnRenderRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }

                if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                    mBtnRenderRotation.setBackgroundResource(R.drawable.trtcdemo_portrait);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
                } else if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                    mBtnRenderRotation.setBackgroundResource(R.drawable.trtcdemo_landscape);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
                }

                mVodPlayer.setRenderRotation(mCurrentRenderRotation);
            }
        });

        //平铺模式
        mBtnRenderMode = (Button) rootView.findViewById(R.id.btnRenderMode);
        mBtnRenderMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVodPlayer == null) {
                    return;
                }

                if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                    mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
                    mBtnRenderMode.setBackgroundResource(R.drawable.trtcdemo_fill_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                } else if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                    mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                    mBtnRenderMode.setBackgroundResource(R.drawable.trtcdemo_adjust_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
                }
            }
        });

        //硬件解码
        mBtnHWDecode = (Button) rootView.findViewById(R.id.btnHWDecode);
        mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);
        mBtnHWDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHWDecode = !mHWDecode;
                mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);

                if (mHWDecode) {
                    Toast.makeText(getActivity(), getString(R.string.trtcdemo_open_hardware_code_tips), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.trtcdemo_close_hardware_code_tips), Toast.LENGTH_SHORT).show();
                }

                if (mVideoPlay) {

                    stopPlayVod();
                    mVideoPlay = startPlayVod();
                    if (mVideoPause) {
                        if (mPlayerView != null) {
                            mPlayerView.onResume();
                        }
                        mVideoPause = false;
                    }
                }
            }
        });

        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bFromUser) {
                mTextStart.setText(String.format("%02d:%02d", progress / 1000 / 60, progress / 1000 % 60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVodPlayer != null) {
                    mVodPlayer.seek(seekBar.getProgress() / 1000.f);
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStartSeek = false;
                    }
                }, 500);
            }
        });

        mTextDuration = (TextView) rootView.findViewById(R.id.duration);
        mTextStart = (TextView) rootView.findViewById(R.id.play_start);
        mTextDuration.setTextColor(Color.rgb(255, 255, 255));
        mTextStart.setTextColor(Color.rgb(255, 255, 255));

        mBtnCache = (Button) rootView.findViewById(R.id.btnCache);
        mBtnCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
        mBtnCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableCache = !mEnableCache;
                mBtnCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
            }
        });

        mBtnSpd = (Button) rootView.findViewById(R.id.btnSpd);
        mBtnSpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayRate == 1.0f) {
                    mPlayRate = 1.5f;
                    mBtnSpd.setBackgroundDrawable(getResources().getDrawable(R.drawable.trtcdemo_spd1_5));
                } else if (mPlayRate == 1.5f) {
                    mPlayRate = 2.0f;
                    mBtnSpd.setBackgroundDrawable(getResources().getDrawable(R.drawable.trtcdemo_spd2));
                } else {
                    mPlayRate = 1.0f;
                    mBtnSpd.setBackgroundDrawable(getResources().getDrawable(R.drawable.trtcdemo_spd));
                }
                if (mVodPlayer != null)
                    mVodPlayer.setRate(mPlayRate);
            }
        });

        rootView.findViewById(R.id.webrtc_link_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(DOCUMENT_URL));
                IntentUtils.safeStartActivity(VodPlayerFragment.this.getActivity(), intent);
            }
        });
    }

    /**
     * 获取内置SD卡路径
     *
     * @return
     */
    public String getInnerSDCardPath() {
        return getActivity().getExternalFilesDir(null).getAbsolutePath();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVodPlayer != null) {
            mVodPlayer.stopPlay(true);
            mVodPlayer = null;
        }
        if (mPlayerView != null) {
            mPlayerView.onDestroy();
            mPlayerView = null;
        }
        mPlayConfig = null;
        Log.d(TAG, "onDestroy()");
        mPhoneListener.stopListen();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        if (mVodPlayer != null) {
            mVodPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (mVodPlayer != null) {
            mVodPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (mVideoPlay && !mVideoPause) {
            if (mVodPlayer != null) {
                mVodPlayer.resume();
            }
        }
    }

    public void unPublish() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button btnVideo = (Button) getActivity().findViewById(R.id.btnVideo);
                mVodPlayer.unpublishVideo();
                btnVideo.setBackgroundResource(R.drawable.trtcdemo_play_start);
                mVideoPublish = false;

                final Button btnAudio = (Button) getActivity().findViewById(R.id.btnAudio);
                mVodPlayer.unpublishAudio();
                btnAudio.setBackgroundResource(R.drawable.trtcdemo_play_start);
                mAudioPublish = false;
            }
        });
    }

    private boolean startPlayVod() {
        mBtnPlay.setBackgroundResource(R.drawable.trtcdemo_play_pause);
        mRootView.setBackgroundColor(0xff000000);

        if (mTrtcCloud != null) {
//            mVodPlayer.attachTRTC(mTrtcCloud);
            mVodPlayer.setLoop(true);
        }
        mVodPlayer.setPlayerView(mPlayerView);

        mVodPlayer.setVodListener(this);
        mVodPlayer.setRate(mPlayRate);
        // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
        // (1) 只有 4.3 以上android系统才支持
        // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
        mVodPlayer.enableHardwareDecode(mHWDecode);
        mVodPlayer.setRenderRotation(mCurrentRenderRotation);
        mVodPlayer.setRenderMode(mCurrentRenderMode);

        if (mEnableCache) {
            mPlayConfig.setCacheFolderPath(getInnerSDCardPath() + "/txcache");
            mPlayConfig.setMaxCacheItems(1);
        } else {
            mPlayConfig.setCacheFolderPath(null);
        }
        Map<String, String> header = new HashMap<>();
        mPlayConfig.setProgressInterval(200);
        mPlayConfig.setHeaders(header);
        mVodPlayer.setConfig(mPlayConfig);
        mVodPlayer.setAutoPlay(true);
        int result = mVodPlayer.startPlay(PLAY_URL); // result返回值：0 success;  -1 empty url;
        if (result != 0) {
            mBtnPlay.setBackgroundResource(R.drawable.trtcdemo_play_start);
            mRootView.setBackgroundResource(R.drawable.trtcdemo_main_bkg);
            return false;
        }

        Log.w("video render", "timetrack start play");

        startLoadingAnimation();

        mStartPlayTS = System.currentTimeMillis();

        mRootView.findViewById(R.id.playerHeaderView).setVisibility(View.VISIBLE);

        return true;
    }

    private void stopPlayVod() {
        mBtnPlay.setBackgroundResource(R.drawable.trtcdemo_play_start);
        mRootView.setBackgroundResource(R.drawable.trtcdemo_main_bkg);
        stopLoadingAnimation();
        if (mVodPlayer != null) {
            mVodPlayer.setVodListener(null);
            mVodPlayer.stopPlay(true);
        }

        mVideoPause = false;
        mVideoPlay = false;
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "receive event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            Log.d(TAG, playEventLog);
        }

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED || event == TXLiveConstants.PLAY_EVT_VOD_LOADING_END) {
            stopLoadingAnimation();
        }

        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            handlePlayBeginEvent();
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            if (mStartSeek) {
                return;
            }
            handlePlayProgressEvent(param);
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END || event == TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND) {
            hanldePlayEndEvent();
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING) {
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
            mRootView.findViewById(R.id.playerHeaderView).setVisibility(View.GONE);
            if (mPhoneListener.isInBackground()) {
                Log.i(TAG, "is in background, pause player");
                mVodPlayer.pause();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
        } else if (event == TXLiveConstants.PLAY_ERR_HLS_KEY) {//HLS 解密 key 获取失败
            stopPlayVod();
        } else if (event == TXLiveConstants.PLAY_WARNING_RECONNECT) {
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_ROTATION) {
            return;
        }

        if (event < 0) {
            Toast.makeText(getActivity(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
    }

    private void hanldePlayEndEvent() {
        stopPlayVod();
        mVideoPlay = false;
        mVideoPause = false;
        if (mTextStart != null) {
            mTextStart.setText("00:00");
        }
        if (mSeekBar != null) {
            mSeekBar.setProgress(0);
        }
    }

    private void handlePlayProgressEvent(Bundle param) {
        int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
        int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
        int playable = param.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS);

        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
            mSeekBar.setSecondaryProgress(playable);
        }
        if (mTextStart != null) {
            mTextStart.setText(String.format("%02d:%02d", progress / 1000 / 60, progress / 1000 % 60));
        }
        if (mTextDuration != null) {
            mTextDuration.setText(String.format("%02d:%02d", duration / 1000 / 60, duration / 1000 % 60));
        }
        if (mSeekBar != null) {
            mSeekBar.setMax(duration);
        }
    }

    private void handlePlayBeginEvent() {
        stopLoadingAnimation();
        Log.d("AutoMonitor", "PlayFirstRender,cost=" + (System.currentTimeMillis() - mStartPlayTS));
        if (mPhoneListener.isInBackground()) {
            mVodPlayer.pause();
        } else {
            mBtnPlay.setBackgroundResource(R.drawable.trtcdemo_play_pause);
            mRootView.setBackgroundColor(0xff000000);
            mVideoPause = false;
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

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

    static class TXPhoneStateListener extends PhoneStateListener implements Application.ActivityLifecycleCallbacks {
        WeakReference<TXVodPlayer> mPlayer;
        Context                    mContext;
        int                        activityCount;

        public TXPhoneStateListener(Context context, TXVodPlayer player) {
            mPlayer = new WeakReference<>(player);
            mContext = context.getApplicationContext();
        }

        public void startListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
            CommonApplication.get().registerActivityLifecycleCallbacks(this);
        }

        public void stopListen() {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(this, PhoneStateListener.LISTEN_NONE);

            CommonApplication.get().unregisterActivityLifecycleCallbacks(this);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXVodPlayer player = mPlayer.get();
            switch (state) {
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "CALL_STATE_RINGING");
                    if (player != null) player.pause();
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                    if (player != null) player.pause();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "CALL_STATE_IDLE");
                    if (player != null && activityCount >= 0) player.resume();
                    break;
            }
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            activityCount++;
            Log.d(TAG, "onActivityResumed" + activityCount);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityCount--;
            Log.d(TAG, "onActivityStopped" + activityCount);
        }

        boolean isInBackground() {
            return (activityCount < 0);
        }
    }

    private TXPhoneStateListener mPhoneListener = null;
}
