package com.tencent.liteav.demo.videoediter.bubble;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.liteav.demo.videoediter.common.utils.TCConstants;
import com.tencent.liteav.demo.videoediter.common.utils.PlayState;
import com.tencent.liteav.demo.videoediter.R;
import com.tencent.liteav.demo.videoediter.TCVideoEditerWrapper;
import com.tencent.liteav.demo.videoediter.bubble.ui.bubble.TCBubbleViewParams;
import com.tencent.liteav.demo.videoediter.bubble.ui.bubble.TCWordBubbleView;
import com.tencent.liteav.demo.videoediter.bubble.ui.bubble.TCWordBubbleViewFactory;
import com.tencent.liteav.demo.videoediter.bubble.ui.others.TCWordInputDialog;
import com.tencent.liteav.demo.videoediter.bubble.ui.popwin.TCBubbleSettingView;
import com.tencent.liteav.demo.videoediter.bubble.ui.popwin.TCWordParamsInfo;
import com.tencent.liteav.demo.videoediter.bubble.utils.TCBubbleManager;
import com.tencent.liteav.demo.videoediter.common.widget.layer.TCLayerOperationView;
import com.tencent.liteav.demo.videoediter.common.widget.layer.TCLayerViewGroup;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.RangeSliderViewContainer;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressController;
import com.tencent.liteav.demo.videoediter.common.widget.videotimeline.VideoProgressView;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by hans on 2017/10/19.
 */
public class TCWordEditActivity extends FragmentActivity implements View.OnClickListener,
        TCVideoEditerWrapper.TXVideoPreviewListenerWrapper,
        TCWordInputDialog.OnWordInputCallback,
        TCBubbleSettingView.OnWordInfoCallback,
        TCBubbleSettingView.OnAddClickListener,
        TCWordBubbleView.IOperationViewClickListener,
        TCLayerViewGroup.OnItemClickListener {
    private static final String TAG = "TCWordEditActivity";

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
    private long mPreviewAtTime;

    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    //==================================头布局===================================
    private LinearLayout mLlBack;

    //==================================中部字幕移动布局===========================
    private TCLayerViewGroup mTCBubbleViewGroup;//字幕父布局

    //==================================播放布局==================================
    private ImageView mIvPlay;
    private Button mBtnAdd; //添加字幕按钮

    //==================================字幕样式选择布局==================================
    private TCWordInputDialog mWordInputDialog;
    private TCBubbleSettingView mBubblePopWin; // 气泡字幕的 背景、颜色的配置板

    private boolean mIsEditWordAgain = false;// 用于判定当前是否修改字幕内容

    private TXVideoEditConstants.TXVideoInfo mTXVideoInfo;
    private boolean mIsPicCombine;
    private boolean mNeedProcessVideo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_edit);
        mIsPicCombine = getIntent().getBooleanExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, false);
        mNeedProcessVideo = getIntent().getBooleanExtra(TCConstants.VIDEO_EDITER_IMPORT, false);
        wrapper = TCVideoEditerWrapper.getInstance();
        wrapper.addTXVideoPreviewListenerWrapper(this);
        mTXVideoEditer = wrapper.getEditer();
        mTXVideoInfo = wrapper.getTXVideoInfo();
        mCutterStartTime = wrapper.getCutterStartTime();
        mCutterEndTime = wrapper.getCutterEndTime();

        updateDefaultTime();
        initViews();
        initVideoProgressView();
        initPlayer();
        recoverFromManager();
    }

    /**
     * 根据当前控件数量 更新默认的一个控件开始时间和结束时间
     */
    private void updateDefaultTime() {
        int count = mTCBubbleViewGroup != null ? mTCBubbleViewGroup.getChildCount() : 0;
        mDefaultWordStartTime = mCutterStartTime + count * 3000; // 两个之间间隔3秒
        mDefaultWordEndTime = mDefaultWordStartTime + 2000;

        if (mDefaultWordStartTime > mCutterEndTime) {
            mDefaultWordStartTime = mCutterEndTime - 2000;
            mDefaultWordEndTime = mCutterEndTime;
        } else if (mDefaultWordEndTime > mCutterEndTime) {
            mDefaultWordEndTime = mCutterEndTime;
        }
    }

    private long getCutterStartTime() {
        return wrapper.getCutterStartTime();
    }

    private long getCutterEndTime() {
        return wrapper.getCutterEndTime();
    }

    private void initVideoProgressView() {
        TCVideoEditerWrapper wrapper = TCVideoEditerWrapper.getInstance();
        List<Bitmap> thumbnailList = wrapper.getThumbnailList(0, wrapper.getTXVideoInfo().duration);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        mVideoProgressView = (VideoProgressView) findViewById(R.id.video_progress_view);
        mVideoProgressView.setViewWidth(screenWidth);
        // 初始化缩略图
        mVideoProgressView.setThumbnailData();
        mVideoProgressView.addAllThumbnail(thumbnailList);

        mVideoProgressController = new VideoProgressController(wrapper.getTXVideoInfo().duration);
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
        mVideoProgressController.setVideoProgressSeekListener(new VideoProgressController.VideoProgressSeekListener() {
            @Override
            public void onVideoProgressSeek(long ptsMs) {
                pausePlay(true);
                mPreviewAtTime = ptsMs;
                mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
                mTXVideoEditer.previewAtTime(ptsMs);
            }

            @Override
            public void onVideoProgressSeekFinish(long ptsMs) {
                pausePlay(true);
                mPreviewAtTime = ptsMs;
                mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
                mTXVideoEditer.previewAtTime(ptsMs);
            }
        });
        mVideoProgressController.setVideoProgressDisplayWidth(screenWidth);
    }


    private void initViews() {
        mLlBack = (LinearLayout) findViewById(R.id.back_ll);
        mLlBack.setOnClickListener(this);

        mTCBubbleViewGroup = (TCLayerViewGroup) findViewById(R.id.word_bubble_container);
        mTCBubbleViewGroup.setOnItemClickListener(this);

        mLayoutPlayer = (FrameLayout) findViewById(R.id.word_fl_video_view);

        mIvPlay = (ImageView) findViewById(R.id.btn_play);
        mIvPlay.setOnClickListener(this);

        mVideoProgressView = (VideoProgressView) findViewById(R.id.video_progress_view);

        mBtnAdd = (Button) findViewById(R.id.word_btn_add);
        mBtnAdd.setOnClickListener(this);

        // 展示气泡样式修改的面板
        mBubblePopWin = (TCBubbleSettingView) findViewById(R.id.word_bubble_setting);
        mBubblePopWin.setBubbles(TCBubbleManager.getInstance(this).loadBubbles());
        mBubblePopWin.setOnAddClickListener(this);
        mBubblePopWin.setOnWordInfoCallback(this);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_ll) {
            clickBack();

        } else if (i == R.id.btn_play) {
            onClickPlay();

        } else if (i == R.id.word_btn_add) {
            onClickAddWord();

        }
    }

    private void clickBack() {
        TXLog.i(TAG, "clickBack, stop play");
        saveIntoManager();
        stopPlay();
        finish();
    }

    private void onClickAddWord() {
        pausePlay(true);
        showInputDialog(null);
    }

    private void showInputDialog(String text) {
        if (mWordInputDialog == null) {
            mWordInputDialog = new TCWordInputDialog();
        }
        mWordInputDialog.setOnWordInputCallback(this);
        mWordInputDialog.setCancelable(false);
        mWordInputDialog.setDefaultText(text);
        mWordInputDialog.show(getSupportFragmentManager(), "word_input_dialog");
    }

    private void onClickPlay() {
        if (mCurrentState == PlayState.STATE_PAUSE || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            playVideo();
        } else if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            pausePlay(true);
        }
    }

    /**
     * ===========================播放器以及Activity生命周期相关===========================
     */
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TCVideoEditerWrapper.getInstance().removeTXVideoPreviewListenerWrapper(this);
    }

    private void initPlayer() {
        mTXVideoEditer.stopPlay();
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mLayoutPlayer;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    private void startPlay(long startTime, long endTime) {
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoEditer.startPlayFromTime(startTime, endTime);
            mCurrentState = PlayState.STATE_PLAY;

            mIvPlay.setImageResource(R.drawable.icon_word_pause);
            // 后台切换回来的时候 需要隐藏掉这个
            mTCBubbleViewGroup.setVisibility(View.INVISIBLE);
        }
    }

    private void playVideo() {
        int selectedIndex = mTCBubbleViewGroup.getSelectedViewIndex();
        if (selectedIndex != -1) {// 说明有控件被选中 那么显示出时间区间的选择
            RangeSliderViewContainer view = mVideoProgressController.getRangeSliderView(selectedIndex);
            view.setEditComplete();
        }
        // 再次播放的时候，会顶层控件隐藏，将字幕添加入视频画面中。
        mIvPlay.setImageResource(R.drawable.icon_word_pause);
        mTCBubbleViewGroup.setVisibility(View.INVISIBLE);
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
            mTCBubbleViewGroup.setVisibility(View.VISIBLE);
            mTXVideoEditer.refreshOneFrame();// 将视频画面中的字幕清除  ，避免与上层控件造成混淆导致体验不好的问题。
        }
        int selectedIndex = mTCBubbleViewGroup.getSelectedViewIndex();
        if (selectedIndex != -1) {// 说明有控件被选中 那么显示出时间区间的选择
            RangeSliderViewContainer view = mVideoProgressController.getRangeSliderView(selectedIndex);
            if (isShow) {
                if (view != null)
                    view.showEdit();
            } else {
                if (view != null)
                    view.setEditComplete();
            }
        }

        if (mCurrentState == PlayState.STATE_PLAY || mCurrentState == PlayState.STATE_RESUME) {
            mTXVideoEditer.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            mIvPlay.setImageResource(R.drawable.icon_word_play);
        }
    }

    private void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY
                || mCurrentState == PlayState.STATE_PAUSE || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            mTXVideoEditer.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            mIvPlay.setImageResource(R.drawable.icon_word_play);
        }
    }

    /**
     * ===================================字幕输入回调===================================
     */
    @Override
    public void onInputSure(String text) {
        mWordInputDialog = null;
        if (!mIsEditWordAgain) {// 新增字幕

            // 上一个控件编辑完毕
            int index = mTCBubbleViewGroup.getSelectedViewIndex();
            if (index != -1) {
                RangeSliderViewContainer slider = mVideoProgressController.getRangeSliderView(index);
                if (slider != null) {
                    slider.setEditComplete();
                }
            }
            // 在时间戳上增加一个view
            updateDefaultTime();

            // 输入的时候，设置为可见
            mTCBubbleViewGroup.setVisibility(View.VISIBLE);
            // 创建一个默认的参数
            TCBubbleViewParams params = TCBubbleViewParams.createDefaultParams(text);
            // 添加到气泡view
            TCWordBubbleView view = createDefaultBubbleView(params);
            mTCBubbleViewGroup.addOperationView(view);// 添加到Group中去管理
            mTCBubbleViewGroup.post(new Runnable() {
                @Override
                public void run() {
                    addSubtitlesIntoVideo();
                }
            });

            RangeSliderViewContainer rangeSliderView = new RangeSliderViewContainer(this);
            rangeSliderView.init(mVideoProgressController, mDefaultWordStartTime, mDefaultWordEndTime - mDefaultWordStartTime, TCVideoEditerWrapper.getInstance().getTXVideoInfo().duration);
            rangeSliderView.setDurationChangeListener(mOnDurationChangeListener);
            mVideoProgressController.addRangeSliderView(rangeSliderView);
            mVideoProgressController.setCurrentTimeMs(mDefaultWordStartTime);
            mBubblePopWin.show(null);
        } else {
            // 修改字幕内容
            TCWordBubbleView view = (TCWordBubbleView) mTCBubbleViewGroup.getSelectedLayerOperationView();
            if (view != null) {
                TCBubbleViewParams params = view.getBubbleParams();
                params.text = text;
                params.bubbleBitmap = TCBubbleManager.getInstance(this).getBitmapFromAssets(params.wordParamsInfo.getBubbleInfo().getBubblePath());
                view.setBubbleParams(params);
                mIsEditWordAgain = false;
            }
            addSubtitlesIntoVideo();
        }

    }

    @Override //字幕输入取消
    public void onInputCancel() {
        mWordInputDialog = null;
    }

    // 添加一个字幕控件到Group中，并显示出来
    public TCWordBubbleView createDefaultBubbleView(TCBubbleViewParams params) {
        final TCWordBubbleView view = TCWordBubbleViewFactory.newOperationView(this); // 创建一个气泡字幕的控件实例

        // 根据params初始化对应的控件
        view.setBubbleParams(params);

        // 设置view显示出来的位置
        view.setCenterX(mTCBubbleViewGroup.getWidth() / 2);// 控件显示在父容器的中心
        view.setCenterY(mTCBubbleViewGroup.getHeight() / 2);// 控件显示在父容器的中心

        // 初始化字幕的默认的显示时间区间
        view.setStartTime(mDefaultWordStartTime, mDefaultWordEndTime);

        view.setIOperationViewClickListener(this);// 监听回调

        return view;
    }

    /**
     * ====================================气泡样式修改面板回调===========================
     */
    @Override
    public void onWordInfoCallback(TCWordParamsInfo info) {
        //获取当前处于编辑状态的气泡字幕的view
        TCWordBubbleView view = (TCWordBubbleView) mTCBubbleViewGroup.getSelectedLayerOperationView();
        if (view != null) {
            TCBubbleViewParams params = view.getBubbleParams();
            params.wordParamsInfo = info;
            params.bubbleBitmap = TCBubbleManager.getInstance(this).getBitmapFromAssets(params.wordParamsInfo.getBubbleInfo().getBubblePath());
            view.setBubbleParams(params);
            addSubtitlesIntoVideo();
        }
    }

    @Override
    public void onAdd() {
        addSubtitlesIntoVideo();
    }

    /**
     * ===========================气泡字幕控件的回调=================================
     */
    @Override // 删除回调
    public void onDeleteClick() {
        int index = mTCBubbleViewGroup.getSelectedViewIndex();
        TCWordBubbleView view = (TCWordBubbleView) mTCBubbleViewGroup.getSelectedLayerOperationView();
        if (view != null) {
            mTCBubbleViewGroup.removeOperationView(view);
        }
        mVideoProgressController.removeRangeSliderView(index);
        addSubtitlesIntoVideo();
    }

    @Override// 编辑回调
    public void onEditClick() {
        TCWordBubbleView view = (TCWordBubbleView) mTCBubbleViewGroup.getSelectedLayerOperationView();
        if (view != null)
            mBubblePopWin.show(view.getBubbleParams().wordParamsInfo);

        addSubtitlesIntoVideo();
    }

    @Override// 旋转回调
    public void onRotateClick() {
        addSubtitlesIntoVideo();
    }

    /**
     * ===========================Group控件的回调=================================
     */
    @Override
    public void onLayerOperationViewItemClick(TCLayerOperationView view, int lastSelectedPos, int currentSelectedPos) {
        Log.i(TAG, "onLayerOperationViewItemClick: lastSelectedPos = " + lastSelectedPos + " current pos = " + currentSelectedPos);
        pausePlay(true);
        // 说明选中同一个控件两次，那么则展开字幕编辑界面
        if (lastSelectedPos == currentSelectedPos) {
            mIsEditWordAgain = true;
            showInputDialog(((TCWordBubbleView) view).getBubbleParams().text); // 再次点击已选中的 字幕控件，则弹出文字输入框
        } else {
            mIsEditWordAgain = false;
            RangeSliderViewContainer lastSlider = mVideoProgressController.getRangeSliderView(lastSelectedPos);
            if (lastSlider != null) {
                lastSlider.setEditComplete();
            }

            RangeSliderViewContainer currentSlider = mVideoProgressController.getRangeSliderView(currentSelectedPos);
            if (currentSlider != null) {
                currentSlider.showEdit();
            }
        }
    }

    private RangeSliderViewContainer.OnDurationChangeListener mOnDurationChangeListener = new RangeSliderViewContainer.OnDurationChangeListener() {
        @Override
        public void onDurationChange(long startTimeMs, long endTimeMs) {
            if (mTCBubbleViewGroup != null) {
                TCLayerOperationView operationView = mTCBubbleViewGroup.getSelectedLayerOperationView();
                if (operationView != null) {
                    operationView.setStartTime(startTimeMs, endTimeMs);
                }
            }
            // 时间范围修改也马上设置到sdk中去
            addSubtitlesIntoVideo();
        }
    };

    /**
     * ===========================预览进度的回调=================================
     * <p>
     */
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

    /**
     * ===========================将字幕添加到SDK中去=================================
     */
    private void addSubtitlesIntoVideo() {
        List<TXVideoEditConstants.TXSubtitle> subtitleList = new ArrayList<>();
        for (int i = 0; i < mTCBubbleViewGroup.getChildCount(); i++) {
            TCWordBubbleView view = (TCWordBubbleView) mTCBubbleViewGroup.getOperationView(i);
            TXVideoEditConstants.TXSubtitle subTitle = new TXVideoEditConstants.TXSubtitle();
            subTitle.titleImage = view.getRotateBitmap();

            TXVideoEditConstants.TXRect rect = new TXVideoEditConstants.TXRect();
            rect.x = view.getImageX();
            rect.y = view.getImageY();

            rect.width = view.getImageWidth();
            subTitle.frame = rect;
            subTitle.startTime = view.getStartTime();
            subTitle.endTime = view.getEndTime();
            subtitleList.add(subTitle);
        }
        mTXVideoEditer.setSubtitleList(subtitleList);
    }


    /**
     * ===========================将字幕控件参数保存到Manager中去=================================
     * <p>
     * 将字幕控件的相关参数保存到Manager中去，方便出去之后可以重新进来再次编辑字幕
     */
    private void saveIntoManager() {
        TCBubbleViewInfoManager manager = TCBubbleViewInfoManager.getInstance();
        manager.clear();
        for (int i = 0; i < mTCBubbleViewGroup.getChildCount(); i++) {
            TCWordBubbleView view = (TCWordBubbleView) mTCBubbleViewGroup.getOperationView(i);

            Log.i(TAG, "saveIntoManager: x = " + view.getCenterX() + " y = " + view.getCenterY());

            TCBubbleViewInfo info = new TCBubbleViewInfo();
            info.setViewCenterX(view.getCenterX());
            info.setViewCenterY(view.getCenterY());
            info.setRotation(view.getImageRotate());
            info.setViewParams(view.getBubbleParams());
            info.setStartTime(view.getStartTime());
            info.setEndTime(view.getEndTime());
            info.setScale(view.getImageScale());

            view.setBubbleParams(null);
            manager.add(info);
        }
    }

    /**
     * 将字幕控件的相关参数从Manager中重新恢复出来，恢复字幕编辑的场景。 以便继续编辑
     */
    private void recoverFromManager() {
        TCBubbleViewInfoManager manager = TCBubbleViewInfoManager.getInstance();
        for (int i = 0; i < manager.size(); i++) {
            TCBubbleViewInfo info = manager.get(i);

            TCBubbleViewParams params = info.getViewParams();
            // params设置进Bubble之后是不保存bitmap的,会被置空释放掉
            // 重新loadBitmap (因为在设置BubbleView的时候，原来的气泡Bitmap回在内部被回收。 所以这里直接重新load多一边
            params.bubbleBitmap = TCBubbleManager.getInstance(this).getBitmapFromAssets(params.wordParamsInfo.getBubbleInfo().getBubblePath());

            TCWordBubbleView view = createDefaultBubbleView(info.getViewParams());
            view.setCenterX(info.getViewCenterX());
            view.setCenterY(info.getViewCenterY());
            Log.i(TAG, "recoverFromManager: x = " + info.getViewCenterX() + " y = " + info.getViewCenterY());
            view.setImageRotate(info.getRotation());
            view.setImageScale(info.getScale());

            // 恢复时间的时候，需要检查一下是否符合这一次区间的startTime和endTime
            long viewStartTime = info.getStartTime();
            long viewEndTime = info.getEndTime();
            view.setStartTime(viewStartTime, viewEndTime);
            mTCBubbleViewGroup.addOperationView(view);// 添加到Group中去管理


            RangeSliderViewContainer rangeSliderView = new RangeSliderViewContainer(this);
            rangeSliderView.init(mVideoProgressController, viewStartTime, viewEndTime - viewStartTime, TCVideoEditerWrapper.getInstance().getTXVideoInfo().duration);
            rangeSliderView.setDurationChangeListener(mOnDurationChangeListener);
            rangeSliderView.setEditComplete();
            mVideoProgressController.addRangeSliderView(rangeSliderView);
        }
    }


    @Override
    public void onBackPressed() {
        clickBack();
    }

}
