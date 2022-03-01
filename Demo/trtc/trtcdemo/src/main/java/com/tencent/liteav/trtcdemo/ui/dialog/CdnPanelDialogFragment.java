package com.tencent.liteav.trtcdemo.ui.dialog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.helper.SettingConfigHelper;
import com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig;
import com.tencent.liteav.trtcdemo.model.manager.CdnPlayManager;
import com.tencent.liteav.trtcdemo.ui.base.BaseDialogFragment;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.trtcdemo.ui.widget.settingitem.RadioButtonSettingItem;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.trtcdemo.model.bean.CdnPlayerConfig.CACHE_STRATEGY_FAST;
import static com.tencent.rtmp.TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
import static com.tencent.rtmp.TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
import static com.tencent.rtmp.TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
import static com.tencent.rtmp.TXLiveConstants.RENDER_ROTATION_PORTRAIT;

/**
 * cdn播放管理界面
 *
 * @author : xander
 * @date : 2021/5/25
 */
public class CdnPanelDialogFragment extends BaseDialogFragment {
    private LinearLayout           mContentItem;
    private List<BaseSettingItem>  mSettingItemList;
    private RadioButtonSettingItem mVideoFillModeItem;
    private RadioButtonSettingItem mRotationItem;
    private RadioButtonSettingItem mCacheTypeItem;
    private CdnPlayerConfig        mCdnPlayerConfig;
    private CdnPlayManager         mCdnPlayManager;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    public void setCdnPlayManager(CdnPlayManager cdnPlayManager) {
        mCdnPlayManager = cdnPlayManager;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtcdemo_fragment_confirm_setting;
    }

    private void initView(@NonNull final View itemView) {
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mCdnPlayerConfig = SettingConfigHelper.getInstance().getCdnPlayerConfig();
        mSettingItemList = new ArrayList<>();

        mVideoFillModeItem = new RadioButtonSettingItem(getContext(),
                new BaseSettingItem.ItemText(getString(R.string.trtcdemo_frame_fill_mode),
                        getString(R.string.trtcdemo_frame_fill_mode_fill), getString(R.string.trtcdemo_frame_fill_mode_fit)),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mCdnPlayerConfig.setCurrentRenderMode(index == 0 ? RENDER_MODE_FULL_FILL_SCREEN
                                : RENDER_MODE_ADJUST_RESOLUTION);
                        if (mCdnPlayManager != null) {
                            mCdnPlayManager.applyConfigToPlayer();
                        }
                    }
                });
        mSettingItemList.add(mVideoFillModeItem);

        mRotationItem = new RadioButtonSettingItem(getContext(),
                new BaseSettingItem.ItemText(getString(R.string.trtcdemo_frame_rotate), "0", "270"),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        int rotation = RENDER_ROTATION_PORTRAIT;
                        if (index == 1) {
                            rotation = RENDER_ROTATION_LANDSCAPE;
                        }
                        mCdnPlayerConfig.setCurrentRenderRotation(rotation);
                        if (mCdnPlayManager != null) {
                            mCdnPlayManager.applyConfigToPlayer();
                        }
                    }
                });
        mSettingItemList.add(mRotationItem);

        mCacheTypeItem = new RadioButtonSettingItem(getContext(), new BaseSettingItem.ItemText("缓冲方式", "快速", "平滑", "自动"),
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        mCdnPlayerConfig.setCacheStrategy(CACHE_STRATEGY_FAST + index);
                        if (mCdnPlayManager != null) {
                            mCdnPlayManager.applyConfigToPlayer();
                        }
                    }
                });
        mSettingItemList.add(mCacheTypeItem);

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(5), 0, 0);
            mContentItem.addView(view);
        }

        updateView();
    }

    private void updateView() {
        mVideoFillModeItem.setSelect(mCdnPlayerConfig.getCurrentRenderMode() == RENDER_MODE_ADJUST_RESOLUTION ? 1 : 0);
        mRotationItem.setSelect(getRotationIndex(mCdnPlayerConfig.getCurrentRenderRotation()));
        mCacheTypeItem.setSelect(mCdnPlayerConfig.getCacheStrategy() - CACHE_STRATEGY_FAST);
    }

    private int getRotationIndex(int type) {
        switch (type) {
            case RENDER_ROTATION_PORTRAIT:
                return 0;
            case RENDER_ROTATION_LANDSCAPE:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    protected int getHeight(DisplayMetrics dm) {
        return (int) (dm.heightPixels * 0.4);
    }

}
