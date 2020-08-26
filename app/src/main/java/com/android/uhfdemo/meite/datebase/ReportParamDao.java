package com.android.uhfdemo.meite.datebase;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.android.uhfdemo.meite.parambean.FileBean;

import java.util.List;

@Dao
public interface ReportParamDao extends BaseDao<FileBean> {

    @Query("DELETE FROM FileBean")
    public void deleteAllData();

    @Query("SELECT * FROM FileBean where EPCID in (:epcs)")
    public List<FileBean> getDataByEpcs(List<String> epcs);

    @Query("SELECT * FROM FileBean ")
    public List<FileBean> getAllData();
}
