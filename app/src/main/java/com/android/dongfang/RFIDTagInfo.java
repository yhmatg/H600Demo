package com.android.dongfang;

//单个标签内容bean类
public class RFIDTagInfo implements Cloneable{
    /**
     * TID
     * */
    public String tid;

    /**
     * EPC id
     * */
    public String epcID;

    /**
     * RSSI 信号值
     * 统一转换为0 ～100
     * */
    public int optimizedRSSI;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getEpcID() {
        return epcID;
    }

    public void setEpcID(String epcID) {
        this.epcID = epcID;
    }

    public int getOptimizedRSSI() {
        return optimizedRSSI;
    }

    public void setOptimizedRSSI(int optimizedRSSI) {
        this.optimizedRSSI = optimizedRSSI;
    }

    @Override
    public String toString() {
        return "RFIDTagInfo{" +
                "tid='" + tid + '\'' +
                ", epcID='" + epcID + '\'' +
                ", optimizedRSSI=" + optimizedRSSI +
                '}';
    }

    //重写clone方法
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
