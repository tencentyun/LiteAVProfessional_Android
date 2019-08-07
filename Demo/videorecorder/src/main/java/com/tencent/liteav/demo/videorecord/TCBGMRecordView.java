package com.tencent.liteav.demo.videorecord;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.videorecord.utils.TCUtils;
import com.tencent.liteav.demo.videorecord.view.RangeSlider;
import com.tencent.liteav.demo.videorecord.utils.TCBGMInfo;
import com.tencent.liteav.demo.videorecord.view.TCReversalSeekBar;
import com.tencent.ugc.TXUGCRecord;

/**
 * Created by hanszhli on 2017/6/15. Modified by vinsonswang to select bgm and play before record
 * <p>
 * 录制界面的音频操作面板
 */
public class TCBGMRecordView extends RelativeLayout implements RangeSlider.OnRangeChangeListener {

    private String TAG = TCBGMRecordView.class.getSimpleName();

    private TCReversalSeekBar mTCReversalSeekBar;
    private TextView mTvTip, mTvDelete, mTvMusicName;
    private LinearLayout mLlMainPanel;
    private RelativeLayout mRlMusicInfo;
    private TCBGMRecordChooseLayout mRlChoseMusic;
    private RangeSlider mRangeSlider;
    private long mDuration;
    private long mStartPos;
    private long mEndPos;

    private View mBackView;

    private IBGMRecordSelectListener mIBgmRecordSelectListener;

    private TXUGCRecord mRecord;
    private TCBGMInfo mTCBGMInfo;

    public interface IBGMRecordSelectListener {
        void onBGMDelete();

        boolean onBGMInfoSetting(TCBGMInfo info);

        void onBGMRangeKeyUp(long startTime, long endTime); //ms
    }

    public TCBGMRecordView(Context context) {
        super(context);

        init();
    }

    public TCBGMRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TCBGMRecordView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public void setIBGMRecordSelectListener(IBGMRecordSelectListener iBgmRecordSelectListener) {
        mIBgmRecordSelectListener = iBgmRecordSelectListener;
    }

    private void init() {
        View.inflate(getContext(), R.layout.item_bgm_record_view, this);
        initEditMusicView();
    }

    public void setRecord(TXUGCRecord record) {
        mRecord = record;
    }

    public float getProgress() {
        return mTCReversalSeekBar.getProgress();
    }


    private void initEditMusicView() {
        mBackView = findViewById(R.id.back_ll);
        mBackView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.GONE);
            }
        });
        mTvTip = (TextView) findViewById(R.id.bgm_tv_tip);
        mTvMusicName = (TextView) findViewById(R.id.bgm_tv_music_name);
        mTvDelete = (TextView) findViewById(R.id.bgm_tv_delete);
        mTvDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlMainPanel.setVisibility(GONE);
                mRlChoseMusic.setVisibility(VISIBLE);
                if (mIBgmRecordSelectListener != null) {
                    mIBgmRecordSelectListener.onBGMDelete();
                }
            }
        });
        mRlMusicInfo = (RelativeLayout) findViewById(R.id.bgm_rl_bgm_info);
        mRlMusicInfo.setVisibility(GONE);

        mRangeSlider = (RangeSlider) findViewById(R.id.bgm_range_slider);
        mRangeSlider.setRangeChangeListener(this);

        mLlMainPanel = (LinearLayout) findViewById(R.id.bgm_ll_main_panel);
        mRlChoseMusic = (TCBGMRecordChooseLayout) findViewById(R.id.bgm_bgm_chose);
        mRlChoseMusic.setOnItemClickListener(new TCBGMRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                boolean success = setBGMInfo(mRlChoseMusic.getMusicList().get(position));
                if (success) {
                    mRlChoseMusic.setVisibility(GONE);
                    mLlMainPanel.setVisibility(VISIBLE);
                }
            }
        });
        mTCReversalSeekBar = (TCReversalSeekBar) findViewById(R.id.bgm_sb_voice);
        mTCReversalSeekBar.setOnSeekProgressListener(new TCReversalSeekBar.OnSeekProgressListener() {
            @Override
            public void onSeekDown() {

            }

            @Override
            public void onSeekUp() {

            }

            @Override
            public void onSeekProgress(float progress) {
            }
        });
    }


    public long getSegmentFrom() {
        return mStartPos;
    }

    public long getSegmentTo() {
        return mEndPos;
    }

    public boolean setBGMInfo(TCBGMInfo bgmInfo) {
        if (bgmInfo == null) {
            return false;
        }
        mTCBGMInfo = bgmInfo;

        if (mIBgmRecordSelectListener != null) {
            mIBgmRecordSelectListener.onBGMInfoSetting(mTCBGMInfo);
        }

        mRlMusicInfo.setVisibility(VISIBLE);
        mDuration = bgmInfo.getDuration();
        mStartPos = 0;
        mEndPos = (int) mDuration;
        mTvMusicName.setText(bgmInfo.getSongName() + " — " + bgmInfo.getSingerName() + "   " + bgmInfo.getFormatDuration());

        resetViews();
        return true;
    }


    /**
     * 重置游标以及Tips
     */
    private void resetViews() {
        mRangeSlider.resetRangePos();
        mTvTip.setText("截取所需音频片段");
    }


    @Override
    public void onKeyDown(int type) {
    }


    @Override
    public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
        long leftTime = mDuration * leftPinIndex / 100; //ms
        long rightTime = mDuration * rightPinIndex / 100;

        if (type == RangeSlider.TYPE_LEFT) {
            mStartPos = leftTime;
        } else {
            mEndPos = rightTime;
        }
        if (mIBgmRecordSelectListener != null) {
            mIBgmRecordSelectListener.onBGMRangeKeyUp(mStartPos, mEndPos);
        }
        mTvTip.setText(String.format("左侧 : %s, 右侧 : %s ", TCUtils.duration(leftTime), TCUtils.duration(rightTime)));
    }

}
