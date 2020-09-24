package com.hs.uav.moudle.main.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.hs.uav.R;
import com.hs.uav.common.entity.PointInfo;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.base.MySystemTipsDialog;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

/***
 * 航线标记Point 适配器
 */
public class PointManagerAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<PointInfo> pointInfos;
    private List<String> scaleList;
    private ScaleRulerPageAdapter adapter;
    private Context context;
    public SingleLiveEvent<PointInfo> delPointEvent = new SingleLiveEvent<>();

    public PointManagerAdapter(Context context, List<PointInfo> pointInfo) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.pointInfos = pointInfo;
        getScaleList();
        adapter = new ScaleRulerPageAdapter(context, scaleList);
    }

    public void getScaleList() {
        scaleList = new ArrayList<>();
        for (int i = 10; i <= 100; i++) {
            if (i % 10 == 0) {
                scaleList.add(String.valueOf(i));
            }
        }
    }

    public int getScaleIndex(String scale) {
        for (int i = 0; i < scaleList.size(); i++) {
            if (scale.equals(scaleList.get(i))) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getCount() {
        return pointInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return pointInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        convertView = mInflater.inflate(R.layout.item_air_point_layout, null);
        holder = new ViewHolder();
        holder.mPointIndexTV = convertView.findViewById(R.id.point_index_tv);
        holder.mAltitudeVP = convertView.findViewById(R.id.scaleWheelView_altitude);
        holder.mDelIV = convertView.findViewById(R.id.delete_iv);
        holder.mItemLL = convertView.findViewById(R.id.item_ll);

        PointInfo pointInfo = pointInfos.get(position);
        holder.mPointIndexTV.setText(String.valueOf(pointInfo.getPointID()));
        holder.mAltitudeVP.setAdapter(adapter);
        holder.mItemLL.setBackgroundColor(pointInfo.isCheck() ? Color.parseColor("#730175EE") : Color.parseColor("#80000000"));
        holder.mDelIV.setVisibility(pointInfo.isCheck() ? View.VISIBLE : View.GONE);
        holder.mAltitudeVP.setCurrentItem(getScaleIndex(String.valueOf(pointInfo.getAltitude())));

        holder.mDelIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MySystemTipsDialog dialog = new MySystemTipsDialog(context);
                dialog.setItem("Are you sure want to delete?", new MySystemTipsDialog.OperaterListener() {
                    @Override
                    public void operater() {
                        delPointEvent.setValue(pointInfo);
                        pointInfos.remove(pointInfo);
                        for (int i = 0; i < pointInfos.size(); i++) {
                            PointInfo pointInfo1 = pointInfos.get(i);
                            pointInfo1.setPointID(i + 1);
                            pointInfos.set(i, pointInfo1);
                        }
                        notifyDataSetChanged();
                    }
                });
                dialog.show();
            }
        });

        holder.mAltitudeVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                pointInfo.setAltitude(Integer.parseInt(scaleList.get(pos)));
                pointInfos.set(position, pointInfo);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return convertView;
    }

    public class ViewHolder {
        public RelativeLayout mItemLL;
        public TextView mPointIndexTV;
        public ViewPager mAltitudeVP;
        public ImageView mDelIV;
    }
}


