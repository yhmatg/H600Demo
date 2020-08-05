package com.android.sourthuhf.njdemo.responsebean;

import java.util.List;

public class WriteTagInfoBean {
    private String rtnCode;
    private String errorMsg;
    private List<String> tagnumber;

    public String getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(String rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<String> getTagnumber() {
        return tagnumber;
    }

    public void setTagnumber(List<String> tagnumber) {
        this.tagnumber = tagnumber;
    }
}
