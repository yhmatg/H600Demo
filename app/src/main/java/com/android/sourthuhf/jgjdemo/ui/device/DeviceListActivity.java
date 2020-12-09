package com.android.sourthuhf.jgjdemo.ui.device;

import android.content.Intent;
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
import com.android.sourthuhf.jgjdemo.ui.scandetail.ScanDeviceActivity;
import com.android.sourthuhf.jgjdemo.ui.scandetail.TooltemAdapter;
import com.android.sourthuhf.njdemo.http.StringUtils;
import com.android.sourthuhf.original.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import butterknife.BindView;

public class DeviceListActivity extends BaseActivity implements TooltemAdapter.OnItemClickListener {
    @BindView(R.id.rv_devices)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    TooltemAdapter deviceAdapter;
    private List<ToolBean> mDevices = new ArrayList<>();
    private MaterialDialog openDialog;
    private MaterialDialog deleteDialog;
    private EditText deviceName;
    private EditText deviceCode;
    private EditText epcCode;
    private Spinner typeSpinner;
    private ToolBean mSelectToolBean;
    private boolean isChange;
    private Button confirmBt;
    private Button deleteBt;
    private Button deleteConfirm;
    private Button deleteCancle;

    private void initData() {
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
                if (openDialog != null) {
                    deviceName.setText("");
                    deviceCode.setText("");
                    epcCode.setText("");
                    typeSpinner.setSelection(0);
                }
                return true;
            case R.id.delete_device:
                //todo 删除设备
                showDeleteDialog();
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
                    dismissOpenDialog();
                }
            });
            deleteBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if(isChange){
                        mDevices.remove(mSelectToolBean);
                        deviceAdapter.notifyDataSetChanged();
                        BaseDb.getInstance().getToolDao().deleteItem(mSelectToolBean);
                        BaseDb.getInstance().getMaintenanceDao().deleteDataByDeviceId(mSelectToolBean.getId());
                    }*/
                    dismissOpenDialog();
                }
            });
            openDialog = new MaterialDialog.Builder(this)
                    .customView(contentView, false)
                    .show();
            Window window = openDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

    }

    public void dismissOpenDialog() {
        if (openDialog != null && openDialog.isShowing()) {
            openDialog.dismiss();
        }
    }

    public void showDeleteDialog() {
        if (deleteDialog != null) {
            deleteDialog.show();
        } else {
            View contentView = LayoutInflater.from(this).inflate(R.layout.delete_device_dialog, null);
            deleteConfirm = contentView.findViewById(R.id.bt_delete_sure);
            deleteCancle = contentView.findViewById(R.id.bt_delete_cancel);
            deleteConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<ToolBean> selectBeans = deviceAdapter.getSelectBeans();
                    if (selectBeans.size() == 0) {
                        ToastUtils.showShort("请选择需要删除的设备！");
                    } else {
                        mDevices.removeAll(selectBeans);
                        deviceAdapter.notifyDataSetChanged();
                        List<Integer> collect = selectBeans.stream().map(new Function<ToolBean, Integer>() {
                            @Override
                            public Integer apply(ToolBean toolBean) {
                                return toolBean.getId();
                            }
                        }).collect(Collectors.toList());
                        BaseDb.getInstance().getToolDao().deleteItems(selectBeans);
                        BaseDb.getInstance().getMaintenanceDao().deleteDataByDeviceId(collect);
                    }
                    dismissDeleteDialog();
                }
            });
            deleteCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDeleteDialog();
                }
            });
            deleteDialog = new MaterialDialog.Builder(this)
                    .customView(contentView, false)
                    .show();
            Window window = deleteDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

    }

    public void dismissDeleteDialog() {
        if (deleteDialog != null && deleteDialog.isShowing()) {
            deleteDialog.dismiss();
        }
        //取出偶数
        List<Integer> list = Arrays.asList(1,2,3,4);
        //1.for循环
        List<Integer> newList1 = new ArrayList<>();
        for (Integer integer : list) {
            if(integer % 2 == 0){
                newList1.add(integer);
            }
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
        mSelectToolBean = toolBean;
    }
}
