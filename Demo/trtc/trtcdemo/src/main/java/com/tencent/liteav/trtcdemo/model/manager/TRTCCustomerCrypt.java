package com.tencent.liteav.trtcdemo.model.manager;


import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 自定义数据加密保护（加解密密码为进房密码）
 */
public class TRTCCustomerCrypt {

    private static TRTCCustomerCrypt sInstance = null;

    public String encryptKey;

    public static TRTCCustomerCrypt sharedInstance() {
        synchronized (TRTCCustomerCrypt.class) {
            if (sInstance == null) {
                sInstance = new TRTCCustomerCrypt();
            }
            return sInstance;
        }
    }

    static {
        System.loadLibrary("CustomerCrypt");	// 2.加载实现了native函数的动态库，只需要写动态库的名字
    }

    public long getEncodedDataProcessingListener() {
        if (TextUtils.isEmpty(encryptKey)) return 0;
        String keyMD5 = md5(encryptKey);
        Log.i("customerCrypt", "key:" + keyMD5);
        return nativeGetEncodedDataProcessingListener(keyMD5);
    }


    private String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result.toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected native long nativeGetEncodedDataProcessingListener(String encryptKey);
}
