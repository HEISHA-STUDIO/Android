package com.hs.uav.moudle.aas.model;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hs.uav.logic.data.repositor.AASDataRepository;

import me.goldze.mvvmhabit.base.BaseViewModel;

/***
 * 启动设备管理VM
 */
public class SplashVM extends BaseViewModel<AASDataRepository> {
    public SplashVM(@NonNull Application application, AASDataRepository aasDataRepository) {
        super(application, aasDataRepository);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
