package com.tencent.liteav.demo.videoediter.bubble.ui.popwin;


import com.tencent.liteav.demo.videoediter.bubble.utils.TCBubbleInfo;

/**
 * Created by hans on 2017/10/24.
 *
 * 保存 从{@link TCBubbleSettingView} 之后的设定的 气泡字幕index、以及字体颜色的数据结构
 */
public class TCWordParamsInfo  {
    private int bubblePos;
    private int textColor;
    private TCBubbleInfo bubbleInfo;


    public int getBubblePos() {
        return bubblePos;
    }

    public void setBubblePos(int bubblePos) {
        this.bubblePos = bubblePos;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public TCBubbleInfo getBubbleInfo() {
        return bubbleInfo;
    }

    public void setBubbleInfo(TCBubbleInfo bubbleInfo) {
        this.bubbleInfo = bubbleInfo;
    }


}
