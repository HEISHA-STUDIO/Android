package com.hs.uav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.lifecycle.ViewModelProviders;

import com.MAVLink.DLink.msg_command_int;
import com.MAVLink.DLink.msg_mission_count;
import com.MAVLink.DLink.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_SEVERITY;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hs.uav.common.app.AppController;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.common.app.Injection;
import com.hs.uav.common.entity.MapAirLineInfo;
import com.hs.uav.common.entity.PointInfo;
import com.hs.uav.common.utils.GCJ02ToWGS84Util;
import com.hs.uav.common.view.DragScaleView;
import com.hs.uav.common.view.RoundRectImageView;
import com.hs.uav.databinding.ActivityMainBinding;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;
import com.hs.uav.logic.listener.OnRefreshUavConfigLinstener;
import com.hs.uav.logic.mqtt.Config;
import com.hs.uav.logic.mqtt.MqttService;
import com.hs.uav.moudle.MainViewModel;
import com.hs.uav.moudle.main.FragmentGroup;
import com.hs.uav.moudle.main.SmallFragmentGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.goldze.mvvmhabit.BR;
import me.goldze.mvvmhabit.base.AppManager;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.base.MySystemTipsDialog;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.constant.Contact;

/***
 * 主界面管理UI
 * @author tony.liu
 */
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements
        OnRefreshUavConfigLinstener {
    private FragmentGroup fg;
    private SmallFragmentGroup sfg;
    public PowerManager.WakeLock mWakeLock;
    private CommonDaoUtils<MapAirLineInfo> mapLineDaoUtils;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public final static int MODEL_VIDEO = 0; //摄像头模式
    public final static int MODEL_MAP = 1;//地图模式

    public static Context mContext;
    public static int MODEL = 0;//当前主界面显示的视图是视频还是地图
    public static int SMALL_MODEL = 0;//当前小窗显示视图，0-地图，1-视频
    public static int videoSourceFrom = 1;//0-停机坪摄像头 1-无人机摄像头
    public static float CURRENT_LAT = 0f;
    public static float CURRENT_LON = 0f;
    public static boolean AIRLINE = false;//是否有航线
    public static String CUR_PIN_CODE = Injection.aasDataRe().getUserName();
    public AMapLocationClient mLocationClient;
    public AMapLocationClientOption mLocationOption = null;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public MainViewModel initViewModel() {
        AppController.getInstance().regOnRefreshUavConfigLinstener(this);
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(MainViewModel.class);
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void initData() {
        super.initData();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        }

        mContext = this;
        SPUtils.getInstance().put("video_type", 1);//默认为停机坪摄像头
        DaoUtilsStore _Store = DaoUtilsStore.getInstance();
        mapLineDaoUtils = _Store.getmapAirLineInfoDaoUtils();

        Config config = new Config.Builder().setServerUrl(Injection.aasDataRe().getConfigInfo())
                .setClientID(Contact.getAndroidID())
                .create();
        MqttService.startService(MainActivity.this, config);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        fg = new FragmentGroup(getSupportFragmentManager(), R.id.fl_main_container);
        sfg = new SmallFragmentGroup(getSupportFragmentManager(), R.id.container);
        fg.onItemSelect(MODEL_VIDEO, null);
        sfg.onItemSelect(0, null);
        localCurrentPoint();
        changeVideoSource();
        addMapPointEvent();
        uploadAirPoint();
        airContralManager();
        getMapAirImgListView();
        changeSmallWindow();
        //拖动视图动态设置大小
        binding.indexWindowView.setChangeViewListener(new DragScaleView.OnRefreshViewChangeListener() {
            @Override
            public void refreshSizeView(int oriLeft, int oriRight, int oriTop, int oriBottom) {
                int width = oriRight - oriLeft;
                int height = oriBottom - oriTop;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

                params.leftMargin = Contact.dip2px(MainActivity.this, 30);
                params.bottomMargin = Contact.dip2px(MainActivity.this, 20);
                binding.container.setLayoutParams(params);
            }

            @Override
            public void refreshStop(int oriLeft, int oriRight, int oriTop, int oriBottom) {
                int width = oriRight - oriLeft;
                int height = oriBottom - oriTop;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

                params.leftMargin = Contact.dip2px(MainActivity.this, 30);
                params.bottomMargin = Contact.dip2px(MainActivity.this, 20);
                binding.indexWindowView.setLayoutParams(params);
            }
        });
        binding.toggerCloseIv.setOnClickListener(view -> {
            if (binding.indexWindowView.getVisibility() == View.VISIBLE) {
                binding.toggerCloseIv.setImageResource(R.mipmap.btn_arrow_open);
                showSmallWindow(false);
            } else {
                binding.toggerCloseIv.setImageResource(R.mipmap.btn_arrow_close);
                showSmallWindow(true);
            }
        });
    }

    /***
     * 定位当前手机所在位置
     */
    public void localCurrentPoint() {
        mLocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        aMapLocation.getLocationType();
                        aMapLocation.getLatitude();//获取纬度
                        aMapLocation.getLongitude();//获取经度
                        CURRENT_LAT = (float) aMapLocation.getLatitude();
                        CURRENT_LON = (float) aMapLocation.getLongitude();
                        KLog.e("当前位置所在经纬度：" + aMapLocation.getLatitude() + ",," + aMapLocation.getLongitude());
                    } else {
                        Log.e("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
                    }
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    private void changeFragment(int type) {
        MODEL = type;
        if (type == 0) {
            if (fg.gmf != null) {
                fg.gmf.aMap = null;
            }
            showSmallWindow(true);
            binding.batteryPro.setVisibility(View.VISIBLE);
        } else {
            showSmallWindow(false);
            binding.batteryPro.setVisibility(View.GONE);
        }
        fg.onItemSelect(type, null);
        binding.airHomeIv.setVisibility(type == MODEL_VIDEO ? View.VISIBLE : View.GONE);
        binding.airUpPointIv.setVisibility(type == MODEL_VIDEO ? View.GONE : View.VISIBLE);
        binding.changeVideoIv.setVisibility(type == MODEL_VIDEO ? View.VISIBLE : View.GONE);
    }

    private void showSmallWindow(boolean flag) {
        binding.indexWindowView.setVisibility(flag ? View.VISIBLE : View.GONE);
        binding.container.setVisibility(flag ? View.VISIBLE : View.GONE);
        sfg.MGDF.mMapView.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    /****
     * 切换摄像头数据源
     */
    private void changeVideoSource() {
        binding.changeVideoIv.setOnClickListener(view -> {
            int videoType = SPUtils.getInstance().getInt("video_type");
            if (videoType == 1) {//切换至无人机摄像头
                SPUtils.getInstance().put("video_type", 2);
                msg_command_int msg_command_int = new msg_command_int();
                msg_command_int.command = MAV_CMD.MAV_CMD_VIDEO_STREAMING_REQUEST;
                msg_command_int.param1 = 2;
                MqttService.publish(msg_command_int.pack().encodePacket());
                binding.changeVideoIv.setImageResource(R.mipmap.ic_moncam);
            } else if (videoType == 2) {//切换至停机坪摄像头
                SPUtils.getInstance().put("video_type", 1);
                msg_command_int msg_command_int = new msg_command_int();
                msg_command_int.command = MAV_CMD.MAV_CMD_VIDEO_STREAMING_REQUEST;
                msg_command_int.param1 = 1;
                MqttService.publish(msg_command_int.pack().encodePacket());
                binding.changeVideoIv.setImageResource(R.mipmap.ic_drone_view);
            }
        });
    }

    /***
     * 添加航线数据，切换到地图模式
     */
    private void addMapPointEvent() {
        binding.addMapPointIv.setOnClickListener(view -> {
            //备飞前可进行航线编辑操作
            if (!SPUtils.getInstance().getBoolean(CUR_PIN_CODE + "_flyResult")) {
                if (MODEL_VIDEO == MODEL) {
                    changeFragment(MODEL_MAP);
                } else {
                    fg.gmf.airLineID = "";
                    fg.gmf.mInfo = null;
                    fg.gmf.pointList.clear();
                    fg.gmf.pointList = new ArrayList<>();
                    fg.gmf.setAirLineMode("");
                }
            }
        });
    }

    /***
     * 飞行控制
     */
    private void airContralManager() {
        binding.airHomeIv.setOnClickListener(view -> {
            if (videoSourceFrom == 0) {
                videoSourceFrom = 1;
                binding.airHomeIv.setImageResource(R.mipmap.ic_pad_control);
            } else {
                videoSourceFrom = 0;
                binding.airHomeIv.setImageResource(R.mipmap.icon_flight);
            }
            fg.vmf.showAirController(videoSourceFrom);
        });
    }

    /****
     * 上传航线信息
     */
    private void uploadAirPoint() {
        binding.airUpPointIv.setOnClickListener(view -> {
            if (fg.gmf != null && fg.gmf.pointList != null && !fg.gmf.pointList.isEmpty()) {
                fg.gmf.aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {
                        try {
                            String uuid = "H" + UUID.randomUUID().hashCode() + "Z";
                            String filePath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/DNest_Air_Point_"
                                    + uuid + ".jpg";
                            FileOutputStream fos = new FileOutputStream(filePath);
                            boolean ifSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                            fos.flush();
                            fos.close();
                            if (ifSuccess) {
                                if (!TextUtils.isEmpty(fg.gmf.airLineID)) {
                                    if (fg.gmf.mInfo != null) {
                                        fg.gmf.mInfo.setPreImagePath(filePath);
                                        fg.gmf.mInfo.setCreateTime(dateFormat.format(new Date()));
                                        fg.gmf.mInfo.setPointsJson(new Gson().toJson(fg.gmf.pointList));
                                        mapLineDaoUtils.update(fg.gmf.mInfo);
                                        //保存当前设备的最后航线
                                        Injection.aasDataRe().saveLastMapLineId(CUR_PIN_CODE,
                                                fg.gmf.mInfo.getUUID());
                                    }
                                } else {
                                    String uid = UUID.randomUUID().toString().replace("-", "");
                                    MapAirLineInfo info = new MapAirLineInfo();
                                    info.setPreImagePath(filePath);
                                    info.setUUID(uid);
                                    info.setPinCode(CUR_PIN_CODE);
                                    info.setCreateTime(dateFormat.format(new Date()));
                                    info.setPointsJson(new Gson().toJson(fg.gmf.pointList));
                                    mapLineDaoUtils.insert(info);
                                    //保存当前最后航线
                                    Injection.aasDataRe().saveLastMapLineId(CUR_PIN_CODE, info.getUUID());
                                }
                                //将MapPoint推送至服务器端
                                //第一步，通知Histation 航点count数量
                                msg_mission_count msg_mission_count = new msg_mission_count();
                                msg_mission_count.count = fg.gmf.pointList.size();
                                MqttService.publish(msg_mission_count.pack().encodePacket());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMapScreenShot(Bitmap bitmap, int i) {

                    }
                });
            } else {
                ToastUtils.showLong("您并未定制航线的航点信息");
            }
        });
    }

    /***
     * 获取航线地图预览列表
     */
    private void getMapAirImgListView() {
        List<MapAirLineInfo> mapAirLineInfos = mapLineDaoUtils.queryByNativeSql(" where PIN_CODE = ? ",
                new String[]{CUR_PIN_CODE});
        binding.airMapsLl.removeAllViews();
        if (mapAirLineInfos != null && !mapAirLineInfos.isEmpty()) {
            AIRLINE = true;
            binding.airMapsLl.setVisibility(View.VISIBLE);
            for (MapAirLineInfo mapAirLineInfo : mapAirLineInfos) {

                View view = LayoutInflater.from(this).inflate(R.layout.item_air_mapline_layout, null);
                RoundRectImageView mMapIV = view.findViewById(R.id.map_iv);
                ImageView mDelIV = view.findViewById(R.id.del_map_iv);
                ImageView mSelectIV = view.findViewById(R.id.select_iv);

                mDelIV.setTag(mapAirLineInfo.getUUID());
                Glide.with(this).load(new File(mapAirLineInfo.getPreImagePath())).into(mMapIV);
                mSelectIV.setVisibility(mapAirLineInfo.getIsCheck() ? View.VISIBLE : View.GONE);

                //点击切换航线，在备飞前可以编辑切换航线
                mMapIV.setOnClickListener(v -> {
                    int readyStatus = SPUtils.getInstance().getInt(CUR_PIN_CODE + "_readyStatus");
                    if (readyStatus == 0) {//备飞前可进行航线编辑操作
                        if (MODEL_VIDEO == MODEL) {
                            binding.batteryPro.setVisibility(View.GONE);
                            changeFragment(MODEL_MAP);//切换到地图
                            fg.gmf.setAirLineMode(mapAirLineInfo.getUUID());
                        } else {
                            for (MapAirLineInfo mapAirLineInfo1 : mapAirLineInfos) {
                                if (mapAirLineInfo1.getUUID().equals(mapAirLineInfo.getUUID())) {
                                    mapAirLineInfo1.setIsCheck(true);
                                } else {
                                    mapAirLineInfo1.setIsCheck(false);
                                }
                                mapLineDaoUtils.update(mapAirLineInfo1);
                            }
                            getMapAirImgListView();
                            resetMapAirLineView(mapAirLineInfo);
                        }
                    }
                });

                //删除航线
                mDelIV.setOnClickListener(v -> {
                    MySystemTipsDialog dialog = new MySystemTipsDialog(this);
                    dialog.setItem("Are you sure want to delete?", new MySystemTipsDialog.OperaterListener() {
                        @Override
                        public void operater() {
                            mapLineDaoUtils.delete(mapAirLineInfo);
                            getMapAirImgListView();
                        }
                    });
                    dialog.show();
                });

                //长按地图缩略图时只进行航线删除操作
                mMapIV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int readyStatus = SPUtils.getInstance().getInt(CUR_PIN_CODE + "_readyStatus");
                        if (readyStatus == 0) {
                            mDelIV.setVisibility(View.VISIBLE);
                            mSelectIV.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                });
                binding.airMapsLl.addView(view);
            }
            binding.airMapsHsv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.airMapsHsv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            }, 100);
        } else {
            binding.airMapsLl.setVisibility(View.GONE);
            AIRLINE = false;//当前没有航向了
            if (fg.gmf != null) {
                fg.gmf.airLineID = "";
            }
        }
    }

    @Override
    public void refreshBattery(int currentBattery) {
        viewModel.currentBattery.set(currentBattery + "%");
        if (currentBattery >= 0 && currentBattery <= 30) {
            binding.batteryIv.setImageResource(R.mipmap.ic_status_battery_red);
        } else if (currentBattery > 30 && currentBattery <= 60) {
            binding.batteryIv.setImageResource(R.mipmap.ic_status_battery_yellow);
        } else if (currentBattery > 60 && currentBattery <= 100) {
            binding.batteryIv.setImageResource(R.mipmap.ic_status_battery_white);
        }

        //实时更新进度
        binding.batteryPro.setVisibility(View.VISIBLE);
        binding.batteryPro.setProgress(currentBattery);
        if (binding.batteryPro.getProgress() <= 20) {
            binding.batteryPro.setSecondaryProgress(binding.batteryPro.getProgress());
            if (binding.batteryPro.getProgress() <= 0) {
                runOnUiThread(() -> {
                    binding.batteryPro.setVisibility(View.GONE);
                });
            }
        }
    }

    @Override
    public void refreshBatteryOutTime(int outTimeBattery, int totalTimeBattery) {
        //剩余电量飞行时间
        int hour = outTimeBattery / 3600;//!小时
        int minute = outTimeBattery % 3600 / 60; //!分钟
        int second = outTimeBattery % 60;  //!秒
        if (hour == 0) {
            binding.batteryPro.SetValue(minute + "'" + second + "''");
        } else {
            binding.batteryPro.SetValue(hour + "°" + minute + "'" + second + "''");
        }
    }

    @Override
    public void refreshSignStatus(int signStatus) {
        //遥控器是否在线
        if (signStatus < 10) {
            binding.signIv.setImageResource(R.mipmap.ic_status_signalbar);
            binding.signIv.setAlpha(0.5f);
        }
        //检查无人机是否在线
        int connStatus = Short.parseShort(String.valueOf(signStatus)) >> 4;
        //无人机遥控器连接状态
        SPUtils.getInstance().put(CUR_PIN_CODE + "_isRcLock", connStatus == 0 ? 0 : 1);
        if (connStatus == 0) {
            //无人机离线
            binding.batteryIv.setImageResource(R.mipmap.ic_status_battery_white);
            binding.batteryIv.setAlpha(0.5f);
        } else {
            binding.batteryIv.setAlpha(1f);
        }

        //第6位代表飞行条件检查，0代表不具备飞行条件，1代表满足飞行条件。
        int readyStatus = Short.parseShort(String.valueOf(signStatus)) >> 5;
        //记录当前无人机备飞状态
        SPUtils.getInstance().put(CUR_PIN_CODE + "_readyStatus", readyStatus);

        fg.vmf.setUavFlyReadyStatus(readyStatus == 0);
        //第7位指示飞行状态，0代表未飞行，1代表飞行中。
        int flyStatus = Short.parseShort(String.valueOf(signStatus)) >> 6;
        fg.vmf.setUavFlyStatus(flyStatus == 0);

        //第8位指示任务暂停状态，0代表未暂停，1代表暂停中。
        int pauseStatus = Short.parseShort(String.valueOf(signStatus)) >> 7;
        if (flyStatus == 1 && pauseStatus == 0) {
            //显示飞机暂停按钮
            fg.vmf.setUavPauseStatus(true);
        } else if (flyStatus == 1 && pauseStatus == 1) {
            //显示继续按钮
            fg.vmf.setUavPauseStatus(false);
        }
    }

    @Override
    public void publishCurrentAirLinePoint(int seq, boolean isFinish) {
        if (fg.gmf == null || fg.gmf.pointList == null || (fg.gmf.pointList != null && fg.gmf.pointList.isEmpty())) {
            return;
        }
        if (isFinish) {
            fg.gmf.airLineID = "";
            fg.gmf.mInfo = null;
            fg.gmf.pointList.clear();
            fg.gmf.pointList = new ArrayList<>();
            changeFragment(MODEL_VIDEO);
            getMapAirImgListView();
            if (binding.upPointPro.getVisibility() != View.GONE) {
                binding.upPointPro.setVisibility(View.GONE);
                binding.systemTipsTv.setText("Upload WayPoint Success");
                binding.systemTipsTv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.systemTipsTv.setText("");
                    }
                }, 1500);
            }
        } else {
            binding.upPointPro.setMax(fg.gmf.pointList.size());
            binding.upPointPro.setProgress(seq + 1);
            binding.upPointPro.setVisibility(View.VISIBLE);
            binding.systemTipsTv.setText("Upload WayPoint ...");
            PointInfo pointInfo = fg.gmf.pointList.get(seq);
            KLog.e(TAG, "上传前坐标 x=" + pointInfo.getLat() + ",y = " + pointInfo.getLag());
            Map<String, Double> map = GCJ02ToWGS84Util.gcj2wgs(pointInfo.getLag(), pointInfo.getLat());
            msg_mission_item msg_mission_item = new msg_mission_item();
            msg_mission_item.seq = seq;
            msg_mission_item.command = 16;
            msg_mission_item.x = map.get("lat").floatValue();
            msg_mission_item.y = map.get("lon").floatValue();
            msg_mission_item.z = pointInfo.getAltitude();
            MqttService.publish(msg_mission_item.pack().encodePacket());
            KLog.e(TAG, "上传Point seq=" + msg_mission_item.seq + " x=" + msg_mission_item.x + " y=" + msg_mission_item.y);
        }
    }

    @Override
    public void showGpsNumView(int satellites_visible) {
        binding.satellitesVisibleNumTv.setText(String.valueOf(satellites_visible));
    }

    @Override
    public void currentSystemTips(int severity, String tips) {
        //显示当前系统提示语
        binding.systemTipsTv.setText(tips);
        switch (severity) {
            case MAV_SEVERITY.MAV_SEVERITY_ERROR:
                binding.stautsBarAir.setBackgroundResource(R.drawable.stauts_bar_red);
                break;
            case MAV_SEVERITY.MAV_SEVERITY_WARNING:
                binding.stautsBarAir.setBackgroundResource(R.drawable.stauts_bar_yellow);
                break;
            case MAV_SEVERITY.MAV_SEVERITY_INFO:
                binding.stautsBarAir.setBackgroundResource(R.drawable.stauts_bar_green);
                break;
            case MAV_SEVERITY.MAV_SEVERITY_NOTICE:
                binding.stautsBarAir.setBackgroundResource(R.drawable.stauts_bar_blue);
                break;
        }
    }

    @Override
    public void currentSignerStrong(int rssi) {
        //rssi 0-254 显示控制器信号强度
        int total = 254;
        binding.signIv.setAlpha(1f);
        if (rssi >= 0 && rssi <= total * 0.2) {
            binding.signIv.setImageResource(R.mipmap.ic_signal_1);
        } else if (rssi > total * 0.2 && rssi <= total * 0.4) {
            binding.signIv.setImageResource(R.mipmap.ic_signal_2);
        } else if (rssi > total * 0.4 && rssi <= total * 0.6) {
            binding.signIv.setImageResource(R.mipmap.ic_signal_3);
        } else if (rssi > total * 0.6 && rssi <= total * 0.8) {
            binding.signIv.setImageResource(R.mipmap.ic_signal_4);
        } else if (rssi > total * 0.8 && rssi <= total) {
            binding.signIv.setImageResource(R.mipmap.ic_status_signalbar);
        }
    }

    public void changeSmallWindow() {
        binding.indexWindowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SMALL_MODEL == 0) {//切换到视频
                    SMALL_MODEL = 1;
                    sfg.onItemSelect(1, null);
                    fg.onItemSelect(2, null);
                }else if (SMALL_MODEL == 1){//切换到地图
                    SMALL_MODEL = 0;
                    sfg.onItemSelect(0, null);
                    fg.onItemSelect(0, null);
                }
            }
        });
    }

    @Override
    public void refreshPointSmallImgView() {
        //刷新地图缩略图
        getMapAirImgListView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AppManager.getAppManager().finishAllActivity();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void resetMapAirLineView(MapAirLineInfo mapAirLineInfo) {
        fg.gmf.airLineID = "";
        fg.gmf.mInfo = null;
        fg.gmf.pointList.clear();
        fg.gmf.pointList = new ArrayList<>();
        fg.gmf.setAirLineMode(mapAirLineInfo.getUUID());
        fg.gmf.drawAirLineView();
    }
}
