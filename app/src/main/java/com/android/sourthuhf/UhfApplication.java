package com.android.sourthuhf;

import android.app.Application;

import com.xuexiang.xlog.XLog;
import com.xuexiang.xlog.crash.CrashHandler;

import cn.com.example.rfid.driver.Driver;

public class UhfApplication extends Application {
    private static UhfApplication instance;
    private static Driver mDriver;
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
}
