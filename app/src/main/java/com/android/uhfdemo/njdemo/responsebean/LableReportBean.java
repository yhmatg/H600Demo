package com.android.uhfdemo.njdemo.responsebean;

import com.google.gson.annotations.SerializedName;

public class LableReportBean {

    private String rtnCode;
    private String errorMsg;

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
}
