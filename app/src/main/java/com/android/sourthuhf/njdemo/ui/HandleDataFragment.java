package com.android.sourthuhf.njdemo.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.BaseFragment;
import com.android.sourthuhf.MainActivity;
import com.android.sourthuhf.R;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.responsebean.LableReportBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class HandleDataFragment extends BaseFragment implements EpcItemAdapter.OnItemClickListener {
    private static final String FILE_EXTENSION = ".txt";
    private static final String INVENTORY_SUMMARY = "INVENTORY SUMMARY";
    private static final String UNIQUE_COUNT = "UNIQUE COUNT:";
    private static final String TOTAL_COUNT = "TOTAL COUNT:";
    MainActivity mainActivity;
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
    //思必拓枪柄按键广播接收者
    public static final String START_SCAN = "com.spd.action.start_uhf";
    public static final String STOP_SCAN = "com.spd.action.stop_uhf";
    public static final String MAIN_SCAN = "com.spd.action.start_uhf_mainactivity";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Receive", "Enter here");
            String action = intent.getAction();
            startOrStopScan();
        }
    };

    @Override
    protected void initEventAndData() {
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundId = soundPool.load(mainActivity, R.raw.barcodebeep, 1);
        mListView.setLayoutManager(new LinearLayoutManager(mainActivity));
        mListView.addItemDecoration(new DividerItemDecoration(mainActivity,LinearLayoutManager.VERTICAL));
        adapter = new EpcItemAdapter(tagList, mainActivity);
        adapter.setOnItemClickListener(this);
        mListView.setAdapter(adapter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleEpc((String) msg.obj);
            }
        };
        initReceive();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_handledata;
    }

    @OnClick({R.id.open_or_stop, R.id.clear_btn, R.id.tv_destory, R.id.tv_return})
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
        }
    }

    @Override
    protected void initView() {
        super.initView();
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

    public String AsciiStringToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    public int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    @Override
    public void onEpcItemClick(EpcBean fileBean) {
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

        if (1 == SoundFlag) {
            soundPool.play(soundId, 1, 1, 0, 1, 1);
        }
        //显示读取ecp个格式，默认16进制
       /* if (!mCharType.isChecked()) {
            tmp[1] = AsciiStringToString(tmp[1]);
        }*/
        tmp[1] = AsciiStringToString(tmp[1]);
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

    private void initReceive() {
        IntentFilter filter = new IntentFilter();
       /* filter.addAction(START_SCAN);
        filter.addAction(STOP_SCAN);*/
        filter.addAction(MAIN_SCAN);
        mainActivity.registerReceiver(receiver, filter);
    }


    @Override
    public void onDestroy() {
        stopInventory();
        mainActivity.unregisterReceiver(receiver);
        handler.removeMessages(0);
        super.onDestroy();
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

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
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
            Toast.makeText(mainActivity, R.string.not_get_storage_permission, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mainActivity.getApplicationContext(), "文件夹创建失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!file.createNewFile()) {    // 创建文件
                Toast.makeText(mainActivity.getApplicationContext(), "文件创建失败", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(mainActivity.getApplicationContext(), "导出成功", Toast.LENGTH_SHORT).show();
    }

    private String getFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
        return "RFID" + "_" + sdf.format(new Date()) + FILE_EXTENSION;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopInventory();
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
                            Toast.makeText(mainActivity, "注销上报成功", Toast.LENGTH_SHORT).show();
                        } else {
                            String errMes = "注销上报失败 " + (lableReportBean.getErrorMsg() == null ? "" : lableReportBean.getErrorMsg());
                            Toast.makeText(mainActivity, errMes, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(mainActivity, "还框上报成功", Toast.LENGTH_SHORT).show();
                        } else {
                            String errMes = "还框上报失败 " + (lableReportBean.getErrorMsg() == null ? "" : lableReportBean.getErrorMsg());
                            Toast.makeText(mainActivity, errMes, Toast.LENGTH_SHORT).show();
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

}
