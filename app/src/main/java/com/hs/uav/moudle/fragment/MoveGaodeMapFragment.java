package com.hs.uav.moudle.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.MAVLink.DLink.msg_gps_raw_int;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
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
import com.hs.uav.common.utils.GCJ02ToWGS84Util;
import com.hs.uav.databinding.FragmentMoveGaodeLayoutBinding;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;
import com.hs.uav.logic.listener.OnRefreshLastAirLineListener;
import com.hs.uav.moudle.main.model.GaoMapMainVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.BR;
import me.goldze.mvvmhabit.base.BaseFragment;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.Utils;

/***
 * 可拖动的高德Map图层Fragment，用于悬浮在视频Fragment上方
 */
public class MoveGaodeMapFragment extends BaseFragment<FragmentMoveGaodeLayoutBinding, GaoMapMainVM>
        implements OnRefreshLastAirLineListener {
    public TextureMapView mMapView = null;
    public AMap aMap;
    public Polyline polyline;

    private CommonDaoUtils<MapAirLineInfo> mapAirLineInfoDaoUtils;
    private DaoUtilsStore _Store;
    public long airLineID = 0;// 航线ID
    public List<PointInfo> pointInfoList = new ArrayList<>(); //marker航点数据集合
    public Marker marker;

    @Override
    public GaoMapMainVM initViewModel() {
        AppController.getInstance().regOnRefreshLastAirLineListener(this);
        AppViewModelFactory factory = AppViewModelFactory.getInstance(MyApplication.getInstance());
        return ViewModelProviders.of(this, factory).get(GaoMapMainVM.class);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_move_gaode_layout;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        _Store = DaoUtilsStore.getInstance();
        mapAirLineInfoDaoUtils = _Store.getmapAirLineInfoDaoUtils();
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
    }

    private void initMap() {
        //初始化定位蓝点样式类
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setLogoBottomMargin(-100);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setAllGesturesEnabled(MainActivity.SMALL_MODEL == 0?false:true);
        drawAirMapLine();
    }

    private void drawAirMapLine() {
        List<MapAirLineInfo> mapAirLineInfos = mapAirLineInfoDaoUtils.queryByNativeSql(" where UUID = ?",
                new String[]{Injection.aasDataRe().getLastMapLineId(Injection.aasDataRe().getUserName())});
        if (mapAirLineInfos != null && !mapAirLineInfos.isEmpty()) {
            MapAirLineInfo mapAirLineInfo = mapAirLineInfos.get(0);
            airLineID = mapAirLineInfo.get_id();
            List<PointInfo> pointInfoList = new Gson().fromJson(mapAirLineInfo.getPointsJson(),
                    new TypeToken<List<PointInfo>>() {
                    }.getType());
            this.pointInfoList.addAll(pointInfoList);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pointInfoList.get(0).getLat(),
                    pointInfoList.get(0).getLag()), 18f));
            for (PointInfo pointInfo : pointInfoList) {
                setMarker(new LatLng(pointInfo.getLat(), pointInfo.getLag()), pointInfo.getPointID());
            }
            drawMarkerLineList();
        }
    }

    /***
     * 设置地图标记Marker Point
     * @param latLng 纬度,经度
     * @param mid 当前marker 编号ID
     */
    public void setMarker(LatLng latLng, int mid) {
        LayoutInflater mInflater = LayoutInflater.from(MainActivity.mContext);
        View view = mInflater.inflate(R.layout.marker_small_point_layout, null);
        TextView markerPointTV = view.findViewById(R.id.point_index_tv);
        markerPointTV.setText(String.valueOf(mid));
        markerPointTV.setBackgroundResource(mid % 2 == 0 ? R.drawable.marker_point_green_bg_20 : R.drawable.marker_point_orange_bg_20);
        Bitmap bitmap = Utils.convertViewToBitmap(view);
        //绘制marker
        aMap.addMarker(new MarkerOptions()
                .position(latLng).period(mid)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .draggable(true));
    }

    public void addHomeMarker(LatLng latLng) {
        LayoutInflater mInflater = LayoutInflater.from(MainActivity.mContext);
        View view = mInflater.inflate(R.layout.marker_small_point_layout, null);
        TextView markerPointTV = view.findViewById(R.id.point_index_tv);
        markerPointTV.setBackgroundResource(R.mipmap.ic_current_marker);
        Bitmap bitmap = Utils.convertViewToBitmap(view);
        //绘制marker
        marker = aMap.addMarker(new MarkerOptions()
                .position(latLng).period(0)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .draggable(true));
    }

    /****
     * 航线Point标记串联
     */
    private void drawMarkerLineList() {
        List<LatLng> latLngList = new ArrayList<>();
        for (int i = 0; i < pointInfoList.size(); i++) {
            PointInfo pointInfo = pointInfoList.get(i);
            latLngList.add(new LatLng(pointInfo.getLat(), pointInfo.getLag()));
        }
        latLngList.add(new LatLng(pointInfoList.get(0).getLat(), pointInfoList.get(0).getLag()));
        if (polyline != null) {
            polyline.remove();
        }
        polyline = aMap.addPolyline(new PolylineOptions().addAll(latLngList).width(7).color(Color.parseColor("#F09819")));
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
    }

    @Override
    public void refreshLastAirLine(MapAirLineInfo mapAirLineInfo) {

    }

    @Override
    public void refreshCurrentMapMarker(msg_gps_raw_int gpsRaw, int hdg) {
        //刷新当前飞机marker所在位置，要进行坐标转化
        float lat = gpsRaw.lat / 10000000f;
        float lon = gpsRaw.lon / 10000000f;
        Map<String, Double> map = GCJ02ToWGS84Util.transform(lon, lat);
        LatLng latLng = new LatLng(map.get("lat").floatValue(), map.get("lon").floatValue());
        if (marker == null) {
            addHomeMarker(latLng);
        } else {
            marker.setPosition(latLng);
        }
        marker.setRotateAngle(360-hdg);
    }
}
