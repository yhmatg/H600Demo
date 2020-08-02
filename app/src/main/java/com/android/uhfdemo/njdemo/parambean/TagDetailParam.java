package com.android.uhfdemo.njdemo.parambean;

import com.android.uhfdemo.njdemo.http.RetrofitClient;
import com.android.uhfdemo.njdemo.http.WmsApi;

public class TagDetailParam {
    private String flg;
    private String tagnumber;

    public TagDetailParam(String flg, String tagnumber) {
        this.flg = flg;
        this.tagnumber = tagnumber;
    }

    public String getFlg() {
        return flg;
    }

    public void setFlg(String flg) {
        this.flg = flg;
    }

    public String getTagnumber() {
        return tagnumber;
    }

    public void setTagnumber(String tagnumber) {
        this.tagnumber = tagnumber;
    }
}
