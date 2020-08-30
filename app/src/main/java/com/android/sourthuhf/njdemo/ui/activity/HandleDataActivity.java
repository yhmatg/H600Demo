package com.android.sourthuhf.njdemo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.Utils;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.responsebean.LableReportBean;
import com.android.sourthuhf.njdemo.ui.EpcBean;
import com.android.sourthuhf.njdemo.ui.EpcItemAdapter;
import com.android.sourthuhf.njdemo.ui.EpcItemDetailActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class HandleDataActivity extends AppCompatActivity implements EpcItemAdapter.OnItemClickListener {
    private Unbinder unBinder;
    private static final String FILE_EXTENSION = ".txt";
    private static final String INVENTORY_SUMMARY = "INVENTORY SUMMARY";
    private static final String UNIQUE_COUNT = "UNIQUE COUNT:";
    private static final String TOTAL_COUNT = "TOTAL COUNT:";
    @BindView(R.id.title_content)
    TextView title;
    @BindView(R.id.epc_count)
    TextView mEpcCount;
    @BindView(R.id.epc_sum)
    TextView mEpcSum;
    @BindView(R.id.cb_sound)
    CheckBox mSoundBox;
    @BindView(R.id.char_type)
    CheckBox mCharType;
    @BindView(R.id.rv_inv_epcs)
    RecyclerView mListView;
    @BindView(R.id.open_or_stop)
    TextView mStartOrStop;
    @BindView(R.id.clear_btn)
    TextView mClear;
    @BindView(R.id.tv_destory)
    TextView mDestory;
    @BindView(R.id.tv_return)
    TextView mReturn;
    Driver mDriver;
    private SoundPool soundPool;
    private int soundId;
    //声音提示
    private int SoundFlag = 0;
    //开始停止盘点标记
    private int flag = 0;
    //listview 需要的集合
    private ArrayList<EpcBean> tagList = new ArrayList<>();
    //存放单条读取信息
    private HashMap<String, EpcBean> hmap = new HashMap<>();
    private EpcItemAdapter adapter;
    //标签编号
    int iIndex = 0;
    //读取次数p
    int Ct = 0;
    Handler handler;
    private boolean loopFlag = false;
    Date curDate;
    private Boolean canRfid = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_handledata);
        unBinder = ButterKnife.bind(this);
        initView();
        initEventAndData();
    }

    protected void initView() {
        mDriver = UhfApplication.getDriver();
        title.setText("异常处理");
        mSoundBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SoundFlag = 1;
                } else {
                    SoundFlag = 0;
                }
            }
        });

        mCharType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }

    @OnClick({R.id.open_or_stop, R.id.clear_btn, R.id.tv_destory, R.id.tv_return,R.id.title_back})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.open_or_stop:
                startOrStopScan();
                break;
            case R.id.clear_btn:
                adapter.clearSelectedepcBeans();
                tagList.clear();
                adapter.notifyDataSetChanged();
                hmap.clear();
                iIndex = 0;
                Ct = 0;
                mEpcSum.setText("");
                mEpcCount.setText("");
                break;
            case R.id.tv_destory:
                List<EpcBean> selectedLabelEpcs = adapter.getSelectedepcBeans();
                ArrayList<String> labelEpcs = new ArrayList<>();
                for (EpcBean selectedepcBean : selectedLabelEpcs) {
                    labelEpcs.add(selectedepcBean.getEpc());
                }
                lableReport(labelEpcs);
                break;
            case R.id.tv_return:
                List<EpcBean> selectedReturnEpc = adapter.getSelectedepcBeans();
                ArrayList<String> ReturnEpcs = new ArrayList<>();
                for (EpcBean selectedepcBean : selectedReturnEpc) {
                    ReturnEpcs.add(selectedepcBean.getEpc());
                }
                returnBasket(ReturnEpcs);
                break;
            case R.id.title_back:
               finish();
                break;
        }
    }

    protected void initEventAndData() {
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundId = soundPool.load(this, R.raw.barcodebeep, 1);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
        adapter = new EpcItemAdapter(tagList, this);
        adapter.setOnItemClickListener(this);
        mListView.setAdapter(adapter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleEpc((String) msg.obj);
            }
        };
    }

    private void handleEpc(String epc) {
        Log.e("HandleDataFragment","all data ===" + epc);
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

        if (1 == SoundFlag) {
            soundPool.play(soundId, 1, 1, 0, 1, 1);
        }
        //显示读取ecp个格式，默认16进制
       /* if (!mCharType.isChecked()) {
            tmp[1] = AsciiStringToString(tmp[1]);
        }*/
        tmp[1] = Utils.AsciiStringToString(tmp[1]);
        if (hmap.containsKey(tmp[1])) {
            EpcBean epcBean = hmap.get(tmp[1]);
            if (epcBean != null) {
                int count = epcBean.getCount() + 1;
                epcBean.setCount(count);
                epcBean.setRssi(rssi);
            }
        } else {
            iIndex++;
            EpcBean epcBean = new EpcBean(tmp[1], iIndex, 1, rssi, false);
            hmap.put(tmp[1], epcBean);
            if (!tagList.contains(epcBean)) {
                tagList.add(epcBean);
            }
        }
        adapter.notifyDataSetChanged();
        Ct++;
        mEpcCount.setText(String.valueOf(tagList.size()));
        mEpcSum.setText(String.valueOf(Ct));
    }

    private void stopInventory() {

        if (loopFlag) {

            loopFlag = false;

            mDriver.stopRead();

        }
    }

    public void startOrStopScan() {
        switch (flag) {
            case 0:
                tagList.clear();
                adapter.notifyDataSetChanged();
                hmap.clear();
                iIndex = 0;
                Ct = 0;
                mEpcCount.setText("");
                mEpcSum.setText("");
                mDriver.readMore();
                new TagThread().start();
                loopFlag = true;
                curDate = new Date(System.currentTimeMillis());
                mStartOrStop.setText(R.string.scann_stop);
                flag = 1;
                break;
            case 1:
                iIndex = 0;
                stopInventory();
                mStartOrStop.setText(R.string.scann_start);
                flag = 0;
                break;
            default:
                break;
        }
    }

    @Override
    public void onEpcItemClick(EpcBean fileBean) {
        String head = fileBean.getEpc().substring(0,1);
        String type = ("K".equals(head) || "k".equals(head)) ? "K" : "T";
        Intent intent = new Intent();
        intent.putExtra("epcode", fileBean.getEpc());
        intent.putExtra("typeode", type);
        intent.setClass(this, EpcItemDetailActivity.class);
        startActivity(intent);
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

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        11);

            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        11);
            }
        } else {
            exportData();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //todo 保存数据
            exportData();
        } else {
            Toast.makeText(this, R.string.not_get_storage_permission, Toast.LENGTH_SHORT).show();
        }
    }

    public void exportData() {
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/inventory/" + getFilename();
        String content = "";
        content += INVENTORY_SUMMARY + "\n";
        content += UNIQUE_COUNT + hmap.size() + "\n";
        content += TOTAL_COUNT + Ct + "\n";
        for (int i = 0; i < tagList.size(); i++) {
            //todo
            EpcBean epcBean = tagList.get(i);
            content += epcBean.toString() + "\n";
        }
        try {
            write(fileName, content);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void write(String filePath, String string) throws IOException {
        File file = new File(filePath);
        // 判断文件是否存在
        if (!file.exists()) {
            File path = new File(file.getParent());
            if (!path.exists() && !path.mkdirs()) {   // 判断文件夹是否存在，不存在则创建文件夹
                Toast.makeText(getApplicationContext(), "文件夹创建失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!file.createNewFile()) {    // 创建文件
                Toast.makeText(getApplicationContext(), "文件创建失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // 实例化对象：文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        // 写入文件
        fileOutputStream.write(string.getBytes());
        // 清空输出流缓存
        fileOutputStream.flush();
        // 关闭输出流
        fileOutputStream.close();
        Toast.makeText(getApplicationContext(), "导出成功", Toast.LENGTH_SHORT).show();
    }

    private String getFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
        return "RFID" + "_" + sdf.format(new Date()) + FILE_EXTENSION;
    }

    //注销上报功能
    public void lableReport(List<String> epcs) {
        RetrofitClient.getInstance().create(WmsApi.class).lableReport(epcs)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<LableReportBean>() {
                    @Override
                    public void onNext(LableReportBean lableReportBean) {
                        if ("0000000".equals(lableReportBean.getRtnCode())) {
                            Toast.makeText(HandleDataActivity.this, "注销上报成功", Toast.LENGTH_SHORT).show();
                        } else {
                            String errMes = "注销上报失败 " + (lableReportBean.getErrorMsg() == null ? "" : lableReportBean.getErrorMsg());
                            Toast.makeText(HandleDataActivity.this, errMes, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("HandleDataFragment", e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //还筐上报功能
    public void returnBasket(List<String> epcs) {
        RetrofitClient.getInstance().create(WmsApi.class).returnBasket(epcs)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<LableReportBean>() {
                    @Override
                    public void onNext(LableReportBean lableReportBean) {
                        if ("0000000".equals(lableReportBean.getRtnCode())) {
                            Toast.makeText(HandleDataActivity.this, "还框上报成功", Toast.LENGTH_SHORT).show();
                        } else {
                            String errMes = "还框上报失败 " + (lableReportBean.getErrorMsg() == null ? "" : lableReportBean.getErrorMsg());
                            Toast.makeText(HandleDataActivity.this, errMes, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("HandleDataFragment", e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(canRfid){
            if (mDriver!= null && keyCode == KeyEvent.KEYCODE_F1) {
                startOrStopScan();
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

    @Override
    protected void onPause() {
        super.onPause();
        stopInventory();
    }
}
