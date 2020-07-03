package com.tencent.liteav.demo.videouploader.videopublish;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videouploader.R;
import com.tencent.liteav.demo.videouploader.common.utils.TCConstants;
import com.tencent.liteav.demo.videouploader.common.view.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videouploader.common.utils.DialogUtil;
import com.tencent.liteav.demo.videouploader.common.utils.TCEditerUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TCCompressActivity extends FragmentActivity {
    private final String TAG = "TCCompressActivity";
    private TXVideoEditer mTXVideoEditer;
    private LinearLayout llBack;
    private RadioGroup rgVideoResolution;
    private RadioButton rbVideoNotCompress, rbVideoResolution360p, rbVideoResolution480p, rbVideoResolution540p, rbVideoResolution720p;
    private EditText mEtCompressBitrate;
    private Button mBtnStartCompress;
    private int mVideoResolution;
    private String mInputSource;
    private String mOutputPath;
    private TXVideoEditer.TXVideoGenerateListener mTXVideoGenerateListener;
    private VideoWorkProgressFragment mWorkLoadingProgress; // 生成视频的等待框
    private TXVideoEditConstants.TXVideoInfo mTXVideoInfo;
    private int mBiteRate; // 码率
    private boolean mCompressing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        initView();

        initData();
    }

    private void initData() {
        mTXVideoEditer = new TXVideoEditer(getApplicationContext());
        mVideoResolution = -1;
        String path = getIntent().getStringExtra(TCConstants.VIDEO_EDITER_PATH);
        String uri = getIntent().getStringExtra(TCConstants.VIDEO_EDITER_URI);

        // Android 10（Q）Google官方尚未强制启用 App 沙箱运行，当且仅当 targetSDK 为 29 的时候才会在沙箱下运行
        // Google官方预计 2020 年在 Android 11（R）强制启动沙箱机制，届时所有 app 无论 targetSDK 是否为 29，都运行在沙箱机制。
        // 因此为了您的 app 保持较高兼容性，推荐您在系统版本为 Android 10或以上的设备，都使用 Google 官方推荐的 uri 统一资源定位符的方式传递给 SDK。
        if (Build.VERSION.SDK_INT >= 29) {
            mInputSource = uri;
        } else {
            mInputSource = path;
        }
        mOutputPath = TCEditerUtil.generateVideoPath(this);
        int ret = mTXVideoEditer.setVideoPath(mInputSource);
        if (ret != 0) {
            if (ret == TXVideoEditConstants.ERR_SOURCE_NO_TRACK) {
                DialogUtil.showDialog(this, "视频处理失败", "不支持的视频格式", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_AUDIO_FORMAT) {
                DialogUtil.showDialog(this, "视频处理失败", "暂不支持非单双声道的视频格式", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            return;
        }
        mTXVideoInfo = TXVideoInfoReader.getInstance(this).getVideoFileInfo(mInputSource);
        initListener();
    }

    private void initListener() {
        mTXVideoGenerateListener = new TXVideoEditer.TXVideoGenerateListener() {
            @Override
            public void onGenerateProgress(final float progress) {
                TXCLog.i(TAG, "onGenerateProgress = " + progress);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWorkLoadingProgress.setProgress((int) (progress * 100));
                    }
                });
            }

            @Override
            public void onGenerateComplete(final TXVideoEditConstants.TXGenerateResult result) {
                TXCLog.i(TAG, "onGenerateComplete result retCode = " + result.retCode);
                mCompressing = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWorkLoadingProgress != null && mWorkLoadingProgress.isAdded()) {
                            mWorkLoadingProgress.dismiss();
                        }
                        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
                            // 生成成功
                            if (!TextUtils.isEmpty(mOutputPath)) {
                                startPublishActivity(mOutputPath);
                            }
                        } else {
                            Toast.makeText(TCCompressActivity.this, result.descMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        mTXVideoEditer.setVideoGenerateListener(mTXVideoGenerateListener);
    }

    private void initView() {
        rgVideoResolution = (RadioGroup) findViewById(R.id.rg_video_resolution);
        rbVideoNotCompress = (RadioButton) findViewById(R.id.rb_video_compress_none);
        rbVideoResolution360p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_360p);
        rbVideoResolution480p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_480p);
        rbVideoResolution540p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_540p);
        rbVideoResolution720p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_720p);

        mEtCompressBitrate = (EditText) findViewById(R.id.et_compress_bitrate);

        rgVideoResolution.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbVideoNotCompress.getId()) {
                    mVideoResolution = -1;
                    mEtCompressBitrate.setText("");
                } else if (checkedId == rbVideoResolution360p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_360P;
                    mEtCompressBitrate.setText("2400");
                } else if (checkedId == rbVideoResolution480p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_480P;
                    mEtCompressBitrate.setText("2400");
                } else if (checkedId == rbVideoResolution540p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_540P;
                    mEtCompressBitrate.setText("6500");
                } else if (checkedId == rbVideoResolution720p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_720P;
                    mEtCompressBitrate.setText("9600");
                }
            }
        });

        mBtnStartCompress = (Button) findViewById(R.id.btn_compress_ok);
        mBtnStartCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCompressVideo();
            }
        });

        llBack = (LinearLayout) findViewById(R.id.back_ll);
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void startCompressVideo() {
        if (mVideoResolution == -1) {
            // Android 10（Q）Google官方尚未强制启用 App 沙箱运行，当且仅当 targetSDK 为 29 的时候才会在沙箱下运行
            // Google官方预计 2020 年在 Android 11（R）强制启动沙箱机制，届时所有 app 无论 targetSDK 是否为 29，都运行在沙箱机制。
            // 为了您的日后的兼容性着想，建议按照以下流程上传视频：

            // 1. 若上传视频为SD卡视频（如，图库文件），视频上传目前不支持 Uri 统一资源定位符方式进行上传。因此上传图库文件（不在app私有目录下）需要拷贝视频文件到本地目录。
            // 2. 若上传视频为app私有目录视频文件，直接上传即可。
            if (Build.VERSION.SDK_INT >= 29) {
                // 说明是统一资源定位符,需要拷贝图库文件到app私有目录，再执行上传
                if (mInputSource.startsWith("content://")) {
                    if (mWorkLoadingProgress == null) {
                        initWorkLoadingProgress();
                    }
                    mWorkLoadingProgress.setProgress(0);
                    mWorkLoadingProgress.setCancelable(false);
                    mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");
                    // 拷贝文件到本地
                    copyVideoToLocal();
                } else {
                    File file = new File(mInputSource);
                    if (file.exists() && file.canRead()) {
                        startPublishActivity(mInputSource);
                    } else {
                        Toast.makeText(this, "找不到文件或文件读取失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                startPublishActivity(mInputSource);
            }
        } else {
            if (mWorkLoadingProgress == null) {
                initWorkLoadingProgress();
            }
            mWorkLoadingProgress.setProgress(0);
            mWorkLoadingProgress.setCancelable(false);
            mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");

            String inputBitrateStr = mEtCompressBitrate.getText().toString();
            if (!TextUtils.isEmpty(inputBitrateStr)) {
                mBiteRate = Integer.parseInt(inputBitrateStr);
                if (mBiteRate == 0) {
                    mBiteRate = 2400;
                } else if (mBiteRate > 20000) {
                    mBiteRate = 20000;
                }
            } else {
                // 如果没有设置码率，默认设置一个码率
                mBiteRate = 2400;
            }
            mTXVideoEditer.setVideoBitrate(mBiteRate);
            mTXVideoEditer.setCutFromTime(0, mTXVideoInfo.duration);

            if (!TextUtils.isEmpty(mOutputPath)){
                mTXVideoEditer.generateVideo(mVideoResolution, mOutputPath);
            }
            mCompressing = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCompressVideo();
    }

    private void stopCompressVideo() {
        if (!mCompressing) {
            TXCLog.e(TAG, "stopCompressVideo, mCompressing is false, ignore");
            return;
        }
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setProgress(0);
            mWorkLoadingProgress.dismiss();
        }
        mTXVideoEditer.cancel();
    }

    /**
     * Android 10（Q）Google官方尚未强制启用 App 沙箱运行，当且仅当 targetSDK 为 29 的时候才会在沙箱下运行
     * Google官方预计 2020 年在 Android 11（R）强制启动沙箱机制，届时所有 app 无论 targetSDK 是否为 29，都运行在沙箱机制。
     * <p>
     * <p>
     * 为了您的日后的兼容性着想，建议按照以下流程上传视频：
     * <p>
     * 1. 若上传视频为SD卡视频（如，图库文件），视频上传目前不支持 Uri 统一资源定位符方式进行上传。因此上传图库文件（不在app私有目录下）需要拷贝视频文件到本地目录。
     * 2. 若上传视频为app私有目录视频文件，直接上传即可。
     */
    private void copyVideoToLocal() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = TCCompressActivity.this.getContentResolver();
                InputStream in = null;
                FileOutputStream fos = null;
                try {
                    in = resolver.openInputStream(Uri.parse(mInputSource));
                    if (in != null) {
                        File file = new File(mOutputPath);
                        if (file.exists()) {
                            file.delete();
                        }
                        boolean result = file.createNewFile();
                        if (!result) {
                            in.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgress();
                                    Toast.makeText(TCCompressActivity.this, "拷贝文件到私有目录失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        fos = new FileOutputStream(file);
                        final long available = in.available();
                        long progress = 0;
                        byte[] bytes = new byte[4096];
                        int i = 0;
                        while ((i = in.read(bytes)) > 0) {
                            fos.write(bytes, 0, i);
                            progress += i;
                            final long finalProgress = progress;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mWorkLoadingProgress.setProgress((int) (finalProgress * 1.0 / available));
                                }
                            });
                        }
                        fos.flush();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgress();
                                startPublishActivity(mOutputPath);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void dismissProgress() {
        if (mWorkLoadingProgress != null && mWorkLoadingProgress.isAdded()) {
            mWorkLoadingProgress.dismiss();
        }
    }

    private void initWorkLoadingProgress() {
        if (mWorkLoadingProgress == null) {
            mWorkLoadingProgress = new VideoWorkProgressFragment();
            mWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTXVideoEditer != null) {
                        mTXVideoEditer.cancel();
                        mWorkLoadingProgress.dismiss();
                        mWorkLoadingProgress.setProgress(0);
                    }
                }
            });
        }
        mWorkLoadingProgress.setProgress(0);
    }

    private void startPublishActivity(String videoPath) {
        Intent intent = new Intent(TCCompressActivity.this, TCVideoPublishActivity.class);
        intent.putExtra(TCConstants.VIDEO_EDITER_PATH, videoPath);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
