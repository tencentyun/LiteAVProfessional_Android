package com.tencent.liteav.trtcdemo.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.customcapture.utils.Utils;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.ui.base.BaseSettingFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.base.AbsRadioButtonItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem.CloudMixStreamItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem.MixBackgroundItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem.MixCustomIdItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem.PublishCDNStreamItem;

/**
 * 混流相关配置
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class SteamSettingFragment extends BaseSettingFragment implements View.OnClickListener {

    private ImageView          mImageQr;
    private Button             mButtonCopy;
    private String             mPlayUrl;
    private LinearLayout       mContent;
    private AbsRadioButtonItem mCloudMixStreamItem;
    private AbsRadioButtonItem mMixBackgroundItem;

    @Override
    protected void initView(View itemView) {
        mContent = itemView.findViewById(R.id.ll_content);
        mImageQr = itemView.findViewById(R.id.iv_qrcode);
        mButtonCopy = itemView.findViewById(R.id.btn_copy);
        mButtonCopy.setOnClickListener(this);
        updateQrView();

        initContent();
    }

    private void updateQrView() {
        mPlayUrl = getPlayUrl();
        if (mImageQr == null) {
            return;
        }
        if (TextUtils.isEmpty(mPlayUrl)) {
            mImageQr.setVisibility(View.GONE);
            mButtonCopy.setVisibility(View.GONE);
            return;
        } else {
            mImageQr.setVisibility(View.VISIBLE);
            mButtonCopy.setVisibility(View.VISIBLE);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = Utils.createQRCodeBitmap(mPlayUrl, 400, 400);
                mImageQr.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageQr.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private void initContent() {

        mCloudMixStreamItem = new CloudMixStreamItem(getContext(),
                getString(R.string.trtcdemo_cloud_mixstream),
                getString(R.string.trtcdemo_cloud_mixstream_close),
                getString(R.string.trtcdemo_cloud_mixstream_manual),
                getString(R.string.trtcdemo_cloud_mixstream_audio),
                getString(R.string.trtcdemo_cloud_mixstream_preset)) {
            @Override
            public void onSelected(int index) {
                super.onSelected(index);
                if (mTRTCRemoteUserManager != null)
                    mTRTCRemoteUserManager.updateCloudMixtureParams();
            }
        };
        mContent.addView(mCloudMixStreamItem);

        mMixBackgroundItem = new MixBackgroundItem(getContext(),
                getString(R.string.trtcdemo_background),
                getString(R.string.trtcdemo_background_close),
                getString(R.string.trtcdemo_background1),
                getString(R.string.trtcdemo_background2)) {
            @Override
            public void onSelected(int index) {
                super.onSelected(index);
                if (mTRTCRemoteUserManager != null)
                    mTRTCRemoteUserManager.updateCloudMixtureParams();
            }
        };
        mContent.addView(mMixBackgroundItem);

        MixCustomIdItem mixCustomIdItem = new MixCustomIdItem(getContext()) {
            @Override
            public void onClickSet(String mixId) {
                if (mTRTCRemoteUserManager == null) {
                    return;
                }
                mTRTCRemoteUserManager.updateCloudMixtureParams();
            }
        };
        mContent.addView(mixCustomIdItem);

        PublishCDNStreamItem publishCDNStreamItem = new PublishCDNStreamItem(mTRTCCloudManager, getContext());
        mContent.addView(publishCDNStreamItem);
    }

    /**
     * 注意：该功能需要在控制台开启【旁路直播】功能，
     * 此功能是获取 CDN 直播地址，通过此功能，方便您能够在常见播放器中，播放音视频流。
     * 【*****】更多信息，您可以参考：https://cloud.tencent.com/document/product/647/16826
     *
     * @return 播放地址
     */
    private String getPlayUrl() {
        String playUrl;
        String customStreamId = SettingConfigHelper.getInstance().getVideoConfig().getMixStreamId();
        String steamId = mTRTCCloudManager.getDefaultPlayUrl();
        playUrl = "http://3891.liveplay.myqcloud.com/live/" + (TextUtils.isEmpty(customStreamId) ? steamId : customStreamId) + ".flv";
        return playUrl;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_mix_setting;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_copy) {
            if (TextUtils.isEmpty(mPlayUrl)) {
                ToastUtils.showShort(R.string.trtcdemo_play_address_not_empty);
                return;
            }
            copyContentToClipboard(mPlayUrl, getContext());
            ToastUtils.showShort(R.string.trtcdemo_completed_copy);
        }
    }

    public void copyContentToClipboard(String content, Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", content);
        cm.setPrimaryClip(mClipData);
    }

    private boolean isEnableDebugMode() {
        return AppRuntime.get().isDebug();
    }
}
