package com.android.sourthuhf.jgjdemo.ui.scandetail;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.android.sourthuhf.jgjdemo.database.bean.ParameterBean;
import com.android.sourthuhf.jgjdemo.database.room.BaseDb;
import com.android.sourthuhf.njdemo.http.StringUtils;
import com.android.sourthuhf.original.BaseFragment;
import com.android.sourthuhf.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PictureFragmentOne extends BaseFragment implements ParameterAdapter.OnItemClickListener {
    @BindView(R.id.title_back)
    ImageView titleLeft;
    @BindView(R.id.title_content)
    TextView title;
    @BindView(R.id.tv_sure)
    TextView newPara;
    @BindView(R.id.rv_para)
    RecyclerView mRecycleView;
    private MaterialDialog detailDialog;
    private TextView detailPerformance;
    private TextView detailSkill;
    private TextView detailContent;
    private Button detailDelete;
    private Button detailCancle;
    private MaterialDialog newDialog;
    private EditText newPerformance;
    private EditText newSkill;
    private TextView newContent;
    private Button newSure;
    private Button newCancel;
    private ParameterAdapter mAdapter;
    private List<ParameterBean> parameterBeans = new ArrayList<>();
    private int currentDeviceId;
    private ParameterBean selectBean;
    private boolean isChange;

    @Override
    protected void initEventAndData() {
        titleLeft.setVisibility(View.GONE);
        title.setText("设备详情");
        newPara.setText("添加");
        newPara.setVisibility(View.VISIBLE);
        mAdapter = new ParameterAdapter(parameterBeans, getActivity());
        mAdapter.setOnItemClickListener(this);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        currentDeviceId = UhfApplication.getInstance().getCurrentDeviceId();
        parameterBeans.addAll(BaseDb.getInstance().getParameterDao().findParamByDeviceId(currentDeviceId));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_one;
    }

    @OnClick({R.id.tv_sure})
    public void performClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sure:
                isChange = false;
                showOpenDialog();
                newPerformance.setText("");
                newSkill.setText("");
                break;
        }
    }

    @Override
    public void onParamClick(ParameterBean parameterBean) {
        isChange = true;
        selectBean = parameterBean;
        showDetailDialog();
        detailPerformance.setText(parameterBean.getPerformance());
        detailSkill.setText(parameterBean.getSkill());
    }

    public void showDetailDialog() {
        if (detailDialog != null) {
            detailDialog.show();
        } else {
            View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.mainteance_detail_dialog, null);
            detailPerformance = contentView.findViewById(R.id.et_maintenance_name);
            detailSkill = contentView.findViewById(R.id.et_maintenance_time);
            detailContent = contentView.findViewById(R.id.et_maintenance_content);
            TextView tvName = contentView.findViewById(R.id.tv_name);
            TextView tvTime = contentView.findViewById(R.id.tv_time);
            TextView tvParam = contentView.findViewById(R.id.tv_title);
            tvParam.setText("参数详情");
            tvName.setText("性能指标");
            tvTime.setText("技术参数");
            detailContent.setVisibility(View.GONE);
            detailDelete = contentView.findViewById(R.id.bt_sure);
            detailCancle = contentView.findViewById(R.id.bt_cancel);
            detailDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parameterBeans.remove(selectBean);
                    mAdapter.notifyDataSetChanged();
                    BaseDb.getInstance().getParameterDao().deleteItem(selectBean);
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

    public void showOpenDialog() {
        if (newDialog != null) {
            newDialog.show();
        } else {
            View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.new_history_dialog, null);
            newSure = contentView.findViewById(R.id.bt_sure);
            newCancel = contentView.findViewById(R.id.bt_cancel);
            newPerformance = contentView.findViewById(R.id.et_maintenance_name);
            newSkill = contentView.findViewById(R.id.et_maintenance_time);
            newContent = contentView.findViewById(R.id.et_maintenance_content);
            newContent.setVisibility(View.GONE);
            TextView tvName = contentView.findViewById(R.id.tv_name);
            TextView tvTime = contentView.findViewById(R.id.tv_time);
            tvName.setText("性能指标");
            tvTime.setText("技术参数");
            newPerformance.setHint("性能指标");
            newSkill.setHint("技术参数");
            newSure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nameStr = newPerformance.getText().toString();
                    String timeStr = newSkill.getText().toString();
                    if (StringUtils.isEmpty(nameStr)) {
                        ToastUtils.showShort("请性能指标");
                        return;
                    }
                    if (StringUtils.isEmpty(timeStr)) {
                        ToastUtils.showShort("请输入技术参数");
                        return;
                    }
                    ParameterBean parameterBean = new ParameterBean(currentDeviceId, timeStr, nameStr);
                    parameterBeans.add(parameterBean);
                    mAdapter.notifyDataSetChanged();
                    BaseDb.getInstance().getParameterDao().insertItem(parameterBean);
                    dismissNewDialog();
                }
            });
            newCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissNewDialog();
                }
            });
            newDialog = new MaterialDialog.Builder(getActivity())
                    .customView(contentView, false)
                    .show();
            Window window = newDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public void dismissNewDialog() {
        if (newDialog != null && newDialog.isShowing()) {
            newDialog.dismiss();
        }
    }
}
