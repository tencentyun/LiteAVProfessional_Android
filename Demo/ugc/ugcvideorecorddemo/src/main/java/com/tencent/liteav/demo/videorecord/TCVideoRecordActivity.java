package com.tencent.liteav.demo.videorecord;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.liteav.demo.videorecord.fragment.TCVideoRecordFragment;
import com.tencent.liteav.demo.videorecord.inter.FragmentLifeHold;

/**
 * 小视频录制界面容器
 */
public class TCVideoRecordActivity extends FragmentActivity implements FragmentLifeHold {


    TCVideoRecordFragment tcVideoRecordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowParam();
        setContentView(R.layout.ugcrecord_activity_video_record);
        showFragment();
    }

    private void showFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (null != tcVideoRecordFragment && tcVideoRecordFragment.isAdded()) {
            fragmentTransaction.show(tcVideoRecordFragment);
        } else {
            fragmentTransaction.add(R.id.fl_container, tcVideoRecordFragment = TCVideoRecordFragment.newInstance(getIntent(), this));
        }
        fragmentTransaction.commitAllowingStateLoss();

    }

    private void initWindowParam() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void finishFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(tcVideoRecordFragment);
        fragmentTransaction.commitNowAllowingStateLoss();
        finish();
    }

    @Override
    public void onBackPressed() {
        tcVideoRecordFragment.onBackPressed();
    }
}