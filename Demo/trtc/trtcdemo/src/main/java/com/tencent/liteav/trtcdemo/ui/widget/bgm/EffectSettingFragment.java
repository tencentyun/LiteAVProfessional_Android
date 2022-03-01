package com.tencent.liteav.trtcdemo.ui.widget.bgm;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.FileUtils;
import com.tencent.liteav.trtcdemo.model.manager.TRTCBgmManager;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.trtc.TRTCCloud;

import java.io.File;
import java.io.IOException;

/**
 * 音效管理界面
 * 默认播放 "/sdcard/trtc_test_effect/clap.aac" 和 "/sdcard/trtc_test_effect/gift_sent.aac"
 * 您可以通过 {@link TRTCBgmManager#playAudioEffect(int, String, int, boolean, double)} 播放任意的音频
 *
 * @author guanyifeng
 */
public class EffectSettingFragment extends BaseSettingFragment implements View.OnClickListener {
    private static final String TAG = "EffectSettingFragment";

    private              Button         mStopAllBtn;
    private              SeekBar        mAudioVolAllSb;
    private              EditText       mLoopTimeEt;
    private              int            mLoopTime      = 0;
    private              EffectItemView mEffectClipSe;
    private              EffectItemView mEffectGiftSe;
    private              int            mClipVol       = 100;
    private              int            mGiftVol       = 100;
    private              boolean        mClipUpload    = false;
    private              boolean        mGiftUpload    = false;
    private              TRTCBgmManager mTRTCBgmManager;
    private static final int            CLAP_EFFECT_ID = 1;
    private static final int            GIFT_EFFECT_ID = 2;

    @Override
    protected void initView(View view) {
        mStopAllBtn = (Button) view.findViewById(R.id.btn_stop_all);
        mStopAllBtn.setOnClickListener(this);
        mAudioVolAllSb = (SeekBar) view.findViewById(R.id.sb_audio_vol_all);
        mLoopTimeEt = (EditText) view.findViewById(R.id.et_loop_time);
        mEffectClipSe = (EffectItemView) view.findViewById(R.id.se_effect_clip);
        mEffectGiftSe = (EffectItemView) view.findViewById(R.id.se_effect_gift);
        mEffectClipSe.setCallback(new EffectItemView.Callback() {
            @Override
            public void onStart() {
                if (mTRTCBgmManager != null) {

                    File sdcardDir = getContext().getExternalFilesDir(null);
                    if (sdcardDir != null) {
                        mTRTCBgmManager.playAudioEffect(CLAP_EFFECT_ID,
                                sdcardDir.getAbsolutePath() + "/trtc_test_effect/clap.aac",
                                mLoopTime,
                                mEffectClipSe.isCheckUpload(),
                                mEffectClipSe.getVol());
                    }
                }
            }

            @Override
            public void onPause() {
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.pauseAudioEffect(CLAP_EFFECT_ID);
                }
            }

            @Override
            public void onResume() {
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.resumeAudioEffect(CLAP_EFFECT_ID);
                }
            }

            @Override
            public void onEnd() {
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.stopAudioEffect(CLAP_EFFECT_ID);
                }
            }

            @Override
            public void onVolChange(int gain) {
                mClipVol = gain;
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.setAudioEffectVolume(CLAP_EFFECT_ID, gain);
                }
            }

            @Override
            public void onUploadChange(boolean upload) {
                mClipUpload = upload;
            }
        });
        mEffectClipSe.setTitle(getString(R.string.trtcdemo_title_clip));
        mEffectClipSe.setProgress(mClipVol);
        mEffectClipSe.setCheckUpload(mClipUpload);

        mEffectGiftSe.setCallback(new EffectItemView.Callback() {
            @Override
            public void onStart() {
                if (mTRTCBgmManager != null) {
                    File sdcardDir = getContext().getExternalFilesDir(null);
                    if (sdcardDir != null) {
                        mTRTCBgmManager.playAudioEffect(GIFT_EFFECT_ID,
                                sdcardDir.getAbsolutePath() + "/trtc_test_effect/gift_sent.aac",
                                mLoopTime,
                                mEffectGiftSe.isCheckUpload(),
                                mEffectGiftSe.getVol());
                    }
                }
            }

            @Override
            public void onPause() {
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.pauseAudioEffect(GIFT_EFFECT_ID);
                }
            }

            @Override
            public void onResume() {
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.resumeAudioEffect(GIFT_EFFECT_ID);
                }
            }

            @Override
            public void onEnd() {
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.stopAudioEffect(GIFT_EFFECT_ID);
                }
            }

            @Override
            public void onVolChange(int gain) {
                mGiftVol = gain;
                if (mTRTCBgmManager != null) {
                    mTRTCBgmManager.setAudioEffectVolume(GIFT_EFFECT_ID, gain);
                }
            }

            @Override
            public void onUploadChange(boolean upload) {
                mGiftUpload = upload;
            }
        });
        mEffectGiftSe.setTitle(getString(R.string.trtcdemo_title_gift));
        mEffectGiftSe.setProgress(mGiftVol);
        mEffectGiftSe.setCheckUpload(mGiftUpload);
        mAudioVolAllSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mTRTCBgmManager != null) {
                    TRTCCloud.sharedInstance(getContext()).getAudioEffectManager().setAllMusicVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mLoopTimeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mLoopTime = Integer.valueOf(s.toString().trim());
                } catch (Exception e) {
                    mLoopTime = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mLoopTimeEt.setText(String.valueOf(mLoopTime));
        updateEffectItemView();
    }

    /**
     * 拷贝音效文件到本地
     */
    public void copyEffectFolder(final Context context) {
        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir == null) {
            Log.e(TAG, "sdcardDir is null");
            return;
        }
        final String localPath = sdcardDir.getAbsolutePath() + "/trtc_test_effect";
        final String assetsPath = "effect";
        File musicFolder = new File(localPath);
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            File[] listFiles = musicFolder.listFiles();
            try {
                String[] musicFilePaths = context.getAssets().list(assetsPath);
                // 将musicFiles拷贝到本地
                if (listFiles != null && listFiles.length != musicFilePaths.length) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            FileUtils.copyFilesFromAssets(context, assetsPath, localPath);
                            Log.i(TAG, "run -> copy effect assets finish.");
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTRTCBgmManager(TRTCBgmManager trtcBgmManager) {
        mTRTCBgmManager = trtcBgmManager;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_sound_effect;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_stop_all) {
            if (mTRTCBgmManager != null) {
                mTRTCBgmManager.stopAllAudioEffects();
                mEffectClipSe.onPlayComplete();
                mEffectGiftSe.onPlayComplete();
            }
        }
    }

    public void onAudioEffectFinished(int effectId, int code) {
        mTRTCBgmManager.onAudioEffectFinished(effectId);
        if (effectId == CLAP_EFFECT_ID) {
            mEffectClipSe.onPlayComplete();
        } else if (effectId == GIFT_EFFECT_ID) {
            mEffectGiftSe.onPlayComplete();
        }
    }

    private void updateEffectItemView() {
        int status = mTRTCBgmManager.getNextStatus(CLAP_EFFECT_ID);
        mEffectClipSe.restoreNextStatus(status);
        status = mTRTCBgmManager.getNextStatus(GIFT_EFFECT_ID);
        mEffectGiftSe.restoreNextStatus(status);
    }
}
