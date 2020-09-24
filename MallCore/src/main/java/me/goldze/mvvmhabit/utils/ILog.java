package me.goldze.mvvmhabit.utils;

import android.util.Log;

/**
 * 日志的功能操作类 可将日志保存至SD卡
 */
public class ILog {

    /**
     * DEBUG级别开关
     */
	public static  boolean DEBUG = true;
	
    /**
     * DEBUG_ERROR级别开关
     */
	private static final boolean DEBUG_ERROR = true;

	/**
	 * 是否保存至SD卡
	 */
	private static final boolean SAVE_TO_SD = true;
	/**
	 * 用于打印错误级的日志信息
	 * @param strModule LOG TAG
	 * @param strErrMsg 打印信息
	 */
	public static void e(String strModule, String strErrMsg) {
		if (DEBUG_ERROR) {
			Log.e(strModule, strErrMsg);
		}
	}
}
