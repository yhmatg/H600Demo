package com.android.sourthuhf.original;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;
import com.android.sourthuhf.SharePreferenceUtils;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;

public class SearchTagFragment extends BaseFragment {
    private static final String TAG = "SearchTagFragment";
    public static final int HANDLE_EPC = 0;
    public static final int INIT_PROGRESS = 1;
    public static final int UPDATE_SEARCH_STATUS = 2;
    MainActivity mainActivity;
    Driver mDriver;
    @BindView(R.id.filter_clear)
    TextView mFilterClear;
    @BindView(R.id.filter_set)
    TextView mFilterSet;
    @BindView(R.id.et_start)
    EditText mStart;
    @BindView(R.id.et_length)
    EditText mLength;
    @BindView(R.id.et_data)
    EditText mData;
    @BindView(R.id.filter_save)
    CheckBox mFilterSave;
    @BindView(R.id.search_tag)
    TextView mSearchTag;
    @BindView(R.id.pb_Search)
    ProgressBar pSearch;
    @BindView(R.id.tv_SearchTag)
    TextView tvCurrentTag;

    Handler SearchHandler;
    private boolean flag;
    boolean loopFlag = false;
    private short maxValue = -30, minValue = -80; //RSSI的最大值和最小值
    private String content;
    int finalRssi;
    private SoundPool soundPool;
    private int soundId;
    private long currentMinute, oldMinute;

    @Override
    protected void initEventAndData() {
        //soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        AudioAttributes abs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build() ;
        soundPool =  new SoundPool.Builder()
                .setMaxStreams(10)   //设置允许同时播放的流的最大值
                .setAudioAttributes(abs)   //完全可以设置为null
                .build() ;

        soundId = soundPool.load(mainActivity, R.raw.beep, 1);
        SearchHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.arg1) {
                    case HANDLE_EPC:
                        DoWithEpc("" + msg.obj);
                        break;
                    case INIT_PROGRESS:
                        tvCurrentTag.setText(null);
                        pSearch.setProgress(0);
                        break;
                    case UPDATE_SEARCH_STATUS:
                        tvCurrentTag.setText(content + "====" + finalRssi);
                        pSearch.setProgress(finalRssi);
                        playSound(finalRssi);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_tag;
    }

    @OnClick({R.id.filter_set, R.id.filter_clear, R.id.search_tag})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.filter_set:
                filterSet();
                break;
            case R.id.filter_clear:
                filterClear();
                break;
            case R.id.search_tag:
                StartOrStopRfid();
                break;
        }
    }

    //开启或停止读卡
    public void StartOrStopRfid() {
        if (!flag) {
            flag = true;
            mDriver.readMore();
            new TagThread().start();
            loopFlag = true;
            mSearchTag.setText(R.string.stop_search);
            flag = true;
        } else {
            stopInventory();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSearchTag.setText(R.string.start_search);
            flag = false;
        }
    }

    private void filterSet() {
        if (!checkOutEditext()) {
            return;
        }
        int Status = 0;
        int ads = Integer.valueOf(mStart.getText().toString());
        int len = Integer.valueOf(mLength.getText().toString());
        int val = 1;
        Status = mDriver.Set_Filter_Data(val, ads, len, mData.getText().toString(), mFilterSave.isChecked());
        Log.e(TAG, "Status===" + Status);
        if (-1 == Status) {
            Toast.makeText(mainActivity, R.string.filter_failed, Toast.LENGTH_SHORT).show();
        } else {
            SharePreferenceUtils.getInstance().setFilterData(mData.getText().toString());
            SharePreferenceUtils.getInstance().setFilterStartArea(mStart.getText().toString());
            SharePreferenceUtils.getInstance().setFilterLength(mLength.getText().toString());
            Toast.makeText(mainActivity, R.string.filter_sucess, Toast.LENGTH_SHORT).show();
        }
    }

    private void filterClear() {
        /*if ( mData.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.data_null, Toast.LENGTH_SHORT).show();
            return;
        }*/
        int Status = 0;
        int ads = 0;
        int len = 0;
        int val = 1;
        Status = mDriver.Set_Filter_Data(val, ads, len, mData.getText().toString(), mFilterSave.isChecked());
        Log.e(TAG, "Status===" + Status);
        if (-1 == Status) {
            Toast.makeText(mainActivity, R.string.clear_failed, Toast.LENGTH_SHORT).show();
        } else {
            mData.setText("");
            SharePreferenceUtils.getInstance().setFilterData("");
            Toast.makeText(mainActivity, R.string.clear_sucess, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkOutEditext() {
        boolean result = false;
        if (mStart.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.start_area_null, Toast.LENGTH_SHORT).show();
        } else if (mLength.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.length_null, Toast.LENGTH_SHORT).show();
        } else if (mData.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.data_null, Toast.LENGTH_SHORT).show();
        } else {
            result = true;
        }
        return result;
    }

    //处理EPC
    public void DoWithEpc(String epcid) {
        Log.e("DoWithEpc", "Enter");
        int Hb = 0;
        int Lb = 0;
        int rssi = 0;
        String[] tmp = new String[3];
        String text = epcid.substring(4);
        String len = epcid.substring(0, 2);
        int epclen = (Integer.parseInt(len, 16) / 8) * 4;
        tmp[0] = text.substring(epclen, text.length() - 6);
        tmp[1] = text.substring(0, epclen);
        tmp[2] = text.substring(text.length() - 6, text.length() - 2);
        content = tmp[1];
        if (4 != tmp[2].length()) {
            tmp[2] = "0000";
        } else {
            Hb = Integer.parseInt(tmp[2].substring(0, 2), 16);
            Lb = Integer.parseInt(tmp[2].substring(2, 4), 16);
            rssi = ((Hb - 256 + 1) * 256 + (Lb - 256)) / 10;
        }

        // int length = maxValue - minValue; //取差值
        if (rssi >= maxValue) {
            rssi = maxValue;
        } else if (rssi <= minValue) {
            rssi = minValue;
        }
        rssi -= minValue;
        finalRssi = rssi;
       /* final int finalRssi = rssi;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCurrentTag.setText(content);
                pSearch.setProgress(finalRssi);
                playSound(finalRssi);
            }
        });*/
        /*Message updataMsg = new Message();
        updataMsg.arg1 = UPDATE_SEARCH_STATUS;
        SearchHandler.sendMessage(updataMsg);*/
        tvCurrentTag.setText(content + "====" + finalRssi);
        pSearch.setProgress(finalRssi);
        playSound(finalRssi);
    }

    class TagThread extends Thread {

        private int mBetween = 0;

        public TagThread() {
        }

        public void run() {
            while (loopFlag) {
                String[] strEpc1 = {mDriver.GetBufData()};
                String strEpc = strEpc1[0];
                Message msg = new Message();
                if (!TextUtils.isEmpty(strEpc)) {
                    msg.arg1 = HANDLE_EPC;
                    msg.obj = strEpc;

                } else {
                    msg.arg1 = INIT_PROGRESS;
                }
                SearchHandler.sendMessage(msg);
                try {
                    sleep(mBetween);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            mDriver.stopRead();

        }
    }

    //音源播放
    private void playSound(int val) {
        if (val > 30) {
            //playSound();
            soundPool.play(soundId, 1, 1, 0, 1, 1);
            oldMinute = System.currentTimeMillis();
        } else if (val > 20) {
            currentMinute = System.currentTimeMillis();
            if (currentMinute - oldMinute > 300) {
                //playSound();
                soundPool.play(soundId, 0.8f, 0.8f, 0, 1, 1);
                oldMinute = currentMinute;
            }
        } else if (val > 10) {
            currentMinute = System.currentTimeMillis();
            if (currentMinute - oldMinute > 600) {
                //playSound();
                soundPool.play(soundId, 0.6f, 0.6f, 0, 1, 1);
                oldMinute = currentMinute;
            }
        } else if (val > 0) {
            currentMinute = System.currentTimeMillis();
            if (currentMinute - oldMinute > 900) {
                //playSound();
                soundPool.play(soundId, 0.4f, 0.4f, 0, 1, 1);
                oldMinute = currentMinute;
            }
        }
    }

    public void myPlaySound(int val) {
        if (val > 30) {
            soundPool.play(soundId, 1, 1, 0, 1, 2);
        } else if (val > 20) {
            soundPool.play(soundId, 0.8f, 0.8f, 0, 1, 1.5f);
        } else if (val > 10) {
            soundPool.play(soundId, 0.6f, 0.6f, 0, 1, 1f);
        } else if (val > 0) {
            soundPool.play(soundId, 0.4f, 0.4f, 0, 1, 0.5f);

        }
    }

    //播放滴滴滴的声音
    public void playSound() {
        soundPool.play(soundId, 1, 1, 0, 1, 1);
    }
}
