package com.hs.uav.logic.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.MAVLink.DLink.msg_attitude;
import com.MAVLink.DLink.msg_battery_batterystatus;
import com.MAVLink.DLink.msg_camera_image_captured;
import com.MAVLink.DLink.msg_chargepad_status;
import com.MAVLink.DLink.msg_command_ack;
import com.MAVLink.DLink.msg_command_int;
import com.MAVLink.DLink.msg_command_progress;
import com.MAVLink.DLink.msg_global_position_int;
import com.MAVLink.DLink.msg_gps_raw_int;
import com.MAVLink.DLink.msg_heartbeat;
import com.MAVLink.DLink.msg_mediafile_count;
import com.MAVLink.DLink.msg_mediafile_data_segment;
import com.MAVLink.DLink.msg_mediafile_information;
import com.MAVLink.DLink.msg_mission_ack;
import com.MAVLink.DLink.msg_mission_count;
import com.MAVLink.DLink.msg_mission_item;
import com.MAVLink.DLink.msg_mission_request;
import com.MAVLink.DLink.msg_radio_status;
import com.MAVLink.DLink.msg_statustext;
import com.MAVLink.DLink.msg_sys_status;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_RESULT;
import com.MAVLink.enums.MEDIATYPE;
import com.google.gson.Gson;
import com.hs.uav.common.app.AppController;
import com.hs.uav.common.app.Injection;
import com.hs.uav.common.entity.MapAirLineInfo;
import com.hs.uav.common.entity.PointInfo;
import com.hs.uav.common.utils.GCJ02ToWGS84Util;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.constant.Contact;

/**
 * Created by Shuai
 * 09/12/2019.
 */
public class MqttService extends Service {
    public final String TAG = MqttService.class.getSimpleName();
    private static volatile boolean isChecking = false;
    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    private static final Object sync = new Object();

    //4、	订阅topic-1接收来自设备的mavlink数据。
    //5、	发布topic-2向设备发送mavlink数据。
    //6、	订阅topic接收来自设备的视频数据
    private static String TOPIC_PANEL_RECE_UNVIDEO_DATA = Injection.aasDataRe().getPublicKey() + "-1";//订阅来自设备的MavLink数据
    private static String TOPIC_PANEL_PUBLISH_DATA = Injection.aasDataRe().getPublicKey() + "-2";//发布topic-2向设备发送mavlink数据
    private static String TOPIC_PANEL_VIDEO_DATA = Injection.aasDataRe().getPublicKey();//订阅topic接收来自设备的视频数据

    private static Handler mHandler;
    private static HandlerThread mHandlerThread;
    private final static int MQTT_QOS_HIGH = 2;
    private final static boolean MQTT_RETAINED = false;
    private MAVLinkPacket mavLinkPacket;//协议包解析工具
    private static Handler videoHandler;

    private static int currentIndex = 0;//当前包序列号
    private List<VideoSource> currentFrameVideoList = new ArrayList<>();//当前帧的数据包集合
    private msg_mediafile_information currentMediaFile;//当前下载的文件信息
    private byte[] currentFileBytes;//当前文件信息的数组
    private List<msg_mediafile_data_segment> mediaFileDataSegmentList = new ArrayList<>();

    private static int pointCount = 0;//当前航线下载个数
    private static int currentPointIndex = 0;//当前请求的航点标记
    private static List<PointInfo> pointInfos = new ArrayList<>();//当前航线航点信息;
    private CommonDaoUtils<MapAirLineInfo> mapAirLineInfoDaoUtils;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void setHandler(Handler handler) {
        videoHandler = handler;
    }

    @Override
    public void onCreate() {
        init();
        DaoUtilsStore _Store = DaoUtilsStore.getInstance();
        mapAirLineInfoDaoUtils = _Store.getmapAirLineInfoDaoUtils();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 开启服务
     *
     * @param mContext context 上下文
     * @param config   配置项
     */
    public static void startService(Context mContext, Config config) {
        mContext.startService(new Intent(mContext, MqttService.class));
    }

    /**
     * 关闭服务
     */
    public static void stopService(Context mContext) {
        mContext.stopService(new Intent(mContext, MqttService.class));
    }

    /**
     * 发送check心跳包
     */
    public static void startChecking() {
        if (!isChecking) {
            synchronized (sync) {
                if (!isChecking) {
                    isChecking = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            publish(new msg_heartbeat().pack().encodePacket());
                            mHandler.postDelayed(this, 6 * 1000);
                        }
                    }, 6 * 1000);
                }
            }
        }
    }

    public static void stopChecking() {
        if (isChecking) {
            synchronized (sync) {
                if (isChecking) {
                    isChecking = false;
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
        }
    }

    /***
     * 向服务器push消息
     * @param message
     */
    public static void publish(byte[] message) {
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message);
            mqttMessage.setQos(0);
            if (mqttAndroidClient != null) {
                mqttAndroidClient.publish(TOPIC_PANEL_PUBLISH_DATA, mqttMessage);
            } else {
                KLog.e("MQTT Client is Null");
            }
        } catch (MqttException e) {
            KLog.e(Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void init() {
        if(TextUtils.isEmpty(Injection.aasDataRe().getUserName())){
            return;
        }
        initHandler();
        String serverURI = Injection.aasDataRe().getConfigInfo();
        String DeviceID = Contact.getAndroidID();
        Log.e(TAG, "DeviceID:" + DeviceID);
        Log.e(TAG, "serverURI:" + serverURI);
        Log.e(TAG, "userName:" + Injection.aasDataRe().getUserName());
        Log.e(TAG, "password:" + Injection.aasDataRe().getPassword());
        mqttAndroidClient = new MqttAndroidClient(this, serverURI, DeviceID);
        mqttAndroidClient.setCallback(mqttCallback); //设置监听订阅消息的回调
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存
        mMqttConnectOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(20); //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions.setUserName(Injection.aasDataRe().getUserName()); //设置用户名
        mMqttConnectOptions.setPassword(Injection.aasDataRe().getPassword().toCharArray()); //设置密码
        boolean doConnect = true;
        //doConnect = sendLastLiveMsg();
        if (doConnect) {
            doClientConnection();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!mqttAndroidClient.isConnected() && isConnectIsNormal()) {
            try {
                currentIndex = 0;
                currentFrameVideoList = new ArrayList<>();
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * MQTT是否连接成功的监听
     * 并订阅消息数据
     */
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken arg0) {
            try {
                Log.e(TAG, "MQTT服务连接成功");
                startChecking();
                // 订阅主题，参数：主题、服务质量,subscribe必须写在这，不然broker重启后subscribe不会恢复
                mqttAndroidClient.subscribe(TOPIC_PANEL_RECE_UNVIDEO_DATA, 2);
                mqttAndroidClient.subscribe(TOPIC_PANEL_VIDEO_DATA, 2);
                //初始化摄像头视频源 1-停机坪摄像头，2-飞机摄像头，3-下载源文件（在图片文件预览的界面要做切换）
                msg_command_int msg_command_int = new msg_command_int();
                msg_command_int.command = MAV_CMD.MAV_CMD_VIDEO_STREAMING_REQUEST;
                msg_command_int.param1 = 1;
                publish(msg_command_int.pack().encodePacket());

                //获取当前无人机状态
                msg_command_int msgCommandInt = new msg_command_int();
                msgCommandInt.command = MAV_CMD.MAV_CMD_PAD_REQUEST_STATUS;
                publish(msgCommandInt.pack().encodePacket());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            mHandler.postDelayed(() -> {
                Log.e(TAG, "MQTT服务连接异常 进行重连中...");
                doClientConnection();
            }, 5000);
        }
    };

    /***
     * 订阅主题的回调
     */
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (topic.equals(TOPIC_PANEL_VIDEO_DATA)) {
                //视频实时流数据返回
                VideoSource videoSource = parseVideoSource(message.getPayload());
                if (currentIndex == 0) {
                    currentIndex = videoSource.index;
                    currentFrameVideoList.add(videoSource);
                    if (videoSource.num == 1) {
                        //当前帧只有一个数据包，直接渲染
                        Bundle bundle = new Bundle();
                        bundle.putLong("pts", videoSource.pts);
                        bundle.putByteArray("videoSource", videoSource.payload);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = 1000;
                        videoHandler.sendMessage(msg);
                        currentFrameVideoList = new ArrayList<>();
                        currentIndex = 0;
                    }
                } else {
                    if (currentIndex == videoSource.index) {//当前是同一帧的数据包，执行拼包流程
                        currentFrameVideoList.add(videoSource);
                        if (videoSource.seq == videoSource.num - 1) {
                            //数据包已拼包完成
                            int totalLength = 0;
                            for (VideoSource videoSource1 : currentFrameVideoList) {
                                totalLength += videoSource1.len;
                            }
                            byte[] bytes = new byte[totalLength];
                            int length = 0;
                            for (VideoSource videoSource1 : currentFrameVideoList) {
                                System.arraycopy(videoSource1.payload, 0, bytes, length, videoSource1.len);
                                length += videoSource1.len;
                            }
                            Bundle bundle = new Bundle();
                            bundle.putLong("pts", videoSource.pts);
                            bundle.putByteArray("videoSource", bytes);
                            Message msg = new Message();
                            msg.setData(bundle);
                            msg.what = 1000;
                            videoHandler.sendMessage(msg);
                            //重置拼包
                            currentFrameVideoList = new ArrayList<>();
                            currentIndex = 0;
                        }
                    } else {
                        KLog.e(TAG, "新的帧数据：" + videoSource.index);
                        currentIndex = 0;
                        currentFrameVideoList = new ArrayList<>();
                    }
                }
            } else if (TOPIC_PANEL_RECE_UNVIDEO_DATA.equals(topic)) {
                Parser mavLinkParser = new Parser();
                for (int i = 0; i < message.getPayload().length; i++) {
                    int ch = 0xFF & message.getPayload()[i];
                    if ((mavLinkPacket = mavLinkParser.mavlink_parse_char(ch)) != null) {
                        handleMavLinkMsg(mavLinkPacket);
                    }
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void connectionLost(Throwable arg0) {
            mHandler.postDelayed(() -> {
                Log.e(TAG, "MQTT服务连接失败 进行重连中...");
                currentIndex = 0;
                currentFrameVideoList = new ArrayList<>();
                try {
                    if (mqttAndroidClient != null) {
                        mqttAndroidClient.disconnect();
                        mqttAndroidClient.unregisterResources();
                    }
                    init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 5000);
        }
    };

    /***
     * 视频流数据分包解析器
     * @param bytes
     * @return
     */
    public static VideoSource parseVideoSource(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        VideoSource videoSource = new VideoSource();
        byte[] byteTemp = new byte[4];
        byteBuffer.get(byteTemp, 0, 4);
        videoSource.index = bytesToInt(byteTemp, 0);

        byteTemp = new byte[4];
        byteBuffer.get(byteTemp, 0, 4);
        videoSource.num = bytesToInt(byteTemp, 0);

        byteTemp = new byte[4];
        byteBuffer.get(byteTemp, 0, 4);
        videoSource.seq = bytesToInt(byteTemp, 0);

        byteTemp = new byte[4];
        byteBuffer.get(byteTemp, 0, 4);
        videoSource.len = bytesToInt(byteTemp, 0);

        videoSource.payload = new byte[videoSource.len];
        byteBuffer.get(videoSource.payload);

        byteTemp = new byte[8];
        byteBuffer.get(byteTemp, 0, 8);
        videoSource.pts = bytesToLong(byteTemp, 0);
        return videoSource;
    }


    /****
     * byte 数组取long 数值，转小端模式
     * @param input
     * @param offset
     * @return
     */
    public static long bytesToLong(byte[] input, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(input, offset, 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序。
     *
     * @param ary    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000));
        return value;
    }

    /***
     * 数据转化为图像
     * @param bytes
     * @return
     */
    public static Bitmap convertByteToBitmap(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        bytes = new byte[buf.remaining()];
        buf.get(bytes, 0, bytes.length);
        buf.clear();
        bytes = new byte[buf.capacity()];
        buf.get(bytes, 0, bytes.length);
        final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bmp;
    }

    public void saveBitmap(Bitmap bitmap, String path, String filename) {
        File file = new File(path + filename);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /****
     * 接收到的消息体进行对应的协议解析
     * @param packet 消息协议解析器
     */
    private void handleMavLinkMsg(MAVLinkPacket packet) {
        switch (packet.msgid) {
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT://心跳包
                //协议说明：HiStation每秒钟发布一个心跳包，以指示自身的状态。HiStation中只用到了system_status字段，
                //该字段分为低4位和4位，第四位采用顺序编码，数值可以为0~15，其中0~9为原协议保留，10表示DJI 遥控器已连接上HiStation应用。
                //11~15保留。高4位按位编码，
                //第5位指示无人机连接状态，0代表未连接，1代表已连接。
                //第6位代表飞行条件检查，0代表不具备飞行条件，1代表满足飞行条件。
                //第7位指示飞行状态，0代表未飞行，1代表飞行中。
                //第8位指示任务暂停状态，0代表未暂停，1代表暂停中。
                msg_heartbeat msg = (msg_heartbeat) packet.unpack();
                AppController.getInstance().postRefreshUavSignStatus(msg.system_status);
                break;
            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE://飞行姿态
                //协议说明：HiStation只实现了roll/pitch/yaw三个字段，分别上报无人机的横滚角、俯仰角及偏航角。
                msg_attitude msgAttitude = (msg_attitude) packet.unpack();
                //KLog.e(TAG, "receive_msg = " + msgAttitude.toString());
                break;
            case msg_battery_batterystatus.MAVLINK_MSG_ID_BATTERY_batterySTATUS:
                //电池剩余时间
                msg_battery_batterystatus battery_batterystatus = (msg_battery_batterystatus) packet.unpack();
                //字段：time_total-----满电估计飞行总时长
                //字段：time_remaining-----剩余电量飞行时长
                AppController.getInstance().postRefreshUavTimeBattery(battery_batterystatus.time_remaining, battery_batterystatus.time_total);
                break;
            case msg_command_progress.MAVLINK_MSG_ID_COMMAND_PROGRESS:
                //备飞进度
                // 字段：command-----进度对应的协议代码
                //字段：step_total-----指令总步数
                //字段：step_complete-----当前已完成步骤
                msg_command_progress msgCommandProgress = (msg_command_progress) packet.unpack();
                AppController.getInstance().postRefreshUavReadyFlyProgress(msgCommandProgress.command, msgCommandProgress.step_complete, msgCommandProgress.step_total);
                break;
            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS://无人机电量提示
                //协议说明：HiStation只实现了其中关于电池状态的部分，其中voltage_battery为电池电压，
                //单位mV，current_battery为电池工作电流，单位mA，battery_remaining为电池剩余电量百分比。
                msg_sys_status msgSysStatus = (msg_sys_status) packet.unpack();
                //KLog.e(TAG, "receive_msg = " + msgSysStatus.toString());
                AppController.getInstance().postRefreshUavBattery(msgSysStatus.battery_remaining);
                break;
            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT://无人机全局位置
                //该协议向平台上报无人机的经纬度、高度、航向及速度信息。
                msg_global_position_int msgGlobalPositionInt = (msg_global_position_int) packet.unpack();
                int d = msgGlobalPositionInt.distance_to_home / 100; //单位cm
                int h = msgGlobalPositionInt.relative_alt / 1000;
                //开根号，然后除以1000
                float hs = (float) (Math.sqrt((msgGlobalPositionInt.vx * msgGlobalPositionInt.vx + msgGlobalPositionInt.vy * msgGlobalPositionInt.vy)) / 100f);
                float vs = msgGlobalPositionInt.vz / 100f;
                AppController.getInstance().postRefreshFlyData(d, h, hs, vs);
                //航向
                int hdg = msgGlobalPositionInt.hdg / 100;

                //刷新飞机所在位置：
                msg_gps_raw_int msgGpsRawInt = new msg_gps_raw_int();
                msgGpsRawInt.lat = msgGlobalPositionInt.lat;
                msgGpsRawInt.lon = msgGlobalPositionInt.lon;
                AppController.getInstance().postRefreshCurrentMapMarker(msgGpsRawInt, hdg);

                break;
            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT://GPS状态协议
                //协议说明：该条协议用于上报GPS定位状态，包括经纬度、高度、定位状态。
                msg_gps_raw_int msGpsRawInt = (msg_gps_raw_int) packet.unpack();

                AppController.getInstance().postShowGpsNumView(msGpsRawInt.satellites_visible);
                float lat = msGpsRawInt.lat / 10000000f;
                float lon = msGpsRawInt.lon / 10000000f;
                if (TextUtils.isEmpty(Injection.aasDataRe().getCurrentUAVPoint(Injection.aasDataRe().getUserName()))) {
                    Map<String, Double> map = GCJ02ToWGS84Util.transform(lon, lat);
                    KLog.e("设置当前飞机所在位置：" + map.get("lat").floatValue() + ",," + map.get("lon").floatValue());
                    Injection.aasDataRe().saveCurrentUAVPoint(Injection.aasDataRe().getUserName(),
                            map.get("lat").floatValue() + "#" + map.get("lon").floatValue());
                }
                break;
            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT://系统提示信息
                //协议说明：该协议用来向外发布系统提示信息，text是提示的文本内容，severity为提示等级，
                //等级的定义件枚举变量MAV_SEVERITY:
                msg_statustext msgStatustext = (msg_statustext) packet.unpack();
                AppController.getInstance().postShowSystemTips(msgStatustext.severity, msgStatustext.getText());
                break;
            case msg_chargepad_status.MAVLINK_MSG_ID_CHARGEPAD_STATUS:
                //充电板操作默认状态
                //字段：bar_position-----归中推杆位置（取值见常数CHARGE_PAD_BAR_POSITION）
                //字段：charge_status-----充电状态（取值见常数CHARGE_PAD_CHARGE_STATUS）
                //字段：canopy_status-----防雨棚状态（取值见常数CANOPY_STATUS）
                msg_chargepad_status msgChargepadStatus = (msg_chargepad_status) packet.unpack();
                KLog.e(TAG, "充电板默认状态：归中杆：" + msgChargepadStatus.bar_position + ",,充电状态：" + msgChargepadStatus.charge_status + ",,防雨棚状态：" + msgChargepadStatus.canopy_status);
                //记录防雨棚状态
                SPUtils.getInstance().put(Injection.aasDataRe().getUserName() + "_isCanopyLock", msgChargepadStatus.canopy_status == 1 ? 1 : 0);
                //记录归中杆状态
                SPUtils.getInstance().put(Injection.aasDataRe().getUserName() + "_isLock", msgChargepadStatus.bar_position == 1 ? 0 : 1);
                //记录充电状态
                SPUtils.getInstance().put(Injection.aasDataRe().getUserName() + "_isOpenBattery", msgChargepadStatus.charge_status == 0 ? 0 : 1);
                AppController.getInstance().postRefreshMenuStatus();
                break;
            case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT:
                //下载飞机航线-获取当前航线航点个数
                msg_mission_count msgMissionList = (msg_mission_count) packet.unpack();
                KLog.e(TAG, "当前航线下载个数：" + msgMissionList.count);
                pointCount = msgMissionList.count;
                if (pointCount > 0) {
                    currentPointIndex = 0;
                    msg_mission_request msgMissionRequest = new msg_mission_request();
                    msgMissionRequest.seq = currentPointIndex;
                    publish(msgMissionRequest.pack().encodePacket());
                } else {
                    ToastUtils.showLong("没有找到飞行航线，您需要重新设置上传飞行航线");
                }
                break;
            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
                //获取航点对应额经纬度信息
                msg_mission_item msgMissionItem = (msg_mission_item) packet.unpack();
                KLog.e(TAG, "当前航点信息：seq = " + msgMissionItem.seq + ",," + msgMissionItem.x + ",,," + msgMissionItem.y + ",,," + msgMissionItem.z);
                Map<String, Double> map = GCJ02ToWGS84Util.transform(msgMissionItem.y, msgMissionItem.x);

                PointInfo pointInfo = new PointInfo();
                pointInfo.setPointID(msgMissionItem.seq + 1);
                pointInfo.setLat(map.get("lat").floatValue());//纬度
                pointInfo.setLag(map.get("lon").floatValue());//经度
                pointInfo.setAltitude((int) msgMissionItem.z);//高度
                pointInfos.add(pointInfo);

                currentPointIndex++;
                if (msgMissionItem.seq < pointCount) {
                    msg_mission_request msgMissionRequest1 = new msg_mission_request();
                    msgMissionRequest1.seq = currentPointIndex;
                    publish(msgMissionRequest1.pack().encodePacket());
                    if (msgMissionItem.seq == pointCount - 1) {
                        KLog.e(TAG, "当前航线已经下载完成");
                        String uid = UUID.randomUUID().toString().replace("-", "");
                        MapAirLineInfo mapAirLineInfo = new MapAirLineInfo();
                        mapAirLineInfo.setPreImagePath("");
                        mapAirLineInfo.setUUID(uid);
                        mapAirLineInfo.setPinCode(Injection.aasDataRe().getUserName());
                        mapAirLineInfo.setCreateTime(dateFormat.format(new Date()));
                        mapAirLineInfo.setPointsJson(new Gson().toJson(pointInfos));
                        mapAirLineInfoDaoUtils.insert(mapAirLineInfo);
                        pointCount = 0;
                        currentPointIndex = 0;
                        pointInfos.clear();
                        pointInfos = new ArrayList<>();
                        //刷新地图UI
                        Injection.aasDataRe().saveLastMapLineId(Injection.aasDataRe().getUserName(), mapAirLineInfo.getUUID());
                        AppController.getInstance().postRefreshLastAirLine(mapAirLineInfo);
                    }
                }
                break;
            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                //信号塔强度rssi
                msg_radio_status msgRadioStatus = (msg_radio_status) packet.unpack();
                AppController.getInstance().postCurrentSignerStrong(msgRadioStatus.rssi);
                break;
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK://控制指令响应协议
                //协议说明：HiStation对每一条2.1中的控制指令都会有发布响应，其中command字段标识所响应的指令代码，
                //result为响应的结果，其含义见枚举变量MAV_RESULT:
                msg_command_ack msgCommandAck = (msg_command_ack) packet.unpack();
                switch (msgCommandAck.command) {
                    case MAV_CMD.MAV_CMD_SET_CAMERA_MODE:
                        //设置相机模式指令返回
                        KLog.e(TAG, "相机模式设置成功");
                        AppController.getInstance().postTakeCameraPic();
                        break;
                    case MAV_CMD.MAV_CMD_SET_CAMERA_ZOOM:
                        KLog.e(TAG, "相机Zoom设置成功");
                        break;
                    case MAV_CMD.MAV_CMD_REQUEST_CAMERA_IMAGE_CAPTURE:
                        KLog.e(TAG, "拍照指令发送成功");
                        break;
                    case MAV_CMD.MAV_CMD_VIDEO_START_CAPTURE:
                        KLog.e(TAG, "开始录像指令发送成功");
                        break;
                    case MAV_CMD.MAV_CMD_VIDEO_STOP_CAPTURE:
                        KLog.e(TAG, "停止录像指令发送成功");
                        break;
                    case MAV_CMD.MAV_CMD_FLIGHT_PREPARE:
                        if (msgCommandAck.result == MAV_RESULT.MAV_RESULT_FAILED) {
                            KLog.e(TAG, "备飞准备失败");
                        } else if (msgCommandAck.result == MAV_RESULT.MAV_RESULT_ACCEPTED) {
                            KLog.e(TAG, "备飞准备接收成功");
                        } else if (msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS) {
                            KLog.e(TAG, "备飞成功");
                        }
                        break;
                    case MAV_CMD.MAV_CMD_PAD_TURN_OFF_DRONE:
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_TURN_OFF_DRONE, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_TURN_ON_DRONE:
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_TURN_ON_DRONE, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_TURN_OFF_RC:
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_TURN_OFF_RC, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_TURN_ON_RC:
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_TURN_ON_RC, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_UNLOCK:
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_UNLOCK, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_LOCK:
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_LOCK, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_CANOPY_CLOSE:
                        //防雨棚关闭
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_CANOPY_CLOSE, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_CANOPY_OPEN:
                        //防雨棚开启
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_CANOPY_OPEN, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_ONE_KEY_TO_CHARGE:
                        //无人机开启充电
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_ONE_KEY_TO_CHARGE, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_PAD_TURN_OFF_CHARGE:
                        //无人机关闭充电
                        AppController.getInstance().postRefreshMenuStatus(MAV_CMD.MAV_CMD_PAD_TURN_OFF_CHARGE, msgCommandAck.result == MAV_RESULT.MAV_RESULT_SUCCESS);
                        break;
                    case MAV_CMD.MAV_CMD_VIDEO_STREAMING_REQUEST:
                        //视频流切换
                        break;
                }
                break;
            case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK://点个航点上传确认
                //协议说明：其中type保持为0。
                msg_mission_ack msgMissionAck = (msg_mission_ack) packet.unpack();
                KLog.e(TAG, "receive_msg msg_mission_ack = " + msgMissionAck.toString());
                AppController.getInstance().postCurrentAirLinePoint(msgMissionAck.type, true);
                break;
            case msg_camera_image_captured.MAVLINK_MSG_ID_CAMERA_IMAGE_CAPTURED:
                msg_camera_image_captured cameraMsg = (msg_camera_image_captured) packet.unpack();
                break;
            case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST:
                msg_mission_request msg_mission_request = (msg_mission_request) packet.unpack();
                KLog.e(TAG, "receive_msg msg_mission_request = " + msg_mission_request.toString());
                AppController.getInstance().postCurrentAirLinePoint(msg_mission_request.seq, false);
                break;
            case msg_mediafile_count.MAVLINK_MSG_ID_MEDIAFILE_COUNT:
                //获取文件列表数量
                msg_mediafile_count msg_mediafile_count = (msg_mediafile_count) packet.unpack();
                KLog.e(TAG, "文件总数：" + msg_mediafile_count.count);
                AppController.getInstance().postFileCount(msg_mediafile_count.count);
                break;
            case msg_mediafile_information.MAVLINK_MSG_ID_MEDIAFILE_INFORMATION:
                //单个文件信息
                currentMediaFile = (msg_mediafile_information) packet.unpack();
                KLog.e(TAG, "单个文件信息：" + currentMediaFile.toString());
                currentFileBytes = new byte[(int) currentMediaFile.file_size];
                AppController.getInstance().postSaveFileLocal(currentMediaFile);
                break;
            case msg_mediafile_data_segment.MAVLINK_MSG_ID_MEDIAFILE_DATA_SEGMENT:
                //文件分包流
                msg_mediafile_data_segment msg_mediafile_data_segment = (msg_mediafile_data_segment) packet.unpack();
                if (currentMediaFile != null && currentMediaFile.file_type == MEDIATYPE.JPEG) {
                    //当前获取的文件为图片文件
                    mediaFileDataSegmentList.add(msg_mediafile_data_segment);
                    if (msg_mediafile_data_segment.seq == msg_mediafile_data_segment.total - 1) {
                        int length = 0;
                        for (msg_mediafile_data_segment msgMediafileDataSegment : mediaFileDataSegmentList) {
                            System.arraycopy(msgMediafileDataSegment.data, 0, currentFileBytes, length, msgMediafileDataSegment.len);
                            length += msgMediafileDataSegment.len;
                        }
                        //生产图片
                        Bitmap bitmap = convertByteToBitmap(currentFileBytes);
                        String filePath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/DNest_Air_Point_" + new String(currentMediaFile.getFile_Name()).split(".jpg")[0] + ".jpg";
                        saveBitmap(bitmap, filePath, new String(currentMediaFile.file_name).split(".jpg")[0] + ".jpg");
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initHandler() {
        if (null == mHandlerThread) {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
        }
        if (null == mHandler) {
            mHandler = new Handler(mHandlerThread.getLooper());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void releaseHandler() {
        if (null != mHandler) {
            synchronized (sync) {
                if (null != mHandler) {
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler = null;
                }
            }
        }
        if (null != mHandlerThread) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            return true;
        } else {
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler().postDelayed(() -> doClientConnection(), 3000);
            return false;
        }
    }

    /****
     * 链接断开前发布链接遗言
     */
    private boolean sendLastLiveMsg() {
        try {
            //String message = "{\"terminal_uid\":\"" + Contact.getAndroidID() + "\"}";
            //mMqttConnectOptions.setWill(LAST_WILL_PANEL_SATAUS, message.getBytes(), MQTT_QOS_HIGH, MQTT_RETAINED);
            //mMqttConnectOptions.setWill(LAST_WILL_PANEL_CHECK, message.getBytes(), MQTT_QOS_HIGH, MQTT_RETAINED);
        } catch (Exception e) {
            Log.e(TAG, "Exception Occured Last Live Msg ", e);
            iMqttActionListener.onFailure(null, e);
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onDestroy() {
        Log.d(TAG, "service onDestroy");
        if (isChecking) {
            stopChecking();
        }
        releaseHandler();
        try {
            currentIndex = 0;
            currentFrameVideoList = new ArrayList<>();
            mqttAndroidClient.disconnect(); //断开连接
            mqttAndroidClient.unregisterResources();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
