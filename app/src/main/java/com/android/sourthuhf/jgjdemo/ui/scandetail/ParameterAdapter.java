package com.android.sourthuhf.jgjdemo.ui.scandetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.sourthuhf.R;
import com.android.sourthuhf.jgjdemo.database.bean.ParameterBean;

import java.util.List;

public class ParameterAdapter extends RecyclerView.Adapter<ParameterAdapter.MyHoder> {
    private List<ParameterBean> paraBeans;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public ParameterAdapter(List<ParameterBean> paraBeans, Context mContext) {
        this.paraBeans = paraBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.param_item_layout, viewGroup, false);
        return new MyHoder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHoder myHoder, int i) {
        ParameterBean parameterBean = paraBeans.get(i);
        myHoder.mIndex.setText(String.valueOf(i + 1));
        myHoder.mPerformance.setText(parameterBean.getPerformance());
        myHoder.mSkill.setText(parameterBean.getSkill());
        myHoder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null){
                    onItemClickListener.onParamClick(parameterBean);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return paraBeans == null ? 0 : paraBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder {

        private TextView mIndex;
        private TextView mPerformance;
        private TextView mSkill;
        private RelativeLayout mLayout;

        private MyHoder(View itemView) {
            super(itemView);
            mIndex = itemView.findViewById(R.id.tv_index);
            mPerformance =  itemView.findViewById(R.id.tv_performance);
            mSkill = itemView.findViewById(R.id.tv_skill);
            mLayout = itemView.findViewById(R.id.parameter_layout);
        }
    }

    public interface OnItemClickListener {
        void onParamClick(ParameterBean parameterBean);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
