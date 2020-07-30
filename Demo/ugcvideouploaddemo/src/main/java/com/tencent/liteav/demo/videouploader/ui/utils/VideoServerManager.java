package com.tencent.liteav.demo.videouploader.ui.utils;

import android.text.TextUtils;

import com.tencent.liteav.basic.log.TXCLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 用来和业务服务器进行交互，主要有如下两个业务：
 * - 向业务服务器获得视频上传的签名信息；
 * - 向业务服务器汇报视频信息；
 */
public class VideoServerManager {
    private static final String TAG = "VideoServerManager";

    private static VideoServerManager sInstance;
    private OkHttpClient              mOkHttpClient;
    private PublishSigListener        mPublishSigListener;

    public static VideoServerManager getInstance() {
        if (sInstance == null) {
            sInstance = new VideoServerManager();
        }
        return sInstance;
    }

    private VideoServerManager() {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)    // 设置超时时间
                .readTimeout(5, TimeUnit.SECONDS)       // 设置读取超时时间
                .writeTimeout(5, TimeUnit.SECONDS)      // 设置写入超时时间
                .build();
    }

    public void setPublishSigListener(PublishSigListener listener) {
        mPublishSigListener = listener;
    }

    /**
     * 获得发布的签名信息；
     * */
    public void getPublishSig() {
        String sigParams = getSigParams();
        Request request = new Request.Builder()
                .url(Constants.ADDRESS_SIG + "?" + sigParams)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TXCLog.e(TAG, "getPublishSig onFailure : " + e.toString());
                notifyGetPublishSigFail(Constants.RetCode.CODE_REQUEST_ERR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // response.body().string()调用后，会把流关闭，因此只能调用一次
                String contentStr = response.body().string();
                TXCLog.i(TAG, "getPublishSig onResponse : " + contentStr);
                parseSigRes(contentStr);
            }
        });
    }

    private void parseSigRes(String sigRes) {
        if (TextUtils.isEmpty(sigRes)) {
            notifyGetPublishSigFail(Constants.RetCode.CODE_PARSE_ERR);
            TXCLog.e(TAG, "parseSigRes err, sigRes is empty!");
            return;
        }
        try {
            JSONObject resJson = new JSONObject(sigRes);
            int code = resJson.optInt("code");
            if (code != Constants.RetCode.CODE_SUCCESS) {
                TXCLog.e(TAG, "parseSigRes fail, code = " + code);
                notifyGetPublishSigFail(code);
                return;
            }
            JSONObject dataObj = resJson.getJSONObject("data");
            String signature = dataObj.optString("signature");
            if (TextUtils.isEmpty(signature)) {
                TXCLog.e(TAG, "parseSigRes, after parse signature is empty!");
                notifyGetPublishSigFail(Constants.RetCode.CODE_PARSE_ERR);
                return;
            }
            notifyGetPublishSigSuccess(signature);
        } catch (JSONException e) {
            e.printStackTrace();
            notifyGetPublishSigFail(Constants.RetCode.CODE_PARSE_ERR);
        }
    }

    /**
     * 访问服务器需要加鉴权
     */
    private String getSigParams() {
        long timeStamp = System.currentTimeMillis() / 1000;
        String nonce = Utils.getMD5Encryption(String.valueOf(System.currentTimeMillis()));
        String sig = Utils.getMD5Encryption(Constants.VOD_APPID + String.valueOf(timeStamp) + nonce + Constants.VOD_APPKEY);
        String sigParams = "timestamp=" + timeStamp
                + "&" + "nonce=" + nonce
                + "&" + "sig=" + sig
                + "&" + "appid=" + Constants.VOD_APPID;
        return sigParams;
    }

    private void notifyGetPublishSigFail(int errCode) {
        if (mPublishSigListener != null) {
            mPublishSigListener.onFail(errCode);
        }
    }

    private void notifyGetPublishSigSuccess(String signatureStr) {
        if (mPublishSigListener != null) {
            mPublishSigListener.onSuccess(signatureStr);
        }
    }

    /**
     * 把上传返回的视频信息或者自定义的视频信息上报到自己的业务服务器
     */
    public void reportVideoInfo(String fileId, String authorName) {
        Request request = new Request.Builder()
                .url(Constants.ADDRESS_VIDEO_REPORT + fileId + "?" + getSigParams())
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TXCLog.e(TAG, "reportVideoInfo onFailure : " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String contentStr = response.body().string();
                TXCLog.i(TAG, "reportVideoInfo onResponse : " + contentStr);
                parseReportVideoResult(contentStr);
            }
        });
    }

    private void parseReportVideoResult(String contentStr) {
        if (TextUtils.isEmpty(contentStr)) {
            TXCLog.e(TAG, "parseReportVideoResult err, contentStr is empty!");
            return;
        }
        JSONObject resJson = null;
        try {
            resJson = new JSONObject(contentStr);
            int code = resJson.optInt("code");
            if (code != Constants.RetCode.CODE_SUCCESS) {
                TXCLog.e(TAG, "parseReportVideoResult fail, code = " + code);
                notifyGetPublishSigFail(code);
                return;
            }
            TXCLog.i(TAG, "reportVideoInfo, report video info success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface PublishSigListener {
        void onSuccess(String signatureStr);

        void onFail(int errCode);
    }

}
