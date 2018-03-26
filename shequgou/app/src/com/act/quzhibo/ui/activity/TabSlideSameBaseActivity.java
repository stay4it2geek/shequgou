package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.ui.fragment.BackHandledFragment;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public abstract class TabSlideSameBaseActivity extends FragmentActivity implements BackHandledFragment.BackHandledInterface {

    protected MyPagerAdapter mAdapter;
    protected BackHandledFragment mBackHandedFragment;
    ArrayList<Fragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_tab);
        mFragments.addAll(getFragments());
        initUiView();
    }

    public abstract ArrayList<String> getTabTitles();

    public abstract ArrayList<Fragment> getFragments();

    public abstract String getDialogTitle();

    protected void initUiView() {
        ArrayList<String> tabTitles = getTabTitles();
        ArrayList<Fragment> fragments = getFragments();

        if (tabTitles == null) {
            return;
        }
        if (fragments == null) {
            return;
        }
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new MyPagerAdapter(getSupportFragmentManager(), tabTitles.toArray(new String[tabTitles.size()]));
        pager.setAdapter(mAdapter);
        final SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.sListLayout);
        tabLayout.setViewPager(pager, tabTitles.toArray(new String[tabTitles.size()]), this, mFragments);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        pager.setCurrentItem(0);
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }

    @Override
    public void onBackPressed() {
        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    FragmentDialog.newInstance(false, getDialogTitle(), "真的要离开人家吗？", "继续浏览", "有事要忙", "", "", false, new FragmentDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegtiveClick(final Dialog dialog) {
                            RootUser user = new RootUser();
                            if (user != null) {
                                user.lastLoginTime = System.currentTimeMillis() + "";
                                user.update( BmobUser.getCurrentUser(RootUser.class).getObjectId(), new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            dialog.dismiss();
                                            finish();
                                            ToastUtil.showToast(TabSlideSameBaseActivity.this, "退出时间" + System.currentTimeMillis());
                                        }else{
                                            finish();
                                        }
                                    }
                                });
                            }else{
                                finish();
                            }

                        }
                    }).show(getSupportFragmentManager(), "");
                } else {
                    getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private String[] tabTitles;

        public MyPagerAdapter(FragmentManager fm, String[] tabTitles) {
            super(fm);
            this.tabTitles = tabTitles;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
}
