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
    private int type = 0;
    private String mUrl;
    private String mTitle;

    @Override
    protected void onCreateInit() {
        type = getIntent().getIntExtra("type", 0);
        initView();
        initData();
        getTopBarView().setTopBarStatus(R.drawable.back, -1, mTitle, 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        },null, R.color.bar_bg);
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
        switch (type){
            case 0:
                mTitle = getString(R.string.help);
                mUrl = Config.getPhonePushSetting(this);
                break;
            case 1:
                mTitle = getString(R.string.user_agreement);
                mUrl = Config.getUserProtocol(this);
                break;
            case 2:
                mTitle = getString(R.string.privacy_policy);
                mUrl = Config.getPrivacyAgreement(this);
                break;
        }
        webView.loadUrl(mUrl);
    }
}
