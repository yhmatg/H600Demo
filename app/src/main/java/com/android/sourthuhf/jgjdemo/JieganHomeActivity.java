package com.android.sourthuhf.jgjdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.serialport.DeviceControlSpd;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.sourthuhf.R;
import com.android.sourthuhf.ToastUtils;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.njdemo.ui.activity.HandleDataActivity;
import com.android.sourthuhf.njdemo.ui.activity.InitDataActivity;
import com.android.sourthuhf.njdemo.ui.activity.SingleInitDataActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.com.example.rfid.driver.Driver;
import cn.com.example.rfid.driver.RfidDriver;

import static android.serialport.DeviceControlSpd.PowerType.EXPAND;

public class JieganHomeActivity extends AppCompatActivity {
    @BindView(R.id.title_back)
    ImageView titleLeft;
    @BindView(R.id.title_content)
    TextView title;
    @BindView(R.id.tv_status)
    TextView status;
    @BindView(R.id.rv_tool_epcs)
    RecyclerView toolsView;
    private Unbinder unBinder;
    private Driver mDriver;
    private DeviceControlSpd newUHFDeviceControl;
    private List<ToolBean> initBeans = new ArrayList<>();
    private List<ToolBean> invBeans = new ArrayList<>();
    private HashMap<String,ToolBean> initMap = new HashMap<>();
    private TooltemAdapter mAdapter;
    private Boolean canRfid = true;
    private boolean loopFlag = false;
    Handler handler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiegan_home);
        unBinder = ButterKnife.bind(this);
        titleLeft.setVisibility(View.GONE);
        title.setText("首页");
        initData();
        initRfid();
    }

    private void initData() {
        ToolBean toolOne = new ToolBean("111111111111111111111111", "000001", "设备1");
        ToolBean toolTwo = new ToolBean("222222222222222222222222", "000002", "设备2");
        initMap.put("111111111111111111111111",toolOne);
        initMap.put("222222222222222222222222",toolTwo);
        mAdapter = new TooltemAdapter(invBeans,this);
        toolsView.setLayoutManager(new LinearLayoutManager(this));
        toolsView.setAdapter(mAdapter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                    handleEpc((String) msg.obj);

            }
        };
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
        ToolBean toolBean = initMap.get(tmp[1]);
        if(toolBean != null && !invBeans.contains(toolBean)){
            invBeans.add(toolBean);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initRfid() {
        mDriver = new RfidDriver();
        int[] gpios = {9, 14};
        try {
            newUHFDeviceControl = new DeviceControlSpd(EXPAND, gpios);
            newUHFDeviceControl.PowerOnDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int Status;
        Status = mDriver.initRFID("/dev/ttyMT0");
        if (-1000 == Status) {
            return;
        }
        String Fw_buffer;
        Fw_buffer = mDriver.readUM7fwOnce();

        if (Fw_buffer.equals("-1000") || Fw_buffer.equals("-1020") || Fw_buffer == null) {
            ToastUtils.showShort(R.string.device_connect_failed);
        }
        //将手持机手柄出发动作改为uhf
        SystemProperties.set("persist.sys.PistolKey", "uhf");
        UhfApplication.setDriver(mDriver);
        mDriver.Read_Tag_Mode_Set(1,false);
        UhfApplication.setDriver(mDriver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            newUHFDeviceControl.PowerOffDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (unBinder != null && unBinder != Unbinder.EMPTY) {
            unBinder.unbind();
            unBinder = null;
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

    public void startInventory() {
        try {
            if (!loopFlag) {
                loopFlag = true;
                invBeans.clear();
                mAdapter.notifyDataSetChanged();
                mDriver.readMore();
                new TagThread().start();
                status.setText("扫描中...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            mDriver.stopRead();
            status.setText("已停止");
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (canRfid) {
            if (mDriver != null && keyCode == KeyEvent.KEYCODE_F1) {
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
}
