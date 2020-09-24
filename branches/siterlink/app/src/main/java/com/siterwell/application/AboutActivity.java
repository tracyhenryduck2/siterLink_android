package com.siterwell.application;

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
    private SettingItem app_txt;
    private UpdateAppAuto updateAppAuto;

    @Override
    protected void onCreateInit() {
        initGuider();
        initview();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    private void initGuider(){
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getResources().getString(R.string.about), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        }, null, R.color.bar_bg);
    }

    private void initview(){
        app_txt     = (SettingItem)findViewById(R.id.app_version);
        updateAppAuto = new UpdateAppAuto(this,app_txt,true);
        String verName = Config.getVerName(this, getPackageName());
        app_txt.setDetailText(verName);
        updateAppAuto.getUpdateInfo();
    }

}
