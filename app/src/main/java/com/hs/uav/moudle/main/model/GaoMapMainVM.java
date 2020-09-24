package com.hs.uav.moudle.main.model;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hs.uav.logic.data.repositor.AASDataRepository;

import me.goldze.mvvmhabit.base.BaseViewModel;

/***
 * 高德地图管理VM
 */
public class GaoMapMainVM extends BaseViewModel<AASDataRepository> {
    public GaoMapMainVM(@NonNull Application application, AASDataRepository aasDataRepository) {
        super(application, aasDataRepository);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
