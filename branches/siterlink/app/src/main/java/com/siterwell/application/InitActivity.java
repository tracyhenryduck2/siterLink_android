package com.siterwell.application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.siterwell.application.common.CCPAppManager;
import com.siterwell.application.common.ECPreferenceSettings;
import com.siterwell.application.common.ECPreferences;
import com.siterwell.application.common.SharedUtil;
import com.siterwell.application.common.StatusBarUtil;
import com.siterwell.application.commonview.PrivacyPolicyDialog;
import com.siterwell.sdk.http.HekrUserAction;
import com.siterwell.sdk.http.bean.UserBean;

import java.io.InvalidClassException;

import me.hekr.sdk.Constants;
import me.hekr.sdk.Hekr;
import me.hekr.sdk.inter.HekrCallback;
import me.hekr.sdk.utils.CacheUtil;


/**
 * Created by TracyHenry on 2018/5/9.
 */
public class InitActivity extends AppCompatActivity {

    private final static String TAG = "InitActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_init);
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setLightMode(this);
        checkUserAgreement();
        findViewById(R.id.layout_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserAgreement();
            }
        });
    }

    private void checkUserAgreement() {
        if (SharedUtil.getBoolean(this, "user_agreement")){
            onLoginIn();
        }else {
            showUserAgreement();
        }
    }

    private void onLoginIn(){
        if (TextUtils.isEmpty(HekrUserAction.getInstance(this).getJWT_TOKEN())) {
            handler.sendEmptyMessage(2);
        } else {
            toLoginIn();
        }
    }

    private void showUserAgreement() {
        PrivacyPolicyDialog mDialog = new PrivacyPolicyDialog(this, new PrivacyPolicyDialog.OnCallBackToRefresh() {
            @Override
            public void onConfirm() {
                SharedUtil.putBoolean(InitActivity.this, "user_agreement", true);
                onLoginIn();
            }

            @Override
            public void onCancel() {
                SharedUtil.putBoolean(InitActivity.this, "user_agreement", false);
            }
        });
        mDialog.show();
    }

    private void toLoginIn(){
       Log.i(TAG,"自动登录");
       Hekr.getHekrUser().login(getUsername(), getPassword(), new HekrCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"自动登录成功");
                UserBean userBean = new UserBean(getUsername(), getPassword(), CacheUtil.getUserToken(), CacheUtil.getString(Constants.REFRESH_TOKEN,""));
                HekrUserAction.getInstance(InitActivity.this).setUserCache(userBean);
                handler.sendEmptyMessageDelayed(1, 2000);
            }

            @Override
            public void onError(int errorCode, String message) {
                try {
                    JSONObject d = JSON.parseObject(message);
                    int code = d.getInteger("code");
                    //密码错误
                    if(code == 3400010){
                        try {
                            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
                        } catch (InvalidClassException e) {
                            e.printStackTrace();
                        }
                        HekrUserAction.getInstance(InitActivity.this).userLogout();
                        CCPAppManager.setClientUser(null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.i(TAG,"自动登录失败");
                }
                handler.sendEmptyMessage(2);
            }
        });
    }

    private String getUsername(){
        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_USERNAME;
        return sharedPreferences.getString(flag.getId(), (String) flag.getDefaultValue());
    }

    private String getPassword(){
        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_PASSWORD;
        return sharedPreferences.getString(flag.getId(), (String) flag.getDefaultValue());
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    startActivity(new Intent(InitActivity.this, MainActivity.class));
                    finish();
                    handler.removeMessages(1);
                    break;
                case 2:
                    startActivity(new Intent(InitActivity.this, LoginActivity.class));
                    finish();
                    handler.removeMessages(2);
                    break;
            }
            return false;
        }
    });
}
