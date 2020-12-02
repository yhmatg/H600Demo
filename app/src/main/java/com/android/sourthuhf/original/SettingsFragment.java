package com.android.sourthuhf.original;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;
import com.android.sourthuhf.SharePreferenceUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;

public class SettingsFragment extends BaseFragment {

    public static final String TAG = "SettingsFragment";
    public static final int SHOW_UPDATE_PROGRESS = 0;
    public static final int UPDATE_SUCESS = 1;
    public static final int UPDATE_FAILED = -1;
    @BindView(R.id.spinner_output)
    Spinner mOutputSpinner;
    @BindView(R.id.output_get)
    TextView mOutputGet;
    @BindView(R.id.output_set)
    TextView mOutputSet;
    ArrayAdapter<String> outputAdapter;

    @BindView(R.id.spinner_frequence)
    Spinner mFrequenceSpinner;
    @BindView(R.id.frequence_get)
    TextView mFrequenceGet;
    @BindView(R.id.frequence_set)
    TextView mFrequenceSet;
    ArrayAdapter<String> frequenceAdapter;

    @BindView(R.id.spinner_mode)
    Spinner mModeSpinner;
    @BindView(R.id.mode_get)
    TextView mModeGet;
    @BindView(R.id.mode_set)
    TextView mModeSet;
    @BindView(R.id.cb_save)
    CheckBox mModeSave;
    ArrayAdapter<String> modeAdapter;

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

    @BindView(R.id.selected_file)
    TextView mSelectedFile;
    @BindView(R.id.choose_file)
    TextView mChooseFile;
    @BindView(R.id.update_set)
    TextView mUpdate;

    MainActivity mainActivity;
    Driver mDriver;
    private String filePath;
    private String fileName;
    private Handler handler;

    @Override
    protected void initEventAndData() {
        String[] output = getResources().getStringArray(R.array.output_array);
        outputAdapter = new ArrayAdapter<String>(mainActivity,
                R.layout.simple_spinner_item, output);
        mOutputSpinner.setAdapter(outputAdapter);

        String[] frequence = getResources().getStringArray(R.array.area_array);
        frequenceAdapter = new ArrayAdapter<String>(mainActivity,
                R.layout.simple_spinner_item, frequence);
        frequenceAdapter.setDropDownViewResource(R.layout.frequence_spinner_dropdown_item);
        mFrequenceSpinner.setAdapter(frequenceAdapter);

        mData.setText(SharePreferenceUtils.getInstance().getFilterData());
        mStart.setText(SharePreferenceUtils.getInstance().getFilterStartArea());
        mLength.setText(SharePreferenceUtils.getInstance().getFilterLength());
       /* mOutputSpinner.setSelection(SharePreferenceUtils.getInstance().getOutput());
        mFrequenceSpinner.setSelection(SharePreferenceUtils.getInstance().getfrequence());
        mModeSpinner.setSelection(SharePreferenceUtils.getInstance().getWorkMode());*/
        getOutput(false);
        getFrequence(false);
        getMode(false);
        final ProgressDialog waitingDialog =
                new ProgressDialog(mainActivity);
        waitingDialog.setTitle("提示");
        waitingDialog.setMessage("升级中...");
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.arg1) {
                    case SHOW_UPDATE_PROGRESS:
                        mUpdate.setEnabled(false);
                        waitingDialog.show();
                        break;
                    case UPDATE_SUCESS:
                        mUpdate.setEnabled(true);
                        waitingDialog.dismiss();
                        Toast.makeText(mainActivity, R.string.update_success, Toast.LENGTH_SHORT).show();
                        break;
                    case UPDATE_FAILED:
                        mUpdate.setEnabled(true);
                        waitingDialog.dismiss();
                        Toast.makeText(mainActivity, R.string.update_failed, Toast.LENGTH_SHORT).show();
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
        return R.layout.activity_settings;
    }

    @OnClick({R.id.output_get, R.id.output_set, R.id.frequence_get, R.id.frequence_set, R.id.mode_get,
            R.id.mode_set, R.id.filter_set, R.id.filter_clear, R.id.choose_file, R.id.update_set})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.output_get:
                getOutput(true);
                break;
            case R.id.output_set:
                setOutput();
                break;
            case R.id.frequence_get:
                getFrequence(true);
                break;
            case R.id.frequence_set:
                setFrequence();
                break;
            case R.id.mode_get:
                getMode(true);
                break;
            case R.id.mode_set:
                setMode();
                break;
            case R.id.filter_set:
                filterSet();
                break;
            case R.id.filter_clear:
                filterClear();
                break;
            case R.id.choose_file:
                selectFile();
                break;
            case R.id.update_set:
                requestPermission();
                break;
        }
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("提示");
        builder.setMessage("确定升级模块？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String temp = mDriver.readUM7fwOnce();
                if ("-1000".equals(temp) ||"-1020" .equals(temp)) {
                    Toast.makeText(mainActivity, R.string.update_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (fileName != null && fileName.endsWith("bin")) {
                    new DownLoadThread().start();
                } else {
                    Toast.makeText(mainActivity, R.string.select_right_file, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
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

    private void filterSet() {
        if(!checkOutEditext()){
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

    private void setMode() {
        int poisiton = mModeSpinner.getSelectedItemPosition();
        if (mDriver.Inventory_Model_Set(poisiton, mModeSave.isChecked())) {
            SharePreferenceUtils.getInstance().setWorkMode(poisiton);
            Toast.makeText(mainActivity, R.string.set_sucess, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity, R.string.set_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void getMode(boolean isShowSucToast) {
        int val = mDriver.Inventory_Model_Get();
        if (-1000 == val) {
            Toast.makeText(mainActivity, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (-1020 == val) {
            Toast.makeText(mainActivity, R.string.get_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        mModeSpinner.setSelection(val);
        if(isShowSucToast)
        Toast.makeText(mainActivity, R.string.get_sucess, Toast.LENGTH_SHORT).show();
    }

    private void setFrequence() {
        int sel = mFrequenceSpinner.getSelectedItemPosition();
        int status = mDriver.SetRegion(sel);
        if (-1000 == status) {
            Toast.makeText(mainActivity, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (-1020 == status || 0 == status) {
            Toast.makeText(mainActivity, R.string.set_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        SharePreferenceUtils.getInstance().setFrequence(mFrequenceSpinner.getSelectedItemPosition());
        Toast.makeText(mainActivity, R.string.set_sucess, Toast.LENGTH_SHORT).show();
    }

    private void getFrequence(boolean isShowSucToast ) {
        mFrequenceSpinner.setSelection(-1);
        String sum;
        sum = mDriver.getRegion();
        if (sum.equals("-1000")) {
            Toast.makeText(mainActivity, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (sum.equals("-1020")) {
            Toast.makeText(mainActivity, R.string.get_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String text1 = sum.substring(2, 4);
        int i = Integer.parseInt(text1, 16);
        switch (i) {
            case 0x01:
                mFrequenceSpinner.setSelection(0);
                break;
            case 0x02:
                mFrequenceSpinner.setSelection(1);
                break;
            case 0x04:
                mFrequenceSpinner.setSelection(2);
                break;
            case 0x08:
                mFrequenceSpinner.setSelection(3);
                break;
            case 0x16:
                mFrequenceSpinner.setSelection(4);
                break;
            case 0x32:
                mFrequenceSpinner.setSelection(5);
                break;
            default:
                break;
        }
        if(isShowSucToast)
        Toast.makeText(mainActivity, R.string.get_sucess, Toast.LENGTH_SHORT).show();
    }

    private void setOutput() {
        int val = mOutputSpinner.getSelectedItemPosition() + 5;
        int status = mDriver.setTxPowerOnce(val);
        if (-1000 == status) {
            Toast.makeText(mainActivity, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (-1020 == status || 0 == status) {
            Toast.makeText(mainActivity, R.string.set_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        SharePreferenceUtils.getInstance().setOutput(mOutputSpinner.getSelectedItemPosition());
        Toast.makeText(mainActivity, R.string.set_sucess, Toast.LENGTH_SHORT).show();
    }

    private void getOutput(boolean isShowSucToast) {
        int text = mDriver.GetTxPower();

        if (-1020 == text) {
            Toast.makeText(mainActivity, R.string.get_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        if (-1000 == text) {
            Toast.makeText(mainActivity, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (text < 5) {
            Toast.makeText(mainActivity, R.string.get_failed, Toast.LENGTH_SHORT).show();
        } else {
            String text1 = String.valueOf(text);
            int poistion = outputAdapter.getPosition(text1);
            mOutputSpinner.setSelection(poistion);
            if(isShowSucToast)
            Toast.makeText(mainActivity, R.string.get_sucess, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = uri.getPath();
            String authority = uri.getAuthority();
            String[] split = path.split(":");
            filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
            String[] nameSplit = filePath.split("/");
            fileName = nameSplit[nameSplit.length - 1];
            mSelectedFile.setText(filePath);
        }

    }

    private class DownLoadThread extends Thread {

        public DownLoadThread() {

        }

        @Override
        public void run() {
            Log.i("DownLoad", "Path: " + filePath + " FileName: " + fileName);
            Log.e(TAG, "start time===" + new Date());
            Message startMsg = new Message();
            startMsg.arg1 = SHOW_UPDATE_PROGRESS;
            handler.sendMessage(startMsg);
            int value = mDriver.Down_LoadFw(filePath, fileName);
            Log.e(TAG, "end time===" + new Date());
            Log.e(TAG, "value===" + value);
            Message endMes = new Message();
            endMes.arg1 = value;
            handler.sendMessage(endMes);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showUpdateDialog();
        } else {
            Toast.makeText(mainActivity, R.string.not_get_storage_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(mainActivity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        10);
            } else {
                ActivityCompat.requestPermissions(mainActivity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        10);
            }
        } else {
            showUpdateDialog();
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
}
