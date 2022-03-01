package com.tencent.liteav.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.rtmp.TXLiveBase;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_about);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView sdkVersion = findViewById(R.id.tv_sdk_version);
        TextView appVersion = findViewById(R.id.tv_app_version);
        sdkVersion.setText(TXLiveBase.getSDKVersionStr());
        appVersion.setText(getVersionName(this));
        TextView tvLogOff = findViewById(R.id.tv_app_logout);
        tvLogOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogOffActivity();
            }
        });
    }

    private void startLogOffActivity() {
        Intent intent = new Intent(this, LogoffActivity.class);
        startActivity(intent);
    }

    public String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }
}
