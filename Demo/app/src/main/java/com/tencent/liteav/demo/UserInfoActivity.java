package com.tencent.liteav.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.common.widget.ModifyUserAvatarDialog;
import com.tencent.liteav.demo.common.widget.ModifyUserNameDialog;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.LoginActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG                                                       = "UserInfoActivity";
    private static final String TENCENT_VIDEO_CLOUD_TOOLKIT_PRIVACY_PROTECTION_GUIDELINES =
            "https://web.sdk.qcloud.com/document/Tencent-Video-Cloud-Toolkit-Privacy-Protection-Guidelines.html";
    private static final String TENCENT_VIDEO_CLOUD_TOOLKIT_USER_AGREEMENT                =
            "https://web.sdk.qcloud.com/document/Tencent-Video-Cloud-Toolkit-User-Agreement.html";

    private CircleImageView mIvAvatar;
    private TextView        mTvNickName;
    private TextView        mTvUserId;
    private Dialog          mDialog;
    private AlertDialog     mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_my_info);
        initView();
    }

    private void initView() {
        mIvAvatar = findViewById(R.id.iv_avatar);
        mTvNickName = findViewById(R.id.tv_show_name);
        mTvUserId = findViewById(R.id.tv_userid);
        String userId = UserModelManager.getInstance().getUserModel().userId;
        String userName = UserModelManager.getInstance().getUserModel().userName;
        String userAvatar = UserModelManager.getInstance().getUserModel().userAvatar;
        Picasso.get().load(userAvatar).into(mIvAvatar);
        mTvNickName.setText(userName);
        mTvUserId.setText(userId);
        findViewById(R.id.img_show_name).setOnClickListener(this);
        findViewById(R.id.iv_avatar).setOnClickListener(this);
        findViewById(R.id.tv_about).setOnClickListener(this);
        findViewById(R.id.tv_privacy_statement).setOnClickListener(this);
        findViewById(R.id.tv_user_agreement).setOnClickListener(this);
        findViewById(R.id.tv_statement).setOnClickListener(this);

        // 导航栏回退/设置
        Toolbar mToolbar = findViewById(R.id.rl_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView mBtnLogout = findViewById(R.id.tv_logout);
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
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
                                    mAlertDialog.dismiss();
                                    startLoginActivity();
                                }

                                @Override
                                public void onFailed(int code, String msg) {
                                }
                            });
                        }
                    }).setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
        }
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showStatementDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.app_show_tip_dialog_confirm, null);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        tvMessage.setText(getString(R.string.app_statement_detail));
        Button tvBtn = (Button) view.findViewById(R.id.btn_negative);
        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        if (mDialog == null) {
            mDialog = new Dialog(this, R.style.AppShowTipTheme);
        }
        mDialog.setContentView(view);
        mDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_show_name:
                ModifyUserNameDialog modifyUserNameDialog =
                        new ModifyUserNameDialog(this, new ModifyUserNameDialog.ModifySuccessListener() {
                            @Override
                            public void onSuccess() {
                                String userName = UserModelManager.getInstance().getUserModel().userName;
                                mTvNickName.setText(userName);
                            }
                        });
                modifyUserNameDialog.show();
                break;
            case R.id.iv_avatar:
                ModifyUserAvatarDialog modifyUserAvatarDialog =
                        new ModifyUserAvatarDialog(this, new ModifyUserAvatarDialog.ModifySuccessListener() {
                            @Override
                            public void onSuccess() {
                                String userAvatar = UserModelManager.getInstance().getUserModel().userAvatar;
                                Picasso.get().load(userAvatar).into(mIvAvatar);
                            }
                        });
                modifyUserAvatarDialog.show();
                break;
            case R.id.tv_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_privacy_statement:
                Intent privacyIntent = new Intent("com.tencent.liteav.action.videocloud.webview");
                privacyIntent.putExtra("title", getString(R.string.app_privacy_statement));
                privacyIntent.putExtra("url", TENCENT_VIDEO_CLOUD_TOOLKIT_PRIVACY_PROTECTION_GUIDELINES);
                IntentUtils.safeStartActivity(UserInfoActivity.this, privacyIntent);
                break;
            case R.id.tv_user_agreement:
                Intent agreementIntent = new Intent("com.tencent.liteav.action.videocloud.webview");
                agreementIntent.putExtra("title", getString(R.string.app_user_agreement));
                agreementIntent.putExtra("url", TENCENT_VIDEO_CLOUD_TOOLKIT_USER_AGREEMENT);
                IntentUtils.safeStartActivity(UserInfoActivity.this, agreementIntent);
                break;
            case R.id.tv_statement:
                showStatementDialog();
                break;
            default :
                Log.e(TAG, "wrong id : " + v.getId());
                break;
        }

    }
}
