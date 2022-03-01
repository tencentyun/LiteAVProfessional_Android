package com.tencent.liteav.trtcdemo.ui.widget.bgm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.bean.AudioEqualizationConfig;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.FileUtils;
import com.tencent.liteav.trtcdemo.model.manager.TRTCBgmManager;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioEqualizationItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.audioitem.AudioEqualizationParamItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsSeekBarItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.BGMVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.EarMonitorVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.MICVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.PlayoutVolumeItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.bgmitem.PublishVolumeItem;
import com.tencent.trtc.TRTCCloud;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * BGM设置页
 *
 * @author guanyifeng
 */
public class BgmSettingFragment extends BaseSettingFragment implements View.OnClickListener {
    public static final  int    STATUS_IDLE               = 0;
    public static final  int    STATUS_PAUSE              = 1;
    public static final  int    STATUS_RESUME             = 2;
    public static final  int    AUDIO_VOLUME_MAX          = 150;
    private static final int    AUDIO_EQUALIZATION_CUSTOM = 11;
    private static final long   BGM_DURATION_DEFAULT      = -1L;
    private static final String LOCAL_BGM_FILE_NAME       = "shuangshengdao.mp3";
    private static final String ONLINE_BMG_FILE_ADDRESS   = "https://sdk-liteav-1252463788.cos.ap-hongkong.myqcloud.com/customer/%E6%B5%8B%E8%AF%95/eb40490d-8601-4a4d-9aab-ef6c786cf7e1.mp3";
    private              String mLocalBGMPath;

    private LinearLayout          mContentItem;
    private List<View>            mSettingItemList;
    private BGMVolumeItem         mBGMVolumeItem;
    private MICVolumeItem         mMICVolumeItem;
    private AbsSeekBarItem        mEarMonitorVolumeItem;
    private AbsSeekBarItem        mPlayoutVolumeItem;
    private AbsSeekBarItem        mPublishVolumeItem;
    private AbsSeekBarItem        mEqualization31HZ;
    private AbsSeekBarItem        mEqualization62HZ;
    private AbsSeekBarItem        mEqualization125HZ;
    private AbsSeekBarItem        mEqualization250HZ;
    private AbsSeekBarItem        mEqualization500HZ;
    private AbsSeekBarItem        mEqualization1000HZ;
    private AbsSeekBarItem        mEqualization2000HZ;
    private AbsSeekBarItem        mEqualization4000HZ;
    private AbsSeekBarItem        mEqualization8000HZ;
    private AbsSeekBarItem        mEqualization16000HZ;
    private AudioEqualizationItem mEqualizationItem;
    private TextView              mTitle;
    private Button                mButtonBGMStart;
    private Button                mButtonBGMEnd;
    private SeekBar               mSeekBGMProgress;
    private TextView              mTextBGMProgress;

    private int mPlayNextStatus           = STATUS_IDLE;
    private int mPlayIndex                = 0;
    private int mBgmVolume                = 100;
    private int mMicVolume                = 100;
    private int mEarMonitorVolume         = 100;
    private int mPlayoutVolume            = 100;
    private int mPublishVolume            = 100;
    private int mCurrentEqualizationIndex = 0;
    private int mEqualizationType         = 0;

    private long mBGMDuration = BGM_DURATION_DEFAULT;

    private TRTCBgmManager          mTRTCBgmManager;
    private AudioEqualizationConfig mEqualizationConfig = new AudioEqualizationConfig();

    private TRTCCloud.BGMNotify mBGMNotify = new TRTCCloud.BGMNotify() {
        @Override
        public void onBGMStart(int errCode) {
        }

        @Override
        public void onBGMProgress(final long progress, final long duration) {
            if (mSeekBGMProgress != null) {
                mSeekBGMProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBGMProgress.setProgress((int) (progress / (float) duration * 100));
                        mTextBGMProgress.setText(String.valueOf(100 * progress / duration));
                    }
                });
            }
        }

        @Override
        public void onBGMComplete(int err) {
            if (mButtonBGMStart != null) {
                mButtonBGMStart.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
            }
            if (mSeekBGMProgress != null) {
                mSeekBGMProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mTRTCBgmManager.stopBGM();
                        mButtonBGMStart.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
                        mSeekBGMProgress.setProgress(0);
                        mTextBGMProgress.setText(String.valueOf(0));
                        mPlayNextStatus = STATUS_IDLE;
                        mBGMDuration = BGM_DURATION_DEFAULT;
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File sdcardDir = getContext().getExternalFilesDir(null);
        if (sdcardDir != null) {
            mLocalBGMPath = sdcardDir.getAbsolutePath() + LOCAL_BGM_FILE_NAME;
        }
        // 拷贝mp3文件到sdcard
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mLocalBGMPath)) {
                    return;
                }
                File file = new File(mLocalBGMPath);
                if (file.exists()) {
                    return;
                }
                FileUtils.copyFilesFromAssets(BgmSettingFragment.this.getActivity(),
                        LOCAL_BGM_FILE_NAME,
                        mLocalBGMPath);
            }
        });
    }

    public TRTCCloud.BGMNotify getBGMNotify() {
        return mBGMNotify;
    }

    public void setTRTCBgmManager(TRTCBgmManager trtcBgmManager) {
        mTRTCBgmManager = trtcBgmManager;
        initTRTCBgmManager();
    }

    private void initBGMPositionControl(View itemView) {
        mSeekBGMProgress = (SeekBar) itemView.findViewById(R.id.trtcdemo_bgm_position_sb);
        if (mTRTCBgmManager == null) {
            return;
        }
        if (mSeekBGMProgress == null) {
            return;
        }
        mSeekBGMProgress.setMax(100);
        mSeekBGMProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                if (mBGMDuration <= 0) {
                    return;
                }
                final int curPosition = progress;
                mTRTCBgmManager.setBGMPosition((int)(curPosition * mBGMDuration / 100));
                mSeekBGMProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextBGMProgress.setText(String.valueOf(curPosition));
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void initView(View itemView) {
        mTitle = (TextView) itemView.findViewById(R.id.title);
        mButtonBGMStart = (Button) itemView.findViewById(R.id.btn_start);
        mButtonBGMStart.setOnClickListener(this);
        mButtonBGMEnd = (Button) itemView.findViewById(R.id.btn_end);
        mButtonBGMEnd.setOnClickListener(this);
        mTextBGMProgress = (TextView) itemView.findViewById(R.id.tv_bgm_progress);
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mSettingItemList = new ArrayList<>();

        initBGMPositionControl(itemView);
        mBGMVolumeItem = new BGMVolumeItem(getContext(), getString(R.string.trtcdemo_title_bgm_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mBgmVolume = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCBgmManager) {
                        mTRTCBgmManager.setBGMVolume(index);
                    }
                }
            }
        };
        mBGMVolumeItem.setProgress(mBgmVolume).setMax(AUDIO_VOLUME_MAX).setTip(String.valueOf(mBgmVolume));
        mSettingItemList.add(mBGMVolumeItem);

        mMICVolumeItem = new MICVolumeItem(getContext(),  getString(R.string.trtcdemo_title_mic_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mMicVolume = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCBgmManager) {
                        mTRTCBgmManager.setMicVolumeOnMixing(index);
                    }
                }
            }
        };
        mMICVolumeItem.setProgress(mMicVolume).setMax(AUDIO_VOLUME_MAX).setTip(String.valueOf(mMicVolume));
        mSettingItemList.add(mMICVolumeItem);

        mEarMonitorVolumeItem = new EarMonitorVolumeItem(getContext(),  getString(R.string.trtcdemo_title_earmonitor_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mEarMonitorVolume = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCBgmManager) {
                        mTRTCBgmManager.setEarMonitorVolume(index);
                    }
                }
            }
        };
        mEarMonitorVolumeItem.setProgress(mEarMonitorVolume).setMax(AUDIO_VOLUME_MAX).setTip(String.valueOf(mEarMonitorVolume));
        mSettingItemList.add(mEarMonitorVolumeItem);

        mPlayoutVolumeItem = new PlayoutVolumeItem(getContext(),  getString(R.string.trtcdemo_title_playout_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mPlayoutVolume = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCBgmManager) {
                        mTRTCBgmManager.setPlayoutVolume(index);
                    }
                }
            }
        };
        mPlayoutVolumeItem.setProgress(mPlayoutVolume).setMax(AUDIO_VOLUME_MAX).setTip(String.valueOf(mPlayoutVolume));
        mSettingItemList.add(mPlayoutVolumeItem);

        mPublishVolumeItem = new PublishVolumeItem(getContext(),  getString(R.string.trtcdemo_title_publish_volume)) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (fromUser) {
                    mPublishVolume = index;
                    setTip(String.valueOf(index));
                    if (null != mTRTCBgmManager) {
                        mTRTCBgmManager.setPublishVolume(index);
                    }
                }
            }
        };
        mPublishVolumeItem.setProgress(mPublishVolume).setMax(AUDIO_VOLUME_MAX).setTip(String.valueOf(mPublishVolume));
        mSettingItemList.add(mPublishVolumeItem);

        initEqualizationParamsView();

        // 将这些view添加到对应的容器中
        for (View view : mSettingItemList) {
            mContentItem.addView(view);
        }
        updateStartBtnIcon();
    }

    private void initEqualizationParamsView() {
        mEqualizationItem = new AudioEqualizationItem(new AudioEqualizationItem.OnSelectListener() {
            @Override
            public void onSelected(int index, String str) {
                mCurrentEqualizationIndex = index;
                mEqualizationType = mEqualizationItem.getSelected();
                mTRTCBgmManager.setEqualizationParam(mEqualizationType, mEqualizationConfig);
            }
        }, getContext(), getString(R.string.trtcdemo_title_audio_equalization_setting),
                getResources().getStringArray(R.array.trtcdemo_audio_equalization_type));
        mEqualizationItem.setSelect(mCurrentEqualizationIndex);
        mSettingItemList.add(mEqualizationItem);

        String[] itemNames = getResources().getStringArray(R.array.trtcdemo_audio_equalization_custom_param);

        mEqualization31HZ = new AudioEqualizationParamItem(getContext(), itemNames[0]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain31HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization31HZ.setProgress(mEqualizationConfig.getGain31HZ());
        mSettingItemList.add(mEqualization31HZ);

        mEqualization62HZ = new AudioEqualizationParamItem(getContext(),  itemNames[1]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain62HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization62HZ.setProgress(mEqualizationConfig.getGain62HZ());
        mSettingItemList.add(mEqualization62HZ);

        mEqualization125HZ = new AudioEqualizationParamItem(getContext(),  itemNames[2]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain125HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization125HZ.setProgress(mEqualizationConfig.getGain125HZ());
        mSettingItemList.add(mEqualization125HZ);

        mEqualization250HZ = new AudioEqualizationParamItem(getContext(),  itemNames[3]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain250HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization250HZ.setProgress(mEqualizationConfig.getGain250HZ());
        mSettingItemList.add(mEqualization250HZ);

        mEqualization500HZ = new AudioEqualizationParamItem(getContext(),  itemNames[4]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain500HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization500HZ.setProgress(mEqualizationConfig.getGain500HZ());
        mSettingItemList.add(mEqualization500HZ);

        mEqualization1000HZ = new AudioEqualizationParamItem(getContext(),  itemNames[5]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain1000HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization1000HZ.setProgress(mEqualizationConfig.getGain1000HZ());
        mSettingItemList.add(mEqualization1000HZ);

        mEqualization2000HZ = new AudioEqualizationParamItem(getContext(),  itemNames[6]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain2000HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization2000HZ.setProgress(mEqualizationConfig.getGain2000HZ());
        mSettingItemList.add(mEqualization2000HZ);

        mEqualization4000HZ = new AudioEqualizationParamItem(getContext(),  itemNames[7]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain4000HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization4000HZ.setProgress(mEqualizationConfig.getGain4000HZ());
        mSettingItemList.add(mEqualization4000HZ);

        mEqualization8000HZ = new AudioEqualizationParamItem(getContext(),  itemNames[8]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain8000HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization8000HZ.setProgress(mEqualizationConfig.getGain8000HZ());
        mSettingItemList.add(mEqualization8000HZ);

        mEqualization16000HZ = new AudioEqualizationParamItem(getContext(),  itemNames[9]) {
            @Override
            public void onSeekBarChange(int index, boolean fromUser) {
                super.onSeekBarChange(index, fromUser);
                if (!fromUser) {
                    return;
                }
                mEqualizationConfig.setGain16000HZ(index);
                setEqualizationCustomParam();
            }
        };
        mEqualization16000HZ.setProgress(mEqualizationConfig.getGain16000HZ());
        mSettingItemList.add(mEqualization16000HZ);
    }

    private void setEqualizationCustomParam() {
        if (mEqualizationType == AUDIO_EQUALIZATION_CUSTOM) {
            mTRTCBgmManager.setEqualizationParam(AUDIO_EQUALIZATION_CUSTOM, mEqualizationConfig);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_bgm;

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start) {
            if (mTRTCBgmManager == null) {
                return;
            }
            switch (mPlayNextStatus) {
                case STATUS_IDLE:
                    mPlayIndex = (mPlayIndex + 1) % 2;
                    if (mPlayIndex == 0) {
                        mBGMDuration = mTRTCBgmManager.getBGMDuration(ONLINE_BMG_FILE_ADDRESS);
                        mTRTCBgmManager.playBGM(ONLINE_BMG_FILE_ADDRESS, 1, mBgmVolume, mMicVolume, mBGMNotify);
                    } else if (!TextUtils.isEmpty(mLocalBGMPath)) {
                        mBGMDuration = mTRTCBgmManager.getBGMDuration(mLocalBGMPath);
                        mTRTCBgmManager.playBGM(mLocalBGMPath, 1, mBgmVolume, mMicVolume, mBGMNotify);
                    }
                    mPlayNextStatus = STATUS_PAUSE;
                    break;
                case STATUS_PAUSE:
                    mTRTCBgmManager.pauseBGM();
                    mPlayNextStatus = STATUS_RESUME;
                    break;
                case STATUS_RESUME:
                    mTRTCBgmManager.resumeBGM();
                    mPlayNextStatus = STATUS_PAUSE;
                    break;
                default:
                    break;
            }
            updateStartBtnIcon();
        } else if (id == R.id.btn_end) {
            if (mTRTCBgmManager == null) {
                return;
            }
            mTRTCBgmManager.stopBGM();
            mButtonBGMStart.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
            mSeekBGMProgress.setProgress(0);
            mTextBGMProgress.setText(String.valueOf(0));
            mPlayNextStatus = STATUS_IDLE;
        }
    }

    private void updateStartBtnIcon() {
        switch (mPlayNextStatus) {
            case STATUS_IDLE:
                mButtonBGMStart.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
                break;
            case STATUS_PAUSE:
                mButtonBGMStart.setBackgroundResource(R.drawable.trtcdemo_ic_play_pause);
                break;
            case STATUS_RESUME:
                mButtonBGMStart.setBackgroundResource(R.drawable.trtcdemo_ic_play_start);
                break;
            default:
                break;
        }
    }

    private void initTRTCBgmManager() {
        if (null == mTRTCBgmManager) {
            return;
        }
        mTRTCBgmManager.setBGMVolume(mBgmVolume);
        mTRTCBgmManager.setEarMonitorVolume(mEarMonitorVolume);
        mTRTCBgmManager.setMicVolumeOnMixing(mMicVolume);
        mTRTCBgmManager.setPlayoutVolume(mPlayoutVolume);
        mTRTCBgmManager.setPublishVolume(mPublishVolume);
        mTRTCBgmManager.setEqualizationParam(mEqualizationType, mEqualizationConfig);
    }
}
