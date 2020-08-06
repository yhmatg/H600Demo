package com.android.sourthuhf.njdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.sourthuhf.MainActivity;
import com.android.sourthuhf.R;
import com.android.sourthuhf.SharePreferenceUtils;
import com.android.sourthuhf.njdemo.http.RetrofitClient;
import com.android.sourthuhf.njdemo.http.StringUtils;
import com.android.sourthuhf.njdemo.http.WmsApi;
import com.android.sourthuhf.njdemo.parambean.LoginParam;
import com.android.sourthuhf.njdemo.responsebean.LoginResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    private Unbinder unBinder;
    @BindView(R.id.edit_account)
    EditText mAccountEdit;
    @BindView(R.id.edit_password)
    EditText mPasswordEdit;
    @BindView(R.id.btn_login)
    Button mLoginBtn;
    @BindView(R.id.password_invisible)
    ImageView ivEye;
    private boolean isOpenEye = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unBinder = ButterKnife.bind(this);
        initDataAndView();
    }

    private void initDataAndView() {
        mAccountEdit.setText(SharePreferenceUtils.getInstance().getUserName());
        mPasswordEdit.setText(SharePreferenceUtils.getInstance().getPassWord());
    }

    @OnClick({R.id.btn_login,R.id.password_invisible}) void performClick(View view) {
        switch (view.getId()) {
            case R.id.password_invisible:
                if (isOpenEye) {
                    ivEye.setSelected(false);
                    isOpenEye = false;
                    ivEye.setImageResource(R.drawable.hide_icon);
                    mPasswordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    ivEye.setSelected(true);
                    isOpenEye = true;
                    ivEye.setImageResource(R.drawable.show_icon);
                    mPasswordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                break;
            case R.id.btn_login:
                if (StringUtils.isEmpty(mAccountEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "请输入账号！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (StringUtils.isEmpty(mPasswordEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "请输入密码！", Toast.LENGTH_SHORT).show();
                    return;
                }
                LoginParam loginParam = new LoginParam(mAccountEdit.getText().toString(), mPasswordEdit.getText().toString());
                login(loginParam);
                break;
        }
    }

    public void login(final LoginParam loginParam){
        RetrofitClient.getInstance().create(WmsApi.class).login(loginParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<LoginResponse>() {
                    @Override
                    public void onNext(LoginResponse loginResponse) {
                        if("0000000".equals(loginResponse.getCode())){
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            SharePreferenceUtils.getInstance().setUserName(loginParam.getUsername());
                            SharePreferenceUtils.getInstance().setPassWord(loginParam.getPassword());
                            finish();
                        }else {
                            String errMes = "登录失败 " + (loginResponse.getMessage() == null ? "" : loginResponse.getMessage());
                            Toast.makeText(LoginActivity.this, errMes, Toast.LENGTH_SHORT).show();
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unBinder != null && unBinder != Unbinder.EMPTY) {
            unBinder.unbind();
            unBinder = null;
        }
    }


}
