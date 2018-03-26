package com.act.quzhibo.ui.activity;

import android.support.v4.app.FragmentActivity;
import butterknife.ButterKnife;

public abstract class BaseActivity extends FragmentActivity  {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }


}
