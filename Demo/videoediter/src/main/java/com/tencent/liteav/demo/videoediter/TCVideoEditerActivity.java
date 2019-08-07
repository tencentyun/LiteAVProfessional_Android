package com.tencent.liteav.demo.videoediter;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videoediter.common.utils.FileUtils;
import com.tencent.liteav.demo.videoediter.common.utils.TCConstants;
import com.tencent.liteav.demo.videoediter.common.videopreview.TCVideoPreviewActivity;
import com.tencent.liteav.demo.videoediter.common.widget.progress.VideoWorkProgressFragment;
import com.tencent.liteav.demo.videoediter.common.utils.GifUtil;
import com.tencent.liteav.demo.videoediter.common.utils.PlayState;
import com.tencent.liteav.demo.videoediter.common.utils.TCEditerUtil;
import com.tencent.liteav.demo.videoediter.bgm.TCBGMSettingFragment;
import com.tencent.liteav.demo.videoediter.bubble.TCBubbleViewInfoManager;
import com.tencent.liteav.demo.videoediter.bubble.TCWordEditActivity;
import com.tencent.liteav.demo.videoediter.common.TCConfirmDialog;
import com.tencent.liteav.demo.videoediter.common.TCToolsView;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressController;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressView;
import com.tencent.liteav.demo.videoediter.cutter.TCCutterFragment;
import com.tencent.liteav.demo.videoediter.filter.TCStaticFilterFragment;
import com.tencent.liteav.demo.videoediter.motion.TCMotionFragment;
import com.tencent.liteav.demo.videoediter.paster.TCPasterActivity;
import com.tencent.liteav.demo.videoediter.paster.TCPasterViewInfoManager;
import com.tencent.liteav.demo.videoediter.time.TCTimeFragment;
import com.tencent.liteav.demo.videoediter.transition.TCTransitionFragment;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by hans on 2017/11/6.
 */

public class TCVideoEditerActivity extends FragmentActivity implements
        TCToolsView.OnItemClickListener,
        View.OnClickListener,
        TCVideoEditerWrapper.TXVideoPreviewListenerWrapper,
        TXVideoEditer.TXVideoGenerateListener {
    private static final String TAG = "TCVideoEditerActivity";

    private TCVideoEditerWrapper mEditerWrapper;
    // 短视频SDK获取到的视频信息
    private TXVideoEditer mTXVideoEditer;                   // SDK接口类
    /**
     * 布局相关
     */
    private LinearLayout mLlBack;                           // 左上角返回
    private FrameLayout mVideoPlayerLayout;                 // 视频承载布局
    private ImageButton mIbPlay;                            // 播放按钮
    private TextView mTvDone;
    private TCToolsView mToolsView;                         // 底部工具栏

    private VideoWorkProgressFragment mWorkLoadingProgress; // 生成视频的等待框


    private Fragment mCurrentFragment,                      // 标记当前的Fragment
            mCutterFragment,                                // 裁剪的Fragment
            mTimeFragment,                                  // 时间特效的Fragment
            mStaticFilterFragment,                          // 静态滤镜的Fragment
            mMotionFragment,                                // 动态滤镜的Fragment
            mBGMSettingFragment,                            // BGM设置的Fragment
            mTransitionFragment;                            // 转场的Fragment

    private int mCurrentState = PlayState.STATE_NONE;       // 播放器当前状态

    private String mVideoOutputPath;                        // 视频输出路径
    private int mVideoResolution = -1;                      // 分辨率类型（如果是从录制过来的话才会有，这参数）

    private long mVideoDuration;                            // 视频的总时长
    private long mPreviewAtTime;                            // 当前单帧预览的时间

    private TXPhoneStateListener mPhoneListener;            // 电话监听

    private KeyguardManager mKeyguardManager;
    private int mVideoFrom;
    public boolean isPreviewFinish;

    // =========== 音乐设置项（在选择背景音乐的时候出现） ===========
    private RelativeLayout rlBGMSetting;
    public CheckBox cbBgmLoop;
    public CheckBox cbBgmFadeInOut;
    /**
     * 缩略图进度条相关
     */
    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    private VideoProgressController.VideoProgressSeekListener mVideoProgressSeekListener = new VideoProgressController.VideoProgressSeekListener() {
        @Override
        public void onVideoProgressSeek(long currentTimeMs) {
            TXCLog.i(TAG, "onVideoProgressSeek, currentTimeMs = " + currentTimeMs);

            previewAtTime(currentTimeMs);
        }

        @Override
        public void onVideoProgressSeekFinish(long currentTimeMs) {
            TXCLog.i(TAG, "onVideoProgressSeekFinish, currentTimeMs = " + currentTimeMs);

            previewAtTime(currentTimeMs);
        }
    };
    private String mRecordProcessedPath;
    private int mCustomBitrate;
    private int beautyLevel = 0;
    private int whiteLevel = 0;
    private boolean mIsPicCombine; // 图片合成
    private List<String> picPathList;
    private ArrayList<Bitmap> mBitmapList;
    private boolean mNeedProcessVideo;
    private Handler mMainHandler;
    private boolean mGifStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editer);

        mMainHandler = new Handler(Looper.getMainLooper());
        mEditerWrapper = TCVideoEditerWrapper.getInstance();
        mEditerWrapper.addTXVideoPreviewListenerWrapper(this);

        mIsPicCombine = getIntent().getBooleanExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, false);
        mNeedProcessVideo = getIntent().getBooleanExtra(TCConstants.VIDEO_EDITER_IMPORT, false);
        if (mIsPicCombine) {
            picPathList = getIntent().getStringArrayListExtra(TCConstants.INTENT_KEY_MULTI_PIC_LIST);
            decodeFileToBitmap(picPathList);
            mTXVideoEditer = new TXVideoEditer(this);
            mEditerWrapper.setEditer(mTXVideoEditer);
            int result = mTXVideoEditer.setPictureList(mBitmapList, 20);
            if (result == TXVideoEditConstants.PICTURE_TRANSITION_FAILED) {
                Toast.makeText(this, "图片设置异常，结束编辑", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            // 注意：
            // 1、接口调用顺序：setPictureList在前，setPicTransferType在后，必须顺序调用
            // 1、图片转视频的时长需要设置转场类型后获取，因为不同的转场类型时长会不一样
            // 2、宽高信息sdk内部会处理成9：16比例，上层只有在加片尾水印的时候算归一化坐标用到，所以这里可以设置成720P（720 * 1280）或者540P（540 * 960）来计算。注意最终视频的分辨率是按照生成时传的参数决定的。
            // （5.0以前版本是按照第一张图片的宽高来决定最终的宽高，导致的问题是如果第一张图片有一边比较短，后面的图片会以最短边等比例缩放，显示出来就小了）
            mVideoDuration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
            TXVideoEditConstants.TXVideoInfo txVideoInfo = new TXVideoEditConstants.TXVideoInfo();
            txVideoInfo.duration = mVideoDuration;
            txVideoInfo.width = 720;
            txVideoInfo.height = 1280;
            mEditerWrapper.setTXVideoInfo(txVideoInfo);
        } else {
            mTXVideoEditer = mEditerWrapper.getEditer();
            if (mTXVideoEditer == null || mEditerWrapper.getTXVideoInfo() == null) {
                Toast.makeText(this, "状态异常，结束编辑", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            mVideoDuration = mEditerWrapper.getTXVideoInfo().duration;
        }
        mEditerWrapper.setCutterStartTime(0, mVideoDuration);
        mVideoResolution = getIntent().getIntExtra(TCConstants.VIDEO_RECORD_RESOLUTION, -1);
        mCustomBitrate = getIntent().getIntExtra(TCConstants.RECORD_CONFIG_BITE_RATE, 0);

        mVideoFrom = getIntent().getIntExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_EDIT);
        // 录制经过预处理的视频路径，在编辑后需要删掉录制源文件
        mRecordProcessedPath = getIntent().getStringExtra(TCConstants.VIDEO_EDITER_PATH);

        initViews();
        initPhoneListener();
        initVideoProgressLayout();
        prepareVideoView(); // 设置编辑预览参数
        mKeyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    }
    private int mRotation;
    private void decodeFileToBitmap(List<String> picPathList) {
        if (picPathList == null) {
            return;
        }
        mBitmapList = new ArrayList<>();
        for (int i = 0; i < picPathList.size(); i++) {
            String filePath = picPathList.get(i);
            Bitmap bitmap = TCEditerUtil.decodeSampledBitmapFromFile(filePath, 720, 1280);
            mBitmapList.add(bitmap);
            TCVideoEditerWrapper.getInstance().addThumbnailBitmap(0, bitmap);
        }
    }

    private void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void initViews() {
        mToolsView = (TCToolsView) findViewById(R.id.editer_tools_view);
        mToolsView.setOnItemClickListener(this);
        mLlBack = (LinearLayout) findViewById(R.id.editer_back_ll);
        mLlBack.setOnClickListener(this);
        mTvDone = (TextView) findViewById(R.id.editer_tv_done);
        mTvDone.setOnClickListener(this);
        mVideoPlayerLayout = (FrameLayout) findViewById(R.id.editer_fl_video);

        mIbPlay = (ImageButton) findViewById(R.id.editer_ib_play);
        mIbPlay.setOnClickListener(this);

        rlBGMSetting = (RelativeLayout) findViewById(R.id.rl_bgm_setting);
        cbBgmLoop = (CheckBox) findViewById(R.id.cb_bgm_loop);
        cbBgmFadeInOut = (CheckBox) findViewById(R.id.cb_bgm_fade);
    }

    public void showBgmSetting(boolean isShow){
        if(isShow){
            rlBGMSetting.setVisibility(View.VISIBLE);
        }else if(rlBGMSetting.getVisibility() == View.VISIBLE){
            rlBGMSetting.setVisibility(View.GONE);
        }
    }

    private long getCutterStartTime() {
        return mEditerWrapper.getCutterStartTime();
    }

    private long getCutterEndTime() {
        return mEditerWrapper.getCutterEndTime();
    }

    /**
     * ==========================================SDK播放器生命周期==========================================
     */

    private void prepareVideoView() {
        showCutterFragment();
        initPlayerLayout();         // 初始化预览视频布局
    }

    private void initVideoProgressLayout() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        mVideoProgressView = (VideoProgressView) findViewById(R.id.editer_video_progress_view);
        mVideoProgressView.setViewWidth(screenWidth);
        if (!mIsPicCombine) {
            List<Bitmap> thumbnailList = TCVideoEditerWrapper.getInstance().getAllThumbnails();
            mVideoProgressView.setThumbnailData();
            if (thumbnailList != null || thumbnailList.size() > 0) {
                mVideoProgressView.addAllThumbnail(thumbnailList);
            }
        } else {
            if (mBitmapList != null) {
                mVideoProgressView.setThumbnailData();
                mVideoProgressView.addAllThumbnail(mBitmapList);
            }
        }
        mVideoProgressController = new VideoProgressController(mVideoDuration);
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        if (mIsPicCombine) {
            mVideoProgressController.setThumbnailPicListDisplayWidth(mVideoProgressView.getThumbnailCount());
        } else {
            if (mNeedProcessVideo) {
                mVideoProgressController.setThumbnailPicListDisplayWidth(mVideoProgressView.getThumbnailCount());
            } else {
                mVideoProgressController.setThumbnailPicListDisplayWidth(TCVideoEditerWrapper.mThumbnailCount);
                mTXVideoEditer.getThumbnail(TCVideoEditerWrapper.mThumbnailCount, 100, 100, false, mThumbnailListener);
            }
        }
        mVideoProgressController.setVideoProgressSeekListener(mVideoProgressSeekListener);
        mVideoProgressController.setVideoProgressDisplayWidth(screenWidth);
    }

    private boolean mLoadThumbnailSucc = false;
    private TXVideoEditer.TXThumbnailListener mThumbnailListener = new TXVideoEditer.TXThumbnailListener() {
        @Override

        public void onThumbnail(int index, long timeMs, final Bitmap bitmap) {
            Log.i(TAG, "onThumbnail: index = " + index + ",timeMs:" + timeMs);
            TCVideoEditerWrapper.getInstance().addThumbnailBitmap(timeMs, bitmap);

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoProgressView.addThumbnail(bitmap);
                }
            });
            if (index == TCVideoEditerWrapper.mThumbnailCount - 1) {
                mLoadThumbnailSucc = true;
            }
        }
    };

    private void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mVideoPlayerLayout;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    /**
     * 调用mTXVideoEditer.previewAtTime后，需要记录当前时间，下次播放时从当前时间开始
     * x
     *
     * @param timeMs
     */
    public void previewAtTime(long timeMs) {
        pausePlay();
        isPreviewFinish = false;
        mTXVideoEditer.previewAtTime(timeMs);
        mPreviewAtTime = timeMs;
        mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
    }

    /**
     * 给子Fragment调用 （子Fragment不在意Activity中对于播放器的生命周期）
     */
    public void restartPlay() {
        stopPlay();
        startPlay(getCutterStartTime(), getCutterEndTime());
    }

    public void startPlay(long startTime, long endTime) {
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoEditer.startPlayFromTime(startTime, endTime);
            mCurrentState = PlayState.STATE_PLAY;
            isPreviewFinish = false;
            mIbPlay.setImageResource(R.drawable.ic_pause);
        }
    }

    public void resumePlay() {
        if (mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.resumePlay();
            mCurrentState = PlayState.STATE_RESUME;
            mIbPlay.setImageResource(R.drawable.ic_pause);

        }
    }

    public void pausePlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mTXVideoEditer.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            mIbPlay.setImageResource(R.drawable.ic_play);
        }
    }

    public void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY ||
                mCurrentState == PlayState.STATE_PREVIEW_AT_TIME || mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            mIbPlay.setImageResource(R.drawable.ic_play);
        }
    }

    /**
     * ==========================================activity生命周期==========================================
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TXCLog.i(TAG, "onActivityResult, request code = " + requestCode);
        // 如果编辑视频的界面发生变化，需要重新调用initPlayerLayout
        if (requestCode == TCConstants.REQUEST_CODE_PASTER || requestCode == TCConstants.REQUEST_CODE_WORD) {
            initPlayerLayout();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TXCLog.i(TAG, "onResume");
        mEditerWrapper.addTXVideoPreviewListenerWrapper(this);
//        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) { // 魅族此方法（mKeyguardManager.inKeyguardRestrictedInputMode()）返回true
        playVideo(false);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
        // 若当前处于生成状态，离开当前activity，直接停止生成
        if (mCurrentState == PlayState.STATE_GENERATE) {
            stopGenerate();
        }
        mEditerWrapper.removeTXVideoPreviewListenerWrapper(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        // 清空保存的气泡字幕参数 （避免下一个视频混入上一个视频的气泡设定
        TCBubbleViewInfoManager.getInstance().clear();
        // 清空保存的贴纸参数
        TCPasterViewInfoManager.getInstance().clear();
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setOnClickStopListener(null);
        }

        if (!mNeedProcessVideo) {   //快速导入内存释放
            if (mTXVideoEditer != null) {
                // 注意释放，不然容易内存泄漏
                mTXVideoEditer.setThumbnailListener(null);
                mTXVideoEditer.setVideoProcessListener(null);
                mTXVideoEditer.cancel();
                mTXVideoEditer.release();
                mTXVideoEditer = null;
            }
        } else { //全功能导入内存
            if (mTXVideoEditer != null) {
                stopPlay();
                mTXVideoEditer.setVideoGenerateListener(null);
                //编辑完成后，销毁资源，避免影响处理下一个视频
                mTXVideoEditer.release();
                mTXVideoEditer = null;
            }
        }
        // 清除对TXVideoEditer的引用以及相关配置
        mEditerWrapper.removeTXVideoPreviewListenerWrapper(this);
        mEditerWrapper.cleaThumbnails();
        mEditerWrapper.clear();
    }


    /**
     * ==========================================SDK回调==========================================
     */
    @Override // 预览进度回调
    public void onPreviewProgressWrapper(final int timeMs) {
        // 视频的进度回调是异步的，如果不是处于播放状态，那么无需修改进度
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mVideoProgressController.setCurrentTimeMs(timeMs);
        }
    }

    @Override // 预览完成回调
    public void onPreviewFinishedWrapper() {
        TXCLog.d(TAG, "---------------onPreviewFinished-----------------");
        isPreviewFinish = true;
        // 如果是在单帧预览条件下结束的，不要开始播放
        if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            return;
        }
        stopPlay();
        if ((mMotionFragment != null && mMotionFragment.isAdded() && !mMotionFragment.isHidden()) ||
                (mTimeFragment != null && mTimeFragment.isAdded() && !mTimeFragment.isHidden())) {
            // 处于动态滤镜或者时间特效界面,忽略 不做任何操作
        } else {
            // 如果当前不是动态滤镜界面或者时间特效界面，那么会自动开始重复播放;
            startPlay(getCutterStartTime(), getCutterEndTime());
        }
    }

    @Override
    public void onPreviewError(TXVideoEditConstants.TXPreviewError error) {
        Toast.makeText(this,"预览播放失败：" + error.errorMsg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 创建缩略图，并跳转至视频预览的Activity
     */
    private void createThumbFile(final TXVideoEditConstants.TXGenerateResult result) {
        AsyncTask<Void, String, String> task = new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                File outputVideo = new File(mVideoOutputPath);
                if (!outputVideo.exists())
                    return null;
                Bitmap bitmap = TXVideoInfoReader.getInstance().getSampleImage(0, mVideoOutputPath);
                if (bitmap == null)
                    return null;
                String mediaFileName = outputVideo.getAbsolutePath();
                if (mediaFileName.lastIndexOf(".") != -1) {
                    mediaFileName = mediaFileName.substring(0, mediaFileName.lastIndexOf("."));
                }
                String folder = Environment.getExternalStorageDirectory() + File.separator + TCConstants.DEFAULT_MEDIA_PACK_FOLDER + File.separator + mediaFileName;
                File appDir = new File(folder);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                String fileName = "thumbnail" + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }

            @Override
            protected void onPostExecute(String s) {
                if (mVideoFrom == TCConstants.VIDEO_RECORD_TYPE_UGC_RECORD) {
                    FileUtils.deleteFile(mRecordProcessedPath);
                }
                startPreviewActivity(result, s);
            }

        };
        task.execute();
    }

    private void startPreviewActivity(TXVideoEditConstants.TXGenerateResult result, String thumbPath) {
        Intent intent = new Intent(getApplicationContext(), TCVideoPreviewActivity.class);
        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_EDIT);
        intent.putExtra(TCConstants.VIDEO_RECORD_RESULT, result.retCode);
        intent.putExtra(TCConstants.VIDEO_RECORD_DESCMSG, result.descMsg);
        intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
        if (thumbPath != null)
            intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, thumbPath);
        intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, getCutterEndTime() - getCutterStartTime());
        startActivity(intent);
        finish();
    }

    /**
     * ==========================================工具栏的点击回调==========================================
     */


    private void showCutterFragment() {
        if (mCutterFragment == null) {
            mCutterFragment = new TCCutterFragment();
        }
        showFragment(mCutterFragment, "cutter_fragment");
    }


    @Override
    public void onClickTime() {
        if (mIsPicCombine) {
            showTransitionFragment();
        } else {
            showTimeFragment();
        }
    }

    @Override
    public void onClickCutter() {
        showCutterFragment();
    }

    private void showTimeFragment() {
        Bundle args = new Bundle();
        args.putBoolean("needProcessVideo", mNeedProcessVideo);

        if (mTimeFragment == null) {
            mTimeFragment = new TCTimeFragment();
            mTimeFragment.setArguments(args);
        }

        showFragment(mTimeFragment, "time_fragment");
    }

    private void showTransitionFragment() {
        if (mTransitionFragment == null) {
            mTransitionFragment = new TCTransitionFragment();
        }
        showFragment(mTransitionFragment, "transition_fragment");
    }

    @Override
    public void onClickStaticFilter() {
        if (mStaticFilterFragment == null) {
            mStaticFilterFragment = new TCStaticFilterFragment();
        }
        showFragment(mStaticFilterFragment, "static_filter_fragment");
    }

    @Override
    public void onClickMotionFilter() {
        if (mMotionFragment == null) {
            mMotionFragment = new TCMotionFragment();
        }
        showFragment(mMotionFragment, "motion_fragment");
    }

    @Override
    public void onClickBGM() {
        if (mBGMSettingFragment == null) {
            mBGMSettingFragment = new TCBGMSettingFragment();
        }
        showFragment(mBGMSettingFragment, "bgm_setting_fragment");
    }

    @Override
    public void onClickPaster() {
        stopPlay();
        Intent intent = new Intent(this, TCPasterActivity.class);
        intent.putExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, mIsPicCombine);
        intent.putExtra(TCConstants.VIDEO_EDITER_IMPORT, mNeedProcessVideo);
        startActivityForResult(intent, TCConstants.REQUEST_CODE_PASTER);
    }

    @Override
    public void onClickBubbleWord() {
        stopPlay();
        Intent intent = new Intent(this, TCWordEditActivity.class);
        intent.putExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, mIsPicCombine);
        intent.putExtra(TCConstants.VIDEO_EDITER_IMPORT, mNeedProcessVideo);
        startActivityForResult(intent, TCConstants.REQUEST_CODE_WORD);
    }


    private void showFragment(Fragment fragment, String tag) {
        if (fragment == mCurrentFragment) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.editer_fl_container, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        mCurrentFragment = fragment;
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.editer_back_ll) {
            finish();
        } else if (i == R.id.editer_tv_done) {
            startGenerate();
        } else if (i == R.id.editer_ib_play) {
            playVideo(false);
        }
    }

    /**
     * 如果是滤镜特效的界面调用：
     * 1、在播放状态下，按住滤镜不会停止播放
     * 2、播放到末尾了，按住时，不会重新播放
     *
     * @param isMotionFilter
     */
    public void playVideo(boolean isMotionFilter) {
        TXCLog.i(TAG, "editer_ib_play clicked, mCurrentState = " + mCurrentState);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            startPlay(getCutterStartTime(), getCutterEndTime());
        } else if ((mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) && !isMotionFilter) {
            pausePlay();
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            resumePlay();
        } else if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            if ((mPreviewAtTime >= getCutterEndTime() || mPreviewAtTime <= getCutterStartTime()) && !isMotionFilter) {
                startPlay(getCutterStartTime(), getCutterEndTime());
            } else if (!TCVideoEditerWrapper.getInstance().isReverse()) {
                startPlay(mPreviewAtTime, getCutterEndTime());
            } else {
                startPlay(getCutterStartTime(), mPreviewAtTime);
            }
        }
    }

    /**
     * =========================================视频生成相关==========================================
     */
    private void startGenerate() {
        stopPlay(); // 停止播放
        mTXVideoEditer.cancel(); // 注意：生成时，停止输出缩略图

        mIbPlay.setImageResource(R.drawable.ic_play);
        if (mIsPicCombine) {
            startGenerateVideo();
            return;
        }

        AlertDialog.Builder normalDialog = new AlertDialog.Builder(TCVideoEditerActivity.this, R.style.ConfirmDialogStyle);
        normalDialog.setMessage("选择生成模式");
        normalDialog.setCancelable(true);
        normalDialog.setNegativeButton("生成视频", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startGenerateVideo();
            }
        });
        normalDialog.setPositiveButton("原视频转换为gif", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startGenerateGif();
            }
        });
        normalDialog.show();
    }

    private void startGenerateGif() {
        if(mGifStart){
            Toast.makeText(TCVideoEditerActivity.this, "正在生成gif，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(TCVideoEditerActivity.this, "开始生成gif", Toast.LENGTH_SHORT).show();
        mGifStart = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Bitmap> thumbnailList = TCVideoEditerWrapper.getInstance().getAllThumbnails();
                    String gifPath = GifUtil.createGifByBitmaps(getGifFilePath(), thumbnailList, 200, 100, 100);
                    notifyGifFinish(gifPath);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mGifStart = false;
                }
            }

            private void notifyGifFinish(final String gifPath) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TCVideoEditerActivity.this, "gif生成成功，存放位置：" + gifPath, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private String getGifFilePath(){
        File dir = new File("/sdcard/TXUGC/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir, "GifExample.gif");
        if (f.exists()) {
            f.delete();
        }
        return f.getAbsolutePath();
    }

    private void startGenerateVideo() {
        if(mGifStart){
            Toast.makeText(TCVideoEditerActivity.this, "正在生成gif，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        // 处于生成状态
        mCurrentState = PlayState.STATE_GENERATE;
        // 防止
        mTvDone.setEnabled(false);
        mTvDone.setClickable(false);
        // 生成视频输出路径
        mVideoOutputPath = TCEditerUtil.generateVideoPath();
        if (mWorkLoadingProgress == null) {
            initWorkLoadingProgress();
        }
        mWorkLoadingProgress.setProgress(0);
        mWorkLoadingProgress.setCancelable(false);
        mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");

        // 添加片尾水印
        addTailWaterMark();

        mTXVideoEditer.setCutFromTime(getCutterStartTime(), getCutterEndTime());
        mTXVideoEditer.setVideoGenerateListener(this);

        if (mCustomBitrate != 0) { // 是否自定义码率
            mTXVideoEditer.setVideoBitrate(mCustomBitrate);
        }
        if (mVideoResolution == -1) {// 默认情况下都将输出720的视频
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
        } else if (mVideoResolution == TXRecordCommon.VIDEO_RESOLUTION_360_640) {
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_360P, mVideoOutputPath);
        } else if (mVideoResolution == TXRecordCommon.VIDEO_RESOLUTION_540_960) {
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
        } else if (mVideoResolution == TXRecordCommon.VIDEO_RESOLUTION_720_1280) {
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
        }
    }

    private void stopGenerate() {
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setProgress(0);
            mWorkLoadingProgress.dismiss();
        }
        if (mCurrentState == PlayState.STATE_GENERATE) {
            mTvDone.setEnabled(true);
            mTvDone.setClickable(true);
            Toast.makeText(TCVideoEditerActivity.this, "取消视频生成", Toast.LENGTH_SHORT).show();

            mCurrentState = PlayState.STATE_NONE;
            if (mTXVideoEditer != null) {
                mTXVideoEditer.cancel();
            }
        }
    }

    /**
     * 添加片尾水印
     */
    private void addTailWaterMark() {

        TXVideoEditConstants.TXVideoInfo info = TCVideoEditerWrapper.getInstance().getTXVideoInfo();

        Bitmap tailWaterMarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tcloud_logo);
        float widthHeightRatio = tailWaterMarkBitmap.getWidth() / (float) tailWaterMarkBitmap.getHeight();

        TXVideoEditConstants.TXRect txRect = new TXVideoEditConstants.TXRect();
        txRect.width = 0.25f; // 归一化的片尾水印，这里设置了一个固定值，水印占屏幕宽度的0.25。
        // 后面根据实际图片的宽高比，计算出对应缩放后的图片的宽度：txRect.width * videoInfo.width 和高度：txRect.width * videoInfo.width / widthHeightRatio，然后计算出水印放中间时的左上角位置
        txRect.x = (info.width - txRect.width * info.width) / (2f * info.width);
        txRect.y = (info.height - txRect.width * info.width / widthHeightRatio) / (2f * info.height);

        mTXVideoEditer.setTailWaterMark(tailWaterMarkBitmap, txRect, 3);
    }

    @Override // 生成进度回调
    public void onGenerateProgress(final float progress) {
        mWorkLoadingProgress.setProgress((int) (progress * 100));
    }

    @Override // 生成完成
    public void onGenerateComplete(final TXVideoEditConstants.TXGenerateResult result) {
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            // 生成成功
            createThumbFile(result);
        } else {
            TCConfirmDialog confirmDialog = TCConfirmDialog.newInstance("错误", result.descMsg, false, "取消", "取消");
            confirmDialog.setCancelable(false);
            confirmDialog.setOnConfirmCallback(new TCConfirmDialog.OnConfirmCallback() {
                @Override
                public void onSureCallback() {
                    if (mWorkLoadingProgress != null) {
                        mWorkLoadingProgress.dismiss();
                    }
                }

                @Override
                public void onCancelCallback() {
                }
            });
            confirmDialog.show(getSupportFragmentManager(), "confirm_dialog");
        }
        mTvDone.setEnabled(true);
        mTvDone.setClickable(true);
        mCurrentState = PlayState.STATE_NONE;
    }

    /**
     * ==========================================进度条==========================================
     */
    private void initWorkLoadingProgress() {
        if (mWorkLoadingProgress == null) {
            mWorkLoadingProgress = new VideoWorkProgressFragment();
            mWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopGenerate();

                    if (!mLoadThumbnailSucc) {
                        mVideoProgressController.setCurrentTimeMs(0);
                        reloadThumbnail();
                    }
                }
            });
        }
        mWorkLoadingProgress.setProgress(0);
    }

    private void reloadThumbnail() {
        if (mIsPicCombine) {
            return;
        }
        if (!mNeedProcessVideo) {
            mVideoProgressView.clearAll();
            TCVideoEditerWrapper.getInstance().cleaThumbnails();
            mVideoProgressController.setThumbnailPicListDisplayWidth(TCVideoEditerWrapper.mThumbnailCount);
            mTXVideoEditer.getThumbnail(TCVideoEditerWrapper.mThumbnailCount, 100, 100, false, mThumbnailListener);
        }
    }

    public VideoProgressController getVideoProgressViewController() {
        return mVideoProgressController;
    }


    /*********************************************监听电话状态**************************************************/
    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TCVideoEditerActivity> mEditer;

        public TXPhoneStateListener(TCVideoEditerActivity editer) {
            mEditer = new WeakReference<TCVideoEditerActivity>(editer);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TCVideoEditerActivity activity = mEditer.get();
            if (activity == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    // 生成状态 取消生成
                    if (activity.mCurrentState == PlayState.STATE_GENERATE) {
                        activity.stopGenerate();
                    }
                    // 直接停止播放
                    activity.stopPlay();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }
}
