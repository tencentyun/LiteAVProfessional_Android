package com.tencent.liteav.trtcdemo.model.manager;

import com.blankj.utilcode.util.ToastUtils;

import java.io.UnsupportedEncodingException;

/**
 * 实现发送网络层音频消息功能
 */
class TRTCAudioPkgListener {

    private static TRTCAudioPkgListener sTRTCAudioPkgListener;

    public static TRTCAudioPkgListener getInstance() {
        if (sTRTCAudioPkgListener == null) {
            sTRTCAudioPkgListener = new TRTCAudioPkgListener();
        }
        return sTRTCAudioPkgListener;
    }

    static {
        System.loadLibrary("CustomerCrypt");
    }

    public long getCustomerAudioPkgListener() {
        return nativeGetCustomerAudioPkgListener();
    }

    public void bindMsgToAudioPkg(String msg) {
        nativeBindMsgToAudioPkg(msg.getBytes());
    }

    public void onReceiveAudioPacketExtraData(String userId, byte[] extraData) {
        String msg = "";
        if (extraData != null && extraData.length > 0) {
            try {
                msg = new String(extraData, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ToastUtils.showLong("收到" + userId + "的网络层消息：" + msg);
        }
    }

    protected native long nativeGetCustomerAudioPkgListener();
    protected native void nativeBindMsgToAudioPkg(byte[] msg);

}
