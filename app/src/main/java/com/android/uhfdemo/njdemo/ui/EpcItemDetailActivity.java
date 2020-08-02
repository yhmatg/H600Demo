package com.android.uhfdemo.njdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.uhfdemo.R;
import com.android.uhfdemo.UhfApplication;
import com.android.uhfdemo.njdemo.http.RetrofitClient;
import com.android.uhfdemo.njdemo.http.WmsApi;
import com.android.uhfdemo.njdemo.parambean.TagDetailParam;
import com.android.uhfdemo.njdemo.responsebean.TagDetailBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.com.example.rfid.driver.Driver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class EpcItemDetailActivity extends AppCompatActivity {
    private Unbinder unBinder;
    @BindView(R.id.request_name)
    TextView mReqName;
    @BindView(R.id.type_nme)
    TextView mTypeName;
    @BindView(R.id.fruit_name)
    TextView mFruitName;
    @BindView(R.id.level_name)
    TextView mLevelName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epcdetail_layout);
        unBinder = ButterKnife.bind(this);
        Intent intent = getIntent();
        String epcode = intent.getStringExtra("epcode");
        String typeode = intent.getStringExtra("typeode");
        getTagDetail(new TagDetailParam(typeode,epcode));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unBinder != null && unBinder != Unbinder.EMPTY) {
            unBinder.unbind();
            unBinder = null;
        }
    }

    public void getTagDetail(TagDetailParam detailParam){
        RetrofitClient.getInstance().create(WmsApi.class).getTagDetail(detailParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<TagDetailBean>() {
                    @Override
                    public void onNext(TagDetailBean tagDetailBean) {
                        if("00000000".equals(tagDetailBean.getRtnCode())){
                            mReqName.setText(tagDetailBean.getRecipients());
                            mTypeName.setText(tagDetailBean.getPtype());
                            mFruitName.setText(tagDetailBean.getPmode());
                            mLevelName.setText(tagDetailBean.getPrule());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
