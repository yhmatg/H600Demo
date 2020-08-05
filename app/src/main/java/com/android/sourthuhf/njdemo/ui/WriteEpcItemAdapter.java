package com.android.sourthuhf.njdemo.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.sourthuhf.R;

import java.util.ArrayList;
import java.util.List;

public class WriteEpcItemAdapter extends RecyclerView.Adapter<WriteEpcItemAdapter.MyHoder> {
    private List<WriteEpcBean> epcBeans;
    private  Context mContext;
    private List<WriteEpcBean>  selectedepcBeans = new ArrayList<>();
    private WriteEpcItemClickListener writeClickListener;

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
        myHoder.writeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(writeClickListener != null){
                    writeClickListener.onWriteEpcClick(epcBean);
                }
            }
        });
        myHoder.writeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(writeClickListener != null){
                    writeClickListener.onEpcItemClick(epcBean);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return epcBeans == null ? 0 : epcBeans.size();
    }

    class MyHoder extends RecyclerView.ViewHolder{

        private TextView sn;
        private TextView epc;
        private Button writeStatus;
        private LinearLayout writeLayout;

        private MyHoder(View itemView) {
            super(itemView);

            sn = (TextView) itemView.findViewById(R.id.sn);
            epc = (TextView) itemView.findViewById(R.id.epc);
            writeStatus = (Button) itemView.findViewById(R.id.write_status);
            writeLayout = (LinearLayout) itemView.findViewById(R.id.write_layout);
        }
    }

    public List<WriteEpcBean> getSelectedepcBeans() {
        return selectedepcBeans;
    }

    public interface WriteEpcItemClickListener {
        void onEpcItemClick(WriteEpcBean fileBean);
        void onWriteEpcClick(WriteEpcBean fileBean);
    }

    public void setWriteClickListener(WriteEpcItemClickListener writeClickListener) {
        this.writeClickListener = writeClickListener;
    }
}
