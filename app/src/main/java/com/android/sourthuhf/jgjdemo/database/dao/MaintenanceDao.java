package com.android.sourthuhf.jgjdemo.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;

import java.util.List;

@Dao
public interface MaintenanceDao extends BaseDao<MaintenanceBean> {
    @Query("SELECT * FROM MaintenanceBean where deviceId = :deviceId and type = :type")
    public List<MaintenanceBean> findMaintenanceByDeviceId(int deviceId,int type);

    @Query("DELETE FROM MaintenanceBean")
    public void deleteAllData();

}
