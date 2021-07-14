package com.tencent.liteav.trtcdemo.model.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

public class SharedPreferenceUtils {
    private SharedPreferences           mSharedPreferences;
    private SharedPreferences.Editor    mEditor;
    private static final String         FILE_NAME = "TRTC_SHARED_PARAMS";

    public interface KEY{
        public static final String LAST_INPUT_ROOMID = "last_input_room_id";
    }

    private static volatile SharedPreferenceUtils instance;

    public static SharedPreferenceUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SharedPreferenceUtils.class) {
                if (instance == null) {
                    instance = new SharedPreferenceUtils(context);
                }
            }
        }
        return instance;
    }

    private SharedPreferenceUtils(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void put(String key, Object object) {
        if (object instanceof String) {
            mEditor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            mEditor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mEditor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            mEditor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            mEditor.putLong(key, (Long) object);
        } else {
            mEditor.putString(key, object.toString());
        }
        mEditor.commit();
    }

    /**
     * 获取保存的数据
     */
    @Nullable
    public Object getSharedPreference(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return mSharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return mSharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return mSharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return mSharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return mSharedPreferences.getString(key, null);
        }
    }

    public void remove(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }

    public void clear() {
        mEditor.clear();
        mEditor.commit();
    }

    public Boolean contain(String key) {
        return mSharedPreferences.contains(key);
    }

    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }
}
