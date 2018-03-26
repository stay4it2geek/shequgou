package com.act.quzhibo.ui.activity;

import android.Manifest;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.TabEntity;
import com.act.quzhibo.event.ChangeEvent;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.act.quzhibo.common.Constants.HIDE_TAB_VIEW;

@SuppressWarnings("ALL")
public class TabMainActivity extends TabActivity {
    private TabHost tabHost;
    private View mDecorView;
    private String[] mTitles = {"有料", "直播", "社区", "聊天", "我的"};
    private String[] mTitlesSpecial = {"有料", "我的"};

    private int[] mIconUnselectIds = {R.drawable.courses, R.drawable.show, R.drawable.square, R.drawable.chat, R.drawable.mine};

    private int[] mIconSelectIds = {R.drawable.courses_s, R.drawable.show_s, R.drawable.square_s, R.drawable.chat_s, R.drawable.mine_s};

    private int[] mIconUnselectIdsSpecial = {R.drawable.courses, R.drawable.mine};
    private int[] mIconSelectIdsSpecial = {R.drawable.courses_s, R.drawable.mine_s};

    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private CommonTabLayout mTabLayout;
    private StringBuffer catagory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabmain);
        tabHost = TabMainActivity.this.getTabHost();
        Intent showListIntent = new Intent(TabMainActivity.this, ShowerListActivity.class);
        showListIntent.putExtra(Constants.TAB_PLATE_LIST, getIntent().getStringExtra(Constants.TAB_PLATE_LIST));
        if (CommonUtil.getInitData(this,HIDE_TAB_VIEW).equals("false")) {
            tabHost.addTab(tabHost.newTabSpec("有料")
                    .setIndicator(null, null)
                    .setContent(new Intent(TabMainActivity.this, ShowGirlVideoActivity.class)));
            tabHost.addTab(tabHost.newTabSpec("直播")
                    .setIndicator(null, null)
                    .setContent(showListIntent));
            tabHost.addTab(tabHost.newTabSpec("社区")
                    .setIndicator(null, null)
                    .setContent(new Intent(TabMainActivity.this, SquareActivity.class)));
            tabHost.addTab(tabHost.newTabSpec("聊天")
                    .setIndicator(null, null)
                    .setContent(new Intent(TabMainActivity.this, ChatFriendsActivity.class)));
            tabHost.addTab(tabHost.newTabSpec("我的")
                    .setIndicator(null, null)
                    .setContent(new Intent(TabMainActivity.this, MineActivity.class)));
        } else {
            tabHost.addTab(tabHost.newTabSpec("有料")
                    .setIndicator(null, null)
                    .setContent(new Intent(TabMainActivity.this, ShowGirlVideoActivity.class)));
            tabHost.addTab(tabHost.newTabSpec("我的")
                    .setIndicator(null, null)
                    .setContent(new Intent(TabMainActivity.this, MineActivity.class)));
        }
        SetIndexButton();
        mTabLayout.setCurrentTab(0);
        tabHost.setCurrentTab(0);
    }

    public void SetIndexButton() {
        if (CommonUtil.getInitData(this,HIDE_TAB_VIEW).equals("false")) {
            for (int i = 0; i < mTitles.length; i++) {
                mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
            }
        } else {
            for (int i = 0; i < mTitlesSpecial.length; i++) {
                mTabEntities.add(new TabEntity(mTitlesSpecial[i], mIconSelectIdsSpecial[i], mIconUnselectIdsSpecial[i]));
            }
        }
        mTabLayout = (CommonTabLayout) findViewById(R.id.tabMain);
        mTabLayout.setTabData(mTabEntities);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mTabLayout.setCurrentTab(position);
                tabHost.setCurrentTab(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

    }

    private boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(TabMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(TabMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(TabMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(TabMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions.size() != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
