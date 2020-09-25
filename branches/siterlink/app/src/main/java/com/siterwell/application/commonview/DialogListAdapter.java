package com.siterwell.application.commonview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siterwell.application.R;


public class DialogListAdapter extends RecyclerView.Adapter<DialogListAdapter.ItemHolder> {

    private Context mContext;
    private String[] mText;
    private onCallBack mCallBack;

    public DialogListAdapter(Context mContext, String[] mText) {
        this.mContext = mContext;
        this.mText = mText;
    }

    public void setCallBack(onCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_list, viewGroup, false);
        return new ItemHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder itemHolder, @SuppressLint("RecyclerView") final int i) {
        itemHolder.tvItemMsg.setText(mText[i]);
        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.callBack(i);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mText == null ? 0 : mText.length;
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        TextView tvItemMsg;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            tvItemMsg = itemView.findViewById(R.id.tv_item_msg);
        }
    }

    public interface onCallBack{
        void callBack(int i);
    }
}
