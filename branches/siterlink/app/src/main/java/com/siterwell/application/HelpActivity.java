package com.siterwell.application;

import android.annotation.SuppressLint;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.siterwell.application.common.Config;
import com.siterwell.application.common.TopbarSuperActivity;

/**
 * @author skygge
 * @date 2020/9/24.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class HelpActivity extends TopbarSuperActivity {

    private WebView webView;

    @Override
    protected void onCreateInit() {
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getString(R.string.help), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        },null, R.color.bar_bg);
        initView();
        initData();
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_help;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webView.setInitialScale(25);
        webView.setWebViewClient(new WebViewClient());
    }

    private void initData() {
        String mUrl = Config.getPhonePushSetting(this);
        webView.loadUrl(mUrl);
    }
}
