package com.tencent.liteav.demo.videojoiner.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videojoiner.R;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.module.picker.data.PickerManagerKit;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.VideoChecker;

import java.util.ArrayList;

public class TCVideoJoinChooseActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "TCVideoJoinChooseActivity";

    public static final int TYPE_MULTI_CHOOSE = 1;              // 视频拼接
    public static final int TYPE_PUBLISH_CHOOSE = 2;            // 视频上传
    public static final int TYPE_MULTI_CHOOSE_PICTURE = 3;      // 图片转场

    private Button       mButtonOk;
    private ImageButton  mButtonLink;
    private RecyclerView mRecyclerView;
    private TextView     mTextRight;
    private TextView     mTextTitle;

    private TCVideoEditerListAdapter mAdapter;

    private int           mType;
    private Handler       mHandler;
    private HandlerThread mHandlerThread;

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<TCVideoFileInfo> fileInfoArrayList = (ArrayList<TCVideoFileInfo>) msg.obj;
            mAdapter.addAll(fileInfoArrayList);
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ugcjoin_activity_ugc_video_list);

        mHandlerThread = new HandlerThread("LoadList");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mType = getIntent().getIntExtra("TYPE", TYPE_MULTI_CHOOSE);

        init();
        if (mType == TYPE_MULTI_CHOOSE_PICTURE) {
            loadPictureList();
        } else {
            loadVideoList();
        }
    }

    private void loadPictureList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<TCVideoFileInfo> fileInfoArrayList = PickerManagerKit.getInstance(TCVideoJoinChooseActivity.this).getAllPictrue();

                    Message msg = new Message();
                    msg.obj = fileInfoArrayList;
                    mMainHandler.sendMessage(msg);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mHandlerThread.quit();
        mHandlerThread = null;
        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(null);

        mButtonOk.setOnClickListener(null);

        mTextRight.setOnClickListener(null);
        super.onDestroy();
    }

    private void loadVideoList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<TCVideoFileInfo> fileInfoArrayList = PickerManagerKit.getInstance(TCVideoJoinChooseActivity.this).getAllVideo();

                    Message msg = new Message();
                    msg.obj = fileInfoArrayList;
                    mMainHandler.sendMessage(msg);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mType == TYPE_MULTI_CHOOSE_PICTURE) {
                loadPictureList();
            } else {
                loadVideoList();
            }
        }
    }

    private void init() {
        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(this);

        mButtonOk = (Button) findViewById(R.id.btn_ok);
        mButtonOk.setOnClickListener(this);
        mButtonLink = (ImageButton) findViewById(R.id.webrtc_link_button);
        mButtonLink.setOnClickListener(this);

        mTextRight = (TextView) findViewById(R.id.tv_right);
        mTextRight.setOnClickListener(this);

        mTextTitle = (TextView) findViewById(R.id.title_tv);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mAdapter = new TCVideoEditerListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        if (mType == TYPE_PUBLISH_CHOOSE) {
            mAdapter.setMultiplePick(false);
        } else {
            // 多选,排序
            mAdapter.setMultiplePick(true);
        }

        if (mType == TYPE_PUBLISH_CHOOSE) {
            mTextRight.setText("回放");
            mTextRight.setVisibility(View.VISIBLE);
        }

        if (mType == TYPE_MULTI_CHOOSE_PICTURE) {
            mTextTitle.setText("选择图片");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            doSelect();

        } else if (id == R.id.back_ll) {
            finish();

        } else if (id == R.id.tv_right) {
            if (mType == TYPE_PUBLISH_CHOOSE) {
                Intent intent = new Intent();
                intent.setAction("com.tencent.liteav.demo.play.action.float.click");
                intent.putExtra(UGCKitConstants.PLAYER_DEFAULT_VIDEO, false);
                startActivity(intent);
            }

        } else if (id == R.id.webrtc_link_button) {
            showCloudLink();

        }
    }

    private void showCloudLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (mType == TYPE_PUBLISH_CHOOSE) {
            intent.setData(Uri.parse("https://cloud.tencent.com/document/product/584/15535"));
        } else if (mType == TYPE_MULTI_CHOOSE_PICTURE) {
            intent.setData(Uri.parse("https://cloud.tencent.com/document/product/584/9502#16.-.E5.9B.BE.E7.89.87.E7.BC.96.E8.BE.91"));
        } else {
            intent.setData(Uri.parse("https://cloud.tencent.com/document/product/584/9503"));
        }
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Glide.with(this).pauseRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(this).resumeRequests();
    }

    private void doSelect() {
        if (mType == TYPE_MULTI_CHOOSE) {
            // ugc精简版本没有TCVideoJoinerActivity
            Intent intent = new Intent();
            intent.setAction("com.tencent.liteav.demo.videojoiner");
            ArrayList<TCVideoFileInfo> videoFileInfos = mAdapter.getMultiSelected();
            if (videoFileInfos == null || videoFileInfos.size() == 0) {
                TXCLog.d(TAG, "select file null");
                return;
            }
            if (videoFileInfos.size() < 2) {
                Toast.makeText(this, "必须选择两个以上视频文件", Toast.LENGTH_SHORT).show();
                return;
            }
            if (VideoChecker.isVideoDamaged(this, videoFileInfos)) {
                VideoChecker.showErrorDialog(this, "包含已经损坏的视频文件");
                return;
            }
            intent.putExtra(UGCKitConstants.INTENT_KEY_MULTI_CHOOSE, videoFileInfos);
            startActivity(intent);
        } else if (mType == TYPE_MULTI_CHOOSE_PICTURE) {
            // ugc精简版本没有TCVideoEditerActivity
            Intent intent = new Intent();
            intent.setAction("com.tencent.liteav.demo.picturejoin");
            ArrayList<TCVideoFileInfo> pictureList = mAdapter.getInOrderMultiSelected();
            if (pictureList == null || pictureList.size() == 0) {
                TXCLog.d(TAG, "select file null");
                return;
            }
            if (pictureList.size() < 3) {
                Toast.makeText(this, "必须选择三个以上图片", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<String> picturePathList = new ArrayList<String>();
            for (TCVideoFileInfo info : pictureList) {
                if (Build.VERSION.SDK_INT >= 29) {
                    picturePathList.add(info.getFileUri().toString());
                } else {
                    picturePathList.add(info.getFilePath());
                }
            }
            intent.putStringArrayListExtra(UGCKitConstants.INTENT_KEY_MULTI_PIC_LIST, picturePathList);
            startActivity(intent);
        } else if (mType == TYPE_PUBLISH_CHOOSE) {
            Intent intent = new Intent();
            intent.setAction("com.tencent.liteav.demo.videocompress");
            TCVideoFileInfo fileInfo = mAdapter.getSingleSelected();
            if (fileInfo == null) {
                TXCLog.d(TAG, "select file null");
                return;
            }
            if (VideoChecker.isVideoDamaged(this, fileInfo)) {
                VideoChecker.showErrorDialog(this, "该视频文件已经损坏");
                return;
            }
            intent.putExtra(UGCKitConstants.VIDEO_URI, fileInfo.getFileUri().toString());
            intent.putExtra(UGCKitConstants.VIDEO_PATH, fileInfo.getFilePath());
            startActivity(intent);
        }
        finish();
    }

}
