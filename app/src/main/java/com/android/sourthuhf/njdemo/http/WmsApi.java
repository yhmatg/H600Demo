package com.android.sourthuhf.njdemo.http;

import com.android.sourthuhf.njdemo.parambean.TagDetailParam;
import com.android.sourthuhf.njdemo.parambean.WriteTagInfoParam;
import com.android.sourthuhf.njdemo.parambean.WriteTagResultParam;
import com.android.sourthuhf.njdemo.responsebean.LableReportBean;
import com.android.sourthuhf.njdemo.responsebean.TagDetailBean;
import com.android.sourthuhf.njdemo.responsebean.WriteTagInfoBean;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WmsApi {
    //注销上报功能
    @POST("/app/labelReport")
    Observable<LableReportBean> lableReport(@Body List<String> epcs);

    //还筐上报功能
    @POST("/app/returnbasket")
    Observable<LableReportBean> returnBasket(@Body List<String> epcs);

    //写标签信息获取
    @POST("/app/labelWrite")
    Observable<WriteTagInfoBean> labelWrite(@Body WriteTagInfoParam infoParam);

    //写标签成功失败上报
    @POST("/app/reportsuccess")
    Observable<LableReportBean> reportWriteResult(@Body WriteTagResultParam resultParam);

    //查看标签详情
    @POST("/app/getcontentdetails")
    Observable<TagDetailBean> getTagDetail(@Body TagDetailParam detailParam);

}
