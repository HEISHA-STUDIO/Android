package me.goldze.mvvmhabit.http;

/**
 * @Description 请求异常抛出
 */

public class ResponseThrowable extends Exception {
    public int code;
    public String message;

    public ResponseThrowable(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }
}
