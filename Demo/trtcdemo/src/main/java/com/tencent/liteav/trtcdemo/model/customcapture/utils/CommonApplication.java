package com.tencent.liteav.trtcdemo.model.customcapture.utils;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.Method;

public class CommonApplication {
    @SuppressLint("StaticFieldLeak")
    private static Application application;

    public static Application get() {
        if (application == null) {
            synchronized (CommonApplication.class) {
                if (application == null) {
                    new CommonApplication();
                }
            }
        }
        return application;
    }

    private CommonApplication() {
        Object activityThread;
        try {
            Class acThreadClass = Class.forName("android.app.ActivityThread");
            if (acThreadClass == null)
                return;
            Method acThreadMethord = acThreadClass.getMethod("currentActivityThread");
            if (acThreadMethord == null)
                return;
            acThreadMethord.setAccessible(true);
            activityThread = acThreadMethord.invoke(null);
            Method applicationMethord = activityThread.getClass().getMethod("getApplication");
            if (applicationMethord == null)
                return;
            Object app = applicationMethord.invoke(activityThread);
            application = (Application) app;

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
