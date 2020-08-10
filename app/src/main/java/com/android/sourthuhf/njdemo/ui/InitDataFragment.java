package com.android.sourthuhf.njdemo.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.BaseFragment;
import com.android.sourthuhf.MainActivity;
import com.android.sourthuhf.R;
import com.android.sourthuhf.Utils;
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

public class InitDataFragment extends BaseFragment implements WriteEpcItemAdapter.WriteEpcItemClickListener {
    @BindView(R.id.bt_k)
    Button mButtonK;
    @BindView(R.id.bt_t)
    Button mButtonT;
    @BindView(R.id.tv_current_box)
    TextView mCurrentBox;
    @BindView(R.id.tv_write_status)
    TextView mWriteStatsu;
    @BindView(R.id.et_tag_num)
    EditText mEpcSum;
    @BindView(R.id.rv_write_epcs)
    RecyclerView mListView;
    private ArrayList<WriteEpcBean> tagList = new ArrayList<>();
    private WriteEpcItemAdapter adapter;
    MainActivity mainActivity;
    Driver mDriver;
    public static final String TAG = "ReadAndWriteFragment";
    private Dialog writeDialog;
    private View writeView;
    //写标签
    ImageView spinImg;
    Button confirm;
    TextView identify;
    private boolean loopFlag = false;
    Handler handler;
    private Boolean canRfid = true;
    List<String> scanEpcs = new ArrayList<>();
    private Animation anim1;
    private String epcode;
    private String hexEpcode;
    private WriteEpcBean currentWrite;
    private Toast mTost;
    private boolean isSingleWrite;
    //一次扫描的标签
    private ArrayList<ScanEpcBean> OneAllDatas = new ArrayList<>();
    //写入成功的标签
    private ArrayList<ScanEpcBean> OneWriteDatas = new ArrayList<>();
    private int oneScanSize;
    private int currentIndex;
    private int repeatTime;

    @Override
    protected void initEventAndData() {
        mTost = Toast.makeText(mainActivity, "", Toast.LENGTH_SHORT);
        adapter = new WriteEpcItemAdapter(tagList, mainActivity);
        adapter.setWriteClickListener(this);
        mListView.setLayoutManager(new LinearLayoutManager(mainActivity));
        mListView.addItemDecoration(new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL));
        mListView.setAdapter(adapter);
        //写标签布局
        writeView = LayoutInflater.from(mainActivity).inflate(R.layout.activity_write_layout, null);
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
                        WriteTagResultParam writeTagResultParam = new WriteTagResultParam();
                        ArrayList<String> sucList = new ArrayList<>();
                        ArrayList<String> errList = new ArrayList<>();
                        sucList.add(epcode);
                        writeTagResultParam.setOkboxs(sucList);
                        writeTagResultParam.setErrboxs(errList);
                        reportWriteResult(writeTagResultParam);
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
                if (isSingleWrite) {
                    handleEpc((String) msg.obj);
                } else {
                    handleMultiWroteEpc((String) msg.obj);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_initdata_layout;
    }

    @OnClick({R.id.bt_k, R.id.bt_t})
    void performClick(View view) {
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (view.getId()) {
            case R.id.bt_k:
                mCurrentBox.setText("当前选项：框");
                String kStr = mEpcSum.getText().toString();
                if (kStr.isEmpty() || Integer.parseInt(kStr) < 1) {
                    mTost.setText("请先输入正确标签数量");
                    mTost.show();
                    return;
                }
                labelWrite(new WriteTagInfoParam("K", kStr));
                imm.hideSoftInputFromWindow(mainActivity.getCurrentFocus().getWindowToken(), 0);
                break;
            case R.id.bt_t:
                mCurrentBox.setText("当前选项：托");
                String tStr = mEpcSum.getText().toString();
                if (tStr.isEmpty() || Integer.parseInt(tStr) < 1) {
                    mTost.setText("请先输入正确标签数量");
                    mTost.show();
                    return;
                }
                imm.hideSoftInputFromWindow(mainActivity.getCurrentFocus().getWindowToken(), 0);
                labelWrite(new WriteTagInfoParam("T", tStr));
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
                            OneAllDatas.clear();
                            tagList.clear();
                            currentIndex = 0;
                            for (int i = 0; i < writeTagInfoBean.getTagnumber().size(); i++) {
                                WriteEpcBean writeEpcBean = new WriteEpcBean(writeTagInfoBean.getTagnumber().get(i), i, false);
                                tagList.add(writeEpcBean);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onEpcItemClick(WriteEpcBean fileBean) {
       /* String s = mCurrentBox.getText().toString();
        String type = "框".equals(s) ? "K" : "T";
        Intent intent = new Intent();
        intent.putExtra("epcode", fileBean.getEpc());
        intent.putExtra("typeode", type);
        intent.setClass(mainActivity, EpcItemDetailActivity.class);
        startActivity(intent);*/
    }

    @Override
    public void onWriteEpcClick(WriteEpcBean fileBean) {
       /* String s = mCurrentBox.getText().toString();
        String type = "框".equals(s) ? "K" : "T";
        Intent intent = new Intent();
        intent.putExtra("epcode", fileBean.getEpc());
        intent.putExtra("typeode", type);
        intent.setClass(mainActivity, WriteEpcActivity.class);
        startActivity(intent);*/
        currentWrite = fileBean;
        epcode = fileBean.getEpc();
        hexEpcode = asciiToHex(epcode);
        showWriteDialog();
    }

    public void showWriteDialog() {
        if (writeDialog != null) {
            writeDialog.show();
        } else {
            writeDialog = new Dialog(mainActivity) {
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
                    confirm.setEnabled(false);
                    confirm.setBackground(mainActivity.getDrawable(R.drawable.button_background_three));
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
            stopInventory();
            confirm.setEnabled(true);
            confirm.setBackground(mainActivity.getDrawable(R.drawable.button_background_two));
        } else {
            confirm.setEnabled(false);
            confirm.setBackground(mainActivity.getDrawable(R.drawable.button_background_three));
        }
    }

    private synchronized void handleMultiWroteEpc(String epc) {
        mWriteStatsu.setText("写入中...");
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
        tmp[1] = Utils.AsciiStringToString(tmp[1]);
        ScanEpcBean scanEpcBean = new ScanEpcBean(tmp[1], false, tmp[0]);
        if (OneAllDatas.size() < oneScanSize && !OneAllDatas.contains(scanEpcBean)) {
            OneAllDatas.add(scanEpcBean);
        }
        if (OneAllDatas.size() == oneScanSize) {
            stopInventory();
            writeHandler.postDelayed(writeRunable,2000);
        }

    }

    private Handler writeHandler = new Handler();
    private Runnable writeRunable = new Runnable() {
        @Override
        public void run() {
            if (tagList.size() - 1 < currentIndex || OneAllDatas.size() - 1 < currentIndex) {
                WriteTagResultParam writeTagResultParam = new WriteTagResultParam();
                ArrayList<String> sucList = new ArrayList<>();
                ArrayList<String> errList = new ArrayList<>();
                for (WriteEpcBean writeEpcBean : tagList) {
                    if (writeEpcBean.isWrite()){
                        sucList.add(writeEpcBean.getEpc());
                    }else {
                        errList.add(writeEpcBean.getEpc());
                    }
                }
                writeTagResultParam.setOkboxs(sucList);
                writeTagResultParam.setErrboxs(errList);
                reportMutiWriteResult(writeTagResultParam);
                mWriteStatsu.setText("正常");
                mTost.setText("已经写完，请检查结果");
                mTost.show();
                return;
            }
            int i = multiWriteEpc(currentIndex);
            if(i == 0){
                currentIndex++;
                repeatTime = 0;
            }else {
                if(repeatTime > 3){
                    currentIndex++;
                }
                repeatTime ++;
            }
            writeHandler.postDelayed(this,2000);
        }
    };

    public int multiWriteEpc(int i) {
        int writeResult = 1;
        WriteEpcBean writeEpcBean = tagList.get(i);
        ScanEpcBean scanEpcBean = OneAllDatas.get(i);
        writeResult = writeEpcTagbyTid(scanEpcBean.getTid(), Utils.asciiToHex(writeEpcBean.getEpc()));
        if(writeResult == 0){
           /* mTost.setText(writeEpcBean.getEpc() + i + "写入成功");
            mTost.show();*/
            writeEpcBean.setWrite(true);
            adapter.notifyDataSetChanged();
            Toast.makeText(mainActivity,writeEpcBean.getEpc()  + "写入成功",Toast.LENGTH_SHORT).show();
        }else {
           /* mTost.setText(writeEpcBean.getEpc() + i + "写入失败1111");
            mTost.show();*/
            Toast.makeText(mainActivity,writeEpcBean.getEpc()  + "写入失败",Toast.LENGTH_SHORT).show();
        }
        return writeResult;
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

    //多个写，以tid作为锁定条件

    public int writeEpcTagbyTid(String selectTid, String epcData) {
        //密码
        String passWard = "00000000";
        //选中区域 epc:1
        int selectArea = 2;
        //选中标签起始地址
        int selectStartAdr = 0;
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
        int writeResult = mDriver.Write_Data_Tag(passWard, selectArea, selectStartAdr, selectLength, selectTid, writeArea, writeStartAdr, writeLength, epcData);
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
                            currentWrite.setWrite(true);
                            adapter.notifyDataSetChanged();
                            mTost.setText("写标签上报成功");
                            mTost.show();
                            writeDialog.dismiss();
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

    public void reportMutiWriteResult(WriteTagResultParam resultParam) {
        RetrofitClient.getInstance().create(WmsApi.class).reportWriteResult(resultParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<LableReportBean>() {
                    @Override
                    public void onNext(LableReportBean lableReportBean) {
                        if ("0000000".equals(lableReportBean.getRtnCode())) {
                            mTost.setText("写标签上报成功");
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
                spinImg.startAnimation(anim1);
                confirm.setEnabled(false);
                confirm.setBackground(mainActivity.getDrawable(R.drawable.button_background_three));
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

    public void startStopScanning() {
        String kStr = mEpcSum.getText().toString();
        if (kStr.isEmpty() || Integer.parseInt(kStr) < 1 || tagList.size() != Integer.parseInt(kStr)) {
            mTost.setText("请先获取要写入标签数据");
            mTost.show();
            return;
        } else {
            oneScanSize = Integer.parseInt(kStr);
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
    }

}
