package com.siterwell.application.commonview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.siterwell.application.R;

/**
 * @author skygge
 * @date 2020-03-21.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：底部弹出的选择列表
 */
public class BottomListDialog extends Dialog {

    RecyclerView mRecyclerView;
    TextView tvDialogCancel;

    private Context mContext;
    private String[] mMsg;
    private onCallBack mCallBack;

    public BottomListDialog(Context context, onCallBack callBack) {
        super(context, R.style.window_background);
        this.mContext = context;
        this.mCallBack = callBack;
    }

    public void setMsg(String[] mMsg) {
        this.mMsg = mMsg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base_list);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams params = window.getAttributes();
        //dialog显示位置，不设置默认居中
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.main_menu_animStyle);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        tvDialogCancel = findViewById(R.id.tv_dialog_cancel);
        mRecyclerView = findViewById(R.id.rv_dialog_list);
        DialogListAdapter mAdapter = new DialogListAdapter(mContext, mMsg);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setCallBack(new DialogListAdapter.onCallBack() {
            @Override
            public void callBack(int i) {
                dismiss();
                mCallBack.callBack(i);
            }
        });

        tvDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public interface onCallBack{
        void callBack(int i);
    }
}
