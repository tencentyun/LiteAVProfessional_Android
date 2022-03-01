package com.tencent.liteav.demo.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.UserModel;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.login.model.ProfileManager;

import java.util.Arrays;
import java.util.List;

public class ModifyUserAvatarDialog extends BottomSheetDialog {
    private UserModel             mUserModel;
    private Context               mContext;
    private ModifySuccessListener mListener;
    private RecyclerView          mRvAvatar;
    private String                mSelectAvatarUrl;

    /**
     * Modify the user avatar dialog.
     *
     * @param context
     * @param listener
     */
    public ModifyUserAvatarDialog(Context context, ModifySuccessListener listener) {
        super(context, R.style.AppBottomDialog);
        mUserModel = UserModelManager.getInstance().getUserModel();
        if (mUserModel == null) {
            dismiss();
            return;
        }
        mContext = context;
        mListener = listener;
        setContentView(R.layout.app_view_modify_user_avatar_dialog);
        mRvAvatar = findViewById(R.id.rv_avatar);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        mRvAvatar.setLayoutManager(gridLayoutManager);
        String[] avatarArr = AvatarConstant.USER_AVATAR_ARRAY;
        List<String> avatarList = Arrays.asList(avatarArr);
        AvatarListAdapter adapter =
                new AvatarListAdapter(context, avatarList, new AvatarListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(String avatarUrl) {
                        mSelectAvatarUrl = avatarUrl;
                    }
                });
        mRvAvatar.setAdapter(adapter);
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfile(mSelectAvatarUrl);
            }
        });
    }

    private void setProfile(final String avatarUrl) {
        if (TextUtils.isEmpty(avatarUrl) || mUserModel.userId == null) {
            return;
        }
        ProfileManager.getInstance().setAvatar(avatarUrl, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.showLong(mContext.getString(R.string.app_toast_success_to_set_username));
                mListener.onSuccess();
                dismiss();
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtils.showLong(mContext.getString(R.string.app_toast_failed_to_set_username, msg));
            }
        });
    }

    public interface ModifySuccessListener {
        void onSuccess();
    }
}