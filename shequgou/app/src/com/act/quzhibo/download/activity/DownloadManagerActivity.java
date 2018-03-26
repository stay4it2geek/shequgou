package com.act.quzhibo.download.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.act.quzhibo.common.Constants;
import com.act.quzhibo.download.fragment.DownloadedFragment;
import com.act.quzhibo.download.fragment.DownloadingFragment;
import com.act.quzhibo.ui.activity.TabSlideDifferentBaseActivity;

import java.util.ArrayList;

public class DownloadManagerActivity extends TabSlideDifferentBaseActivity {

    @Override
    protected boolean getDetailContentViewFlag() {
        return false;
    }

    @Override
    protected boolean isNeedShowBackDialog() {
        return false;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{"下载中", " 已下载"};
    }

    @Override
    protected ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        DownloadingFragment downloadingFragment=new DownloadingFragment();
        DownloadedFragment downloadedFragment=new DownloadedFragment();
        Bundle bundle=new Bundle();
        bundle.putString(Constants.DOWN_LOAD_TYPE,getIntent().getStringExtra(Constants.DOWN_LOAD_TYPE));
        downloadingFragment.setArguments(bundle);
        downloadedFragment.setArguments(bundle);
        fragments.add(downloadingFragment);
        fragments.add(downloadedFragment);

        return fragments;
    }



}
