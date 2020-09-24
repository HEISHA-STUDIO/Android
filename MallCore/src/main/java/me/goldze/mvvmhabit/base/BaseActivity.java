package me.goldze.mvvmhabit.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import crossoverone.statuslib.StatusUtil;
import me.goldze.mvvmhabit.R;
import me.goldze.mvvmhabit.base.BaseViewModel.ParameterField;
import me.goldze.mvvmhabit.bus.LogOutRefreshListener;
import me.goldze.mvvmhabit.bus.Messenger;
import me.goldze.mvvmhabit.utils.MaterialDialogUtils;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;


/**
 * 一个拥有DataBinding框架的基Activity
 * 这里根据项目业务可以换成你自己熟悉的BaseActivity, 但是需要继承RxAppCompatActivity,方便LifecycleProvider管理生命周期
 */
public abstract class BaseActivity<V extends ViewDataBinding, VM extends BaseViewModel> extends RxAppCompatActivity implements IBaseView, LogOutRefreshListener {
    public final static String TAG = BaseActivity.class.getSimpleName();
    protected V binding;
    protected VM viewModel;
    private int viewModelId;
    private MaterialDialog dialog;
    public static Activity mContext;
    protected ImageView mLeftIV;
    protected TextView mLeftTV;
    protected TextView mTitleTV;
    protected LinearLayout mMenuLL;
    protected LinearLayout mMenuLL2;
    protected TextView mMenuTxt1;
    protected TextView mMenuTxt2;
    protected ImageView mMenuIV1;
    protected ImageView mMenuIV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
            // 透明状态栏
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        mContext = this;
        setStatusColor();
        setSystemInvadeBlack();
        //页面接受的参数方法
        initParam();
        //私有的初始化Databinding和ViewModel方法
        initViewDataBinding(savedInstanceState);
        //私有的ViewModel与View的契约事件回调逻辑
        registorUIChangeLiveDataCallBack();
        //页面数据初始化方法
        initData();
        //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
        initViewObservable();
        //注册RxBus
        viewModel.registerRxBus();
        BaseAppController.getInstance().regLogOutRefreshListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除Messenger注册
        Messenger.getDefault().unregister(viewModel);
        //解除ViewModel生命周期感应
        getLifecycle().removeObserver(viewModel);
        if (viewModel != null) {
            viewModel.removeRxBus();
        }
        if (binding != null) {
            binding.unbind();
        }
    }

    protected void setStatusColor() {
        StatusUtil.setUseStatusBarColor(this,getResources().getColor(R.color.colorPrimary));
    }

    protected void setSystemInvadeBlack() {
        StatusUtil.setSystemStatus(this, true, false);
    }

    /**
     * 注入绑定
     */
    private void initViewDataBinding(Bundle savedInstanceState) {
        //DataBindingUtil类需要在project的build中配置 dataBinding {enabled true }, 同步后会自动关联android.databinding包
        binding = DataBindingUtil.setContentView(this, initContentView(savedInstanceState));
        viewModelId = initVariableId();
        viewModel = initViewModel();
        if (viewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[1];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) createViewModel(this, modelClass);
        }
        //关联ViewModel
        binding.setVariable(viewModelId, viewModel);
        //让ViewModel拥有View的生命周期感应
        getLifecycle().addObserver(viewModel);
        //注入RxLifecycle生命周期
        viewModel.injectLifecycleProvider(this);
    }

    //刷新布局
    public void refreshLayout() {
        if (viewModel != null) {
            binding.setVariable(viewModelId, viewModel);
        }
    }

    /**
     * =====================================================================
     **/
    //注册ViewModel与View的契约UI回调事件
    protected void registorUIChangeLiveDataCallBack() {
        //加载对话框显示
        viewModel.getUC().getShowDialogEvent().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String title) {
                showDialog(title);
            }
        });
        //加载对话框消失
        viewModel.getUC().getDismissDialogEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                dismissDialog();
            }
        });
        //跳入新页面
        viewModel.getUC().getStartActivityEvent().observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(@Nullable Map<String, Object> params) {
                Class<?> clz = (Class<?>) params.get(ParameterField.CLASS);
                Bundle bundle = (Bundle) params.get(ParameterField.BUNDLE);
                startActivity(clz, bundle);
            }
        });
        //跳入ContainerActivity
        viewModel.getUC().getStartContainerActivityEvent().observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(@Nullable Map<String, Object> params) {
                String canonicalName = (String) params.get(ParameterField.CANONICAL_NAME);
                Bundle bundle = (Bundle) params.get(ParameterField.BUNDLE);
                startContainerActivity(canonicalName, bundle);
            }
        });
        //关闭界面
        viewModel.getUC().getFinishEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                finish();
            }
        });
        //关闭上一层
        viewModel.getUC().getOnBackPressedEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                onBackPressed();
            }
        });
    }

    public void showDialog(String title) {
        if (dialog != null) {
            dialog.show();
        } else {
            MaterialDialog.Builder builder = MaterialDialogUtils.showIndeterminateProgressDialog(this, title, true);
            dialog = builder.show();
        }
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    public void startActivity(Class<?> clz) {
        startActivity(new Intent(this, clz));
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     */
    public void startContainerActivity(String canonicalName) {
        startContainerActivity(canonicalName, null);
    }

    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     * @param bundle        跳转所携带的信息
     */
    public void startContainerActivity(String canonicalName, Bundle bundle) {
        Intent intent = new Intent(this, ContainerActivity.class);
        intent.putExtra(ContainerActivity.FRAGMENT, canonicalName);
        if (bundle != null) {
            intent.putExtra(ContainerActivity.BUNDLE, bundle);
        }
        startActivity(intent);
    }

    /**
     * =====================================================================
     **/
    @Override
    public void initParam() {
    }

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    public abstract int initContentView(Bundle savedInstanceState);

    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    public abstract int initVariableId();

    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    public VM initViewModel() {
        return null;
    }

    @Override
    public void initData() {
    }

    @Override
    public void initViewObservable() {
    }

    /**
     * 创建ViewModel
     *
     * @param cls
     * @param <T>
     * @return
     */
    public <T extends ViewModel> T createViewModel(FragmentActivity activity, Class<T> cls) {
        return ViewModelProviders.of(activity).get(cls);
    }

    public void setTitleBarView() {
        mLeftTV = binding.getRoot().findViewById(R.id.left_tv);
        mLeftIV = binding.getRoot().findViewById(R.id.left_icon);
        mTitleTV = binding.getRoot().findViewById(R.id.title_tv);
        mMenuLL = binding.getRoot().findViewById(R.id.text_ll);
        mMenuLL2 = binding.getRoot().findViewById(R.id.img_ll);
        mMenuTxt1 = binding.getRoot().findViewById(R.id.menu1_tv);
        mMenuTxt2 = binding.getRoot().findViewById(R.id.menu2_tv);
        mMenuIV1 = binding.getRoot().findViewById(R.id.menu1_iv);
        mMenuIV2 = binding.getRoot().findViewById(R.id.menu2_iv);
    }

    public void setTitleBar(Integer leftResId, String title, String menu1Txt,
                            String menu2Txt, boolean titleVis,
                            boolean menu1LLVis, boolean menu2LLVis,
                            boolean menu1TxtVis, boolean menu2TxtVis,
                            boolean menu1IvVis, boolean menu2IvVis,
                            boolean isBack, boolean isBackClick, Integer menu1ResId, Integer menu2ResId) {
        if (isBack) {
            mLeftIV.setImageResource(leftResId);
            if (isBackClick) {
                mLeftIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        }
        mTitleTV.setText(title);
        mTitleTV.setVisibility(titleVis ? View.VISIBLE : View.GONE);

        mMenuLL.setVisibility(menu1LLVis ? View.VISIBLE : View.GONE);
        mMenuLL2.setVisibility(menu2LLVis ? View.VISIBLE : View.GONE);

        mMenuTxt1.setVisibility(menu1TxtVis ? View.VISIBLE : View.GONE);
        mMenuTxt2.setVisibility(menu2TxtVis ? View.VISIBLE : View.GONE);

        mMenuIV1.setVisibility(menu1IvVis ? View.VISIBLE : View.GONE);
        mMenuIV2.setVisibility(menu2IvVis ? View.VISIBLE : View.GONE);

        mMenuTxt1.setText(menu1Txt);
        mMenuTxt2.setText(menu2Txt);
        mMenuIV1.setImageResource(menu1ResId);
        mMenuIV2.setImageResource(menu2ResId);
    }

    public void setTitleBar(boolean isShowLeft, Integer leftResStr, Integer leftResId, String title, String menu1Txt,
                            String menu2Txt, boolean titleVis,
                            boolean menu1LLVis, boolean menu2LLVis,
                            boolean menu1TxtVis, boolean menu2TxtVis,
                            boolean menu1IvVis, boolean menu2IvVis,
                            boolean isBack, boolean isBackClick, Integer menu1ResId, Integer menu2ResId) {

        if (isShowLeft) {
            mLeftTV.setVisibility(View.VISIBLE);
            if (leftResStr !=0) {
                mLeftTV.setText(mContext.getString(leftResStr));
            }
            mLeftTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        if (isBack) {
            mLeftIV.setImageResource(leftResId);
            if (isBackClick) {
                mLeftIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        }
        mTitleTV.setText(title);
        mTitleTV.setVisibility(titleVis ? View.VISIBLE : View.GONE);

        mMenuLL.setVisibility(menu1LLVis ? View.VISIBLE : View.GONE);
        mMenuLL2.setVisibility(menu2LLVis ? View.VISIBLE : View.GONE);

        mMenuTxt1.setVisibility(menu1TxtVis ? View.VISIBLE : View.GONE);
        mMenuTxt2.setVisibility(menu2TxtVis ? View.VISIBLE : View.GONE);

        mMenuIV1.setVisibility(menu1IvVis ? View.VISIBLE : View.GONE);
        mMenuIV2.setVisibility(menu2IvVis ? View.VISIBLE : View.GONE);

        mMenuTxt1.setText(menu1Txt);
        mMenuTxt2.setText(menu2Txt);
        mMenuIV1.setImageResource(menu1ResId);
        mMenuIV2.setImageResource(menu2ResId);
    }

    @Override
    public void reLogin() throws ClassNotFoundException {
        SPUtils.getInstance().put("loginUser", "");
        SPUtils.getInstance().put("token", "");
        viewModel.loginUser.set(null);
        ToastUtils.showLong(R.string.logout_tips);
        AppManager.getAppManager().finishAllActivity();
        startActivity(Class.forName("com.mde.entrust.moudle.aas.LoginActivity"));
    }

    @Override
    public void cancelDialog() {
        dismissDialog();
    }
}
