package com.android.sourthuhf;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.example.rfid.driver.Driver;

public class DestoryFragment extends BaseFragment {
    @BindView(R.id.et_pass_word)
    EditText mPassword;
    @BindView(R.id.et_start)
    EditText mStart;
    @BindView(R.id.et_length)
    EditText mLength;
    @BindView(R.id.et_data)
    EditText mData;
    @BindView(R.id.destory_button)
    TextView mDestory;
    MainActivity mainActivity;
    Driver mDriver;
    public static final String TAG = "DestoryFragment";

    @Override
    protected void initEventAndData() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) getActivity());
        mDriver = mainActivity.getDriver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_destory;
    }

    @OnClick({R.id.destory_button})
    void performClick(View view) {
        switch (view.getId()) {
            case R.id.destory_button:
                destoryTag();
                break;
        }
    }

    private void destoryTag() {
        if(!checkOutEditext()){
            return;
        }
        int Status = 0;
        int ads = Integer.valueOf(mStart.getText().toString());
        int len = Integer.valueOf(mLength.getText().toString());
        int val =  1;
        Status = mDriver.Kill_Tag(mPassword.getText().toString(), val, ads, len, mData.getText().toString());
        Log.e(TAG, "Status===" + Status);
        if (-1 == Status) {
            Toast.makeText(mainActivity, R.string.kill_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity, R.string.kill_suc, Toast.LENGTH_SHORT).show();
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
