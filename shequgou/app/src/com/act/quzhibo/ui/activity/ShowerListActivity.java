package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.PlateCatagory;
import com.act.quzhibo.bean.PlateList;
import com.act.quzhibo.bean.Room;
import com.act.quzhibo.ui.fragment.ShowerListFragment;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;

import java.util.ArrayList;

import static com.act.quzhibo.common.Constants.HIDE_TAB_VIEW;
import static com.act.quzhibo.common.Constants.TAB_PLATE_LIST;

public class ShowerListActivity extends TabSlideSameBaseActivity implements ShowerListFragment.OnCallShowViewListner {

     PlateList plates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        plates = CommonUtil.parseJsonWithGson(getIntent().getStringExtra(TAB_PLATE_LIST), PlateList.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    public ArrayList<String> getTabTitles() {
        ArrayList<String> tabTitles = new ArrayList<>();
        if (plates == null) {
            return new ArrayList<>();
        }
        String hideShowPlates =CommonUtil.getInitData(this,Constants.HIDE_SHOW_PLATES);
            for (PlateCatagory plateCatagory : plates.plateList) {
            if (hideShowPlates.contains(plateCatagory.titleName)) {
                continue;
            } else {
                tabTitles.add(plateCatagory.titleName);
            }
        }
        return tabTitles;
    }

    @Override
    public ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> mFragments = new ArrayList<>();
        String hideShowPlates =CommonUtil.getInitData(this,Constants.HIDE_SHOW_PLATES);
        for (PlateCatagory plateCatagory : plates.plateList) {
            if (hideShowPlates.contains(plateCatagory.titleName)) {
                continue;
            } else {
                ShowerListFragment fragment = new ShowerListFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.CATEGORY_ID, plateCatagory.titleId);
                bundle.putString(Constants.CATEGORY_TITLE, plateCatagory.titleName);
                fragment.setArguments(bundle);
                mFragments.add(fragment);
            }
        }
        return mFragments;
    }

    @Override
    public String getDialogTitle() {
        return "哥哥姐姐们，再看一会呗";
    }


    @Override
    public void onShowVideo(Room room) {
        Intent intent;

        if (room.liveStream != null) {
            intent = new Intent(ShowerListActivity.this, VideoPlayerActivity.class);
            intent.putExtra("showFullScreen", true);
        } else {
            intent = new Intent(ShowerListActivity.this, ShowerInfoActivity.class);
            intent.putExtra("FromShowListActivity", true);
            ToastUtil.showToast(ShowerListActivity.this, "该主播未直播哦");

        }
        intent.putExtra("room", room);
        startActivity(intent);
    }

}
