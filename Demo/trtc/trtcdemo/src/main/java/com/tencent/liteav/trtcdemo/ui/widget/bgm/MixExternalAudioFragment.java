package com.tencent.liteav.trtcdemo.ui.widget.bgm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.FileUtils;
import com.tencent.liteav.trtcdemo.model.manager.TRTCMixAudioManager;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.PlayVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.PublishVolumeItem;
import com.tencent.trtc.TRTCCloudDef;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * BGM设置页
 *
 * @author guanyifeng
 */
public class MixExternalAudioFragment extends BaseSettingFragment implements View.OnClickListener {
    public static final int STATUS_STOP      = 0;
    public static final int STATUS_PLAY      = 1;
    public static final int STATUS_PAUSE     = 2;
    public static final int AUDIO_VOLUME_MAX = 150;

    private static final String EXTERNAL_AUDIO_FILE_NAME = "mixExternAudio.pcm";
    private              String mExternalAudioPath;

    private LinearLayout   mContentItem;
    private List<View>     mSettingItemList;
    private AbsSeekBarItem mPublishVolumeItem;
    private AbsSeekBarItem mPlayVolumeItem;
    private Button         mStartBtn;
    private Button         mEndBtn;
    private CheckBox       mLocalPlayCb;
    private CheckBox       mRemotePlayCb;
    private int            mPlayStatus = STATUS_STOP;
    private int            mPublishVol = 100;
    private int            mPlayVol    = 100;

    private TRTCMixAudioManager mTRTCMixAudioManager;
    private boolean        isAudioPlaying = false;
    private boolean        isAudioPlause  = false;
    private boolean        enablePlayout = true;
    private boolean        enablePublish = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.arg1 == STATUS_STOP) {
                mPlayStatus = STATUS_STOP;
                updateStartBtnIcon();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File sdcardDir = getContext().getExternalFilesDir(null);
        if (sdcardDir != null) {
            mExternalAudioPath = sdcardDir.getAbsolutePath() + "/" + EXTERNAL_AUDIO_FILE_NAME;
        }
        // 拷贝mp3文件到sdcard
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mExternalAudioPath)) {
                    return;
                }
                File file = new File(mExternalAudioPath);
                if (file.exists()) {
                    return;
                }
                FileUtils.copyFilesFromAssets(MixExternalAudioFragment.this.getActivity(),
                        EXTERNAL_AUDIO_FILE_NAME,
                        mExternalAudioPath);
            }
        });
    }
    
    public void setTRTCMixAudioManager(TRTCMixAudioManager trtcMixAudioManager) {
        mTRTCMixAudioManager = trtcMixAudioManager;
    }

    @Override
    protected void initView(View itemView) {
        mStartBtn = (Button) itemView.findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(this);
        mEndBtn = (Button) itemView.findViewById(R.id.btn_end);
        mEndBtn.setOnClickListener(this);
        mLocalPlayCb = itemView.findViewById(R.id.trtc_local_play_cb);
        mLocalPlayCb.setChecked(enablePlayout);
        mLocalPlayCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enablePlayout = isChecked;
                if (mTRTCMixAudioManager != null) {
                    mTRTCMixAudioManager.enableMixExternalAudioFrame(enablePublish, enablePlayout);
                }
            }
        });
        mRemotePlayCb = itemView.findViewById(R.id.trtc_remote_play_cb);
        mRemotePlayCb.setChecked(enablePublish);
        mRemotePlayCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enablePublish = isChecked;
                if (mTRTCMixAudioManager != null) {
                    mTRTCMixAudioManager.enableMixExternalAudioFrame(enablePublish, enablePlayout);
                }
            }
        });
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();

        mPublishVolumeItem = new PublishVolumeItem(getContext(), getString(R.string.trtcdemo_text_publish_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mPublishVol = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCMixAudioManager) {
                        mTRTCMixAudioManager.setMixExternalAudioVolume(mPublishVol, -1);
                    }
                }
            }
        };
        mPublishVolumeItem.setMax(AUDIO_VOLUME_MAX).setProgress(mPublishVol).setTip(String.valueOf(mPublishVol));
        mSettingItemList.add(mPublishVolumeItem);

        mPlayVolumeItem = new PlayVolumeItem(getContext(), getString(R.string.trtcdemo_text_play_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mPlayVol = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCMixAudioManager) {
                        mTRTCMixAudioManager.setMixExternalAudioVolume(-1, mPlayVol);
                    }
                }
            }
        };
        mPlayVolumeItem.setMax(AUDIO_VOLUME_MAX).setProgress(mPlayVol).setTip(String.valueOf(mPlayVol));
        mSettingItemList.add(mPlayVolumeItem);

        // 将这些view添加到对应的容器中
        for (View view : mSettingItemList) {
            mContentItem.addView(view);
        }
        updateStartBtnIcon();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_mix_external_audio;

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start) {
            if (mTRTCMixAudioManager == null) {
                return;
            }
            switch (mPlayStatus) {
                case STATUS_STOP:
                    startPlay();
                    mPlayStatus = STATUS_PLAY;
                    break;
                case STATUS_PAUSE:
                    resumePlay();
                    mPlayStatus = STATUS_PLAY;
                    break;
                case STATUS_PLAY:
                    pausePlay();
                    mPlayStatus = STATUS_PAUSE;
                    break;
                default:
                    break;
            }
            updateStartBtnIcon();
        } else if (id == R.id.btn_end) {
            if (mTRTCMixAudioManager == null) {
                return;
            }
            stopPlay();
            mPlayStatus = STATUS_STOP;
            updateStartBtnIcon();
        }
    }

    private void updateStartBtnIcon() {
        switch (mPlayStatus) {
            case STATUS_STOP:
                mStartBtn.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
                break;
            case STATUS_PLAY:
                mStartBtn.setBackgroundResource(R.drawable.trtcdemo_ic_play_pause);
                break;
            case STATUS_PAUSE:
                mStartBtn.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
                break;
            default:
                break;
        }
    }

    private void startPlay() {
        if (mTRTCMixAudioManager == null) {
            return;
        }
        isAudioPlaying = true;
        isAudioPlause = false;
        enablePublish = true;
        enablePlayout = true;

        mTRTCMixAudioManager.enableMixExternalAudioFrame(enablePublish, enablePlayout);
        mTRTCMixAudioManager.setMixExternalAudioVolume(mPublishVol, mPlayVol);
        mLocalPlayCb.setChecked(enablePublish);
        mRemotePlayCb.setChecked(enablePlayout);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                mixExternalAudio();
            }
        });
        thread.start();
    }

    public void mixExternalAudio() {
        File file = new File(mExternalAudioPath);
        if (file.exists() && file.length() > 0) {
            InputStream is = null;
            BufferedInputStream bis = null;
            DataInputStream dis = null;
            try {
                TRTCCloudDef.TRTCAudioFrame frame = new TRTCCloudDef.TRTCAudioFrame();
                frame.timestamp = 0;
                frame.sampleRate = 44100;
                frame.channel = 2;
                int bufferSize = frame.sampleRate * frame.channel * 2 / 10; //100ms数据
                frame.data = new byte[(int) bufferSize];
                is = new FileInputStream(file);
                bis = new BufferedInputStream(is);
                dis = new DataInputStream(bis);
                dis.mark((int) file.length());
                while (isAudioPlaying) {
                    int cache_size_ms = 0;
                    if (!isAudioPlause) {
                        if (dis.available() > 0) {
                            int bufferReadResult = dis.read(frame.data);
                            if (bufferReadResult == bufferSize) {
                                cache_size_ms = mTRTCMixAudioManager.mixExternalAudioFrame(frame);
                            } else if (bufferReadResult > 0) {
                                dis.reset();
                                continue;
                            } else {
                                break;
                            }
                        }
                    }
                    if (cache_size_ms > 200) {
                        Thread.sleep(100);
                    } else {
                        Thread.sleep(50);
                    }
                }
                Message message = Message.obtain();
                message.arg1 = STATUS_STOP;
                mHandler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtils.closeQuietly(dis);
                FileUtils.closeQuietly(bis);
                FileUtils.closeQuietly(is);
            }
        }
    }

    private void stopPlay() {
        if (mTRTCMixAudioManager == null) {
            return;
        }
        isAudioPlaying = false;
        mTRTCMixAudioManager.enableMixExternalAudioFrame(false, false);
    }

    private void pausePlay() {
        if (mTRTCMixAudioManager == null) {
            return;
        }
        isAudioPlause = true;
    }

    private void resumePlay() {
        if (mTRTCMixAudioManager == null) {
            return;
        }
        isAudioPlause = false;
    }
}
