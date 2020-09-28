package com.siterwell.application;

import android.content.Intent;
import android.view.View;

import com.siterwell.application.common.Config;
import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.commonview.SettingItem;
import com.siterwell.application.updateapp.UpdateAppAuto;

/**
 * Created by gc-0001 on 2017/5/28.
 */

public class AboutActivity extends TopbarSuperActivity {
    private final String TAG  = "AboutActivity";

    @Override
    protected void onCreateInit() {
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getString(R.string.about), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        }, null, R.color.bar_bg);
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    private void initView(){
        SettingItem app_txt = (SettingItem) findViewById(R.id.app_version);
        SettingItem user_agreement = (SettingItem) findViewById(R.id.user_agreement);
        SettingItem privacy_policy = (SettingItem) findViewById(R.id.privacy_policy);
        UpdateAppAuto updateAppAuto = new UpdateAppAuto(this, app_txt, true);
        String verName = Config.getVerName(this, getPackageName());
        app_txt.setDetailText(verName);
        updateAppAuto.getUpdateInfo();
        user_agreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startToIntent(1);
            }
        });
        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startToIntent(2);
            }
        });
    }

    private void startToIntent(int type){
        Intent intent = new Intent(AboutActivity.this, HelpActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }
}
