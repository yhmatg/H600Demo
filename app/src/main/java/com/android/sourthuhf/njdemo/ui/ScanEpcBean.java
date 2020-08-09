package com.android.sourthuhf.njdemo.ui;

import java.util.Objects;

public class ScanEpcBean {
    private String epc;
    private boolean isWrite;
    private String tid;

    public ScanEpcBean(String epc, boolean isWrite, String tid) {
        this.epc = epc;
        this.isWrite = isWrite;
        this.tid = tid;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public boolean isWrite() {
        return isWrite;
    }

    public void setWrite(boolean write) {
        isWrite = write;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanEpcBean)) return false;
        ScanEpcBean that = (ScanEpcBean) o;
        return Objects.equals(getTid(), that.getTid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTid());
    }
}
