package me.goldze.mvvmhabit.http;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import io.reactivex.observers.DisposableObserver;
import me.goldze.mvvmhabit.R;
import me.goldze.mvvmhabit.base.BaseAppController;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.ToastStatusUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.Utils;

/**
 * 统一的Code封装处理
 */
public abstract class ApiDisposableObserver<T> extends DisposableObserver<T> {
    public abstract void onResult(T t);

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        BaseAppController.getInstance().postCancelDialogListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!NetworkUtil.isNetworkAvailable(Utils.getContext())) {
            onComplete();
        }
    }

    @Override
    public void onNext(Object o) {
        try {
            BaseResponse baseResponse = (BaseResponse) o;
            onResult((T)baseResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static boolean isJSONValid(String test) {
        try {
            JSONObject.parseObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public static final class CodeRule {
        //业务成功, 正确的操作方式
        static final int CODE_200 = 1;
    }
}