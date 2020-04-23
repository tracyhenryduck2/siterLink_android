package com.siterwell.application.folder.guide;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.siterwell.application.R;
import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.folder.configuration.ConfigurationActivity;
import com.siterwell.sdk.bean.DeviceType;
import com.siterwell.sdk.protocol.GS140Command;

/**
 * Created by ST-020111 on 2017/4/14.
 */

public class GuideBattery1Activty extends TopbarSuperActivity implements View.OnClickListener{

    private ImageView imageView_guide;
    private Button btn_sig,btn_other;

    @Override
    protected void onCreateInit() {
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_guide_battery1;
    }

    private void initView(){
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getResources().getString(R.string.add_battery_instruct), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        }, null,R.color.bar_bg);
        imageView_guide = (ImageView)findViewById(R.id.guide);
        btn_sig = (Button)findViewById(R.id.siterwell);
        btn_other = (Button)findViewById(R.id.other);
        btn_sig.setOnClickListener(this);
        btn_other.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.siterwell:
                Intent intent = new Intent(this,ConfigurationActivity.class);
                intent.putExtra("devicetype", DeviceType.BATTERY.toString());
                intent.putExtra("dev", GS140Command.SET_SMOKE_SENSOR_SITERWELL);
                startActivity(intent);
                break;
            case R.id.other:
                Intent intent2 = new Intent(this,ConfigurationActivity.class);
                intent2.putExtra("devicetype", DeviceType.BATTERY.toString());
                intent2.putExtra("dev",GS140Command.SET_SMOKE_SENSOR_OTHER);
                startActivity(intent2);
                break;
        }
          finish();
    }
}
