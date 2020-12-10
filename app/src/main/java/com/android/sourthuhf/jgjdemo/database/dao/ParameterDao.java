package com.android.sourthuhf.jgjdemo.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.bean.ParameterBean;

import java.util.List;

@Dao
public interface ParameterDao extends BaseDao<ParameterBean> {
    @Query("SELECT * FROM ParameterBean where deviceId = :deviceId")
    public List<ParameterBean> findParamByDeviceId(int deviceId);

    @Query("DELETE FROM ParameterBean where deviceId in (:deviceId)")
    public void deleteDataByDeviceId(List<Integer> deviceId);

    @Query("DELETE FROM ParameterBean")
    public void deleteAllData();

}
