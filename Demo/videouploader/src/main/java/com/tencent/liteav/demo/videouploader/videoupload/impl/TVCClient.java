package com.tencent.liteav.demo.videouploader.videoupload.impl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.transfer.UploadService;
import com.tencent.liteav.demo.videouploader.videoupload.impl.compute.TXHttpTaskMetrics;
import com.tencent.liteav.demo.videouploader.videoupload.impl.compute.TXOnGetHttpTaskMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 视频上传客户端
 */
public class TVCClient {
    private final static String TAG = "TVC-Client";
    private Context context;
    private Handler mainHandler;
    private boolean busyFlag = false;
    private boolean cancleFlag = false;

    private TVCUploadInfo uploadInfo;

    private UGCClient ugcClient;
    private TVCUploadListener tvcListener;

    private int cosAppId;   //点播上传用到的COS appid
    private int userAppId;  //客户自己的appid，数据上报需要
    private String uploadRegion = "";
    private String cosBucket;
    private String cosTmpSecretId = "";
    private String cosTmpSecretKey = "";
    private String cosToken = "";
    private long cosExpiredTime;
    private long localTimeAdvance = 0;        //本地时间相对unix时间戳提前间隔

    private String cosVideoPath;
    private String videoFileId;
    private String cosCoverPath;

    private boolean isOpenCosAcc = false;   //是否使用cos动态加速
    private String cosAccDomain = "";       //动态加速域名
    private String cosHost = "";

    private String domain;
    private String cosIP = "";
    private String vodSessionKey = null;

    private long reqTime = 0;            //各阶段开始请求时间
    private long initReqTime = 0;        //上传请求时间，用于拼接reqKey。串联请求
    private String customKey = "";       //用于数据上报

    private CosXmlService cosService;
    private UploadService cosUploadHelper;

    // 断点重传session本地缓存
    // 以文件路径作为key值得，存储的内容是<session, uploadId, fileLastModify, expiredTime>
    private static final String LOCALFILENAME = "TVCSession";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mShareEditor;

    private String uploadId = null;
    private long fileLastModTime = 0;     //视频文件最后修改时间
    private boolean enableResume = true;
    private boolean enableHttps = false;
    private UGCReport.ReportInfo reportInfo;

    private static final int VIRTUAL_TOTAL_PERCENT = 10;    //前后的虚拟进度占的百分比
    private TimerTask virtualProgress = null;   //虚拟进度任务
    private Timer mTimer;                       //定时器
    private int virtualPercent = 0;             //虚拟进度
    private boolean realProgressFired = false;

    /**
     * 初始化上传实例
     *
     * @param signature 签名
     * @param iTimeOut  超时时间
     */
    public TVCClient(Context context, String customKey, String signature, boolean enableResume, boolean enableHttps, int iTimeOut) {
        TXUGCPublishOptCenter.getInstance().prepareUpload(signature);

        this.context = context.getApplicationContext();
        ugcClient = UGCClient.getInstance(signature, iTimeOut);
        mainHandler = new Handler(context.getMainLooper());
        mSharedPreferences = context.getSharedPreferences(LOCALFILENAME, Activity.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();
        this.enableResume = enableResume;
        this.enableHttps = enableHttps;
        this.customKey = customKey;
        reportInfo = new UGCReport.ReportInfo();
        clearLocalCache();
    }

    /**
     * 初始化上传实例
     *
     * @param ugcSignature 签名
     */
    public TVCClient(Context context, String customKey, String ugcSignature, boolean resumeUpload, boolean enableHttps) {
        this(context, customKey, ugcSignature, resumeUpload, enableHttps, 8);
    }

    // 清理一下本地缓存，过期的删掉
    private void clearLocalCache() {
        if (mSharedPreferences != null) {
            try {
                Map<String, ?> allContent = mSharedPreferences.getAll();
                //注意遍历map的方法
                for(Map.Entry<String, ?>  entry : allContent.entrySet()){
                    JSONObject json = new JSONObject((String) entry.getValue());
                    long expiredTime = json.optLong("expiredTime", 0);
                    // 过期了清空key
                    if (expiredTime < System.currentTimeMillis() / 1000) {
                        mShareEditor.remove(entry.getKey());
                        mShareEditor.commit();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (virtualProgress != null) {
            virtualProgress = null;
        }

        if (virtualProgress == null) {
            virtualProgress = new TimerTask() {
                @Override
                public void run() {
                    postVirtualProgress();
                }
            };
        }

        mTimer = new Timer();
        mTimer.schedule(virtualProgress, 2000/VIRTUAL_TOTAL_PERCENT, 2000/VIRTUAL_TOTAL_PERCENT);   //前后的虚拟进度大概持续2s
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (virtualProgress != null) {
            virtualProgress = null;
        }
    }

    private void postVirtualProgress() {
        if (uploadInfo != null) {
            long total = uploadInfo.getFileSize() + (uploadInfo.isNeedCover()? uploadInfo.getCoverFileSize(): 0);
            if ((virtualPercent >= 0 && virtualPercent < 10) || (virtualPercent >=90 && virtualPercent <100)) {
                ++virtualPercent;
                notifyUploadProgress(virtualPercent * total / 100, total);
            }
        }
    }

    // 通知上层上传成功
    private void notifyUploadSuccess(final String fileId, final String playUrl, final String coverUrl) {
        TXUGCPublishOptCenter.getInstance().delPublishing(uploadInfo.getFilePath());
        final long total = uploadInfo.getFileSize() + (uploadInfo.isNeedCover()? uploadInfo.getCoverFileSize(): 0);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onProgress(total, total);
                tvcListener.onSucess(fileId, playUrl, coverUrl);
            }
        });
        stopTimer();
    }

    // 通知上层上传失败
    private void notifyUploadFailed(final int errCode, final String errMsg) {
        TXUGCPublishOptCenter.getInstance().delPublishing(uploadInfo.getFilePath());
        mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    tvcListener.onFailed(errCode, errMsg);
                }
            });
        stopTimer();
    }

    // 通知上层上传进度
    private void notifyUploadProgress(final long currentSize, final long totalSize) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onProgress(currentSize, totalSize);
            }
        });
    }

    private boolean isVideoFileExist(String path) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("getFileSize", "getFileSize: " + e);
            return false;
        }
        return false;
    }

    /**
     * 上传视频文件
     *
     * @param info     视频文件信息
     * @param listener 上传回调
     * @return
     */
    public int uploadVideo(TVCUploadInfo info, TVCUploadListener listener) {
        if (busyFlag) {     // 避免一个对象传输多个文件
            return TVCConstants.ERR_CLIENT_BUSY;
        }
        busyFlag = true;
        this.uploadInfo = info;
        this.tvcListener = listener;

        String fileName = info.getFileName();
        Log.d(TAG, "fileName = " + fileName);
        if (fileName != null && fileName.getBytes().length > 200) { //视频文件名太长 直接返回
            tvcListener.onFailed(TVCConstants.ERR_UGC_FILE_NAME, "file name too long");
            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_FILE_NAME,0, "", "file name too long", System.currentTimeMillis(), 0, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0 ,0);

            return TVCConstants.ERR_UGC_FILE_NAME;
        }

        if (info.isContainSpecialCharacters(fileName)) {//视频文件名包含特殊字符 直接返回
            tvcListener.onFailed(TVCConstants.ERR_UGC_FILE_NAME, "file name contains special character / : * ? \" < >");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_FILE_NAME, 0, "","file name contains special character / : * ? \" < >", System.currentTimeMillis(), 0, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),"", "", 0 ,0);

            return TVCConstants.ERR_UGC_FILE_NAME;
        }

        if (!TXUGCPublishOptCenter.getInstance().isPublishing(info.getFilePath()) && enableResume)
            getResumeData(info.getFilePath());
        TXUGCPublishOptCenter.getInstance().addPublishing(info.getFilePath());
        getCosUploadInfo(info, vodSessionKey);
        return TVCConstants.NO_ERROR;
    }

    /**
     * 取消（中断）上传。中断之后恢复上传再用相同的参数调用uploadVideo即可。
     * @return 成功或者失败
     */
    public void cancleUpload() {
        if (cosUploadHelper != null) {
            cosUploadHelper.pause();
            cancleFlag = true;
        }
    }

    private void getCosUploadInfo(TVCUploadInfo info, String vodSessionKey) {
        startTimer();   //启动开始虚拟进度
        // 第一步 向UGC请求上传(获取COS认证信息)
        reqTime = System.currentTimeMillis();
        initReqTime = reqTime;
        ugcClient.initUploadUGC(info, customKey, vodSessionKey, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "initUploadUGC->onFailure: " + e.toString());
                notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, e.toString());

                txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, 1, "", e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, "HTTP Code:" + response.code());

                    txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, response.code(), "", "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),"", "", 0 ,0);

                    setResumeData(uploadInfo.getFilePath(), "", "");

                    Log.e(TAG, "initUploadUGC->http code: " + response.code());
                    throw new IOException("" + response);
                } else {
                    parseInitRsp(response.body().string());
                }
            }
        });
    }

    // 解析上传请求返回信息
    private void parseInitRsp(String rspString) {
        Log.i(TAG, "parseInitRsp: " + rspString);
        if (TextUtils.isEmpty(rspString)) {
            Log.e(TAG, "parseInitRsp->response is empty!");
            notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, "init response is empty");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, 2, "", "init response is empty", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0,0);

            setResumeData(uploadInfo.getFilePath(), "", "");

            return;
        }

        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            Log.i(TAG, "parseInitRsp: " + code);

            String message = "";
            try {
                message = new String(jsonRsp.optString("message", "").getBytes("UTF-8"),"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (0 != code) {
                notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, code + "|" + message);

                txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, code, "", code + "|" + message, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),"", "", 0,0);

                vodSessionKey = null;
                setResumeData(uploadInfo.getFilePath(), "", "");

                return;
            }

            JSONObject dataObj = jsonRsp.getJSONObject("data");
            JSONObject videoObj = dataObj.getJSONObject("video");
            cosVideoPath = videoObj.getString("storagePath");

            // cos上传临时证书
            JSONObject tempCertificate = dataObj.getJSONObject("tempCertificate");
            cosTmpSecretId = tempCertificate.optString("secretId");
            cosTmpSecretKey = tempCertificate.optString("secretKey");
            cosToken = tempCertificate.optString("token");
            cosExpiredTime = tempCertificate.optLong("expiredTime");

            long serverTS = dataObj.optLong("timestamp", 0);

            Log.d(TAG, "isNeedCover:" + uploadInfo.isNeedCover());
            if (uploadInfo.isNeedCover()) {
                JSONObject coverObj = dataObj.getJSONObject("cover");
                cosCoverPath = coverObj.getString("storagePath");
            }
            cosAppId = dataObj.getInt("storageAppId");
            cosBucket = dataObj.getString("storageBucket") + "-" + cosAppId; //从5.4.10升级到5.4.20之后，废除了setAppIdAndRegion接口，需要自行拼接保证costBucket格式为 bucket-appId
            uploadRegion = dataObj.getString("storageRegionV5");
            domain = dataObj.getString("domain");
            vodSessionKey = dataObj.getString("vodSessionKey");
            userAppId = dataObj.getInt("appId");

            JSONObject cosAccObj = dataObj.optJSONObject("cosAcc");
            if (cosAccObj != null) {
                isOpenCosAcc = cosAccObj.optInt("isOpen", 0) == 0? false: true;
                cosAccDomain = cosAccObj.optString("domain", "");
            }

            Log.d(TAG, "cosVideoPath=" + cosVideoPath);
            Log.d(TAG, "cosCoverPath=" + cosCoverPath);
            Log.d(TAG, "cosAppId=" + cosAppId);
            Log.d(TAG, "cosBucket=" + cosBucket);
            Log.d(TAG, "uploadRegion=" + uploadRegion);
            Log.d(TAG, "domain=" + domain);
            Log.d(TAG, "vodSessionKey=" + vodSessionKey);
            Log.d(TAG, "cosAcc.isOpen=" + isOpenCosAcc);
            Log.d(TAG, "cosAcc.domain=" + cosAccDomain);


            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .setRegion(uploadRegion)
                    .setDebuggable(true)
                    .isHttps(enableHttps)
                    .builder();

            cosHost = getCosIP(cosXmlServiceConfig);


            long localTS = System.currentTimeMillis() / 1000L;
            if (serverTS > 0 && (localTS - serverTS > 5 * 60 || serverTS - localTS > 5 * 60)) {
                localTimeAdvance = localTS - serverTS;
            }
            cosService = new CosXmlService(context, cosXmlServiceConfig,
                        new TVCDirectCredentialProvider(cosTmpSecretId, cosTmpSecretKey, cosToken, localTS - localTimeAdvance, cosExpiredTime));

            List<String> cosIps = TXUGCPublishOptCenter.getInstance().query(cosHost);
            if (cosIps != null && cosIps.size() > 0) {
                cosService.addCustomerDNS(cosHost, cosIps.toArray(new String[cosIps.size()]));
            }

            // 第二步 通过COS上传视频
            uploadCosVideo();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_PARSE_FAILED, 3, "",  e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "",0, 0);
            notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, e.toString());
            return;
        } catch (CosXmlClientException e) {
            //addCustomerDNS出现异常，不影响正常上传
            Log.e(TAG, e.toString());
        }

        txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, 0, 0, "", "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),"", "", 0,0);
    }

    private String getCosIP(CosXmlServiceConfig cosXmlServiceConfig) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosBucket, cosCoverPath, uploadInfo.getCoverPath());
        final String cosHost = putObjectRequest.getHost(cosXmlServiceConfig, isOpenCosAcc, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(cosHost);
                    cosIP = address.getHostAddress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return cosHost;
    }

    // 通过COS上传封面
    private void uploadCosCover() {
        reqTime = System.currentTimeMillis();
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosBucket, cosCoverPath, uploadInfo.getCoverPath());
        putObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.d(TAG, "uploadCosCover->progress: " + progress + "/" + max);
                if (progress >= max) {
                    virtualPercent = 100 - VIRTUAL_TOTAL_PERCENT;
                    startTimer();   //上传完成，启动结束虚拟进度
                } else {
                    max += uploadInfo.getFileSize();
                    notifyUploadProgress((progress + uploadInfo.getFileSize())*(100 - 2 * VIRTUAL_TOTAL_PERCENT)/100 + max*VIRTUAL_TOTAL_PERCENT/100, max);
                }
            }
        });

//        putObjectRequest.setSign(reqTime/1000L - localTimeAdvance, cosExpiredTime); // 5.4.10 升级到 5.4.20 该API被弃用， 以key时间过期为准 CosXmlService 中设置
        putObjectRequest.isSupportAccelerate(isOpenCosAcc);
        final TXHttpTaskMetrics metrics = new TXHttpTaskMetrics();
        putObjectRequest.attachMetrics(metrics);
        cosService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                String requestId = getRequestId(cosXmlResult);
                txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, 0, 0, "", "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getCoverFileSize(), uploadInfo.getCoverImgType(), uploadInfo.getCoverName(),"", requestId, metrics.getTCPConnectionTimeCost(), metrics.getRecvRspTimeCost());
                startFinishUploadUGC(cosXmlResult);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                String cosErr = "";
                if(qcloudException != null){
                    stringBuilder.append(qcloudException.getMessage());
                    cosErr = String.valueOf(qcloudException.errorCode);
                }else {
                    stringBuilder.append(qcloudServiceException.toString());
                    cosErr = qcloudServiceException.getErrorCode();
                }

                notifyUploadFailed(TVCConstants.ERR_UPLOAD_COVER_FAILED, "cos upload error:" + stringBuilder.toString());

                String requestId = "";
                if (qcloudServiceException != null) {
                    requestId =  qcloudServiceException.getRequestId();
                }
                txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_COVER_FAILED, 0, cosErr, stringBuilder.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getCoverFileSize(), uploadInfo.getCoverImgType(), uploadInfo.getCoverName(), "", requestId, metrics.getTCPConnectionTimeCost(), metrics.getRecvRspTimeCost());
            }
        });
    }

    private String getRequestId(CosXmlResult result) {
        List<String> requestIds = result.headers.get("x-cos-request-id");
        String requestId = requestIds != null && requestIds.size() != 0 ? requestIds.get(0) : "";
        return requestId;
    }


    // 解析cos上传视频返回信息
    private void startUploadCoverFile(CosXmlResult result) {
        // 第三步 通过COS上传封面
        if (uploadInfo.isNeedCover()) {
            uploadCosCover();
        } else {
            startFinishUploadUGC(result);
        }
    }


    // 通过COS上传视频
    private void uploadCosVideo() {
        new Thread() {
            @Override
            public void run() {
                reqTime = System.currentTimeMillis();

                Log.i(TAG, "uploadCosVideo begin :  cosBucket " + cosBucket + " cosVideoPath: " + cosVideoPath + "  path " + uploadInfo.getFilePath());
                long tcpConenctionTimeCost = 0;
                long recvRspTimeCost = 0;
                try {
                    CosXmlResult result;
                    UploadService.ResumeData resumeData = new UploadService.ResumeData();
                    resumeData.bucket = cosBucket;
                    resumeData.cosPath = cosVideoPath;
                    resumeData.srcPath = uploadInfo.getFilePath();
                    resumeData.sliceSize = 1024 * 1024;

                    boolean hasComputeTimeCost = false;

                    if (isResumeUploadVideo()) {
                        resumeData.uploadId = uploadId;
                    } else {
                        hasComputeTimeCost = true;
                        InitMultipartUploadRequest initMultipartUploadRequest = new InitMultipartUploadRequest(cosBucket, cosVideoPath);
                        initMultipartUploadRequest.isSupportAccelerate(isOpenCosAcc);
//                        initMultipartUploadRequest.setSign(reqTime/1000L - localTimeAdvance, cosExpiredTime); // 5.4.10 升级到 5.4.20 该API被弃用， 以key时间过期为准 CosXmlService 中设置
                        // 用HttpTaskMetrics统计耗时
                        TXHttpTaskMetrics metrics = new TXHttpTaskMetrics();
                        initMultipartUploadRequest.attachMetrics(metrics);
                        InitMultipartUploadResult initMultipartUploadResult = cosService.initMultipartUpload(initMultipartUploadRequest);
                        // initMultipartUpload 之后可以获取到耗时
                        recvRspTimeCost = metrics.getRecvRspTimeCost();
                        tcpConenctionTimeCost = metrics.getTCPConnectionTimeCost();
                        uploadId = initMultipartUploadResult.initMultipartUpload.uploadId;
                        setResumeData(uploadInfo.getFilePath(), vodSessionKey, uploadId);
                        resumeData.uploadId = uploadId;
                    }

                    cosUploadHelper = new UploadService(cosService, resumeData);
                    cosUploadHelper.setProgressListener(new CosXmlProgressListener() {
                        @Override
                        public void onProgress(long progress, long max) {
                            if (uploadInfo.isNeedCover()) {
                                max += uploadInfo.getCoverFileSize();
                            }

                            if (!realProgressFired){
                                stopTimer();        //cos上传开始有进度回来，停掉开始虚拟进度回调
                                realProgressFired = true;
                            }

                            if (progress >= max) {
                                virtualPercent = 100 - VIRTUAL_TOTAL_PERCENT;
                                startTimer();   //上传完成，启动结束虚拟进度
                            } else {
                                notifyUploadProgress(progress*(100 - 2 * VIRTUAL_TOTAL_PERCENT)/100 + VIRTUAL_TOTAL_PERCENT*max/100, max);
                            }
                        }
                    });
                    cosUploadHelper.setSign(reqTime/1000L - localTimeAdvance, cosExpiredTime);
                    cosUploadHelper.isSupportAccelerate(isOpenCosAcc);

                    TXOnGetHttpTaskMetrics onGetHttpTaskMetrics = null;
                    // 如果是没有init的话，是还没有统计到首次包连接；那么需要给Service添加监听
                    if (!hasComputeTimeCost) {
                        onGetHttpTaskMetrics = new TXOnGetHttpTaskMetrics();
                        cosUploadHelper.setOnGetHttpTaskMetrics(onGetHttpTaskMetrics);
                    }
                    result = cosUploadHelper.resume(resumeData);
                    // 同步得到result后，那么就可以获取到连接的耗时了
                    if (onGetHttpTaskMetrics != null) {
                        tcpConenctionTimeCost = onGetHttpTaskMetrics.getTCPConnectionTimeCost();
                        recvRspTimeCost = onGetHttpTaskMetrics.getRecvRspTimeCost();
                    }
                    String requestId = getRequestId(result);
                    //分片上传完成之后清空本地缓存的断点续传信息
                    setResumeData(uploadInfo.getFilePath(), "", "");
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, 0, 0, "", "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", requestId, tcpConenctionTimeCost, recvRspTimeCost);

                    Log.w(TAG,result.accessUrl);
                    Log.i(TAG, "uploadCosVideo finish:  cosBucket " + cosBucket + " cosVideoPath: " + cosVideoPath + "  path: " + uploadInfo.getFilePath() + "  size: " + uploadInfo.getFileSize());

                    startUploadCoverFile(result);
                } catch (CosXmlClientException e) {
                    Log.w(TAG,"CosXmlClientException = " + e.getMessage());
                    //网络中断导致的
                    if (!TVCUtils.isNetworkAvailable(context)) {
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error: network unreachable");
                    } else if (!cancleFlag) { //其他错误，非主动取消
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error:" + e.getMessage());
                        setResumeData(uploadInfo.getFilePath(), "", "");
                    }

                    int errCode = TVCConstants.ERR_UPLOAD_VIDEO_FAILED;
                    if (cancleFlag && e.getMessage().contains("request is cancelled by manual pause")) {
                        busyFlag = false;
                        cancleFlag = false;
                        errCode = TVCConstants.ERR_USER_CANCEL;
                        notifyUploadFailed(TVCConstants.ERR_USER_CANCEL, "request is cancelled by manual pause");
                    }
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, errCode, 0, String.valueOf(e.errorCode), "CosXmlClientException:" + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "","", 0,0);
                } catch (CosXmlServiceException e) {
                    Log.w(TAG,"CosXmlServiceException =" + e.toString());
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_VIDEO_FAILED, 0, e.getErrorCode(), "CosXmlServiceException:" + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", e.getRequestId(), tcpConenctionTimeCost, recvRspTimeCost);
                    // 临时密钥过期，重新申请一次临时密钥，不中断上传
                    if(e.getErrorCode().equalsIgnoreCase("RequestTimeTooSkewed")) {
                        getCosUploadInfo(uploadInfo, vodSessionKey);
                    } else {
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error:" + e.getMessage());
                        setResumeData(uploadInfo.getFilePath(), "", "");
                    }
                } catch (Exception e) {
                    Log.w(TAG,"Exception =" + e.toString());
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_VIDEO_FAILED, 0, "Exception", "HTTP Code:" + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
                    notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error:" + e.getMessage());
                    setResumeData(uploadInfo.getFilePath(), "", "");
                }
            }
        }.start();
    }

    // 解析cos上传视频返回信息
    private void startFinishUploadUGC(CosXmlResult result) {
        String strAccessUrl = result.accessUrl;
        Log.i(TAG, "startFinishUploadUGC: " + strAccessUrl);

        reqTime = System.currentTimeMillis();

        // 第三步 上传结束
        ugcClient.finishUploadUGC(domain, customKey, vodSessionKey, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "FinishUploadUGC: fail" + e.toString());
                notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, e.toString());

                txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, 1, "", e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0,0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, "HTTP Code:" + response.code());
                    Log.e(TAG, "FinishUploadUGC->http code: " + response.code());

                    txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, response.code(), "","HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

                    throw new IOException("" + response);
                } else {
                    Log.i(TAG, "FinishUploadUGC Suc onResponse body : " + response.body().toString());
                    parseFinishRsp(response.body().string());
                }
            }
        });
    }


    // 解析结束上传返回信息.
    private void parseFinishRsp(String rspString) {
        Log.i(TAG, "parseFinishRsp: " + rspString);
        if (TextUtils.isEmpty(rspString)) {
            Log.e(TAG, "parseFinishRsp->response is empty!");
            notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, "finish response is empty");

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, 2, "", "finish response is empty", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),"", "", 0, 0);

            return;
        }
        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            String message = jsonRsp.optString("message", "");
            if (0 != code) {
                notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, code + "|" + message);

                txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, code, "",code + "|" + message, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),"", "", 0, 0);

                return;
            }
            JSONObject dataRsp = jsonRsp.getJSONObject("data");
            String coverUrl = "";
            if (uploadInfo.isNeedCover()) {
                JSONObject coverObj = dataRsp.getJSONObject("cover");
                coverUrl = coverObj.getString("url");
                if (enableHttps) {
                    coverUrl = coverUrl.replace("http", "https");
                }
            }
            JSONObject videoObj = dataRsp.getJSONObject("video");
            String playUrl = videoObj.getString("url");
            if (enableHttps) {
                playUrl = playUrl.replace("http", "https");
            }
            videoFileId = dataRsp.getString("fileId");
            notifyUploadSuccess(videoFileId, playUrl, coverUrl);

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, 0, 0, "", "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), videoFileId, "",0 ,0);

            Log.d(TAG, "playUrl:" + playUrl);
            Log.d(TAG, "coverUrl: " + coverUrl);
            Log.d(TAG, "videoFileId: " + videoFileId);
        } catch (JSONException e) {
            notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, e.toString());

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED,3, "", e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
        }
    }


    /**
     * 数据上报
     * @param reqType：请求类型，标识是在那个步骤
     * @param errCode：错误码
     * @param vodErrCode：点播返回的错误码
     * @param cosErrCode：COS上传的错误码，字符串
     * @param errMsg：错误详细信息，COS的错误把requestId拼在错误信息里带回
     * @param reqTime：请求时间
     * @param reqTimeCost：耗时，单位ms
     * @param fileSize :文件大小
     * @param fileType :文件类型
     * @param fileId :上传完成后点播返回的fileid
     */
    void txReport(int reqType, int errCode, int vodErrCode,  String cosErrCode, String errMsg, long reqTime, long reqTimeCost, long fileSize, String fileType, String fileName, String fileId, String cosRequestId, long cosTcpConnTimeCost, long cosRecvRespTimeCost) {
        reportInfo.reqType = reqType;
        reportInfo.errCode = errCode;
        reportInfo.errMsg = errMsg;
        reportInfo.reqTime = reqTime;
        reportInfo.reqTimeCost = reqTimeCost;
        reportInfo.fileSize = fileSize;
        reportInfo.fileType = fileType;
        reportInfo.fileName = fileName;
        reportInfo.fileId = fileId;
        reportInfo.appId = userAppId;
        reportInfo.vodErrCode = vodErrCode;
        reportInfo.cosErrCode = cosErrCode;
        reportInfo.cosRegion = uploadRegion;
        if (reqType == TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD) {
            reportInfo.useHttpDNS = TXUGCPublishOptCenter.getInstance().useHttpDNS(cosHost)? 1: 0;
            reportInfo.reqServerIp = cosIP;
            reportInfo.tcpConnTimeCost = cosTcpConnTimeCost;
            reportInfo.recvRespTimeCost = cosRecvRespTimeCost;
            reportInfo.requestId = cosRequestId == null ? "" : cosRequestId;
        } else {
            reportInfo.useHttpDNS = TXUGCPublishOptCenter.getInstance().useHttpDNS(TVCConstants.VOD_SERVER_HOST)? 1: 0;
            reportInfo.reqServerIp = ugcClient.getServerIP();
            reportInfo.tcpConnTimeCost = ugcClient.getTcpConnTimeCost();
            reportInfo.recvRespTimeCost = ugcClient.getRecvRespTimeCost();
            reportInfo.requestId = "";
        }
        reportInfo.useCosAcc = isOpenCosAcc? 1: 0;
        reportInfo.reportId = customKey;
        reportInfo.reqKey = String.valueOf(uploadInfo.getFileLastModifyTime()) + ";" + String.valueOf(initReqTime);
        reportInfo.vodSessionKey = vodSessionKey;
        UGCReport.getInstance(context).addReportInfo(reportInfo);

        if ((errCode == 0 && reqType == TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT) || errCode != 0) {
            UGCReport.ReportInfo dauReportInfo = new UGCReport.ReportInfo(reportInfo);
            dauReportInfo.reqType = TVCConstants.UPLOAD_EVENT_DAU;
            UGCReport.getInstance(context).addReportInfo(dauReportInfo);
        }
    }

    // 断点续传
    // 本地保存 filePath --> <session, uploadId, expireTime> 的映射集合，格式为json
    // session的过期时间是1天
    private void getResumeData(String filePath) {
        vodSessionKey = null;
        uploadId = null;
        fileLastModTime = 0;
        if (TextUtils.isEmpty(filePath) || enableResume == false) {
            return;
        }

        if (mSharedPreferences != null && mSharedPreferences.contains(filePath)) {
            try {
                JSONObject json = new JSONObject(mSharedPreferences.getString(filePath, ""));
                long expiredTime = json.optLong("expiredTime", 0);
                if (expiredTime > System.currentTimeMillis() / 1000) {
                    vodSessionKey = json.optString("session", "");
                    uploadId = json.optString("uploadId", "");
                    fileLastModTime = json.optLong("fileLastModTime", 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return;
    }

    private void setResumeData(String filePath, String vodSessionKey, String uploadId) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        if (mSharedPreferences != null) {
            try {
                // vodSessionKey、uploadId为空就表示删掉该记录
                String itemPath = filePath;
                if ( TextUtils.isEmpty(vodSessionKey) || TextUtils.isEmpty(uploadId)) {
                    mShareEditor.remove(itemPath);
                    mShareEditor.commit();
                } else {
                    String comment = "";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("session", vodSessionKey);
                    jsonObject.put("uploadId", uploadId);
                    jsonObject.put("expiredTime", System.currentTimeMillis() / 1000 + 24 * 60 * 60);
                    jsonObject.put("fileLastModTime", uploadInfo.getFileLastModifyTime());
                    comment = jsonObject.toString();
                    mShareEditor.putString(itemPath, comment);
                    mShareEditor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 视频是否走断点续传
    public boolean isResumeUploadVideo() {
        if (enableResume
                && !TextUtils.isEmpty(uploadId)
                && uploadInfo != null && fileLastModTime != 0 && fileLastModTime == uploadInfo.getFileLastModifyTime()) {
            return true;
        }
        return false;
    }

    public void updateSignature(String signature) {
        if (ugcClient != null) {
            ugcClient.updateSignature(signature);
        }
    }

    public Bundle getStatusInfo() {
        Bundle b = new Bundle();
        b.putString("reqType",String.valueOf(reportInfo.reqType));
        b.putString("errCode",String.valueOf(reportInfo.errCode));
        b.putString("errMsg",reportInfo.errMsg);
        b.putString("reqTime",String.valueOf(reportInfo.reqTime));
        b.putString("reqTimeCost",String.valueOf(reportInfo.reqTimeCost));
        b.putString("fileSize",String.valueOf(reportInfo.fileSize));
        b.putString("fileType",reportInfo.fileType);
        b.putString("fileName",reportInfo.fileName);
        b.putString("fileId",reportInfo.fileId);
        b.putString("appId",String.valueOf(reportInfo.appId));
        b.putString("reqServerIp",reportInfo.reqServerIp);
        b.putString("reportId",reportInfo.reportId);
        b.putString("reqKey",reportInfo.reqKey);
        b.putString("vodSessionKey",reportInfo.vodSessionKey);

        b.putString("cosRegion",reportInfo.cosRegion);
        b.putInt("vodErrCode",reportInfo.vodErrCode);
        b.putString("cosErrCode",reportInfo.cosErrCode);
        b.putInt("useHttpDNS",reportInfo.useHttpDNS);
        b.putInt("useCosAcc",reportInfo.useCosAcc);
        b.putLong("tcpConnTimeCost",reportInfo.tcpConnTimeCost);
        b.putLong("recvRespTimeCost",reportInfo.recvRespTimeCost);
        return b;
    }

    public void setAppId(int appId) {
        this.userAppId = appId;
    }
}
