package com.hs.uav.common.app;

import android.os.Handler;
import android.os.Looper;

import com.MAVLink.DLink.msg_gps_raw_int;
import com.MAVLink.DLink.msg_mediafile_information;
import com.hs.uav.common.entity.MapAirLineInfo;
import com.hs.uav.logic.listener.ChangerSmallWindowSourceLinstener;
import com.hs.uav.logic.listener.CommandAckLinstener;
import com.hs.uav.logic.listener.DownloadFileLinster;
import com.hs.uav.logic.listener.OnRefreshFlyDataLinstener;
import com.hs.uav.logic.listener.OnRefreshLastAirLineListener;
import com.hs.uav.logic.listener.OnRefreshUavConfigLinstener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用实例整体控制器
 */
public class AppController {
    private static final String TAG = AppController.class.getCanonicalName();
    /**
     * Hanldr Looper
     */
    public static Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 存储所有页面Handler键值对
     * key:getClass().getCanonicalName()
     * value:页面Handler
     * !注意页面销毁时remove掉!
     */
    private Map<String, Handler> handlerMap = new HashMap<String, Handler>();
    private static AppController instance;

    private List<OnRefreshUavConfigLinstener> onRefreshUavConfigLinsteners = new ArrayList<>();
    private List<CommandAckLinstener> commandAckLinsteners = new ArrayList<>();
    private List<DownloadFileLinster> downloadFileLinsters = new ArrayList<>();
    private List<OnRefreshFlyDataLinstener> onRefreshFlyDataLinsteners = new ArrayList<>();
    private List<ChangerSmallWindowSourceLinstener> changerSmallWindowSourceLinsteners = new ArrayList<>();
    private List<OnRefreshLastAirLineListener> onRefreshLastAirLineListeners = new ArrayList<>();

    /**
     * Looper线程
     */
    private LooperThread looperThread;

    /**
     * 初始化项目用到的类
     */
    private AppController() {
        looperThread = new LooperThread();
        looperThread.setName("LooperThread");
        looperThread.start();
    }

    public static AppController getInstance() {
        synchronized (AppController.class) {
            if (null == instance) {
                instance = new AppController();
            }
        }
        return instance;
    }

    public Map<String, Handler> getHandlerMap() {
        return handlerMap;
    }

    /************Get方法******end******/
    /*** 单线程执行数据的刷新操作*/
    public class LooperThread extends Thread {
        /*** 线程内部handler*/
        public Handler handler;
        /*** 线程内部Looper*/
        public Looper looper;

        @Override
        public void run() {
            Looper.prepare();
            looper = Looper.myLooper();
            handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                }
            };
            Looper.loop();
        }
    }

    public void regOnRefreshUavConfigLinstener(OnRefreshUavConfigLinstener listener) {
        if (!onRefreshUavConfigLinsteners.contains(listener)) {
            onRefreshUavConfigLinsteners.add(listener);
        }
    }

    public void regChangerSmallWindowSourceLinstener(ChangerSmallWindowSourceLinstener listener) {
        if (!changerSmallWindowSourceLinsteners.contains(listener)) {
            changerSmallWindowSourceLinsteners.add(listener);
        }
    }

    public void regCommandAckLinstener(CommandAckLinstener listener) {
        if (!commandAckLinsteners.contains(listener)) {
            commandAckLinsteners.add(listener);
        }
    }

    public void regOnRefreshFlyDataLinstener(OnRefreshFlyDataLinstener listener) {
        if (!onRefreshFlyDataLinsteners.contains(listener)) {
            onRefreshFlyDataLinsteners.add(listener);
        }
    }

    public void regDownloadFileLinster(DownloadFileLinster listener) {
        if (!downloadFileLinsters.contains(listener)) {
            downloadFileLinsters.add(listener);
        }
    }

    public void regOnRefreshLastAirLineListener(OnRefreshLastAirLineListener listener) {
        if (!onRefreshLastAirLineListeners.contains(listener)) {
            onRefreshLastAirLineListeners.add(listener);
        }
    }

    public void postRefreshFlyData(final int d, final int h, final float hs, final float vs) {
        mMainHandler.post(() -> {
            for (OnRefreshFlyDataLinstener listener : onRefreshFlyDataLinsteners) {
                listener.refreshFlyData(d, h, hs, vs);
            }
        });
    }

    public void postRefreshMenuStatus() {
        mMainHandler.post(() -> {
            for (OnRefreshFlyDataLinstener listener : onRefreshFlyDataLinsteners) {
                listener.refreshMenuStatus();
            }
        });
    }

    public void postRefreshUavBattery(final int battery) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.refreshBattery(battery);
            }
        });
    }

    public void postRefreshUavTimeBattery(final int outTimeBattery, final int totalTimeBattery) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.refreshBatteryOutTime(outTimeBattery, totalTimeBattery);
            }
        });
    }

    public void postRefreshUavSignStatus(final int signStatus) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.refreshSignStatus(signStatus);
            }
        });
    }


    public void postRefreshUavReadyFlyProgress(final int command, final int step, final int total) {
        mMainHandler.post(() -> {
            for (OnRefreshFlyDataLinstener listener : onRefreshFlyDataLinsteners) {
                listener.readyFlyProgress(command, step, total);
            }
        });
    }


    public void postShowGpsNumView(final int satellites_visible) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.showGpsNumView(satellites_visible);
            }
        });
    }

    public void postShowSystemTips(final int MAV_SEVERITY, final String system_tips) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.currentSystemTips(MAV_SEVERITY, system_tips);
            }
        });
    }

    public void postCurrentSignerStrong(final int rssi) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.currentSignerStrong(rssi);
            }
        });
    }


    public void postCurrentAirLinePoint(final int seq, final boolean isFinish) {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.publishCurrentAirLinePoint(seq, isFinish);
            }
        });
    }


    public void postTakeCameraPic() {
        mMainHandler.post(() -> {
            for (CommandAckLinstener listener : commandAckLinsteners) {
                listener.takeCameraPic();
            }
        });
    }

    public void postChangeSmallWindow(final int smallWindow) {
        mMainHandler.post(() -> {
            for (ChangerSmallWindowSourceLinstener listener : changerSmallWindowSourceLinsteners) {
                listener.changeSmallWindow(smallWindow);
            }
        });
    }

    public void postChangeSmallWindowSize(final int oriLeft, final int oriRight, final int oriTop, final int oriBottom) {
        mMainHandler.post(() -> {
            for (ChangerSmallWindowSourceLinstener listener : changerSmallWindowSourceLinsteners) {
                listener.refreshSizeView(oriLeft,oriRight,oriTop,oriBottom);
            }
        });
    }

    public void postRefreshMapSmallImgView() {
        mMainHandler.post(() -> {
            for (OnRefreshUavConfigLinstener listener : onRefreshUavConfigLinsteners) {
                listener.refreshPointSmallImgView();
            }
        });
    }

    public void postRefreshMenuStatus(final int ack, final boolean isChange) {
        mMainHandler.post(() -> {
            for (CommandAckLinstener listener : commandAckLinsteners) {
                listener.refreshMenuStatus(ack, isChange);
            }
        });
    }

    public void postFileCount(final int count) {
        mMainHandler.post(() -> {
            for (DownloadFileLinster listener : downloadFileLinsters) {
                listener.getFileCount(count);
            }
        });
    }

    public void postSaveFileLocal(final msg_mediafile_information msg_mediafile_information) {
        mMainHandler.post(() -> {
            for (DownloadFileLinster listener : downloadFileLinsters) {
                listener.saveFileLocal(msg_mediafile_information);
            }
        });
    }

    public void postRefreshLastAirLine(final MapAirLineInfo mapAirLineInfo) {
        mMainHandler.post(() -> {
            for (OnRefreshLastAirLineListener listener : onRefreshLastAirLineListeners) {
                listener.refreshLastAirLine(mapAirLineInfo);
            }
        });
    }

    public void postRefreshCurrentMapMarker(final msg_gps_raw_int gpsRaw,final  int hdg) {
        mMainHandler.post(() -> {
            for (OnRefreshLastAirLineListener listener : onRefreshLastAirLineListeners) {
                listener.refreshCurrentMapMarker(gpsRaw,hdg);
            }
        });
    }

}
