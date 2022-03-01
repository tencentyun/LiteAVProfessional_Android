package com.tencent.liteav.v1liveplayerdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.common.custom.CustomRenderVideoFrame;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.v1liveplayerdemo.model.SettingInfo;
import com.tencent.liteav.v1liveplayerdemo.model.SettingItem;
import com.tencent.liteav.v1liveplayerdemo.view.LogInfoWindow;
import com.tencent.liteav.v1liveplayerdemo.view.PlayerSettingFragment;
import com.tencent.liteav.v1liveplayerdemo.view.RadioButton;
import com.tencent.liteav.v1liveplayerdemo.view.RadioSelectView;
import com.tencent.liteav.v1liveplayerdemo.view.SettingItemView;
import com.tencent.liteav.v1liveplayerdemo.view.SettingItemView.OnEventListener;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * 腾讯云 {@link TXLivePlayer} 直播播放器使用参考 Demo
 * 有以下功能参考 ：
 * - 基本功能参考： 启动推流 {@link #startPlay()}与 结束推流 {@link #stopPlay()}
 * - 硬件加速： 使用硬解码
 * - 性能数据查看参考： {@link #onNetStatus(Bundle)}
 * - 处理 SDK 回调事件参考： {@link #onPlayEvent(int, Bundle)}
 * - 渲染角度、渲染模式切换： 横竖屏渲染、铺满与自适应渲染
 * - 缓存策略选择：{@link #setCacheStrategy} 缓存策略：自动、极速、流畅。 极速模式：时延会尽可能低、但抗网络抖动效果不佳；流畅模式：时延较高、抗抖动能力较强
 */
public class V1LivePlayerMainActivity extends AppCompatActivity implements ITXLivePlayListener,
        TXLivePlayer.ITXSnapshotListener, TXLivePlayer.ITXAudioVolumeEvaluationListener,
        TXLivePlayer.ITXLivePlayVideoRenderListener, TXLivePlayer.ITXVideoRawDataListener {
    private static final String TAG                     = "V1LivePlayerActivity";
    private static final int    PERMISSION_REQUEST_CODE = 100;      //申请权限的请求码
    private static final int    TOAST_INTERVAL          = 500;      //Toast显示时间间隔，单位：ms。
    private static final String NORMAL_PLAY_URL         =
            "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv";
    private static final String H265_PLAY_URL           =
            "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid_demoH265.flv";

    private Context          mContext;
    private ImageView        mImageLoading;          //显示视频缓冲动画
    private RelativeLayout   mLayoutRoot;            //视频暂停时更新背景
    private ImageView        mImageRoot;             //背景icon
    private ImageButton      mButtonPlay;            //视频的播放控制按钮
    private ImageButton      mButtonRenderRotation;  //调整视频播放方向：横屏、竖屏
    private ImageButton      mButtonRenderMode;      //调整视频渲染模式：全屏、自适应
    private ImageButton      mButtonCacheStrategy;   //设置视频的缓存策略
    private ImageView        mImageCacheStrategyShadow;
    private ImageButton      mImageLogInfo;
    private RadioSelectView  mLayoutCacheStrategy;   //显示所有缓存模式的View
    private RadioSelectView  mLayoutHWDecode;        //显示所有缓存模式的View
    private LogInfoWindow    mLogInfoWindow;
    private TXLivePlayer     mLivePlayer;            //直播拉流的视频播放器
    private TXLivePlayConfig mPlayerConfig;          //TXLivePlayer 播放配置项
    private TXCloudVideoView mVideoView;
    private SurfaceView      mSurfaceView;
    private TextureView      mTextureView;
    private ProgressBar      mVolumeProgressBar;     //音量提示

    private PlayerSettingFragment mPlayerSettingFragment;
    private OkHttpClient          mOkHttpClient = null;   //获取超低时延视频源直播地址

    private int     mLogClickCount      = 0;
    private String  mPlayURL            = "";
    private boolean mIsPlaying          = false;
    private boolean mFetching           = false;          //是否正在获取视频源，测试专用
    private boolean mHWDecode           = false;          //是否启用了硬解码
    private int     mCacheStrategy      = Constants.CACHE_STRATEGY_AUTO;                    //Player缓存策略
    private int     mActivityPlayType   = Constants.ACTIVITY_TYPE_LIVE_PLAY;                //播放类型
    private int     mCurrentPlayURLType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;                 //Player 当前播放链接类型
    private int     mRenderMode         = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;    //Player 当前渲染模式
    private int     mRenderRotation     = TXLiveConstants.RENDER_ROTATION_PORTRAIT;         //Player 当前渲染角度
    private long    mStartPlayTS        = 0;         //保存开始播放的时间戳，测试专用
    private long    mTimeLastToast      = 0;         //上次弹出Toast的时间。
    private boolean mIsH265Playing      = false;

    private SettingInfo            mSettingInfo;
    private CustomRenderVideoFrame mCustomRenderVideoFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.v1liveplayer_main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initialize();
    }

    private void initialize() {
        mLayoutRoot = (RelativeLayout) findViewById(R.id.v1liveplayer_rl_root);
        mImageRoot = (ImageView) findViewById(R.id.v1liveplayer_iv_root);
        initPlayView();
        initLogInfo();
        initPlayButton();
        initHWDecodeButton();
        initRenderRotationButton();
        initRenderModeButton();
        initCacheStrategyButton();
        initSwitchStreamButton();
        initSettingButton();
        initNavigationBack();
        requestPermissions();
        initRTMPURL();

        // 初始化完成之后自动播放
        startPlay();
    }

    private void initSwitchStreamButton() {
        findViewById(R.id.v1liveplayer_btn_switch_stream).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsPlaying) {
                    Toast.makeText(v.getContext(), getString(R.string.v1liveplayer_please_start_play),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String playUrl = NORMAL_PLAY_URL;
                String playUrl265 = H265_PLAY_URL;
                int index = mPlayURL.lastIndexOf(".flv");
                if (index != -1 && !mPlayURL.equals(playUrl)) {
                    playUrl = mPlayURL;
                    playUrl265 = mPlayURL.substring(0, index) + "_h265.flv";
                }
                if (mIsH265Playing) {
                    Toast.makeText(v.getContext(), getString(R.string.v1liveplayer_switch_h264_toast),
                            Toast.LENGTH_SHORT).show();
                    mIsH265Playing = false;
                    mLivePlayer.switchStream(playUrl);
                    v.setBackgroundResource(R.drawable.v1liveplayer_h265);
                } else {
                    Toast.makeText(v.getContext(), getString(R.string.v1liveplayer_switch_h265_toast),
                            Toast.LENGTH_SHORT).show();
                    mIsH265Playing = true;
                    mLivePlayer.switchStream(playUrl265);
                    v.setBackgroundResource(R.drawable.v1liveplayer_h264);
                }
            }
        });
    }

    private void initRTMPURL() {
        int activityType = getIntent().getIntExtra(Constants.INTENT_ACTIVITY_TYPE, Constants.ACTIVITY_TYPE_LIVE_PLAY);
        String playURL = getIntent().getStringExtra(Constants.INTENT_URL);
        mSettingInfo = (SettingInfo) getIntent().getSerializableExtra(Constants.INTENT_SETTING_RESULT);
        if (activityType == Constants.ACTIVITY_TYPE_REALTIME_PLAY) {
            if (TextUtils.isEmpty(playURL)) {
                setPlayURL(Constants.ACTIVITY_TYPE_LIVE_PLAY, Constants.NORMAL_PLAY_URL);
                mButtonCacheStrategy.setClickable(true);
                mImageCacheStrategyShadow.setVisibility(View.GONE);
            } else {
                setPlayURL(Constants.ACTIVITY_TYPE_REALTIME_PLAY, playURL);
                mButtonCacheStrategy.setClickable(false);
                mImageCacheStrategyShadow.setVisibility(View.VISIBLE);
            }
        } else {
            if (TextUtils.isEmpty(playURL)) {
                setPlayURL(activityType, Constants.NORMAL_PLAY_URL);
            } else {
                setPlayURL(activityType, playURL);
            }
            mButtonCacheStrategy.setClickable(true);
            mImageCacheStrategyShadow.setVisibility(View.GONE);
        }
    }

    private void initPlayView() {
        mVideoView = (TXCloudVideoView) findViewById(R.id.v1liveplayer_video_view);
        mVideoView.setLogMargin(12, 12, 110, 60);
        mVideoView.showLog(false);
        mSurfaceView = findViewById(R.id.v1liveplayer_surface);
        mTextureView = findViewById(R.id.v1liveplayer_texture);
        mPlayerConfig = new TXLivePlayConfig();
        mLivePlayer = new TXLivePlayer(mContext);
        mLivePlayer.setVolume(50);
        mImageLoading = (ImageView) findViewById(R.id.v1liveplayer_iv_loading);
        mVolumeProgressBar = findViewById(R.id.v1liveplayer_main_pb_volume);
        if (AppRuntime.get().isDebug()) {
            mVolumeProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void initPlayButton() {
        mButtonPlay = (ImageButton) findViewById(R.id.v1liveplayer_btn_play);
        mButtonPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlay();
            }
        });
    }

    private void initHWDecodeButton() {
        mLayoutHWDecode = (RadioSelectView) findViewById(R.id.v1liveplayer_rsv_decode);
        findViewById(R.id.v1liveplayer__btn_decode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutHWDecode.setVisibility(View.VISIBLE);
            }
        });
        mLayoutHWDecode.setTitle(R.string.v1liveplayer_hw_decode);
        String[] stringArray = getResources().getStringArray(R.array.v1liveplayer_hw_decode);
        mLayoutHWDecode.setData(stringArray, 1);
        mLayoutHWDecode.setRadioSelectListener(new RadioSelectView.RadioSelectListener() {
            @Override
            public void onClose() {
                mLayoutHWDecode.setVisibility(View.GONE);
            }

            @Override
            public void onChecked(int prePosition, RadioButton preRadioButton, int curPosition,
                                  RadioButton curRadioButton) {
                mLayoutHWDecode.setVisibility(View.GONE);
                if (curPosition == 0) {
                    Toast.makeText(getApplicationContext(), R.string.v1liveplayer_toast_start_hw_decode,
                            Toast.LENGTH_SHORT).show();
                } else if (curPosition == 1) {
                    Toast.makeText(getApplicationContext(), R.string.v1liveplayer_toast_close_hw_decode,
                            Toast.LENGTH_SHORT).show();
                }
                setHWDecode(curPosition);
            }
        });
    }

    private void initRenderRotationButton() {
        mButtonRenderRotation = (ImageButton) findViewById(R.id.v1liveplayer_btn_render_rotate_landscape);
        mButtonRenderRotation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int renderRotation = getRenderRotation();
                if (renderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                    mButtonRenderRotation.setBackgroundResource(R.drawable.v1liveplayer_render_rotate_portrait);
                    renderRotation = TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
                } else if (renderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                    mButtonRenderRotation.setBackgroundResource(R.drawable.v1liveplayer_render_rotate_landscape);
                    renderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
                }
                setRenderRotation(renderRotation);
            }
        });
    }

    private void initRenderModeButton() {
        mButtonRenderMode = (ImageButton) findViewById(R.id.v1liveplayer_btn_render_mode_fill);
        mButtonRenderMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int renderMode = getRenderMode();
                if (getRenderMode() == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                    mButtonRenderMode.setBackgroundResource(R.drawable.v1liveplayer_render_mode_fill);
                    renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                } else if (getRenderMode() == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                    mButtonRenderMode.setBackgroundResource(R.drawable.v1liveplayer_adjust_mode_btn);
                    renderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
                }
                setRenderMode(renderMode);
            }
        });
    }

    private void initCacheStrategyButton() {
        mLayoutCacheStrategy = (RadioSelectView) findViewById(R.id.v1liveplayer_rsv_cache_strategy);
        mImageCacheStrategyShadow = (ImageView) findViewById(R.id.v1liveplayer_btn_cache_strategy_shadow);
        mButtonCacheStrategy = (ImageButton) findViewById(R.id.v1liveplayer_btn_cache_strategy);
        mButtonCacheStrategy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutCacheStrategy.setVisibility(mLayoutCacheStrategy.getVisibility() == View.VISIBLE ? View.GONE :
                        View.VISIBLE);
            }
        });
        mLayoutCacheStrategy.setTitle(R.string.v1liveplayer_cache_strategy);
        String[] stringArray = getResources().getStringArray(R.array.v1liveplayer_cache_strategy);
        mLayoutCacheStrategy.setData(stringArray, Constants.CACHE_STRATEGY_AUTO);
        mLayoutCacheStrategy.setRadioSelectListener(new RadioSelectView.RadioSelectListener() {
            @Override
            public void onClose() {
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }

            @Override
            public void onChecked(int prePosition, RadioButton preRadioButton, int curPosition,
                                  RadioButton curRadioButton) {
                if (curPosition == Constants.CACHE_STRATEGY_FAST) {
                    mLogInfoWindow.setCacheTime(Constants.CACHE_TIME_FAST);
                } else {
                    mLogInfoWindow.setCacheTime(Constants.CACHE_TIME_SMOOTH);
                }
                setCacheStrategy(curPosition);
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });
        setCacheStrategy(Constants.CACHE_STRATEGY_AUTO);
        mLogInfoWindow.setCacheTime(Constants.CACHE_TIME_SMOOTH);
    }

    private void initSettingButton() {
        View btnSetting = findViewById(R.id.v1liveplayer_btn_setting);
        if (AppRuntime.get().isDebug()) {
            btnSetting.setVisibility(View.VISIBLE);
            findViewById(R.id.v1liveplayer_btn_setting_right).setVisibility(View.VISIBLE);
        }
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerSettingFragment.toggle(getSupportFragmentManager(), "PlayerSettingFragment");
            }
        });

        mPlayerSettingFragment = new PlayerSettingFragment(this);

        List<SettingItem> data = new ArrayList<>();
        data.add(new SettingItem(getString(R.string.v1liveplayer_pause), false,
                new SettingItemView.OnEventListener() {

                    @Override
                    public void onChecked(SettingItemView itemView, boolean isChecked) {
                        if (mLivePlayer != null && mIsPlaying) {
                            if (isChecked) {
                                mLivePlayer.pause();
                            } else {
                                mLivePlayer.resume();
                            }
                        }
                    }
                }));
        data.add(new SettingItem(getString(R.string.v1liveplayer_volume), 100, 50, new OnEventListener() {
            @Override
            public void onProgress(SettingItemView itemView, int progress) {
                if (mLivePlayer != null) {
                    mLivePlayer.setVolume(progress);
                }
            }
        }));
        data.add(new SettingItem(getString(R.string.v1liveplayer_volume_tips), 500, 0, new OnEventListener() {
            @Override
            public void onProgress(SettingItemView itemView, int progress) {
                if (mLivePlayer != null) {
                    mLivePlayer.enableAudioVolumeEvaluation(progress);
                }
            }
        }));
        data.add(new SettingItem(getString(R.string.v1liveplayer_snapshot), getString(R.string.v1liveplayer_btn_ok),
                new OnEventListener() {
                    @Override
                    public void onButtonClick(SettingItemView itemView) {
                        if (mLivePlayer != null) {
                            mLivePlayer.snapshot(V1LivePlayerMainActivity.this);
                        }
                    }
                }));
        data.add(new SettingItem(getString(R.string.v1liveplayer_open_mute), false,
                new SettingItemView.OnEventListener() {

                    @Override
                    public void onChecked(SettingItemView itemView, boolean isChecked) {
                        if (mLivePlayer != null && mIsPlaying) {
                            mLivePlayer.setMute(isChecked);
                        }
                    }
                }));
        mPlayerSettingFragment.setData(data);
    }

    private void initNavigationBack() {
        findViewById(R.id.v1liveplayer_ibtn_left).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                destroy();
                finish();
            }
        });
    }

    private void initLogInfo() {
        mImageLogInfo = (ImageButton) findViewById(R.id.v1liveplayer_ibtn_right);
        mImageLogInfo.setImageResource(R.drawable.v1liveplayer_log_info_btn_show);
        mImageLogInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLogInfoWindow.isShowing()) {
                    mLogInfoWindow.dismiss();
                }
                int count = mLogClickCount % 3;
                if (count == 0) {
                    mLogInfoWindow.show(v);
                    showVideoLog(false);
                } else if (count == 1) {
                    showVideoLog(true);
                } else if (count == 2) {
                    showVideoLog(false);
                }
                mLogClickCount++;
            }
        });
        mLogInfoWindow = new LogInfoWindow(mContext);
    }

    @Override
    public void onPlayEvent(int event, Bundle param) {
        Log.d(TAG, "receive event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION));
        mLogInfoWindow.setLogText(null, param, event);
        switch (event) {
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN:
                Log.d("AutoMonitor", "PlayFirstRender,cost=" + (System.currentTimeMillis() - mStartPlayTS));
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME:
                stopLoadingAnimation();
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                startLoadingAnimation();
                break;
            case TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION:
                Log.d(TAG, "size " + param.getInt(TXLiveConstants.EVT_PARAM1) + "x"
                        + param.getInt(TXLiveConstants.EVT_PARAM2));
                break;
            case TXLiveConstants.PLAY_EVT_GET_MESSAGE:
                byte[] data = param.getByteArray(TXLiveConstants.EVT_GET_MSG);
                String seiMessage = "";
                if (data != null && data.length > 0) {
                    try {
                        seiMessage = new String(data, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getApplicationContext(), seiMessage, Toast.LENGTH_SHORT).show();
                break;
            case TXLiveConstants.PLAY_EVT_CHANGE_ROTATION:
                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT:
            case TXLiveConstants.PLAY_EVT_PLAY_END:
                stopPlay();
                break;
            case TXLiveConstants.PLAY_EVT_GET_FLVSESSIONKEY:
                String flvSessionKey = param.getString(TXLiveConstants.EVT_DESCRIPTION, "");
                postToast("event PLAY_EVT_GET_FLVSESSIONKEY: " + flvSessionKey);
                break;
            case TXLiveConstants.PLAY_EVT_GET_METADATA:
                postToast("event PLAY_EVT_GET_METADATA");
                break;
            case TXLiveConstants.PLAY_WARNING_VIDEO_PLAY_LAG:
                postToast("event PLAY_WARNING_VIDEO_PLAY_LAG");
                break;
        }
        if (event < 0) {
            Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
    }

    private void postToast(final String text) {
        long delay = System.currentTimeMillis() - mTimeLastToast > TOAST_INTERVAL ? 0 : TOAST_INTERVAL;
        mTimeLastToast = System.currentTimeMillis();
        mLayoutRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        }, delay);
    }

    @Override
    public void onNetStatus(Bundle bundle) {
        Log.d(TAG, "Current status, CPU:" + bundle.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)
                + ", RES:" + bundle.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)
                + "*" + bundle.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)
                + ", SPD:" + bundle.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)
                + "Kbps" + ", FPS:" + bundle.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS)
                + ", ARA:" + bundle.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps"
                + ", VRA:" + bundle.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
        mLogInfoWindow.setLogText(bundle, null, 0);
    }

    public void onFetchURLFailure() {
        stopLoadingAnimation();
        Toast.makeText(mContext, R.string.v1liveplayer_error_get_test_res, Toast.LENGTH_LONG).show();
    }

    public void onFetchURLSuccess(String url) {
        stopLoadingAnimation();
        Toast.makeText(mContext, R.string.v1liveplayer_toast_fetch_test_res, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        stopPlay();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private boolean requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private void startLoadingAnimation() {
        if (mImageLoading != null) {
            mImageLoading.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mImageLoading.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mImageLoading != null) {
            mImageLoading.setVisibility(View.GONE);
            ((AnimationDrawable) mImageLoading.getDrawable()).stop();
        }
    }

    public void onPlayStart(int code) {
        switch (code) {
            case Constants.PLAY_STATUS_SUCCESS:
                startLoadingAnimation();
                break;
            case Constants.PLAY_STATUS_EMPTY_URL:
                Toast.makeText(mContext, R.string.v1liveplayer_warning_res_url_empty, Toast.LENGTH_SHORT).show();
                break;
            case Constants.PLAY_STATUS_INVALID_URL:
                Toast.makeText(mContext, R.string.v1liveplayer_warning_res_url_invalid, Toast.LENGTH_SHORT).show();
                break;
            case Constants.PLAY_STATUS_INVALID_PLAY_TYPE:
                break;
            case Constants.PLAY_STATUS_INVALID_RTMP_URL:
                Toast.makeText(mContext, R.string.v1liveplayer_warning_low_latency_format, Toast.LENGTH_SHORT).show();
                break;
            case Constants.PLAY_STATUS_INVALID_SECRET_RTMP_URL:
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.v1liveplayer_error_play_video)
                        .setMessage(R.string.v1liveplayer_warning_low_latency_singed)
                        .setNegativeButton(R.string.v1liveplayer_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton(R.string.v1liveplayer_btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(Constants.LIVE_PLAYER_REAL_TIME_PLAY_DOCUMENT_URL);
                        IntentUtils
                                .safeStartActivity(V1LivePlayerMainActivity.this, new Intent(Intent.ACTION_VIEW, uri));
                        dialog.dismiss();
                    }
                }).show();
                break;
            default:
                break;
        }
        if (code != Constants.PLAY_STATUS_SUCCESS) {
            mButtonPlay.setBackgroundResource(R.drawable.v1liveplayer_play_start_btn);
            mLayoutRoot.setBackgroundResource(R.drawable.v1liveplayer_content_bg);
            mImageRoot.setVisibility(View.VISIBLE);
            Bundle params = new Bundle();
            params.putString(TXLiveConstants.EVT_DESCRIPTION,
                    mContext.getResources().getString(R.string.v1liveplayer_warning_checkout_res_url));
            mLogInfoWindow.setLogText(null, params, LogInfoWindow.CHECK_RTMP_URL_FAIL);
        } else {
            mButtonPlay.setBackgroundResource(R.drawable.v1liveplayer_play_pause_btn);
            mLayoutRoot.setBackgroundColor(getResources().getColor(R.color.v1liveplayer_black));
            mImageRoot.setVisibility(View.GONE);
            Bundle params = new Bundle();
            params.putString(TXLiveConstants.EVT_DESCRIPTION,
                    mContext.getResources().getString(R.string.v1liveplayer_warning_checkout_res_url));
            mLogInfoWindow.setLogText(null, params, LogInfoWindow.CHECK_RTMP_URL_OK);
        }
    }

    public void onPlayStop() {
        mButtonPlay.setBackgroundResource(R.drawable.v1liveplayer_play_start_btn);
        mLayoutRoot.setBackgroundResource(R.drawable.v1liveplayer_content_bg);
        mImageRoot.setVisibility(View.VISIBLE);
        mLogInfoWindow.clear();
        stopLoadingAnimation();
    }

    private void setSettingInfo() {
        mPlayerConfig.setConnectRetryCount(mSettingInfo.connectRetryCount);
        mPlayerConfig.setConnectRetryInterval(mSettingInfo.connectRetryInterval);
        mPlayerConfig.setVideoBlockThreshold(mSettingInfo.videoBlockThreshold);
        mPlayerConfig.setFlvSessionKey(mSettingInfo.flvSessionKey);
        mPlayerConfig.setEnableMessage(mSettingInfo.enableSei);
        mPlayerConfig.setEnableMetaData(mSettingInfo.enableMetaData);
        mPlayerConfig.setAutoAdjustCacheTime(mSettingInfo.enableAutoAdjustCacheTime);
        mPlayerConfig.setCacheTime(mSettingInfo.cacheTime);
        mPlayerConfig.setMinAutoAdjustCacheTime(mSettingInfo.minAdjustCacheTime);
        mPlayerConfig.setMaxAutoAdjustCacheTime(mSettingInfo.maxAdjustCacheTime);

        if (SettingInfo.RENDER_TYPE_CLOUD_VIDEO_VIEW == mSettingInfo.renderType) {
            mVideoView.setVisibility(View.VISIBLE);
            mLivePlayer.setPlayerView(mVideoView);
        } else if (SettingInfo.RENDER_TYPE_SURFACE == mSettingInfo.renderType) {
            mSurfaceView.setVisibility(View.VISIBLE);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mLivePlayer.setSurface(mSurfaceView.getHolder().getSurface());
            mLivePlayer.setSurfaceSize(dm.widthPixels, dm.heightPixels);
        } else if (SettingInfo.RENDER_TYPE_CUSTOM == mSettingInfo.renderType) {
            if (SettingInfo.CUSTOM_RENDER_TYPE_TEXTURE == mSettingInfo.customRenderType) {
                mLivePlayer.setVideoRenderListener(this, null);
                mTextureView.setVisibility(View.VISIBLE);
                mCustomRenderVideoFrame = new CustomRenderVideoFrame();
                mCustomRenderVideoFrame.start(mTextureView);
            } else if (SettingInfo.CUSTOM_RENDER_TYPE_I420 == mSettingInfo.customRenderType) {
                mLivePlayer.setVideoRawDataListener(this);
                mTextureView.setVisibility(View.VISIBLE);
                mCustomRenderVideoFrame = new CustomRenderVideoFrame();
                mCustomRenderVideoFrame.start(mTextureView);
            }
        }
    }

    private void startPlay() {
        String playURL = mPlayURL;
        int code = checkPlayURL(playURL);
        if (code != Constants.PLAY_STATUS_SUCCESS) {
            mIsPlaying = false;
        } else {
            setSettingInfo();
            mLivePlayer.setPlayListener(this);
            /**
             * 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
             * - 只有 4.3 以上android系统才支持
             * - 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
             *
             */
            mLivePlayer.enableHardwareDecode(mHWDecode);
            mLivePlayer.setRenderRotation(mRenderRotation);
            mLivePlayer.setRenderMode(mRenderMode);
            mLivePlayer.setConfig(mPlayerConfig);
            mLivePlayer.setAudioVolumeEvaluationListener(this);
            /**
             * result返回值：
             * 0 success; -1 empty url; -2 invalid url; -3 invalid playType;
             */
            code = mLivePlayer.startPlay(playURL, mCurrentPlayURLType);
            mIsPlaying = code == 0;

            Log.d("video render", "timetrack start play");
            mStartPlayTS = System.currentTimeMillis();
        }

        //处理UI相关操作
        onPlayStart(code);
    }

    private void stopPlay() {
        if (!mIsPlaying) {
            return;
        }
        if (mLivePlayer != null) {
            mLivePlayer.stopRecord();
            mLivePlayer.setPlayListener(null);
            mLivePlayer.setAudioVolumeEvaluationListener(null);
            mLivePlayer.stopPlay(true);
        }
        mIsPlaying = false;

        if (mCustomRenderVideoFrame != null) {
            mCustomRenderVideoFrame.stop();
        }
        //处理UI相关操作
        onPlayStop();
    }

    private void togglePlay() {
        Log.d(TAG, "togglePlay: mIsPlaying:" + mIsPlaying + ", mCurrentPlayType:" + mActivityPlayType);
        if (mIsPlaying) {
            stopPlay();
        } else {
            startPlay();
        }
    }

    private void setPlayURL(int activityPlayType, String url) {
        mActivityPlayType = activityPlayType;
        mPlayURL = url;
    }

    private void setCacheStrategy(int cacheStrategy) {
        if (mCacheStrategy == cacheStrategy) {
            return;
        }
        mCacheStrategy = cacheStrategy;
        switch (cacheStrategy) {
            case Constants.CACHE_STRATEGY_FAST:
                mPlayerConfig.setAutoAdjustCacheTime(true);
                mPlayerConfig.setMaxAutoAdjustCacheTime(Constants.CACHE_TIME_FAST);
                mPlayerConfig.setMinAutoAdjustCacheTime(Constants.CACHE_TIME_FAST);
                mLivePlayer.setConfig(mPlayerConfig);
                break;
            case Constants.CACHE_STRATEGY_SMOOTH:
                mPlayerConfig.setAutoAdjustCacheTime(false);
                mPlayerConfig.setMaxAutoAdjustCacheTime(Constants.CACHE_TIME_SMOOTH);
                mPlayerConfig.setMinAutoAdjustCacheTime(Constants.CACHE_TIME_SMOOTH);
                mLivePlayer.setConfig(mPlayerConfig);
                break;
            case Constants.CACHE_STRATEGY_AUTO:
                mPlayerConfig.setAutoAdjustCacheTime(true);
                mPlayerConfig.setMaxAutoAdjustCacheTime(Constants.CACHE_TIME_SMOOTH);
                mPlayerConfig.setMinAutoAdjustCacheTime(Constants.CACHE_TIME_FAST);
                mLivePlayer.setConfig(mPlayerConfig);
                break;
            default:
                break;
        }
    }

    private void setRenderMode(int renderMode) {
        mRenderMode = renderMode;
        mLivePlayer.setRenderMode(renderMode);
    }

    private int getRenderMode() {
        return mRenderMode;
    }

    private void setRenderRotation(int renderRotation) {
        mRenderRotation = renderRotation;
        mLivePlayer.setRenderRotation(renderRotation);
    }

    private int getRenderRotation() {
        return mRenderRotation;
    }

    private void setHWDecode(int mode) {
        mHWDecode = mode == 0;
        if (mIsPlaying) {
            stopPlay();
            startPlay();
        }
    }

    private void showVideoLog(boolean enable) {
        Class clazz = mLivePlayer.getClass();
        try {
            Method method = clazz.getMethod("showDebugView", new Class[]{boolean.class});
            method.invoke(mLivePlayer, enable);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            mVideoView.showLog(enable);
        }
    }

    private void destroy() {
        if (mOkHttpClient != null) {
            mOkHttpClient.dispatcher().cancelAll();
        }
        if (mLivePlayer != null) {
            mLivePlayer.stopPlay(true);
            mLivePlayer = null;
        }
        if (mVideoView != null) {
            mVideoView.onDestroy();
            mVideoView = null;
        }
        mPlayerConfig = null;
    }

    private int checkPlayURL(final String playURL) {
        if (TextUtils.isEmpty(playURL)) {
            return Constants.PLAY_STATUS_EMPTY_URL;
        }

        if (!playURL.startsWith(Constants.URL_PREFIX_HTTP) && !playURL.startsWith(Constants.URL_PREFIX_HTTPS)
                && !playURL.startsWith(Constants.URL_PREFIX_RTMP) && !playURL.startsWith("/")) {
            return Constants.PLAY_STATUS_INVALID_URL;
        }

        boolean isLiveRTMP = playURL.startsWith(Constants.URL_PREFIX_RTMP);
        boolean isLiveFLV = (playURL.startsWith(Constants.URL_PREFIX_HTTP)
                || playURL.startsWith(Constants.URL_PREFIX_HTTPS)) && playURL.contains(Constants.URL_SUFFIX_FLV);

        if (mActivityPlayType == Constants.ACTIVITY_TYPE_LIVE_PLAY) {
            if (isLiveRTMP) {
                mCurrentPlayURLType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
                return Constants.PLAY_STATUS_SUCCESS;
            }
            if (isLiveFLV) {
                mCurrentPlayURLType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
                return Constants.PLAY_STATUS_SUCCESS;
            }
            return Constants.PLAY_STATUS_INVALID_URL;
        }

        if (mActivityPlayType == Constants.ACTIVITY_TYPE_REALTIME_PLAY) {
            if (!isLiveRTMP) {
                return Constants.PLAY_STATUS_INVALID_RTMP_URL;
            }
            if (!playURL.contains(Constants.URL_TX_SECRET)) {
                return Constants.PLAY_STATUS_INVALID_SECRET_RTMP_URL;
            }
            mCurrentPlayURLType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP_ACC;
            return Constants.PLAY_STATUS_SUCCESS;
        }
        return Constants.PLAY_STATUS_INVALID_URL;
    }

    @Override
    public void onRenderVideoFrame(TXLivePlayer.TXLiteAVTexture texture) {
        if (mCustomRenderVideoFrame != null) {
            CustomRenderVideoFrame.LiteAVTexture liteAVTexture = new CustomRenderVideoFrame.LiteAVTexture();
            liteAVTexture.textureId = texture.textureId;
            liteAVTexture.eglContext = texture.eglContext;
            liteAVTexture.width = texture.width;
            liteAVTexture.height = texture.height;
            mCustomRenderVideoFrame.onRenderVideoFrame(liteAVTexture);
        }
    }

    @Override
    public void onVideoRawDataAvailable(byte[] yuvBuffer, int width, int height, int timestamp) {

        if (mCustomRenderVideoFrame != null) {
            CustomRenderVideoFrame.LiveVideoFrame videoFrame = new CustomRenderVideoFrame.LiveVideoFrame();
            videoFrame.data = yuvBuffer;
            videoFrame.width = width;
            videoFrame.height = height;
            videoFrame.renderType = CustomRenderVideoFrame.RENDER_TYPE_I420;
            mCustomRenderVideoFrame.onRenderVideoFrame(videoFrame);
        }
    }

    @Override
    public void onSnapshot(Bitmap image) {
        if (null == image) {
            return;
        }
        final Dialog dialog = new Dialog(this);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = image.getWidth();
        params.height = image.getHeight();
        dialog.getWindow().setAttributes(params);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(image);
        dialog.setContentView(imageView);
        dialog.show();
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    @Override
    public void onAudioVolumeEvaluationNotify(int volume) {
        mVolumeProgressBar.setProgress(volume);
    }
}