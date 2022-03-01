package com.tencent.liteav.trtcdemo.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.liteav.demo.common.listener.OnSingleClickListener;

import java.util.Random;

public class TRTCSpeedTestActivity extends AppCompatActivity {

    private static final String TAG                       = "TRTCSpeedTestActivity";
    private static final int    STATE_SPEED_TEST_IDLE     = 0;
    private static final int    STATE_SPEED_TEST_TESTING  = 1;
    private static final int    STATE_SPEED_TEST_FINISHED = 2;
    private static final int    SDK_APP_ID                = GenerateTestUserSig.SDKAPPID;

    private TRTCCloud mTRTCCloud;
    private TextView  mTextTestResult;
    private Button    mButtonSpeedTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trtcdemo_activity_speedtest);
        initView();
    }

    private void initView() {
        mButtonSpeedTest = findViewById(R.id.btn_speedtest_start);
        mButtonSpeedTest.setOnClickListener(mSpeedTestClickListener);
        mButtonSpeedTest.setTag(STATE_SPEED_TEST_IDLE);
        mTextTestResult = findViewById(R.id.tv_speedtest_result);
        findViewById(R.id.trtc_ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseModule();
    }

    private void releaseModule() {
        if (mTRTCCloud != null) {
            mTRTCCloud.stopSpeedTest();
            mTRTCCloud.setListener(null);
            mTRTCCloud = null;
            TRTCCloud.destroySharedInstance();
        }
    }

    private void initModule() {
        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloud.setListener(mTRTCCloudListener);
    }
    
    private final View.OnClickListener mSpeedTestClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (mTRTCCloud == null) {
                initModule();
            }
            String userId = UserModelManager.getInstance().getUserModel().userId;
            String userSig = UserModelManager.getInstance().getUserModel().userSig;
            handleTriggerSpeedTest(userId, userSig);
        }
    };

    private void handleTriggerSpeedTest(String userId, String userSig) {
        Integer status = (Integer) mButtonSpeedTest.getTag();
        switch (status) {
            case STATE_SPEED_TEST_IDLE: {
                mTRTCCloud.startSpeedTest(SDK_APP_ID, userId, userSig);
                mButtonSpeedTest.setTag(STATE_SPEED_TEST_TESTING);
                mButtonSpeedTest.setText("0%");
                break;
            }
            case STATE_SPEED_TEST_FINISHED: {
                mTextTestResult.setText(null);
                mButtonSpeedTest.setTag(STATE_SPEED_TEST_IDLE);
                mButtonSpeedTest.setText(R.string.trtcdemo_start_test_speed);
                break;
            }
            case STATE_SPEED_TEST_TESTING:
            default: {
                break;
            }
        }
    }

    private final TRTCCloudListener mTRTCCloudListener = new TRTCCloudListener() {
        @Override
        public void onSpeedTest(TRTCCloudDef.TRTCSpeedTestResult currentResult, int finishedCount, int totalCount) {
            int percent = finishedCount * 100 / totalCount;
            mTextTestResult.append(currentResult.toString());
            mTextTestResult.append("\n\n");
            if (percent == 100) {
                mButtonSpeedTest.setTag(STATE_SPEED_TEST_FINISHED);
                mButtonSpeedTest.setText(R.string.trtcdemo_stop_test_speed);
            } else {
                mButtonSpeedTest.setTag(STATE_SPEED_TEST_TESTING);
                mButtonSpeedTest.setText(percent + "%");
            }
        }

        @Override
        public void onError(int i, String s, Bundle bundle) {
            mButtonSpeedTest.setTag(STATE_SPEED_TEST_IDLE);
            mButtonSpeedTest.setText(R.string.trtcdemo_speed_test_fail);
        }
    };

    private static int generateRandomInt(int bound) {
        Random random = new Random();
        int result = random.nextInt(bound);
        while (result == 0) {
            result = random.nextInt(bound);
        }
        return result;
    }

}
