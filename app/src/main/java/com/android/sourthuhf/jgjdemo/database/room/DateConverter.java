package com.android.sourthuhf.jgjdemo.database.room;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date revertDate(long value) {
        if(value==0){
            return null;
        }
        return new Date(value);
    }

    @TypeConverter
    public static long converterDate(Date value) {
        if(value==null){
            return 0l;
        }
        return value.getTime();
    }
}