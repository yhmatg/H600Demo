package com.android.uhfdemo.njdemo.ui;

import android.support.annotation.NonNull;

import java.util.Objects;

public class EpcBean {
    private String epc;
    private int sn;
    private int count;
    private int rssi;
    private boolean isSelect;

    public EpcBean(String epc, int sn, int count, int rssi, boolean isSelect) {
        this.epc = epc;
        this.sn = sn;
        this.count = count;
        this.rssi = rssi;
        this.isSelect = isSelect;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EpcBean)) return false;
        EpcBean epcBean = (EpcBean) o;
        return getEpc().equals(epcBean.getEpc());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEpc());
    }

    @NonNull
    @Override
    public String toString() {
        return "epc:" + epc +"   ,sn:" + sn + "   ,count:" + count + "   ,rssi:" + rssi;
    }
}
