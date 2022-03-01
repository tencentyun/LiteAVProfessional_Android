package com.tencent.liteav.login.model;

import java.util.List;

public class CountryCodeInfo {

    private List<CountryCodeEntity> countryCodeList;

    public List<CountryCodeEntity> getCountryCodeList() {
        return countryCodeList;
    }

    public void setCountryCodeList(List<CountryCodeEntity> countryCodeList) {
        this.countryCodeList = countryCodeList;
    }

    public static class CountryCodeEntity {
        /**
         * en : China
         * zh : 中国大陆
         * locale : CN
         * code : 86
         */

        private String en;
        private String zh;
        private String locale;
        private int    code;

        public String getEn() {
            return en;
        }

        public void setEn(String en) {
            this.en = en;
        }

        public String getZh() {
            return zh;
        }

        public void setZh(String zh) {
            this.zh = zh;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}
