package com.android.sourthuhf.jgjdemo.database.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;
import com.android.sourthuhf.jgjdemo.database.dao.MaintenanceDao;
import com.android.sourthuhf.jgjdemo.database.dao.ToolDao;

@Database(entities = {
        ToolBean.class,
        MaintenanceBean.class
}
        , version = 1)
@TypeConverters(DateConverter.class)
public abstract class BaseDb extends RoomDatabase {
    public static final String DB_NAME = "straw.db";
    private static volatile BaseDb instance;

    public static synchronized BaseDb getInstance() {
        if (instance == null) {
            instance = createDb();
        }
        return instance;
    }

    private static BaseDb createDb() {
        BaseDb build = Room.databaseBuilder(
                UhfApplication.getInstance(),
                BaseDb.class,
                DB_NAME).addCallback(new Callback() {
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

    public abstract ToolDao getToolDao();

    public abstract MaintenanceDao getMaintenanceDao();
}