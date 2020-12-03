package com.android.sourthuhf;

import android.app.Application;

import com.android.sourthuhf.original.MyCrashListener;
import com.xuexiang.xlog.XLog;
import com.xuexiang.xlog.crash.CrashHandler;

import cn.com.example.rfid.driver.Driver;

public class UhfApplication extends Application {
    private static UhfApplication instance;
    private static Driver mDriver;
    private int currentDeviceId;
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        instance = this;
        XLog.init(this);
        CrashHandler.getInstance().setOnCrashListener(new MyCrashListener());
    }

    public static synchronized UhfApplication getInstance() {
        return instance;
    }

    public static Driver getDriver(){
        return mDriver;
    }

    public static void setDriver(Driver driver){
        mDriver = driver;
    }

    public int getCurrentDeviceId() {
        return currentDeviceId;
    }

    public void setCurrentDeviceId(int currentDeviceId) {
        this.currentDeviceId = currentDeviceId;
    }
}
