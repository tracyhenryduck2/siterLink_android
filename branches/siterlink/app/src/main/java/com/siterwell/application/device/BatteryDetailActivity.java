package com.siterwell.application.device;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siterwell.application.BaseActivity;
import com.siterwell.application.BusEvents.GetDeviceStatusEvent;
import com.siterwell.application.common.Errcode;
import com.siterwell.application.commonview.ProgressDialog;
import com.siterwell.sdk.bean.BatteryBean;
import com.siterwell.application.R;
import com.siterwell.application.common.DateUtil;
import com.siterwell.application.common.TopbarSuperActivity;
import com.siterwell.application.commonview.ParallaxListView;
import com.siterwell.application.device.bean.BatteryDescBean;
import com.siterwell.application.device.bean.WarningHistoryBean;
import com.siterwell.application.storage.DeviceDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import com.siterwell.sdk.common.RefreshBatteryListener;
import com.siterwell.sdk.common.SitewellSDK;
import com.siterwell.sdk.event.SilenceEvent;
import com.siterwell.sdk.event.UdpShakeHandsEvent;
import com.siterwell.sdk.http.HekrUserAction;
import com.siterwell.sdk.http.bean.DeviceBean;
import com.siterwell.sdk.protocol.BatteryCommand;

/**
 * Created by ST-020111 on 2017/4/14.
 */

public class BatteryDetailActivity extends BaseActivity implements View.OnClickListener,ParallaxListView.IXListViewListener,RefreshBatteryListener{
    private final String TAG = "BatteryDetailActivity";
    private String deviceId;
    private ImageView signal,battery;
    private ImageView backbtn;
    private ImageView settinbtn;
    private TextView status;
    private DeviceDao batteryDao;
    private BatteryDescBean batteryDescBean;
    private BatteryHistoryParaAdapter batteryHistoryAdapter;
    private List<WarningHistoryBean> hislist;
    private ParallaxListView recyclerView_his;
    private int page = 0;
    private int height = 0;
    private boolean show = false;
    private RelativeLayout relativeLayout_tip;//测试提示栏
    private Button btn_silence;
    private ProgressDialog progressDialog;
    private BatteryCommand batteryCommand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_detail);
        initView();
    }

    private void initView(){
        SitewellSDK.getInstance(this).addRefreshBatteryListener(this);
        EventBus.getDefault().register(this);
        batteryDao = new DeviceDao(this);
        height=(int)getResources().getDimension(R.dimen.battery_tip_test_height);
        deviceId = getIntent().getStringExtra("deviceId");

        View headerView = View.inflate(this, R.layout.battery_header_view, null);
        ImageView ivBackground = (ImageView) headerView.findViewById(R.id.ivBackground);
        relativeLayout_tip = (RelativeLayout)headerView.findViewById(R.id.tishi);
        relativeLayout_tip.setVisibility(View.GONE);
        btn_silence =(Button)headerView.findViewById(R.id.btnConfirm);
        btn_silence.setVisibility(View.GONE);
        btn_silence.setOnClickListener(this);
        backbtn = (ImageView)headerView.findViewById(R.id.back);
        settinbtn = (ImageView)headerView.findViewById(R.id.more);
        signal = (ImageView)headerView.findViewById(R.id.signal);
        battery = (ImageView)headerView.findViewById(R.id.battery);
        status = (TextView)headerView.findViewById(R.id.status);
        recyclerView_his = (ParallaxListView)findViewById(R.id.historylist);
        recyclerView_his.setXListViewListener(this);
        recyclerView_his.addHeaderView(headerView);
        recyclerView_his.setParallaxImageView(ivBackground);
        recyclerView_his.setPullLoadEnable(true);
        recyclerView_his.mFooterView.hide();
        hislist = new ArrayList<>();
        batteryHistoryAdapter = new BatteryHistoryParaAdapter(this,hislist);
        recyclerView_his.setAdapter(batteryHistoryAdapter);
        backbtn.setOnClickListener(this);
        settinbtn.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        page = 0;
        queryhistory();
        doActions();
    }

    private void refresh(){
        batteryDescBean = batteryDao.findBatteryBySid(deviceId);
        Log.i(TAG," batteryDescBean.getStatus():"+ batteryDescBean.getStatus());

        signal.setImageResource(BatteryDescBean.getSignal(batteryDescBean.getSignal()));
        battery.setImageResource(BatteryDescBean.getQuantinity(batteryDescBean.getBattPercent()));
        status.setText(batteryDescBean.getStatusDtail(batteryDescBean.getStatus()));
        status.setTextColor((ColorStateList)getResources().getColorStateList(BatteryDescBean.getStatusColor(batteryDescBean.getStatus())));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)         //订阅内网握手事件
    public  void onEventMainThread(UdpShakeHandsEvent event){
          if(event.getType()==3){
              Toast.makeText(BatteryDetailActivity.this, getResources().getString(R.string.local_udp_search_failed),Toast.LENGTH_LONG).show();
          }else if(event.getType()==2){
              progressDialog = new ProgressDialog(this,getResources().getText(R.string.wait));
              progressDialog.show();
              batteryCommand = new BatteryCommand(batteryDescBean,BatteryDetailActivity.this);
              batteryCommand.sendLocalSilence();
          }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)         //订阅内网握手事件
    public  void onEventMainThread(SilenceEvent event){
        batteryCommand.resetTimer();
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
            if(!TextUtils.isEmpty(event.getDevTid()) && event.getDevTid().equals(deviceId) && event.getSuccess()==1){
                Toast.makeText(BatteryDetailActivity.this, getResources().getString(R.string.silence_success),Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(BatteryDetailActivity.this, getResources().getString(R.string.timeout),Toast.LENGTH_LONG).show();
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//取消注册
        SitewellSDK.getInstance(this).removeRefreshBatteryListener(this);
    }


    private void queryhistory(){
        Log.i(TAG,"page:"+page);

        HekrUserAction.getInstance(this).getAlarmHistory(page, 20, batteryDescBean, new HekrUserAction.GetHekrDataListener() {
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
                        //handler.sendEmptyMessage(5);
                    }else{
                        //handler.sendEmptyMessage(6);
                        page = 0;
                        recyclerView_his.mFooterView.hide();
                    }
                    handler.sendEmptyMessage(6);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void getFail(int errorCode) {
                page = 0;
                handler.sendEmptyMessage(6);
                Toast.makeText(BatteryDetailActivity.this, Errcode.errorCode2Msg(BatteryDetailActivity.this,errorCode),Toast.LENGTH_LONG).show();
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
             case R.id.btnConfirm:
                 //batteryCommand.sendCommand(1,dataReceiverListener);
                 UdpShakeHandsEvent udpShakeHandsEvent = new UdpShakeHandsEvent();
                 udpShakeHandsEvent.setType(1);
                 EventBus.getDefault().post(udpShakeHandsEvent);
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


    private void showfilterList(){


//        List<WarningHistoryBean> deletelist = new ArrayList<WarningHistoryBean>();
//        if(hislist.size()<=1){
//            batteryHistoryAdapter.Refresh(hislist);
//        }else{
//            for(int i=0;i<hislist.size()-1;i++){
//
//                if(hislist.get(i).getWarningsubject().equals(hislist.get(i+1).getWarningsubject())){
//                     if(Math.abs(hislist.get(i).getReportTime() - hislist.get(i+1).getReportTime())<=60000l){
//                         deletelist.add(hislist.get(i+1));
//                     }
//                }
//            }
//            Log.i(TAG,"ds需要删除的索引:"+deletelist.toString());
//
//            hislist.removeAll(deletelist);
//
            batteryHistoryAdapter.Refresh(hislist);
            recyclerView_his.stopLoadMore();
//        }

        if(hislist.size()==0){
           recyclerView_his.mFooterView.showEmpty();
        }else{
            recyclerView_his.mFooterView.DisshowEmpty();
        }

        if(hislist.size()==0){
            performAnim2(true);
        }else{
            if(DateUtil.is_current_7daybefore(hislist.get(0).getReportTime())){
                performAnim2(true);
            }else{
                performAnim2(false);
            }
        }

    }


    private void doActions() {

        List<DeviceBean> deviceBeanList = new ArrayList<>();

        DeviceBean deviceBean = batteryDao.findDeviceBySid(deviceId);
        deviceBeanList.add(deviceBean);

        GetDeviceStatusEvent getDeviceStatusEvent = new GetDeviceStatusEvent();
        getDeviceStatusEvent.setDeviceBeans(deviceBeanList);
        EventBus.getDefault().post(getDeviceStatusEvent);

    }


    @Override
    public void onLoadMore() {
        queryhistory();
    }


    private void performAnim2(boolean open){
        //View是否显示的标志
        show = open;
        //属性动画对象
        ValueAnimator va ;
        if(show){
            //显示view，高度从0变到height值
            relativeLayout_tip.setVisibility(View.VISIBLE);
            va = ValueAnimator.ofInt(0,height);
        }else{
            //隐藏view，高度从height变为0
            va = ValueAnimator.ofInt(height,0);
        }
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(Integer)valueAnimator.getAnimatedValue();
                //动态更新view的高度
                relativeLayout_tip.getLayoutParams().height = h;
                relativeLayout_tip.requestLayout();
            }
        });
        va.setDuration(1000);
        //开始动画
        va.start();
    }

    @Override
    public void RefreshBattery(BatteryBean batteryBean) {
        signal.setImageResource(BatteryDescBean.getSignal(batteryBean.getSignal()));
        battery.setImageResource(BatteryDescBean.getQuantinity(batteryBean.getBattPercent()));
        status.setText(batteryDescBean.getStatusDtail(batteryBean.getStatus()));
        status.setTextColor((ColorStateList)getResources().getColorStateList(BatteryDescBean.getStatusColor(batteryBean.getStatus())));

        if(batteryBean.getSignal()!=0 || batteryBean.getBattPercent() != 0 || batteryBean.getStatus() !=0){
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
