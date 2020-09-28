package com.siterwell.application.commonview;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.siterwell.application.HelpActivity;
import com.siterwell.application.R;

/**
 * @author skygge
 * @date 2019-12-03.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：隐私政策弹出框
 */
public class PrivacyPolicyDialog extends Dialog {

    TextView tvMsg;
    Button tvDialogCancel;
    Button tvDialogConfirm;

    private Context mContext;
    private OnCallBackToRefresh mCallBack;

    public PrivacyPolicyDialog(Context context, OnCallBackToRefresh callBack) {
        super(context, R.style.window_background);
        this.mContext = context;
        this.mCallBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_privacy_policy);
        setCanceledOnTouchOutside(false);
        initView();
        initData();
        setMsg(mContext.getString(R.string.login_protocol), tvMsg);
    }

    private void initView() {
        tvMsg = findViewById(R.id.tv_tip);
        tvDialogCancel = findViewById(R.id.bt_dialog_cancel);
        tvDialogConfirm = findViewById(R.id.bt_dialog_confirm);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        tvDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null) mCallBack.onCancel();
                dismiss();
            }
        });

        tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null) mCallBack.onConfirm();
                dismiss();
            }
        });
    }

    /**
     *
     * @param content   文字内容
     * @param textView  加载文字的textview
     */

    private void setMsg(String content, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        int i = content.indexOf(getContext().getResources().getString(R.string.login_protocol_1));//截取文字开始的下标
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //点击后的操作
                Intent intent = new Intent();
                intent.setClass(mContext, HelpActivity.class);
                intent.putExtra("type", 1);
                mContext.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(mContext, R.color.edit_color));       //设置文字颜色
                ds.setUnderlineText(false);      //设置下划线//根据需要添加
            }
        }, i, i +getContext().getResources().getString(R.string.login_protocol_1).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int i2 = content.indexOf(getContext().getResources().getString(R.string.login_protocol_3));//截取文字开始的下标
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent();
                intent.setClass(mContext, HelpActivity.class);
                intent.putExtra("type", 2);
                mContext.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(mContext,R.color.edit_color));       //设置文字颜色
                ds.setUnderlineText(false);      //设置下划线//根据需要添加
            }
        }, i2, i2 +getContext().getResources().getString(R.string.login_protocol_3).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明，否则会一直出现高亮
        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public interface OnCallBackToRefresh {

        void onConfirm();

        void onCancel();
    }
}
