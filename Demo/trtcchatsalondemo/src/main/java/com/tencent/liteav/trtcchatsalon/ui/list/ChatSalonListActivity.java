package com.tencent.liteav.trtcchatsalon.ui.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.liteav.login.model.RoomManager;
import com.tencent.liteav.trtcchatsalon.R;

/**
 * 用于显示列表页的activity
 */
public class ChatSalonListActivity extends AppCompatActivity {

    private static final String TAG = ChatSalonListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trtcchatsalon_activity_room_list);

        findViewById(R.id.liveroom_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.liveroom_link_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://cloud.tencent.com/document/product/647/35428"));
                startActivity(intent);
            }
        });

        initializeChatSalonRoom();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private void initializeChatSalonRoom() {
        setTitle(R.string.trtcchatsalon_app_name);
        RoomManager.getInstance().initSdkAppId(GenerateTestUserSig.SDKAPPID);
        showFragment();
    }

    private void showFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof ChatSalonListFragment)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            fragment = ChatSalonListFragment.newInstance();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }
    }


}
