package com.hs.uav.common.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * 获得屏幕像素 
 */
public class Pixels {

    private static int[] px = new int[101];
    private static int[] py = new int[101];
    private int w,h;
    private DisplayMetrics dm;
    public Pixels(Activity activity){
        dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        w = dm.widthPixels;
        h = dm.heightPixels;
        int length = px.length;
        for(int index=0;index<length;index++){
            px[index] = (int)(w*0.01*index);
            py[index] = (int)(h*0.01*index);
        }
    }
    /**
     * 获得x点值
     * @param index
     * @return
     */
    public static int getpixels_x(int index){
        return px[index];
    }
    /**
     * 获得y点值
     * @param index
     * @return
     */
    public static int getpixels_y(int index){
        return py[index];
    }
}