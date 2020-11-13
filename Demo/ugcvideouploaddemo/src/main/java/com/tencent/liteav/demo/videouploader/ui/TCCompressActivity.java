package com.tencent.liteav.demo.videouploader.ui;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videouploader.R;
import com.tencent.liteav.demo.videouploader.ui.utils.Constants;
import com.tencent.liteav.demo.videouploader.ui.view.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videouploader.ui.utils.Utils;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TCCompressActivity extends FragmentActivity {
    private final String TAG = "TCCompressActivity";

    private TXVideoEditer             mTXVideoEditer;
    private LinearLayout              mLayoutBack;
    private RadioGroup                mRadioGroupVideoResolution;
    private RadioButton               mRadioVideoNotCompress;
    private RadioButton               mRadioVideoResolution360p;
    private RadioButton               mRadioVideoResolution480p;
    private RadioButton               mRadioVideoResolution540p;
    private RadioButton               mRadioVideoResolution720p;
    private EditText                  mEtCompressBitrate;
    private Button                    mBtnStartCompress;
    private VideoWorkProgressFragment mFragmentWorkLoadingProgress;     // 生成视频的等待框

    private int     mVideoResolution;
    private int     mBiteRate;                                          // 码率
    private String  mInputSource;
    private String  mVideoSourcePath;                                   //未压缩视频源的路径
    private String  mOutputPath;
    private boolean mCompressing;

    private TXVideoEditer.TXVideoGenerateListener mTXVideoGenerateListener;
    private TXVideoEditConstants.TXVideoInfo      mTXVideoInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ugcupload_activity_compress);

        initView();

        initData();
    }

    private void initData() {
        mTXVideoEditer = new TXVideoEditer(getApplicationContext());
        mVideoResolution = -1;
        String path = getIntent().getStringExtra(Constants.VIDEO_EDITER_PATH);
        String uri = getIntent().getStringExtra(Constants.VIDEO_EDITER_URI);

        // Android 10（Q）Google官方尚未强制启用 App 沙箱运行，当且仅当 targetSDK 为 29 的时候才会在沙箱下运行
        // Google官方预计 2020 年在 Android 11（R）强制启动沙箱机制，届时所有 app 无论 targetSDK 是否为 29，都运行在沙箱机制。
        // 因此为了您的 app 保持较高兼容性，推荐您在系统版本为 Android 10或以上的设备，都使用 Google 官方推荐的 uri 统一资源定位符的方式传递给 SDK。
        if (Build.VERSION.SDK_INT >= 29) {
            mInputSource = uri;
        } else {
            mInputSource = path;
        }
        mVideoSourcePath = path; //视频封面的提取兼容9.0以上的机型
        mOutputPath = Utils.generateVideoPath(this);
        int ret = mTXVideoEditer.setVideoPath(mInputSource);
        if (ret != 0) {
            if (ret == TXVideoEditConstants.ERR_SOURCE_NO_TRACK) {
                showDialog(R.string.ugcupload_error_handle_video, R.string.ugcupload_error_invalid_format, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_AUDIO_FORMAT) {
                showDialog(R.string.ugcupload_error_handle_video, R.string.ugcupload_error_unsupport_audio_format, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            return;
        }
        mTXVideoInfo = TXVideoInfoReader.getInstance(this).getVideoFileInfo(mInputSource);

        // 部分机型上会产生00:00时长的视频文件，导致此处VideoInfo为空
        if (mTXVideoInfo == null) {
            showDialog(R.string.ugcupload_error_handle_video, R.string.ugcupload_error_invalid_format, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
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
                        mFragmentWorkLoadingProgress.setProgress((int) (progress * 100));
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
                        if (mFragmentWorkLoadingProgress != null && mFragmentWorkLoadingProgress.isAdded()) {
                            mFragmentWorkLoadingProgress.dismiss();
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
        mRadioGroupVideoResolution = (RadioGroup) findViewById(R.id.rg_video_resolution);
        mRadioVideoNotCompress = (RadioButton) findViewById(R.id.rb_video_compress_none);
        mRadioVideoResolution360p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_360p);
        mRadioVideoResolution480p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_480p);
        mRadioVideoResolution540p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_540p);
        mRadioVideoResolution720p = (RadioButton) findViewById(R.id.rb_video_compress_resolution_720p);

        mEtCompressBitrate = (EditText) findViewById(R.id.et_compress_bitrate);

        mRadioGroupVideoResolution.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mRadioVideoNotCompress.getId()) {
                    mVideoResolution = -1;
                    mEtCompressBitrate.setText("");
                } else if (checkedId == mRadioVideoResolution360p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_360P;
                    mEtCompressBitrate.setText("2400");
                } else if (checkedId == mRadioVideoResolution480p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_480P;
                    mEtCompressBitrate.setText("2400");
                } else if (checkedId == mRadioVideoResolution540p.getId()) {
                    mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_540P;
                    mEtCompressBitrate.setText("6500");
                } else if (checkedId == mRadioVideoResolution720p.getId()) {
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

        mLayoutBack = (LinearLayout) findViewById(R.id.back_ll);
        mLayoutBack.setOnClickListener(new View.OnClickListener() {
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
                if (mInputSource.startsWith(Constants.PREFIX_MEDIA_URI)) {
                    if (mFragmentWorkLoadingProgress == null) {
                        initWorkLoadingProgress();
                    }
                    mFragmentWorkLoadingProgress.setProgress(0);
                    mFragmentWorkLoadingProgress.setCancelable(false);
                    mFragmentWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");
                    // 拷贝文件到本地
                    copyVideoToLocal();
                } else {
                    File file = new File(mInputSource);
                    if (file.exists() && file.canRead()) {
                        startPublishActivity(mInputSource);
                    } else {
                        Toast.makeText(this, R.string.ugcupload_error_read_file, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                startPublishActivity(mInputSource);
            }
        } else {
            if (mFragmentWorkLoadingProgress == null) {
                initWorkLoadingProgress();
            }
            mFragmentWorkLoadingProgress.setProgress(0);
            mFragmentWorkLoadingProgress.setCancelable(false);
            mFragmentWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");

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
        if (mFragmentWorkLoadingProgress != null) {
            mFragmentWorkLoadingProgress.setProgress(0);
            mFragmentWorkLoadingProgress.dismiss();
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
                                    Toast.makeText(TCCompressActivity.this, R.string.ugcupload_error_copy_file, Toast.LENGTH_SHORT).show();
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
                                    mFragmentWorkLoadingProgress.setProgress((int) (finalProgress * 1.0 / available));
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
        if (mFragmentWorkLoadingProgress != null && mFragmentWorkLoadingProgress.isAdded()) {
            mFragmentWorkLoadingProgress.dismiss();
        }
    }

    private void initWorkLoadingProgress() {
        if (mFragmentWorkLoadingProgress == null) {
            mFragmentWorkLoadingProgress = new VideoWorkProgressFragment();
            mFragmentWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTXVideoEditer != null) {
                        mTXVideoEditer.cancel();
                        mFragmentWorkLoadingProgress.dismiss();
                        mFragmentWorkLoadingProgress.setProgress(0);
                    }
                }
            });
        }
        mFragmentWorkLoadingProgress.setProgress(0);
    }

    private void startPublishActivity(String videoPath) {
        Intent intent = new Intent(TCCompressActivity.this, TCVideoPublishActivity.class);
        intent.putExtra(Constants.VIDEO_EDITER_PATH, videoPath);
        intent.putExtra(Constants.VIDEO_SOURCE_PATH, mVideoSourcePath);
        startActivity(intent);
    }

    private void showDialog(int titleRes, int contentRes, final View.OnClickListener listener) {
        final Dialog dialog = new Dialog(this, R.style.UGCUploadConfirmDialogStyle);
        final View v = LayoutInflater.from(this).inflate(R.layout.ugcupload_dialog_ugc_tip, null);
        dialog.setContentView(v);
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        TextView tvContent = (TextView) dialog.findViewById(R.id.tv_msg);
        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        tvTitle.setText(titleRes);
        tvContent.setText(contentRes);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(view);
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
