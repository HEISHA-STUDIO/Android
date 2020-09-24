package com.hs.uav.moudle.main.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.hs.uav.R;

import java.util.List;

/***
 * 高度尺子适配器
 */
public class ScaleRulerPageAdapter extends PagerAdapter {
    private Context context;
    private List<String> scaleInfos;

    public ScaleRulerPageAdapter(Context context, List<String> scaleInfos) {
        this.context = context;
        this.scaleInfos = scaleInfos;
    }

    @Override
    public int getCount() {
        return scaleInfos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        TextView mScaleRulerTV = new TextView(context);
        params.gravity = Gravity.CENTER;
        mScaleRulerTV.setLayoutParams(params);
        mScaleRulerTV.setTextSize(12f);
        mScaleRulerTV.setGravity(Gravity.CENTER);
        mScaleRulerTV.setTextColor(context.getResources().getColor(R.color.black));
        mScaleRulerTV.setText(scaleInfos.get(position));
        container.addView(mScaleRulerTV);
        return mScaleRulerTV;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
