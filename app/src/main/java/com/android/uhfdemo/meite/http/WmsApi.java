package com.android.uhfdemo.meite.http;


import com.android.uhfdemo.meite.parambean.ReportPara;
import com.android.uhfdemo.meite.responsebean.ReportResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WmsApi {
    //登录功能
    @POST("/api/getRFIDData")
    Observable<ReportResponse> reportData(@Body ReportPara reportPara);

}
