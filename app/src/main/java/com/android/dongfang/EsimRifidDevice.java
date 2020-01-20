package com.android.dongfang;

import android.os.AsyncTask;
import android.serialport.DeviceControlSpd;
import android.util.Log;
import java.io.IOException;
import cn.com.example.rfid.driver.Driver;
import cn.com.example.rfid.driver.RfidDriver;
import static android.serialport.DeviceControlSpd.PowerType.EXPAND;

public class EsimRifidDevice implements IRfidDevice {
    private static final String TAG = "EsimRifidDevice";
    private Driver driver;
    private DeviceControlSpd newUHFDeviceControl;
    //是否已经初始化
    private boolean isOpen;
    //是否开启连续盘点
    private boolean isStartScan;

    /**
     * 初始化设备
     *
     * @return 成功后返回true, 失败返回false
     */
    @Override
    public boolean init() {
        if (!isOpen && !isStartScan) {
            driver = new RfidDriver();
            int[] gpios = {9, 14};
            try {
                newUHFDeviceControl = new DeviceControlSpd(EXPAND, gpios);
                newUHFDeviceControl.PowerOnDevice();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int status = driver.initRFID("/dev/ttyMT0");
            if (1000 == status) {
                //设置读取epc和tid
                //设置读取epc和tid start
                driver.Read_Tag_Mode_Set(1, false);
                //设置读取epc和tid end
                isOpen = true;
                return true;
            } else {
                isOpen = false;
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 释放设备资源
     *
     * @return 成功后返回true, 失败返回false
     */
    @Override
    public boolean free() {
        return true;
    }

    /**
     * 打开设备，如果初始化设备之后RFID功能就可以使用，这个方法返回设备初始化的值
     *
     * @return 成功后返回true, 失败返回false
     */
    @Override
    public boolean open() {
        return isOpen;
    }

    /**
     * 判断该RFID是否可用
     *
     * @return true, 此时设备可以使用读写功能， false 此时设备不可以使用读写功能
     **/
    @Override
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 关闭设备
     *
     * @return true 关闭成功， false 关闭失败
     */
    @Override
    public boolean close() {
        if (isOpen && !isStartScan) {
            try {
                newUHFDeviceControl.PowerOffDevice();
                isOpen = false;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }else {
            return false;
        }
    }

    /**
     * RFID单次扫描
     *
     * @return 返回EPC的值
     */
    @Override
    public RFIDTagInfo singleScan() {
        if (isOpen && !isStartScan) {
            //参数为超时时间 暂时有问题
            String s = driver.SingleRead(10);
            RFIDTagInfo rfidTagInfo = new RFIDTagInfo();
            rfidTagInfo.setEpcID(s);
            return rfidTagInfo;
        } else {
            return null;
        }

    }

    /**
     * 开始连续扫描
     */
    @Override
    public void startScan(RFIDCallback rfidCallback) {
        if (isOpen) {
            try {
                if (!isStartScan) {
                    isStartScan = true;
                    //扫描返回1020成功，其他失败
                    int status = driver.readMore();
                    if (1020 == status) {
                        //new TagThread(rfidCallback).start();
                        MasyncTask masyncTask = new MasyncTask(rfidCallback);
                        masyncTask.execute();
                    } else {
                        rfidCallback.onError(-1);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 关闭连续扫描
     */
    @Override
    public void stopScan() {
        if (isOpen) {
            if (isStartScan) {
                isStartScan = false;
                driver.stopRead();
            }
        }


    }

    /**
     * 对EPC进行写入
     *
     * @param content 写入到EPC的内容
     * @return 0表示成功，-1表示失败
     */
    @Override
    public int write(String content) {
        if (isOpen && !isStartScan) {
            String passWord = "00000000";
            String regex = "^[A-Fa-f0-9]+$";
            int offset = 2;
            int length = content.length();
            if (length % 4 == 0 && content.matches(regex)) {
                length = length / 4;
                int Status = driver.Write_Epc_Data(passWord, offset, length, content);
                return Status;
            }
        }
        return -1;
    }

    /**
     * 设置功率
     *
     * @param power 0~33的整数
     */
    @Override
    public boolean setPower(int power) {
        if (isOpen && !isStartScan) {
            if (power > 30) {
                power = 30;
            }
            if (power < 1) {
                power = 1;
            }
            int status = driver.setTxPowerOnce(power);
            return 1 == status;
        } else {
            return false;
        }
    }

    /**
     * 读取功率
     * 0~33的整数
     */
    @Override
    public int getPower() {
        if (isOpen && !isStartScan) {
            int text = driver.GetTxPower();
            if (5 < text && text <= 30) {
                return text;
            }
        }
        return -1;
    }

    /**
     * 读取数据
     *
     * @param bank   数据所在区域，如EPC TID USER RESERVED，
     * @param offset 起始位置偏移量 单位是1个字
     * @param length 要读取的长度 单位是1个字
     * @return 读取到的内容
     */
    @Override
    public String readData(RFIDAreaEnum bank, int offset, int length) {
        if (isOpen && !isStartScan) {
            String passWord = "00000000";
            int selectArea = 1;
            switch (bank) {
                case EPC:
                    selectArea = 1;
                    break;
                case TID:
                    selectArea = 2;
                    break;
                case USER:
                    selectArea = 3;
                    break;
                case RESERVED:
                    selectArea = 0;
                    break;
            }
            String data = driver.Read_Data_Tag(passWord, 0, 0, 0, "0", selectArea, offset, length);
            Log.e(TAG, "data===" + data);
            return data;
        } else {
            return null;
        }


    }

    /**
     * 根据tid，写入epc数据
     *
     * @param tid     指定要写入的RFID标签的TID
     * @param content 要写入的内容
     * @return 0表示成功，-1表示失败
     */
    @Override
    public int write(String tid, String content) {
        if (isOpen && !isStartScan) {
            //16进制数正则
            String regex = "^[A-Fa-f0-9]+$";
            //访问密码
            String passWord = "00000000";
            //过滤区域
            int filtterArea = 2;
            //写入区域epc
            int selectArea = 1;
            int ads = 2;
            int epcLen = content.length();
            int tidLen = tid.length();
            //if (tid.matches(regex) && content.matches(regex) && epcLen % 4 == 0 && tidLen % 4 == 0) {
            if (tid.matches(regex) && content.matches(regex) && epcLen % 4 == 0 && tidLen % 2 == 0) {
                epcLen = epcLen / 4;
                tidLen = tidLen * 4;
                int result = driver.Write_Data_Tag(passWord, filtterArea, 0, tidLen, tid, selectArea, ads, epcLen, content);
                return result;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * 往USER去区写入数据
     *
     * @return true表示成功，false表示失败
     */
    @Override
    public boolean writeUser(String content) {
        if (isOpen && !isStartScan) {
            //16进制数正则
            String regex = "^[A-Fa-f0-9]+$";
            //访问密码
            String passWord = "00000000";
            //写入区域user
            int selectArea = 3;
            int ads = 0;
            int epcLen = content.length();
            if (content.matches(regex) && epcLen % 4 == 0) {
                epcLen = epcLen / 4;
                int result = driver.Write_Data_Tag(passWord, 0, 0, 0, "0", selectArea, ads, epcLen, content);
                return result == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    /**
     * public abstract class AsyncTask<Params, Progress, Result>
     * AsyncTask是个泛型类，有三个泛型参数
     * Params：参数的类型
     * Progress：后台任务执行的进度的类型
     * Result：后台任务执行的最终结果值
     */
    private class MasyncTask extends AsyncTask<Void, RFIDTagInfo, Void> {
        RFIDCallback mRfidCallback;

        public MasyncTask(RFIDCallback mRfidCallback) {
            this.mRfidCallback = mRfidCallback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (isStartScan) {
                String[] strEpc1 = {driver.GetBufData()};
                String strEpc = strEpc1[0];
                if (strEpc != null && strEpc.length() != 0) {
                    int Hb = 0;
                    int Lb = 0;
                    int rssi = 0;
                    String[] tmp = new String[3];
                    //去除头4位
                    String text = strEpc.substring(4);
                    //epc长度原始16进制数据
                    String len = strEpc.substring(0, 2);
                    //实际epc长度
                    int epcLen = (Integer.parseInt(len, 16) / 8) * 4;
                    //tid
                    tmp[0] = text.substring(epcLen, text.length() - 6);
                    //epc
                    tmp[1] = text.substring(0, epcLen);
                    //rssi
                    tmp[2] = text.substring(text.length() - 6, text.length() - 2);
                    if (4 != tmp[2].length()) {
                        tmp[2] = "0000";
                    } else {
                        Hb = Integer.parseInt(tmp[2].substring(0, 2), 16);
                        Lb = Integer.parseInt(tmp[2].substring(2, 4), 16);
                        rssi = ((Hb - 256 + 1) * 256 + (Lb - 256)) / 10;
                        //初始范围 -80 -- -30
                        rssi = (rssi + 80) * 2;
                        if (rssi > 100) {
                            rssi = 100;
                        }
                        if (rssi < 1) {
                            rssi = 0;
                        }
                    }
                    RFIDTagInfo rfidTagInfo = new RFIDTagInfo();
                    rfidTagInfo.setTid(tmp[0]);
                    rfidTagInfo.setEpcID(tmp[1]);
                    rfidTagInfo.setOptimizedRSSI(rssi);
                    publishProgress(rfidTagInfo);
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(RFIDTagInfo... values) {
            super.onProgressUpdate(values);
            RFIDTagInfo rfidTagInfo = values[0];
            //if (rfidTagInfo.getEpcID() != null && rfidTagInfo.getTid() != null && rfidTagInfo.getOptimizedRSSI() > 0 && rfidTagInfo.getOptimizedRSSI() < 100) {
            if (rfidTagInfo.getEpcID() != null && rfidTagInfo.getTid() != null) {
                mRfidCallback.onResponse(rfidTagInfo);
            } else {
                mRfidCallback.onError(-1);
            }
        }
    }
}
