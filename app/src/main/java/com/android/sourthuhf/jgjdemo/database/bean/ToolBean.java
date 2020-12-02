package com.android.sourthuhf.jgjdemo.database.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

@Entity
public class ToolBean {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String epc;
    private String code;
    private String name;
    private int type;

    public ToolBean(String epc, String code, String name, int type) {
        this.epc = epc;
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        if (!(o instanceof ToolBean)) return false;
        ToolBean toolBean = (ToolBean) o;
        return getEpc().equals(toolBean.getEpc()) &&
                getCode().equals(toolBean.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEpc(), getCode());
    }
}
