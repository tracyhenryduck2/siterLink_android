package com.siterwell.application.device;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siterwell.application.common.Errcode;
import com.siterwell.sdk.bean.WaterSensorBean;
import com.siterwell.application.R;
import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.commonview.ParallaxListView;
import com.siterwell.application.device.bean.WarningHistoryBean;
import com.siterwell.application.device.bean.WaterSensorDescBean;
import com.siterwell.application.storage.DeviceDao;
import com.siterwell.sdk.common.RefreshWaterSensorListener;
import com.siterwell.sdk.common.SitewellSDK;
import com.siterwell.sdk.http.HekrUserAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ST-020111 on 2017/7/27.
 */

public class WaterSensorDetailActivity extends TopbarSuperActivity implements View.OnClickListener,ParallaxListView.IXListViewListener,RefreshWaterSensorListener{
    private final String TAG = WaterSensorDetailActivity.class.getName();
    private String deviceId;
    private ImageView signal,battery,waterIcon;
    private ImageView backbtn;
    private ImageView settinbtn;
    private TextView status;
    private DeviceDao batteryDao;
    private WaterSensorDescBean waterSensorDescBean;
    private WaterSensorHistoryParaAdapter batteryHistoryAdapter;
    private List<WarningHistoryBean> hislist;
    private ParallaxListView recyclerView_his;
    private int page = 0;
    private RelativeLayout relativeLayout_tip;//测试提示栏
    private Button btn_silence;

    @Override
    protected void onCreateInit() {
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_battery_detail;
    }

    private void initView(){
        SitewellSDK.getInstance(this).addRefreshWaterSensorListener(this);
        batteryDao = new DeviceDao(this);
        deviceId = getIntent().getStringExtra("deviceId");
        getTopBarView().setTopBarStatus(R.drawable.back, -1, getResources().getString(R.string.battery_detail), 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        },null,R.color.bar_bg);
        getTopBarView().setVisibility(View.GONE);

        View headerView = View.inflate(this, R.layout.battery_header_view, null);
        ImageView ivBackground = (ImageView) headerView.findViewById(R.id.ivBackground);
        btn_silence =(Button)headerView.findViewById(R.id.btnConfirm);
        btn_silence.setVisibility(View.GONE);
        backbtn = (ImageView)headerView.findViewById(R.id.back);
        signal = (ImageView)headerView.findViewById(R.id.signal);
        battery = (ImageView)headerView.findViewById(R.id.battery);
        waterIcon = (ImageView)headerView.findViewById(R.id.icon);
        settinbtn = (ImageView)headerView.findViewById(R.id.more);
        relativeLayout_tip = (RelativeLayout)headerView.findViewById(R.id.tishi);
        relativeLayout_tip.setVisibility(View.GONE);
        waterIcon.setImageResource(R.mipmap.waters1);
        status = (TextView)headerView.findViewById(R.id.status);
        recyclerView_his = (ParallaxListView)findViewById(R.id.historylist);
        recyclerView_his.setXListViewListener(this);
        recyclerView_his.addHeaderView(headerView);
        recyclerView_his.setParallaxImageView(ivBackground);
        hislist = new ArrayList<>();

        batteryHistoryAdapter = new WaterSensorHistoryParaAdapter(this,hislist);
        recyclerView_his.setAdapter(batteryHistoryAdapter);
        recyclerView_his.setPullLoadEnable(true);
        recyclerView_his.mFooterView.hide();
        backbtn.setOnClickListener(this);
        settinbtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        page = 0;
        queryhistory();
    }

    private void refresh(){
        waterSensorDescBean = batteryDao.findWaterSensorBySid(deviceId);
        Log.i(TAG," batteryDescBean.getStatus():"+ waterSensorDescBean.getStatus());

        getTopBarView().setTextTitle(TextUtils.isEmpty(waterSensorDescBean.getDeviceName())?DeviceActivitys.getDeviceType(waterSensorDescBean): waterSensorDescBean.getDeviceName());
        signal.setImageResource(WaterSensorDescBean.getSignal(waterSensorDescBean.getSignal()));
        battery.setImageResource(WaterSensorDescBean.getQuantinity(waterSensorDescBean.getBattPercent()));
        status.setText(waterSensorDescBean.getStatusDtail(waterSensorDescBean.getStatus()));
        status.setTextColor((ColorStateList)getResources().getColorStateList(WaterSensorDescBean.getStatusColor(waterSensorDescBean.getStatus())));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SitewellSDK.getInstance(this).removeRefreshWaterSensorListener(this);
    }


    private void queryhistory(){

        HekrUserAction.getInstance(this).getAlarmHistory(page, 20, waterSensorDescBean, new HekrUserAction.GetHekrDataListener() {
            @Override
            public void getSuccess(Object object) {
                Log.i(TAG,"object:"+object.toString());
                try {
                    JSONObject jsonObject = new JSONObject(object.toString());
                    JSONArray his = jsonObject.getJSONArray("content");
                    int pageload = jsonObject.getInt("number");
                    if(pageload == 0)  {
                        page = 0;
                        hislist.clear();
                    }


                    for(int i=0;i<his.length();i++){
                        WarningHistoryBean batteryHistoryBean = new WarningHistoryBean();
                        batteryHistoryBean.setWarningId(his.getJSONObject(i).getString("id"));
                        batteryHistoryBean.setContent(his.getJSONObject(i).getString("content"));
                        batteryHistoryBean.setWarningsubject(his.getJSONObject(i).getString("subject"));
                        batteryHistoryBean.setReportTime(his.getJSONObject(i).getLong("reportTime"));
                        hislist.add(batteryHistoryBean);
                    }

                    boolean last = jsonObject.getBoolean("last");
                    if(!last) {
                        page = pageload + 1;
                        recyclerView_his.mFooterView.show();
                    }else{
                        recyclerView_his.mFooterView.hide();
                    }
                    handler.sendEmptyMessage(6);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void getFail(int errorCode) {
                handler.sendEmptyMessage(6);
                Toast.makeText(WaterSensorDetailActivity.this, Errcode.errorCode2Msg(WaterSensorDetailActivity.this,errorCode),Toast.LENGTH_LONG).show();
            }
        });

    }


    //在view加载完成时设定缩放级别
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            recyclerView_his.setViewsBounds(2);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.more:
                Intent intent = new Intent(this,DeviceSettingActivity.class);
                intent.putExtra("deviceid",deviceId);
                startActivity(intent);
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    page = 0;
                    queryhistory();
                    break;
                case 5:
                    queryhistory();
                    break;
                case 6:
                    showfilterList();
                    break;

            }
        }
    };

    @Override
    public void onLoadMore() {
        queryhistory();
    }


    private void showfilterList(){

        batteryHistoryAdapter.Refresh(hislist);
        recyclerView_his.stopLoadMore();

        if(hislist.size()==0){
            recyclerView_his.mFooterView.showEmpty();
        }else{
            recyclerView_his.mFooterView.DisshowEmpty();
        }

    }

    @Override
    public void RefreshWaterSensor(WaterSensorBean waterSensorBean) {
        signal.setImageResource(WaterSensorDescBean.getSignal(waterSensorBean.getSignal()));
        battery.setImageResource(WaterSensorDescBean.getQuantinity(waterSensorBean.getBattPercent()));
        status.setText(waterSensorDescBean.getStatusDtail(waterSensorBean.getStatus()));
        status.setTextColor((ColorStateList)getResources().getColorStateList(WaterSensorDescBean.getStatusColor(waterSensorBean.getStatus())));

        if(waterSensorBean.getSignal()!=0 || waterSensorBean.getBattPercent() != 0 || waterSensorBean.getStatus() !=0){
            new Thread(new Runnable(){

                public void run(){

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.sendEmptyMessage(4); //告诉主线程执行任务

                }

            }).start();
        }

    }
}
