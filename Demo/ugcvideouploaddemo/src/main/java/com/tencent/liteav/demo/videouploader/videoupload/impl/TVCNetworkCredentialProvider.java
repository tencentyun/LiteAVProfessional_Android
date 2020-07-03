package com.tencent.liteav.demo.videouploader.videoupload.impl;

/**
 * Created by carolsuo on 2017/10/9.
 */

import android.text.TextUtils;

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.BasicQCloudCredentials;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.rtmp.TXLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/** 从服务器获取签名 signKey keyTime*/
public class TVCNetworkCredentialProvider extends BasicLifecycleCredentialProvider {
    static final String TAG = "TVCNetworkCredentialProvider";
    static final String SVR_POST_URL = ""; //获取签名的服务器地址
    static final String SVR_RETURN_CODE  = "returnValue";
    static final String SVR_RETURN_MSG   = "returnMsg";
    static final String SVR_RETURN_DATA  = "returnData";
    String secretId = null;

    public TVCNetworkCredentialProvider(String secretId) {
        this.secretId = secretId;
    }


    @Override
    protected QCloudLifecycleCredentials fetchNewCredentials() throws QCloudClientException {
        String signKey = null;
        String keyTime = null;
        try {
            URL signKeyUrl = new URL(SVR_POST_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) signKeyUrl.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setUseCaches(false);
            urlConnection.setReadTimeout(3000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            urlConnection.connect();
            JSONObject json = new JSONObject();
            json.put("Action", "GetCOSSignV2");
            String jsonstr = json.toString();
            OutputStream out = urlConnection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(jsonstr);
            bw.flush();
            out.close();
            bw.close();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String str = null;
                StringBuffer buffer = new StringBuffer();
                while ((str = br.readLine()) != null) {
                    buffer.append(str);
                }
                in.close();
                br.close();
                JSONObject rjson = new JSONObject(buffer.toString());
                if (rjson.has(SVR_RETURN_CODE) && rjson.getInt(SVR_RETURN_CODE) == 0) {
                    JSONObject retData = rjson.optJSONObject(SVR_RETURN_DATA);
                    if (retData.has("signKey")) {
                        signKey = retData.getString("signKey");
                    }
                    if (retData.has("keyTime")) {
                        keyTime = retData.getString("keyTime");
                    }
                    TXLog.d(TAG, "upload got cos sig succeed");
                } else {
                    TXLog.d(TAG, "upload got cos sig failed");
                }
            }
            urlConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(signKey) && !TextUtils.isEmpty(keyTime))
            return new BasicQCloudCredentials(this.secretId, signKey, keyTime);
        else
            return null;
    }
}
