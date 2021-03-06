package com.siterwell.application;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.firebase.iid.FirebaseInstanceId;
import com.siterwell.application.BusEvents.LogoutEvent;
import com.siterwell.application.BusEvents.BindPushEvent;
import com.siterwell.application.BusEvents.SychronizeEvent;
import com.siterwell.application.common.CCPAppManager;
import com.siterwell.application.common.Config;
import com.siterwell.application.common.ECPreferenceSettings;
import com.siterwell.application.common.ECPreferences;
import com.siterwell.application.common.Errcode;
import com.siterwell.application.common.PermissionUtils;
import com.siterwell.application.common.SystemUtil;
import com.siterwell.application.common.UnitTools;
import com.siterwell.application.commonview.BaseDialog;
import com.siterwell.application.commonview.BottomListDialog;
import com.siterwell.application.commonview.ECAlertDialog;
import com.siterwell.application.commonview.EmRecylerView;
import com.siterwell.application.commonview.ProgressDialog;
import com.siterwell.application.commonview.SlidingMenu;
import com.siterwell.application.device.Controller;
import com.siterwell.application.device.DeviceActivitys;
import com.siterwell.application.folder.*;
import com.siterwell.application.folder.bean.LocalFolderBean;
import com.siterwell.application.storage.DeviceDao;
import com.siterwell.application.storage.FolderDao;
import com.siterwell.application.storage.WifiTimerDao;
import com.siterwell.application.updateapp.UpdateAppAuto;
import com.siterwell.application.user.PersonalActivity;
import com.siterwell.sdk.http.HekrUser;
import com.siterwell.sdk.http.HekrUserAction;
import com.siterwell.sdk.http.bean.DeviceBean;
import com.siterwell.sdk.http.bean.FolderListBean;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.hekr.sdk.Hekr;
import me.hekr.sdk.inter.HekrCallback;
import me.hekr.sdk.utils.ErrorCodeUtil;


/**
 * Created by gc-0001 on 2017/4/22.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener,HomeAdapter.OnRecyclerViewItemClickListener{
    private final String TAG ="MainActivity";
    private SlidingMenu mMenu;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeAdapter mAdapter;
    private List<LocalFolderBean> folderBeanList;
    private ECAlertDialog alertDialog;

    private FolderDao folderDao;
    private SwipeRefreshLayout swipeRefreshLayout_em;
    private ImageView img_menu,img_add;
    private TextView username;
    private int flag_update = 0;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        if(!Controller.getInstance().flag_service) {
            Controller.getInstance().flag_service = true;
            Intent intent = new Intent(MainActivity.this, SychronizeService.class);
            startService(intent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        folderBeanList = new ArrayList<>();
        initGTService();
        initView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//取消注册
    }

    @Override
    public void onClick(View view) {
              switch (view.getId()){
                  case R.id.person:
                      handler.sendEmptyMessageDelayed(1,500);
                      startActivity(new Intent(this, PersonalActivity.class));
                      break;
                  case R.id.warn_list:
                      handler.sendEmptyMessageDelayed(1,500);
                      startActivity(new Intent(this, AlarmListActivity.class));
                      break;
                  case R.id.system_setting:
                      handler.sendEmptyMessageDelayed(1,500);
                      startActivity(new Intent(this, SystemActivity.class));
                      break;
                  case R.id.left_img:
                      mMenu.toggle();
                      break;
                  case R.id.right_img:
                      startActivity(new Intent(MainActivity.this,AddFolderActivity.class));
                      break;
                  case R.id.learned:
                      Intent intent = new Intent();
                      intent.setAction("android.intent.action.VIEW");
                      UnitTools unitTools = new UnitTools(this);
                      if("zh".equals(unitTools.readLanguage())){
                          Uri content_url = Uri.parse("http://www.china-siter.cn/");
                          intent.setData(content_url);
                          startActivity(intent);
                      }
                      else{
                          Uri content_url = Uri.parse("http://www.china-siter.com/");
                          intent.setData(content_url);
                          startActivity(intent);
                      }
                      break;
                  case R.id.logout:


                      showProgressDialog(getResources().getString(R.string.wait));

                          if ("huawei".equals(SystemUtil.getDeviceBrand().toLowerCase()) || "honor".equals(SystemUtil.getDeviceBrand().toLowerCase())) {
                              String token = getHuaweiToken();
                              if (TextUtils.isEmpty(token)) {
                                  handler.sendEmptyMessageDelayed(2, 1000);
                              } else {

                                  HekrUserAction.getInstance(this).unPushTagBind(token, 2, new HekrUser.UnPushTagBindListener() {
                                      @Override
                                      public void unPushTagBindSuccess() {
                                          handler.sendEmptyMessageDelayed(2, 1000);
                                      }

                                      @Override
                                      public void unPushTagBindFail(int errorCode) {

                                          if(errorCode == 1){
                                              handler.sendEmptyMessage(2);
                                          }else{
                                              Toast.makeText(MainActivity.this, Errcode.errorCode2Msg(MainActivity.this, errorCode),Toast.LENGTH_LONG).show();
                                              hideProgressDialog();
                                          }


                                      }
                                  });

                              }
                          } else if ("xiaomi".equals(SystemUtil.getDeviceBrand().toLowerCase())) {
                              String clientid = MiPushClient.getRegId(MainActivity.this);


                              HekrUserAction.getInstance(this).unPushTagBind(clientid, 1, new HekrUser.UnPushTagBindListener() {
                                  @Override
                                  public void unPushTagBindSuccess() {
                                      handler.sendEmptyMessageDelayed(2, 1000);
                                  }

                                  @Override
                                  public void unPushTagBindFail(int errorCode) {
                                      if(errorCode == 1){
                                          handler.sendEmptyMessage(2);
                                      }else{
                                          Toast.makeText(MainActivity.this,Errcode.errorCode2Msg(MainActivity.this, errorCode),Toast.LENGTH_LONG).show();
                                          hideProgressDialog();
                                      }
                                  }
                              });
                          } else {

                              String fcmtoken = FirebaseInstanceId.getInstance().getToken();
                              if(TextUtils.isEmpty(fcmtoken)) {
                                  handler.sendEmptyMessageDelayed(2, 1000);
                              }else{
                                  assert fcmtoken != null;
                                  HekrUserAction.getInstance(this).unPushTagBind(fcmtoken, 3, new HekrUser.UnPushTagBindListener() {
                                      @Override
                                      public void unPushTagBindSuccess() {
                                          handler.sendEmptyMessageDelayed(2, 1000);
                                      }

                                      @Override
                                      public void unPushTagBindFail(int errorCode) {
                                          if(errorCode == 1){
                                              handler.sendEmptyMessage(2);
                                          }else{
                                              Toast.makeText(MainActivity.this,Errcode.errorCode2Msg(MainActivity.this, errorCode),Toast.LENGTH_LONG).show();
                                              hideProgressDialog();
                                          }
                                      }
                                  });
                              }

                          }
                      break;
                  default:
                      break;
              }
    }

    private void initView(){
        folderDao = new FolderDao(this);
        img_menu = (ImageView)findViewById(R.id.left_img);
        img_add  = (ImageView)findViewById(R.id.right_img);
        RelativeLayout lay_personal_setting = (RelativeLayout) findViewById(R.id.person);
        lay_personal_setting.setOnClickListener(this);
        RelativeLayout lay_warn_list = (RelativeLayout) findViewById(R.id.warn_list);
        lay_warn_list.setOnClickListener(this);
        RelativeLayout lay_system_setting = (RelativeLayout) findViewById(R.id.system_setting);
        lay_system_setting.setOnClickListener(this);

        RelativeLayout lay_learned = (RelativeLayout) findViewById(R.id.learned);
        lay_learned.setOnClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.grid_swipe_refresh);
        //调整SwipeRefreshLayout的位置
        swipeRefreshLayout.setColorSchemeResources(R.color.edit_color);
        swipeRefreshLayout.setProgressViewOffset(false, 0,  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mMenu = (SlidingMenu) findViewById(R.id.id_menu);
        username = (TextView)findViewById(R.id.admin);
        Button logout = (Button) findViewById(R.id.logout);
        EmRecylerView recyclerView = (EmRecylerView) findViewById(R.id.id_recyclerview);
        //Empty_view = findViewById(R.id.id_empty_view);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        folderBeanList = folderDao.findAllFolders();
        mAdapter = new HomeAdapter(this,folderBeanList);
        mAdapter.setOnItemClickListener(this);
        logout.setOnClickListener(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        //recyclerView.setEmptyView(Empty_view);
        swipeRefreshLayout_em = (SwipeRefreshLayout) LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        ((LinearLayout)swipeRefreshLayout.getParent()).addView(swipeRefreshLayout_em);
        //调整SwipeRefreshLayout的位置
        swipeRefreshLayout_em.setColorSchemeResources(R.color.bar_bg);
        swipeRefreshLayout_em.setProgressViewOffset(false, 0,  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        if(folderBeanList.size()==0){
            swipeRefreshLayout.setVisibility(View.GONE);
            swipeRefreshLayout_em.setVisibility(View.VISIBLE);
        }else{
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout_em.setVisibility(View.GONE);
        }
        UpdateAppAuto updateAppAuto = new UpdateAppAuto(this);
        updateAppAuto.getUpdateInfo();
        setListener();
    }

    private void setListener(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFolderInfo();
            }
        });
        swipeRefreshLayout_em.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFolderInfo();
            }
        });
        img_menu.setOnClickListener(this);
        img_add.setOnClickListener(this);
        if(TextUtils.isEmpty(CCPAppManager.getClientUser().getPhoneNumber())){
            username.setText(CCPAppManager.getClientUser().getEmail());
        }else{
            username.setText(CCPAppManager.getClientUser().getPhoneNumber());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initGTService() {
            String fcmclientid = FirebaseInstanceId.getInstance().getToken();
            if(!TextUtils.isEmpty(fcmclientid)){
                Log.i(TAG,"FCM平台CLIENTID："+fcmclientid);
                    BindPushEvent stEvent = new BindPushEvent();
                    stEvent.setType(3);
                    stEvent.setToken(fcmclientid);
                    EventBus.getDefault().post(stEvent);
            }else{
                if( "honor".equals(SystemUtil.getDeviceBrand().toLowerCase()) || "huawei".equals(SystemUtil.getDeviceBrand().toLowerCase())){
                    com.huawei.android.pushagent.api.PushManager.requestToken(this);
                }else if("xiaomi".equals(SystemUtil.getDeviceBrand().toLowerCase())){
                    String ds = MiPushClient.getRegId(this);
                    Log.i(TAG,"小米平台CLIENTID："+ds);
                    if(!TextUtils.isEmpty(ds)) {
                        BindPushEvent stEvent = new BindPushEvent();
                        stEvent.setType(1);
                        stEvent.setToken(ds);
                        EventBus.getDefault().post(stEvent);
                    }else{
                        Log.i(TAG, "小米平台CLIENTID为空");
                    }
                }
        }
    }


    @Override
    public void onItemClick(View view, LocalFolderBean folderBean) {
        Intent intent = new Intent(this, DeviceListActivity.class);
        intent.putExtra("folderBean",folderBean);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view,final LocalFolderBean deviceBean) {
        BottomListDialog mDialog = new BottomListDialog(this, new BottomListDialog.onCallBack() {
            @Override
            public void callBack(int i) {
                switch (i) {
                    case 0:
                        modifyName(deviceBean);
                        break;
                    case 1:
                        deleteStation(deviceBean);
                        break;
                }
            }
        });
        mDialog.setMsg(getResources().getStringArray(R.array.folder_operation));
        mDialog.show();
    }

    private void modifyName(final LocalFolderBean deviceBean){
        alertDialog = ECAlertDialog.buildAlert(MainActivity.this, getResources().getString(R.string.update_name),getResources().getString(R.string.dialog_btn_cancel),getResources().getString(R.string.dialog_btn_confim), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.setDismissFalse(true);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText text = (EditText) alertDialog.getContent().findViewById(R.id.tet);
                final String newname = text.getText().toString().trim();

                if(!TextUtils.isEmpty(newname)){

                    try {
                        if(newname.getBytes("GBK").length<=15){
                            alertDialog.setDismissFalse(true);
                            HekrUserAction.getInstance(MainActivity.this).renameFolder(newname+deviceBean.getImage(), deviceBean.getFolderId(), new HekrUser.RenameFolderListener() {
                                @Override
                                public void renameSuccess() {
                                    folderDao.updateFolderName(newname,deviceBean.getFolderId());
                                    Toast.makeText(MainActivity.this,getResources().getString(R.string.success_modify),Toast.LENGTH_SHORT).show();
                                    getFolderInfo();
                                }

                                @Override
                                public void renameFail(int errorCode) {
                                    Toast.makeText(MainActivity.this, Errcode.errorCode2Msg(MainActivity.this,errorCode),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            alertDialog.setDismissFalse(false);
                            Toast.makeText(MainActivity.this,getResources().getString(R.string.name_is_too_long),Toast.LENGTH_SHORT).show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    alertDialog.setDismissFalse(false);
                    Toast.makeText(MainActivity.this,getResources().getString(R.string.name_is_null),Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setContentView(R.layout.edit_alert);
        alertDialog.setTitle(getResources().getString(R.string.update_name));
        EditText text = (EditText) alertDialog.getContent().findViewById(R.id.tet);
        text.setText(deviceBean.getFolderName());
        text.setSelection(deviceBean.getFolderName().length());
        alertDialog.show();
    }

    private void deleteStation(final LocalFolderBean deviceBean){
        String ds = getResources().getString(R.string.root);
        if(!"root".equals(deviceBean.getFolderName())){
            ds = deviceBean.getFolderName();
        }
        String deletething = String.format(getResources().getString(R.string.delete_it_or_not),ds);
        ECAlertDialog elc = ECAlertDialog.buildAlert(MainActivity.this,deletething, getResources().getString(R.string.dialog_btn_cancel), getResources().getString(R.string.dialog_btn_confim),null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HekrUserAction.getInstance(MainActivity.this).deleteFolder(deviceBean.getFolderId(), new HekrUser.DeleteFileListener() {
                    @Override
                    public void deleteSuccess()
                    {
                        folderDao.deleteByFolderId(deviceBean.getFolderId());
                        Toast.makeText(MainActivity.this,getResources().getString(R.string.success_delete),Toast.LENGTH_SHORT).show();
                        getFolderInfo();
                    }

                    @Override
                    public void deleteFail(int errorCode) {
                        if(6400017 == errorCode){
                            Toast.makeText(MainActivity.this,getResources().getString(R.string.this_folder_not_allow_to_delete),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, Errcode.errorCode2Msg(MainActivity.this,errorCode),Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        elc.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(TextUtils.isEmpty(HekrUserAction.getInstance(this).getJWT_TOKEN())){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }else{
            getFolderInfo();
            getNotificationState();
        }
    }

    private void getNotificationState(){
        boolean notice = PermissionUtils.isPermissionOpen(this);
        if (!notice){
            String title  = getString(R.string.open_notification);
            BaseDialog mDialog = new BaseDialog(this, new BaseDialog.OnCallBackToRefresh() {
                @Override
                public void onConfirm() {
                   PermissionUtils.openPermissionSetting(MainActivity.this);
                }

                @Override
                public void onCancel() {
                    startActivity(new Intent(MainActivity.this, HelpActivity.class));
                }
            });
            mDialog.setTitleAndButton(title, getString(R.string.help), getString(R.string.goto_set));
            mDialog.setTitleSize(14);
            mDialog.show();
        }
    }

    private void getFolderInfo(){
        swipeRefreshLayout_em.setRefreshing(true);
        swipeRefreshLayout.setRefreshing(true);

        HekrUserAction.getInstance(this).getFolder(0, new HekrUser.GetFolderListsListener() {
            @Override
            public void getSuccess(List<FolderListBean> folderList) {
                Log.i(TAG,folderList.toString());
                    swipeRefreshLayout_em.setRefreshing(false);
                    swipeRefreshLayout.setRefreshing(false);

                folderBeanList.clear();
                for(FolderListBean folderListBean:folderList){
                    LocalFolderBean localFolderBean = new LocalFolderBean();
                    localFolderBean.setFolderId(folderListBean.getFolderId());
                    int index = folderListBean.getFolderName().indexOf("ufile");
                    if(index!=-1){
                        localFolderBean.setFolderName(folderListBean.getFolderName().substring(0,index));
                        localFolderBean.setImage(folderListBean.getFolderName().substring(index));
                    }else{
                        localFolderBean.setFolderName(folderListBean.getFolderName());
                        localFolderBean.setImage("");
                    }

                    folderBeanList.add(localFolderBean);

                }
                mAdapter.Refresh(folderBeanList);
                if(folderBeanList.size()==0){

                    swipeRefreshLayout.setVisibility(View.GONE);
                    swipeRefreshLayout_em.setVisibility(View.VISIBLE);
                }else{
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout_em.setVisibility(View.GONE);

                }
                folderDao.deleteAll();
                folderDao.insertFolderList(folderBeanList);
                   List<String> arrayList = new ArrayList<String>();

                   for(int i=0;i<folderBeanList.size();i++){
                       arrayList.add(folderBeanList.get(i).getFolderId());
                   }

                if(flag_update==0){
                    flag_update = 1;
                    SychronizeEvent sychronizeEvent = new SychronizeEvent();
                    sychronizeEvent.setFolderids(arrayList);
                    EventBus.getDefault().post(sychronizeEvent);
                }

            }

            @Override
            public void getFail(int errorCode) {
                swipeRefreshLayout.setRefreshing(false);
                if( errorCode == 1){
                    handler.sendEmptyMessage(2);
                    Toast.makeText(MainActivity.this,  Errcode.errorCode2Msg(MainActivity.this,1),Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,  Errcode.errorCode2Msg(MainActivity.this,errorCode),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private long exitTime = 0;

    private void goBack(){
            String ds = String.format(getResources().getString(R.string.exit_hint), Config.getAppName(this));
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), ds, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                this.finish();
            }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            String deviceid = intent.getStringExtra("devTid");
            Log.i(TAG,"onNewIntent:deviceid"+deviceid);
            DeviceDao deviceDao = new DeviceDao(this);
            DeviceBean deviceBean = deviceDao.findDeviceBySid(deviceid);
            DeviceActivitys.startDeviceDetailActivity(this,deviceBean);
            UnitTools.stopMusic(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void resetStorage(){
        FolderDao folderDao = new FolderDao(this);
        DeviceDao batteryDao = new DeviceDao(this);
        WifiTimerDao wifiTimerDao = new WifiTimerDao(this);
        folderDao.reset();
        batteryDao.reset();
        wifiTimerDao.reset();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mMenu.close2Menu();
                    break;
                case 2:

                    Hekr.getHekrUser().logout(new HekrCallback() {
                        @Override
                        public void onSuccess() {
                            hideProgressDialog();
                            try {


                                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
                            } catch (InvalidClassException e) {
                                e.printStackTrace();
                            }
                            Controller.getInstance().flag_service = false;
                            resetStorage();
                            HekrUserAction.getInstance(MainActivity.this).userLogout();
                            stopService(new Intent(MainActivity.this, SychronizeService.class));
                            startActivity(new Intent(MainActivity.this,LoginActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(int errorCode, String message) {
                            hideProgressDialog();
                            if(errorCode == 1){
                                try {
                                    ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
                                } catch (InvalidClassException e) {
                                    e.printStackTrace();
                                }
                                ErrorCodeUtil.getErrorDesc(errorCode);
                                Controller.getInstance().flag_service = false;
                                resetStorage();
                                HekrUserAction.getInstance(MainActivity.this).userLogout();
                                stopService(new Intent(MainActivity.this, SychronizeService.class));
                                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                                finish();
                            }else{
                                try {
                                    com.alibaba.fastjson.JSONObject d = JSON.parseObject(message);
                                    int code = d.getInteger("code");
                                    Toast.makeText(MainActivity.this,Errcode.errorCode2Msg(MainActivity.this,code),Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(MainActivity.this,Errcode.errorCode2Msg(MainActivity.this,errorCode),Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                    break;
            }
            return false;
        }
    });


    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(final  BindPushEvent event){

            MainActivity.this.runOnUiThread(new Thread(){
                @Override
                public void run() {
                    if(!TextUtils.isEmpty(event.getToken())){
                        HekrUserAction.getInstance(MainActivity.this).pushTagBind(event.getToken(), event.getType(), new HekrUser.PushTagBindListener() {
                            @Override
                            public void pushTagBindSuccess() {

                            }

                            @Override
                            public void pushTagBindFail(int errorCode) {
                                if(errorCode==1){
                                    LogoutEvent tokenTimeoutEvent = new LogoutEvent();
                                    EventBus.getDefault().post(tokenTimeoutEvent);
                                }
                            }
                        });

                    }
                }
            });


    }

    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(final  LogoutEvent event){
        if(mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        HekrUserAction.getInstance(this).userLogout();
        CCPAppManager.setClientUser(null);

        try {
            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
        stopService(new Intent(this, SychronizeService.class));
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();

    }

    private String getHuaweiToken(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN;
        String autoflag = sharedPreferences.getString(flag.getId(), (String) flag.getDefaultValue());
        return autoflag;
    }

    protected void showProgressDialog(String title){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog
                .setPressText(title);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    protected void hideProgressDialog(){
        if(mProgressDialog!=null&&mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
}
