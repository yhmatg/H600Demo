package com.android.sourthuhf.jgjdemo.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;
import java.util.List;

@Dao
public interface ToolDao extends BaseDao<ToolBean> {
    @Query("SELECT * FROM ToolBean")
    public List<ToolBean> findAllTools();

    @Query("DELETE FROM ToolBean")
    public void deleteAllData();

}
