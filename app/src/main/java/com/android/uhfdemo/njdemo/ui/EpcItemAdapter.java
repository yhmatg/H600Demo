package com.android.uhfdemo.njdemo.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.uhfdemo.R;

import java.util.List;

public class EpcItemAdapter extends RecyclerView.Adapter<EpcItemAdapter.MyHoder> {
    private List<EpcBean> epcBeans;
    private  Context mContext;
    private List<EpcBean>  selectedepcBeans;

    public EpcItemAdapter(List<EpcBean> epcBeans, Context mContext) {
        this.epcBeans = epcBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.epc_item_layout, viewGroup, false);
        return new MyHoder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHoder myHoder, int i) {
        final EpcBean epcBean = epcBeans.get(i);
        myHoder.sn.setText(String.valueOf(epcBean.getSn()));
        myHoder.epc.setText(epcBean.getEpc());
        myHoder.count.setText(String.valueOf(epcBean.getCount()));
        myHoder.rssi.setText(String.valueOf(epcBean.getRssi()));
        myHoder.select.setChecked(epcBean.isSelect());
        myHoder.select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myHoder.select.isChecked()){
                    epcBean.setSelect(true);
                    if(!selectedepcBeans.contains(epcBean)){
                        selectedepcBeans.add(epcBean);
                    }
                }else {
                    epcBean.setSelect(false);
                    selectedepcBeans.remove(epcBean);
                }
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return epcBeans == null ? 0 : epcBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder{

        private CheckBox select;
        private TextView sn;
        private TextView epc;
        private TextView count;
        private TextView rssi;

        private MyHoder(View itemView) {
            super(itemView);

            select = (CheckBox) itemView.findViewById(R.id.cb_select);
            sn = (TextView) itemView.findViewById(R.id.sn);
            epc = (TextView) itemView.findViewById(R.id.epc);
            count = (TextView) itemView.findViewById(R.id.count);
            rssi = (TextView) itemView.findViewById(R.id.rssi);
        }
    }

    public List<EpcBean> getSelectedepcBeans() {
        return selectedepcBeans;
    }
}
