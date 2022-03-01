package com.tencent.liteav.v1liveplayerdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.liteav.v1liveplayerdemo.model.SettingInfo;
import com.tencent.liteav.v1liveplayerdemo.model.SettingItem;
import com.tencent.liteav.v1liveplayerdemo.view.InputView;
import com.tencent.liteav.v1liveplayerdemo.view.SettingItemView;
import com.tencent.liteav.v1liveplayerdemo.view.VariableRadioSelectorView;

import java.util.ArrayList;
import java.util.List;

public class V1LivePlayerSettingActivity extends Activity {

    private VariableRadioSelectorView mRenderTypeRadioSelectView;
    private VariableRadioSelectorView mCustomRenderTypeRadioSelectView;

    private InputView mInputCacheTime;
    private InputView mInputMaxCacheTime;
    private InputView mInputMinCacheTime;
    private InputView mInputVideoBlock;
    private InputView mInputConnRetryCount;
    private InputView mInputConnRetryInterval;
    private InputView mInputFLVSessionKey;

    private Context     mContext;
    private SettingInfo mSettingInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.v1liveplayer_activity_live_player_setting);
        initViews();
    }

    private void initViews() {
        mSettingInfo = (SettingInfo) getIntent().getSerializableExtra(Constants.INTENT_SETTING_RESULT);
        ((TextView) findViewById(R.id.v1liveplayer_title_textview)).setText(R.string.v1liveplayer_setting);
        findViewById(R.id.v1liveplayer_ibtn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.v1liveplayer_ibtn_right).setVisibility(View.GONE);

        final LinearLayout llContent = findViewById(R.id.v1liveplayer_ll_content);
        List<SettingItem> data = new ArrayList<>();
        data.add(new SettingItem(getString(R.string.v1liveplayer_enable_sei),
                mSettingInfo.enableSei, new SettingItemView.OnEventListener() {
            @Override
            public void onChecked(SettingItemView itemView, boolean isChecked) {
                mSettingInfo.enableSei = isChecked;
            }
        }));
        data.add(new SettingItem(getString(R.string.v1liveplayer_enable_metadata),
                mSettingInfo.enableMetaData, new SettingItemView.OnEventListener() {
            @Override
            public void onChecked(SettingItemView itemView, boolean isChecked) {
                mSettingInfo.enableMetaData = isChecked;
            }
        }));
        data.add(new SettingItem(getString(R.string.v1liveplayer_enable_auto_adjust_cachetime),
                mSettingInfo.enableAutoAdjustCacheTime, new SettingItemView.OnEventListener() {
            @Override
            public void onChecked(SettingItemView itemView, boolean isChecked) {
                mSettingInfo.enableAutoAdjustCacheTime = isChecked;
            }
        }));
        setData(llContent, data);

        TextView textView = new TextView(mContext);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setText(getString(R.string.v1liveplayer_render_type));
        llContent.addView(textView);
        mRenderTypeRadioSelectView = new VariableRadioSelectorView(mContext);
        llContent.addView(mRenderTypeRadioSelectView);
        mRenderTypeRadioSelectView.setOrientation(LinearLayout.VERTICAL);
        mRenderTypeRadioSelectView.setData(getResources().getStringArray(R.array.v1liveplayer_render_type),
                mSettingInfo.renderType);
        mRenderTypeRadioSelectView.setRadioSelectListener(new VariableRadioSelectorView.RadioSelectListener() {
            @Override
            public void onChecked(int prePosition, VariableRadioSelectorView.RadioButton preRadioButton,
                                  int curPosition, VariableRadioSelectorView.RadioButton curRadioButton) {
                if (curPosition == SettingInfo.RENDER_TYPE_CUSTOM) {
                    mCustomRenderTypeRadioSelectView.setVisibility(View.VISIBLE);
                } else {
                    mCustomRenderTypeRadioSelectView.setVisibility(View.GONE);
                }
            }
        });
        mCustomRenderTypeRadioSelectView = new VariableRadioSelectorView(mContext);
        llContent.addView(mCustomRenderTypeRadioSelectView);
        mCustomRenderTypeRadioSelectView.setPadding(40, 0, 0, 0);
        mCustomRenderTypeRadioSelectView.setVisibility(View.GONE);
        mCustomRenderTypeRadioSelectView.setOrientation(LinearLayout.VERTICAL);
        mCustomRenderTypeRadioSelectView
                .setData(getResources().getStringArray(R.array.v1liveplayer_custom_render_type),
                        mSettingInfo.customRenderType);
        if (mSettingInfo.renderType == SettingInfo.RENDER_TYPE_CUSTOM) {
            mCustomRenderTypeRadioSelectView.setVisibility(View.VISIBLE);
        }
        mInputCacheTime = new InputView(mContext, getString(R.string.v1liveplayer_player_cachetime),
                String.valueOf(mSettingInfo.cacheTime));
        mInputCacheTime.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        llContent.addView(mInputCacheTime);
        mInputMaxCacheTime = new InputView(mContext, getString(R.string.v1liveplayer_max_auto_adjust_cachetime),
                String.valueOf(mSettingInfo.maxAdjustCacheTime));
        mInputMaxCacheTime.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        llContent.addView(mInputMaxCacheTime);
        mInputMinCacheTime = new InputView(mContext, getString(R.string.v1liveplayer_min_auto_adjust_cachetime),
                String.valueOf(mSettingInfo.minAdjustCacheTime));
        mInputMinCacheTime.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        llContent.addView(mInputMinCacheTime);

        mInputVideoBlock = new InputView(mContext, getString(R.string.v1liveplayer_video_block_threshold),
                String.valueOf(mSettingInfo.videoBlockThreshold));
        mInputVideoBlock.setInputType(InputType.TYPE_CLASS_NUMBER);
        llContent.addView(mInputVideoBlock);
        mInputConnRetryCount = new InputView(mContext, getString(R.string.v1liveplayer_connect_retry_count),
                String.valueOf(mSettingInfo.connectRetryCount));
        mInputConnRetryCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        llContent.addView(mInputConnRetryCount);
        mInputConnRetryInterval = new InputView(mContext, getString(R.string.v1liveplayer_connect_retry_interval),
                String.valueOf(mSettingInfo.connectRetryInterval));
        mInputConnRetryInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        llContent.addView(mInputConnRetryInterval);
        mInputFLVSessionKey = new InputView(mContext, getString(R.string.v1liveplayer_flv_session_key),
                String.valueOf(mSettingInfo.flvSessionKey));
        llContent.addView(mInputFLVSessionKey);
    }

    /**
     * set data.
     *
     * @param data
     */
    public void setData(LinearLayout contentView, List<SettingItem> data) {
        if (data == null || data.size() == 0) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            SettingItem item = data.get(i);
            int type = item.getType();
            SettingItemView view = new SettingItemView(mContext, type);
            view.setTitleText(item.getTitle());
            view.setListener(item.getListener());
            fillData(item, view);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = dip2px(8);
            view.setLayoutParams(layoutParams);
            contentView.addView(view);
            view.setPadding(0, 0, 0, 0);
        }
    }

    private void fillData(SettingItem item, SettingItemView view) {
        int type = item.getType();
        if (type == SettingItemView.TYPE_SWITCH) {
            view.setChecked(item.isChecked());
        } else if (type == SettingItemView.TYPE_BUTTON) {
            view.setButtonText(item.getButtonText());
        } else if (type == SettingItemView.TYPE_PROGRESS) {
            view.setProgressMax(item.getProgressMax());
            view.setProgress(item.getProgress());
        }
        view.setTitleSize(16);
        view.setTitleColor(Color.WHITE);
        view.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void finish() {
        mSettingInfo.cacheTime = str2Float(mInputCacheTime.getInput());
        mSettingInfo.minAdjustCacheTime = str2Float(mInputMinCacheTime.getInput());
        mSettingInfo.maxAdjustCacheTime = str2Float(mInputMaxCacheTime.getInput());
        mSettingInfo.renderType = mRenderTypeRadioSelectView.getSelectPosition();
        mSettingInfo.customRenderType = mCustomRenderTypeRadioSelectView.getSelectPosition();
        mSettingInfo.connectRetryCount = str2Int(mInputConnRetryCount.getInput());
        mSettingInfo.connectRetryInterval = str2Int(mInputConnRetryInterval.getInput());
        mSettingInfo.videoBlockThreshold = str2Int(mInputVideoBlock.getInput());
        mSettingInfo.flvSessionKey = mInputFLVSessionKey.getInput();
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_SETTING_RESULT, mSettingInfo);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    private int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int str2Int(String value) {
        int ret = 0;
        try {
            ret = Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private float str2Float(String value) {
        float ret = 0;
        try {
            ret = Float.parseFloat(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
