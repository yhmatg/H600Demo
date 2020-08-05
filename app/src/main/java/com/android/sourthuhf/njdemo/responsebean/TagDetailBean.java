package com.android.sourthuhf.njdemo.responsebean;

public class TagDetailBean {

    private String rtnCode;
    private String errorMsg;
    private String recipients;
    private String ptype;
    private String pmode;
    private String prule;

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

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getPmode() {
        return pmode;
    }

    public void setPmode(String pmode) {
        this.pmode = pmode;
    }

    public String getPrule() {
        return prule;
    }

    public void setPrule(String prule) {
        this.prule = prule;
    }
}
