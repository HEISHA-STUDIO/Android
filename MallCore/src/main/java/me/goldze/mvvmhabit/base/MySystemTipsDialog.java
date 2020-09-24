package me.goldze.mvvmhabit.base;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import me.goldze.mvvmhabit.R;
import me.goldze.mvvmhabit.utils.constant.Contact;

/**
 * 我的系统提示弹出框
 */
public class MySystemTipsDialog extends Dialog {
    private Context mContext;
    private View mainView;
    private TextView mContentTV, mNextTV, mCancelTV;
    private OperaterListener listener;

    public MySystemTipsDialog(Context context) {
        super(context, R.style.Theme_Hold_Dialog_Base);
        setCanceledOnTouchOutside(true);
        mContext = context;
        init();
    }

    public MySystemTipsDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        setCanceledOnTouchOutside(true);
        init();
    }

    protected MySystemTipsDialog(Context context, boolean cancelable,
                                 OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        setCanceledOnTouchOutside(true);
        init();
    }

    private void init() {
        mainView = LayoutInflater.from(mContext).inflate(R.layout.dialog_system_tips_layout, null);
        mCancelTV = mainView.findViewById(R.id.cancel_tv);
        mContentTV = mainView.findViewById(R.id.content_tv);
        mNextTV = mainView.findViewById(R.id.sure_tv);
        mCancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mNextTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.operater();
                dismiss();
            }
        });
        setContentView(mainView);
    }

    @Override
    public void show() {
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        super.show();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = Contact.DISPLAYW;
        window.setAttributes(lp);
    }


    public void setItem(String content, OperaterListener listener) {
        this.listener = listener;
        mContentTV.setText(content);
        if (TextUtils.isEmpty(content)) {
            mContentTV.setVisibility(View.GONE);
        }
    }

    public View getMainView() {
        return mainView;
    }

    public interface OperaterListener {
        void operater();
    }
}
