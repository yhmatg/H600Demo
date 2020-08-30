package com.android.sourthuhf.njdemo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.serialport.DeviceControlSpd;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.sourthuhf.R;
import com.android.sourthuhf.ToastUtils;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.njdemo.ui.EpcBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.com.example.rfid.driver.Driver;
import cn.com.example.rfid.driver.RfidDriver;

import static android.serialport.DeviceControlSpd.PowerType.EXPAND;

public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.title_back)
    ImageView titleLeft;
    @BindView(R.id.title_content)
    TextView title;
    private Unbinder unBinder;
    private Driver driver;
    private DeviceControlSpd newUHFDeviceControl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        unBinder = ButterKnife.bind(this);
        titleLeft.setVisibility(View.GONE);
        title.setText("首页");
        initRfid();
    }

    @OnClick({R.id.bt_handle_data, R.id.bt_init_data, R.id.bt_single_initdata})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.bt_handle_data:
               startActivity(new Intent(this,HandleDataActivity.class));
                break;
            case R.id.bt_init_data:
                startActivity(new Intent(this,InitDataActivity.class));
                break;
            case R.id.bt_single_initdata:
                startActivity(new Intent(this,SingleInitDataActivity.class));
                break;
        }
    }

    private void initRfid() {
        driver = new RfidDriver();
        int[] gpios = {9, 14};
        try {
            newUHFDeviceControl = new DeviceControlSpd(EXPAND, gpios);
            newUHFDeviceControl.PowerOnDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int Status;
        Status = driver.initRFID("/dev/ttyMT0");
        if (-1000 == Status) {
            return;
        }
        String Fw_buffer;
        Fw_buffer = driver.readUM7fwOnce();

        if (Fw_buffer.equals("-1000") || Fw_buffer.equals("-1020") || Fw_buffer == null) {
            ToastUtils.showShort(R.string.device_connect_failed);
        }
        //将手持机手柄出发动作改为uhf
        SystemProperties.set("persist.sys.PistolKey", "uhf");
        UhfApplication.setDriver(driver);
        driver.Read_Tag_Mode_Set(1,false);
        UhfApplication.setDriver(driver);
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
}
