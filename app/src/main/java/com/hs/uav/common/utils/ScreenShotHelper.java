package com.hs.uav.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.ViewGroup;

import com.amap.api.maps.MapView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ScreenShotHelper {
    /**
     * 组装地图截图和其他View截图，并且将截图存储在本地sdcard，需要注意的是目前提供的方法限定为MapView与其他View在同一个ViewGroup下
     *
     * @param bitmap        地图截图回调返回的结果
     * @param viewContainer MapView和其他要截图的View所在的父容器ViewGroup
     * @param mapView       MapView控件
     */
    public static void saveScreenShot(final Bitmap bitmap, final ViewGroup viewContainer, final MapView mapView) {
        new Thread() {
            public void run() {
                Bitmap screenShotBitmap = getMapAndViewScreenShot(bitmap, viewContainer, mapView);
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test1.png");
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        screenShotBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        screenShotBitmap.recycle();
                        bitmap.recycle();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 组装地图截图和其他View截图，需要注意的是目前提供的方法限定为MapView与其他View在同一个ViewGroup下
     *
     * @param bitmap        地图截图回调返回的结果
     * @param viewContainer MapView和其他要截图的View所在的父容器ViewGroup
     * @param mapView       MapView控件
     */
    public static Bitmap getMapAndViewScreenShot(Bitmap bitmap, ViewGroup viewContainer, MapView mapView) {
        int width = viewContainer.getWidth();
        int height = viewContainer.getHeight();
        final Bitmap screenBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(screenBitmap);
        canvas.drawBitmap(bitmap, mapView.getLeft(), mapView.getTop(), null);
        return screenBitmap;
    }
}