package com.hs.uav.moudle.main.model;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hs.uav.logic.data.repositor.AASDataRepository;

import me.goldze.mvvmhabit.base.BaseViewModel;

/***
 * 视频摄像头管理VM
 */
public class VideoMainVM extends BaseViewModel<AASDataRepository> {
    public VideoMainVM(@NonNull Application application, AASDataRepository aasDataRepository) {
        super(application, aasDataRepository);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
