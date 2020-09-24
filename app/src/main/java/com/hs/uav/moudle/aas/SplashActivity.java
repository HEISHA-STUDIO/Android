package com.hs.uav.moudle.aas;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.hs.uav.BR;
import com.hs.uav.R;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.databinding.ActivitySplashBinding;
import com.hs.uav.moudle.aas.model.SplashVM;

import me.goldze.mvvmhabit.base.BaseActivity;

/***
 * 启动UI
 */
public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashVM> {


    @Override
    public SplashVM initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(SplashVM.class);
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_splash;
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
//        final AnimationDrawable animationDrawable = (AnimationDrawable) binding.iv.getBackground();
//        animationDrawable.start();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(binding.iv,"alpha",0.4f,1.0f);
        binding.iv.setImageResource(R.drawable.ic_logo_orangeandred_2);
        objectAnimator.setDuration(3000);
        objectAnimator.start();
        binding.iv.postDelayed(() -> {
            objectAnimator.cancel();
            startActivity(DeviceManagerActivity.class);
            finish();
        },4000);
    }
    }