package com.tencent.liteav.demo.videoediter.bubble.ui.bubble;

/**
 * Created by hanszhli on 2017/6/20.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;

import com.tencent.liteav.demo.videoediter.common.widget.layer.TCLayerOperationView;


/**
 * 气泡字幕的View
 * <p>
 * 根绝参数 初始化气泡字幕
 */
public class TCWordBubbleView extends TCLayerOperationView {
    private static final String TAG = "TCWordBubbleView";

    private TCBubbleViewParams mParams;

    public TCWordBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TCWordBubbleView(Context context) {
        super(context);
    }

    public TCWordBubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBubbleParams(TCBubbleViewParams params) {
        mParams = params;
        if (params == null) {
            return;
        }
        if (params.text == null) {
            params.text = "";
            Log.w(TAG, "setBubbleParams: bubble text is null");
        }
        TCBubbleViewHelper helper = new TCBubbleViewHelper();
        helper.setBubbleTextParams(params);
        Bitmap bitmap = helper.createBubbleTextBitmap();
        setImageBitamp(bitmap);
        mParams.bubbleBitmap = null;
        invalidate();
    }

    public TCBubbleViewParams getBubbleParams() {
        return mParams;
    }

    private long mStartTime, mEndTime;

    public void setStartTime(long startTime, long endTime) {
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }


}