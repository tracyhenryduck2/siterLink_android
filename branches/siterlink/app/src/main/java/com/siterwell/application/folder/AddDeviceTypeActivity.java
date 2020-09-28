package com.siterwell.application.folder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.siterwell.application.R;
import com.siterwell.application.common.Errcode;
import com.siterwell.application.common.PermissionUtils;
import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.common.UnitTools;
import com.siterwell.application.commonview.BaseDialog;
import com.siterwell.application.commonview.BottomListDialog;
import com.siterwell.application.commonview.ProgressDialog;
import com.siterwell.application.folder.configuration.ConfigurationActivity;
import com.siterwell.application.folder.guide.GuideBattery1Activty;
import com.siterwell.application.storage.DeviceDao;
import com.siterwell.sdk.bean.DeviceType;
import com.siterwell.sdk.http.HekrUser;
import com.siterwell.sdk.http.HekrUserAction;
import com.siterwell.sdk.http.bean.DeviceBean;
import com.zbar.lib.ScanCaptureAct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imac on 2018/10/14.
 */

public class AddDeviceTypeActivity extends TopbarSuperActivity implements View.OnClickListener {
    private ProgressDialog progressDialog;
    private DeviceDao deviceDao;
    private int page = 0;
    private List<DeviceBean> deviceBeanList;

    private static final int REQUEST_LOCATION = 1000;
    private static final int REQUEST_CAMERA = 1001;
    private static final  int REQUEST_LOCATION_SERVICE = 1002;

    private String[] permission1 = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private String[] permission2 = new String[]{Manifest.permission.CAMERA};

    @Override
    protected void onCreateInit() {
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_device_type;
    }

    private void initView(){
        RelativeLayout wifi_config_linear = findViewById(R.id.wifi_btn);
        RelativeLayout scan_config_linear = findViewById(R.id.scan_btn);
        wifi_config_linear.setOnClickListener(this);
        scan_config_linear.setOnClickListener(this);
        deviceDao = new DeviceDao(this);
        deviceBeanList = new ArrayList<>();
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getResources().getString(R.string.add_device), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        },null,R.color.bar_bg);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.wifi_btn:
                if(PermissionUtils.requestPermission(this, permission1, REQUEST_LOCATION)){
                    if(UnitTools.isLocServiceEnable(this)){
                        showTypeList();
                    }else {
                        getLocalService();
                    }
                }
                break;
            case R.id.scan_btn:
                if(PermissionUtils.requestPermission(this, permission2, REQUEST_CAMERA)){
                    startActivityForResult(new Intent(this, ScanCaptureAct.class),1);
                }
                break;
        }
    }

    private void showTypeList(){
        String[] title = new String[]{ getString(R.string.battery), getString(R.string.socket), getString(R.string.watersensor)};
        BottomListDialog mDialog = new BottomListDialog(this, new BottomListDialog.onCallBack() {
            @Override
            public void callBack(int i) {
                switch (i) {
                    case 0:
                        startActivity(new Intent(AddDeviceTypeActivity.this, GuideBattery1Activty.class));
                        finish();
                        break;
                    case 1:
                        Intent intent = new Intent(AddDeviceTypeActivity.this, ConfigurationActivity.class);
                        intent.putExtra("devicetype", DeviceType.WIFISOKECT.toString());
                        startActivity(intent);
                        finish();
                        break;
                    case 2:
                        Intent intent2 = new Intent(AddDeviceTypeActivity.this, ConfigurationActivity.class);
                        intent2.putExtra("devicetype", DeviceType.WATERSENEOR.toString());
                        startActivity(intent2);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });
        mDialog.setMsg(title);
        mDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK) return;
        if(requestCode == 1){
            String sn = data.getExtras().getString("SCAN_RESULT");
            progressDialog = new ProgressDialog(this,R.string.wait);
            progressDialog.show();
            HekrUserAction.getInstance(this).registerAuth(sn, new HekrUser.RegisterOAuthQRCodeListener() {
                @Override
                public void registerSuccess() {
                    page = 0;
                    getDeviceList();
                }

                @Override
                public void registerFail(int errorCode) {
                    if(progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AddDeviceTypeActivity.this, Errcode.errorCode2Msg(AddDeviceTypeActivity.this,errorCode),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getDeviceList(){
        HekrUserAction.getInstance(this).getDevices(page, 20, new HekrUser.GetDevicesListener() {
            @Override
            public void getDevicesSuccess(List<DeviceBean> devicesLists) {
                if(devicesLists.size()>=20){
                    if(page == 0){
                        deviceBeanList.clear();
                    }
                    deviceBeanList.addAll(devicesLists);
                    page ++;
                    getDeviceList();
                }else {
                    page = 0;
                    deviceBeanList.addAll(devicesLists);

                    moveDeviceToFold();
                }
            }

            @Override
            public void getDevicesFail(int errorCode) {
                  page = 0;
                moveDeviceToFold();
            }
        });
    }

    private void moveDeviceToFold(){
        List<DeviceBean> listcache = deviceDao.findAllDevice();
        String devtid = "";
        String ctrlKey = "";
        for(int i=0;i<deviceBeanList.size();i++){
            boolean flag_has = false;

            for (int j=0;j<listcache.size();j++){
                if(listcache.get(j).getDevTid().equals(deviceBeanList.get(i).getDevTid())){
                    flag_has = true;
                    break;
                }
            }
            if(!flag_has){
                devtid = deviceBeanList.get(i).getDevTid();
                ctrlKey = deviceBeanList.get(i).getCtrlKey();
                break;
            }

        }
        HekrUserAction.getInstance(this).devicesPutFolder(FolderPojo.getInstance().folderId, ctrlKey, devtid, new HekrUser.DevicePutFolderListener() {
            @Override
            public void putSuccess() {
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(AddDeviceTypeActivity.this, getResources().getString(R.string.success_add),Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void putFail(int errorCode) {
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(AddDeviceTypeActivity.this, Errcode.errorCode2Msg(AddDeviceTypeActivity.this,errorCode),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocalService(){
        BaseDialog mDialog = new BaseDialog(this, new BaseDialog.OnCallBackToRefresh() {
            @Override
            public void onConfirm() {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, REQUEST_LOCATION_SERVICE);
            }

            @Override
            public void onCancel() {
                finish();
            }
        });
        mDialog.setTitleAndButton(getString(R.string.permission_reject_location_service_tip), getString(R.string.dialog_cancel), getString(R.string.goto_set));
        mDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (permissions.length == grantResults.length) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if(UnitTools.isLocServiceEnable(this)){
                            showTypeList();
                        }else {
                            getLocalService();
                        }
                    }else {
                        BaseDialog mDialog = new BaseDialog(this, new BaseDialog.OnCallBackToRefresh() {
                            @Override
                            public void onConfirm() {
                                PermissionUtils.startToSetting(AddDeviceTypeActivity.this);
                            }

                            @Override
                            public void onCancel() {
                                finish();
                            }
                        });
                        mDialog.setTitleAndButton(getString(R.string.permission_reject_location_tip), getString(R.string.dialog_cancel), getString(R.string.goto_set));
                        mDialog.show();
                    }
                }
            }
        }else if (requestCode == REQUEST_CAMERA) {
            if (permissions.length == grantResults.length) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        startActivityForResult(new Intent(this, ScanCaptureAct.class),1);
                    } else {
                        BaseDialog mDialog = new BaseDialog(this, new BaseDialog.OnCallBackToRefresh() {
                            @Override
                            public void onConfirm() {
                                PermissionUtils.startToSetting(AddDeviceTypeActivity.this);
                            }

                            @Override
                            public void onCancel() {
                                finish();
                            }
                        });
                        mDialog.setTitleAndButton(getString(R.string.permission_reject_camera_tip), getString(R.string.dialog_cancel), getString(R.string.goto_set));
                        mDialog.show();
                    }
                }
            }
        }
    }
}
