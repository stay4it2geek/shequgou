package com.act.quzhibo.util;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.VideoListInfoAdapter;
import com.act.quzhibo.bean.TabEntity;
import com.act.quzhibo.i.OnQueryDataListner;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;

import static android.support.design.R.attr.layoutManager;

public class ViewDataUtil {

    public static void setLayManager(final OnQueryDataListner listner, Context context, final XRecyclerView rv, int count, boolean refresh, boolean loadmore) {
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(context, count);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(gridLayoutManager);
        rv.setPullRefreshEnabled(refresh);
        rv.setLoadingMoreEnabled(loadmore);
        rv.setLoadingMoreProgressStyle(R.style.Small);
        rv.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rv.setNoMore(false);
                        rv.setLoadingMoreEnabled(true);
                        if(listner!=null){
                        listner.onRefresh();}
                        rv.refreshComplete();
                    }
                }, 1000);
            }
            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(listner!=null){
                            listner.onLoadMore();}
                    }
                }, 1000);
            }
        });
    }

    public static void setLayManagerSpecial(StaggeredGridLayoutManager gridLayoutManager , final OnQueryDataListner listner, Context context, final XRecyclerView rv, int count, boolean refresh, boolean loadmore) {
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setPullRefreshEnabled(refresh);
        rv.setLoadingMoreEnabled(loadmore);
        rv.setLoadingMoreProgressStyle(R.style.Small);
        rv.setLayoutManager(gridLayoutManager);
        rv.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rv.setNoMore(false);
                        rv.setLoadingMoreEnabled(true);
                        listner.onRefresh();
                        rv.refreshComplete();
                    }
                }, 1000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listner.onLoadMore();

                    }
                }, 1000);
            }
        });
    }


    public static void initView(String[] mTitles, View view, final ViewPager viewPager, FragmentPagerAdapter mAdapter) {
        final CommonTabLayout layout;
        viewPager.setAdapter(mAdapter);
        layout = (CommonTabLayout) view.findViewById(R.id.layout);
        layout.setVisibility(View.VISIBLE);
        ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], 0, 0));
        }
        layout.setTabData(mTabEntities);
        layout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                layout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOffscreenPageLimit(mTabEntities.size());
        viewPager.setCurrentItem(0);
    }

    public static void switchFragment(Fragment fragment, int layoutId, FragmentActivity activity) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        if (fragment != null && !fragment.isAdded()) {
            transaction.add(layoutId, fragment);
        }
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }
}
