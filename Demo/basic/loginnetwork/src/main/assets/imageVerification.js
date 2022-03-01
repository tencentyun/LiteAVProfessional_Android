var captcha = new TencentCaptcha('2079625916', function (res) {
    if (res) {
        if (res.ret === 0) {
            window.imageVerificationJsBridge.verifySuccess(res.ticket, res.randstr);
        } else {
            window.imageVerificationJsBridge.verifyError(res.ret, "user canceled");
        }
    } else {
        window.imageVerificationJsBridge.verifyError(-1, "unknow error");
    }
}, {
    ready: function (data) {
    },
    needFeedBack: true,
    enableDarkMode: true,
});
captcha.show();