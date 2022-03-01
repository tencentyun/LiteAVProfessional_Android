package com.tencent.liteav.trtcdemo.ui.widget.bgm;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.manager.TRTCBgmManager;
import com.tencent.liteav.trtcdemo.model.manager.TRTCMixAudioManager;
import com.tencent.liteav.trtcdemo.ui.base.BaseTabDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 点击音乐按钮后弹出的窗口
 *
 * @author guanyifeng
 */
public class BgmSettingFragmentDialog extends BaseTabDialogFragment {

    public  String[]                 mtitleList;
    private List<Fragment>           mFragmentList;
    private BgmSettingFragment       mBgmSettingFragment;
    private EffectSettingFragment    mEffectSettingFragment;
    private MixExternalAudioFragment mMixExternalAudioFragment;
    private TRTCBgmManager           mTRTCBgmManager;
    private TRTCMixAudioManager      mTRTCMixAudioManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment();
    }

    private void initFragment() {
        if (mFragmentList == null) {
            mtitleList = new String[]{getString(R.string.trtcdemo_title_bgm), getString(R.string.trtcdemo_title_sound_effect), getString(R.string.trtcdemo_title_external_mix)};
            mFragmentList = new ArrayList<>();
            mBgmSettingFragment = new BgmSettingFragment();
            mMixExternalAudioFragment = new MixExternalAudioFragment();
            mEffectSettingFragment = new EffectSettingFragment();
            mEffectSettingFragment.copyEffectFolder(getActivity());
            if (mTRTCBgmManager != null) {
                mBgmSettingFragment.setTRTCBgmManager(mTRTCBgmManager);
                mEffectSettingFragment.setTRTCBgmManager(mTRTCBgmManager);
            }
            if (mTRTCMixAudioManager != null) {
                mMixExternalAudioFragment.setTRTCMixAudioManager(mTRTCMixAudioManager);
            }
            mFragmentList.add(mBgmSettingFragment);
            mFragmentList.add(mEffectSettingFragment);
            mFragmentList.add(mMixExternalAudioFragment);
        }
    }

    public void setTRTCBgmManager(TRTCBgmManager trtcBgmManager) {
        mTRTCBgmManager = trtcBgmManager;
        if (mBgmSettingFragment != null) {
            mBgmSettingFragment.setTRTCBgmManager(mTRTCBgmManager);
        }
        if (mEffectSettingFragment != null) {
            mEffectSettingFragment.setTRTCBgmManager(mTRTCBgmManager);
        }
    }

    public void setTRTCMixAudioManager(TRTCMixAudioManager trtcMixAudioManager) {
        mTRTCMixAudioManager = trtcMixAudioManager;
        if (mMixExternalAudioFragment != null) {
            mMixExternalAudioFragment.setTRTCMixAudioManager(trtcMixAudioManager);
        }
    }

    @Override
    protected List<Fragment> getFragments() {
        return mFragmentList;
    }

    @Override
    protected List<String> getTitleList() {
        return Arrays.asList(mtitleList);
    }

    @Override
    protected int getHeight(DisplayMetrics dm) {
        return (int) (dm.heightPixels * 0.6);
    }

    public void onAudioEffectFinished(int effectId, int code) {
        mEffectSettingFragment.onAudioEffectFinished(effectId, code);
    }
}
