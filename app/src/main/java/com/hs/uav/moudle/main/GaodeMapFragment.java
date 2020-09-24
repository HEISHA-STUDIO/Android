package com.hs.uav.moudle.main;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.MAVLink.DLink.msg_gps_raw_int;
import com.MAVLink.DLink.msg_mission_request_list;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hs.uav.MainActivity;
import com.hs.uav.R;
import com.hs.uav.common.app.AppController;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.common.app.Injection;
import com.hs.uav.common.app.MyApplication;
import com.hs.uav.common.entity.MapAirLineInfo;
import com.hs.uav.common.entity.PointInfo;
import com.hs.uav.databinding.FragmentGaodeLayoutBinding;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;
import com.hs.uav.logic.listener.OnRefreshLastAirLineListener;
import com.hs.uav.logic.mqtt.MqttService;
import com.hs.uav.moudle.main.adapter.PointManagerAdapter;
import com.hs.uav.moudle.main.model.GaoMapMainVM;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.goldze.mvvmhabit.BR;
import me.goldze.mvvmhabit.base.BaseFragment;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.Utils;

/***
 * 高德地图Map 管理Fragment
 * @author tony.liu
 * key-Name:  hs_uav_android
 * key:  e76bc13062179a881cdc2257f13ec0b1
 */
public class GaodeMapFragment extends BaseFragment<FragmentGaodeLayoutBinding, GaoMapMainVM>
        implements AMap.OnMapClickListener, AMap.OnMarkerDragListener, AMap.OnMarkerClickListener, OnRefreshLastAirLineListener {
    public MapView mMapView = null;
    public AMap aMap;
    public Polyline polyline;
    private CommonDaoUtils<MapAirLineInfo> mapLineUtils;
    private DaoUtilsStore _Store;

    public String airLineID;// 航线ID
    public MapAirLineInfo mInfo;//当前航线
    public boolean isStartAddPoint = false;//是否开启新增航点
    public List<PointInfo> pointList = new ArrayList<>(); //marker航点数据集合
    private PointManagerAdapter pointManagerAdapter;

    /****
     * 设置航线模式
     * @param airLineID 当前航线ID，新增时id为0
     */
    public void setAirLineMode(String airLineID) {
        this.airLineID = airLineID;
        if (aMap != null) {
            aMap.clear();
            aMap = null;
        }
        if (mMapView != null) {
            aMap = mMapView.getMap();
            initMap();
        }
    }

    public void drawAirLineView() {
        if (mapLineUtils == null){
            _Store = DaoUtilsStore.getInstance();
            mapLineUtils = _Store.getmapAirLineInfoDaoUtils();
        }
        if (!TextUtils.isEmpty(airLineID)) {
            pointList.clear();
            pointList = new ArrayList<>();
            List<MapAirLineInfo> mapAirLineInfos = mapLineUtils.queryByNativeSql(" where UUID = ? ",new String[]{airLineID});
            if (mapAirLineInfos != null && !mapAirLineInfos.isEmpty()) {
                mInfo = mapAirLineInfos.get(0);
                if (mInfo != null) {
                    List<PointInfo> pointInfos = new Gson().fromJson(mInfo.getPointsJson(),
                            new TypeToken<List<PointInfo>>() {}.getType());
                    pointList.addAll(pointInfos);
                    pointManagerAdapter = new PointManagerAdapter(MainActivity.mContext, pointList);
                    binding.mapPointListview.setAdapter(pointManagerAdapter);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pointList.get(0).getLat(),
                            pointList.get(0).getLag()), 18));
                    for (PointInfo pointInfo : pointList) {
                        setMarker(new LatLng(pointInfo.getLat(), pointInfo.getLag()), pointInfo.getPointID());
                    }
                    drawMarkerLineList();
                    if (TextUtils.isEmpty(mInfo.getPreImagePath())) {
                        //如果当前航线没有缩略图，去生成缩略图
                        setMapSmallImgView();
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public GaoMapMainVM initViewModel() {
        AppController.getInstance().regOnRefreshLastAirLineListener(this);
        AppViewModelFactory factory = AppViewModelFactory.getInstance(MyApplication.getInstance());
        return ViewModelProviders.of(this, factory).get(GaoMapMainVM.class);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_gaode_layout;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        if (mapLineUtils == null) {
            _Store = DaoUtilsStore.getInstance();
            mapLineUtils = _Store.getmapAirLineInfoDaoUtils();
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        mMapView = binding.gaodeMapView;
        mMapView.onCreate(saveState);
        if (aMap != null) {
            aMap.clear();
        }
        aMap = mMapView.getMap();
        initMap();
        initClickEvent();
        drawAirLineView();
        if (MainActivity.AIRLINE) {
            binding.downloadPointLl.setVisibility(View.GONE);
        } else {
            binding.downloadPointLl.setVisibility(View.VISIBLE);
        }
    }

    private void initMap() {
        //初始化定位蓝点样式类
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setOnMapClickListener(this);
        aMap.getUiSettings().setLogoBottomMargin(-100);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.addOnMarkerClickListener(this);
        aMap.addOnMarkerDragListener(this);
        initMapLocation();
    }

    private void initClickEvent() {
        binding.downloadPointLl.setOnClickListener(view -> {
            msg_mission_request_list msg = new msg_mission_request_list();
            MqttService.publish(msg.pack().encodePacket());
        });

        binding.startAddPointLl.setOnClickListener(view -> {
            if (!isStartAddPoint) {
                isStartAddPoint = true;
                binding.startAddPointLl.setBackgroundResource(R.drawable.ic_rang_2);
            } else {
                isStartAddPoint = false;
                binding.startAddPointLl.setBackgroundResource(0);
            }
        });
    }

    private void initMapLocation() {
        if (aMap != null) {
            String location = Injection.aasDataRe().getCurrentUAVPoint(Injection.aasDataRe().getUserName());
            if (TextUtils.isEmpty(location)) {//没有飞机所在位置则显示本地位置
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.CURRENT_LAT,
                        MainActivity.CURRENT_LON), 18));
            } else {
                String arr[] = location.split("#");
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(arr[0]),
                        Double.parseDouble(arr[1])), 18));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aMap = null;
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        pointList.clear();
        pointList = new ArrayList<>();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (isStartAddPoint) {
            PointInfo pointInfo = new PointInfo();
            pointInfo.setPointID(pointList.size() + 1);
            pointInfo.setLat((float) latLng.latitude);
            pointInfo.setLag((float) latLng.longitude);
            pointList.add(pointInfo);
            //添加标记点
            setMarker(latLng, pointInfo.getPointID());
            drawMarkerLineList();
        } else {
            ToastUtils.showLong("请点击左上角+号开始新增航点信息");
        }
    }

    /***
     * 设置地图标记Marker Point
     * @param latLng 纬度,经度
     * @param mid 当前marker 编号ID
     */
    public void setMarker(LatLng latLng, int mid) {
        LayoutInflater mInflater = LayoutInflater.from(MainActivity.mContext);
        View view = mInflater.inflate(R.layout.marker_point_layout, null);
        TextView markerPointTV = view.findViewById(R.id.point_index_tv);
        markerPointTV.setText(String.valueOf(mid));
        markerPointTV.setBackgroundResource(R.drawable.marker_point_green_bg_20);
        for (Marker marker : aMap.getMapScreenMarkers()) {
            View view1 = mInflater.inflate(R.layout.marker_point_layout, null);
            TextView markerPointTV1 = view1.findViewById(R.id.point_index_tv);
            markerPointTV1.setText(String.valueOf(marker.getPeriod()));
            markerPointTV1.setBackgroundResource(R.drawable.marker_point_orange_bg_20);
            Bitmap bitmap = Utils.convertViewToBitmap(view1);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
        Bitmap bitmap = Utils.convertViewToBitmap(view);
        //绘制marker
        aMap.addMarker(new MarkerOptions()
                .position(latLng).period(mid)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .draggable(true));
    }

    /****
     * 航线Point标记串联
     */
    private void drawMarkerLineList() {
        List<LatLng> latLngList = new ArrayList<>();
        for (int i = 0; i < pointList.size(); i++) {
            PointInfo pointInfo = pointList.get(i);
            latLngList.add(new LatLng(pointInfo.getLat(), pointInfo.getLag()));
        }
        latLngList.add(new LatLng(pointList.get(0).getLat(), pointList.get(0).getLag()));
        if (polyline != null) {
            polyline.remove();
        }
        polyline = aMap.addPolyline(new PolylineOptions().addAll(latLngList).width(7).color(Color.parseColor("#F09819")));
        pointManagerAdapter = new PointManagerAdapter(MainActivity.mContext, pointList);
        binding.mapPointListview.setAdapter(pointManagerAdapter);

        //某个航点删除后的回调
        pointManagerAdapter.delPointEvent.observe(this, pointInfo -> {
            for (Marker marker : aMap.getMapScreenMarkers()) {
                if (marker.getPeriod() == pointInfo.getPointID()) {
                    marker.remove();
                    break;
                }
            }
            pointList.remove(pointInfo);
            LayoutInflater mInflater = LayoutInflater.from(MainActivity.mContext);
            int index = -1;
            for (int i = 0; i < pointList.size(); i++) {
                index++;
                PointInfo pointInfo1 = pointList.get(i);
                Marker marker = returnCurrentMarker(pointInfo1.getPointID());
                if (marker != null) {
                    pointInfo1.setPointID(i + 1);
                    View view = mInflater.inflate(R.layout.marker_point_layout, null);
                    TextView markerPointTV = view.findViewById(R.id.point_index_tv);
                    markerPointTV.setText(String.valueOf(pointInfo1.getPointID()));
                    markerPointTV.setBackgroundResource((index < pointList.size() - 1) ?
                            R.drawable.marker_point_orange_bg_20 : R.drawable.marker_point_green_bg_20);
                    Bitmap bitmap = Utils.convertViewToBitmap(view);
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    marker.setPeriod(pointInfo1.getPointID());
                    pointList.set(i, pointInfo1);
                }
            }
            //重新绘制marker
            drawMarkerLineList();
        });
    }

    public Marker returnCurrentMarker(int mid) {
        for (Marker marker : aMap.getMapScreenMarkers()) {
            if (marker.getPeriod() == mid) {
                return marker;
            }
        }
        return null;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        LayoutInflater mInflater = LayoutInflater.from(MainActivity.mContext);
        for (Marker marker1 : aMap.getMapScreenMarkers()) {
            View view = mInflater.inflate(R.layout.marker_point_layout, null);
            TextView markerPointTV = view.findViewById(R.id.point_index_tv);
            if (marker.getPeriod() == marker1.getPeriod()) {
                markerPointTV.setText(String.valueOf(marker1.getPeriod()));
                markerPointTV.setBackgroundResource(R.drawable.marker_point_blue_bg_20);
                Bitmap bitmap = Utils.convertViewToBitmap(view);
                marker1.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            } else {
                markerPointTV.setText(String.valueOf(marker1.getPeriod()));
                markerPointTV.setBackgroundResource(R.drawable.marker_point_orange_bg_20);
                Bitmap bitmap = Utils.convertViewToBitmap(view);
                marker1.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
        }
        for (int i = 0; i < pointList.size(); i++) {
            PointInfo pointInfo = pointList.get(i);
            if (pointInfo.getPointID() == marker.getPeriod()) {
                pointInfo.setCheck(true);
            } else {
                pointInfo.setCheck(false);
            }
            pointList.set(i, pointInfo);
        }
        pointManagerAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        for (int i = 0; i < pointList.size(); i++) {
            PointInfo pointInfo = pointList.get(i);
            if (pointInfo.getPointID() == marker.getPeriod()) {
                pointInfo.setLat((float) marker.getPosition().latitude);
                pointInfo.setLag((float) marker.getPosition().longitude);
                pointList.set(i, pointInfo);
                break;
            }
        }
        drawMarkerLineList();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }

    @Override
    public void refreshLastAirLine(MapAirLineInfo mapAirLineInfo) {
        //刷新下载的地图航线信息
        if (aMap == null) {
            aMap = mMapView.getMap();
            initMap();
        }
        aMap.clear();
        airLineID = mapAirLineInfo.getUUID();
        binding.downloadPointLl.setVisibility(View.GONE);
        drawAirLineView();
    }

    @Override
    public void refreshCurrentMapMarker(msg_gps_raw_int gpsRaw, int hdg) {

    }

    private void setMapSmallImgView() {
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                try {
                    String uuid = "H" + UUID.randomUUID().hashCode() + "Z";
                    String filePath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/DNest_Air_Point_" + uuid + ".jpg";
                    FileOutputStream fos = new FileOutputStream(filePath);
                    boolean ifSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    fos.flush();
                    fos.close();
                    if (ifSuccess) {
                        if (!TextUtils.isEmpty(airLineID) && mInfo != null) {
                            mInfo.setPreImagePath(filePath);
                            mInfo.setPointsJson(new Gson().toJson(pointList));
                            mapLineUtils.update(mInfo);
                            AppController.getInstance().postRefreshMapSmallImgView();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int i) {

            }
        });
    }
}
