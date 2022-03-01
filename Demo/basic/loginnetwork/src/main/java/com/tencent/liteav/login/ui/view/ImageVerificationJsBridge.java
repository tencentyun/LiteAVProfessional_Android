package com.tencent.liteav.login.ui.view;

import android.webkit.JavascriptInterface;


public class ImageVerificationJsBridge {
    CallBack mCallBack;

    public ImageVerificationJsBridge(CallBack callBack) {
        this.mCallBack = callBack;
    }

    @JavascriptInterface
    public void verifySuccess(String ticket, String randStr) {
        if (mCallBack != null) {
            mCallBack.onSuccess(ticket, randStr);
        }
    }

    @JavascriptInterface
    public void verifyError(int errorCode, String errorMsg) {
        if (mCallBack != null) {
            mCallBack.onError(errorCode, errorMsg);
        }
    }

    public interface CallBack {
        void onSuccess(String ticket, String randStr);

        void onError(int errorCode, String errorMsg);
    }
}