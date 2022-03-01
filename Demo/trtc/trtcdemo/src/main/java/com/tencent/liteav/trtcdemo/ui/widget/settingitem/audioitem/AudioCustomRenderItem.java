package com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsBaseItem;
import com.tencent.trtc.TRTCCloudDef;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class AudioCustomRenderItem extends AbsBaseItem {

    private static final String TAG = "AudioCustomRenderItem";

    public static final int MSG_GET_DATA = 1;
    public static final int MSG_START    = 2;
    public static final int MSG_PLAY     = 3;
    public static final int MSG_STOP     = 4;

    private static final int TIMEOUT_DURATION = 30 * 1000;

    private TextView     mTextTitle;
    private SwitchCompat mSwitchCompat;
    private Button       mButton;
    private String       mTitle;

    private TRTCCloudManager mTRTCCloudManager;
    private Timer            mTimer;
    private PlayTimerTask    mPlayTimerTask;
    private HandlerThread    mRenderThread;
    private RenderHandler    mRenderHandler;
    private Handler          mUIHandler = new Handler(Looper.getMainLooper());

    private boolean enableCustomAudioRendering = false;
    private boolean flag                       = false;

    public AudioCustomRenderItem(TRTCCloudManager manager, Context context, String title) {
        super(context, true);
        mTRTCCloudManager = manager;
        mTitle = title;
        initThread();
        initView(context);
        setCheck(enableCustomAudioRendering);
        setPadding(0, SizeUtils.dp2px(8), 0, 0);
    }

    private void initThread() {
        mRenderThread = new HandlerThread(TAG);
        mRenderThread.start();
        mRenderHandler = new RenderHandler(mRenderThread.getLooper(), this);

        mTimer = new Timer();
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_setting_check_button, this, true);
        mTextTitle = findViewById(R.id.title);
        mSwitchCompat = findViewById(R.id.cb_item);
        mButton = findViewById(R.id.btn_play);
        mButton.setText(getResources().getString(R.string.trtcdemo_audio_custom_render_play));
        if (!TextUtils.isEmpty(mTitle)) {
            mTextTitle.setText(mTitle);
        }
        mSwitchCompat.setSaveEnabled(false);
        mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag) {
                    onChecked();
                } else {
                    ToastUtils.showShort(getResources().getString(R.string.trtcdemo_audio_custom_render_playing));
                }
            }
        });
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCustomAudio();
            }
        });
    }

    public void setCheck(final boolean check) {
        mSwitchCompat.setChecked(check);
    }

    public boolean getChecked() {
        return mSwitchCompat.isChecked();
    }

    public void onChecked() {
        boolean checked = mSwitchCompat.isChecked();
        mTRTCCloudManager.enableCustomAudioRendering(checked);
        if (checked) {
            ToastUtils.showShort(getResources().getString(R.string.trtcdemo_audio_custom_render_enable_tips));
            mRenderHandler.sendEmptyMessage(MSG_GET_DATA);
            if (mTimer != null) {
                mPlayTimerTask = new PlayTimerTask();
                mTimer.schedule(mPlayTimerTask, TIMEOUT_DURATION);
            }
        } else {
            handleCustomAudio();
        }
        enableCustomAudioRendering = checked;
    }

    public void destroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mRenderHandler != null) {
            mRenderHandler.sendEmptyMessage(MSG_STOP);
        }

        if (mRenderThread != null) {
            mRenderThread.quitSafely();
        }
    }

    private void handleCustomAudio() {
        if (!flag) {
            if (!enableCustomAudioRendering) {
                ToastUtils.showShort(getResources().getString(R.string.trtcdemo_audio_custom_render_disable_tips));
                return;
            }

            mButton.setText(getResources().getString(R.string.trtcdemo_audio_custom_render_stop));
            enableCustomAudioRendering = false;
            mSwitchCompat.setChecked(false);
            if (mPlayTimerTask != null) {
                mPlayTimerTask.cancel();
                mPlayTimerTask = null;
            }
            mRenderHandler.sendEmptyMessage(MSG_START);
        } else {
            mButton.setText(getResources().getString(R.string.trtcdemo_audio_custom_render_play));
            mRenderHandler.sendEmptyMessage(MSG_STOP);
        }
        flag = !flag;
    }

    private class PlayTimerTask extends TimerTask {

        @Override
        public void run() {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    enableCustomAudioRendering = false;
                    mSwitchCompat.setChecked(false);
                }
            });
            mRenderHandler.sendEmptyMessage(MSG_START);
        }
    }

    public static class RenderHandler extends Handler {

        private static final int CHANNEL            = 1;
        private static final int SAMPLE_RATE        = 48000;
        private static final int AUDIO_INTERVAL_MS  = 20;
        private static final int AUDIO_FRAME_LENGTH = SAMPLE_RATE * CHANNEL * AUDIO_INTERVAL_MS / 1000 * 2;

        private final WeakReference<AudioCustomRenderItem>    audioCustomRender;
        private final LinkedList<TRTCCloudDef.TRTCAudioFrame> audioFrames = new LinkedList<>();

        private AudioTrack audioTrack;
        private boolean    play = false;

        public RenderHandler(Looper looper, AudioCustomRenderItem audioCustomRenderItem) {
            super(looper);
            audioCustomRender = new WeakReference<>(audioCustomRenderItem);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            AudioCustomRenderItem audioCustomRenderItem = audioCustomRender.get();
            if (audioCustomRenderItem != null) {
                int what = msg.what;
                switch (what) {
                    case MSG_GET_DATA:
                        getAudioFrame(audioCustomRenderItem);
                        break;
                    case MSG_START:
                        if (play) {
                            ToastUtils.showShort(audioCustomRenderItem
                                    .getResources().getString(R.string.trtcdemo_audio_custom_render_playing));
                            return;
                        }
                        play = true;
                        startPlay(audioCustomRenderItem);
                        break;
                    case MSG_PLAY:
                        if (!audioFrames.isEmpty() && play) {
                            TRTCCloudDef.TRTCAudioFrame audioFrame = audioFrames.poll();
                            if (audioFrame != null && audioTrack != null) {
                                audioTrack.write(audioFrame.data, 0, audioFrame.data.length);
                                sendEmptyMessageDelayed(MSG_PLAY, AUDIO_INTERVAL_MS);
                            }
                        } else {
                            ToastUtils.showShort(audioCustomRenderItem
                                    .getResources().getString(R.string.trtcdemo_audio_custom_render_play_complete));
                            sendEmptyMessage(MSG_STOP);
                        }
                        break;
                    case MSG_STOP:
                        play = false;
                        stopPlay(audioCustomRenderItem);
                        break;
                    default:
                        Log.e(TAG, "RenderHandler: not match");
                        break;
                }
            }

        }

        private void stopPlay(final AudioCustomRenderItem audioCustomRenderItem) {
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }
            audioFrames.clear();
            audioCustomRenderItem.mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    audioCustomRenderItem.mButton.setText(audioCustomRenderItem.getResources()
                            .getString(R.string.trtcdemo_audio_custom_render_play));
                    audioCustomRenderItem.flag = false;
                }
            });
        }

        private void startPlay(AudioCustomRenderItem audioCustomRenderItem) {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, AUDIO_FRAME_LENGTH, AudioTrack.MODE_STREAM);
            audioTrack.play();
            ToastUtils.showShort(audioCustomRenderItem
                    .getResources().getString(R.string.trtcdemo_audio_custom_render_start_play));
            sendEmptyMessage(MSG_PLAY);
        }

        private void getAudioFrame(AudioCustomRenderItem audioCustomRenderItem) {
            if (audioCustomRenderItem.enableCustomAudioRendering) {
                TRTCCloudDef.TRTCAudioFrame audioFrame = new TRTCCloudDef.TRTCAudioFrame();
                audioFrame.channel = CHANNEL;
                audioFrame.sampleRate = SAMPLE_RATE;
                audioFrame.data = new byte[AUDIO_FRAME_LENGTH];
                audioCustomRenderItem.mTRTCCloudManager.getCustomAudioRenderingFrame(audioFrame);
                audioFrames.add(audioFrame);
                sendEmptyMessageDelayed(MSG_GET_DATA, AUDIO_INTERVAL_MS);
            }
        }
    }

}
