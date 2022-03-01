package com.tencent.liteav.demo.livelinkmicnew.settting;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.liteav.demo.livelinkmicnew.R;
import com.tencent.live2.V2TXLivePusher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PushSetting extends DialogFragment {

    private TabLayout                mTopTl;
    private ViewPager                mContentVp;
    private List<Fragment>           mFragmentList;
    private List<String>             mTitleList;
    private PagerAdapter             mPagerAdapter;
    private String[]                 TITLE_LIST;
    private PushAudioSettingFragment mAudioSettingFragment;
    private PushVideoSettingFragment mVideoSettingFragment;
    private V2TXLivePusher           mLivePusher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.V2BaseFragmentDialogTheme);
        TITLE_LIST = new String[]{
                getString(R.string.livelinkmicnew_title_video),
                getString(R.string.livelinkmicnew_title_audio)
        };
        initFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_link_mic_new_fragment_base_tab_setting, container, false);
    }

    private void initData() {
        mFragmentList = getFragments();
        mTitleList = getTitleList();

        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
        }
        mTopTl.setupWithViewPager(mContentVp, false);
        mPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList == null ? null : mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList == null ? 0 : mFragmentList.size();
            }
        };
        mContentVp.setAdapter(mPagerAdapter);
        for (int i = 0; i < mTitleList.size(); i++) {
            TabLayout.Tab tab = mTopTl.getTabAt(i);
            if (tab != null) {
                tab.setText(mTitleList.get(i));
            }
        }
    }

    public void addFragment(Fragment fragment) {
        if (mFragmentList == null) {
            return;
        }
        mFragmentList.add(fragment);
    }

    private void initView(@NonNull final View itemView) {
        mTopTl = (TabLayout) itemView.findViewById(R.id.tl_top);
        mContentVp = (ViewPager) itemView.findViewById(R.id.vp_content);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置弹窗占据屏幕的大小
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setAttributes(windowParams);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.getWindow().setLayout(getWidth(dm), getHeight(dm));
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    public void setLivePusher(V2TXLivePusher pusher) {
        mLivePusher = pusher;
    }

    public void setSnapshotImage(Bitmap bitmap) {
        if (mVideoSettingFragment != null) {
            mVideoSettingFragment.setSnapshotImage(bitmap);
        }
    }

    private void initFragment() {
        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
            mAudioSettingFragment = new PushAudioSettingFragment();
            mAudioSettingFragment.setLivePusher(mLivePusher);
            mVideoSettingFragment = new PushVideoSettingFragment();
            mVideoSettingFragment.setLivePusher(mLivePusher);

            mFragmentList.add(mVideoSettingFragment);
            mFragmentList.add(mAudioSettingFragment);
        }
    }

    protected List<Fragment> getFragments() {
        return mFragmentList;
    }

    protected List<String> getTitleList() {
        return Arrays.asList(TITLE_LIST);
    }

    protected int getHeight(DisplayMetrics dm) {
        return (int) (dm.heightPixels * 0.7);
    }

    /**
     * 可以通过覆盖这个函数达到改变弹窗大小的效果
     *
     * @param dm DisplayMetrics
     * @return 界面宽度
     */
    protected int getWidth(DisplayMetrics dm) {
        return (int) (dm.widthPixels * 0.9);
    }

}