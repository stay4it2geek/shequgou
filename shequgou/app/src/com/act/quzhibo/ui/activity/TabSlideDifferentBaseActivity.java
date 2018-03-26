package com.act.quzhibo.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.ui.fragment.BackHandledFragment;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.FragmentDialog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public abstract class TabSlideDifferentBaseActivity extends FragmentActivity implements BackHandledFragment.BackHandledInterface {
    protected MyPagerAdapter mAdapter;
    private BackHandledFragment mBackHandedFragment;
    ArrayList<Fragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    protected abstract boolean getDetailContentViewFlag();

    protected void initView() {
        if (!getDetailContentViewFlag()) {
            setContentView(R.layout.activity_common_tab);
        }
        mFragments = getFragments();
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        ViewDataUtil.initView(getTitles(), getWindow().getDecorView(), (ViewPager) findViewById(R.id.viewpager), mAdapter);
    }

    protected void setPage(int positon) {
        ((ViewPager) findViewById(R.id.viewpager)).setCurrentItem(positon);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getTitles()[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }


    @Override
    public void onBackPressed() {
        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (isNeedShowBackDialog()) {
                    FragmentDialog.newInstance(false, "客官再看一会儿呗", "还是留下来再看看吧", "再欣赏下", "有事要忙", "", "", false, new FragmentDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegtiveClick(final Dialog dialog) {
                            RootUser user = new RootUser();
                            if (user != null) {
                                user.lastLoginTime = System.currentTimeMillis() + "";
                                user.update(BmobUser.getCurrentUser(RootUser.class).getObjectId(), new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            dialog.dismiss();
                                            finish();
                                            ToastUtil.showToast(TabSlideDifferentBaseActivity.this, "退出时间" + System.currentTimeMillis());
                                        }else {
                                            finish();
                                        }
                                    }
                                });
                            }else {
                                finish();
                            }

                        }
                    }).show(getSupportFragmentManager(), "");
                } else {
                    finish();
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }


    protected abstract boolean isNeedShowBackDialog();

    protected abstract String[] getTitles();

    protected abstract ArrayList<Fragment> getFragments();

    public void startActivity(Class<? extends Activity> target, Bundle bundle, boolean finish) {
        Intent intent = new Intent();
        intent.setClass(this, target);
        if (bundle != null)
            intent.putExtra(getPackageName(), bundle);
        startActivity(intent);
        if (finish)
            finish();
    }

    public Bundle getBundle() {
        if (getIntent() != null && getIntent().hasExtra(getPackageName()))
            return getIntent().getBundleExtra(getPackageName());
        else
            return null;
    }

}
