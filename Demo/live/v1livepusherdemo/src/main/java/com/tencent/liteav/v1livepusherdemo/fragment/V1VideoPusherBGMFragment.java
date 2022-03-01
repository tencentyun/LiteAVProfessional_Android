package com.tencent.liteav.v1livepusherdemo.fragment;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.audio.TXAudioEffectManager.TXMusicPlayObserver;
import com.tencent.liteav.v1livepusherdemo.R;
import com.tencent.liteav.v1livepusherdemo.customcapture.utils.FileUtils;
import java.io.File;

@SuppressLint("ValidFragment")
public class V1VideoPusherBGMFragment extends DialogFragment {

    private static final String TAG             = "V1VideoPusherBGM";
    private static final String LOCAL_BGM_PATH  = "zhouye.mp3";
    private static final String ONLINE_BGM_PATH = "https://liteav.sdk.qcloud.com/app/res/bgm/keluodiya.mp3";
    private              EditText mEtLoop;          // 循环次数
    private              CheckBox mCbOnline;        // 在线音乐
    private              String   mTestMusicPath;
    private              SeekBar  mBGMSeekBar;
    private              TextView mTvPlayTime;      // 显示当前BGM播放进度
    private              TextView mTvDuration;      // 显示当前BGM总时长
    private              boolean  mIsOnLine;        // 是否为在线音乐
    private              long     mBGMDuration = 0; // BGM总时长
    private              int      mBGMId;

    private              TXAudioEffectManager mTXAudioEffectManager;
    private              Handler              mHandler = new Handler(Looper.getMainLooper());

    public V1VideoPusherBGMFragment(TXAudioEffectManager audioEffectManager) {
        mTXAudioEffectManager = audioEffectManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.v1livepusher_dialog_fragment);

        Context context = getActivity();
        if (context != null) {
            File sdcardDir = context.getExternalFilesDir(null);
            if (sdcardDir != null) {
                mTestMusicPath = sdcardDir.getAbsolutePath() + LOCAL_BGM_PATH;
            }
        }

        // 拷贝MP3文件到本地
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mTestMusicPath)) {
                    return;
                }
                File file = new File(mTestMusicPath);
                if (file.exists()) {
                    return;
                }
                FileUtils.copyFilesFromAssets(V1VideoPusherBGMFragment.this.getActivity(), LOCAL_BGM_PATH,
                        mTestMusicPath);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v1livepusher_video_bgm_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dismissAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mEtLoop = (EditText) view.findViewById(R.id.v1livepusher_pusher_et_bgm_loop);

        mCbOnline = ((CheckBox) view.findViewById(R.id.v1livepusher_pusher_cb_online));

        ((SeekBar) view.findViewById(R.id.v1livepusher_pusher_sb_mic))
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mTXAudioEffectManager.setVoiceCaptureVolume(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

        ((SeekBar) view.findViewById(R.id.v1livepusher_pusher_sb_bgm))
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mTXAudioEffectManager.setAllMusicVolume(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

        ((SeekBar) view.findViewById(R.id.v1livepusher_pusher_sb_bgm_pitch))
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float pitch = progress / 100.0f - 1;// pitch -1 ~ 1的范围
                        Log.i(TAG, "onProgressChanged: progress:" + progress + ", pitch:" + pitch);
                        mTXAudioEffectManager.setMusicPitch(mBGMId, pitch);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
        mTvPlayTime = ((TextView) view.findViewById(R.id.v1livepusher_start_time));
        mTvPlayTime.setText("00:00");
        mIsOnLine = mCbOnline.isChecked();
        mTvDuration = ((TextView) view.findViewById(R.id.v1livepusher_end_time));
        mCbOnline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        mBGMSeekBar = ((SeekBar) view.findViewById(R.id.v1livepusher_pusher_sb_bgm_seek));
        mBGMSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.d(TAG, "onProgressChanged, seek progress:" + progress);
                    int currentTime = (int) (progress * mBGMDuration / 100.0f);
                    mTXAudioEffectManager.seekMusicToPosInMS(mBGMId, currentTime);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        view.findViewById(R.id.v1livepusher_pusher_btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsOnLine = mCbOnline.isChecked();
                if (!mIsOnLine) {
                    File file = new File(mTestMusicPath);
                    if (!file.exists()) {
                        if (mBGMId != -1) {
                            mTXAudioEffectManager.stopPlayMusic(mBGMId);
                        }
                        Toast.makeText(v.getContext(), getString(R.string.v1livepusher_local_file_not_exists),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mBGMId = mIsOnLine ? 1 : 0;
                int loop;
                try {
                    loop = Integer.parseInt(mEtLoop.getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                    loop = 0;
                }
                TXAudioEffectManager.AudioMusicParam audioMusicParam = new TXAudioEffectManager.AudioMusicParam(mBGMId,
                        mIsOnLine ? ONLINE_BGM_PATH : mTestMusicPath);
                audioMusicParam.loopCount = loop;
                audioMusicParam.publish = true; //上行
                mTXAudioEffectManager.startPlayMusic(audioMusicParam);
                mTXAudioEffectManager.setMusicObserver(mBGMId, new TXMusicPlayObserver() {
                    @Override
                    public void onStart(int id, int errCode) {

                    }

                    @Override
                    public void onPlayProgress(int id, final long curPtsMS, final long durationMS) {
                        mBGMDuration = durationMS;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                long progress = (curPtsMS * 100 / durationMS);
                                mTvDuration.setText(timeParse(durationMS / 1000));
                                mBGMSeekBar.setProgress((int) progress);
                                mTvPlayTime.setText(timeParse(curPtsMS / 1000));
                            }
                        });
                    }

                    @Override
                    public void onComplete(int id, int errCode) {
                        Log.d(TAG, "onComplete, id: " + id + ", errCode: " + errCode);
                    }
                });
            }
        });

        view.findViewById(R.id.v1livepusher_pusher_btn_resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTXAudioEffectManager.resumePlayMusic(mBGMId);
            }
        });

        view.findViewById(R.id.v1livepusher_pusher_btn_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTXAudioEffectManager.pausePlayMusic(mBGMId);
            }
        });

        view.findViewById(R.id.v1livepusher_pusher_btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTXAudioEffectManager != null) {
                    mTXAudioEffectManager.stopPlayMusic(mBGMId);
                }
            }
        });

    }

    private String timeParse(long second) {
        String hs;
        long h = second / 3600;
        long m = (second % 3600) / 60;
        long s = (second % 3600) % 60;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }
        String ms;
        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }
        String ss;
        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        String formatTime;
        if (h > 0) {
            formatTime = hs + ":" + ms + ":" + ss;
        } else {
            formatTime = ms + ":" + ss;
        }
        return formatTime;
    }

    public void resumeBGM() {
        if (mTXAudioEffectManager != null) {
            mTXAudioEffectManager.resumePlayMusic(mBGMId);
        }
    }

    public void pauseBGM() {
        if (mTXAudioEffectManager != null) {
            mTXAudioEffectManager.pausePlayMusic(mBGMId);
        }
    }

    public void stopBGM() {
        if (mTXAudioEffectManager != null) {
            mTXAudioEffectManager.stopPlayMusic(mBGMId);
        }
    }
}
