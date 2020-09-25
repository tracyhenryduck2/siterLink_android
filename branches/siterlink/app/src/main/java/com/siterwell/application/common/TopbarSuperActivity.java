package com.siterwell.application.common;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.siterwell.application.R;
import com.siterwell.application.commonview.ProgressDialog;
import com.siterwell.application.commonview.TopBarView;

/**
 * ClassName:TopbarSuperActivity
 * 作者：Henry on 2017/4/22 16:29
 * 邮箱：xuejunju_4595@qq.com
 * 描述:
 */
public abstract class TopbarSuperActivity extends AppCompatActivity {

    protected String TAG = getClass().getSimpleName();
    /**
     * 标题
     */
    private TopBarView mTopBarView;
    private LayoutInflater mLayoutInflater;
    private View mContentView;
    private ProgressDialog mProgressDialog;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected SwipeRefreshLayout swipeRefreshLayout_em;//空布局；

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.bar_bg), true);
        StatusBarUtil.setLightMode(this);
        setContentView(R.layout.activity_base);
        init();
        swiperefreshLayoutInit();
        onCreateInit();

    }


    public TopBarView getTopBarView() {
        if(mTopBarView instanceof TopBarView) {
            return (TopBarView) mTopBarView;
        }
        return null;
    }

    private void init()  {

        int layoutId = getLayoutId();
        ViewGroup mRootView = (ViewGroup)findViewById(R.id.root);
        mLayoutInflater = LayoutInflater.from(this);
        mTopBarView = (TopBarView)findViewById(R.id.top_bar);

        if (layoutId != -1) {
            mContentView = mLayoutInflater.inflate(getLayoutId(), null);
            mRootView.addView(mContentView, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
        }
    }

    protected void swiperefreshLayoutInit(){
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swip);
        if(swipeRefreshLayout!=null){
            //调整SwipeRefreshLayout的位置
            swipeRefreshLayout.setColorSchemeResources(R.color.edit_color);
            swipeRefreshLayout.setProgressViewOffset(false, 0,  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
            swipeRefreshLayout_em = (SwipeRefreshLayout) LayoutInflater.from(this).inflate(R.layout.empty_view, null);
            ((ViewGroup)swipeRefreshLayout.getParent()).addView(swipeRefreshLayout_em);
            //调整SwipeRefreshLayout的位置
            swipeRefreshLayout_em.setColorSchemeResources(R.color.edit_color);
            swipeRefreshLayout_em.setProgressViewOffset(false, 0,  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        }

    }

    protected abstract void onCreateInit();

    protected abstract int getLayoutId();

    /**
     * hide inputMethod
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null ) {
            View localView = this.getCurrentFocus();
            if(localView != null && localView.getWindowToken() != null ) {
                IBinder windowToken = localView.getWindowToken();
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSoftKeyboard();
    }

    protected void showProgressDialog(String title){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog
                .setPressText(title);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    protected void hideProgressDialog(){
        if(mProgressDialog!=null&&mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
}
