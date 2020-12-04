package com.android.sourthuhf.jgjdemo.database.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

@Entity
public class MaintenanceBean {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deviceId;
    private String time;
    private String name;
    //1 维保记录，0下次维保时间
    private int type;

    public MaintenanceBean(int deviceId, String time, String name, int type) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceBean)) return false;
        MaintenanceBean that = (MaintenanceBean) o;
        return getId() == that.getId() &&
                getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
