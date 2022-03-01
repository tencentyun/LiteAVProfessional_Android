package com.tencent.liteav.login.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.squareup.picasso.Picasso;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.common.UserModel;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.ProfileManager;


public class ProfileActivity extends BaseActivity {
    private static final String TAG = "ProfileActivity";

    private ImageView mImageAvatar;
    private EditText  mEditUserName;
    private Button    mButtonRegister;
    private UserModel mUserModel;

    public static void start(Context context) {
        Intent starter = new Intent(context, ProfileActivity.class);
        context.startActivity(starter);
    }

    private void startMainActivity() {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("com.tencent.liteav.action.liteavapp");
        IntentUtils.safeStartActivity(ProfileActivity.this, intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserModel = UserModelManager.getInstance().getUserModel();
        if (mUserModel == null) {
            finish();
        }
        setContentView(R.layout.login_activity_profile);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mButtonRegister.setEnabled(mEditUserName.length() != 0);
    }

    private void initView() {
        mImageAvatar = (ImageView) findViewById(R.id.iv_user_avatar);
        mEditUserName = (EditText) findViewById(R.id.et_user_name);
        mButtonRegister = (Button) findViewById(R.id.tv_register);

        Picasso.get().load(mUserModel.userAvatar).into(mImageAvatar);
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfile();
            }
        });

        mEditUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                mButtonRegister.setEnabled(text.length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setProfile() {
        String userName = mEditUserName.getText().toString().trim();
        if (TextUtils.isEmpty(userName) || mUserModel.userId == null) {
            ToastUtils.showLong(getString(R.string.login_toast_set_username));
            return;
        }
        String reg = "^[a-z0-9A-Z\\u4e00-\\u9fa5\\_]{2,20}$";
        if (!userName.matches(reg)) {
            ToastUtils.showLong(getString(R.string.login_tips_user_name));
            return;
        }
        ProfileManager.getInstance().setUserName(mUserModel.userId, userName, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.showLong(getString(R.string.login_toast_register_success_and_logging_in));
                startMainActivity();
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtils.showLong(getString(R.string.login_toast_failed_to_set_username, msg));
            }
        });
    }

}
