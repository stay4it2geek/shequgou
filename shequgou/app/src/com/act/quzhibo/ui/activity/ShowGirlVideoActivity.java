package com.act.quzhibo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.act.quzhibo.bean.HomeTag;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.ui.fragment.GirlShowListFagment;

import java.util.ArrayList;


public class ShowGirlVideoActivity extends TabSlideSameBaseActivity {
    @Override
    public ArrayList<String> getTabTitles() {
        ArrayList<String> tabTitles = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            if (i == 1) {
                continue;
            }
            tabTitles.add("推荐");
            tabTitles.add("自拍");
            tabTitles.add("跳舞");
            tabTitles.add("健身");
            tabTitles.add("搞笑");
            tabTitles.add("唱歌");
        }
        return tabTitles;
    }

    @Override
    public ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> mFragments = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            if (i == 1) {
                continue;
            }
            GirlShowListFagment fragment = new GirlShowListFagment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.CATEGORY_ID, i+"");
            fragment.setArguments(bundle);
            mFragments.add(fragment);
        }
        return mFragments;
    }

    @Override
    public String getDialogTitle() {
        return "还有很多美女没翻牌呢";
    }
}
