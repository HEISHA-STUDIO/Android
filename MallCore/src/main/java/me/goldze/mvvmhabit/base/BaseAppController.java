package me.goldze.mvvmhabit.base;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.bus.LogOutRefreshListener;
import me.goldze.mvvmhabit.bus.LoginRefreshListener;

/**
 * 应用实例整体控制器
 */
public class BaseAppController {
    private static final String TAG = BaseAppController.class.getCanonicalName();
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
    private List<LogOutRefreshListener> logOutRefreshListeners = new ArrayList<>();
    private static BaseAppController instance;

    /*** Looper线程*/
    private LooperThread looperThread;

    /**
     * 初始化项目用到的类
     */
    private BaseAppController() {
        looperThread = new LooperThread();
        looperThread.setName("LooperThread");
        looperThread.start();
    }

    public static BaseAppController getInstance() {
        synchronized (BaseAppController.class) {
            if (null == instance) {
                instance = new BaseAppController();
            }
        }
        return instance;
    }
    public Map<String, Handler> getHandlerMap() {
        return handlerMap;
    }
    /**
     * 单线程执行数据的刷新操作
     */
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

    public void regLogOutRefreshListener(LogOutRefreshListener listener) {
        if (!logOutRefreshListeners.contains(listener)) {
            logOutRefreshListeners.add(listener);
        }
    }

    public void postLogOutRefreshListener() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (LogOutRefreshListener listener : logOutRefreshListeners) {
                    try {
                        listener.reLogin();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void postCancelDialogListener() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (LogOutRefreshListener listener : logOutRefreshListeners) {
                    listener.cancelDialog();
                }
            }
        });
    }
}
