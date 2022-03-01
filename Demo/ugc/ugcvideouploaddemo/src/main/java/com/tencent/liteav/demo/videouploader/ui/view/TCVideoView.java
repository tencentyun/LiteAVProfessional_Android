package com.tencent.liteav.demo.videouploader.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * TCVideoView在TXCloudVideoView上面叠加一个logview,用于显示sdk 内部状态及事件
 */
public class TCVideoView extends TXCloudVideoView {

    private TCLogView mLogView;

    public TCVideoView(Context context) {
        this(context, null);
    }

    public TCVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLogView = new TCLogView(context);
        addView(mLogView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mLogView.setVisibility(GONE);
    }

    /**
     * 下面的代码用于在视频浮层显示Log和事件
     */
    public void disableLog(boolean disable) {
        if (disable) {
            mLogView.setVisibility(GONE);
        } else {
            mLogView.setVisibility(VISIBLE);
        }
    }

    public void clearLog() {
        mLogView.clearLog();
    }

    public void setLogText(Bundle status, Bundle event, int eventId) {
        mLogView.setLogText(status, event, eventId);
    }

}
