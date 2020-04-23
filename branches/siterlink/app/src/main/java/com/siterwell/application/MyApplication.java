package com.siterwell.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.support.multidex.MultiDexApplication;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.siterwell.application.common.CrashHandler;
import com.siterwell.sdk.common.SitewellSDK;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

import me.hekr.sdk.HekrSDK;


/**
 * Created by hekr_jds on 6/30 0030.
 **/
public class MyApplication extends MultiDexApplication {
    private static MyApplication mApp;
    public static Activity sActivity;
    // user your appid the key.
    private static final String APP_ID = "2882303761517683139";
    // user your appid the key.
    private static final String APP_KEY = "5251768341139";

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }
        //推送服务初始化
//        PushManager.getInstance().initialize(getApplicationContext());
        //初始化HekrSDK
        HekrSDK.init(getApplicationContext(), R.raw.config);
        HekrSDK.enableDebug(true);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(80, 80)
                .denyCacheImageMultipleSizesInMemory()
                //.writeDebugLogs()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                sActivity=activity;

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
        SitewellSDK.getInstance(this).init();
        CrashHandler.getInstance().init(getApplicationContext());
    }
    public static Context getAppContext() {
        return mApp;
    }
    public static Activity getActivity(){
        return sActivity;
    }


    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
