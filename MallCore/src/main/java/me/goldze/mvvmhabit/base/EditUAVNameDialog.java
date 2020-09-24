package me.goldze.mvvmhabit.base;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import me.goldze.mvvmhabit.R;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.constant.Contact;

/**
 * 修改设备名称弹出框
 */
public class EditUAVNameDialog extends Dialog {
    private Context mContext;
    private View mainView;
    private TextView mNextTV, mCancelTV;
    private EditText mNameEdt;
    private OperaterListener listener;

    public EditUAVNameDialog(Context context) {
        super(context, R.style.Theme_Hold_Dialog_Base);
        setCanceledOnTouchOutside(true);
        mContext = context;
        init();
    }

    public EditUAVNameDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        setCanceledOnTouchOutside(true);
        init();
    }

    protected EditUAVNameDialog(Context context, boolean cancelable,
                                OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        setCanceledOnTouchOutside(true);
        init();
    }

    private void init() {
        mainView = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_device_layout, null);
        mCancelTV = mainView.findViewById(R.id.cancel_tv);
        mNameEdt = mainView.findViewById(R.id.pin_code_edt);
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
                if (TextUtils.isEmpty(mNameEdt.getText().toString())) {
                    ToastUtils.showLong("请输入无人机别名");
                    return;
                }
                listener.operater(mNameEdt.getText().toString());
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


    public void setItem(String name, OperaterListener listener) {
        this.listener = listener;
        mNameEdt.setText(name);
    }

    public View getMainView() {
        return mainView;
    }

    public interface OperaterListener {
        void operater(String name);
    }
}
