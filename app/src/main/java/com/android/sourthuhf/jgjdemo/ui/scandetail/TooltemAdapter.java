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
import com.android.sourthuhf.jgjdemo.database.bean.ToolBean;

import java.util.List;

public class TooltemAdapter extends RecyclerView.Adapter<TooltemAdapter.MyHoder> {
    private List<ToolBean> toolBeans;
    private Context mContext;

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
                mContext.startActivity(new Intent(mContext, DetailActivity.class));
            }
        });
        switch (epcBean.getType()) {
            case 0:
                myHoder.mImage.setImageResource(R.mipmap.icon_one);
                break;
            case 1:
                myHoder.mImage.setImageResource(R.mipmap.icon_two);
                break;
            case 2:
                myHoder.mImage.setImageResource(R.mipmap.icon_three);
                break;
            case 3:
                myHoder.mImage.setImageResource(R.mipmap.icon_four);
                break;
            case 4:
                myHoder.mImage.setImageResource(R.mipmap.icon_five);
                break;
            case 5:
                myHoder.mImage.setImageResource(R.mipmap.icon_six);
                break;
            case 6:
                myHoder.mImage.setImageResource(R.mipmap.icon_seven);
                break;
            case 7:
                myHoder.mImage.setImageResource(R.mipmap.icon_eight);
                break;
            case 8:
                myHoder.mImage.setImageResource(R.mipmap.icon_nine);
                break;
            case 9:
                myHoder.mImage.setImageResource(R.mipmap.icon_ten);
                break;
            case 10:
                myHoder.mImage.setImageResource(R.mipmap.icon_eleven);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return toolBeans == null ? 0 : toolBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder {

        private TextView mName;
        private TextView mEpc;
        private TextView mCode;
        private ImageView mImage;
        private RelativeLayout toolLayout;

        private MyHoder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.tv_name);
            mEpc = (TextView) itemView.findViewById(R.id.tv_epc);
            mImage = (ImageView) itemView.findViewById(R.id.iv_icon);
            toolLayout = (RelativeLayout) itemView.findViewById(R.id.tool_layout);
        }
    }
}
