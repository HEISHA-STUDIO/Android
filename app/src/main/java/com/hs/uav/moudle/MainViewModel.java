package com.hs.uav.moudle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.hs.uav.logic.data.repositor.AASDataRepository;

import java.util.Observable;

import me.goldze.mvvmhabit.base.BaseViewModel;

/***
 * 主页视图model
 */
public class MainViewModel extends BaseViewModel<AASDataRepository> {
    public ObservableField<String> currentBattery = new ObservableField<>();
    public MainViewModel(@NonNull Application application, AASDataRepository aasDataRepository) {
        super(application, aasDataRepository);
        currentBattery.set("100%");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
