package com.tencent.liteav.trtcdemo.model.bean;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class AudioEqualizationConfig {
    private static final String TAG = "AudioEqualizationConfig";

    private int gain31HZ    = 0;
    private int gain62HZ    = 0;
    private int gain125HZ   = 0;
    private int gain250HZ   = 0;
    private int gain500HZ   = 0;
    private int gain1000HZ  = 0;
    private int gain2000HZ  = 0;
    private int gain4000HZ  = 0;
    private int gain8000HZ  = 0;
    private int gain16000HZ = 0;

    public int getGain31HZ() {
        return gain31HZ;
    }

    public void setGain31HZ(int gain31HZ) {
        this.gain31HZ = gain31HZ;
    }

    public int getGain62HZ() {
        return gain62HZ;
    }

    public void setGain62HZ(int gain62HZ) {
        this.gain62HZ = gain62HZ;
    }

    public int getGain125HZ() {
        return gain125HZ;
    }

    public void setGain125HZ(int gain125HZ) {
        this.gain125HZ = gain125HZ;
    }

    public int getGain250HZ() {
        return gain250HZ;
    }

    public void setGain250HZ(int gain250HZ) {
        this.gain250HZ = gain250HZ;
    }

    public int getGain500HZ() {
        return gain500HZ;
    }

    public void setGain500HZ(int gain500HZ) {
        this.gain500HZ = gain500HZ;
    }

    public int getGain1000HZ() {
        return gain1000HZ;
    }

    public void setGain1000HZ(int gain1000HZ) {
        this.gain1000HZ = gain1000HZ;
    }

    public int getGain2000HZ() {
        return gain2000HZ;
    }

    public void setGain2000HZ(int gain2000HZ) {
        this.gain2000HZ = gain2000HZ;
    }

    public int getGain4000HZ() {
        return gain4000HZ;
    }

    public void setGain4000HZ(int gain4000HZ) {
        this.gain4000HZ = gain4000HZ;
    }

    public int getGain8000HZ() {
        return gain8000HZ;
    }

    public void setGain8000HZ(int gain8000HZ) {
        this.gain8000HZ = gain8000HZ;
    }

    public int getGain16000HZ() {
        return gain16000HZ;
    }

    public void setGain16000HZ(int gain16000HZ) {
        this.gain16000HZ = gain16000HZ;
    }

    public JSONArray getJSONArray() {
        try {
            return new JSONArray("[" + gain31HZ + "," + gain62HZ + "," + gain125HZ + "," + gain250HZ + "," + gain500HZ
                    + "," + gain1000HZ + "," + gain2000HZ + "," + gain4000HZ + "," + gain8000HZ + "," + gain16000HZ
                    + "]");
        } catch (JSONException e) {
            Log.e(TAG, "getJSONArray JSONException");
        }
        return null;
    }
}
