package com.android.sourthuhf.njdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.sourthuhf.BaseFragment;
import com.android.sourthuhf.MainActivity;
import com.android.sourthuhf.R;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.parambean.WriteTagInfoParam;
import com.android.sourthuhf.njdemo.responsebean.WriteTagInfoBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class InitDataFragment extends BaseFragment implements WriteEpcItemAdapter.WriteEpcItemClickListener {
    @BindView(R.id.bt_k)
    Button mButtonK;
    @BindView(R.id.bt_t)
    Button mButtonT;
    @BindView(R.id.tv_current_box)
    TextView mCurrentBox;
    @BindView(R.id.et_tag_num)
    EditText mEpcSum;
    @BindView(R.id.rv_write_epcs)
    RecyclerView mListView;
    private ArrayList<WriteEpcBean> tagList = new ArrayList<>();
    private WriteEpcItemAdapter adapter;
    MainActivity mainActivity;
    Driver mDriver;
    public static final String TAG = "ReadAndWriteFragment";

    @Override
    protected void initEventAndData() {
        adapter = new WriteEpcItemAdapter(tagList,mainActivity);
        adapter.setWriteClickListener(this);
        mListView.setLayoutManager(new LinearLayoutManager(mainActivity));
        mListView.addItemDecoration(new DividerItemDecoration(mainActivity,LinearLayoutManager.VERTICAL));
        mListView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_initdata_layout;
    }

    @OnClick({R.id.bt_k, R.id.bt_t})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.bt_k:
                mCurrentBox.setText("当前选项：框");
                String kStr = mEpcSum.getText().toString();
                if(kStr.isEmpty()){
                    kStr = "10";
                }
                labelWrite(new WriteTagInfoParam("K",kStr));
                break;
            case R.id.bt_t:
                mCurrentBox.setText("当前选项：托");
                String tStr = mEpcSum.getText().toString();
                if(tStr.isEmpty()){
                    tStr = "10";
                }
                labelWrite(new WriteTagInfoParam("T",tStr));
                break;
        }
    }

    public void labelWrite(WriteTagInfoParam infoParam){
        RetrofitClient.getInstance().create(WmsApi.class).labelWrite(infoParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<WriteTagInfoBean>() {

                    @Override
                    public void onNext(WriteTagInfoBean writeTagInfoBean) {
                        if("0000000".equals(writeTagInfoBean.getRtnCode())){
                            tagList.clear();
                            for (int i = 0; i < writeTagInfoBean.getTagnumber().size(); i++) {
                                WriteEpcBean writeEpcBean = new WriteEpcBean(writeTagInfoBean.getTagnumber().get(i), i, false);
                                tagList.add(writeEpcBean);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onEpcItemClick(WriteEpcBean fileBean) {
        String s = mCurrentBox.getText().toString();
        String type = "框".equals(s) ? "K" : "T";
        Intent intent = new Intent();
        intent.putExtra("epcode", fileBean.getEpc());
        intent.putExtra("typeode", type);
        intent.setClass(mainActivity, EpcItemDetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onWriteEpcClick(WriteEpcBean fileBean) {
        String s = mCurrentBox.getText().toString();
        String type = "框".equals(s) ? "K" : "T";
        Intent intent = new Intent();
        intent.putExtra("epcode", fileBean.getEpc());
        intent.putExtra("typeode", type);
        intent.setClass(mainActivity, WriteEpcActivity.class);
        startActivity(intent);
    }
}
