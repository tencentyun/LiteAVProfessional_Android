package com.tencent.liteav.login.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.CountryCodeInfo;
import com.tencent.liteav.login.model.CountryCodeUtils;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.utils.Utils;
import com.tencent.liteav.login.ui.view.CountryCodeDialog;
import com.tencent.liteav.login.ui.view.ImageVerificationJsBridge;
import com.tencent.liteav.login.ui.view.LoginStatusLayout;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.tencent.liteav.login.model.ProfileManager.ERROR_CODE_NEED_REGISTER;


public class LoginActivity extends BaseActivity {
    private static final String TAG                  = "LoginActivity";
    private static final int    FLAG_COUNT_START     = 0;              // 发送验证码后开始计时的标志位
    private static final int    FLAG_COUNT_COUNTING  = 1;
    private static final int    FLAG_COUNT_STOP      = 2;               // 发送验证码后停止计时的标志位
    private static final int    STATUS_WITHOUT_LOGIN = 0;          // 未登录
    private static final int    STATUS_LOGGING_IN    = 1;             // 正在登录
    private static final int    STATUS_LOGIN_SUCCESS = 2;          // 登录成功
    private static final int    STATUS_LOGIN_FAIL    = 3;             // 登录失败
    private static final String DEFAULT_VERIFYJS     = "2079625916";

    private ImageView         mImageHead;
    private EditText          mEditPhone;
    private EditText          mEditSms;
    private TextView          mTextSendSms;
    private TextView          mTvCountryCode;
    private TextView          mTvUserProtocol;
    private CheckBox          mCbAgreeProtocol;
    private View              mProgressBar;
    private Button            mButtonLogin;
    private LoginStatusLayout mLayoutLoginStatus;               // 登录状态的提示栏
    private PopupWindow       mPopupWindow;
    private View              mContentView;
    private WebView           mWebView;
    private boolean           mReceivedSms              = false;                       // 是否已经收到验证码
    private boolean           mIsFirstOpen;                               //是否首次打开
    private Handler           mMainHandler;
    private Handler           mCountdownHandler;
    private String            mCountryCode;
    private Runnable          mResetLoginStatusRunnable = new Runnable() {
        @Override
        public void run() {
            handleLoginStatus(STATUS_WITHOUT_LOGIN);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_login);
        mCountdownHandler = new CountDownHandler(this);
        mMainHandler = new Handler();
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginBtnStatus();
    }

    private void initData() {
        mIsFirstOpen = ProfileManager.getInstance().getUserFirstOpen();
        if (!mIsFirstOpen) {
            showStatementDialog();
            ProfileManager.getInstance().setUserFirstOpen(true);
        }
        String token = ProfileManager.getInstance().getToken();
        if (!TextUtils.isEmpty(token)) {
            handleLoginStatus(STATUS_LOGGING_IN);
            ProfileManager.getInstance().autoLogin(token, new ProfileManager.ActionCallback() {
                @Override
                public void onSuccess() {
                    handleLoginStatus(STATUS_LOGIN_SUCCESS);
                    startMainActivity();
                }

                @Override
                public void onFailed(int code, String msg) {
                    if (code == ERROR_CODE_NEED_REGISTER) {
                        handleLoginStatus(STATUS_LOGIN_SUCCESS);
                        ToastUtils.showLong(R.string.login_tips_register);
                        Intent starter = new Intent(LoginActivity.this, ProfileActivity.class);
                        startActivity(starter);
                        finish();
                    } else {
                        handleLoginStatus(STATUS_LOGIN_FAIL);
                        ToastUtils.showLong(R.string.login_tips_auto_login_fail);
                    }
                }
            });
        }
        CountryCodeInfo info = CountryCodeUtils.getCountryCodeInfo(this);
        final List<CountryCodeInfo.CountryCodeEntity> list = info.getCountryCodeList();
        mCountryCode = String.valueOf(list.get(0).getCode());
        mTvCountryCode.setText("+" + mCountryCode);
        mTvCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountryCodeDialog dialog = new CountryCodeDialog(LoginActivity.this, list, new CountryCodeDialog.OnItemClickListener() {
                    @Override
                    public void onItemClick(CountryCodeInfo.CountryCodeEntity countryCodeEntity) {
                        mTvCountryCode.setText("+" + countryCodeEntity.getCode());
                        mCountryCode = String.valueOf(countryCodeEntity.getCode());
                    }
                });
                dialog.show();
            }
        });
        updateStatement();
    }

    private void initView() {
        mLayoutLoginStatus = (LoginStatusLayout) findViewById(R.id.cl_login_status);
        initHeadImage();
        mTvCountryCode = (TextView) findViewById(R.id.tv_country_code);
        mTvUserProtocol = (TextView) findViewById(R.id.tv_protocol);
        mCbAgreeProtocol = (CheckBox) findViewById(R.id.cb_protocol);
        findViewById(R.id.checkbox_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCbAgreeProtocol.setChecked(!mCbAgreeProtocol.isChecked());
            }
        });
        initEditPhone();
        initEditSms();
        initSendSms();
        initButtonLogin();
        initImageVerificationView();
    }

    private void initHeadImage() {
        mImageHead = (ImageView) findViewById(R.id.iv_head);
        if (Utils.isTRTCDemo(this)) {
            mImageHead.setBackgroundResource(R.drawable.login_head_icon);
        } else {
            mImageHead.setBackgroundResource(R.color.login_transparent);
        }
    }

    private void updateStatement() {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        String protocolStart = getString(R.string.login_protocol_start);
        String privacyProtocol = getString(R.string.login_privacy_protocol_detail);
        String userAgreement = getString(R.string.login_user_agreement_detail);
        String protocolAnd = getString(R.string.login_protocol_and);
        String protocolEnd = getString(R.string.login_protocol_end);
        builder.append(protocolStart);
        builder.append(privacyProtocol);
        builder.append(protocolAnd);
        builder.append(userAgreement);
        builder.append(protocolEnd);

        int privacyStartIndex = protocolStart.length();
        int privacyEndIndex = privacyStartIndex + privacyProtocol.length();
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent("com.tencent.liteav.action.videocloud.webview");
                intent.putExtra("title", getString(R.string.login_privacy_protocol));
                intent.putExtra("url", "https://web.sdk.qcloud.com/document/Tencent-Video-Cloud-Toolkit-Privacy-Protection-Guidelines.html");
                IntentUtils.safeStartActivity(LoginActivity.this, intent);
            }
        };
        builder.setSpan(privacyClickableSpan, privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int userAgreementStartIndex = privacyEndIndex + protocolAnd.length();
        int userAgreementEndIndex = userAgreementStartIndex + userAgreement.length();
        ClickableSpan userAgreementClickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent("com.tencent.liteav.action.videocloud.webview");
                intent.putExtra("title", getString(R.string.login_user_agreement));
                intent.putExtra("url", "https://web.sdk.qcloud.com/document/Tencent-Video-Cloud-Toolkit-User-Agreement.html");
                IntentUtils.safeStartActivity(LoginActivity.this, intent);
            }
        };
        builder.setSpan(userAgreementClickableSpan, userAgreementStartIndex, userAgreementEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTvUserProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        mTvUserProtocol.setText(builder);
        mTvUserProtocol.setHighlightColor(Color.TRANSPARENT);
    }

    private void initImageVerificationView() {
        mContentView = findViewById(R.id.content);
        final View popupView = LayoutInflater.from(this).inflate(R.layout.login_image_verification_popup, null);
        mWebView = popupView.findViewById(R.id.imageVerificationWebView);
        mPopupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        initWebView(mPopupWindow, mWebView);
    }

    /**
     * 开始图片验证码校验
     */
    private void startImageVerification() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        mWebView.loadUrl("file:///android_asset/verification.html");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ProfileManager.getInstance().getGlobalData(new ProfileManager.VerifyIdCallback() {
                    @Override
                    public void onSuccess(String appId) {
                        String webAppId = appId;
                        Log.v(TAG, "onSuccess: webAppId = " + webAppId);

                        if (TextUtils.isEmpty(webAppId)) {
                            webAppId = DEFAULT_VERIFYJS;
                        }
                        mWebView.evaluateJavascript("javascript:callVerifyJS(" + webAppId + ")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.v(TAG, "onReceiveValue: " + value);
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code, String msg) {

                    }
                });
            }
        });
        mPopupWindow.showAtLocation(mContentView, Gravity.CENTER, 0, 0);
    }


    private void initWebView(final PopupWindow popupWindow, WebView webView) {
        webView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new ImageVerificationJsBridge(new ImageVerificationJsBridge.CallBack() {
            @Override
            public void onSuccess(final String ticket, final String randStr) {
                Log.d(TAG, "imageVerification success");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (popupWindow != null && popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                        requestSms(ticket, randStr);
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (popupWindow != null && popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                    }
                });
                Log.e(TAG, "failed , errorCode:" + errorCode + "  " + errorMsg);
            }
        }), "imageVerificationJsBridge");
    }


    private void initEditPhone() {
        mEditPhone = (EditText) findViewById(R.id.et_phone);
        mEditPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginBtnStatus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initEditSms() {
        mEditSms = (EditText) findViewById(R.id.et_sms);
        mEditSms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginBtnStatus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initSendSms() {
        mTextSendSms = (TextView) findViewById(R.id.tv_get_sms);
        mTextSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCbAgreeProtocol.isChecked()) {
                    ToastUtils.showShort(R.string.login_protocol_tip);
                    return;
                }
                final String phone = mEditPhone.getText().toString().trim();
                if (mReceivedSms) {
                    ToastUtils.showLong(R.string.login_warning_get_sms_frequently);
                }
                if (TextUtils.isEmpty(phone)) {
                    ToastUtils.showLong(R.string.login_tips_invalid_phone);
                    return;
                }
                startImageVerification();
            }
        });
    }

    private void initButtonLogin() {
        mButtonLogin = (Button) findViewById(R.id.tv_login);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCbAgreeProtocol.isChecked()) {
                    ToastUtils.showShort(R.string.login_protocol_tip);
                    return;
                }
                if (!mReceivedSms) {
                    ToastUtils.showLong(R.string.login_send_verify_code);
                    return;
                }
                login();
            }
        });
    }

    private void updateLoginBtnStatus() {
        if (mEditPhone.length() == 0 || mEditSms.length() == 0) {
            mButtonLogin.setEnabled(false);
            return;
        }
        mButtonLogin.setEnabled(true);
    }

    /**
     * 获取验证码
     */
    private void requestSms(String ticket, String randstr) {
        mCountdownHandler.sendEmptyMessage(FLAG_COUNT_START);
        final String phone = mEditPhone.getText().toString().trim();
        ProfileManager.getInstance().getSms(phone, mCountryCode, ticket, randstr, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.showLong(R.string.login_tips_sent_sms);
            }

            @Override
            public void onFailed(int code, String msg) {
                mCountdownHandler.sendEmptyMessage(FLAG_COUNT_STOP);
                ToastUtils.showLong(R.string.login_get_sms_fail, msg);
            }
        });
    }

    private void login() {
        String phone = mEditPhone.getText().toString().trim();
        String sms = mEditSms.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(sms)) {
            ToastUtils.showLong(R.string.login_tips_input_correct_info);
            return;
        }
        handleLoginStatus(STATUS_LOGGING_IN);

        ProfileManager.getInstance().login(phone, mCountryCode, sms, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                handleLoginStatus(STATUS_LOGIN_SUCCESS);
                mCountdownHandler.sendEmptyMessage(FLAG_COUNT_STOP);
                startMainActivity();
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                if (code == ERROR_CODE_NEED_REGISTER) {
                    handleLoginStatus(STATUS_LOGIN_SUCCESS);
                    ToastUtils.showLong(R.string.login_tips_register);
                    Intent starter = new Intent(LoginActivity.this, ProfileActivity.class);
                    starter.putExtra("code", ERROR_CODE_NEED_REGISTER);
                    startActivity(starter);
                    finish();
                } else {
                    handleLoginStatus(STATUS_LOGIN_FAIL);
                }
            }
        });
    }

    private SpannableStringBuilder showTipSpan() {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        String protocolStart = getString(R.string.login_protocol_tip_start);
        String privacyProtocol = getString(R.string.login_privacy_protocol_detail);
        String userAgreement = getString(R.string.login_user_agreement_detail);
        String protocolAnd = getString(R.string.login_protocol_and);
        String protocolEnd = getString(R.string.login_protocol_tip_end);
        builder.append(protocolStart);
        builder.append(privacyProtocol);
        builder.append(protocolAnd);
        builder.append(userAgreement);
        builder.append(protocolEnd);

        int privacyStartIndex = protocolStart.length();
        int privacyEndIndex = privacyStartIndex + privacyProtocol.length();
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent("com.tencent.liteav.action.videocloud.webview");
                intent.putExtra("title", getString(R.string.login_privacy_protocol));
                intent.putExtra("url", "https://web.sdk.qcloud.com/document/Tencent-Video-Cloud-Toolkit-Privacy-Protection-Guidelines.html");
                IntentUtils.safeStartActivity(LoginActivity.this, intent);
            }
        };
        builder.setSpan(privacyClickableSpan, privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int userAgreementStartIndex = privacyEndIndex + protocolAnd.length();
        int userAgreementEndIndex = userAgreementStartIndex + userAgreement.length();
        ClickableSpan userAgreementClickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent("com.tencent.liteav.action.videocloud.webview");
                intent.putExtra("title", getString(R.string.login_user_agreement));
                intent.putExtra("url", "https://web.sdk.qcloud.com/document/Tencent-Video-Cloud-Toolkit-User-Agreement.html");
                IntentUtils.safeStartActivity(LoginActivity.this, intent);
            }
        };
        builder.setSpan(userAgreementClickableSpan, userAgreementStartIndex, userAgreementEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private void showStatementDialog() {
        SpannableStringBuilder builder = showTipSpan();

        final Dialog mDialog = new Dialog(this, R.style.LoginShowTipTheme);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.login_show_tip_dialog_confirm, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        tvMessage.setText(builder);
        tvMessage.setHighlightColor(Color.TRANSPARENT);
        Button btnNeg = (Button) view.findViewById(R.id.btn_negative);
        Button btnPos = (Button) view.findViewById(R.id.btn_positive);
        btnNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCbAgreeProtocol.setChecked(true);
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        mDialog.setContentView(view);
        mDialog.show();
    }

    public void handleLoginStatus(int loginStatus) {
        mLayoutLoginStatus.setLoginStatus(loginStatus);

        if (STATUS_LOGGING_IN == loginStatus) {
            mMainHandler.removeCallbacks(mResetLoginStatusRunnable);
            mMainHandler.postDelayed(mResetLoginStatusRunnable, 6000);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("com.tencent.liteav.action.liteavapp");
        IntentUtils.safeStartActivity(LoginActivity.this, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainHandler.removeCallbacks(mResetLoginStatusRunnable);
        mCountdownHandler.removeCallbacksAndMessages(null);
    }

    private static class CountDownHandler extends Handler {

        private WeakReference<LoginActivity> mWeakReference;
        private int                          mCount = 60;

        CountDownHandler(LoginActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case FLAG_COUNT_START:
                    if (activity.mReceivedSms) {
                        return;
                    }
                    activity.mReceivedSms = true;
                    mCount = 60;
                    activity.mTextSendSms.setEnabled(false);
                    activity.mTextSendSms.setText(activity.getString(R.string.login_form_count_time, mCount));
                    sendEmptyMessageDelayed(FLAG_COUNT_COUNTING, 1000);
                    break;
                case FLAG_COUNT_COUNTING:
                    if (!activity.mReceivedSms) {
                        return;
                    }
                    mCount--;
                    if (mCount == 0) {
                        sendEmptyMessage(FLAG_COUNT_STOP);
                    } else {
                        activity.mTextSendSms.setText(activity.getString(R.string.login_form_count_time, mCount));
                        sendEmptyMessageDelayed(FLAG_COUNT_COUNTING, 1000);
                    }
                    break;
                case FLAG_COUNT_STOP:
                    activity.mReceivedSms = false;
                    activity.mTextSendSms.setEnabled(true);
                    activity.mTextSendSms.setText(R.string.login_text_get_sms);
                    break;
                default:
                    break;
            }
        }
    }
}
