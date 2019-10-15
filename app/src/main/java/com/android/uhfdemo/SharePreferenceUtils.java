package com.android.uhfdemo;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtils {
    private final SharedPreferences mPreferences;
    private volatile static SharePreferenceUtils INSTANCE = null;

    private SharePreferenceUtils() {
        mPreferences = UhfApplication.getInstance().getSharedPreferences("my_shared_preference", Context.MODE_PRIVATE);
    }

    public static SharePreferenceUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (SharePreferenceUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SharePreferenceUtils();
                }
            }
        }
        return INSTANCE;
    }

    public void setFilterData(String data) {
        mPreferences.edit().putString("filter_data", data).apply();
    }

    public String getFilterData() {
        return mPreferences.getString("filter_data", "");
    }

    public void setFilterStartArea(String startArea) {
        mPreferences.edit().putString("filter_start_area", startArea).apply();
    }

    public String getFilterStartArea() {
        return mPreferences.getString("filter_start_area", "32");
    }

    public void setFilterLength(String length) {
        mPreferences.edit().putString("filter_length", length).apply();
    }

    public String getFilterLength() {
        return mPreferences.getString("filter_length", "96");
    }

    public void setOutput(int output) {
        mPreferences.edit().putInt("out_put", output).apply();
    }

    public int  getOutput() {
        return mPreferences.getInt("out_put", 5);
    }

    public void setFrequence(int frequence) {
        mPreferences.edit().putInt("frequence", frequence).apply();
    }

    public int  getfrequence() {
        return mPreferences.getInt("frequence", 0);
    }

    public void setWorkMode(int workMode) {
        mPreferences.edit().putInt("work_mode", workMode).apply();
    }

    public int  getWorkMode() {
        return mPreferences.getInt("work_mode", 0);
    }
}

