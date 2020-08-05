package com.android.sourthuhf.njdemo.parambean;

public class WriteTagInfoParam {
    private String flg;
    private String num;

    public String getFlg() {
        return flg;
    }

    public void setFlg(String flg) {
        this.flg = flg;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public WriteTagInfoParam(String flg, String num) {
        this.flg = flg;
        this.num = num;
    }
}
