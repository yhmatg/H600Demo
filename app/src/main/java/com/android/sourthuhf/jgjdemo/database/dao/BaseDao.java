package com.android.sourthuhf.jgjdemo.database.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertItem(T item);//插入单条数据

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertItems(List<T> items);//插入list

    @Delete
    public void deleteItem(T item);//删除

    @Delete
    public void deleteItems(List<T> items);//删除
    @Update
    public void updateItem(T item);//更新

    @Update
    public void updateItems(List<T> items);//更新

}
