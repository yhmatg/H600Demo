package com.android.sourthuhf.jgjdemo.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.sourthuhf.R;
import com.android.sourthuhf.SharePreferenceUtils;
import com.android.sourthuhf.ToastUtils;
import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;
import com.android.sourthuhf.jgjdemo.database.room.BaseDb;
import com.android.sourthuhf.jgjdemo.ui.scandetail.JieganHomeActivity;
import com.android.sourthuhf.jgjdemo.ui.scandetail.TooltemAdapter;
import com.android.sourthuhf.njdemo.http.StringUtils;
import com.android.sourthuhf.original.BaseActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DeviceListActivity extends BaseActivity implements TooltemAdapter.OnItemClickListener {
    @BindView(R.id.rv_devices)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    TooltemAdapter deviceAdapter;
    private List<ToolBean> mDevices = new ArrayList<>();
    private MaterialDialog openDialog;
    private EditText deviceName;
    private EditText deviceCode;
    private EditText epcCode;
    private Spinner typeSpinner;
    private ToolBean mSelectToolBean;
    private boolean isChange;
    private Button confirmBt;
    private Button deleteBt;

    private void initData() {
        if (SharePreferenceUtils.getInstance().getIsFirst()) {
            List<ToolBean> toolBeans = new ArrayList<>();
            toolBeans.add(new ToolBean("100000000000000000000001", "000001", "造粒机", 0));
            toolBeans.add(new ToolBean("100000000000000000000002", "000002", "粗粉旋切机", 1));
            toolBeans.add(new ToolBean("100000000000000000000003", "000003", "细粉旋切机", 2));
            toolBeans.add(new ToolBean("100000000000000000000004", "000004", "除尘机", 3));
            toolBeans.add(new ToolBean("100000000000000000000005", "000005", "烘干机", 4));
            toolBeans.add(new ToolBean("100000000000000000000006", "000006", "搅拌机", 5));
            toolBeans.add(new ToolBean("100000000000000000000007", "000007", "抛圆机", 6));
            toolBeans.add(new ToolBean("100000000000000000000008", "000008", "筛分机", 7));
            toolBeans.add(new ToolBean("100000000000000000000009", "000009", "主料进料机", 8));
            toolBeans.add(new ToolBean("100000000000000000000010", "000010", "自动包装机", 9));
            toolBeans.add(new ToolBean("100000000000000000000011", "000011", "辅料进料机", 10));
            BaseDb.getInstance().getToolDao().insertItems(toolBeans);
            ArrayList<MaintenanceBean> maintenanceBeans = new ArrayList<>();
            toolBeans = BaseDb.getInstance().getToolDao().findAllTools();
            for (ToolBean toolBean : toolBeans) {
                maintenanceBeans.add(new MaintenanceBean(toolBean.getId(), "2020/12/24", "张三", 0));
                maintenanceBeans.add(new MaintenanceBean(toolBean.getId(), "2020/11/17", "张三", 1));
                maintenanceBeans.add(new MaintenanceBean(toolBean.getId(), "2020/11/4", "张三", 1));
                maintenanceBeans.add(new MaintenanceBean(toolBean.getId(), "2020/11/3", "张三", 1));
                maintenanceBeans.add(new MaintenanceBean(toolBean.getId(), "2020/10/27", "张三", 1));
                maintenanceBeans.add(new MaintenanceBean(toolBean.getId(), "2020/10/20", "张三", 1));
            }
            BaseDb.getInstance().getMaintenanceDao().insertItems(maintenanceBeans);
            SharePreferenceUtils.getInstance().setIsFirst(false);
        }
        mDevices.clear();
        mDevices.addAll(BaseDb.getInstance().getToolDao().findAllTools());
        deviceAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                isChange = false;
                showOpenDialog();
                if(openDialog != null){
                    deviceName.setText("");
                    deviceCode.setText("");
                    epcCode.setText("");
                    deleteBt.setText("取消");
                    typeSpinner.setSelection(0);
                }
                return true;
            case R.id.scan_device:
                startActivity(new Intent(this, JieganHomeActivity.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void initEventAndData() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        deviceAdapter = new TooltemAdapter(mDevices, this);
        deviceAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(deviceAdapter);
        initData();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device;
    }

    public void showOpenDialog() {
        if (openDialog != null) {
            openDialog.show();
        } else {
            View contentView = LayoutInflater.from(this).inflate(R.layout.new_device_dialog, null);
            confirmBt = contentView.findViewById(R.id.bt_add_device);
            deleteBt = contentView.findViewById(R.id.bt_delete_device);
            deviceName = contentView.findViewById(R.id.et_device_name);
            deviceCode = contentView.findViewById(R.id.et_device_code);
            epcCode = contentView.findViewById(R.id.et_epc);
            typeSpinner = contentView.findViewById(R.id.spinner_type);
            confirmBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nameStr = deviceName.getText().toString();
                    if (StringUtils.isEmpty(nameStr)) {
                        ToastUtils.showShort("设备名称不能为空");
                        return;
                    }
                    String codeStr = deviceCode.getText().toString();
                    if (StringUtils.isEmpty(codeStr)) {
                        ToastUtils.showShort("设备编号不能为空");
                        return;
                    }
                    String epcStr = epcCode.getText().toString();
                    if (StringUtils.isEmpty(epcStr)) {
                        ToastUtils.showShort("EPC数据不能为空");
                        return;
                    }
                    int selectedItemPosition = typeSpinner.getSelectedItemPosition();
                    if (isChange) {
                        mSelectToolBean.setName(nameStr);
                        mSelectToolBean.setCode(codeStr);
                        mSelectToolBean.setEpc(epcStr);
                        mSelectToolBean.setType(selectedItemPosition);
                        BaseDb.getInstance().getToolDao().insertItem(mSelectToolBean);
                    } else {
                        ToolBean toolBean = new ToolBean(epcStr, codeStr, nameStr, selectedItemPosition);
                        mDevices.add(toolBean);
                        BaseDb.getInstance().getToolDao().insertItem(toolBean);
                    }
                    deviceAdapter.notifyDataSetChanged();
                    dismissUpdateDialog();
                }
            });
            deleteBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isChange){
                        mDevices.remove(mSelectToolBean);
                        deviceAdapter.notifyDataSetChanged();
                        BaseDb.getInstance().getToolDao().deleteItem(mSelectToolBean);
                        BaseDb.getInstance().getMaintenanceDao().deleteDataByDeviceId(mSelectToolBean.getId());
                    }
                    dismissUpdateDialog();
                }
            });
            openDialog = new MaterialDialog.Builder(this)
                    .customView(contentView, false)
                    .show();
            Window window = openDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

    }

    public void dismissUpdateDialog() {
        if (openDialog != null && openDialog.isShowing()) {
            openDialog.dismiss();
        }
    }

    @Override
    public void onDeviceItemClick(ToolBean toolBean) {
        isChange = true;
        showOpenDialog();
        deviceName.setText(toolBean.getName());
        deviceCode.setText(toolBean.getCode());
        epcCode.setText(toolBean.getEpc());
        typeSpinner.setSelection(toolBean.getType());
        deleteBt.setText("删除");
        mSelectToolBean = toolBean;
    }
}
