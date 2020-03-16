package com.android.uhfdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;

public class InventoryFragment extends BaseFragment {
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
    ListView mListView;
    @BindView(R.id.open_or_stop)
    TextView mStartOrStop;
    @BindView(R.id.clear_btn)
    TextView mClear;

    Driver mDriver;
    private SoundPool soundPool;
    private int soundId;
    //声音提示
    private int SoundFlag = 0;
    //开始停止盘点标记
    private int flag = 0;
    //listview 需要的集合
    private ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    //存放单条读取信息
    private HashMap<String, String> hmap = new HashMap<>();
    private SimpleAdapter adapter;
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
        adapter = new SimpleAdapter(mainActivity, tagList, R.layout.item,
                new String[]{"sn", "epc", "count", "rssi"}, new int[]{
                R.id.sn, R.id.epc, R.id.count, R.id.rssi});
        mListView.setAdapter(adapter);
        //mListView长按事件
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.epc);
                ClipboardManager manager = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", tv.getText());
                manager.setPrimaryClip(mClipData);
                Toast.makeText(mainActivity, R.string.copy_sucess, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                addEPCToList(msg.obj + "", hmap, tagList);

                adapter.notifyDataSetChanged();
                Ct++;
                mEpcCount.setText("" + hmap.size());
                mEpcSum.setText("" + Ct);
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
        return R.layout.activity_inventory;
    }

    @OnClick({R.id.open_or_stop, R.id.clear_btn})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.open_or_stop:
                startOrStopScan();
                break;
            case R.id.clear_btn:
                tagList.clear();
                //mListView.setAdapter(adapter);
                //map.clear();
                adapter.notifyDataSetChanged();
                hmap.clear();
                iIndex = 0;
                Ct = 0;
                mEpcSum.setText("");
                mEpcCount.setText("");
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

    private void addEPCToList(String epc, HashMap<String, String> hmap, ArrayList<HashMap<String, String>> tagList) {

        int Hb = 0;
        int Lb = 0;
        int rssi = 0;
        String[] tmp = new String[3];
        HashMap<String, String> temp = new HashMap<>();
        String text = epc.substring(4);
        String len = epc.substring(0, 2);
        int epclen = (Integer.parseInt(len, 16) / 8) * 4;
        tmp[0] = text.substring(epclen, text.length() - 6);
        tmp[1] = text.substring(0, epclen);
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
        if (!mCharType.isChecked()) {
            tmp[1] = AsciiStringToString(tmp[1]);
        }

        int count = 0;
        temp.put("epc", tmp[1]);
        if (hmap.containsKey(tmp[1])) {
            String strTemp = hmap.get(tmp[1]);
            if (null != strTemp) {
                int sn = Integer.valueOf(strTemp.split(",")[1]);
                count = Integer.valueOf(strTemp.split(",")[0]) + 1;
                temp.put("sn", "" + sn);
                temp.put("count", "" + count);
                temp.put("rssi", "" + rssi);
                hmap.put(tmp[1], String.valueOf(count) + "," + String.valueOf(sn) + "," + rssi);
                tagList.set(sn, temp);
            }
        } else {
            hmap.put(tmp[1], "1," + iIndex + "," + rssi);
            temp.put("sn", "" + iIndex);
            temp.put("count", "1");
            temp.put("rssi", "" + rssi);
            tagList.add(temp);
            iIndex++;
        }
    }

    private void stopInventory() {

        if (loopFlag) {

            loopFlag = false;

            mDriver.stopRead();

        }
    }

    private void initReceive() {
        //这边会将手柄出发的动作改为uhf
        IntentFilter filter = new IntentFilter();
       /* filter.addAction(START_SCAN);
        filter.addAction(STOP_SCAN);*/
        filter.addAction(MAIN_SCAN);
        mainActivity.registerReceiver(receiver, filter);
        SystemProperties.set("persist.sys.PistolKey", "uhf");
    }


    @Override
    public void onDestroy() {
        stopInventory();
        mainActivity.unregisterReceiver(receiver);
        SystemProperties.set("persist.sys.PistolKey", "scan");
        super.onDestroy();
    }

    public void startOrStopScan() {
        switch (flag) {
            case 0:
                tagList.clear();
                //mListView.setAdapter(adapter);
                //map.clear();
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
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mStartOrStop.setText(R.string.scann_start);
                flag = 0;
                //GetCnt = 0;
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
            HashMap<String, String> itemMap = tagList.get(i);
            content += itemMap + "\n";
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

}
