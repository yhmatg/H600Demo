package com.android.sourthuhf.njdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.parambean.TagDetailParam;
import com.android.sourthuhf.njdemo.responsebean.TagDetailBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
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
                        if("0000000".equals(tagDetailBean.getRtnCode())){
                            mReqName.setText(tagDetailBean.getRecipients() == null ? "" :tagDetailBean.getRecipients());
                            mTypeName.setText(tagDetailBean.getPtype() == null ? "" : tagDetailBean.getPtype());
                            mFruitName.setText(tagDetailBean.getPmode() == null ? "" : tagDetailBean.getPmode());
                            mLevelName.setText(tagDetailBean.getPrule() == null ? "" : tagDetailBean.getPrule());
                        }else {
                            String errMes = "还框上报失败 " + (tagDetailBean.getErrorMsg() == null ? "" : tagDetailBean.getErrorMsg());
                            Toast.makeText(EpcItemDetailActivity.this, errMes, Toast.LENGTH_SHORT).show();
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
