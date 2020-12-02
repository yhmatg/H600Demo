package com.android.sourthuhf.original;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.sourthuhf.R;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;

public class LockFragment extends BaseFragment {

    @BindView(R.id.et_pass_word)
    EditText mPassword;
    @BindView(R.id.et_start)
    EditText mStart;
    @BindView(R.id.et_length)
    EditText mLength;
    @BindView(R.id.et_data)
    EditText mData;
    @BindView(R.id.spinner_simple)
    Spinner mSpinner;
    @BindView(R.id.lock_button)
    TextView mLock;
    @BindView(R.id.unlock_btn)
    TextView mUnlock;
    MainActivity mainActivity;
    Driver mDriver;
    public static final String TAG = "LockFragment";
    @Override
    protected void initEventAndData() {
        String[] spinnerItems = {"EPC","TID","USER"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mainActivity,
                R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(R.layout.my_spinner_opt_item);
        mSpinner.setAdapter(spinnerAdapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_lock;
    }

    @OnClick({R.id.lock_button, R.id.unlock_btn})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.lock_button:
                lock();
                break;
            case R.id.unlock_btn:
                unlock();
                break;
        }
    }

    public void lock(){
        if(!checkOutEditext()){
            return;
        }
        int Status = 0;
        int ads = Integer.valueOf(mStart.getText().toString());
        int len = Integer.valueOf(mLength.getText().toString());
        int val = 1;
        int lockitem =  mSpinner.getSelectedItemPosition() + 1;
        Status = mDriver.Lock_Tag_Data(mPassword.getText().toString(), val, ads, len, mData.getText().toString(), lockitem);
        Log.e(TAG, "Status===" + Status);
        if (-1 == Status) {
            Toast.makeText(mainActivity, R.string.lock_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity, R.string.lock_suc, Toast.LENGTH_SHORT).show();
        }
    }

    public void unlock(){
        if(!checkOutEditext()){
            return;
        }
        int Status = 0;
        int ads = Integer.valueOf(mStart.getText().toString());
        int len = Integer.valueOf(mLength.getText().toString());
        int val =  1;
        int lockitem =  mSpinner.getSelectedItemPosition() + 1;
        Status = mDriver.unLock_Tag_Data(mPassword.getText().toString(), val, ads, len, mData.getText().toString(),lockitem);
        Log.e(TAG, "Status===" + Status);
        if (-1 == Status) {
            Toast.makeText(mainActivity, R.string.unlock_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity, R.string.unlock_suc, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkOutEditext() {
        boolean result = false;
        if ( mPassword.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.passwork_null, Toast.LENGTH_SHORT).show();
        } else if (mStart.getText().toString().length() ==0 ) {
            Toast.makeText(mainActivity, R.string.start_area_null, Toast.LENGTH_SHORT).show();
        } else if (mLength.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.length_null, Toast.LENGTH_SHORT).show();
        } else if ( mData.getText().toString().length() == 0) {
            Toast.makeText(mainActivity, R.string.data_null, Toast.LENGTH_SHORT).show();
        }else {
            result =  true;
        }
        return result;
    }
}
