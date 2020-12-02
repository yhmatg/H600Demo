package com.android.sourthuhf.njdemo.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.parambean.WriteTagInfoParam;
import com.android.sourthuhf.njdemo.parambean.WriteTagResultParam;
import com.android.sourthuhf.njdemo.responsebean.LableReportBean;
import com.android.sourthuhf.njdemo.responsebean.WriteTagInfoBean;
import com.android.sourthuhf.njdemo.ui.WriteEpcBean;
import com.android.sourthuhf.njdemo.ui.WriteEpcItemAdapter;

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

public class SingleInitDataActivity extends AppCompatActivity implements WriteEpcItemAdapter.WriteEpcItemClickListener {
    private Unbinder unBinder;
    @BindView(R.id.title_content)
    TextView title;
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
    Driver mDriver;
    //写标签
    ImageView spinImg;
    Button confirm;
    TextView identify;
    private boolean loopFlag = false;
    Handler handler;
    List<String> scanEpcs = new ArrayList<>();
    private Animation anim1;
    private WriteEpcBean currentWrite;
    private Toast mTost;
    private String hexEpcode;
    private String assiiEpcode;
    private boolean isSingleWrite;
    //写入成功的标签
    List<String> writeEpcs = new ArrayList<>();
    private String epcode;
    private Boolean canRfid = true;
    private Dialog writeDialog;
    private View writeView;
    private boolean isBox = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_singleinit_layout);
        unBinder = ButterKnife.bind(this);
        initEventAndData();
        beeperSettings();
    }

    protected void initEventAndData() {
        mTost = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        title.setText("单盘数据初始化");
        mDriver = UhfApplication.getDriver();
        adapter = new WriteEpcItemAdapter(tagList, this);
        adapter.setWriteClickListener(this);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mListView.setAdapter(adapter);
        writeView = LayoutInflater.from(this).inflate(R.layout.activity_write_layout, null);
        confirm = writeView.findViewById(R.id.btn_confirm_write);
        spinImg = writeView.findViewById(R.id.iv_spin);
        identify = writeView.findViewById(R.id.identify_tips);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDriver != null) {
                    String selectEpc = scanEpcs.size() > 0 ? scanEpcs.get(0) : "";
                    int reslut = writeEpcTag(selectEpc, hexEpcode);
                    if (0 == reslut) {
                        identify.setText("写入成功");
                        identify.setTextColor(getColor(R.color.green_color));
                        beep();
                    } else {
                        mTost.setText("写入失败，请重试");
                        mTost.show();
                    }
                } else {
                    mTost.setText("RFID异常，请重新进入应用连接");
                    mTost.show();
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(isSingleWrite){
                    handleEpc((String) msg.obj);
                }else {
                    autoHandleEpc((String) msg.obj);
                }

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

    @OnClick({R.id.bt_k, R.id.bt_t, R.id.bt_report,R.id.title_back})
    void performClick(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (view.getId()) {
            case R.id.bt_k:
                mCurrentBox.setText("当前选项：筐");
                labelWrite(new WriteTagInfoParam("K", "1"));
                isBox = true;
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                break;
            case R.id.bt_t:
                mCurrentBox.setText("当前选项：托");
                isBox = false;
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                labelWrite(new WriteTagInfoParam("T", "1"));
                break;
            case R.id.bt_report:
                WriteTagResultParam writeTagResultParam = new WriteTagResultParam();
                ArrayList<String> sucList = new ArrayList<>();
                ArrayList<String> errList = new ArrayList<>();
                sucList.add(assiiEpcode);
                writeTagResultParam.setOkboxs(sucList);
                writeTagResultParam.setErrboxs(errList);
                reportWriteResult(writeTagResultParam);
                break;
            case R.id.title_back:
                finish();
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
                                assiiEpcode = writeTagInfoBean.getTagnumber().get(i);
                                currentWrite = writeEpcBean;
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
       /* tagList.clear();
        currentWrite = new WriteEpcBean("K00000000003", 0, false);
        tagList.add(currentWrite);
        hexEpcode = asciiToHex("K00000000003");
        adapter.notifyDataSetChanged();
        mButtonT.setEnabled(false);
        mButtonK.setEnabled(false);*/
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
            identify.setText(R.string.identify_success);
            identify.setTextColor(getColor(R.color.black));
            stopInventory();
            confirm.setEnabled(true);
            confirm.setBackground(this.getDrawable(R.drawable.button_background_two));
        } else {
            confirm.setEnabled(false);
            confirm.setBackground(this.getDrawable(R.drawable.button_background_three));
        }
    }

    private void autoHandleEpc(String epc) {
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
            if (scanEpcs.size() == 1) {
                writeStatus.setText("开始写入标签");
                stopInventory();
                int reslut = writeEpcTag(scanEpcs.get(0), hexEpcode);
                if (0 == reslut) {
                    if (writeEpcs.size() == 0) {
                        writeEpcs.add(tmp[1]);
                        writeStatus.setText("第一边写入成功");
                        beep();
                        if(isBox){
                            WriteTagResultParam writeTagResultParam = new WriteTagResultParam();
                            ArrayList<String> sucList = new ArrayList<>();
                            ArrayList<String> errList = new ArrayList<>();
                            sucList.add(assiiEpcode);
                            writeTagResultParam.setOkboxs(sucList);
                            writeTagResultParam.setErrboxs(errList);
                            reportWriteResult(writeTagResultParam);
                        }
                    } else if (!isBox && writeEpcs.size() == 1 && !writeEpcs.contains(tmp[1])) {
                        writeEpcs.add(tmp[1]);
                        writeStatus.setText("第二边写入成功");
                        beep();
                        WriteTagResultParam writeTagResultParam = new WriteTagResultParam();
                        ArrayList<String> sucList = new ArrayList<>();
                        ArrayList<String> errList = new ArrayList<>();
                        sucList.add(assiiEpcode);
                        writeTagResultParam.setOkboxs(sucList);
                        writeTagResultParam.setErrboxs(errList);
                        reportWriteResult(writeTagResultParam);
                    }
                } else {
                    if (writeEpcs.size() == 0) {
                        writeStatus.setText("第一边写入失败");
                    } else if (writeEpcs.size() == 1) {
                        writeStatus.setText("第二边写入失败");
                    }
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
                            if(isBox){
                                writeStatus.setText("请写入下一个筐");
                                labelWrite(new WriteTagInfoParam("K", "1"));
                            }else {
                                writeStatus.setText("请写入下一个托盘");
                                labelWrite(new WriteTagInfoParam("T", "1"));
                            }
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

      /*  writeEpcs.clear();
        currentWrite.setWrite(true);
        adapter.notifyDataSetChanged();
        mButtonT.setEnabled(true);
        mButtonK.setEnabled(true);
        mTost.setText("写标签上报成功");
        writeStatus.setText("请写入下一个托盘");
        mTost.show();*/
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

    @Override
    public void onEpcItemClick(WriteEpcBean fileBean) {

    }

    @Override
    public void onWriteEpcClick(WriteEpcBean fileBean) {
        showWriteDialog();
    }

    public void showWriteDialog() {
        if (writeDialog != null) {
            writeDialog.show();
        } else {
            writeDialog = new Dialog(this) {
                @Override
                public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
                    if (canRfid) {
                        if (mDriver != null && keyCode == KeyEvent.KEYCODE_F1) {
                            startStopScanning();
                        }
                        canRfid = false;
                    }
                    return super.onKeyDown(keyCode, event);
                }

                @Override
                public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
                    canRfid = true;
                    return super.onKeyUp(keyCode, event);
                }
            };
            writeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    isSingleWrite = false;
                    identify.setText("扣动扳机 识别标签");
                    identify.setTextColor(getColor(R.color.black));
                    confirm.setEnabled(false);
                    confirm.setBackground(SingleInitDataActivity.this.getDrawable(R.drawable.button_background_three));
                }
            });
            writeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    isSingleWrite = true;
                }
            });
            writeDialog.setContentView(writeView);
            writeDialog.show();
            Window window = writeDialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setPadding(0, 0, 0, 0);
                window.getDecorView().setBackgroundColor(Color.WHITE);
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                window.setAttributes(layoutParams);
            }
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

    private  ToneGenerator toneGenerator;

    public  void beeperSettings() {
        int streamType = AudioManager.STREAM_DTMF;
        int percantageVolume = 100;
        toneGenerator = new ToneGenerator(streamType, percantageVolume);
    }

    public  void beep() {
        int toneType = ToneGenerator.TONE_PROP_BEEP;
        toneGenerator.startTone(toneType);
    }

}
