package com.tencent.liteav.demo.videoediter.bgm.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.tencent.liteav.demo.videoediter.common.utils.TCUtils;
import com.tencent.liteav.demo.videoediter.common.utils.TCBGMInfo;

import java.util.ArrayList;

public class TCMusicManager {
    private static final String TAG = "TCBGMRecordManager";
    private static TCMusicManager sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;

    public static TCMusicManager getInstance(Context context) {
        if (sInstance == null)
            sInstance = new TCMusicManager(context);
        return sInstance;
    }

    private TCMusicManager(Context context) {
        mContext = context.getApplicationContext();
        mContentResolver = context.getApplicationContext().getContentResolver();
    }

    public ArrayList<TCBGMInfo> getAllMusic() {
        ArrayList<TCBGMInfo> musicList = new ArrayList<>();
        String[] mediaColumns = new String[]{
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DURATION
        };
        Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                TCBGMInfo info = new TCBGMInfo();
                info.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                if (info.getDuration() <= 0) {
                    continue;
                }
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                if (!path.endsWith(".mp3") && !path.endsWith(".m4a")) {// 仅支持mp3或M4A
                    continue;
                }
                info.setPath(path);
                info.setSongName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                info.setSingerName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                info.setFormatDuration(TCUtils.duration(info.getDuration()));
                Log.i(TAG, "getAllMusic: info = " + info.toString());
                musicList.add(info);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return musicList;
    }
}