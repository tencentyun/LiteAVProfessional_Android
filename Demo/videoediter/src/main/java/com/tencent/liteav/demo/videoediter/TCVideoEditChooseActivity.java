package com.tencent.liteav.demo.videoediter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by liyuejiao on 2018/6/14.
 */

public class TCVideoEditChooseActivity extends FragmentActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int TYPE_SINGLE_CHOOSE = 0;
    public static final int TYPE_MULTI_CHOOSE = 1;
    public static final int TYPE_PUBLISH_CHOOSE = 2;
    public static final int TYPE_MULTI_CHOOSE_PICTURE = 3;

    private TextView mVideoTv;
    private TextView mPictureTv;
    private ViewPager mViewPager;
    private ArrayList<Fragment> mList;
    private TabFragmentPagerAdapter mAdapter;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private int mType;
    private VideoChooseFragment mVideoChooseFragment;
    private PictureChooseFragment mPictureChooseFragment;
    private LinearLayout mLlBack;
    private ImageButton mBtnLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_choose);
        initView();
        initData();
        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoChooseFragment.loadVideoList();
                    mPictureChooseFragment.loadPictureList();
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoChooseFragment.loadVideoList();
                    mPictureChooseFragment.loadPictureList();
                }
            });
        }
    }

    private void initData() {
        mType = getIntent().getIntExtra("CHOOSE_TYPE", TYPE_SINGLE_CHOOSE);
        mVideoTv.setSelected(true);
        mViewPager.setCurrentItem(0);

        mHandlerThread = new HandlerThread("LoadList");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    private void initView() {
        mVideoTv = (TextView) findViewById(R.id.tv_item_one);
        mPictureTv = (TextView) findViewById(R.id.tv_item_two);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mLlBack = (LinearLayout) findViewById(R.id.back_ll);
        mBtnLink = (ImageButton) findViewById(R.id.webrtc_link_button);

        mVideoTv.setOnClickListener(this);
        mPictureTv.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(new MyPagerChangeListener());
        mLlBack.setOnClickListener(this);
        mBtnLink.setOnClickListener(this);

        mList = new ArrayList<>();
        mVideoChooseFragment = new VideoChooseFragment();
        mPictureChooseFragment = new PictureChooseFragment();
        mList.add(mVideoChooseFragment);
        mList.add(mPictureChooseFragment);
        mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(), mList);
        mViewPager.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_item_one) {
            mVideoTv.setSelected(true);
            mPictureTv.setSelected(false);
            mViewPager.setCurrentItem(0);

        } else if (i == R.id.tv_item_two) {
            mVideoTv.setSelected(false);
            mPictureTv.setSelected(true);
            mViewPager.setCurrentItem(1);

        } else if (i == R.id.webrtc_link_button) {
            showCloudLink();

        } else if (i == R.id.back_ll) {
            finish();

        }
    }

    private void showCloudLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://cloud.tencent.com/document/product/584/9502"));
        startActivity(intent);
    }

    private class MyPagerChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    mVideoTv.setSelected(true);
                    mPictureTv.setSelected(false);
                    break;
                case 1:
                    mVideoTv.setSelected(false);
                    mPictureTv.setSelected(true);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
