package com.tencent.liteav.trtcdemo.ui.widget.settingitem.enteritem;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tencent.liteav.trtcdemo.R;

public class CameraCaptureResolutionItem extends LinearLayout {
    private static final String TAG = "ResolutionItem";

    private EditText mXResolutionEt;
    private EditText mYResolutionEt;

    public CameraCaptureResolutionItem(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trtcdemo_item_camera_capture_resolution, this);
        mXResolutionEt = findViewById(R.id.trtcdemo_x_resolution_et);
        mYResolutionEt = findViewById(R.id.trtcdemo_y_resolution_et);

    }

    public int getXResolution() {
        return getResolution(mXResolutionEt);
    }

    public int getYResolution() {
        return getResolution(mYResolutionEt);
    }

    private int getResolution(EditText editText) {
        String value = editText.getText().toString();
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        int resolution = 0;
        try {
            resolution = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getResolution NumberFormatException : " + value);
        }
        return resolution;
    }
}
