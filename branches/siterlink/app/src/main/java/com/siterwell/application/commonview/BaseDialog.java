package com.siterwell.application.commonview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.siterwell.application.R;

/**
 * @author skygge
 * @date 2019-10-10.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：基础弹出框
 */
public class BaseDialog extends Dialog {

    TextView tvTip;
    TextView tvDialogCancel;
    TextView tvDialogConfirm;
    View mLine;

    private String mMsg;
    private String mCancel;
    private String mConfirm;
    private int mTitleSize = 20;
    private boolean isGone = false;
    private OnCallBackToRefresh mCallBack;

    public BaseDialog(Context context) {
        super(context, R.style.window_background);
    }

    public BaseDialog(Context context, OnCallBackToRefresh callBack) {
        super(context, R.style.window_background);
        this.mCallBack = callBack;
    }

    public void setCancelVisibility(boolean isGone){
        this.isGone = isGone;
    }

    public void setCancelText(String cancel){
        this.mCancel = cancel;
    }

    public void setTitleSize(int mTitleSize) {
        this.mTitleSize = mTitleSize;
    }

    public void setTitleAndButton(String msg, String cancel, String confirm) {
        this.mMsg = msg;
        this.mConfirm = confirm;
        this.mCancel = cancel;
    }

    public void setMsg(String msg) {
        this.mMsg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tip);
        setCanceledOnTouchOutside(false);

        tvTip = findViewById(R.id.tv_tip);
        tvDialogCancel = findViewById(R.id.tv_dialog_cancel);
        tvDialogConfirm = findViewById(R.id.tv_dialog_confirm);
        mLine = findViewById(R.id.iv_line);

        tvTip.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTitleSize);
        tvDialogConfirm.setVisibility(isGone ? View.GONE : View.VISIBLE);
        mLine.setVisibility(isGone ? View.GONE : View.VISIBLE);
        if (!TextUtils.isEmpty(mMsg)) tvTip.setText(mMsg);
        if (!TextUtils.isEmpty(mCancel)) tvDialogCancel.setText(mCancel);
        if (!TextUtils.isEmpty(mConfirm)) tvDialogConfirm.setText(mConfirm);

        tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack!=null) mCallBack.onConfirm();
                dismiss();
            }
        });

        tvDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack!=null) mCallBack.onCancel();
                dismiss();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCallBack!=null) mCallBack.onCancel();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public interface OnCallBackToRefresh {

        void onConfirm();

        void onCancel();
    }
}
