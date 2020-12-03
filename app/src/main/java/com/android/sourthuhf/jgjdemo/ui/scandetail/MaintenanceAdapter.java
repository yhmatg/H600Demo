package com.android.sourthuhf.jgjdemo.ui.scandetail;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.sourthuhf.R;
import com.android.sourthuhf.UhfApplication;
import com.android.sourthuhf.jgjdemo.database.bean.MaintenanceBean;
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;

import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MyHoder> {
    private List<MaintenanceBean> maintenanceBeans;
    private Context mContext;

    public MaintenanceAdapter(List<MaintenanceBean> maintenanceBeans, Context mContext) {
        this.maintenanceBeans = maintenanceBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.maintenance_item_layout, viewGroup, false);
        return new MyHoder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHoder myHoder, int i) {
        MaintenanceBean maintenanceBean = maintenanceBeans.get(i);
        myHoder.mTime.setText(maintenanceBean.getTime());
        myHoder.mName.setText(maintenanceBean.getName());
    }

    @Override
    public int getItemCount() {
        return maintenanceBeans == null ? 0 : maintenanceBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder {

        private TextView mTime;
        private TextView mName;

        private MyHoder(View itemView) {
            super(itemView);
            mTime = (TextView) itemView.findViewById(R.id.tv_time);
            mName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}
