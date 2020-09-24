package me.goldze.mvvmhabit.utils.constant;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.utils.Base64Coder;

/***
 * 全局变量类
 */
public class Contact {
    public final static String X_APP_ID = "2";//android-id
    public final static String APP_KEY = "abcabc";
    public final static int PAGE_SIZE = 10;
    public static int ENTRUST_TYPE = 0;//0-开多，1开空

    //帮助中心
    public final static String WEB_HELP_CENTER_URL = "http://omexh5.tpqy.pro/#/help";
    //联系客服
    public final static String WEB_CONTRACT_SALES_URL = "http://omexh5.tpqy.pro/#/kefu";
    //关于我们
    public final static String WEB_ABOUT_US_URL = "http://omexh5.tpqy.pro/#/aboutus/uaboutus";

    /**
     * 手势密码点的状态
     */
    public static final int POINT_STATE_NORMAL = 0; // 正常状态
    public static final int POINT_STATE_SELECTED = 1; // 按下状态
    public static final int POINT_STATE_WRONG = 2; // 错误状态

    /**
     * 当前屏幕的高度
     */
    public final static int DISPLAYH = BaseApplication.getInstance().getResources().getDisplayMetrics().heightPixels;
    /**
     * 当前屏幕的宽度
     */
    public final static int DISPLAYW = BaseApplication.getInstance().getResources().getDisplayMetrics().widthPixels;


    //获取版本号
    public static int getVersionCode() {
        try {
            return BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static String getVersionName() {
        try {
            return BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /***
     * 获取android设备编码，该编码为android的id
     * @return
     */
    public static String getAndroidID() {
        return Settings.System.getString(BaseApplication.getInstance().getContentResolver(), Settings.System.ANDROID_ID);
    }

    /**
     * 传入url 先压缩 后转为base64
     *
     * @param srcPath
     * @return
     */
    public static String getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 1280f;//这里设置高度为800f
        float ww = 720f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImageToBase64(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private static String compressImageToBase64(Bitmap image) {
        String url = null;
        if (image != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int options = 100;
            while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset();//重置baos即清空baos
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                options -= 10;//每次都减少10
            }
            byte[] bytes = baos.toByteArray();
            url = new String(Base64Coder.encode(bytes));
        }
        return url;
    }

    public static Bitmap getBitmapImage(String data) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapByte = Base64.decode(data, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String returnBase64ToString(String data) {
        String result = null;
        try {
            byte[] bitmapByte = Base64.decode(data, Base64.DEFAULT);
            result = new String(bitmapByte, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
