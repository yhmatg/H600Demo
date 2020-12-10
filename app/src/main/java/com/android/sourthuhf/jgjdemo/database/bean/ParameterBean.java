package com.android.sourthuhf.jgjdemo.database.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

@Entity
public class ParameterBean {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deviceId;
    private String performance;
    private String skill;

    public ParameterBean(int deviceId, String performance, String skill) {
        this.deviceId = deviceId;
        this.performance = performance;
        this.skill = skill;
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

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterBean)) return false;
        ParameterBean that = (ParameterBean) o;
        return getId() == that.getId() &&
                getDeviceId() == that.getDeviceId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDeviceId());
    }
}
