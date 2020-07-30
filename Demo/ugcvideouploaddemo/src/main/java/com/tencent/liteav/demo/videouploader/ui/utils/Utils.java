package com.tencent.liteav.demo.videouploader.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    /**
     * 生成编辑后输出视频路径
     */
    public static String generateVideoPath(Context context) {
        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir == null) {
            return null;
        }
        String outputPath = sdcardDir + File.separator + Constants.DEFAULT_MEDIA_PACK_FOLDER;
        File outputFolder = new File(outputPath);

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        String current = String.valueOf(System.currentTimeMillis() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = sdf.format(new Date(Long.valueOf(current + "000")));
        String saveFileName = String.format("TXVideo_%s.mp4", time);
        return outputFolder + "/" + saveFileName;
    }

    public static String getMD5Encryption(String originString) {
        String result = "";
        if (originString != null) {
            try {
                // 指定加密的方式为MD5
                MessageDigest md = MessageDigest.getInstance("MD5");
                // 进行加密运算
                byte bytes[] = md.digest(originString.getBytes());
                StringBuilder sb = new StringBuilder(40);
                for (byte b : bytes) {
                    if ((b & 0xff) >> 4 == 0) {
                        sb.append("0").append(Integer.toHexString(b & 0xff));
                    } else {
                        sb.append(Integer.toHexString(b & 0xff));
                    }
                }
                result = sb.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
