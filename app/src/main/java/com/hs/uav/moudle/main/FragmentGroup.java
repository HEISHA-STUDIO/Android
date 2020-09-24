package com.hs.uav.moudle.main;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.hs.uav.common.app.DkFragmentGroup;
import com.hs.uav.moudle.fragment.MoveGaodeMapFragment;

import me.goldze.mvvmhabit.utils.KLog;

/****
 * 管理Fragment碎片群组
 * @author tony.liu
 */
public class FragmentGroup extends DkFragmentGroup {
    public VideoManagerFragment vmf;
    public GaodeMapFragment gmf;
    public MoveGaodeMapFragment mgf;

    public FragmentGroup(FragmentManager manager, int containerId) {
        super(manager, containerId);
    }

    @Override
    protected void switchItem(int position, Bundle bundle) {
        switch (position) {
            case 0:
                if (vmf == null) {
                    vmf = new VideoManagerFragment();
                    addItem(vmf);
                } else {
                    showItem(vmf);
                }
                break;
            case 1:
                if (gmf == null) {
                    gmf = new GaodeMapFragment();
                    addItem(gmf);
                } else {
                    showItem(gmf);
                }
                break;
            case 2:
                if (mgf == null) {
                    mgf = new MoveGaodeMapFragment();
                    addItem(mgf);
                } else {
                    showItem(mgf);
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void hide() {
        if (vmf != null) {
            hideItem(vmf);
        }

        if (gmf != null) {
            hideItem(gmf);
        }

        if (mgf != null) {
            hideItem(mgf);
        }
    }
}
