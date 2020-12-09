package com.android.sourthuhf.jgjdemo.ui.home;

import android.content.Intent;
import android.view.View;

import com.android.sourthuhf.R;
import com.android.sourthuhf.SharePreferenceUtils;
import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;
import com.android.sourthuhf.jgjdemo.database.room.BaseDb;
import com.android.sourthuhf.jgjdemo.ui.device.DeviceListActivity;
import com.android.sourthuhf.jgjdemo.ui.scandetail.ScanDeviceActivity;
import com.android.sourthuhf.original.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class DeviceHomeActivity extends BaseActivity {
    @Override
    protected void initEventAndData() {
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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_home;
    }

    @OnClick({R.id.bt_scan_list, R.id.bt_device_list})
    public void performClick(View view) {
        switch (view.getId()) {
            case R.id.bt_scan_list:
                startActivity(new Intent(this, ScanDeviceActivity.class));
                break;
            case R.id.bt_device_list:
                startActivity(new Intent(this, DeviceListActivity.class));
                break;
        }
    }
}
