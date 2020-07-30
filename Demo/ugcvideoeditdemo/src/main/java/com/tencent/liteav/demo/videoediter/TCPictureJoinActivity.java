package com.tencent.liteav.demo.videoediter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitPictureJoin;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.picturetransition.IPictureJoinKit;
import java.util.ArrayList;

/**
 * 图片生成视频类，用于设置图片转场动画，并生成一个视频，返回生成的视频路径
 */
public class TCPictureJoinActivity extends FragmentActivity {
    private static final String TAG = "TCPictureJoinActivity";

    private UGCKitPictureJoin mUGCKitPictureJoin;
    private ArrayList<String> mPicPathList;
    private IPictureJoinKit.OnPictureJoinListener mOnPictureListener = new IPictureJoinKit.OnPictureJoinListener() {

        @Override
        public void onPictureJoinCompleted(UGCKitResult ugcKitResult) {
            startPreviewActivity(ugcKitResult);
        }

        @Override
        public void onPictureJoinCanceled() {
            /**
             * 生成视频操作取消
             */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcedit_picture_join_layout);

        mUGCKitPictureJoin = (UGCKitPictureJoin) findViewById(R.id.picture_transition);
        /**
         *  获取从图片路径集合，并设置给UGCKit {@link TCPictureJoin#setInputPictureList(ArrayList)}
         */
        mPicPathList = getIntent().getStringArrayListExtra(UGCKitConstants.INTENT_KEY_MULTI_PIC_LIST);
        mUGCKitPictureJoin.setInputPictureList(mPicPathList);
        mUGCKitPictureJoin.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUGCKitPictureJoin.stopPlay();
                finish();
            }
        });
    }

    /**
     * 跳转到视频预览界面
     */
    private void startPreviewActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(this, TCEditPreviewActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        intent.putExtra(UGCKitConstants.VIDEO_COVERPATH, ugcKitResult.coverPath);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        mUGCKitPictureJoin.stopPlay();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitPictureJoin.setOnPictureJoinListener(mOnPictureListener);
        mUGCKitPictureJoin.resumePlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitPictureJoin.pausePlay();
        mUGCKitPictureJoin.setOnPictureJoinListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUGCKitPictureJoin.release();
    }
}
