package com.act.quzhibo.ui.activity;

import android.support.v4.app.Fragment;

import com.act.quzhibo.ui.fragment.InterestPlatesFragment;
import com.act.quzhibo.ui.fragment.NearFragment;

import java.util.ArrayList;

public class SquareActivity extends TabSlideDifferentBaseActivity implements InterestPlatesFragment.OnNearByListner {

    @Override
    protected boolean getDetailContentViewFlag() {
        return false;
    }

    @Override
    protected boolean isNeedShowBackDialog() {
        return true;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{"qing", "jin"};
    }

    @Override
    protected ArrayList<Fragment> getFragments() {
        mFragments.add(new InterestPlatesFragment());
        mFragments.add(new NearFragment());
        return mFragments;
    }


    public String pid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public void onNear() {
        setPage(1);
    }


}
