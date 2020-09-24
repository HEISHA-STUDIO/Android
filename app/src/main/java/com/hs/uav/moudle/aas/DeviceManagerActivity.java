package com.hs.uav.moudle.aas;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.hs.uav.BR;
import com.hs.uav.R;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.common.entity.DeviceInfo;
import com.hs.uav.common.view.cardview.DevicePageAdapter;
import com.hs.uav.databinding.ActivityDeviceManagerBinding;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;
import com.hs.uav.moudle.aas.model.SplashVM;
import com.tencent.bugly.beta.Beta;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.base.EditUAVNameDialog;

/***
 * 设备选择管理UI
 */
public class DeviceManagerActivity extends BaseActivity<ActivityDeviceManagerBinding, SplashVM> {
    CommonDaoUtils<DeviceInfo> deviceDaoUtils;
    List<DeviceInfo> deviceInfos = new ArrayList<>();
    DevicePageAdapter devicePageAdapter;

    @Override
    public SplashVM initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(SplashVM.class);
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_device_manager;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        initPermissionsManager();
        DaoUtilsStore _Store = DaoUtilsStore.getInstance();
        deviceDaoUtils = _Store.getDeviceDaoUtils();
        deviceInfos.add(null);
        List<DeviceInfo> deviceInfoListM = deviceDaoUtils.queryAll();
        if (deviceInfoListM != null && !deviceInfoListM.isEmpty()) {
            for (DeviceInfo deviceInfo : deviceInfoListM) {
                deviceInfos.add(deviceInfo);
            }
        }
        deviceInfos.add(new DeviceInfo());
        deviceInfos.add(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void initViewObservable() {
        super.initViewObservable();
        devicePageAdapter = new DevicePageAdapter(this, deviceInfos);
        binding.vpLoop.setAdapter(devicePageAdapter);
        binding.vpLoop.setPageMargin(20);
        binding.vpLoop.setOffscreenPageLimit(3);
        binding.vpLoop.setCurrentItem(deviceInfos.size() - 1);
        binding.vpLoop.setPageTransformer(true, (ViewPager.PageTransformer) (page, position) -> {
            float v = Math.abs(position - 0.33f);
            float v1 = (float) (2 * (v * v));
            page.setScaleY(1 - v1);
            page.setScaleX(1 - v1);
        });

        DevicePageAdapter.onDelEvent.observe(this, deviceInfo -> {
            int index = -1;
            for (int i = 0; i < deviceInfos.size(); i++) {
                DeviceInfo deviceInfo1 = deviceInfos.get(i);
                if (deviceInfo != null && deviceInfo1 != null &&
                        deviceInfo1.getDevicePin().equals(deviceInfo.getDevicePin())) {
                    index = i;
                    break;
                }
            }
            if (index > -1) {
                deviceInfos.remove(index);
                deviceDaoUtils.delete(deviceInfo);
                devicePageAdapter = new DevicePageAdapter(this, deviceInfos);
                binding.vpLoop.setAdapter(devicePageAdapter);
            }
        });

        DevicePageAdapter.onEditEvent.observe(this, deviceInfo -> {
            EditUAVNameDialog dialog = new EditUAVNameDialog(this);
            dialog.setItem(deviceInfo.getDeviceName(), new EditUAVNameDialog.OperaterListener() {
                @Override
                public void operater(String name) {
                    deviceInfo.setDeviceName(name);
                    deviceDaoUtils.update(deviceInfo);
                    int index = -1;
                    for (int i = 0; i < deviceInfos.size(); i++) {
                        DeviceInfo device = deviceInfos.get(i);
                        if (deviceInfo != null && device != null &&
                                device.getDevicePin().equals(deviceInfo.getDevicePin())) {
                            index = i;
                            break;
                        }
                    }
                    if (index > -1) {
                        deviceInfos.set(index, deviceInfo);
                        devicePageAdapter = new DevicePageAdapter(DeviceManagerActivity.this, deviceInfos);
                        binding.vpLoop.setAdapter(devicePageAdapter);
                    }
                }
            });
            dialog.show();
        });

        //版本检测
        Beta.checkUpgrade(true,false);
    }

    private static final int REQUEST_PERMISSION = 1;

    private void initPermissionsManager() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.USE_BIOMETRIC,
                        Manifest.permission.WRITE_SETTINGS,
                        Manifest.permission.WRITE_SECURE_SETTINGS}, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}