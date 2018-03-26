package com.act.quzhibo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;

public abstract class TabSlideSameBaseFragment extends Fragment {

    public View view;
    protected MyPagerAdapter mAdapter;
    ArrayList<Fragment> mFragments = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_sliding_tab, null);
        mFragments.addAll(getFragments());
        initUiView();
        return view;
    }

    public abstract ArrayList<String> getTabTitles();

    public abstract ArrayList<Fragment> getFragments();

    OnChangeFragmentListner listner;

    public interface OnChangeFragmentListner {
        void onChangeFragment(String changeText);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
            listner = (OnChangeFragmentListner) context;
    }

    protected void initUiView() {
        ArrayList<String> tabTitles = getTabTitles();
        if (tabTitles == null) {
            return;
        }
        ViewPager pager = (ViewPager) view.findViewById(R.id.viewpager);
        mAdapter = new MyPagerAdapter(getChildFragmentManager(), tabTitles.toArray(new String[tabTitles.size()]));
        pager.setAdapter(mAdapter);
        final SlidingTabLayout tabLayout = (SlidingTabLayout) view.findViewById(R.id.sListLayout);
        tabLayout.setViewPager(pager, tabTitles.toArray(new String[tabTitles.size()]), getActivity(), mFragments);
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

    public abstract String getChangeText();



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
