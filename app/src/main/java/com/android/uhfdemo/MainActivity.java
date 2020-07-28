package com.android.uhfdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.serialport.DeviceControlSpd;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.uhfdemo.njdemo.ui.HandleDataFragment;
import com.android.uhfdemo.njdemo.ui.InitDataFragment;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.com.example.rfid.driver.Driver;
import cn.com.example.rfid.driver.RfidDriver;

import static android.serialport.DeviceControlSpd.PowerType.EXPAND;

public class MainActivity extends AppCompatActivity {

    private Unbinder unBinder;
    @BindView(R.id.tablayout)
    TabLayout mTablayout;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private ArrayList<BaseFragment> fragments = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();

    private int RbFlag = 0;
    private Driver driver;
    private DeviceControlSpd newUHFDeviceControl;

    public static final String MAIN_SCAN = "com.spd.action.start_uhf_mainactivity";
    public static final String STOP_SCAN = "com.spd.action.stop_uhf";
    private HandleDataFragment minventoryFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unBinder = ButterKnife.bind(this);
        initRfid();
        initDataAndView();
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

    }

    private void initDataAndView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
       /* titles.add("盘存");
        titles.add("读写");
        titles.add("锁定");
        titles.add("销毁");
        titles.add("设置");
        minventoryFragment = new InventoryFragment();
        fragments.add(minventoryFragment);
        fragments.add(new ReadAndWriteFragment());
        fragments.add(new LockFragment());
        fragments.add(new DestoryFragment());
        fragments.add(new SettingsFragment());*/
        titles.add("异常处理");
        titles.add("RFID数据初始化");
        minventoryFragment = new HandleDataFragment();
        fragments.add(minventoryFragment);
        fragments.add(new InitDataFragment());
        for (int i = 0; i < titles.size(); i++) {
            mTablayout.addTab(mTablayout.newTab());
        }
        mTablayout.setupWithViewPager(mViewpager, false);
        mViewpager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments == null ? 0 : fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });
        for (int i = 0; i < titles.size(); i++) {
            mTablayout.getTabAt(i).setText(titles.get(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_firmware_version:            //Menu.FIRST对应itemid为1
                Um7Fw();
                return true;
            case R.id.action_soft_version:
                Toast.makeText(this, R.string.soft_version, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_tem:
                getTempeature();
                return true;
            case R.id.action_export:
                //todo 导出数据
                minventoryFragment.requestPermission();
                return true;
            default:
                return false;
        }
    }

    private void getTempeature() {
        float tmp = driver.Get_Temp();
        if (-1000 == tmp) {
            Toast.makeText(this, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (-1001 == tmp) {
            Toast.makeText(this, R.string.get_version_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (-1020 == tmp) {
            Toast.makeText(this, R.string.get_version_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String currentTem = getResources().getString(R.string.current_tem) + String.valueOf(tmp);
        Toast.makeText(this, currentTem, Toast.LENGTH_SHORT).show();

    }

    private void Um7Fw() {
        String Fw_buffer;
        Fw_buffer = driver.readUM7fwOnce();
        if (Fw_buffer.equals("-1000")) {
            Toast.makeText(this, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Fw_buffer.equals("-1020")) {
            Toast.makeText(this, R.string.get_version_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder fw = new StringBuilder(getDesignatedDecimalData(Fw_buffer));
        fw.insert(0, "固件版本：");
        String Sfw = fw.toString();
        Toast.makeText(this, Sfw, Toast.LENGTH_SHORT).show();

    }

    private String getDesignatedDecimalData(String hexString) {
        StringBuilder version = new StringBuilder();
        int n = 0;
        for (int i = 0; i < 3; i++) {
            int vals = Integer.parseInt(hexString.substring(n, n + 2), 16);
            String val = i < 2 ? vals + "." : vals + "";
            version.append(val);
            n += 2;
        }
        return version.toString();
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

    public Driver getDriver() {
        return driver;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_F1 == keyCode) {
            Intent intent = new Intent();
            intent.setAction(MAIN_SCAN);
            sendBroadcast(intent);
        }
        return super.onKeyDown(keyCode, event);
    }


}

