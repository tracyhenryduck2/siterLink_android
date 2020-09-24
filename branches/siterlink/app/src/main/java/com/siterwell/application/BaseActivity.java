package com.siterwell.application;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.siterwell.application.common.StatusBarUtil;

/**
 * @author skygge
 * @date 2020/9/24.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setLightMode(this);
    }
}
