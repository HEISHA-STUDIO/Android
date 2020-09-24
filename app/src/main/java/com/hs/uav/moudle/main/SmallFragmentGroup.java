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
public class SmallFragmentGroup extends DkFragmentGroup {
    public VideoManagerFragment VF;
    public MoveGaodeMapFragment MGDF;

    public SmallFragmentGroup(FragmentManager manager, int containerId) {
        super(manager, containerId);
    }

    @Override
    protected void switchItem(int position, Bundle bundle) {
        switch (position) {
            case 0:
                if (MGDF == null) {
                    MGDF = new MoveGaodeMapFragment();
                    addItem(MGDF);
                } else {
                    showItem(MGDF);
                }
                break;
            case 1:
                if (VF == null) {
                    VF = new VideoManagerFragment();
                    addItem(VF);
                } else {
                    showItem(VF);
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void hide() {
        if (VF != null) {
            hideItem(VF);
        }
        if (MGDF != null) {
            hideItem(MGDF);
        }
    }
}
