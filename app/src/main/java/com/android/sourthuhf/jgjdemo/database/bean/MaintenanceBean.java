package com.android.sourthuhf.jgjdemo.database.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

@Entity
public class MaintenanceBean {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deviceId;
    private long time;
    private String name;
    //1 维保记录，0下次维保时间
    private int type;

    public MaintenanceBean(int deviceId, long time, String name, int type) {
        this.deviceId = deviceId;
        this.time = time;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
