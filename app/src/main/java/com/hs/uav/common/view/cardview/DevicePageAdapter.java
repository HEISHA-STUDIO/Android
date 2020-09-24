package com.hs.uav.common.view.cardview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;

import com.hs.uav.MainActivity;
import com.hs.uav.R;
import com.hs.uav.common.app.Injection;
import com.hs.uav.common.entity.DeviceInfo;
import com.hs.uav.logic.data.http.service.LoginReq;
import com.hs.uav.moudle.aas.LoginDeviceActivity;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.base.MySystemTipsDialog;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.http.ApiDisposableObserver;
import me.goldze.mvvmhabit.http.BaseResponse;
import me.goldze.mvvmhabit.utils.KLog;

public class DevicePageAdapter extends PagerAdapter {
    private Context context;
    private List<DeviceInfo> deviceInfos;
    public static SingleLiveEvent<DeviceInfo> onDelEvent = new SingleLiveEvent<>();
    public static SingleLiveEvent<DeviceInfo> onEditEvent = new SingleLiveEvent<>();

    public DevicePageAdapter(Context context, List<DeviceInfo> deviceInfos) {
        this.context = context;
        this.deviceInfos = deviceInfos;
    }

    @Override
    public int getCount() {
        return deviceInfos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_layout, container, false);
        LinearLayout mAddLL = view.findViewById(R.id.add_ll);
        ImageView mAddDeviceIV = view.findViewById(R.id.add_device_iv);
        RelativeLayout mDetailLL = view.findViewById(R.id.detail_ll);
        TextView mTitleTV = view.findViewById(R.id.title_tv);
        TextView mNameTV = view.findViewById(R.id.name_tv);
        ImageView mDelIV = view.findViewById(R.id.del_iv);
        LinearLayout mCardLL = view.findViewById(R.id.card_ll);
        DeviceInfo deviceInfo = deviceInfos.get(position);

        if (deviceInfo == null) {
            mCardLL.setVisibility(View.GONE);
        } else {
            mCardLL.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(deviceInfo.getDeviceCode())) {
                mAddLL.setVisibility(View.VISIBLE);
                mDetailLL.setVisibility(View.GONE);
                mAddDeviceIV.setOnClickListener(view1 -> {
                    context.startActivity(new Intent(context, LoginDeviceActivity.class));
                    ((Activity) context).finish();
                });
            } else {
                mAddLL.setVisibility(View.GONE);
                mDetailLL.setVisibility(View.VISIBLE);
                mTitleTV.setText(deviceInfo.getDeviceCode());
                mNameTV.setText(deviceInfo.getDeviceName());

                //进入设备管理
                mDetailLL.setOnClickListener(view1 -> {
                    LoginReq loginReq = new LoginReq();
                    loginReq.setUsername(deviceInfo.getDevicePin());
                    loginReq.setPassword(deviceInfo.getDevicePin());
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
                                    Injection.aasDataRe().saveCurrentUAVPoint(loginReq.getUsername(),null);
                                    Injection.aasDataRe().saveConfigInfo("tcp://" + baseResponse.getIp() + ":" + baseResponse.getPort());
                                    context.startActivity(new Intent(context, MainActivity.class));
                                    ((Activity) context).finish();
                                }
                            });
                });

                //删除设备
                mDelIV.setOnClickListener(view1 -> {
                    MySystemTipsDialog dialog = new MySystemTipsDialog(context);
                    dialog.setItem("Are you sure want to delete?", new MySystemTipsDialog.OperaterListener() {
                        @Override
                        public void operater() {
                            onDelEvent.setValue(deviceInfo);
                        }
                    });
                    dialog.show();
                });

                //长按昵称修改别名
                mNameTV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        onEditEvent.setValue(deviceInfo);
                        return false;
                    }
                });
            }
        }
        container.addView(view);
        return view;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public float getPageWidth(int position) {
        return (float) 0.33;
    }
}
