package com.android.uhfdemo.njdemo.ui;

import android.support.annotation.NonNull;

import java.util.Objects;

public class WriteEpcBean {
    private String epc;
    private int sn;
    private boolean isWrite;

    public WriteEpcBean(String epc, int sn, boolean isWrite) {
        this.epc = epc;
        this.sn = sn;
        this.isWrite = isWrite;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getSn() {
        return sn;
    }

    public void setSn(int sn) {
        this.sn = sn;
    }

    public boolean isWrite() {
        return isWrite;
    }

    public void setWrite(boolean write) {
        isWrite = write;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WriteEpcBean)) return false;
        WriteEpcBean epcBean = (WriteEpcBean) o;
        return getEpc().equals(epcBean.getEpc());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEpc());
    }

}
