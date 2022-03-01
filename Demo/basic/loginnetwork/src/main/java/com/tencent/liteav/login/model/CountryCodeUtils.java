package com.tencent.liteav.login.model;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CountryCodeUtils {

    private static final String TAG                       = "CountryCodeUtils";
    private static final String DEFAULT_COUNTRY_CODE_DATA = "country_code.json";

    public static CountryCodeInfo getCountryCodeInfo(Context context) {
        return createCountryCodeInfo(readAssetsFile(context, DEFAULT_COUNTRY_CODE_DATA));
    }

    public static CountryCodeInfo createCountryCodeInfo(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CountryCodeInfo.class);
    }

    public static String readAssetsFile(Context context, String fileName) {
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = context.getAssets().open(fileName);
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String readLine;
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
