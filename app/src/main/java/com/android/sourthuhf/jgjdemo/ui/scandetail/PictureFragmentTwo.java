package com.android.sourthuhf.jgjdemo.ui.scandetail;

import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.sourthuhf.ToastUtils;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.room.BaseDb;
import com.android.sourthuhf.njdemo.http.StringUtils;
import com.android.sourthuhf.original.BaseFragment;
import com.android.sourthuhf.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PictureFragmentTwo extends BaseFragment implements MaintenanceAdapter.OnItemClickListener {
    @BindView(R.id.tv_next_maintenance_time)
    TextView nextTime;
    @BindView(R.id.rv_maintenance_history)
    RecyclerView mRecycleView;
    @BindView(R.id.title_back)
    ImageView titleLeft;
    @BindView(R.id.title_content)
    TextView title;
    @BindView(R.id.tv_sure)
    TextView newHistory;
    private int currentDeviceId;
    private List<MaintenanceBean> maintenanceHistory = new ArrayList<>();
    private MaintenanceAdapter mAdapter;
    private MaterialDialog openDialog;
    private MaterialDialog detailDialog;
    private EditText name;
    private EditText time;
    private EditText content;
    private int nextId = -1;
    private Button confirmBt;
    private Button cancleBt;
    private TextView detailName;
    private TextView detailTime;
    private TextView detailContent;
    private Button detailDelete;
    private Button detailCancle;
    private MaintenanceBean selectBean;
    private boolean isChange;

    @Override
    protected void initEventAndData() {
        titleLeft.setVisibility(View.GONE);
        title.setText("维保记录");
        newHistory.setText("添加");
        newHistory.setVisibility(View.VISIBLE);
        mAdapter = new MaintenanceAdapter(maintenanceHistory, getActivity());
        mAdapter.setOnItemClickListener(this);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        currentDeviceId = UhfApplication.getInstance().getCurrentDeviceId();
        List<MaintenanceBean> nextMaintenance = BaseDb.getInstance().getMaintenanceDao().findMaintenanceByDeviceId(currentDeviceId, 0);
        if (nextMaintenance.size() > 0) {
            MaintenanceBean maintenanceBean = nextMaintenance.get(0);
            nextTime.setText(maintenanceBean.getTime());
            nextId = maintenanceBean.getId();
        }
        maintenanceHistory.addAll(BaseDb.getInstance().getMaintenanceDao().findMaintenanceByDeviceId(currentDeviceId, 1));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_two;
    }

    @OnClick({R.id.tv_sure})
    public void performClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sure:
                isChange = false;
                showOpenDialog();
                name.setText("");
                time.setText("");
                content.setText("");
                cancleBt.setText("取消");
                break;
        }
    }

    public void showOpenDialog() {
        if (openDialog != null) {
            openDialog.show();
        } else {
            View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.new_history_dialog, null);
            confirmBt = contentView.findViewById(R.id.bt_sure);
            cancleBt = contentView.findViewById(R.id.bt_cancel);
            name = contentView.findViewById(R.id.et_maintenance_name);
            time = contentView.findViewById(R.id.et_maintenance_time);
            content = contentView.findViewById(R.id.et_maintenance_content);
            confirmBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nameStr = name.getText().toString();
                    String timeStr = time.getText().toString();
                    String contentStr = content.getText().toString();
                    if (StringUtils.isEmpty(nameStr)) {
                        ToastUtils.showShort("请输入维保人");
                        return;
                    }
                    if (StringUtils.isEmpty(contentStr)) {
                        ToastUtils.showShort("请输入维保内容");
                        return;
                    }
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    boolean isCorrect = false;
                    Date parse = new Date();
                    try {
                        parse = format.parse(timeStr);
                        isCorrect = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!isCorrect) {
                        ToastUtils.showShort("请输入正确格式的维保日期");
                        return;
                    }
                    if (isChange) {
                        selectBean.setName(nameStr);
                        selectBean.setTime(timeStr);
                        selectBean.setContent(contentStr);
                        if (parse.getTime() > System.currentTimeMillis()) {
                            selectBean.setType(0);
                            if (nextId != -1) {
                                selectBean.setId(nextId);
                            }
                            nextTime.setText(selectBean.getTime());
                            maintenanceHistory.remove(selectBean);
                        }
                        mAdapter.notifyDataSetChanged();
                        BaseDb.getInstance().getMaintenanceDao().insertItem(selectBean);
                    } else {
                        MaintenanceBean maintenanceBean = new MaintenanceBean(currentDeviceId, timeStr, nameStr, 0);
                        maintenanceBean.setContent(contentStr);
                        if (parse.getTime() > System.currentTimeMillis()) {
                            maintenanceBean.setType(0);
                            if (nextId != -1) {
                                maintenanceBean.setId(nextId);
                            }
                            nextTime.setText(maintenanceBean.getTime());
                        } else {
                            maintenanceBean.setType(1);
                            maintenanceHistory.add(maintenanceBean);
                            mAdapter.notifyDataSetChanged();
                        }
                        BaseDb.getInstance().getMaintenanceDao().insertItem(maintenanceBean);
                    }
                    dismissUpdateDialog();
                }
            });
            cancleBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if(isChange){
                        maintenanceHistory.remove(selectBean);
                        mAdapter.notifyDataSetChanged();
                        BaseDb.getInstance().getMaintenanceDao().deleteItem(selectBean);
                    }*/
                    dismissUpdateDialog();
                }
            });
            openDialog = new MaterialDialog.Builder(getActivity())
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

    public void showDetailDialog() {
        if (detailDialog != null) {
            detailDialog.show();
        } else {
            View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.mainteance_detail_dialog, null);
            detailName = contentView.findViewById(R.id.et_maintenance_name);
            detailTime = contentView.findViewById(R.id.et_maintenance_time);
            detailContent = contentView.findViewById(R.id.et_maintenance_content);
            detailDelete = contentView.findViewById(R.id.bt_sure);
            detailCancle = contentView.findViewById(R.id.bt_cancel);
            detailDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    maintenanceHistory.remove(selectBean);
                    mAdapter.notifyDataSetChanged();
                    BaseDb.getInstance().getMaintenanceDao().deleteItem(selectBean);
                    dismissDetailDialog();
                }
            });
            detailCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDetailDialog();
                }
            });
            detailDialog = new MaterialDialog.Builder(getActivity())
                    .customView(contentView, false)
                    .show();
            Window window = detailDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

    }

    public void dismissDetailDialog() {
        if (detailDialog != null && detailDialog.isShowing()) {
            detailDialog.dismiss();
        }
    }

    @Override
    public void onMaintenanceClick(MaintenanceBean maintenanceBean) {
        selectBean = maintenanceBean;
        isChange = true;
        showDetailDialog();
        detailName.setText(maintenanceBean.getName());
        detailTime.setText(maintenanceBean.getTime());
        detailContent.setText(maintenanceBean.getContent());
    }
}
