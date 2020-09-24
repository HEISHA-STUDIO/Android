package com.hs.uav.moudle.aas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.lifecycle.ViewModelProviders;

import com.hs.uav.BR;
import com.hs.uav.MainActivity;
import com.hs.uav.R;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.common.app.Injection;
import com.hs.uav.common.entity.DeviceInfo;
import com.hs.uav.databinding.ActivityLoginDeviceBindingImpl;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;
import com.hs.uav.logic.data.http.service.LoginReq;
import com.hs.uav.moudle.aas.model.SplashVM;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.http.ApiDisposableObserver;
import me.goldze.mvvmhabit.http.BaseResponse;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.ToastStatusUtils;
import me.goldze.mvvmhabit.zxinglibrary.android.CaptureActivity;
import me.goldze.mvvmhabit.zxinglibrary.bean.ZxingConfig;
import me.goldze.mvvmhabit.zxinglibrary.common.Constant;

/***
 * 登录设备信息UI
 */
public class LoginDeviceActivity extends BaseActivity<ActivityLoginDeviceBindingImpl, SplashVM> {
    private int REQUEST_CODE_SCAN = 111;
    CommonDaoUtils<DeviceInfo> deviceDaoUtils;

    @Override
    public SplashVM initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(SplashVM.class);
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_login_device;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
    }

    @SuppressLint("CheckResult")
    @Override
    public void initViewObservable() {
        super.initViewObservable();

        //扫码界面UI
        binding.scanQrBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginDeviceActivity.this, CaptureActivity.class);
            ZxingConfig config = new ZxingConfig();
            config.setPlayBeep(false);//是否播放扫描声音 默认为true
            config.setDecodeBarCode(false);//是否扫描条形码 默认为true
            config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
            config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
            config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
            config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        });

        //绑定设备并登录
        binding.bindingDeviceBtn.setOnClickListener(view -> {
            String pinCode = binding.pinCodeEdt.getText().toString();
            if (TextUtils.isEmpty(pinCode)) {
                ToastStatusUtils.show("请输入Pin 码进行设备绑定", R.mipmap.ic_sys_tips);
                return;
            }
            LoginReq loginReq = new LoginReq();
            loginReq.setUsername(pinCode);
            loginReq.setPassword(pinCode);
            Injection.aasDataRe().getServerConfig(loginReq)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new ApiDisposableObserver() {
                        @SuppressLint("CheckResult")
                        @Override
                        public void onResult(Object o) {
                            BaseResponse baseResponse = (BaseResponse) o;
                            KLog.e("IP地址：" + baseResponse.getIp());
                            KLog.e("端口：", baseResponse.getPort());
                            KLog.e("TOPIC:", baseResponse.getTopic());
                            Injection.aasDataRe().saveUserName(loginReq.getUsername());
                            Injection.aasDataRe().savePassword(loginReq.getPassword());
                            Injection.aasDataRe().savePublicKey(baseResponse.getTopic());
                            Injection.aasDataRe().saveConfigInfo("tcp://" + baseResponse.getIp() + ":" + baseResponse.getPort());
                            Injection.aasDataRe().saveCurrentUAVPoint(loginReq.getUsername(),null);
                            //存储设备信息到本地DB
                            DaoUtilsStore _Store = DaoUtilsStore.getInstance();
                            deviceDaoUtils = _Store.getDeviceDaoUtils();

                            List<DeviceInfo> deviceInfos = deviceDaoUtils.queryAll();
                            int size = deviceInfos.size() + 1;
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setDeviceName(pinCode);
                            deviceInfo.setDeviceCode("D.NEST-" + size);
                            deviceInfo.setDevicePin(pinCode);
                            deviceDaoUtils.insert(deviceInfo);
                            startActivity(MainActivity.class);
                            finish();
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                binding.pinCodeEdt.setText(content);
            }
        }
    }

}