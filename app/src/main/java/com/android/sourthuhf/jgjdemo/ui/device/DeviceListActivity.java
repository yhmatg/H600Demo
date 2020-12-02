package com.android.sourthuhf.jgjdemo.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.sourthuhf.R;
import com.android.sourthuhf.SharePreferenceUtils;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;
import com.android.sourthuhf.jgjdemo.database.room.BaseDb;
import com.android.sourthuhf.jgjdemo.ui.scandetail.JieganHomeActivity;
import com.android.sourthuhf.jgjdemo.ui.scandetail.TooltemAdapter;
import com.android.sourthuhf.original.BaseActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DeviceListActivity extends BaseActivity {
    @BindView(R.id.rv_devices)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    TooltemAdapter deviceAdapter;
    private List<ToolBean> mDevices = new ArrayList<>();

    private void initData() {
        if(SharePreferenceUtils.getInstance().getIsFirst()){
            ArrayList<ToolBean> toolBeans = new ArrayList<>();
            toolBeans.add(new ToolBean("100000000000000000000001", "000001", "造粒机",0));
            toolBeans.add(new ToolBean("100000000000000000000002", "000002", "粗粉旋切机",1));
            toolBeans.add(new ToolBean("100000000000000000000003", "000003", "细粉旋切机",2));
            toolBeans.add(new ToolBean("100000000000000000000004", "000004", "除尘机",3));
            toolBeans.add(new ToolBean("100000000000000000000005", "000005", "烘干机",4));
            toolBeans.add(new ToolBean("100000000000000000000006", "000006", "搅拌机",5));
            toolBeans.add(new ToolBean("100000000000000000000007", "000007", "抛圆机",6));
            toolBeans.add(new ToolBean("100000000000000000000008", "000008", "筛分机",7));
            toolBeans.add(new ToolBean("100000000000000000000009", "000009", "主料进料机",8));
            toolBeans.add(new ToolBean("100000000000000000000010", "000010", "自动包装机",9));
            toolBeans.add(new ToolBean("100000000000000000000011", "000011", "辅料进料机",10));
            BaseDb.getInstance().getToolDao().insertItems(toolBeans);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(deviceAdapter);
        initData();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device;
    }
}
