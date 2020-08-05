package com.android.sourthuhf.njdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.parambean.TagDetailParam;
import com.android.sourthuhf.njdemo.responsebean.TagDetailBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.com.example.rfid.driver.Driver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class WriteEpcActivity extends AppCompatActivity {
    private Unbinder unBinder;
    @BindView(R.id.iv_spin)
    ImageView spinImg;
    @BindView(R.id.btn_confirm_write)
    Button confirm;
    @BindView(R.id.identify_tips)
    TextView identify;
    Driver mDriver;
    private boolean loopFlag = false;
    Handler handler;
    private Boolean canRfid = true;
    List<String> scanEpcs = new ArrayList<>();
    private Animation anim1;
    private String epcode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_layout);
        unBinder = ButterKnife.bind(this);
        mDriver = UhfApplication.getDriver();
        rotateAnim1();
        Intent intent = getIntent();
        epcode = intent.getStringExtra("epcode");
        epcode = asciiToHex(epcode);
        String typeode = intent.getStringExtra("typeode");
        getTagDetail(new TagDetailParam(typeode, epcode));
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleEpc((String) msg.obj);
            }
        };
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
    protected void onDestroy() {
        super.onDestroy();
        if (unBinder != null && unBinder != Unbinder.EMPTY) {
            unBinder.unbind();
            unBinder = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopInventory();
    }

    public void getTagDetail(TagDetailParam detailParam){
        RetrofitClient.getInstance().create(WmsApi.class).getTagDetail(detailParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<TagDetailBean>() {
                    @Override
                    public void onNext(TagDetailBean tagDetailBean) {

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
                spinImg.startAnimation(anim1);
                confirm.setEnabled(false);
                confirm.setBackground(getDrawable(R.drawable.button_background_three));
                int status = mDriver.readMore();
                new TagThread().start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            spinImg.clearAnimation();
            mDriver.stopRead();
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

    public void startStopScanning() {
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

    private void handleEpc(String epc) {
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
            identify.setText(R.string.identify_success);
            stopInventory();
            confirm.setEnabled(true);
            confirm.setBackground(getDrawable(R.drawable.button_background_two));
        }else {
            confirm.setEnabled(false);
            confirm.setBackground(getDrawable(R.drawable.button_background_three));
        }
    }

    @OnClick({R.id.btn_confirm_write})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm_write:
                if(mDriver != null){
                    String selectEpc = scanEpcs.size() > 0? scanEpcs.get(0) : "";
                    int reslut = writeEpcTag(selectEpc, epcode);
                    if(0 == reslut){
                        Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(this, "写入失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "RFID异常，请重新进入应用连接", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(canRfid){
            if (mDriver!= null && keyCode == KeyEvent.KEYCODE_F1) {
                startStopScanning();
            }
            canRfid = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        canRfid = true;
        return super.onKeyUp(keyCode, event);
    }

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
}
