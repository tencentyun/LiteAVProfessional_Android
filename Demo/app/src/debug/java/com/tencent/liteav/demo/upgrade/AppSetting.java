package com.tencent.liteav.demo.upgrade;

import com.google.gson.annotations.SerializedName;

/**
 * AppSetting, 用于升级、配置文件等，详细定义参见: https://iwiki.woa.com/pages/viewpage.action?pageId=1174276897
 */
public class AppSetting {

    /**
     * 状态码
     * 0：表示成功
     * -100：必填请求参数缺失或非空
     * -101：请求参数  appVersion 格式错误，详见 appVersion 要求
     * -102：请求参数 os 格式错误，目前仅支持：["windows", "ios", "mac", "android"]
     * -103：数据为空，请检查 appName 是否正确
     * -500：服务端异常错误
     */
    @SerializedName("code")
    private Integer code;

    /**
     * 状态码对应的说明信息
     */
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        /**
         * 是否需要升级，升级策略详见: http://tapd.oa.com/Qcloud_MLVB/prong/stories/view/1010146251869155023
         */
        @SerializedName("isNeedUpdate")
        private Boolean isNeedUpdate;

        /**
         * 最新版本 app 的版本号
         */
        @SerializedName("appVersion")
        private String appVersion;

        /**
         * 最新版本 app 的下载更新地址
         */
        @SerializedName("downloadUrl")
        private String downloadUrl;

        @SerializedName("settings")
        private Settings settings;

        public Boolean getIsNeedUpdate() {
            return isNeedUpdate;
        }

        public void setIsNeedUpdate(Boolean isNeedUpdate) {
            this.isNeedUpdate = isNeedUpdate;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public Settings getSettings() {
            return settings;
        }

        public void setSettings(Settings settings) {
            this.settings = settings;
        }

        /**
         * 配置文件
         */
        public static class Settings {
        }
    }
}
