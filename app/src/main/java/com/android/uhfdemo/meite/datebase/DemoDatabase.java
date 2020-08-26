package com.android.uhfdemo.meite.datebase;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.android.uhfdemo.UhfApplication;
import com.android.uhfdemo.meite.parambean.FileBean;

@Database(entities = {
        FileBean.class
}
        , version = 1)
@TypeConverters(DateConverter.class)
public abstract class DemoDatabase extends RoomDatabase {
    public static final String DB_NAME = "inventory.db";
    private static volatile DemoDatabase instance;

    public static synchronized DemoDatabase getInstance() {
        if (instance == null) {
            instance = createDb();
        }
        return instance;
    }

    private static DemoDatabase createDb() {
        DemoDatabase build = Room.databaseBuilder(
                UhfApplication.getInstance(),
                DemoDatabase.class,
                DB_NAME).addCallback(new RoomDatabase.Callback() {
            //第一次创建数据库时调用，但是在创建所有表之后调用的
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            //当数据库被打开时调用
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }
        }).allowMainThreadQueries().build();
        return build;
    }

    public abstract ReportParamDao getReportParamDao();
}