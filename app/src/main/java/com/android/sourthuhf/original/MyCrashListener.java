package com.android.sourthuhf.original;

import android.content.Context;
import android.widget.Toast;

import com.xuexiang.xlog.crash.ICrashHandler;
import com.xuexiang.xlog.crash.OnCrashListener;

public class MyCrashListener implements OnCrashListener {
    @Override
    public void onCrash(Context context, ICrashHandler crashHandler, Throwable throwable) {
        Toast.makeText(context, "程序异常日志路径：/storage/emulated/0/Android/data/com.android.uhfdemo/cache/crash_log", Toast.LENGTH_LONG).show();
        crashHandler.setIsHandledCrash(true);
    }
}
