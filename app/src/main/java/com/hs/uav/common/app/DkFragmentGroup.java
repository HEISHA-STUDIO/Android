package com.hs.uav.common.app;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/***
 * Fragment 管理工具类
 * @author tony.liu
 */
public abstract class DkFragmentGroup {
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int containerId;

    public DkFragmentGroup(FragmentManager manager, int containerId) {
        fragmentManager = manager;
        this.containerId = containerId;

    }

    public void onItemSelect(int position, Bundle bundle) {
        if (fragmentManager == null) {
            return;
        }
        transaction = fragmentManager.beginTransaction();
        //hide();
        switchItem(position,bundle);
        transaction.commitAllowingStateLoss();
    }

    /**
     * 选择显示的fragment
     * @param position
     */
    protected abstract void switchItem(int position,Bundle bundle);

    /**
     * 添加fragment
     * @param fragment
     */
    protected void addItem(Fragment fragment) {
        transaction.add(containerId, fragment);
    }
    /**
     * 显示fragment
     * @param fragment
     */
    protected void showItem(Fragment fragment) {
        transaction.replace(containerId,fragment);
    }

    protected abstract void hide();
    /**
     * 隐藏fragment
     * @param fragment
     */
    protected void hideItem(Fragment fragment) {
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }
}
