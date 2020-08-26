package com.android.uhfdemo;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import com.android.uhfdemo.meite.datebase.DemoDatabase;
import com.android.uhfdemo.meite.parambean.FileBean;
import com.android.uhfdemo.meite.utils.MtExcelUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private InventoryFragment minventoryFragment;

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
        titles.add("盘存");
        titles.add("读写");
        titles.add("锁定");
        titles.add("销毁");
        titles.add("设置");
        minventoryFragment = new InventoryFragment();
        fragments.add(minventoryFragment);
        fragments.add(new ReadAndWriteFragment());
        fragments.add(new LockFragment());
        fragments.add(new DestoryFragment());
        fragments.add(new SettingsFragment());
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
            case R.id.action_read:
                //todo 导出数据
                importData();
                return true;
            default:
                return false;
        }
    }
    
    //导入数据 start
    private void importData() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置任意类型
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                importExcle(data);
            }
        }
    }

    /**
     * 导入选中文件
     */
    public void importExcle(Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            String path = getPath(this, uri);
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    //获取的路径
                    String upLoadFilePath = file.toString();
                    //文件名
                    String upLoadFileName = file.getName();
                    String[] strArray = upLoadFileName.split("\\.");
                    int suffixIndex = strArray.length - 1;
                    File dir = new File(upLoadFilePath);
                    //调用查询方法
                    final List<FileBean> fileBeans = new ArrayList<>();
                    if(upLoadFilePath.endsWith(".xls")){
                        fileBeans.addAll(MtExcelUtils.read2DB(dir, this));
                    }else if(upLoadFilePath.endsWith(".csv") || upLoadFilePath.endsWith(".txt")){
                        fileBeans.addAll(MtExcelUtils.readCSV(dir, this));
                    }
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            DemoDatabase.getInstance().getReportParamDao().deleteAllData();
                            DemoDatabase.getInstance().getReportParamDao().insertItems(fileBeans);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Toast.makeText(MainActivity.this,"数据导入成功", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();

                }
            }
        }
    }

    public String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        }
        // Files
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    //导入数据 end
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

