package com.tencent.liteav.demo.upgrade;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.BuildConfig;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.demo.common.utils.IntentUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class UpgradeSettingService {
    private static final String TAG                    = "UpgradeSettingService";
    private static final String HOST = "https://app-setting-6g3yro2b392da038-1256993030.ap-shanghai"
            + ".app.tcloudbase.com/appSetting";
    private static final String REFACTOR_RPACKAGE_NAME = "com.tencent.liteav.trtc_refactor";
    private static final int    TIMEOUT                = 30 * 1000;

    private static       long            mDownloadId;     //下载的ID
    private static       boolean         mDownloading     = false;
    private static       String          mAppDownloadURL;
    private static       String          mAppName;
    private static       DownloadManager mDownloadManager = null;
    private static final Handler         mUIHandler       = new Handler(Looper.getMainLooper());
    @SuppressLint("StaticFieldLeak")
    private static       UpdateAppDialog mAlertDialog;

    public static void init(Context context) {
        if (context.getPackageName().equals(REFACTOR_RPACKAGE_NAME)) {
            mAppName = "TRTC_Android";
        } else {
            mAppName = "Enterprise_Android";
        }
        Log.d(TAG, "init package name:" + mAppName);

        // 蓝盾构建的地址外网无法访问，所以需要使用cos地址
        mAppDownloadURL = "https://sdk-liteav-1252463788.cos.ap-hongkong.myqcloud.com/app/internal/upgrade/android/"
                + mAppName + "_latest.apk";
        getAppSetting(context);
    }

    private static void getAppSetting(final Context context) {
        OkHttpClient client =
                new OkHttpClient.Builder().readTimeout(TIMEOUT, TimeUnit.MILLISECONDS).writeTimeout(TIMEOUT,
                        TimeUnit.MILLISECONDS).build();

        String appVersion = BuildConfig.VERSION_NAME;
        String userId = UserModelManager.getInstance().getUserModel().userId;
        String url = HOST + "?os=android&appName=" + mAppName + "&appVersion=" + appVersion + "&userId=" + userId;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        Log.i(TAG, body);
                        final AppSetting appSetting = GsonUtils.fromJson(body, AppSetting.class);
                        if (appSetting != null && appSetting.getCode() == 0) {
                            handleUpdate(context, appSetting);
                        }
                    } else {
                        Log.i(TAG, "error");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + e.getMessage(), e);
                }
            }
        });
    }

    private static void handleUpdate(final Context context, final AppSetting appSetting) {
        AppSetting.Data data = appSetting.getData();
        if (data != null && data.getIsNeedUpdate()) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    showUpdateDialog(context, appSetting);
                }
            });
        } else {
            Log.i(TAG, "无需升级");
        }
    }

    private static void showUpdateDialog(final Context context, final AppSetting appSetting) {
        if (mAlertDialog != null) {
            mAlertDialog.hide();
            mAlertDialog = null;
        }

        mAlertDialog = new UpdateAppDialog(context, new UpdateAppDialog.OnUpdateListener() {
            @Override
            public void onUpdate() {
                if (!mDownloading) {
                    AppSetting.Data data = appSetting.getData();
                    downloadApk(context, mAppDownloadURL, mAppName + data.getAppVersion() + ".apk");
                }
            }
        });

        try {
            if (!((Activity) context).isFinishing()) {
                mAlertDialog.show();
            }
            mAlertDialog.update(appSetting, mDownloading);
        } catch (Exception e) {
            Log.e(TAG, "showDialog: " + e.getMessage(), e);
        }
    }

    private static void downloadApk(Context context, String url, String name) {
        mDownloading = true;
        ToastUtils.showLong(context.getString(R.string.setting_download_title));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(context.getString(R.string.setting_download_title));
        request.setDescription(context.getString(R.string.setting_download_description, mAppName));
        request.setAllowedOverRoaming(false);
        //设置文件存放目录
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        request.setDestinationUri(Uri.fromFile(file));

        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        mDownloadId = mDownloadManager.enqueue(request);

        context.registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //广播监听下载的各个状态
    private static final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mDownloadId);
            Cursor cursor = mDownloadManager.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_FAILED) {
                    mDownloading = false;
                    ToastUtils.showLong(context.getString(R.string.setting_download_failed));
                    context.unregisterReceiver(downloadCompleteReceiver);
                    // 下载失败，用户可以继续使用
                    if (mAlertDialog != null) {
                        mAlertDialog.dismiss();
                    }
                } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    mDownloading = false;
                    Log.i(TAG, "下载成功");
                    installAPK(context);
                    context.unregisterReceiver(downloadCompleteReceiver);
                }
                cursor.close();
            }
        }
    };

    private static void installAPK(Context context) {
        Uri uri = mDownloadManager.getUriForDownloadedFile(mDownloadId);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        IntentUtils.safeStartActivity(context, intent);
    }

}
