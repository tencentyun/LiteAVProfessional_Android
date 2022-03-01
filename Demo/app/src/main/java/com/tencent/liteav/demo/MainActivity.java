package com.tencent.liteav.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.common.AppRuntime;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.common.widget.expandableadapter.BaseExpandableRecyclerViewAdapter;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {

    private static final String TAG                      = "MainActivity";
    private static final String TRTC_APP_PACKAGE_NAME    = "com.tencent.trtc";
    private static final String TRTC_APP_MAIN_CLASS_NAME = "com.tencent.liteav.demo.SplashActivity";

    private              TextView              mMainTitle;
    private              TextView              mTvVersion;
    private              RecyclerView          mRvList;
    private              MainExpandableAdapter mAdapter;
    private              ImageView             mUserInfoImg;
    private static final int                   CLICK_TIME     = 5; // 连续点击次数
    private static final long[]                mHits          = new long[CLICK_TIME];
    private static final long                  CLICK_DURATION = 1000;
    private              List<GroupBean>       mGroupList     = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            Log.d(TAG, "brought to front");
            finish();
            return;
        }

        LiteAVSDKService.init(getApplicationContext());
        setContentView(R.layout.activity_main);

        mTvVersion = findViewById(R.id.main_tv_version);
        mTvVersion.setText(getString(R.string.app_tv_video_cloud_tools_version, TXLiveBase.getSDKVersionStr(),
                getVersionName(this)));

        mMainTitle = findViewById(R.id.main_title);
        mMainTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        File logFile = getLogFile();
                        if (logFile != null) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("application/octet-stream");
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
                            IntentUtils.safeStartActivity(MainActivity.this,
                                    Intent.createChooser(intent, getString(R.string.app_title_share_log)));
                        }
                    }
                });
                return false;
            }
        });
        mUserInfoImg = findViewById(R.id.img_user_info);
        mUserInfoImg.setVisibility(View.VISIBLE);
        final Intent intent = new Intent(this, UserInfoActivity.class);
        mUserInfoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

        mRvList = findViewById(R.id.main_recycler_view);
        initGroupData();
        mRvList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainExpandableAdapter(mGroupList);
        mAdapter.setListener(
                new BaseExpandableRecyclerViewAdapter.ExpandableRecyclerViewOnClickListener<GroupBean, ChildBean>() {
                    @Override
                    public boolean onGroupLongClicked(GroupBean groupItem) {
                        return false;
                    }

                    @Override
                    public boolean onInterceptGroupExpandEvent(GroupBean groupItem, boolean isExpand) {
                        return false;
                    }

                    @Override
                    public void onGroupClicked(GroupBean groupItem) {
                        mAdapter.setSelectedChildBean(groupItem);
                    }

                    @Override
                    public void onChildClicked(GroupBean groupItem, ChildBean childItem) {
                        if (childItem.mIconId == R.drawable.xiaoshipin) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("http://dldir1.qq.com/hudongzhibo/liteav/XiaoShiPin.apk"));
                            IntentUtils.safeStartActivity(MainActivity.this, intent);
                            return;
                        } else if (childItem.mIconId == R.drawable.xiaozhibo) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("http://dldir1.qq.com/hudongzhibo/liteav/xiaozhibo.apk"));
                            IntentUtils.safeStartActivity(MainActivity.this, intent);
                            return;
                        } else if (getString(R.string.item_trtc_app).equals(childItem.mName)) {
                            jumpTRTCAPP();
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, childItem.getTargetClass());
                        intent.putExtra("TITLE", childItem.mName);
                        intent.putExtra("TYPE", childItem.mType);
                        IntentUtils.safeStartActivity(MainActivity.this, intent);
                    }
                });
        mRvList.setAdapter(mAdapter);

        mMainTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - mHits[0] < CLICK_DURATION) {
                    Arrays.fill(mHits, 0);
                    AppRuntime appRuntime = AppRuntime.get();
                    appRuntime.setDebug(!appRuntime.isDebug());
                    String tip = appRuntime.isDebug() ? getString(R.string.app_debug_switch_open) :
                            getString(R.string.app_debug_switch_close);
                    Toast.makeText(MainActivity.this, tip, Toast.LENGTH_SHORT).show();
                    initGroupData();
                    mAdapter.notifyDataSetChanged();
                    enableSdkLog(appRuntime.isDebug());
                }
            }
        });

        enableSdkLog(AppRuntime.get().isDebug());
    }

    private void enableSdkLog(boolean enable) {
        TXLiveBase.setLogLevel(enable ? TXLiveConstants.LOG_LEVEL_INFO : TXLiveConstants.LOG_LEVEL_NULL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 自动升级逻辑的控制
        if (BuildConfig.BUILD_FROM_DEVOPS) {
            try {
                Class<?> clazz = getClassLoader().loadClass("com.tencent.liteav.demo.upgrade.UpgradeSettingService");
                Method method = clazz.getMethod("init", Context.class);
                method.invoke(null, this);
            } catch (Exception e) {
                Log.e(TAG, "execute UpgradeSettingService init method failed.", e);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //退出登录
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.common_alert_dialog)
                .setMessage(getString(R.string.app_dialog_exit_app))
                .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private void initGroupData() {
        mGroupList.clear();

        if (BuildConfig.BUILD_INTERNATIONAL) {
            // 直播
            try {
                StatesArrayList pusherChildList = new StatesArrayList();
                pusherChildList.addBean(
                        new ChildBean(getString(R.string.app_item_link_mic_new), R.drawable.room_live, 0,
                                Class.forName("com.tencent.liteav.demo.livelinkmicnew.V2MainActivity"), false));

                if (pusherChildList.size() != 0) {
                    GroupBean pusherGroupBean = new GroupBean(getString(R.string.app_item_mlvb), -1, pusherChildList);
                    mGroupList.add(pusherGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.BUILD_LIVE) {
            try {
                // 直播推流
                StatesArrayList pusherChildList = new StatesArrayList();
                pusherChildList.addBean(new ChildBean(getString(R.string.app_item_live_pusher), R.drawable.push, 0,
                        Class.forName("com.tencent.liteav.demo.livepusher.camerapush.ui.CameraPushEntranceActivity"),
                        false));
                pusherChildList.addBean(
                        new ChildBean(getString(R.string.app_item_live_pusher_screen), R.drawable.push, 0,
                                Class.forName("com.tencent.liteav.demo.livepusher.screenpush"
                                        + ".ScreenPushEntranceActivity"), false));
                pusherChildList.addBean(
                        new ChildBean(getString(R.string.app_item_link_mic_new), R.drawable.room_live, 0,
                                Class.forName("com.tencent.liteav.demo.livelinkmicnew.V2MainActivity"), true));

                if (pusherChildList.size() != 0) {
                    GroupBean pusherGroupBean = new GroupBean(getString(R.string.app_item_mlvb), -1, pusherChildList);
                    mGroupList.add(pusherGroupBean);
                }

                // 直播播放
                StatesArrayList playChildList = new StatesArrayList();
                playChildList.addBean(new ChildBean(getString(R.string.app_item_live_player), R.drawable.live, 0,
                        Class.forName("com.tencent.liteav.demo.liveplayer.ui.LivePlayerEntranceActivity"), false));
                playChildList.addBean(new ChildBean(getString(R.string.app_item_leb_player), R.drawable.live, 0,
                        Class.forName("com.tencent.liteav.demo.lebplayer.ui.LebPlayerLauncherActivity"), false));
                playChildList.addBean(new ChildBean(getString(R.string.app_rtc_player), R.drawable.live, 0,
                        Class.forName("com.tencent.liteav.demo.liveplayer.ui.RTCPlayEntranceActivity"), true));

                if (playChildList.size() != 0) {
                    GroupBean pusherGroupBean =
                            new GroupBean(getString(R.string.app_item_live_play), -1, playChildList);
                    mGroupList.add(pusherGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.BUILD_SMART) {
            try {
                // 直播推流
                StatesArrayList pusherChildList = new StatesArrayList();
                pusherChildList.addBean(new ChildBean(getString(R.string.app_item_live_pusher), R.drawable.push, 0,
                        Class.forName("com.tencent.liteav.demo.livepusher.camerapush.ui.CameraPushEntranceActivity"),
                        false));
                pusherChildList.addBean(
                        new ChildBean(getString(R.string.app_item_live_pusher_screen), R.drawable.push, 0,
                                Class.forName("com.tencent.liteav.demo.livepusher.screenpush"
                                        + ".ScreenPushEntranceActivity"), false));

                if (pusherChildList.size() != 0) {
                    GroupBean pusherGroupBean = new GroupBean(getString(R.string.app_item_mlvb), -1, pusherChildList);
                    mGroupList.add(pusherGroupBean);
                }

                // 直播播放
                StatesArrayList playChildList = new StatesArrayList();
                playChildList.addBean(new ChildBean(getString(R.string.app_item_live_player), R.drawable.live, 0,
                        Class.forName("com.tencent.liteav.demo.liveplayer.ui.LivePlayerEntranceActivity"), false));

                if (playChildList.size() != 0) {
                    GroupBean pusherGroupBean =
                            new GroupBean(getString(R.string.app_item_live_play), -1, playChildList);
                    mGroupList.add(pusherGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.BUILD_TRTC) {
            // 实时互动
            try {
                StatesArrayList trtcLinkChildList = new StatesArrayList();
                trtcLinkChildList.addBean(
                        new ChildBean(getString(R.string.item_trtc_speed_test), R.drawable.room_multi, 1,
                                Class.forName("com.tencent.liteav.trtcdemo.ui" + ".TRTCSpeedTestActivity"), false));
                trtcLinkChildList.addBean(new ChildBean(getString(R.string.item_trtc_live), R.drawable.room_multi, 1,
                        Class.forName("com.tencent.liteav.trtcdemo.ui.TRTCLiveEnterActivity"), false));
                trtcLinkChildList.addBean(new ChildBean(getString(R.string.item_trtc_call), R.drawable.room_multi, 1,
                        Class.forName("com.tencent.liteav.trtcdemo.ui.TRTCCallEnterActivity"), false));

                if (trtcLinkChildList.size() != 0) {
                    GroupBean pusherGroupBean =
                            new GroupBean(getString(R.string.app_item_trtc_link), -1, trtcLinkChildList);
                    mGroupList.add(pusherGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.BUILD_UGC) {
            // 短视频
            try {
                StatesArrayList shortVideoChildList = new StatesArrayList();
                shortVideoChildList.addBean(
                        new ChildBean(getString(R.string.app_item_video_recording), R.drawable.video, 0,
                                Class.forName("com.tencent.liteav.demo.videorecord" + ".TCVideoSettingActivity"),
                                false));
                shortVideoChildList.addBean(
                        new ChildBean(getString(R.string.app_item_effects_editor), R.drawable.cut, 0,
                                Class.forName("com.tencent.liteav.demo.videoediter.TCVideoPickerActivity"), false));
                Class<?> classTCVideoJoinChooseActivity =
                        Class.forName("com.tencent.liteav.demo.videojoiner.ui" + ".TCVideoJoinChooseActivity");
                shortVideoChildList.addBean(
                        new ChildBean(getString(R.string.app_item_video_stitching), R.drawable.composite,
                                classTCVideoJoinChooseActivity.getField("TYPE_MULTI_CHOOSE").getInt(null),
                                classTCVideoJoinChooseActivity, false));
                shortVideoChildList.addBean(
                        new ChildBean(getString(R.string.app_item_picture_transition), R.drawable.short_video_picture,
                                classTCVideoJoinChooseActivity.getField("TYPE_MULTI_CHOOSE_PICTURE").getInt(null),
                                classTCVideoJoinChooseActivity, false));
                shortVideoChildList.addBean(new ChildBean(getString(R.string.app_item_video_upload), R.drawable.update,
                        classTCVideoJoinChooseActivity.getField("TYPE_PUBLISH_CHOOSE").getInt(null),
                        classTCVideoJoinChooseActivity, false));

                if (shortVideoChildList.size() != 0) {
                    GroupBean shortVideoGroupBean =
                            new GroupBean(getString(R.string.app_item_ugsv), -1, shortVideoChildList);
                    mGroupList.add(shortVideoGroupBean);
                }
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.BUILD_PLAYER) {
            // 播放器
            try {
                StatesArrayList playerChildList = new StatesArrayList();
                playerChildList.addBean(new ChildBean(getString(R.string.app_item_super_player), R.drawable.play, 3,
                        Class.forName("com.tencent.liteav.demo.player.demo.SuperPlayerActivity"), false));
                playerChildList.addBean(new ChildBean(getString(R.string.app_vod_player), R.drawable.play, 3,
                        Class.forName("com.tencent.liteav.demo.player.demo.VodPlayerActivity"), true));
                playerChildList.addBean(
                        new ChildBean(getString(R.string.app_item_shortvideo_player), R.drawable.play, 3,
                                Class.forName("com.tencent.liteav.demo.player.demo.shortvideo.view.ShortVideoActivity"),
                                false));
                playerChildList.addBean(new ChildBean(getString(R.string.app_feed_player), R.drawable.play, 3,
                        Class.forName("com.tencent.liteav.demo.player.demo.FeedActivity"), false));
                if (playerChildList.size() != 0) {
                    GroupBean playerGroupBean = new GroupBean(getString(R.string.app_item_player), -1, playerChildList);
                    mGroupList.add(playerGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (!BuildConfig.DEBUG) {
            // 场景应用
            StatesArrayList sceneChildList = new StatesArrayList();
            sceneChildList.addBean(
                    new ChildBean(getString(R.string.app_item_xiao_zhi_bo), R.drawable.xiaozhibo, 0, null, false));
            sceneChildList.addBean(
                    new ChildBean(getString(R.string.app_item_xiao_shi_pin), R.drawable.xiaoshipin, 0, null, false));
            sceneChildList.addBean(new ChildBean(getString(R.string.item_trtc_app), 0, 0, null, false));

            if (sceneChildList.size() != 0) {
                GroupBean videoConnectGroupBean = new GroupBean(getString(R.string.app_item_scene), -1, sceneChildList);
                mGroupList.add(videoConnectGroupBean);
            }
        }

        if (BuildConfig.BUILD_LIVE) {
            // 其他工具
            try {
                StatesArrayList otherChildList = new StatesArrayList();
                otherChildList.addBean(new ChildBean(getString(R.string.item_live_pusher_v1), R.drawable.push, 0,
                        Class.forName("com.tencent.liteav.v1livepusherdemo.V1CameraPushEntranceActivity"), true));
                otherChildList.addBean(new ChildBean(getString(R.string.item_live_pusher_video_v1), R.drawable.push, 0,
                        Class.forName("com.tencent.liteav.v1livepusherdemo.V1VideoPusherEntranceActivity"), true));
                otherChildList.addBean(new ChildBean(getString(R.string.item_live_pusher_screen_v1), R.drawable.push, 0,
                        Class.forName("com.tencent.liteav.v1livepusherdemo.V1ScreenPushEntranceActivity"), true));
                otherChildList.addBean(new ChildBean(getString(R.string.item_live_player_v1), R.drawable.room_live, 0,
                        Class.forName("com.tencent.liteav.v1liveplayerdemo.V1LivePlayerEntranceActivity"), true));
                otherChildList.addBean(new ChildBean(getString(R.string.item_link_mic_v1), R.drawable.room_live, 0,
                        Class.forName("com.tencent.liteav.demo.liveroom.ui.LiveRoomActivity"), true));
                if (!otherChildList.isEmpty()) {
                    GroupBean otherGroupBean =
                            new GroupBean(getString(R.string.app_item_other_tools), -1, otherChildList);
                    mGroupList.add(otherGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.BUILD_REFACTORING_LIVE) {
            //TODO:临时增加重构live版本
            try {
                StatesArrayList refactoringLiveChildList = new StatesArrayList();
                refactoringLiveChildList.addBean(
                        new ChildBean(getString(R.string.app_item_live_player), R.drawable.room_multi, 0,
                                Class.forName("com.tencent.liteav.demo.liveplayer.ui" + ".LivePlayerEntranceActivity"),
                                false));
                refactoringLiveChildList.addBean(
                        new ChildBean(getString(R.string.item_live_player_v1), R.drawable.room_live, 0,
                                Class.forName("com.tencent.liteav.v1liveplayerdemo.V1LivePlayerEntranceActivity"),
                                false));
                refactoringLiveChildList.addBean(
                        new ChildBean(getString(R.string.app_item_leb_player), R.drawable.live, 0,
                        Class.forName("com.tencent.liteav.demo.lebplayer.ui.LebPlayerLauncherActivity"),
                        false));
                if (refactoringLiveChildList.size() != 0) {
                    GroupBean pusherGroupBean =
                            new GroupBean(getString(R.string.app_item_refactoring_live), -1, refactoringLiveChildList);
                    mGroupList.add(pusherGroupBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    private static class MainExpandableAdapter
            extends BaseExpandableRecyclerViewAdapter<GroupBean, ChildBean, GroupVH, ChildVH> {
        private List<GroupBean> mListGroupBean;
        private GroupBean       mGroupBean;

        public void setSelectedChildBean(GroupBean groupBean) {
            boolean isExpand = isExpand(groupBean);
            if (mGroupBean != null) {
                GroupVH lastVH = getGroupViewHolder(mGroupBean);
                if (!isExpand) {
                    mGroupBean = groupBean;
                } else {
                    mGroupBean = null;
                }
                notifyItemChanged(lastVH.getAdapterPosition());
            } else {
                if (!isExpand) {
                    mGroupBean = groupBean;
                } else {
                    mGroupBean = null;
                }
            }
            if (mGroupBean != null) {
                GroupVH currentVH = getGroupViewHolder(mGroupBean);
                notifyItemChanged(currentVH.getAdapterPosition());
            }
        }

        public MainExpandableAdapter(List<GroupBean> list) {
            mListGroupBean = list;
        }

        @Override
        public int getGroupCount() {
            return mListGroupBean.size();
        }

        @Override
        public GroupBean getGroupItem(int groupIndex) {
            return mListGroupBean.get(groupIndex);
        }

        @Override
        public GroupVH onCreateGroupViewHolder(ViewGroup parent, int groupViewType) {
            return new GroupVH(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.module_entry_item, parent, false));
        }

        @Override
        public void onBindGroupViewHolder(GroupVH holder, GroupBean groupBean, boolean isExpand) {
            holder.textView.setText(groupBean.mName);
            if (groupBean.mIconId > 0) {
                holder.ivLogo.setVisibility(View.VISIBLE);
                holder.ivLogo.setImageResource(groupBean.mIconId);
            } else {
                holder.ivLogo.setVisibility(View.GONE);
            }
            if (mGroupBean == groupBean) {
                holder.itemView.setBackgroundResource(R.color.main_item_selected_color);
            } else {
                holder.itemView.setBackgroundResource(R.color.main_item_unselected_color);
            }
        }

        @Override
        public ChildVH onCreateChildViewHolder(ViewGroup parent, int childViewType) {
            return new ChildVH(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.module_entry_child_item, parent, false));
        }

        @Override
        public void onBindChildViewHolder(ChildVH holder, GroupBean groupBean, ChildBean childBean) {
            holder.textView.setText(childBean.getName());
            if (groupBean.mChildList.indexOf(childBean) == groupBean.mChildList.size() - 1) { //说明是最后一个
                holder.divideView.setVisibility(View.GONE);
            } else {
                holder.divideView.setVisibility(View.VISIBLE);
            }
            if (childBean.mDebug && !AppRuntime.get().isDebug()) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }
        }
    }


    public static class GroupVH extends BaseExpandableRecyclerViewAdapter.BaseGroupViewHolder {
        ImageView ivLogo;
        TextView  textView;

        GroupVH(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.name_tv);
            ivLogo = (ImageView) itemView.findViewById(R.id.icon_iv);
        }

        @Override
        protected void onExpandStatusChanged(RecyclerView.Adapter relatedAdapter, boolean isExpanding) {
        }

    }


    public static class ChildVH extends RecyclerView.ViewHolder {
        TextView textView;
        View     divideView;

        ChildVH(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.name_tv);
            divideView = itemView.findViewById(R.id.item_view_divide);
        }

    }


    private class GroupBean implements BaseExpandableRecyclerViewAdapter.BaseGroupBean<ChildBean> {
        private String          mName;
        private StatesArrayList mChildList;
        private int             mIconId;

        public GroupBean(String name, int iconId, StatesArrayList list) {
            mName = name;
            mChildList = list;
            mIconId = iconId;
        }

        @Override
        public int getChildCount() {
            return mChildList.size();
        }

        @Override
        public ChildBean getChildAt(int index) {
            return mChildList.size() <= index ? null : (ChildBean) mChildList.get(index);
        }

        @Override
        public boolean isExpandable() {
            return getChildCount() > 0;
        }

        public String getName() {
            return mName;
        }

        public List<ChildBean> getChildList() {
            return mChildList;
        }

        public int getIconId() {
            return mIconId;
        }
    }


    private class ChildBean {
        public String  mName;
        public int     mIconId;
        public Class   mTargetClass;
        public int     mType;
        public boolean mDebug;

        public ChildBean(String name, int iconId, int type, Class targetActivityClass, boolean debug) {
            this.mName = name;
            this.mIconId = iconId;
            this.mTargetClass = targetActivityClass;
            this.mType = type;
            this.mDebug = debug;
        }

        public String getName() {
            return mName;
        }


        public int getIconId() {
            return mIconId;
        }


        public Class getTargetClass() {
            return mTargetClass;
        }
    }


    private File getLogFile() {
        File sdcardDir = getExternalFilesDir(null);
        if (sdcardDir == null) {
            return null;
        }

        String path = sdcardDir.getAbsolutePath() + "/log/liteav";
        List<String> logs = new ArrayList<>();
        File directory = new File(path);
        if (directory != null && directory.exists() && directory.isDirectory()) {
            long lastModify = 0;
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().endsWith("xlog")) {
                        logs.add(file.getAbsolutePath());
                    }
                }
            }
        }

        String zipPath = path + "/liteavLog.zip";
        return zip(logs, zipPath);
    }

    private File zip(List<String> files, String zipFileName) {
        File zipFile = new File(zipFileName);
        zipFile.deleteOnExit();
        InputStream is = null;
        ZipOutputStream zos = null;

        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zos.setComment("LiteAV log");
            for (String path : files) {
                File file = new File(path);
                try {
                    if (file.length() == 0 || file.length() > 8 * 1024 * 1024) {
                        continue;
                    }

                    is = new FileInputStream(file);
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    byte[] buffer = new byte[8 * 1024];
                    int length = 0;
                    while ((length = is.read(buffer)) != -1) {
                        zos.write(buffer, 0, length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "zip log error");
            zipFile = null;
        } finally {
            try {
                zos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return zipFile;
    }


    private void jumpDownloadPage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://dldir1.qq.com/hudongzhibo/liteav/TRTCDemo.apk"));
        IntentUtils.safeStartActivity(this, intent);
    }

    private void jumpTRTCAPP() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName componentName = new ComponentName(TRTC_APP_PACKAGE_NAME, TRTC_APP_MAIN_CLASS_NAME);
        intent.setComponent(componentName);

        if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            jumpDownloadPage();
            return;
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("TAG", "ActivityNotFoundException : " + intent.toString());
        }
    }

    public class StatesArrayList extends ArrayList {

        public boolean addBean(ChildBean bean) {
            if (bean.mDebug && !AppRuntime.get().isDebug()) {
                return true;
            } else {
                return add(bean);
            }
        }

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
