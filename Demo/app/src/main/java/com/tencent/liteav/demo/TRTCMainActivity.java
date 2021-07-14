package com.tencent.liteav.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.bugly.beta.Beta;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.LoginActivity;
import com.tencent.liteav.trtcdemo.ui.TRTCCallEnterActivity;
import com.tencent.liteav.trtcdemo.ui.TRTCLiveEnterActivity;
import com.tencent.liteav.trtcdemo.ui.TRTCSpeedTestActivity;
import com.tencent.rtmp.TXLiveBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TRTCMainActivity extends Activity {

    private static final String TAG = TRTCMainActivity.class.getName();

    private TextView                mMainTitle;
    private TextView                mTvVersion;
    private List<TRTCItemEntity>    mTRTCItemEntityList;
    private RecyclerView            mRvList;
    private TRTCRecyclerViewAdapter mTRTCRecyclerViewAdapter;
    private ImageView               mLogoutImg;
    private AlertDialog             mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            Log.d(TAG, "brought to front");
            finish();
            return;
        }
        setContentView(R.layout.activity_trtc_main);
        mTvVersion = (TextView) findViewById(R.id.main_tv_version);
        mTvVersion.setText(getString(R.string.app_tv_trtc_version, TXLiveBase.getSDKVersionStr()));
        mMainTitle = (TextView) findViewById(R.id.main_title);
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
                            //intent.setPackage("com.tencent.mobileqq");
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
                            startActivity(Intent.createChooser(intent, getString(R.string.app_title_share_log)));
                        }
                    }
                });
                return false;
            }
        });
        mRvList = (RecyclerView) findViewById(R.id.main_recycler_view);
        mTRTCItemEntityList = createTRTCItems();
        mTRTCRecyclerViewAdapter = new TRTCRecyclerViewAdapter(this, mTRTCItemEntityList, new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                TRTCItemEntity entity = mTRTCItemEntityList.get(position);
                Intent         intent = new Intent(TRTCMainActivity.this, entity.mTargetClass);
                intent.putExtra("TITLE", entity.mTitle);
                intent.putExtra("TYPE", entity.mType);
                TRTCMainActivity.this.startActivity(intent);
            }
        });
        mRvList.setLayoutManager(new GridLayoutManager(this, 2));
        mRvList.setAdapter(mTRTCRecyclerViewAdapter);
        mLogoutImg = (ImageView) findViewById(R.id.img_logout);
        mLogoutImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
        interceptHyperLink((TextView) findViewById(R.id.tv_privacy));
        initPermission();
        Beta.checkUpgrade();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showLogoutDialog() {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this, R.style.common_alert_dialog)
                    .setMessage(getString(R.string.app_dialog_log_out))
                    .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 执行退出登录操作
                            ProfileManager.getInstance().logout(new ProfileManager.ActionCallback() {
                                @Override
                                public void onSuccess() {
                                    // 退出登录
                                    startLoginActivity();
                                }

                                @Override
                                public void onFailed(int code, String msg) {
                                }
                            });
                        }
                    })
                    .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private List<TRTCItemEntity> createTRTCItems() {
        List<TRTCItemEntity> list = new ArrayList<>();

        list.add(new TRTCItemEntity(getString(R.string.item_trtc_speed_test),"", R.drawable.room_multi, 1, TRTCSpeedTestActivity.class));
        list.add(new TRTCItemEntity(getString(R.string.item_trtc_live),"", R.drawable.room_multi, 1, TRTCLiveEnterActivity.class));
        list.add(new TRTCItemEntity(getString(R.string.item_trtc_call),"", R.drawable.room_multi, 1, TRTCCallEnterActivity.class));
        list.add(new TRTCItemEntity(getString(R.string.item_trtc_app),"", R.drawable.trtcdemo_fill_adjust, 0, null));

        return list;
    }

    private File getLogFile() {
        String       path      = getExternalFilesDir(null).getAbsolutePath() + "/log/liteav";
        List<String> logs      = new ArrayList<>();
        File         directory = new File(path);
        if (directory != null && directory.exists() && directory.isDirectory()) {
            long lastModify = 0;
            File files[]    = directory.listFiles();
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

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.MICROPHONE, PermissionConstants.CAMERA)
                    .request();
        }
    }

    private File zip(List<String> files, String zipFileName) {
        File zipFile = new File(zipFileName);
        zipFile.deleteOnExit();
        InputStream     is  = null;
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zos.setComment("LiteAV log");
            for (String path : files) {
                File file = new File(path);
                try {
                    if (file.length() == 0 || file.length() > 8 * 1024 * 1024) continue;
                    is = new FileInputStream(file);
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    byte[] buffer = new byte[8 * 1024];
                    int    length = 0;
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

    /**
     * 用于跳转隐私协议
     *
     * @param tv
     */
    private void interceptHyperLink(TextView tv) {
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = tv.getText();
        if (text instanceof Spannable) {
            int       end       = text.length();
            Spannable spannable = (Spannable) tv.getText();
            URLSpan[] urlSpans  = spannable.getSpans(0, end, URLSpan.class);
            if (urlSpans.length == 0) {
                return;
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            // 循环遍历并拦截 所有http://开头的链接
            for (URLSpan uri : urlSpans) {
                String url = uri.getURL();
                if (url.indexOf("https://") == 0 || url.indexOf("http://") == 0) {
                    CustomUrlSpan customUrlSpan = new CustomUrlSpan(this, url);
                    spannableStringBuilder.setSpan(customUrlSpan, spannable.getSpanStart(uri),
                            spannable.getSpanEnd(uri), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            tv.setText(spannableStringBuilder);
        }
    }

    public class CustomUrlSpan extends ClickableSpan {

        private Context context;
        private String  url;

        public CustomUrlSpan(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        @Override
        public void onClick(View widget) {
            // 在这里可以做任何自己想要的处理
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }

    public class TRTCItemEntity {
        public String mTitle;
        public String mContent;
        public int    mIconId;
        public Class  mTargetClass;
        public int    mType;

        public TRTCItemEntity(String title, String content, int iconId, int type, Class targetClass) {
            mTitle = title;
            mContent = content;
            mIconId = iconId;
            mTargetClass = targetClass;
            mType = type;
        }
    }

    public class TRTCRecyclerViewAdapter extends
            RecyclerView.Adapter<TRTCRecyclerViewAdapter.ViewHolder> {

        private Context              context;
        private List<TRTCItemEntity> list;
        private OnItemClickListener  onItemClickListener;

        public TRTCRecyclerViewAdapter(Context context, List<TRTCItemEntity> list,
                                       OnItemClickListener onItemClickListener) {
            this.context = context;
            this.list = list;
            this.onItemClickListener = onItemClickListener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView        mItemImg;
            private TextView         mTitleTv;
            private ConstraintLayout mClItem;

            public ViewHolder(View itemView) {
                super(itemView);
                initView(itemView);
            }

            public void bind(final TRTCItemEntity model,
                             final OnItemClickListener listener) {
                mItemImg.setImageResource(model.mIconId);
                mTitleTv.setText(model.mTitle);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(getLayoutPosition());
                    }
                });
            }

            private void initView(final View itemView) {
                mItemImg = (ImageView) itemView.findViewById(R.id.img_item);
                mTitleTv = (TextView) itemView.findViewById(R.id.tv_title);
                mClItem = (ConstraintLayout) itemView.findViewById(R.id.item_cl);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context        context    = parent.getContext();
            LayoutInflater inflater   = LayoutInflater.from(context);
            View           view       = inflater.inflate(R.layout.module_trtc_entry_item, parent, false);
            ViewHolder     viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TRTCItemEntity item = list.get(position);
            holder.bind(item, onItemClickListener);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}