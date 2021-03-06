package com.siterwell.application;

import android.content.Intent;
import android.view.View;

import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.commonview.SettingItem;
import com.siterwell.application.updateapp.UpdateAppAuto;

/**
 * Created by gc-0001 on 2017/5/5.
 */

public class SystemActivity extends TopbarSuperActivity implements View.OnClickListener{
    private SettingItem settingItem_lan,settingItem_cache,settingItem_answer,settingItem_about, settingItem_help;
    private UpdateAppAuto updateAppAuto;

    @Override
    protected void onCreateInit() {
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_system;
    }

    private void initView(){
        settingItem_lan = (SettingItem)findViewById(R.id.lan);
        settingItem_cache = (SettingItem)findViewById(R.id.cache);
        settingItem_answer = (SettingItem)findViewById(R.id.answer);
        settingItem_about = (SettingItem)findViewById(R.id.about);
        settingItem_help = (SettingItem)findViewById(R.id.help);
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getResources().getString(R.string.system_setting), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        },null,R.color.bar_bg);
        updateAppAuto = new UpdateAppAuto(this,settingItem_about,false);
        updateAppAuto.getUpdateInfo();
        settingItem_answer.setOnClickListener(this);
        settingItem_about.setOnClickListener(this);
        settingItem_help.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lan:
            case R.id.cache:
                break;
            case R.id.answer:
                startActivity(new Intent(this,QuestionActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this,AboutActivity.class));
                break;
            case R.id.help:
                startActivity(new Intent(this,HelpActivity.class));
                break;
        }
    }
}
