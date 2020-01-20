package com.android.dongfang;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.uhfdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.android.dongfang.IRfidDevice.RFIDAreaEnum.EPC;
import static com.android.dongfang.IRfidDevice.RFIDAreaEnum.RESERVED;
import static com.android.dongfang.IRfidDevice.RFIDAreaEnum.TID;
import static com.android.dongfang.IRfidDevice.RFIDAreaEnum.USER;

public class DfActivity extends AppCompatActivity implements RFIDCallback {

    private EsimRifidDevice esimRifidDevice;
    private Unbinder unBinder;
    @BindView(R.id.tb_init)
    Button mInit;
    @BindView(R.id.tb_close)
    Button mClose;
    @BindView(R.id.tb_single_read)
    Button mSingleRead;
    @BindView(R.id.tb_scan)
    Button mScan;
    @BindView(R.id.tb_stop_scan)
    Button mStopScan;
    @BindView(R.id.tb_write_epc)
    Button mWriteEpc;
    @BindView(R.id.tb_set_power)
    Button mSetPower;
    @BindView(R.id.read_diff_area)
    Button mReadDiffArea;
    @BindView(R.id.write_epc_by_tid)
    Button mWriteEpcByTid;
    @BindView(R.id.tb_write_user)
    Button mWriteUser;
    @BindView(R.id.tv_text)
    TextView mTestContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_layout);
        unBinder = ButterKnife.bind(this);
        Log.e("dongfang", Thread.currentThread().getName()+"");
    }

    String scanData = "";
    //回调在子线程中
    @Override
    public void onResponse(RFIDTagInfo rfidTagInfo) {
        Log.e("dongfang", "rfidTagInfo===" + rfidTagInfo);
        Log.e("dongfang", Thread.currentThread().getName()+"");
        scanData += rfidTagInfo.toString() + "\n";
        mTestContent.setText(scanData);
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTestContent.setText(scanData);
            }
        });*/
    }

    @Override
    public int onError(int reason) {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unBinder != null && unBinder != Unbinder.EMPTY) {
            unBinder.unbind();
            unBinder = null;
        }
    }

    @OnClick({R.id.tb_init, R.id.tb_close, R.id.tb_single_read, R.id.tb_scan, R.id.tb_write_epc,R.id.tb_write_user,
            R.id.tb_set_power, R.id.tb_get_power, R.id.read_diff_area, R.id.write_epc_by_tid, R.id.tb_stop_scan})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.tb_init:
                if(esimRifidDevice == null){
                    esimRifidDevice = new EsimRifidDevice();
                }
                esimRifidDevice.init();
                break;
            case R.id.tb_close:
                if (esimRifidDevice.isOpen()) {
                    esimRifidDevice.close();
                }
                break;
            case R.id.tb_single_read:
                if (esimRifidDevice.isOpen()) {
                    scanData = "";
                    RFIDTagInfo rfidTagInfo = esimRifidDevice.singleScan();
                    mTestContent.setText(rfidTagInfo.toString());
                    Log.e("dongfang", "singleRead===" + rfidTagInfo);
                }
                break;
            case R.id.tb_scan:
                if (esimRifidDevice.isOpen()) {
                    scanData = "";
                    esimRifidDevice.startScan(this);
                }
                break;
            case R.id.tb_stop_scan:
                if (esimRifidDevice.isOpen()) {
                    esimRifidDevice.stopScan();
                }
                break;
            case R.id.tb_write_epc:
                if (esimRifidDevice.isOpen()) {
                    esimRifidDevice.write("12121212");
                }
                break;
            case R.id.tb_set_power:
                if (esimRifidDevice.isOpen()) {
                    esimRifidDevice.setPower(15);
                }
                break;
            case R.id.tb_get_power:
                if (esimRifidDevice.isOpen()) {
                    int power = esimRifidDevice.getPower();
                    mTestContent.setText(power + "");
                }
                break;
            case R.id.read_diff_area:
                if (esimRifidDevice.isOpen()) {
                    String epc = esimRifidDevice.readData(EPC, 2, 6);
                    String tid = esimRifidDevice.readData(TID, 0, 6);
                    String user = esimRifidDevice.readData(USER, 0, 6);
                    String reserve1 = esimRifidDevice.readData(RESERVED, 0, 2);
                    String reserve2 = esimRifidDevice.readData(RESERVED, 2, 2);
                    Log.e("dongfang", "epc========" + epc);
                    Log.e("dongfang", "tid========" + tid);
                    Log.e("dongfang", "user=======" + user);
                    Log.e("dongfang", "reserve1======" + reserve1);
                    Log.e("dongfang", "reserve2======" + reserve2);
                }
                break;
            case R.id.write_epc_by_tid:
                if (esimRifidDevice.isOpen()) {
                    esimRifidDevice.write("E20034120136FB000DC49374","34343434");
                }
                break;
            case R.id.tb_write_user:
                if (esimRifidDevice.isOpen()) {
                    esimRifidDevice.writeUser("12121212AAAAAAAABBBBBBBB11111111CCCCCCCC");
                }
                break;
        }
    }
}
