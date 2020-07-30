package com.tencent.liteav.demo.videoediter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videoediter.common.TCVideoEditerListAdapter;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.module.picker.data.PickerManagerKit;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.utils.VideoChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云"短视频导入"功能使用参考 Demo
 * <p>
 * 有"快速导入" 和"全功能导入" 两种视频导入方式
 * <p>
 * 快速导入：
 * 1、优点：导入视频速度快
 * 2、缺点：不具有"视频倒放"、"视频重复"两个时间特效、不具有拖动缩略图进行视频"单帧预览"功能
 * <p>
 * 全功能导入：
 * 1、优点：具有"视频倒放"、"视频重复"两个时间特效、不具有拖动缩略图进行视频"单帧预览"功能
 * 2、缺点：导入视频相对于"快速导入"速度慢
 */
public class TCVideoPickerActivity extends FragmentActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "TCVideoPickerActivity";

    private RecyclerView mRecyclerView;
    private Button       mButtonFullImport;      // 全功能导入
    private Button       mButtonFastImport;      // 快速导入
    private ImageButton  mButtonLink;
    private LinearLayout mLayoutBack;

    private TCVideoEditerListAdapter mAdapter;
    private HandlerThread            mHandlerThread;
    private Handler                  mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcedit_activity_video_choose);
        initView();
        initData();
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (mPermissionList.isEmpty()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadVideoList();
                    }
                });
            } else {
                //存在未允许的权限
                String[] permissionsArr = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissionsArr, 1);
            }
        } else {
            //FIXBUG:Android6.0以下不需要动态获取权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadVideoList();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean forbidden = false;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                forbidden = true;
            }
        }
        if (!forbidden) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadVideoList();
                }
            });
        }
    }

    public void loadVideoList() {
        ArrayList<TCVideoFileInfo> fileInfoArrayList = PickerManagerKit.getInstance(this).getAllVideo();

        Message msg = new Message();
        msg.obj = fileInfoArrayList;
        mMainHandler.sendMessage(msg);
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<TCVideoFileInfo> fileInfoArrayList = (ArrayList<TCVideoFileInfo>) msg.obj;
            mAdapter.addAll(fileInfoArrayList);
        }
    };

    private void initData() {
        mHandlerThread = new HandlerThread("LoadList");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    private void initView() {
        mButtonFullImport = (Button) findViewById(R.id.btn_full_import);
        mButtonFullImport.setOnClickListener(this);
        mButtonFastImport = (Button) findViewById(R.id.btn_fast_import);
        mButtonFastImport.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mAdapter = new TCVideoEditerListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setMultiplePick(false);

        mLayoutBack = (LinearLayout) findViewById(R.id.ll_back);
        mButtonLink = (ImageButton) findViewById(R.id.ib_webrtc_link);

        mLayoutBack.setOnClickListener(this);
        mButtonLink.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        boolean fastImport = false;
        if (v.getId() == R.id.btn_fast_import) {
            // 快速导入
            fastImport = true;
            doSelect(fastImport);
        } else if (v.getId() == R.id.btn_full_import) {
            // 全功能导入
            fastImport = false;
            doSelect(fastImport);
        } else if (i == R.id.ib_webrtc_link) {
            showCloudLink();
        } else if (i == R.id.ll_back) {
            finish();
        }
    }

    private void showCloudLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://cloud.tencent.com/document/product/584/9502"));
        startActivity(intent);
    }

    private void doSelect(boolean fastImport) {
        TCVideoFileInfo fileInfo = mAdapter.getSingleSelected();
        if (fileInfo == null) {
            TXCLog.d(TAG, "select file null");
            return;
        }
        if (VideoChecker.isVideoDamaged(this, fileInfo)) {
            VideoChecker.showErrorDialog(this, "该视频文件已经损坏");
            return;
        }
//        if (fastImport) {
//             快速导入
//            Intent intent = new Intent(this, TCVideoEditerActivity.class);
//            intent.putExtra(UGCKitConstants.VIDEO_PATH, fileInfo.getFilePath());
//            startActivity(intent);
//        } else {
        // 全功能导入
        Intent intent = new Intent(this, TCVideoCutActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, fileInfo.getFilePath());
        intent.putExtra(UGCKitConstants.VIDEO_URI, fileInfo.getFileUri().toString());
        startActivity(intent);
//        }
    }
}
