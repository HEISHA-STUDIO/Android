package com.hs.uav.common.app;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.RequiresApi;

import com.hs.uav.MainActivity;
import com.hs.uav.R;
import com.hs.uav.common.view.scaleruler.utils.DrawUtil;
import com.hs.uav.logic.data.db.DaoManager;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.tencent.bugly.Bugly;

import me.goldze.mvvmhabit.BuildConfig;
import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.crash.CaocConfig;
import me.goldze.mvvmhabit.utils.KLog;

public class MyApplication extends BaseApplication {
    private final static String TAG = MyApplication.class.getSimpleName();
    private static MyApplication instance = null;

    public static MyApplication getInstance() {
        return instance;
    }

    private NetWorkStateReceiver netWorkStateReceiver;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        //是否开启打印日志
        KLog.init(BuildConfig.DEBUG);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        RxPaparazzo.register(this); // 图片选择
        DrawUtil.resetDensity(this);
        initGreenDao();
        //版本检测
        Bugly.init(getApplicationContext(), "9a664fcfee", false);
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
        //初始化全局异常崩溃
        initCrash();

    }

    private void initGreenDao() {
        DaoManager mManager = DaoManager.getInstance();
        mManager.init(this);
    }

    private void initCrash() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //背景模式,开启沉浸式
                .enabled(true) //是否启动全局异常捕获
                .showErrorDetails(true) //是否显示错误详细信息
                .showRestartButton(true) //是否显示重启按钮
                .trackActivities(true) //是否跟踪Activity
                .minTimeBetweenCrashesMs(2000) //崩溃的间隔时间(毫秒)
                .errorDrawable(R.mipmap.ic_launcher) //错误图标
                .errorActivity(MainActivity.class) //崩溃后的错误activity
                .apply();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
