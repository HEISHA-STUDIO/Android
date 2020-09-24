package me.goldze.mvvmhabit.utils;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.goldze.mvvmhabit.R;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.constant.Contact;

/**
 * 显示自定义Toast的工具类
 */
public class ToastStatusUtils {

    private static Toast mToast;

    public static void showToast(String content, int imgId) {
        show(content, imgId);
    }

    public static void showToast(int stringId, int imgId) {
        show(BaseActivity.mContext.getResources().getString(stringId), imgId);
    }

    public static void show(String text, int imgId) {
        if (text == null || TextUtils.isEmpty(text)) {
            return;
        }
        cancelToast();
        View view = LayoutInflater.from(BaseActivity.mContext).inflate(R.layout.toast_layout, null);
        TextView textView = view.findViewById(R.id.tv_message);
        LinearLayout mItemLL = view.findViewById(R.id.item_ll);
        ImageView mIcoIV = view.findViewById(R.id.ico_iv);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Contact.DISPLAYW / 2 + 80, Contact.dip2px(BaseActivity.mContext, 120));
        params.gravity = Gravity.CENTER;
        mItemLL.setLayoutParams(params);

        textView.setText(text);
        mIcoIV.setImageResource(imgId);
        mToast = new Toast(BaseActivity.mContext);
        mToast.setView(view);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void cancelToast() {
        if (null != mToast) {
            mToast.cancel();
        }
    }
}
