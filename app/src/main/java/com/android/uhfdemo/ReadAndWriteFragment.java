package com.android.uhfdemo;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;

public class ReadAndWriteFragment extends BaseFragment {

    @BindView(R.id.rg_areas)
    RadioGroup mRadioGroup;
    @BindView(R.id.et_password)
    EditText mPassword;
    @BindView(R.id.et_start)
    EditText mStart;
    @BindView(R.id.et_length)
    EditText mLength;
    @BindView(R.id.et_data)
    EditText mData;
    @BindView(R.id.read_button)
    TextView mRead;
    @BindView(R.id.write_btn)
    TextView mWrite;
    int selectType = 1;
    MainActivity mainActivity;
    Driver mDriver;
    public static final String TAG = "ReadAndWriteFragment";

    @Override
    protected void initEventAndData() {
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int myCheckedId = group.getCheckedRadioButtonId();
                switch (myCheckedId) {
                    case R.id.rb_ruf:
                        selectType = 0;
                        break;
                    case R.id.rb_epc:
                        selectType = 1;
                        break;
                    case R.id.rb_tid:
                        selectType = 2;
                        break;
                    case R.id.rb_usr:
                        selectType = 3;
                        break;
                }

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
        return R.layout.activity_read_write;
    }

    @OnClick({R.id.read_button, R.id.write_btn})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.read_button:
                readTag();
                break;
            case R.id.write_btn:
                writeTag();
                break;
        }
    }

    public void readTag() {
        if (!checkOutEditext(false)) {
            return;
        }
        String Status;
        String PwdRread = mPassword.getText().toString();
        int MbRead = selectType;
        int ads = Integer.valueOf(mStart.getText().toString());
        int len = Integer.valueOf(mLength.getText().toString());
        Status = mDriver.Read_Data_Tag(PwdRread, 0, 0, 0, "0", MbRead, ads, len);
        Log.e(TAG, "Status===" + Status);
        if (null == Status) {
            Toast.makeText(mainActivity, R.string.read_failed, Toast.LENGTH_SHORT).show();
        } else {
            mData.setText(Status);
            Toast.makeText(mainActivity, R.string.read_suc, Toast.LENGTH_SHORT).show();
        }
    }

    public void writeTag() {
        if (!checkOutEditext(true)) {
            return;
        }
        int Status = 0;
        String PwdWr = mPassword.getText().toString();
        int MbWr = selectType;
        int ads = Integer.valueOf(mStart.getText().toString());
        int len = Integer.valueOf(mLength.getText().toString());
        String data = mData.getText().toString();

        if (1 == MbWr) {
            Status = mDriver.Write_Epc_Data(PwdWr, ads, len, data);
        } else {
            Status = mDriver.Write_Data_Tag(PwdWr, 0, 0, 0, "0", MbWr, ads, len, data);
        }
        Log.e(TAG, "Status===" + Status);
        if (-1 == Status) {
            Toast.makeText(mainActivity, R.string.write_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity, R.string.write_suc, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkOutEditext(boolean isWrite) {
        boolean result = false;
        if ( mPassword.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.passwork_null, Toast.LENGTH_SHORT).show();
        } else if (mStart.getText().toString().length() ==0 ) {
            Toast.makeText(mainActivity, R.string.start_area_null, Toast.LENGTH_SHORT).show();
        } else if (mLength.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.length_null, Toast.LENGTH_SHORT).show();
        } else if (isWrite && mData.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.data_null, Toast.LENGTH_SHORT).show();
        }else {
            result =  true;
        }
        return result;
    }
}
