package com.tencent.liteav.demo.videouploader.videopublish.pic;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.videouploader.R;
import com.tencent.liteav.demo.videouploader.common.view.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videouploader.videopublish.server.PublishSigListener;
import com.tencent.liteav.demo.videouploader.videopublish.server.VideoDataMgr;
import com.tencent.liteav.demo.videouploader.videoupload.TXUGCPublish;
import com.tencent.liteav.demo.videouploader.videoupload.TXUGCPublishTypeDef;
import com.tencent.rtmp.TXLog;

import java.io.File;

/**
 * Created by hanszhli on 2019/4/17.
 */

public class TCPicturePublishActivity extends FragmentActivity implements View.OnClickListener {
    private final String TAG = "TCVideoPublishActivity";
    private String mSignature;
    private String mPicturePath;
    private LinearLayout mLlBack;
    private Button mBtnPublish;
    private ImageView mIvPicture;
    private ImageView mIvNetworkPic;
    private VideoWorkProgressFragment mWorkLoadingProgress; // 进度
    private TXUGCPublish mTXugcPublish;
    private boolean isCancelPublish;
    private PublishSigListener mPublishSiglistener;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_publish);
        initView();
        initData();
    }

    private void initData() {
        mTXugcPublish = new TXUGCPublish(this.getApplicationContext(), "customID");
        initListener();
    }

    private void initListener() {
        mPublishSiglistener = new PublishSigListener() {
            @Override
            public void onSuccess(String signatureStr) {
                mSignature = signatureStr;
                publish();
            }

            @Override
            public void onFail(final int errCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWorkLoadingProgress != null && mWorkLoadingProgress.isAdded()) {
                            mWorkLoadingProgress.dismiss();
                        }
                        Toast.makeText(TCPicturePublishActivity.this, "err code = " + errCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        VideoDataMgr.getInstance().setPublishSigListener(mPublishSiglistener);
    }

    private void initView() {
        mBtnPublish = (Button) findViewById(R.id.btn_publish);
        mLlBack = (LinearLayout) findViewById(R.id.back_ll);
        mIvPicture = (ImageView) findViewById(R.id.iv_picture);
        mIvNetworkPic = (ImageView) findViewById(R.id.iv_picture_network);
        mIvPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 1004);
            }
        });
        mBtnPublish.setOnClickListener(this);
        mLlBack.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1004 && data != null) {
            Uri selectedImage = data.getData();

            String[] mediaColumns = new String[]{
                    MediaStore.Images.Media.DATA,
            };
            ContentResolver contentResolver = getContentResolver();

            Cursor cursor = contentResolver.query(selectedImage, mediaColumns, null, null, null);
            if (cursor != null && selectedImage != null) {
                cursor.moveToFirst();
                mPicturePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                cursor.close();
                Glide.with(this).load(selectedImage).fitCenter().into(mIvPicture);
            } else {
                Toast.makeText(TCPicturePublishActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(TCPicturePublishActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_publish) {
            publishVideo();
        } else if (id == R.id.back_ll) {
            finish();
        }
    }

    private void publishVideo() {
        if (mWorkLoadingProgress == null) {
            initWorkLoadingProgress();
        }
        mWorkLoadingProgress.setProgress(0);
        mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");
        VideoDataMgr.getInstance().getPublishSig();
        isCancelPublish = false;
    }


    private void publish() {
        if (TextUtils.isEmpty(mPicturePath) || !(new File(mPicturePath)).exists()) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TCPicturePublishActivity.this, "图片路径文件不存在", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        mTXugcPublish.setListener(new TXUGCPublishTypeDef.ITXMediaPublishListener() {
            @Override
            public void onMediaPublishProgress(long uploadBytes, long totalBytes) {
                TXLog.d(TAG, "onPublishProgress [" + uploadBytes + "/" + totalBytes + "]");
                if(isCancelPublish){
                    return;
                }
                mWorkLoadingProgress.setProgress((int) ((uploadBytes * 100) / totalBytes));
            }

            @Override
            public void onMediaPublishComplete(TXUGCPublishTypeDef.TXMediaPublishResult result) {
                TXLog.d(TAG, "onPublishComplete [" + result.retCode + "/" + (result.retCode == 0 ? result.mediaURL : result.descMsg) + "]");
                if (mWorkLoadingProgress != null && mWorkLoadingProgress.isAdded()) {
                    mWorkLoadingProgress.dismiss();
                }

                if(isCancelPublish){
                    return;
                }
                if (result.retCode == TXUGCPublishTypeDef.PUBLISH_RESULT_OK) {
                    Toast.makeText(TCPicturePublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onPublishComplete: result = " + result.mediaURL);
                    Glide.with(TCPicturePublishActivity.this).load(result.mediaURL).fitCenter().into(mIvNetworkPic);
                } else {
                    if (result.descMsg.contains("java.net.UnknownHostException") || result.descMsg.contains("java.net.ConnectException")) {
                        Toast.makeText(TCPicturePublishActivity.this, "网络连接断开，媒体上传失败" + result.descMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TCPicturePublishActivity.this, "发布失败，errCode = " + result.retCode + ", msg = " + result.descMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        TXUGCPublishTypeDef.TXMediaPublishParam param = new TXUGCPublishTypeDef.TXMediaPublishParam();
        param.signature = mSignature;
        param.mediaPath = mPicturePath;
        param.fileName = "测试图片";
        mTXugcPublish.publishMedia(param);
    }

    private void initWorkLoadingProgress() {
        if (mWorkLoadingProgress == null) {
            mWorkLoadingProgress = VideoWorkProgressFragment.newInstance("发布中...");
            mWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTXugcPublish != null) {
                        mTXugcPublish.canclePublish();
                        isCancelPublish = true;
                        mWorkLoadingProgress.setProgress(0);
                        mWorkLoadingProgress.dismiss();
                    }
                }
            });
        }
        mWorkLoadingProgress.setProgress(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoDataMgr.getInstance().setPublishSigListener(null);
    }
}
