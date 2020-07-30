package com.tencent.liteav.demo.videorecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.utils.AlbumSaver;
import com.tencent.qcloud.ugckit.utils.FileUtils;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;
import java.util.Locale;

/**
 * 短视频录制完视频预览界面
 */
public class TCRecordPreviewActivity extends Activity implements View.OnClickListener, ITXVodPlayListener {
    public static final String TAG = "TCRecordPreviewActivity";

    private ImageView            mImageStartPreview;
    private ImageView           mImageToEdit;
    private ImageView           mImageViewBg;
    private TXVodPlayer         mTXVodPlayer  = null;
    private TXVodPlayConfig     mTXPlayConfig = null;
    private TXCloudVideoView    mTXCloudVideoView;
    private SeekBar             mSeekBar;
    private TextView            mTextProgressTime;
    private ErrorDialogFragment mFragmentErrDlg;      //错误消息弹窗

    private boolean mVideoPlay  = false;
    private boolean mVideoPause = false;
    private boolean mAutoPause  = false;
    private boolean mStartSeek  = false;
    private String  mVideoPath;
    private String  mCoverImagePath;
    private long    mTrackingTouchTS = 0;
    private long    mVideoDuration;          //    视频时长（ms）
    private int     mVideoResolution;        //录制界面传过来的视频分辨率

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentErrDlg = new ErrorDialogFragment();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.ugcrecord_activity_record_preview);

        mImageStartPreview = (ImageView) findViewById(R.id.record_preview);
        mImageToEdit = (ImageView) findViewById(R.id.record_to_edit);
        mImageToEdit.setOnClickListener(this);

        mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
        mCoverImagePath = getIntent().getStringExtra(UGCKitConstants.VIDEO_COVERPATH);
        mVideoDuration = getIntent().getLongExtra(UGCKitConstants.VIDEO_RECORD_DURATION, 0);
        mVideoResolution = getIntent().getIntExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, -1);
        mImageViewBg = (ImageView) findViewById(R.id.cover);

        if (!TextUtils.isEmpty(mCoverImagePath)) {
            mImageViewBg.setVisibility(View.VISIBLE);
            Glide.with(this).load(Uri.fromFile(new File(mCoverImagePath))).into(mImageViewBg);
        }

        mTXVodPlayer = new TXVodPlayer(this);
        mTXPlayConfig = new TXVodPlayConfig();
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bFromUser) {
                if (mTextProgressTime != null) {
                    mTextProgressTime.setText(String.format(Locale.CHINA, "%02d:%02d/%02d:%02d", (progress) / 60, (progress) % 60, (seekBar.getMax()) / 60, (seekBar.getMax()) % 60));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mTXVodPlayer != null) {
                    mTXVodPlayer.seek(seekBar.getProgress());
                }
                mTrackingTouchTS = System.currentTimeMillis();
                mStartSeek = false;
            }
        });
        mTextProgressTime = (TextView) findViewById(R.id.progress_time);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.record_delete) {
            deleteVideo();
            FileUtils.deleteFile(mCoverImagePath);
        } else if (id == R.id.record_download) {
            downloadRecord();
        } else if (id == R.id.record_preview) {
            if (mVideoPlay) {
                if (mVideoPause) {
                    mTXVodPlayer.resume();
                    mImageStartPreview.setBackgroundResource(R.drawable.ugcrecord_icon_record_pause);
                    mVideoPause = false;
                } else {
                    mTXVodPlayer.pause();
                    mImageStartPreview.setBackgroundResource(R.drawable.ugcrecord_icon_record_start);
                    mVideoPause = true;
                }
            } else {
                startPlay();
            }
        } else if (id == R.id.record_to_edit) {
            startEditVideo();
        }
    }

    private void startEditVideo() {
        // 播放器版本没有此activity
        Intent intent = new Intent();
        intent.setAction("com.tencent.liteav.demo.videoediter");
        intent.putExtra(UGCKitConstants.VIDEO_PATH, mVideoPath);
        if (mVideoResolution != -1) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, mVideoResolution);
        }
        startActivity(intent);
        finish();
    }


    private boolean startPlay() {
        mImageStartPreview.setBackgroundResource(R.drawable.ugcrecord_icon_record_pause);
        mTXVodPlayer.setPlayerView(mTXCloudVideoView);
        mTXVodPlayer.setVodListener(this);

        mTXVodPlayer.enableHardwareDecode(false);
        mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mTXVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);

        mTXVodPlayer.setConfig(mTXPlayConfig);

        int result = mTXVodPlayer.startPlay(mVideoPath); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
        if (result != 0) {
            mImageStartPreview.setBackgroundResource(R.drawable.ugcrecord_icon_record_start);
            return false;
        }

        mVideoPlay = true;
        return true;
    }

    /**
     * 保存录制的视频到系统相册
     */
    private void downloadRecord() {
        AlbumSaver.getInstance(this).setOutputProfile(mVideoPath, mVideoDuration, mCoverImagePath);
        AlbumSaver.getInstance(this).saveVideoToDCIM();
    }

    /**
     * 删除录制的视频
     */
    private void deleteVideo() {
        // 停止播放
        stopPlay(true);
        //删除文件
        FileUtils.deleteFile(mVideoPath);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTXCloudVideoView.onResume();
        if (mVideoPlay && mAutoPause) {
            mTXVodPlayer.resume();
            mAutoPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTXCloudVideoView.onPause();
        if (mVideoPlay && !mVideoPause) {
            mTXVodPlayer.pause();
            mAutoPause = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTXCloudVideoView.onDestroy();
        stopPlay(true);
        mTXVodPlayer = null;
        mTXPlayConfig = null;
        mTXCloudVideoView = null;
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(null);
        }
    }

    protected void stopPlay(boolean clearLastFrame) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setVodListener(null);
            mTXVodPlayer.stopPlay(clearLastFrame);
            mVideoPlay = false;
        }
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.setLogText(null, param, event);
        }
        if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            if (mStartSeek) {
                return;
            }
            if (mImageViewBg.isShown()) {
                mImageViewBg.setVisibility(View.GONE);
            }
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);//单位为s
            long curTS = System.currentTimeMillis();
            // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
            if (Math.abs(curTS - mTrackingTouchTS) < 500) {
                return;
            }
            mTrackingTouchTS = curTS;

            if (mSeekBar != null) {
                mSeekBar.setProgress(progress);
            }
            if (mTextProgressTime != null) {
                mTextProgressTime.setText(String.format(Locale.CHINA, "%02d:%02d/%02d:%02d", (progress) / 60, progress % 60, (duration) / 60, duration % 60));
            }

            if (mSeekBar != null) {
                mSeekBar.setMax(duration);
            }
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {

//            showErrorAndQuit(UGCKitConstants.ERROR_MSG_NET_DISCONNECTED);

        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            mTXVodPlayer.resume(); // 播放结束后，可以直接resume()，如果调用stop和start，会导致重新播放会黑一下
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle bundle) {

    }

    public static class ErrorDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.UGCKitConfirmDialogStyle)
                    .setCancelable(true)
                    .setTitle(getArguments().getString("errorMsg"))
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            getActivity().finish();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            return alertDialog;
        }
    }

    protected void showErrorAndQuit(String errorMsg) {
        if (!mFragmentErrDlg.isAdded() && !this.isFinishing()) {
            Bundle args = new Bundle();
            args.putString("errorMsg", errorMsg);
            mFragmentErrDlg.setArguments(args);
            mFragmentErrDlg.setCancelable(false);

            //此处不使用用.show(...)的方式加载dialogfragment，避免IllegalStateException
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(mFragmentErrDlg, "loading");
            transaction.commitAllowingStateLoss();
        }
    }
}
