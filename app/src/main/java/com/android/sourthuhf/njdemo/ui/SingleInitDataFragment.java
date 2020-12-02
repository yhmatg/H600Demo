package com.android.sourthuhf.njdemo.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.original.BaseFragment;
import com.android.sourthuhf.original.MainActivity;
import com.android.sourthuhf.R;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.parambean.WriteTagInfoParam;
import com.android.sourthuhf.njdemo.parambean.WriteTagResultParam;
import com.android.sourthuhf.njdemo.responsebean.LableReportBean;
import com.android.sourthuhf.njdemo.responsebean.WriteTagInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class SingleInitDataFragment extends BaseFragment {
    @BindView(R.id.bt_k)
    Button mButtonK;
    @BindView(R.id.bt_t)
    Button mButtonT;
    @BindView(R.id.tv_current_box)
    TextView mCurrentBox;
    @BindView(R.id.rv_write_epcs)
    RecyclerView mListView;
    @BindView(R.id.tv_status)
    TextView writeStatus;
    private ArrayList<WriteEpcBean> tagList = new ArrayList<>();
    private WriteEpcItemAdapter adapter;
    MainActivity mainActivity;
    Driver mDriver;
    //写标签
    ImageView spinImg;
    TextView identify;
    private boolean loopFlag = false;
    Handler handler;
    List<String> scanEpcs = new ArrayList<>();
    private Animation anim1;
    private WriteEpcBean currentWrite;
    private Toast mTost;
    private String hexEpcode;
    //写入成功的标签
    List<String> writeEpcs = new ArrayList<>();
    @Override
    protected void initEventAndData() {
        mTost = Toast.makeText(mainActivity, "", Toast.LENGTH_SHORT);
        adapter = new WriteEpcItemAdapter(tagList, mainActivity);
        mListView.setLayoutManager(new LinearLayoutManager(mainActivity));
        mListView.addItemDecoration(new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL));
        mListView.setAdapter(adapter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                    handleEpc((String) msg.obj);
            }
        };
        rotateAnim1();
    }

    private void rotateAnim1() {
        anim1 = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim1.setFillAfter(true); // 设置保持动画最后的状态
        anim1.setDuration(1000); // 设置动画时间
        anim1.setRepeatCount(Animation.INFINITE);//设置动画重复次数 无限循环
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setRepeatMode(Animation.RESTART);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_singleinit_layout;
    }

    @OnClick({R.id.bt_k, R.id.bt_t})
    void performClick(View view) {
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (view.getId()) {
            case R.id.bt_k:
                mCurrentBox.setText("当前选项：框");
                labelWrite(new WriteTagInfoParam("K", "1"));
                imm.hideSoftInputFromWindow(mainActivity.getCurrentFocus().getWindowToken(), 0);
                break;
            case R.id.bt_t:
                mCurrentBox.setText("当前选项：托");
                imm.hideSoftInputFromWindow(mainActivity.getCurrentFocus().getWindowToken(), 0);
                labelWrite(new WriteTagInfoParam("T", "1"));
                break;
        }
    }

    public void labelWrite(WriteTagInfoParam infoParam) {
        RetrofitClient.getInstance().create(WmsApi.class).labelWrite(infoParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<WriteTagInfoBean>() {

                    @Override
                    public void onNext(WriteTagInfoBean writeTagInfoBean) {
                        if ("0000000".equals(writeTagInfoBean.getRtnCode())) {
                            tagList.clear();
                            for (int i = 0; i < writeTagInfoBean.getTagnumber().size(); i++) {
                                WriteEpcBean writeEpcBean = new WriteEpcBean(writeTagInfoBean.getTagnumber().get(i), i, false);
                                tagList.add(writeEpcBean);
                                hexEpcode = asciiToHex(writeTagInfoBean.getTagnumber().get(i));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        mButtonT.setEnabled(false);
                        mButtonK.setEnabled(false);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void handleEpc(String epc) {
        Log.e("InitDataFragment", "all data ===" + epc);
        int Hb = 0;
        int Lb = 0;
        int rssi = 0;
        String[] tmp = new String[3];
        HashMap<String, String> temp = new HashMap<>();
        String text = epc.substring(4);
        String len = epc.substring(0, 2);
        int epclen = (Integer.parseInt(len, 16) / 8) * 4;
        //tid
        tmp[0] = text.substring(epclen, text.length() - 6);
        //epc
        tmp[1] = text.substring(0, epclen);
        //rssi
        tmp[2] = text.substring(text.length() - 6, text.length() - 2);

        if (4 != tmp[2].length()) {
            tmp[2] = "0000";
        } else {
            Hb = Integer.parseInt(tmp[2].substring(0, 2), 16);
            Lb = Integer.parseInt(tmp[2].substring(2, 4), 16);
            rssi = ((Hb - 256 + 1) * 256 + (Lb - 256)) / 10;
        }
        if (scanEpcs.size() == 0) {
            scanEpcs.add(tmp[1]);
        }
        if (scanEpcs.size() == 1) {
            writeStatus.setText("开始写入标签");
            stopInventory();
            int reslut = writeEpcTag(scanEpcs.get(0), hexEpcode);
            if (0 == reslut) {
                if(writeEpcs.size() == 0 ){
                    writeEpcs.add(tmp[1]);
                    writeStatus.setText("第一个标签写入成功");
                }else if(writeEpcs.size() == 1 && !writeEpcs.contains(tmp[1])){
                    writeEpcs.add(tmp[1]);
                    writeStatus.setText("第二个标签写入成功");
                    WriteTagResultParam writeTagResultParam = new WriteTagResultParam();
                    ArrayList<String> sucList = new ArrayList<>();
                    ArrayList<String> errList = new ArrayList<>();
                    sucList.add(hexEpcode);
                    writeTagResultParam.setOkboxs(sucList);
                    writeTagResultParam.setErrboxs(errList);
                    reportWriteResult(writeTagResultParam);
                }
            } else {
                if(writeEpcs.size() == 0){
                    writeStatus.setText("第一个标签写入失败");
                }else if(writeEpcs.size() == 1){
                    writeStatus.setText("第二个标签写入失败");
                }
            }
        }
    }


    //以epc作为锁定条件，单个写
    public int writeEpcTag(String selectEpc, String epcData) {
        //密码
        String passWard = "00000000";
        //选中区域 epc:1
        int selectArea = 1;
        //选中标签起始地址
        int selectStartAdr = 32;
        //选中标签长度
        int selectLength = 96;
        //selectEpc 选中标签epc数据
        //写入区域 epc:1
        int writeArea = 1;
        //写入标签起始地址
        int writeStartAdr = 2;
        //写入标签长度
        int writeLength = 6;
        //epcData 要写入的epc数据
        int writeResult = mDriver.Write_Data_Tag(passWard, selectArea, selectStartAdr, selectLength, selectEpc, writeArea, writeStartAdr, writeLength, epcData);
        return writeResult;
    }

    public String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }

    public void reportWriteResult(WriteTagResultParam resultParam) {
        RetrofitClient.getInstance().create(WmsApi.class).reportWriteResult(resultParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<LableReportBean>() {
                    @Override
                    public void onNext(LableReportBean lableReportBean) {
                        if ("0000000".equals(lableReportBean.getRtnCode())) {
                            writeEpcs.clear();
                            currentWrite.setWrite(true);
                            adapter.notifyDataSetChanged();
                            mButtonT.setEnabled(true);
                            mButtonK.setEnabled(true);
                            mTost.setText("写标签上报成功");
                            writeStatus.setText("请写入下一个托盘");
                            mTost.show();
                        } else {
                            String errMes = "写标签上报失败 " + (lableReportBean.getErrorMsg() == null ? "" : lableReportBean.getErrorMsg());
                            mTost.setText(errMes);
                            mTost.show();
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

    public void startInventory() {
        try {
            if (!loopFlag) {
                loopFlag = true;
                scanEpcs.clear();
                mDriver.readMore();
                new TagThread().start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            mDriver.stopRead();
        }
    }

    public void startStopScanning() {
        if (tagList.size() == 0) {
            mTost.setText("请先获取要写入标签数据");
            mTost.show();
            return;
        }
        try {
            if (!loopFlag) {
                startInventory();
            } else {
                stopInventory();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    class TagThread extends Thread {

        private int mBetween = 0;

        public TagThread() {

        }

        public void run() {

            while (loopFlag) {
                String[] strEpc1 = {mDriver.GetBufData()};
                String strEpc = strEpc1[0];
                if (!(strEpc == null || strEpc.length() == 0)) {
                    Message msg = handler.obtainMessage();
                    msg.obj = strEpc;
                    handler.sendMessage(msg);
                }
                try {
                    sleep(mBetween);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopInventory();
        handler.removeMessages(0);
    }

}
