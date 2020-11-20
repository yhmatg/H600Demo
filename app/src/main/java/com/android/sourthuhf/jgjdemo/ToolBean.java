package com.android.sourthuhf.jgjdemo;

import java.util.Objects;

public class ToolBean {
    private String epc;
    private String code;
    private String name;

    public ToolBean(String epc, String code, String name) {
        this.epc = epc;
        this.code = code;
        this.name = name;
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
