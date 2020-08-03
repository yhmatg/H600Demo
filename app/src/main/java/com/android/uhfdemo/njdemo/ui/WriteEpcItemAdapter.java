package com.android.uhfdemo.njdemo.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.uhfdemo.R;

import java.util.ArrayList;
import java.util.List;

public class WriteEpcItemAdapter extends RecyclerView.Adapter<WriteEpcItemAdapter.MyHoder> {
    private List<WriteEpcBean> epcBeans;
    private  Context mContext;
    private List<WriteEpcBean>  selectedepcBeans = new ArrayList<>();

    public WriteEpcItemAdapter(List<WriteEpcBean> epcBeans, Context mContext) {
        this.epcBeans = epcBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.write_epc_item_layout, viewGroup, false);
        return new MyHoder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHoder myHoder, int i) {
        final WriteEpcBean epcBean = epcBeans.get(i);
        myHoder.sn.setText(String.valueOf(i+1));
        myHoder.epc.setText(epcBean.getEpc());
        String status = epcBean.isWrite() ? "已写":"未写";
        myHoder.writeStatus.setText(status);

    }

    @Override
    public int getItemCount() {
        return epcBeans == null ? 0 : epcBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder{

        private TextView sn;
        private TextView epc;
        private TextView writeStatus;

        private MyHoder(View itemView) {
            super(itemView);

            sn = (TextView) itemView.findViewById(R.id.sn);
            epc = (TextView) itemView.findViewById(R.id.epc);
            writeStatus = (TextView) itemView.findViewById(R.id.write_status);
        }
    }

    public List<WriteEpcBean> getSelectedepcBeans() {
        return selectedepcBeans;
    }
}
