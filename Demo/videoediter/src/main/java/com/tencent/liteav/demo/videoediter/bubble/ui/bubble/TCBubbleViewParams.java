package com.tencent.liteav.demo.videoediter.bubble.ui.bubble;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.tencent.liteav.demo.videoediter.bubble.ui.popwin.TCWordParamsInfo;
import com.tencent.liteav.demo.videoediter.bubble.utils.TCBubbleInfo;


/**
 * Created by hans on 2017/10/19.
 * <p>
 * 用于初始化气泡字幕控件{@link TCWordBubbleView} 的参数配置
 */
public class TCBubbleViewParams {
    public String text;
    public Bitmap bubbleBitmap;
    public TCWordParamsInfo wordParamsInfo;

    public static TCBubbleViewParams createDefaultParams(String text) {
        TCBubbleViewParams params = new TCBubbleViewParams();
        params.bubbleBitmap = null;
        params.text = text;

        TCWordParamsInfo info = new TCWordParamsInfo();
        info.setTextColor(Color.WHITE);

        // 初始化为无字幕的 配置信息
        // 创建一个默认的
        TCBubbleInfo bubbleInfo = new TCBubbleInfo();
        bubbleInfo.setHeight(0);
        bubbleInfo.setWidth(0);
        bubbleInfo.setDefaultSize(40);
        bubbleInfo.setBubblePath(null);
        bubbleInfo.setIconPath(null);
        bubbleInfo.setRect(0, 0, 0, 0);

        info.setBubbleInfo(bubbleInfo);

        params.wordParamsInfo = info;
        return params;
    }
}
