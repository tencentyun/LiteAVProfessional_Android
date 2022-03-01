package com.tencent.liteav.trtcdemo.ui.base;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import com.tencent.liteav.trtcdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象具有TAB切换功能的DialogFragment
 * <p>
 * 抽象设置面板UI，主要有两块：
 * - 顶部切换TAB{@link BaseTabDialogFragment#mTabLayout}
 * - 切换之后显示页面容器{@link BaseTabDialogFragment#mContentVp}
 * <p>
 * 抽象功能：
 * - 设置TAB列表数据{@link BaseTabDialogFragment#getTitleList()}
 * - 设置页面列表数据{@link BaseTabDialogFragment#getFragments()}
 *
 * @author : xander
 * @date : 2021/5/25
 */
public abstract class BaseTabDialogFragment extends BaseDialogFragment {
    private TabLayout      mTabLayout;
    private ViewPager      mContentVp;
    private List<Fragment> mFragmentList;
    private List<String>   mTitleList;
    private PagerAdapter   mPagerAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initData() {
        mFragmentList = getFragments();
        mTitleList = getTitleList();

        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
        }
        mTabLayout.setupWithViewPager(mContentVp, false);
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
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
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

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_base_tab_setting;
    }

    private void initView(@NonNull final View itemView) {
        mTabLayout = (TabLayout) itemView.findViewById(R.id.tl_top);
        mContentVp = (ViewPager) itemView.findViewById(R.id.vp_content);
    }

    /**
     * @return 这里返回对应的fragment
     */
    protected abstract List<Fragment> getFragments();


    /**
     * @return 这里返回对应的标题列表
     */
    protected abstract List<String> getTitleList();
}
