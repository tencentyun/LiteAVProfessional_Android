package com.tencent.liteav.trtcdemo.ui.widget.settingitem.mixstreamitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;
import com.tencent.liteav.trtcdemo.model.manager.TRTCCloudManager;

public class PublishCDNStreamItem extends LinearLayout {
    private EditText         mEditCDNStreamURL;
    private EditText         mEditCDNStreamId;
    private Button           mButtonStartPushCNDStream;
    private TRTCCloudManager mTRTCCloudManager;

    private boolean mPublishingCDNStream = false;

    public PublishCDNStreamItem(TRTCCloudManager manager, Context context) {
        super(context);
        mTRTCCloudManager = manager;
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_publish_cdn_stream_layout, this);
        mEditCDNStreamURL = findViewById(R.id.trtcdemo_et_cdn_stream_url);
        mEditCDNStreamId = findViewById(R.id.trtcdemo_et_cdn_stream_id);
        mButtonStartPushCNDStream = findViewById(R.id.trtcdemo_start_push_cdn_stream_btn);
        mButtonStartPushCNDStream.setText(mPublishingCDNStream ? R.string.trtcdemo_btn_stop_third_party_cdn_publish :
                R.string.trtcdemo_btn_start_third_party_cdn_publish);
        mButtonStartPushCNDStream.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String cdnStreamURL = mEditCDNStreamURL.getText().toString().trim();
                String mixStreamId = mEditCDNStreamId.getText().toString().trim();
                if (mPublishingCDNStream) {
                    mTRTCCloudManager.stopPublishCDNStream();
                } else {
                    mTRTCCloudManager.startPublishCDNStream(cdnStreamURL, mixStreamId);
                }
                mPublishingCDNStream = !mPublishingCDNStream;
                mButtonStartPushCNDStream.setText(mPublishingCDNStream
                        ? R.string.trtcdemo_btn_stop_third_party_cdn_publish :
                        R.string.trtcdemo_btn_start_third_party_cdn_publish);
            }
        });
    }
}
