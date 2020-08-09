package com.android.sourthuhf.njdemo.ui;

import java.util.Objects;

public class WriteEpcBean {
    private String epc;
    private int sn;
    private boolean isWrite;
    //0未上报 1写入成功 2 写入失败
    private int reportStatus;
    private String tid;

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

    public int getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(int reportStatus) {
        this.reportStatus = reportStatus;
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
        if (!(o instanceof WriteEpcBean)) return false;
        WriteEpcBean epcBean = (WriteEpcBean) o;
        return getEpc().equals(epcBean.getEpc());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEpc());
    }

}
