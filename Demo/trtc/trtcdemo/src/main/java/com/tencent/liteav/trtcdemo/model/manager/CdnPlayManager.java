package com.tencent.liteav.trtcdemo.model.manager;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.net.URLEncoder;

import static com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig.CACHE_STRATEGY_AUTO;
import static com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig.CACHE_STRATEGY_FAST;
import static com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig.CACHE_STRATEGY_SMOOTH;
import static com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig.CACHE_TIME_FAST;
import static com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig.CACHE_TIME_SMOOTH;

/**
 * CDN播放相关 管理类
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class CdnPlayManager {
    private V2TXLivePlayer   mLivePlayer;
    private TXCloudVideoView mPlayerView;
    private String           mPlayUrl;
    private CdnPlayerConfig  mCdnPlayerConfig;
    private TXLivePlayConfig mPlayConfig;

    public CdnPlayManager(TXCloudVideoView playerView, V2TXLivePlayerObserver observer) {
        mPlayerView = playerView;
        mLivePlayer = new V2TXLivePlayerImpl(playerView.getContext());
        mPlayConfig = new TXLivePlayConfig();
        mPlayConfig.setEnableMessage(true);
        mLivePlayer.setRenderView(mPlayerView);
        mLivePlayer.setObserver(observer);
    }

    public void initPlayUrl(String roomId, String userId) {
        // 注意：该功能需要在控制台开启【旁路直播】功能，
        // 此功能是获取 CDN 直播地址，通过此功能，方便您能够在常见播放器中，播放音视频流。
        // 【*****】更多信息，您可以参考：https://cloud.tencent.com/document/product/647/16826
        String streamId = "" + GenerateTestUserSig.SDKAPPID + "_" + roomId + "_" + userId + "_main";
        try {
            streamId = URLEncoder.encode(streamId, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String playUrl = "http://3891.liveplay.myqcloud.com/live/" + streamId + ".flv";
        mPlayUrl = playUrl;
    }

    public void applyConfigToPlayer() {
        mCdnPlayerConfig = SettingConfigHelper.getInstance().getCdnPlayerConfig();
        mLivePlayer.setRenderRotation(getRenderRotation(mCdnPlayerConfig.getCurrentRenderRotation()));
        mLivePlayer.setRenderFillMode(getRenderFillMode(mCdnPlayerConfig.getCurrentRenderMode()));
        setCacheStrategy(mCdnPlayerConfig.getCacheStrategy());
    }

    private V2TXLiveDef.V2TXLiveRotation getRenderRotation(int rotation) {
        if (TXLiveConstants.RENDER_ROTATION_LANDSCAPE == rotation) {
            return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation270;
        } else if (TXLiveConstants.RENDER_ROTATION_PORTRAIT == rotation) {
            return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
        }
        return V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
    }

    private V2TXLiveDef.V2TXLiveFillMode getRenderFillMode(int mode) {
        if (TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN == mode) {
            return V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill;
        } else if (TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION == mode) {
            return V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit;
        }
        return V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit;
    }


    public void setDebug(boolean enable) {
        mPlayerView.showLog(enable);
    }

    public void startPlay() {
        if (TextUtils.isEmpty(mPlayUrl)) {
            ToastUtils.showLong("请先设置播放url");
            return;
        }
        applyConfigToPlayer();
        int res = mLivePlayer.startPlay(mPlayUrl);
        if (res == 0) {
        } else {
            ToastUtils.showLong("播放失败：" + res);
        }
    }

    public void destroy() {
        if (isPlaying()) {
            mLivePlayer.stopPlay();
        }
    }

    public void stopPlay() {
        mLivePlayer.stopPlay();
    }

    public boolean isPlaying() {
        return 1 == mLivePlayer.isPlaying();
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      缓存策略配置
    //
    /////////////////////////////////////////////////////////////////////////////////
    private void setCacheStrategy(int nCacheStrategy) {
        switch (nCacheStrategy) {
            case CACHE_STRATEGY_FAST:
                mLivePlayer.setCacheParams(CACHE_TIME_FAST, CACHE_TIME_FAST);
                break;
            case CACHE_STRATEGY_SMOOTH:
                mLivePlayer.setCacheParams(CACHE_TIME_SMOOTH, CACHE_TIME_SMOOTH);
                break;
            case CACHE_STRATEGY_AUTO:
                mLivePlayer.setCacheParams(CACHE_TIME_FAST, CACHE_TIME_SMOOTH);
                break;
            default:
                break;
        }
    }
}
