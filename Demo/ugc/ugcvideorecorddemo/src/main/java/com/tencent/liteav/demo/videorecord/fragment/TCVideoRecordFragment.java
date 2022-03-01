package com.tencent.liteav.demo.videorecord.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.videorecord.R;
import com.tencent.liteav.demo.videorecord.TCRecordPreviewActivity;
import com.tencent.liteav.demo.videorecord.inter.FragmentLifeHold;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.UGCKitVideoRecord;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.effect.bgm.TCMusicActivity;
import com.tencent.qcloud.ugckit.module.record.MusicInfo;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.interfaces.IVideoRecordKit;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.ugc.TXRecordCommon;

import java.util.List;

/**
 * 小视频录制界面，让camera和surfaceView的生命周期跟fragment走，便于控制camera和surfaceView的生命周期。
 * 1、这样activity的onStop和onDestroy就不会因为耗时阻塞，导致界面结束之后1秒左右才执行，避免快速频繁启动界面产生的黑屏问题
 */
public class TCVideoRecordFragment extends Fragment {

    private UGCKitVideoRecord mUGCKitVideoRecord;
    private int               mMinDuration;
    private int               mMaxDuration;
    private int               mAspectRatio;
    private int               mRecommendQuality;
    private int               mVideoBitrate;
    private int               mResolution;
    private int               mFps;
    private int               mGop;
    private int               mOrientation;
    private boolean           mTouchFocus;
    private boolean           mNeedEdit;
    private FragmentLifeHold  mFragmentLifeHold;

    public static TCVideoRecordFragment newInstance(Intent intent, FragmentLifeHold mFragmentLifeHold) {
        TCVideoRecordFragment tcVideoRecordFragment = new TCVideoRecordFragment();
        tcVideoRecordFragment.setFragmentLifeHold(mFragmentLifeHold);
        if (intent != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_MIN_DURATION, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_MIN_DURATION, 5 * 1000));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_MAX_DURATION, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_MAX_DURATION, 60 * 1000));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, TXRecordCommon.VIDEO_ASPECT_RATIO_9_16));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY, -1));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_RESOLUTION, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_540_960));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_BITE_RATE, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_BITE_RATE, 6500));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_FPS, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_FPS, 30));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_GOP, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_GOP, 1));
            bundle.putInt(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, intent.getIntExtra(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN));
            bundle.putBoolean(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS, intent.getBooleanExtra(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS, true));
            bundle.putBoolean(UGCKitConstants.RECORD_CONFIG_NEED_EDITER, intent.getBooleanExtra(UGCKitConstants.RECORD_CONFIG_NEED_EDITER, true));

            tcVideoRecordFragment.setArguments(bundle);
        }
        return tcVideoRecordFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initData();
        View rootView = inflater.inflate(R.layout.ugcrecord_fragment_record, container, false);
        mUGCKitVideoRecord = (UGCKitVideoRecord) rootView.findViewById(R.id.video_record_layout);

        UGCKitRecordConfig ugcKitRecordConfig = UGCKitRecordConfig.getInstance();
        ugcKitRecordConfig.mMinDuration = mMinDuration;
        ugcKitRecordConfig.mMaxDuration = mMaxDuration;
        ugcKitRecordConfig.mAspectRatio = mAspectRatio;
        ugcKitRecordConfig.mQuality = mRecommendQuality;
        ugcKitRecordConfig.mVideoBitrate = mVideoBitrate;
        ugcKitRecordConfig.mResolution = mResolution;
        ugcKitRecordConfig.mFPS = mFps;
        ugcKitRecordConfig.mGOP = mGop;
        ugcKitRecordConfig.mHomeOrientation = mOrientation;
        ugcKitRecordConfig.mTouchFocus = mTouchFocus;
        ugcKitRecordConfig.mIsNeedEdit = mNeedEdit;

        mUGCKitVideoRecord.setConfig(ugcKitRecordConfig);
        mUGCKitVideoRecord.disableTakePhoto();
        mUGCKitVideoRecord.disableLongPressRecord();
        mUGCKitVideoRecord.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUGCKitVideoRecord.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
            @Override
            public void onRecordCanceled() {
                finish();
            }

            @Override
            public void onRecordCompleted(UGCKitResult result) {
                // 下一步进行编辑：进行视频预处理，则不需要传出路径，下一步进行预览，需要路径
                if (mNeedEdit) {
                    startEditActivity(result);
                } else {
                    startPreviewActivity(result);
                }
            }
        });
        mUGCKitVideoRecord.setOnMusicChooseListener(new IVideoRecordKit.OnMusicChooseListener() {
            @Override
            public void onChooseMusic(int position) {
                Intent bgmIntent = new Intent(getActivity(), TCMusicActivity.class);
                bgmIntent.putExtra(UGCKitConstants.MUSIC_POSITION, position);
                startActivityForResult(bgmIntent, UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE);
            }
        });
        return rootView;
    }


    private void initData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMinDuration = arguments.getInt(UGCKitConstants.RECORD_CONFIG_MIN_DURATION, 5 * 1000);
            mMaxDuration = arguments.getInt(UGCKitConstants.RECORD_CONFIG_MAX_DURATION, 60 * 1000);
            mAspectRatio = arguments.getInt(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
            mRecommendQuality = arguments.getInt(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY, -1);
            mResolution = arguments.getInt(UGCKitConstants.RECORD_CONFIG_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_540_960);
            mVideoBitrate = arguments.getInt(UGCKitConstants.RECORD_CONFIG_BITE_RATE, 6500);
            mFps = arguments.getInt(UGCKitConstants.RECORD_CONFIG_FPS, 30);
            mGop = arguments.getInt(UGCKitConstants.RECORD_CONFIG_GOP, 1);
            mOrientation = arguments.getInt(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
            mTouchFocus = arguments.getBoolean(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS, true);
            mNeedEdit = arguments.getBoolean(UGCKitConstants.RECORD_CONFIG_NEED_EDITER, true);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                mUGCKitVideoRecord.start();
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtils.showShort(R.string.ugcrecord_app_camera_storage_mic);
                finish();
            }
        }).request();
    }

    private void startEditActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent();
        intent.setAction("com.tencent.liteav.demo.videoediter");
        if (mRecommendQuality == TXRecordCommon.VIDEO_QUALITY_LOW) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_360_640);
        } else if (mRecommendQuality == TXRecordCommon.VIDEO_QUALITY_MEDIUM) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_540_960);
        } else if (mRecommendQuality == TXRecordCommon.VIDEO_QUALITY_HIGH) {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, TXRecordCommon.VIDEO_RESOLUTION_720_1280);
        } else {
            intent.putExtra(UGCKitConstants.VIDEO_RECORD_RESOLUTION, mResolution);
        }
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        IntentUtils.safeStartActivity(TCVideoRecordFragment.this.getActivity(), intent);
        finish();
    }

    private void startPreviewActivity(UGCKitResult ugcKitResult) {
        Intent intent = new Intent(getActivity(), TCRecordPreviewActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, ugcKitResult.outputPath);
        intent.putExtra(UGCKitConstants.VIDEO_COVERPATH, ugcKitResult.coverPath);
        startActivity(intent);
        finish();
    }


    @Override
    public void onStop() {
        super.onStop();
        mUGCKitVideoRecord.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUGCKitVideoRecord.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUGCKitVideoRecord.screenOrientationChange();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != UGCKitConstants.ACTIVITY_MUSIC_REQUEST_CODE) {
            return;
        }
        if (data == null) {
            return;
        }
        MusicInfo musicInfo = new MusicInfo();

        musicInfo.path = data.getStringExtra(UGCKitConstants.MUSIC_PATH);
        musicInfo.name = data.getStringExtra(UGCKitConstants.MUSIC_NAME);
        musicInfo.position = data.getIntExtra(UGCKitConstants.MUSIC_POSITION, -1);

        mUGCKitVideoRecord.setRecordMusicInfo(musicInfo);
    }

    public void onBackPressed() {
        mUGCKitVideoRecord.backPressed();
    }

    public void setFragmentLifeHold(FragmentLifeHold mFragmentLifeHold) {
        this.mFragmentLifeHold = mFragmentLifeHold;
    }

    public void finish() {
        if (null != mFragmentLifeHold) {
            mFragmentLifeHold.finishFragment();
        }
    }
}
