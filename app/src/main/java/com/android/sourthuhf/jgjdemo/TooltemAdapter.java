package com.android.sourthuhf.jgjdemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.sourthuhf.R;
import java.util.ArrayList;
import java.util.List;

public class TooltemAdapter extends RecyclerView.Adapter<TooltemAdapter.MyHoder> {
    private List<ToolBean> toolBeans;
    private  Context mContext;

    public TooltemAdapter(List<ToolBean> toolBeans, Context mContext) {
        this.toolBeans = toolBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.tool_item_layout, viewGroup, false);
        return new MyHoder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHoder myHoder, int i) {
        final ToolBean epcBean = toolBeans.get(i);
        myHoder.mName.setText(epcBean.getName());
        myHoder.mEpc.setText(epcBean.getEpc());
        myHoder.toolLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext,DetailActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return toolBeans == null ? 0 : toolBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder{

        private TextView mName;
        private TextView mEpc;
        private TextView mCode; 
        private RelativeLayout toolLayout;

        private MyHoder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.tv_name);
            mEpc = (TextView) itemView.findViewById(R.id.tv_epc);
            toolLayout = (RelativeLayout) itemView.findViewById(R.id.tool_layout);
        }
    }
}
