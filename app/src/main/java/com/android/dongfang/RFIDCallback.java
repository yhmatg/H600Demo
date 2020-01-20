package com.android.dongfang;

//多次盘点的回调
public interface RFIDCallback {
    /**
     * 成功的返回
     * */
    void onResponse(RFIDTagInfo rfidTagInfo);

    /**
     * 失败的返回
     * @param reason 失败的原因
     * */
    int onError(int reason);
}
