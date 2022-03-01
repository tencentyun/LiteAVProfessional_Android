package com.tencent.liteav.login.model;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.UserModel;
import com.tencent.liteav.demo.common.UserModelManager;
import com.tencent.liteav.login.R;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public class ProfileManager {
    private static final ProfileManager mOurInstance             = new ProfileManager();
    public static final  int            ERROR_CODE_UNKNOWN       = -1;
    public static final  int            ERROR_CODE_NEED_REGISTER = -2;
    private static final String         PER_APAASUSER_ID         = "per_apaasuser_id";
    private static final String         PER_DATA                 = "per_profile_manager";
    private static final String         PER_USER_MODEL           = "per_user_model";
    private static final String         PER_USER_PHONE           = "per_user_phone";
    private static final String         PER_TOKEN                = "per_user_token";
    private static final String         PER_USER_DATE            = "per_user_publish_video_date";
    private static final String         TAG                      = "ProfileManager";
    private static final String         PER_FIRSTOPEN            = "per_user_first_open";
    private static final int            MSG_KEEP_ALIVE           = 1001;
    private static final int            INTERVAL_TIME            = 10000; //10s

    private String                         mUserPhone;
    private String                         mUserPubishVideoDate;
    private String                         mToken;
    private String                         mSessionId;
    private String                         mWebAppId;
    private String                         mApaasUserId;
    private Retrofit                       mRetrofit;
    private Api                            mApi;
    private Call<ResponseEntity<SmsModel>> mSmsCall;
    private Call<ResponseEntity<UserInfo>> mLoginCall;
    private Call<ResponseEntityEmpty>      mLoginOffCall;
    private Call<ResponseEntityEmpty>      mSetNameCall;
    private boolean                        isLogin = false;
    private Handler                        mBackHandler;

    public static ProfileManager getInstance() {
        return mOurInstance;
    }

    private ProfileManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new HttpLogInterceptor());
        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://demos.trtc.tencent-cloud.com/prod/")
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApi = mRetrofit.create(Api.class);
    }

    public void setApaasUserId(String apaasUserId) {
        mApaasUserId = apaasUserId;
        SPUtils.getInstance(PER_DATA).put(PER_APAASUSER_ID, mApaasUserId);
    }

    public String getApaasUserId() {
        if (mApaasUserId == null) {
            mApaasUserId = SPUtils.getInstance(PER_DATA).getString(PER_APAASUSER_ID, "");
        }
        return mApaasUserId;
    }


    public void getGlobalData(final ProfileManager.VerifyIdCallback callback) {
        Call<ResponseEntity<GlobalSchedule>> scheduleCall = mApi.getGlobalSchedule();
        scheduleCall.enqueue(new Callback<ResponseEntity<GlobalSchedule>>() {
            @Override
            public void onResponse(Call<ResponseEntity<GlobalSchedule>> call, retrofit2.Response<ResponseEntity<GlobalSchedule>> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                int            errorCode    = response.body().errorCode;
                String         errorMessage = response.body().errorMessage;
                GlobalSchedule data         = response.body().data;
                String         service      = data.service;
                mWebAppId = data.captcha_web_appid;
                String wxminiAppid = data.captcha_wxmini_appid;
                Log.d(TAG, "onResponse: mWebAppId = " + mWebAppId + " , wxminiAppid = " + wxminiAppid);
                if (errorCode == 0) {
                    callback.onSuccess(mWebAppId);
                } else {
                    callback.onFailed(errorCode, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<GlobalSchedule>> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "unknown error");
            }
        });
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setUserFirstOpen(boolean isFirstOpen) {
        SPUtils.getInstance(PER_DATA).put(PER_FIRSTOPEN, isFirstOpen);
    }

    public boolean getUserFirstOpen() {
        return SPUtils.getInstance(PER_DATA).getBoolean(PER_FIRSTOPEN, false);
    }

    public String getToken() {
        if (mToken == null) {
            loadToken();
        }
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
        SPUtils.getInstance(PER_DATA).put(PER_TOKEN, mToken);
    }

    private void loadToken() {
        mToken = SPUtils.getInstance(PER_DATA).getString(PER_TOKEN, "");
    }


    public String getUserPhone() {
        if (mUserPhone == null) {
            mUserPhone = SPUtils.getInstance(PER_DATA).getString(PER_USER_PHONE, "");
        }
        return mUserPhone;
    }

    public void setUserPhone(String userPhone) {
        mUserPhone = userPhone;
        try {
            SPUtils.getInstance(PER_DATA).put(PER_USER_PHONE, mUserPhone);
        } catch (Exception e) {
        }
    }

    private String getUserPublishVideoDate() {
        if (mUserPubishVideoDate == null) {
            mUserPubishVideoDate = SPUtils.getInstance(PER_DATA).getString(PER_USER_DATE, "");
        }
        return mUserPubishVideoDate;
    }

    private void setUserPublishVideoDate(String date) {
        mUserPubishVideoDate = date;
        try {
            SPUtils.getInstance(PER_DATA).put(PER_USER_DATE, mUserPubishVideoDate);
        } catch (Exception e) {
        }
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void cancelRequest() {
        if (mSmsCall != null) {
            mSmsCall.cancel();
            mSessionId = null;
        }
        if (mLoginCall != null) {
            mLoginCall.cancel();
        }
    }

    public void getSms(String phone, String countryCode, String ticket, String randstr, final ActionCallback callback) {
        if (mSmsCall != null) {
            mSmsCall.cancel();
            mSessionId = null;
        }
        // 保存用户手机号
        Map<String, String> data = new LinkedHashMap<>();
        data.put("appId", mWebAppId);
        data.put("phone", countryCode + phone);
        data.put("ticket", ticket);
        data.put("randstr", randstr);
        setUserPhone(phone);
        mSmsCall = mApi.getSms(data);
        mSmsCall.enqueue(new Callback<ResponseEntity<SmsModel>>() {
            @Override
            public void onResponse(Call<ResponseEntity<SmsModel>> call, retrofit2.Response<ResponseEntity<SmsModel>> response) {
                ResponseEntity<SmsModel> res = response.body();
                if (res == null) {
                    return;
                }
                if (res.errorCode == 0) {
                    mSessionId = res.data.sessionId;
                    callback.onSuccess();
                } else {
                    callback.onFailed(res.errorCode, res.errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<SmsModel>> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
            }
        });
    }

    public void logoff(final ActionCallback callback) {
        if (mLoginOffCall != null) {
            mLoginOffCall.cancel();
        }
        // 构造注销请求参数
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }
        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }
        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        internalLogOff(data, callback);
    }

    private void internalLogOff(Map<String, String> data, final ActionCallback callback) {
        mLoginOffCall = mApi.deleteUser(data);
        mLoginOffCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                ResponseEntityEmpty res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "data is null");
                    return;
                }

                //注销失败
                if (res.errorCode != 0) {
                    callback.onFailed(res.errorCode, res.errorMessage);
                    return;
                }
                cancelRequest();
                setToken("");
                setApaasUserId("");
                isLogin = false;
                // 注销登录成功后,将头像和昵称设为空
                UserModelManager manager = UserModelManager.getInstance();
                UserModel userModel = manager.getUserModel();
                userModel.userAvatar = null;
                userModel.userName = null;
                userModel.userId = null;
                manager.setUserModel(userModel);
                callback.onSuccess();
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                isLogin = false;
                callback.onFailed(ERROR_CODE_UNKNOWN, "unknown error");
            }
        });
    }

    public void logout(final ActionCallback callback) {
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }
        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        internalLogout(data, callback);
    }

    private void internalLogout(Map<String, String> data, final ActionCallback callback) {
        Call<ResponseEntityEmpty> logoutCall = mApi.logout(data);
        logoutCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                if (response == null || response.body() == null) {
                    Log.d(TAG, "logout response is null");
                    return;
                }
                int    errCode = response.body().errorCode;
                String errMsg  = response.body().errorMessage;
                if (errCode != 0) {
                    Log.d(TAG, "logout failed errCode = " + errCode + " , errMsg = " + errMsg);
                    callback.onFailed(errCode, errMsg);
                    return;
                }
                cancelRequest();
                setToken("");
                setApaasUserId("");
                isLogin = false;
                callback.onSuccess();
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                isLogin = false;
                callback.onFailed(ERROR_CODE_UNKNOWN, "logout unknown error");
            }
        });
    }

    public void login(String phone, String countryCode, String sms, final ActionCallback callback) {
        if (mLoginCall != null) {
            mLoginCall.cancel();
        }

        if (mSessionId == null) {
            callback.onFailed(ERROR_CODE_UNKNOWN, "SessionId为空，请先获取验证码！");
            return;
        }
        // 构造请求参数
        Map<String, String> data = new LinkedHashMap<>();
        data.put("phone", countryCode + phone);
        data.put("code", sms);
        data.put("sessionId", mSessionId);
        mLoginCall = mApi.login(data);
        internalLogin(callback);
    }

    public void autoLogin(String token, final ActionCallback callback) {
        if (mLoginCall != null) {
            mLoginCall.cancel();
        }
        String apaasUserId = getApaasUserId();
        if (TextUtils.isEmpty(apaasUserId)) {
            callback.onFailed(-1, "autoLogin apaasUserId is null");
            return;
        }
        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", apaasUserId);
        data.put("token", token);
        mLoginCall = mApi.autoLogin(data);
        internalLogin(callback);
    }

    private void internalLogin(final ActionCallback callback) {
        mLoginCall.enqueue(new Callback<ResponseEntity<UserInfo>>() {
            @Override
            public void onResponse(Call<ResponseEntity<UserInfo>> call, retrofit2.Response<ResponseEntity<UserInfo>> response) {
                ResponseEntity<UserInfo> res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "获取数据为空");
                    return;
                }
                if (res.errorCode == 0 && res.data != null) {
                    UserInfo userInfo = res.data;
                    setToken(userInfo.token);
                    UserModel userModel = new UserModel();
                    userModel.userAvatar = userInfo.avatar;
                    userModel.phone = userInfo.phone;
                    userModel.userId = userInfo.userId;
                    setApaasUserId(userInfo.apaasUserId);
                    userModel.userName = userInfo.name;
                    if (!TextUtils.isEmpty(userInfo.userSig)) {
                        userModel.userSig = userInfo.userSig;
                    }
                    final UserModelManager userModelManager = UserModelManager.getInstance();
                    userModelManager.setUserModel(userModel);
                    // 如果说第一次没有设置用户名，跳转注册用户名
                    if (TextUtils.isEmpty(userInfo.name)) {
                        callback.onFailed(ERROR_CODE_NEED_REGISTER, "未注册用户");
                    } else {
                        isLogin = true;
                        callback.onSuccess();
                    }
                } else {
                    isLogin = false;
                    setToken("");
                    setApaasUserId("");
                    callback.onFailed(res.errorCode, res.errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<UserInfo>> call, Throwable t) {
                isLogin = false;
                callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
            }
        });
    }

    public void setUserName(String userId, final String userName, final ActionCallback callback) {
        if (mSetNameCall != null) {
            mSetNameCall.cancel();
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("name", userName);
        data.put("token", mToken);

        mSetNameCall = mApi.userUpdate(data);
        mSetNameCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                if (response == null || response.body() == null) {
                    Log.d(TAG, "userUpdate failed");
                    return;
                }
                if (response.body().errorCode == 0) {
                    UserModel userModel = UserModelManager.getInstance().getUserModel();
                    userModel.userName = userName;
                    UserModelManager.getInstance().setUserModel(userModel);
                    isLogin = true;
                    callback.onSuccess();
                } else {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "");
                }
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "");
            }
        });
    }

    public void setAvatar(final String avatar, final ActionCallback callback) {
        UserModel userModel = UserModelManager.getInstance().getUserModel();
        userModel.userAvatar = avatar;
        UserModelManager.getInstance().setUserModel(userModel);
        callback.onSuccess();
    }

    public NetworkAction getUserInfoByUserId(String userId, final GetUserInfoCallback callback) {
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }
        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }
        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        data.put("searchUserId", userId);
        return internalGetUserInfo(data, callback);
    }

    public NetworkAction getUserInfoByPhone(String phone, final GetUserInfoCallback callback) {
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }
        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }
        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        data.put("searchPhone", phone);
        return internalGetUserInfo(data, callback);
    }

    private NetworkAction internalGetUserInfo(Map<String, String> data, final GetUserInfoCallback callback) {
        Call<ResponseEntity<UserInfo>> call = mApi.getUserInfo(data);
        call.enqueue(new Callback<ResponseEntity<UserInfo>>() {
            @Override
            public void onResponse(Call<ResponseEntity<UserInfo>> call, retrofit2.Response<ResponseEntity<UserInfo>> response) {
                ResponseEntity<UserInfo> res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "获取数据为空");
                    return;
                }
                if (res.errorCode == 0 && res.data != null) {
                    UserInfo userInfo = res.data;
                    UserModel userModel = new UserModel();
                    userModel.userAvatar = userInfo.avatar;
                    userModel.phone = userInfo.phone;
                    userModel.userId = userInfo.userId;
                    userModel.userName = userInfo.name;
                    callback.onSuccess(userModel);
                } else {
                    callback.onFailed(res.errorCode, res.errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<UserInfo>> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "");
            }
        });
        return new NetworkAction(call);
    }

    public void getUserInfoBatch(List<String> userIdList, final GetUserInfoBatchCallback callback) {
        if (userIdList == null) {
            return;
        }
        String[] list = userIdList.toArray(new String[0]);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("userId", TextUtils.join("|", list));
        data.put("token", mToken);
        mApi.getUserInfoBatch(data)
                .enqueue(new Callback<ResponseEntity<List<UserInfo>>>() {
                    @Override
                    public void onResponse(Call<ResponseEntity<List<UserInfo>>> call, retrofit2.Response<ResponseEntity<List<UserInfo>>> response) {
                        ResponseEntity<List<UserInfo>> res = response.body();
                        if (res == null) {
                            callback.onFailed(ERROR_CODE_UNKNOWN, "获取数据为空");
                            return;
                        }
                        if (res.errorCode == 0 && res.data != null) {
                            List<UserInfo> userInfoList = res.data;
                            List<UserModel> userModelList = new ArrayList<>();
                            for (UserInfo userInfo : userInfoList) {
                                UserModel userModel = new UserModel();
                                userModel.userAvatar = userInfo.avatar;
                                userModel.phone = userInfo.phone;
                                userModel.userId = userInfo.userId;
                                userModel.userName = userInfo.name;
                                userModelList.add(userModel);
                            }
                            callback.onSuccess(userModelList);
                        } else {
                            callback.onFailed(res.errorCode, res.errorMessage);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseEntity<List<UserInfo>>> call, Throwable t) {

                    }
                });
    }

    /**
     * ==== 网络层相关 ====
     */
    private interface Api {
        @GET("base/v1/gslb")
        Call<ResponseEntity<GlobalSchedule>> getGlobalSchedule();

        @GET("base/v1/auth_users/user_verify_by_picture")
        Call<ResponseEntity<SmsModel>> getSms(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_login_code")
        Call<ResponseEntity<UserInfo>> login(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_login_token")
        Call<ResponseEntity<UserInfo>> autoLogin(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_logout")
        Call<ResponseEntityEmpty> logout(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_delete")
        Call<ResponseEntityEmpty> deleteUser(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_update")
        Call<ResponseEntityEmpty> userUpdate(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_query")
        Call<ResponseEntity<UserInfo>> getUserInfo(@QueryMap Map<String, String> map);

        @POST("getUserInfoBatch")
        @FormUrlEncoded
        Call<ResponseEntity<List<UserInfo>>> getUserInfoBatch(@FieldMap Map<String, String> map);
    }

    private class ResponseEntity<T> {
        public int    errorCode;
        public String errorMessage;
        public T      data;
    }

    private class UserInfo {
        public String token;
        public String phone;
        public String userSig;
        public String userId;
        public String apaasUserId;
        public String name;
        public String avatar;
    }

    private class SmsModel {
        public String sessionId;
    }

    /**
     * okhttp 拦截器
     */

    public static class HttpLogInterceptor implements Interceptor {
        private static final String  TAG  = "HttpLogInterceptor";
        private final        Charset UTF8 = Charset.forName("UTF-8");

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            RequestBody requestBody = request.body();
            String body = null;
            if (requestBody != null) {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                body = buffer.readString(charset);
            }

            Log.d(TAG,
                    "发送请求: method：" + request.method()
                            + "\nurl：" + request.url()
                            + "\n请求头：" + request.headers()
                            + "\n请求参数: " + body);

            long startNs = System.nanoTime();
            Response response = chain.proceed(request);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            ResponseBody responseBody = response.body();
            String rBody;

            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            rBody = buffer.clone().readString(charset);

            Log.d(TAG,
                    "收到响应: code:" + response.code()
                            + "\n请求url：" + response.request().url()
                            + "\n请求body：" + body
                            + "\nResponse: " + rBody);

            return response;
        }
    }

    public static class NetworkAction {
        private Call retrofitCall;

        public NetworkAction() {
        }

        public NetworkAction(Call call) {
            retrofitCall = call;
        }

        public void cancel() {
            if (retrofitCall != null) {
                retrofitCall.cancel();
            }
        }
    }

    // 操作回调
    public interface ActionCallback {
        void onSuccess();

        void onFailed(int code, String msg);
    }

    // 通过userid/phone获取用户信息回调
    public interface GetUserInfoCallback {
        void onSuccess(UserModel model);

        void onFailed(int code, String msg);
    }

    // 通过userId批量获取用户信息回调
    public interface GetUserInfoBatchCallback {
        void onSuccess(List<UserModel> model);

        void onFailed(int code, String msg);
    }

    // 首次TRTC打开摄像头提示"Demo特别配置了无限期云端存储"
    public void checkNeedShowSecurityTips(Activity activity) {
        String profileDate = ProfileManager.getInstance().getUserPublishVideoDate();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        String day = formatter.format(date);
        if (!day.equals(profileDate)) {
            ProfileManager.getInstance().setUserPublishVideoDate(day);
            android.app.AlertDialog.Builder normalDialog = new android.app.AlertDialog.Builder(activity);
            normalDialog.setMessage(activity.getResources().getString(R.string.login_first_enter_room_tips));
            normalDialog.setCancelable(false);
            normalDialog.setPositiveButton(activity.getResources().getString(R.string.login_dialog_ok), null);
            normalDialog.show();
        }
    }

    // 验证码数据回调
    public interface VerifyIdCallback {
        void onSuccess(String appId);

        void onFailed(int code, String msg);
    }

    private class GlobalSchedule {
        public String service;
        public String captcha_web_appid;
        public String captcha_wxmini_appid;
    }

    public class ResponseEntityEmpty {
        public int    errorCode;
        public String errorMessage;
    }

}
