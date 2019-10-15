package com.android.uhfdemo;

import android.app.Application;

import com.xuexiang.xlog.XLog;
import com.xuexiang.xlog.crash.CrashHandler;

public class UhfApplication extends Application {
    private static UhfApplication instance;
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
}
