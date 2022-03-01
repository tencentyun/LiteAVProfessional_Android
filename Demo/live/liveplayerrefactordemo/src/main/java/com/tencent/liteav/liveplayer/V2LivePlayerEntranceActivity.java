package com.tencent.liteav.liveplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;


/**
 * V2直播拉流入口页面
 */
public class V2LivePlayerEntranceActivity extends Activity {

    private Context mContext;
    private RadioGroup mRgRenderType;
    private RadioGroup mRgPayloadType;
    private LinearLayout mLayoutSpinners;
    private RadioButton mRbTexture;
    private RadioButton mRbI420ByteArray;
    private RadioButton mRbI420ByteBuffer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.liveplayer_activity_v2_live_player_entrance);
        initViews();
    }

    private void initViews() {
        TextView textTitle = findViewById(R.id.tv_topBar_title);
        textTitle.setText("V2直播播放");

        findViewById(R.id.btn_topBar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_topBar_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuestionLink();
            }
        });
        findViewById(R.id.btn_v2_entrance_normal_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLivePlayer();
            }
        });

        mRgRenderType = findViewById(R.id.rg_v2_entrance_renderType);
        mRgPayloadType = findViewById(R.id.rg_v2_entrance_payloadType);
        mLayoutSpinners = findViewById(R.id.ll_v2_entrance_spinners);
        mRbI420ByteArray = findViewById(R.id.rb_v2_i420_bytearray);
        mRbI420ByteBuffer = findViewById(R.id.rb_v2_i420_bytebuffer);
        mRbTexture = findViewById(R.id.rb_v2_texture2d);


        mRgRenderType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mRgRenderType.getCheckedRadioButtonId() == R.id.rb_v2_entrance_custom) {
                    mLayoutSpinners.setVisibility(View.VISIBLE);
                } else {
                    mLayoutSpinners.setVisibility(View.GONE);
                }
            }
        });
    }

    private int getRenderType() {
        int checkedRadioButtonId = mRgRenderType.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rb_v2_entrance_TXCloudVideoView) {
            return 1;
        } else if (checkedRadioButtonId == R.id.rb_v2_entrance_textureView) {
            return 2;
        } else if (checkedRadioButtonId == R.id.rb_v2_entrance_surfaceView) {
            return 3;
        } else if (checkedRadioButtonId == R.id.rb_v2_entrance_surface) {
            return 4;
        } else if (checkedRadioButtonId == R.id.rb_v2_entrance_custom) {
            return 5;
        }
        return 0;
    }

    private int getPayloadType() {
        int checkedRadioButtonId = mRgPayloadType.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rb_v2_entrance_seiClose) {
            return 0;
        } else if (checkedRadioButtonId == R.id.rb_v2_entrance_sei5) {
            return 5;
        } else if (checkedRadioButtonId == R.id.rb_v2_entrance_sei242) {
            return 242;
        }
        return 0;
    }

    private void startLivePlayer() {
        Intent intent = new Intent(mContext, V2LivePlayerMainActivity.class);
        intent.putExtra(Constants.RENDER_TYPE, getRenderType());
        intent.putExtra(Constants.PAYLOAD_TYPE, getPayloadType());
        if (mRbTexture.isChecked()) {
            intent.putExtra("pixel_format", 2);
            intent.putExtra("buffer_type", 2);
        } else if (mRbI420ByteBuffer.isChecked()) {
            intent.putExtra("pixel_format", 1);
            intent.putExtra("buffer_type", 1);
        } else {
            intent.putExtra("pixel_format", 1);
            intent.putExtra("buffer_type", 3);
        }

        startActivity(intent);
    }

    private void startQuestionLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.LIVE_PLAYER_DOCUMENT_URL));
        startActivity(intent);
    }

}
