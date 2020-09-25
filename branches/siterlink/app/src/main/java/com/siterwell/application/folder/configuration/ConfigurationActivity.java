package com.siterwell.application.folder.configuration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.siterwell.application.R;
import com.siterwell.application.common.ECPreferenceSettings;
import com.siterwell.application.common.ECPreferences;
import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.common.UnitTools;
import com.siterwell.application.commonview.CodeEdit;
import com.siterwell.application.folder.guide.GuideBattery1Activty;
import com.siterwell.application.folder.guide.GuideBattery2Activty;
import com.siterwell.application.folder.guide.SocketGuide1Activity;
import com.siterwell.application.folder.guide.WaterSensor1Activity;
import com.siterwell.sdk.bean.DeviceType;
import com.siterwell.sdk.protocol.GS140Command;

import java.io.InvalidClassException;

/**
 * Created by ST-020111 on 2017/4/14.
 */
public class ConfigurationActivity extends TopbarSuperActivity implements View.OnClickListener{
    private BroadcastReceiver connectionReceiver;
    private TextView wifi;
    private CodeEdit psw;
    private CheckBox checkBox;
    private GS140Command BatteryType;
    private String Device_type;
    private EspWifiAdminSimple mWifiAdmin;

    @Override
    protected void onCreateInit() {
        initView();
        createReceiver();
        refreshWifi();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_configuration;
    }

    private void initView(){
        BatteryType = (GS140Command)getIntent().getSerializableExtra("dev");
        Device_type  = getIntent().getStringExtra("devicetype");
        if(TextUtils.isEmpty(Device_type)){
            finish();
        }
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getResources().getString(R.string.wifi_contect), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DeviceType.BATTERY.toString().equals(Device_type)){
                    startActivity(new Intent(ConfigurationActivity.this,GuideBattery1Activty.class));
                }
                finish();
            }
        },null,R.color.bar_bg);
        wifi = (TextView)findViewById(R.id.tvApSssidConnected);
        psw = (CodeEdit)findViewById(R.id.edtApPassword);
        Button btn_con = (Button) findViewById(R.id.btnConfirm);
        checkBox = (CheckBox)findViewById(R.id.is_remember);
        btn_con.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean flag = getIsRememberPassword();
        checkBox.setChecked(flag);
    }

    private void refreshWifi(){
        String apSSId = mWifiAdmin.getWifiConnectedSsid();
        if (apSSId != null) {
            //兼容某些8.0以上手机
            if(apSSId.contains("ssid")){
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if(networkInfo.getExtraInfo()==null){
                    apSSId = "";
                }else {
                    apSSId= networkInfo.getExtraInfo().replace("\"","");
                }
            }
            wifi.setText(apSSId);
        } else {
            wifi.setText("");
        }
        UnitTools unitTools = new UnitTools(this);
        String ds = unitTools.readSSidcode(apSSId);
        if(ds!=null){
            psw.getCodeEdit().setText(ds);
            psw.getCodeEdit().setSelection(ds.length());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(DeviceType.BATTERY.toString().equals(Device_type)){
                startActivity(new Intent(ConfigurationActivity.this, GuideBattery1Activty.class));
            }
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnConfirm) {
            hideSoftKeyboard();
            String apSsid = wifi.getText().toString();
            String apPassword = psw.getCodeEdit().getText().toString().trim();
            if (TextUtils.isEmpty(wifi.getText().toString())) {
                Toast.makeText(this, getResources().getString(R.string.no_wifi), Toast.LENGTH_LONG).show();
            } else if (wifi.getText().toString().contains(" ")) {
                Toast.makeText(this, getResources().getString(R.string.ssid_is_illegal), Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(apPassword)) {
                Toast.makeText(this, getResources().getString(R.string.password_is_null), Toast.LENGTH_SHORT).show();
            } else {
                UnitTools unitTools = new UnitTools(this);
                unitTools.writeSSidcode(apSsid, apPassword);
                if (checkBox.isChecked()) {
                    try {
                        ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_CONFIG_REMEMBER_PASSWORD, true, true);
                    } catch (InvalidClassException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_CONFIG_REMEMBER_PASSWORD, false, true);
                    } catch (InvalidClassException e) {
                        e.printStackTrace();
                    }
                }

                if (DeviceType.BATTERY.toString().equals(Device_type)) {
                    Intent intent = new Intent(ConfigurationActivity.this, GuideBattery2Activty.class);
                    intent.putExtra("wifi", apSsid);
                    intent.putExtra("pwd", apPassword);
                    intent.putExtra("dev", BatteryType);
                    startActivity(intent);
                    finish();
                } else if (DeviceType.WATERSENEOR.toString().equals(Device_type)) {
                    Intent intent = new Intent(ConfigurationActivity.this, WaterSensor1Activity.class);
                    intent.putExtra("wifi", apSsid);
                    intent.putExtra("pwd", apPassword);
                    intent.putExtra("devicetype", Device_type);
                    startActivity(intent);
                    finish();
                } else if (DeviceType.WIFISOKECT.toString().equals(Device_type)) {
                    Intent intent = new Intent(ConfigurationActivity.this, SocketGuide1Activity.class);
                    intent.putExtra("wifi", apSsid);
                    intent.putExtra("pwd", apPassword);
                    intent.putExtra("devicetype", Device_type);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(ConfigurationActivity.this, EsptouchAnimationActivity.class);
                    intent.putExtra("wifi", apSsid);
                    intent.putExtra("pwd", apPassword);
                    intent.putExtra("devicetype", Device_type);
                    startActivity(intent);
                    finish();
                }
            }
        }

    }

    private boolean getIsRememberPassword(){
        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_CONFIG_REMEMBER_PASSWORD;
        return sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
    }

    /**
     * 监听网络变化
     */
    public void createReceiver() {
        mWifiAdmin = new EspWifiAdminSimple(this);
        // 创建网络监听广播
        if (connectionReceiver == null) {
            connectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        ConnectivityManager mConnectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isAvailable()) {

                            String apSsid = mWifiAdmin.getWifiConnectedSsid();
                            if (apSsid != null) {
                                //兼容某些8.0以上手机
                                if(apSsid.contains("ssid")){
                                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                                    if(networkInfo.getExtraInfo()==null){
                                        apSsid = "";
                                    }else {
                                        apSsid= networkInfo.getExtraInfo().replace("\"","");
                                    }
                                }
                                wifi.setText(apSsid);
                            } else {
                                wifi.setText("");
                            }

                        } else {
                            wifi.setText("");
                            psw.getCodeEdit().setText("");
                        }
                    }
                }
            };
            // 注册网络监听广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(connectionReceiver, intentFilter);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionReceiver != null) {
            unregisterReceiver(connectionReceiver);
        }
    }
}
