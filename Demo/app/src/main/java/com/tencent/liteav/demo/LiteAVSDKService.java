package com.tencent.liteav.demo;

import android.content.Context;

import com.tencent.rtmp.TXLiveBase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LiteAVSDKService {
    private static final String LICENCE_URL =
            "https://liteav.sdk.qcloud.com/app/res/licence/liteav/android/" + "RDM_Enterprise.license";
    private static final String LICENCE_KEY = "9bc74ac7bfd07ea392e8fdff2ba5678a";

    /**
     * 初始化腾讯云相关sdk。
     * SDK 初始化过程中可能会读取手机型号等敏感信息，需要在用户同意隐私政策后，才能获取。
     */
    public static void init(Context appContext) {
        TXLiveBase.getInstance().setLicence(appContext, LICENCE_URL, LICENCE_KEY);
        //TODO:重构分支暂无该接口
        //TXLiveBase.setListener(new TXLiveBaseListener() {
        //    @Override
        //    public void onUpdateNetworkTime(int errCode, String errMsg) {
        //        if (errCode != 0) {
        //            TXLiveBase.updateNetworkTime();
        //        }
        //    }
        //});
        TXLiveBase.updateNetworkTime();

        // 短视频licence设置
        initUGCKit(appContext, LICENCE_URL, LICENCE_KEY);
    }

    private static void initUGCKit(Context appContext, String licenceUrl, String licenseKey) {
        try {
            Class<?> ugcBaseClass = Class.forName("com.tencent.ugc.TXUGCBase");
            Method getInstanceMethod = ugcBaseClass.getMethod("getInstance");
            Object ugcBaseObject = getInstanceMethod.invoke(null);
            Method setLicenceMethod = ugcBaseClass.getMethod("setLicence", Context.class, String.class, String.class);
            setLicenceMethod.invoke(ugcBaseObject, appContext, licenceUrl, licenseKey);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            Class<?> ugcKitClass = Class.forName("com.tencent.qcloud.ugckit.UGCKit");
            Method initMethod = ugcKitClass.getMethod("init", Context.class);
            initMethod.invoke(null, appContext);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
