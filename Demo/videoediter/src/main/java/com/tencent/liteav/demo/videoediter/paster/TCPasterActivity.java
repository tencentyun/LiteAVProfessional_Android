package com.tencent.liteav.demo.videoediter.paster;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videoediter.common.utils.FileUtils;
import com.tencent.liteav.demo.videoediter.common.utils.TCConstants;
import com.tencent.liteav.demo.videoediter.common.utils.PlayState;
import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.TCVideoEditerWrapper;
import com.tencent.liteav.demo.videoediter.common.widget.layer.TCLayerOperationView;
import com.tencent.liteav.demo.videoediter.common.widget.layer.TCLayerViewGroup;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.RangeSliderViewContainer;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressController;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressView;
import com.tencent.liteav.demo.videoediter.paster.view.PasterAdapter;
import com.tencent.liteav.demo.videoediter.paster.view.PasterOperationView;
import com.tencent.liteav.demo.videoediter.paster.view.TCPasterOperationViewFactory;
import com.tencent.liteav.demo.videoediter.paster.view.TCPasterSelectView;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/10/24.
 */

public class TCPasterActivity extends Activity implements View.OnClickListener,
        TCLayerViewGroup.OnItemClickListener,
        TCLayerOperationView.IOperationViewClickListener,
        TCVideoEditerWrapper.TXVideoPreviewListenerWrapper,
        TCPasterSelectView.OnTabChangedListener,
        TCPasterSelectView.OnAddClickListener,
        PasterAdapter.OnItemClickListener {
    private static final String TAG = "TCPasterActivity";

    private final int MSG_COPY_PASTER_FILES = 1;

    private final String PASTER_FOLDER_NAME = "paster";
    private final String ANIMATED_PASTER_FOLDER_NAME = "AnimatedPaster";
    private final String PASTER_LIST_JSON_FILE_NAME = "pasterList.json";


    //================================== 默认的时间 ==============================
    private long mDefaultWordStartTime;
    private long mDefaultWordEndTime;

    //================================== 播放控制相关 ============================
    private int mCurrentState = PlayState.STATE_NONE;

    private TCVideoEditerWrapper wrapper;
    //==================================SDK相关==================================
    private TXVideoEditer mTXVideoEditer;
    private FrameLayout mLayoutPlayer;
    private long mCutterEndTime;
    private long mCutterStartTime;
    private long mCutterDuration;
    private float mSpeedLevel = 1.0f;

    //==================================头布局===================================
    private LinearLayout mLlBack;
    private TextView mTvSave;

    //==================================中部图层移动布局===========================
    private TCLayerViewGroup mTCLayerViewGroup;//图层父布局

    //==================================播放布局==================================
    private View mPlayerViewContainer;
    private ImageView mIvPlay;

    //==================================贴纸选择布局==================================
    private Button mBtnAddPaster;
    private TCPasterSelectView mTCPasterSelectView;
    private int mCurrentSelectedPos = -1;// 当前被选中的气泡字幕控件

    private List<TCPasterInfo> mPasterInfoList;
    private List<TCPasterInfo> mAnimatedPasterInfoList;

    // 子线程
    private HandlerThread mWorkHandlerThread;
    private Handler mWorkHandler;

    private String mPasterSDcardFolder;
    private String mAnimatedPasterSDcardFolder;

    private boolean mIsUpdatePng = false;

    private TXVideoEditConstants.TXVideoInfo mTXVideoInfo;
    private int mScreenWidth;
    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    private VideoProgressController.VideoProgressSeekListener mVideoProgressSeekListener;
    private RangeSliderViewContainer.OnDurationChangeListener mOnDurationChangeListener;
    private List<Bitmap> mThumbnailList;

    private long mPreviewAtTime;
    private boolean mIsPicCombine;
    private boolean mNeedProcessVideo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paster_edit);

        initData();

        initViews();

        initPlayer();

        initHandler();

        mWorkHandler.sendEmptyMessage(MSG_COPY_PASTER_FILES);

        prepareToRecover();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay(false);
    }

    private void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY
                || mCurrentState == PlayState.STATE_PAUSE || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoEditer.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            mIvPlay.setImageResource(R.drawable.icon_word_play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWorkHandlerThread != null) {
            mWorkHandlerThread.quit();
        }
        TCVideoEditerWrapper.getInstance().removeTXVideoPreviewListenerWrapper(this);
    }

    private void initHandler() {
        mWorkHandlerThread = new HandlerThread("TCPasterActivity_handlerThread");
        mWorkHandlerThread.start();
        mWorkHandler = new Handler(mWorkHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_COPY_PASTER_FILES:
                        if (mIsUpdatePng) {
                            FileUtils.deleteFile(mPasterSDcardFolder);
                            FileUtils.deleteFile(mAnimatedPasterSDcardFolder);
                        }
                        File pasterFolder = new File(mPasterSDcardFolder);
                        File animatedPasterFolder = new File(mAnimatedPasterSDcardFolder);
                        if (!pasterFolder.exists() || !animatedPasterFolder.exists()) {
                            copyPasterFilesToSdcard();
                        }
                        preparePasterInfoToShow();
                        break;
                }
            }
        };
    }

    private void prepareToRecover() {
        if (!mIsUpdatePng) {
            recoverFromManager();
        }
    }

    private void preparePasterInfoToShow() {
        mPasterInfoList = getPasterInfoList(PasterOperationView.TYPE_CHILD_VIEW_PASTER, mPasterSDcardFolder, PASTER_LIST_JSON_FILE_NAME);
        mAnimatedPasterInfoList = getPasterInfoList(PasterOperationView.TYPE_CHILD_VIEW_ANIMATED_PASTER, mAnimatedPasterSDcardFolder, PASTER_LIST_JSON_FILE_NAME);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentTab = mTCPasterSelectView.getCurrentTab();
                changeListViewData(currentTab);
            }
        });
    }

    private void copyPasterFilesToSdcard() {
        File pasterFolder = new File(mPasterSDcardFolder);
        if (!pasterFolder.exists()) {
            FileUtils.copyFilesFromAssets(TCPasterActivity.this, PASTER_FOLDER_NAME, mPasterSDcardFolder);
        }

        File animatedFolder = new File(mAnimatedPasterSDcardFolder);
        if (!animatedFolder.exists()) {
            FileUtils.copyFilesFromAssets(TCPasterActivity.this, ANIMATED_PASTER_FOLDER_NAME, mAnimatedPasterSDcardFolder);
        }
    }

    private void initPlayer() {
        mTXVideoEditer.stopPlay();
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mLayoutPlayer;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    private void initData() {
        mIsPicCombine = getIntent().getBooleanExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, false);
        mNeedProcessVideo = getIntent().getBooleanExtra(TCConstants.VIDEO_EDITER_IMPORT, false);
        wrapper = TCVideoEditerWrapper.getInstance();
        wrapper.addTXVideoPreviewListenerWrapper(this);
        mTXVideoEditer = wrapper.getEditer();
        mTXVideoInfo = wrapper.getTXVideoInfo();

        mCutterStartTime = wrapper.getCutterStartTime();
        mCutterEndTime = wrapper.getCutterEndTime();
        mCutterDuration = mCutterEndTime - mCutterStartTime;

        updateDefaultTime();

        mPasterSDcardFolder = getExternalFilesDir(null) + File.separator + PASTER_FOLDER_NAME + File.separator;
        mAnimatedPasterSDcardFolder = getExternalFilesDir(null) + File.separator + ANIMATED_PASTER_FOLDER_NAME + File.separator;

        initVideoProgressSeekListener();
        initRangeDurationChangeListener();

        mThumbnailList = wrapper.getAllThumbnails();
    }

    private long getCutterStartTime() {
        return wrapper.getCutterStartTime();
    }

    private long getCutterEndTime() {
        return wrapper.getCutterEndTime();
    }

    /**
     * 根据当前控件数量 更新默认的一个控件开始时间和结束时间
     */
    private void updateDefaultTime() {
        int count = mTCLayerViewGroup != null ? mTCLayerViewGroup.getChildCount() : 0;
        mDefaultWordStartTime = mCutterStartTime + count * 3000; // 两个之间间隔3秒
        mDefaultWordEndTime = mDefaultWordStartTime + 2000;

        if (mDefaultWordStartTime > mCutterEndTime) {
            mDefaultWordStartTime = mCutterEndTime - 2000;
            mDefaultWordEndTime = mCutterEndTime;
        } else if (mDefaultWordEndTime > mCutterEndTime) {
            mDefaultWordEndTime = mCutterEndTime;
        }
    }

    private void initViews() {
        mLlBack = (LinearLayout) findViewById(R.id.back_ll);
        mLlBack.setOnClickListener(this);

        mTvSave = (TextView) findViewById(R.id.tv_done);
        mTvSave.setOnClickListener(this);

        mTCLayerViewGroup = (TCLayerViewGroup) findViewById(R.id.paster_container);
        mTCLayerViewGroup.setOnItemClickListener(this);

        mPlayerViewContainer = findViewById(R.id.paster_fl_player);
        mPlayerViewContainer.setOnClickListener(this);
        mLayoutPlayer = (FrameLayout) findViewById(R.id.paster_fl_video_view);

        mIvPlay = (ImageView) findViewById(R.id.btn_play);
        mIvPlay.setOnClickListener(this);

        mBtnAddPaster = (Button) findViewById(R.id.paster_btn_add);
        mBtnAddPaster.setOnClickListener(this);

        mTCPasterSelectView = (TCPasterSelectView) findViewById(R.id.tcpaster_select_view);
        mTCPasterSelectView.setOnTabChangedListener(this);
        mTCPasterSelectView.setOnItemClickListener(this);
        mTCPasterSelectView.setOnAddClickListener(this);
        mTCPasterSelectView.setVisibility(View.GONE);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        mVideoProgressView = (VideoProgressView) findViewById(R.id.video_progress_view);
        mVideoProgressView.setViewWidth(mScreenWidth);
        mVideoProgressView.setThumbnailData();
        mVideoProgressView.addAllThumbnail(mThumbnailList);

        mVideoProgressController = new VideoProgressController(mTXVideoInfo.duration);
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        if (mIsPicCombine) {
            mVideoProgressController.setThumbnailPicListDisplayWidth(mVideoProgressView.getThumbnailCount());
        } else {
            if (mNeedProcessVideo) {
                mVideoProgressController.setThumbnailPicListDisplayWidth(mVideoProgressView.getThumbnailCount());
            } else {
                mVideoProgressController.setThumbnailPicListDisplayWidth(TCVideoEditerWrapper.mThumbnailCount);
            }
        }
        mVideoProgressController.setVideoProgressSeekListener(mVideoProgressSeekListener);
        mVideoProgressController.setVideoProgressDisplayWidth(mScreenWidth);
    }

    private void initRangeDurationChangeListener() {
        mOnDurationChangeListener = new RangeSliderViewContainer.OnDurationChangeListener() {
            @Override
            public void onDurationChange(long startTime, long endTime) {
                // 获取当选中的贴纸，并且将时间设置进去
                PasterOperationView view = (PasterOperationView) mTCLayerViewGroup.getSelectedLayerOperationView();
                if (view != null) {
                    view.setStartTime(startTime, endTime);
                }
                // 时间范围修改也马上设置到sdk中去
                addPasterListVideo();
            }
        };
    }

    private void initVideoProgressSeekListener() {
        mVideoProgressSeekListener = new VideoProgressController.VideoProgressSeekListener() {
            @Override
            public void onVideoProgressSeek(long currentTimeMs) {
                TXCLog.i(TAG, "onVideoProgressSeek, currentTimeMs = " + currentTimeMs);
                pausePlay(true);
                mPreviewAtTime = currentTimeMs;
                mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
                mTXVideoEditer.previewAtTime(currentTimeMs);
            }

            @Override
            public void onVideoProgressSeekFinish(long currentTimeMs) {
                TXCLog.i(TAG, "onVideoProgressSeekFinish, currentTimeMs = " + currentTimeMs);
                pausePlay(true);
                mPreviewAtTime = currentTimeMs;
                mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
                mTXVideoEditer.previewAtTime(currentTimeMs);
            }
        };
    }

    private List<TCPasterInfo> getPasterInfoList(int pasterType, String fileFolder, String fileName) {
        String filePath = fileFolder + fileName;
        List<TCPasterInfo> pasterInfoList = new ArrayList<TCPasterInfo>();
        try {
            String jsonString = FileUtils.getJsonFromFile(filePath);
            if (TextUtils.isEmpty(jsonString)) {
                TXCLog.e(TAG, "getPasterInfoList, jsonString is empty");
                return pasterInfoList;
            }
            JSONObject pasterJson = new JSONObject(jsonString);
            JSONArray pasterInfoJsonArray = pasterJson.getJSONArray("pasterList");
            for (int i = 0; i < pasterInfoJsonArray.length(); i++) {
                JSONObject pasterInfoJsonObject = pasterInfoJsonArray.getJSONObject(i);
                TCPasterInfo tcPasterInfo = new TCPasterInfo();

                tcPasterInfo.setName(pasterInfoJsonObject.getString("name"));
                tcPasterInfo.setIconPath(fileFolder + pasterInfoJsonObject.getString("icon"));
                tcPasterInfo.setPasterType(pasterType);

                pasterInfoList.add(tcPasterInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pasterInfoList;
    }

    private void startPlay(long startTime, long endTime) {
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoEditer.startPlayFromTime(startTime, endTime);
            mCurrentState = PlayState.STATE_PLAY;

            mIvPlay.setImageResource(R.drawable.icon_word_pause);

            // 后台切换回来的时候 需要隐藏掉这个
            mTCLayerViewGroup.setVisibility(View.INVISIBLE);
        }
    }

    private void playVideo() {
        int selectedIndex = mTCLayerViewGroup.getSelectedViewIndex();
        if (selectedIndex != -1) {// 说明有控件被选中 那么显示出时间区间的选择
            RangeSliderViewContainer view = mVideoProgressController.getRangeSliderView(selectedIndex);
            view.setEditComplete();
        }
        // 再次播放的时候，会顶层控件隐藏，将字幕添加入视频画面中。
        mIvPlay.setImageResource(R.drawable.icon_word_pause);
        mTCLayerViewGroup.setVisibility(View.INVISIBLE);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            startPlay(getCutterStartTime(), getCutterEndTime());
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.resumePlay();
            mCurrentState = PlayState.STATE_RESUME;
        } else if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            if ((mPreviewAtTime >= getCutterEndTime() || mPreviewAtTime <= getCutterStartTime())) {
                startPlay(getCutterStartTime(), getCutterEndTime());
            } else if (!TCVideoEditerWrapper.getInstance().isReverse()) {
                startPlay(mPreviewAtTime, getCutterEndTime());
            } else {
                startPlay(getCutterStartTime(), mPreviewAtTime);
            }
        }
    }

    private void pausePlay(boolean isShow) {
        if (isShow) {
            // 将字幕控件显示出来
            mTCLayerViewGroup.setVisibility(View.VISIBLE);
            mTXVideoEditer.refreshOneFrame();// 将视频画面中的字幕清除  ，避免与上层控件造成混淆导致体验不好的问题。
        }
        int selectedIndex = mTCLayerViewGroup.getSelectedViewIndex();
        if (selectedIndex != -1) {// 说明有控件被选中 那么显示出时间区间的选择
            RangeSliderViewContainer view = mVideoProgressController.getRangeSliderView(selectedIndex);
            if (isShow) {
                view.showEdit();
            } else {
                view.setEditComplete();
            }
        }

        if (mCurrentState == PlayState.STATE_PLAY || mCurrentState == PlayState.STATE_RESUME) {
            mTXVideoEditer.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            mIvPlay.setImageResource(R.drawable.icon_word_play);
        }
    }

    /**
     * ===========================将贴纸添加到SDK中去=================================
     */

    private void addPasterListVideo() {
        List<TXVideoEditConstants.TXAnimatedPaster> animatedPasterList = new ArrayList<>();
        List<TXVideoEditConstants.TXPaster> pasterList = new ArrayList<>();
        for (int i = 0; i < mTCLayerViewGroup.getChildCount(); i++) {
            PasterOperationView view = (PasterOperationView) mTCLayerViewGroup.getOperationView(i);
            TXVideoEditConstants.TXRect rect = new TXVideoEditConstants.TXRect();
            rect.x = view.getImageX();
            rect.y = view.getImageY();
            rect.width = view.getImageWidth();
            TXCLog.i(TAG, "addPasterListVideo, adjustPasterRect, paster x y = " + rect.x + "," + rect.y);

            int childType = view.getChildType();
            if (childType == PasterOperationView.TYPE_CHILD_VIEW_ANIMATED_PASTER) {
                TXVideoEditConstants.TXAnimatedPaster txAnimatedPaster = new TXVideoEditConstants.TXAnimatedPaster();

                txAnimatedPaster.animatedPasterPathFolder = mAnimatedPasterSDcardFolder + view.getPasterName() + File.separator;
                txAnimatedPaster.startTime = view.getStartTime();
                txAnimatedPaster.endTime = view.getEndTime();
                txAnimatedPaster.frame = rect;
                txAnimatedPaster.rotation = view.getImageRotate();

                animatedPasterList.add(txAnimatedPaster);
                TXCLog.i(TAG, "addPasterListVideo, txAnimatedPaster startTimeMs, endTime is : " + txAnimatedPaster.startTime + ", " + txAnimatedPaster.endTime);
            } else if (childType == PasterOperationView.TYPE_CHILD_VIEW_PASTER) {
                TXVideoEditConstants.TXPaster txPaster = new TXVideoEditConstants.TXPaster();

                txPaster.pasterImage = view.getRotateBitmap();
                txPaster.startTime = view.getStartTime();
                txPaster.endTime = view.getEndTime();
                txPaster.frame = rect;

                pasterList.add(txPaster);
                TXCLog.i(TAG, "addPasterListVideo, txPaster startTimeMs, endTime is : " + txPaster.startTime + ", " + txPaster.endTime);
            }
        }
        mTXVideoEditer.setAnimatedPasterList(animatedPasterList);
        mTXVideoEditer.setPasterList(pasterList);
    }

    @Override
    public void onBackPressed() {
        clickBack();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.back_ll) {
            clickBack();

        } else if (i == R.id.btn_play) {
            onClickPlay();

        } else if (i == R.id.paster_btn_add) {
            clickBtnAdd();

        }

    }

    private void clickBack() {
        TXLog.i(TAG, "clickBack, stop play");
        saveIntoManager();
        stopPlay();
        finish();
    }

    private void clickBtnAdd() {
        mTCPasterSelectView.show();
        pausePlay(true);
    }

    private void onClickPlay() {
        if (mCurrentState == PlayState.STATE_PAUSE || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            playVideo();
        } else if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            pausePlay(true);
        }
    }

    /**
     * ===========================Group控件的回调=================================
     */
    @Override
    public void onLayerOperationViewItemClick(TCLayerOperationView view, int lastSelectedPos, int currentSelectedPos) {
        pausePlay(true);

        RangeSliderViewContainer lastSlider = mVideoProgressController.getRangeSliderView(lastSelectedPos);
        if (lastSlider != null) {
            lastSlider.setEditComplete();
        }

        RangeSliderViewContainer currentSlider = mVideoProgressController.getRangeSliderView(currentSelectedPos);
        if (currentSlider != null) {
            currentSlider.showEdit();
        }

        mCurrentSelectedPos = currentSelectedPos;
    }

    /****** 可编辑控件的回调start ******/
    @Override
    public void onDeleteClick() {
        int index = mTCLayerViewGroup.getSelectedViewIndex();
        PasterOperationView view = (PasterOperationView) mTCLayerViewGroup.getSelectedLayerOperationView();
        if (view != null) {
            mTCLayerViewGroup.removeOperationView(view);
        }
        mVideoProgressController.removeRangeSliderView(index);
        addPasterListVideo();
    }

    @Override
    public void onEditClick() {
        TXCLog.i(TAG, "onEditClick");
        addPasterListVideo();
    }

    @Override
    public void onRotateClick() {
        TXCLog.i(TAG, "onRotateClick");
        addPasterListVideo();
    }


    @Override
    public void onTabChanged(int currentTab) {
        changeListViewData(currentTab);
    }

    private void changeListViewData(int currentTab) {
        if (currentTab == TCPasterSelectView.TAB_PASTER) {
            mTCPasterSelectView.setPasterInfoList(mPasterInfoList);
        } else if (currentTab == TCPasterSelectView.TAB_ANIMATED_PASTER) {
            mTCPasterSelectView.setPasterInfoList(mAnimatedPasterInfoList);
        }
    }

    @Override
    public void onAdd() {
        addPasterListVideo();
    }

    @Override
    public void onItemClick(final TCPasterInfo tcPasterInfo, int position) {
        // 上一个不显示编辑区域
        int index = mTCLayerViewGroup.getSelectedViewIndex();
        Log.i(TAG, "onItemClick: index = " + index);
        RangeSliderViewContainer lastSlider = mVideoProgressController.getRangeSliderView(index);
        if (lastSlider != null) {
            lastSlider.setEditComplete();
        } else {
            Log.e(TAG, "onItemClick: slider view is null");
        }

        String pasterPath = null;
        Bitmap bitmap = null;
        int pasterType = tcPasterInfo.getPasterType();
        if (pasterType == PasterOperationView.TYPE_CHILD_VIEW_ANIMATED_PASTER) {
            AnimatedPasterConfig animatedPasterConfig = getAnimatedPasterParamFromPath(mAnimatedPasterSDcardFolder + tcPasterInfo.getName() + File.separator);
            if (animatedPasterConfig == null) {
                TXCLog.e(TAG, "onItemClick, animatedPasterConfig is null");
                return;
            }
            int keyFrameIndex = animatedPasterConfig.keyframe;
            String keyFrameName = animatedPasterConfig.frameArray.get(keyFrameIndex - 1).pictureName;
            pasterPath = mAnimatedPasterSDcardFolder + tcPasterInfo.getName() + File.separator + keyFrameName + ".png";
            bitmap = BitmapFactory.decodeFile(pasterPath);
        } else if (pasterType == PasterOperationView.TYPE_CHILD_VIEW_PASTER) {
            pasterPath = mPasterSDcardFolder + tcPasterInfo.getName() + File.separator + tcPasterInfo.getName() + ".png";
            bitmap = BitmapFactory.decodeFile(pasterPath);
        }
        // 更新一下默认配置的时间
        updateDefaultTime();

        PasterOperationView pasterOperationView = TCPasterOperationViewFactory.newOperationView(TCPasterActivity.this);
        pasterOperationView.setPasterPath(pasterPath);
        pasterOperationView.setChildType(tcPasterInfo.getPasterType());
        pasterOperationView.setImageBitamp(bitmap);
        pasterOperationView.setCenterX(mTCLayerViewGroup.getWidth() / 2);
        pasterOperationView.setCenterY(mTCLayerViewGroup.getHeight() / 2);
        pasterOperationView.setStartTime(mDefaultWordStartTime, mDefaultWordEndTime);
        pasterOperationView.setIOperationViewClickListener(TCPasterActivity.this);
        pasterOperationView.setPasterName(tcPasterInfo.getName());

        RangeSliderViewContainer rangeSliderView = new RangeSliderViewContainer(TCPasterActivity.this);
        rangeSliderView.init(mVideoProgressController, mDefaultWordStartTime, mDefaultWordEndTime - mDefaultWordStartTime, mTXVideoInfo.duration);
        rangeSliderView.setDurationChangeListener(mOnDurationChangeListener);
        mVideoProgressController.addRangeSliderView(rangeSliderView);
        mVideoProgressController.setCurrentTimeMs(mDefaultWordStartTime);

        mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
        mPreviewAtTime = mDefaultWordStartTime;

        mTCLayerViewGroup.addOperationView(pasterOperationView);
        mTCLayerViewGroup.post(new Runnable() {
            @Override
            public void run() {
                addPasterListVideo();
            }
        });
    }

    /**
     * ===========================将贴纸控件参数保存到Manager中去=================================
     * <p>
     * 将贴纸控件的相关参数保存到Manager中去，方便出去之后可以重新进来再次编辑贴纸
     */
    private void saveIntoManager() {
        TXCLog.i(TAG, "saveIntoManager");
        TCPasterViewInfoManager manager = TCPasterViewInfoManager.getInstance();
        manager.clear();
        for (int i = 0; i < mTCLayerViewGroup.getChildCount(); i++) {
            PasterOperationView view = (PasterOperationView) mTCLayerViewGroup.getOperationView(i);

            TXCLog.i(TAG, "saveIntoManager, view centerX and centerY = " + view.getCenterX() + ", " + view.getCenterY() +
                    ", start end time = " + view.getStartTime() + ", " + view.getEndTime());

            TCPasterViewInfo info = new TCPasterViewInfo();
            info.setViewCenterX(view.getCenterX());
            info.setViewCenterY(view.getCenterY());
            info.setRotation(view.getImageRotate());
            info.setImageScale(view.getImageScale());
            info.setPasterPath(view.getPasterPath());
            info.setStartTime(view.getStartTime());
            info.setEndTime(view.getEndTime());
            info.setName(view.getPasterName());
            info.setViewType(view.getChildType());

            manager.add(info);
        }
    }

    /**
     * 将贴纸控件的相关参数从Manager中重新恢复出来，恢复贴纸编辑的场景。 以便继续编辑
     */
    private void recoverFromManager() {
        TCPasterViewInfoManager manager = TCPasterViewInfoManager.getInstance();
        TXCLog.i(TAG, "recoverFromManager, manager.size = " + manager.getSize());
        for (int i = 0; i < manager.getSize(); i++) {
            TCPasterViewInfo info = manager.get(i);
            Bitmap pasterBitmap = BitmapFactory.decodeFile(info.getPasterPath());
            TXCLog.i(TAG, "recoverFromManager, info.getPasterPath() = " + info.getPasterPath());
            if (pasterBitmap == null) {
                TXCLog.e(TAG, "recoverFromManager, pasterBitmap is null!");
                continue;
            }
            PasterOperationView view = TCPasterOperationViewFactory.newOperationView(TCPasterActivity.this);
            view.setImageBitamp(pasterBitmap);
            view.setChildType(info.getViewType());
            view.setCenterX(info.getViewCenterX());
            view.setCenterY(info.getViewCenterY());
            view.setImageRotate(info.getRotation());
            view.setImageScale(info.getImageScale());
            view.setPasterPath(info.getPasterPath());
            view.setPasterName(info.getName());
            view.setIOperationViewClickListener(this);

            // 恢复时间的时候，需要检查一下是否符合这一次区间的startTime和endTime
            long viewStartTime = info.getStartTime();
            long viewEndTime = info.getEndTime();
            view.setStartTime(viewStartTime, viewEndTime);

            RangeSliderViewContainer rangeSliderView = new RangeSliderViewContainer(TCPasterActivity.this);
            rangeSliderView.init(mVideoProgressController, viewStartTime, viewEndTime - viewStartTime, mTXVideoInfo.duration);
            rangeSliderView.setDurationChangeListener(mOnDurationChangeListener);
            rangeSliderView.setEditComplete();
            mVideoProgressController.addRangeSliderView(rangeSliderView);
            mTCLayerViewGroup.addOperationView(view);// 添加到Group中去管理
        }
        mCurrentSelectedPos = manager.getSize() - 1;
    }

    private AnimatedPasterConfig getAnimatedPasterParamFromPath(String pathFolder) {
        AnimatedPasterConfig animatedPasterConfig = null;
        String configPath = pathFolder + AnimatedPasterConfig.FILE_NAME;

        String configJsonStr = FileUtils.getJsonFromFile(configPath);

        if (TextUtils.isEmpty(configJsonStr)) {
            TXCLog.e(TAG, "getTXAnimatedPasterParamFromPath, configJsonStr is empty");
            return animatedPasterConfig;
        }

        JSONObject jsonObjectConfig = null;
        try {
            jsonObjectConfig = new JSONObject(configJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObjectConfig == null) {
            TXCLog.e(TAG, "getTXAnimatedPasterParamFromPath, jsonObjectConfig is null");
            return animatedPasterConfig;
        }

        animatedPasterConfig = new AnimatedPasterConfig();
        try {
            animatedPasterConfig.name = jsonObjectConfig.getString(AnimatedPasterConfig.CONFIG_NAME);
            animatedPasterConfig.count = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_COUNT);
            animatedPasterConfig.period = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_PERIOD);
            animatedPasterConfig.width = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_WIDTH);
            animatedPasterConfig.height = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_HEIGHT);
            animatedPasterConfig.keyframe = jsonObjectConfig.getInt(AnimatedPasterConfig.CONFIG_KEYFRAME);
            JSONArray frameJsonArray = jsonObjectConfig.getJSONArray(AnimatedPasterConfig.CONFIG_KEYFRAME_ARRAY);
            for (int i = 0; i < animatedPasterConfig.count; i++) {
                JSONObject frameNameObject = frameJsonArray.getJSONObject(i);
                AnimatedPasterConfig.PasterPicture pasterPicture = new AnimatedPasterConfig.PasterPicture();
                pasterPicture.pictureName = frameNameObject.getString(AnimatedPasterConfig.PasterPicture.PICTURE_NAME);

                animatedPasterConfig.frameArray.add(pasterPicture);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return animatedPasterConfig;
    }

    @Override
    public void onPreviewProgressWrapper(final int time) {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mVideoProgressController.setCurrentTimeMs(time);
        }
    }

    @Override
    public void onPreviewFinishedWrapper() {
        if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            return;
        }
        mCurrentState = PlayState.STATE_STOP;
        startPlay(getCutterStartTime(), getCutterEndTime());//再次播放
    }

    @Override
    public void onPreviewError(TXVideoEditConstants.TXPreviewError error) {
        Toast.makeText(this,"预览播放失败：" + error.errorMsg, Toast.LENGTH_SHORT).show();
    }
}
