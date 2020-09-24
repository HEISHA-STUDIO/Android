package me.goldze.mvvmhabit.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/***
 * 自动解析函数
 */
public final class JsonEasy {

    public static String getString(String json, String key) {
        if (!TextUtils.isEmpty(json) && !TextUtils.isEmpty(key)) {
            try {
                JSONObject obj = JSON.parseObject(json);
                if (obj != null) {
                    String result = obj.getString(key);
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T toObject(String json, Class<T> clss) {
        if (!TextUtils.isEmpty(json)) {
            try {
                T obj = JSON.parseObject(json, clss);
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> List<T> toList(String json, Class<T> clss) {
        if (!TextUtils.isEmpty(json)) {
            try {
                List<T> objList = JSON.parseArray(json, clss);
                return objList;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
