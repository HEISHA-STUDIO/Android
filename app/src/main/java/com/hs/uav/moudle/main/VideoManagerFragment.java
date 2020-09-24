package com.hs.uav.moudle.main;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.MAVLink.DLink.msg_command_int;
import com.MAVLink.DLink.msg_manual_control;
import com.MAVLink.enums.CAMERA_MODE;
import com.MAVLink.enums.MAV_CMD;
import com.hs.uav.MainActivity;
import com.hs.uav.R;
import com.hs.uav.common.app.AppController;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.common.app.MyApplication;
import com.hs.uav.common.view.RockerView;
import com.hs.uav.databinding.FragmentVideoLayoutBinding;
import com.hs.uav.logic.listener.CommandAckLinstener;
import com.hs.uav.logic.listener.OnRefreshFlyDataLinstener;
import com.hs.uav.logic.mqtt.MqttService;
import com.hs.uav.moudle.main.model.VideoMainVM;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import me.goldze.mvvmhabit.BR;
import me.goldze.mvvmhabit.base.BaseFragment;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/***
 * 摄像头视频预览画面Fragment
 * @author tony.liu
 */
public class VideoManagerFragment extends BaseFragment<FragmentVideoLayoutBinding, VideoMainVM>
        implements Handler.Callback, View.OnClickListener, CommandAckLinstener, OnRefreshFlyDataLinstener {
    public final static int TAKE_PHOTO = 1;//拍照
    public final static int TAKE_VIDEO = 2;//录像

    public final static int MODEL_IR_CAMERA = 1;//红外摄像头
    public final static int MODEL_HD_CAMERA = 2;//高清夜景摄像头
    public static int ZOOM = 1;//当前相机缩放级别
    private SurfaceView mSurfaceView;
    private Handler mHandler;
    private MediaCodec mCodec;
    private MediaFormat mediaFormat;
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private static boolean isFrame = true;
    private static int currentCameraMode = TAKE_PHOTO;//当前默认拍照
    private static boolean isStartVideo = false; //是否开始录像
    private static int cameraModel = MODEL_HD_CAMERA;//默认高清摄像头
    private int X, Y;

    @Override
    public VideoMainVM initViewModel() {
        mHandler = new Handler(this);
        MqttService.setHandler(mHandler);
        AppController.getInstance().regOnRefreshFlyDataLinstener(this);
        AppController.getInstance().regCommandAckLinstener(this);
        AppViewModelFactory factory = AppViewModelFactory.getInstance(MyApplication.getInstance());
        return ViewModelProviders.of(this, factory).get(VideoMainVM.class);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_video_layout;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        mSurfaceView = binding.surfaceView;
        initSurfaceVideoView();
        binding.uavReadyIv.setOnClickListener(this);
        binding.uavFlyIv.setOnClickListener(this);
        binding.targetMediaSourceIv.setOnClickListener(this);
        binding.stopOrOpenUavIv.setOnClickListener(this);

        binding.rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_MOVE);
        binding.rockerView.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {

            }

            @Override
            public void callBackXY(int pointX, int pointY, int x, int y) {
                int tempX = x - pointX;
                int tempY = pointY - y;
                //左右上下间距75
                float scaleX = tempX / 75f;
                float scaleY = tempY / 75f;
                X = (int) (scaleX * 1000);
                Y = (int) (scaleY * 1000);
            }

            @Override
            public void onFinish() {
                CameraYunControl(X, Y);
            }
        });
        hideMenuView(MainActivity.SMALL_MODEL == 1?false:true);
        setNormalUAVMenuStatus();
    }

    @Override
    public void initData() {
        super.initData();
    }

    public void hideMenuView(boolean flag) {
        if (binding == null) {
            return;
        }
        binding.toggerIv.setVisibility(flag?View.GONE: View.VISIBLE);
        binding.airFlightDataLl.setVisibility(flag ? View.VISIBLE : View.GONE);
        binding.airFlyedContralLl.setVisibility(flag ? View.VISIBLE : View.GONE);

        boolean enable = SPUtils.getInstance().getBoolean(MainActivity.CUR_PIN_CODE + "_flyResult");
        if (!enable) {//无人机未起飞
            binding.cameraControllerLl.setVisibility(View.GONE);
            binding.takeGalleryRl.setVisibility(View.GONE);
        } else {//无人机已起飞
            binding.cameraControllerLl.setVisibility(View.VISIBLE);
            binding.takeGalleryRl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isFrame = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFrame = false;
        stop();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void initSurfaceVideoView() {
        try {
            //创建解码器
            mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, 1920, 1080);
        //设置帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    isFrame = true;
                    mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(), null, 0);
                    mCodec.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                stop();
            }
        });

        airManagerEventView();
        cameraManagerView();
    }

    public void stop() {
        if (mCodec != null) {
            try {
                mCodec.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void airManagerEventView() {
        binding.flashlightSeekbar.SetValue(1);
        binding.flashlightSeekbar.setProgress(1);
        binding.flashlightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    i = 1;
                }
                ZOOM = i;
                binding.flashlightSeekbar.SetValue(ZOOM);
                binding.cameraZoomTv.setText(ZOOM + "X");
                setCameraZoom(ZOOM);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.addTv.setOnClickListener(view -> {
            if (ZOOM < 6) {
                ZOOM++;
                binding.flashlightSeekbar.setProgress(ZOOM);
                binding.flashlightSeekbar.SetValue(ZOOM);
                binding.cameraZoomTv.setText(ZOOM + "X");
                setCameraZoom(ZOOM);
            }
        });
        binding.substrctTv.setOnClickListener(view -> {
            if (ZOOM > 1) {
                ZOOM--;
                binding.flashlightSeekbar.setProgress(ZOOM);
                binding.flashlightSeekbar.SetValue(ZOOM);
                binding.cameraZoomTv.setText(ZOOM + "X");
                setCameraZoom(ZOOM);
            }
        });
    }

    /***
     * 相机控制相关视图及控件
     */
    private void cameraManagerView() {
        binding.switchCameraIv.setOnClickListener(this);
        binding.takeCameraIv.setOnClickListener(this);
        binding.cameraModeIv.setOnClickListener(this);
        //归中杆控制-释放，收紧
        binding.barLockIv.setOnClickListener(this);
        //充电开关
        binding.chargeOnIv.setOnClickListener(this);
        //防雨棚控制-开启关闭
        binding.canopyCloseIv.setOnClickListener(this);
        //遥控关闭,遥控开启
        binding.rcOffIv.setOnClickListener(this);
        //无人机关机开机
        binding.droneOffIv.setOnClickListener(this);
    }

    public void showAirController(int from) {
        if (from == 0) {
            binding.airFlyedContralLl.setVisibility(View.GONE);
            binding.airReadyContralLl.setVisibility(View.VISIBLE);
            binding.takeGalleryRl.setVisibility(View.GONE);
            binding.airFlightDataLl.setVisibility(View.GONE);
            binding.cameraControllerLl.setVisibility(View.GONE);
        } else if (from == 1) {
            binding.airReadyContralLl.setVisibility(View.GONE);
            binding.airFlyedContralLl.setVisibility(View.VISIBLE);
            binding.takeGalleryRl.setVisibility(View.VISIBLE);
            binding.airFlightDataLl.setVisibility(View.VISIBLE);
            binding.cameraControllerLl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case 1000:
                if (!isFrame) {
                    return false;
                }
                Bundle bundle = message.getData();
                long pts = bundle.getLong("pts");
                byte[] buffer = bundle.getByteArray("videoSource");
                onFrame(buffer, 0, buffer.length, pts);
                break;
        }
        return false;
    }

    public void onFrame(byte[] buf, int offset, int length, long pts) {
        try {
            ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
            int inputBufferIndex = mCodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buf, offset, length);
                mCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.position(), pts, 0);
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
            while (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uav_ready_iv:
                //飞机准备，指令发送后，服务器端返回备飞进度
                if (MainActivity.AIRLINE) {
                    targetUavCmd(MAV_CMD.MAV_CMD_FLIGHT_PREPARE);
                } else {
                    ToastUtils.showLong("请先设置并上传飞行航线");
                }
                break;
            case R.id.uav_fly_iv:
                if (SPUtils.getInstance().getBoolean(MainActivity.CUR_PIN_CODE + "_flyResult")){
                    //起飞状态下可以返航
                    targetUavCmd(MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH);
                }else{
                    //飞机起飞
                    targetUavCmd(MAV_CMD.MAV_CMD_NAV_TAKEOFF);
                }
                break;
            case R.id.stop_or_open_uav_iv:
                //飞机暂停或继续
                targetUavCmd(MAV_CMD.MAV_CMD_DO_PAUSE_CONTINUE);
                break;
            case R.id.target_media_source_iv:
                //去往相册
                startActivity(MediaManagerActivity.class);
                break;
            case R.id.bar_lock_iv:
                if (SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isLock") == 0) {
                    //默认归中杆收紧，现在开始释放
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_UNLOCK);
                } else {
                    //归中杆控制-收紧
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_LOCK);
                }
                binding.barLockIv.setEnabled(false);
                break;
            case R.id.charge_on_iv:
                if (SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isOpenBattery") == 0) {
                    //无人机开启充电
                    targetUavCmd(MAV_CMD.MAV_CMD_ONE_KEY_TO_CHARGE);
                } else {
                    //无人机关闭充电
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_TURN_OFF_CHARGE);
                }
                binding.chargeOnIv.setEnabled(false);
                break;
            case R.id.canopy_close_iv:
                if (SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isCanopyLock") == 0) {
                    //防雨棚控制-开启
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_CANOPY_OPEN);
                } else {
                    //防雨棚控制-关闭
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_CANOPY_CLOSE);
                }
                binding.canopyCloseIv.setEnabled(false);
                break;
            case R.id.rc_off_iv:
                if (SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isRcLock") == 0) {
                    //无人机遥控开启
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_TURN_ON_RC);
                } else {
                    //无人机遥控关闭
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_TURN_OFF_RC);
                }
                binding.rcOffIv.setEnabled(false);
                break;
            case R.id.drone_off_iv:
                if (SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isUavLock") == 0) {
                    //无人机开机
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_TURN_ON_DRONE);
                } else {
                    //无人机关机
                    targetUavCmd(MAV_CMD.MAV_CMD_PAD_TURN_OFF_DRONE);
                }
                binding.droneOffIv.setEnabled(false);
                break;
            case R.id.switch_camera_iv:
                //选择拍照或者是录像
                if (currentCameraMode == TAKE_PHOTO) {
                    //当前是拍照，进入录像模式
                    currentCameraMode = TAKE_VIDEO;
                    binding.switchCameraIv.setImageResource(R.mipmap.icon_switch_photo);
                    binding.takeCameraIv.setImageResource(R.mipmap.btn_record_start);
                } else {
                    currentCameraMode = TAKE_PHOTO;
                    binding.switchCameraIv.setImageResource(R.mipmap.icon_switch_cam);
                    binding.takeCameraIv.setImageResource(R.mipmap.btn_photo_shoot);
                }
                break;
            case R.id.take_camera_iv:
                //拍照和录像的确认操作
                if (currentCameraMode == TAKE_PHOTO) {
                    //当前是拍照,发送拍照指令
                    setCameraMode();
                } else {
                    //当前是录像，发布录像指令
                    if (isStartVideo) {
                        isStartVideo = false;
                        //停止录像，并上报指令
                        binding.takeCameraIv.setImageResource(R.mipmap.btn_record_start);
                        targetUavCmd(MAV_CMD.MAV_CMD_VIDEO_STOP_CAPTURE);
                    } else {
                        //开始录像，并上报指令
                        setCameraMode();
                        isStartVideo = true;
                        binding.takeCameraIv.setImageResource(R.mipmap.btn_record_stop);
                    }
                }
                break;
            case R.id.camera_mode_iv:
                //摄像头模式切换，红外或者夜景摄像头
                if (cameraModel == MODEL_HD_CAMERA) {
                    //当前为高清摄像头，点击切换为红外，并发送指令
                    cameraModel = MODEL_IR_CAMERA;
                    binding.cameraModeIv.setImageResource(R.mipmap.icon_ir);
                } else {
                    //当前为红外摄像头，点击切换高清，并发送指令
                    cameraModel = MODEL_HD_CAMERA;
                    binding.cameraModeIv.setImageResource(R.mipmap.icon_hd);
                }
                break;
        }
    }

    /***
     * 设置相机模式
     * command为MAV_CMD_SET_CAMERA_MODE(530)，param2为相机模式，其余参数为0
     */
    private void setCameraMode() {
        msg_command_int msg_command_int = new msg_command_int();
        msg_command_int.command = MAV_CMD.MAV_CMD_SET_CAMERA_MODE;
        msg_command_int.param2 = CAMERA_MODE.CAMERA_MODE_IMAGE;
        MqttService.publish(msg_command_int.pack().encodePacket());
    }

    /***
     * 设置zoom级别
     * @param zoom param2为Zoom值（0~100），
     */
    private void setCameraZoom(int zoom) {
        msg_command_int msg_command_int = new msg_command_int();
        msg_command_int.command = MAV_CMD.MAV_CMD_SET_CAMERA_ZOOM;
        msg_command_int.param2 = zoom;
        MqttService.publish(msg_command_int.pack().encodePacket());
    }

    /***
     * 拍照 or 录像 or 停止录像
     * 无人机控制指令
     */
    private void targetUavCmd(int cmd) {
        msg_command_int msg_command_int = new msg_command_int();
        msg_command_int.command = cmd;
        MqttService.publish(msg_command_int.pack().encodePacket());
    }

    @Override
    public void takeCameraPic() {
        if (currentCameraMode == TAKE_PHOTO) {
            targetUavCmd(MAV_CMD.MAV_CMD_REQUEST_CAMERA_IMAGE_CAPTURE);
        } else {
            targetUavCmd(MAV_CMD.MAV_CMD_VIDEO_START_CAPTURE);
        }
    }

    @Override
    public void refreshMenuStatus(int ack, boolean isChange) {
        //刷新按钮状态
        switch (ack) {
            case MAV_CMD.MAV_CMD_PAD_UNLOCK:
                //归中杆开启结果
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isLock", isChange ? 1 : 0);
                binding.barLockIv.setEnabled(true);
                binding.barLockIv.setImageResource(isChange ? R.mipmap.icon_barlock : R.mipmap.icon_bar_unlock);
                break;
            case MAV_CMD.MAV_CMD_PAD_LOCK:
                //归中杆收紧结果
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isLock", isChange ? 0 : 1);
                binding.barLockIv.setEnabled(true);
                binding.barLockIv.setImageResource(isChange ? R.mipmap.icon_bar_unlock : R.mipmap.icon_barlock);
                break;
            case MAV_CMD.MAV_CMD_ONE_KEY_TO_CHARGE:
                //无人机开启充电
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isOpenBattery", isChange ? 1 : 0);
                binding.chargeOnIv.setEnabled(true);
                binding.chargeOnIv.setImageResource(isChange ? R.mipmap.icon_charge_off : R.mipmap.icon_charge_on);
                break;
            case MAV_CMD.MAV_CMD_PAD_TURN_OFF_CHARGE:
                //无人机关闭充电
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isOpenBattery", isChange ? 0 : 1);
                binding.chargeOnIv.setEnabled(true);
                binding.chargeOnIv.setImageResource(isChange ? R.mipmap.icon_charge_on : R.mipmap.icon_charge_off);
                break;
            case MAV_CMD.MAV_CMD_PAD_CANOPY_CLOSE:
                //防雨棚关闭
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isCanopyLock", isChange ? 0 : 1);
                binding.canopyCloseIv.setEnabled(true);
                binding.canopyCloseIv.setImageResource(isChange ? R.mipmap.icon_canopy_open : R.mipmap.icon_canopy_close);
                break;
            case MAV_CMD.MAV_CMD_PAD_CANOPY_OPEN:
                //防雨棚开启
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isCanopyLock", isChange ? 1 : 0);
                binding.canopyCloseIv.setEnabled(true);
                binding.canopyCloseIv.setImageResource(isChange ? R.mipmap.icon_canopy_close : R.mipmap.icon_canopy_open);
                break;
            case MAV_CMD.MAV_CMD_PAD_TURN_OFF_RC:
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isRcLock", isChange ? 0 : 1);
                binding.rcOffIv.setEnabled(true);
                binding.rcOffIv.setImageResource(isChange ? R.mipmap.icon_rc_on : R.mipmap.icon_rc_off);
                break;
            case MAV_CMD.MAV_CMD_PAD_TURN_ON_RC:
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isRcLock", isChange ? 1 : 0);
                binding.rcOffIv.setEnabled(true);
                binding.rcOffIv.setImageResource(isChange ? R.mipmap.icon_rc_off : R.mipmap.icon_rc_on);
                break;
            case MAV_CMD.MAV_CMD_PAD_TURN_OFF_DRONE:
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isUavLock", isChange ? 0 : 1);
                binding.droneOffIv.setEnabled(true);
                binding.droneOffIv.setImageResource(isChange ? R.mipmap.icon_drone_on : R.mipmap.icon_drone_off);
                break;
            case MAV_CMD.MAV_CMD_PAD_TURN_ON_DRONE:
                SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_isUavLock", isChange ? 1 : 0);
                binding.droneOffIv.setEnabled(true);
                binding.droneOffIv.setImageResource(isChange ? R.mipmap.icon_drone_off : R.mipmap.icon_drone_on);
                break;
        }
    }

    /***
     * 设置充电板最后的操作状态
     */
    public void setNormalUAVMenuStatus() {
        binding.barLockIv.setImageResource(SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isLock") == 0 ? R.mipmap.icon_barlock : R.mipmap.icon_bar_unlock);
        int batteryStatus = SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isOpenBattery");
        binding.chargeOnIv.setImageResource(batteryStatus == 0 ? R.mipmap.icon_charge_off : R.mipmap.icon_charge_on);
        int canopyStatus = SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isCanopyLock");
        binding.canopyCloseIv.setImageResource(canopyStatus == 0 ? R.mipmap.icon_canopy_close : R.mipmap.icon_canopy_open);
        int rcStatus = SPUtils.getInstance().getInt(MainActivity.CUR_PIN_CODE + "_isRcLock");
        binding.rcOffIv.setImageResource(rcStatus == 0 ? R.mipmap.icon_rc_off : R.mipmap.icon_rc_on);
    }

    /***
     * 云台控制指令
     */
    public void CameraYunControl(int x, int y) {
        msg_manual_control msg_manual_control = new msg_manual_control();
        msg_manual_control.x = (short) x;
        msg_manual_control.y = (short) y;
        MqttService.publish(msg_manual_control.pack().encodePacket());
    }

    public DecimalFormat decimalFormat = new DecimalFormat("0.0");

    @Override
    public void refreshFlyData(int d, int h, float hs, float vs) {
        binding.dSpaceTv.setText(String.valueOf(d));
        binding.hSpaceTv.setText(String.valueOf(h));
        binding.hsSpaceTv.setText(decimalFormat.format(hs));
        binding.vsSpaceTv.setText(decimalFormat.format(-vs));
    }

    @Override
    public void readyFlyProgress(int command, int step, int total) {
        if (total > 0) {
            binding.readyProgress.setVisibility(View.VISIBLE);
        }
        //备飞进度
        binding.readyProgress.setMaxCount(total);
        binding.readyProgress.setCurrentCount(step);
        //实时更新进度
        if (binding.readyProgress.getCurrentCount() >= total) {
            if (getActivity() != null && !getActivity().isFinishing()) {
                getActivity().runOnUiThread(() -> {
                    binding.readyProgress.setVisibility(View.INVISIBLE);
                });
            }
        }
        binding.readyProgress.setCurrentCount(binding.readyProgress.getCurrentCount() + 1);
    }

    @Override
    public void refreshMenuStatus() {
        setNormalUAVMenuStatus();
    }

    /***
     * 无人机备飞状态
     * @param enable
     */
    public void setUavFlyReadyStatus(boolean enable) {
        binding.uavReadyIv.setAlpha(enable ? 1f : 0.5f);
        binding.uavFlyIv.setAlpha(enable ? 0.5f : 1f);
        binding.uavReadyIv.setEnabled(enable ? true : false);
        binding.uavFlyIv.setEnabled(enable ? false : true);
    }

    /***
     * 无人机起飞情况
     * @param enable true 代表起飞失败，false 代表起飞成功
     */
    public void setUavFlyStatus(boolean enable) {
        SPUtils.getInstance().put(MainActivity.CUR_PIN_CODE + "_flyResult", !enable);
        binding.readyRl.setVisibility(enable ? View.VISIBLE : View.GONE);
        binding.spaceV.setVisibility(enable ? View.VISIBLE : View.GONE);
        binding.uavFlyIv.setImageResource(enable ? R.mipmap.icon_takeoff : R.mipmap.icon_land);
        binding.stopOrOpenUavIv.setVisibility(enable ? View.GONE : View.VISIBLE);
        if (enable) {//无人机未起飞
            binding.cameraControllerLl.setVisibility(View.GONE);
            binding.takeGalleryRl.setVisibility(View.GONE);
        } else {//无人机已起飞
            //切换至无人机摄像头画面
            SPUtils.getInstance().put("video_type", 2);
            msg_command_int msg_command_int = new msg_command_int();
            msg_command_int.command = MAV_CMD.MAV_CMD_VIDEO_STREAMING_REQUEST;
            msg_command_int.param1 = 2;
            MqttService.publish(msg_command_int.pack().encodePacket());

            //改变顶部操作栏摄像头按钮为停机坪监控摄像头

            binding.cameraControllerLl.setVisibility(View.VISIBLE);
            binding.takeGalleryRl.setVisibility(View.VISIBLE);
        }
    }

    /***
     * 无人机暂停状态
     * @param enable
     */
    public void setUavPauseStatus(boolean enable) {
        binding.stopOrOpenUavIv.setImageResource(enable ? R.mipmap.icon_flight_pause : R.mipmap.icon_flight_continue);
    }
}
