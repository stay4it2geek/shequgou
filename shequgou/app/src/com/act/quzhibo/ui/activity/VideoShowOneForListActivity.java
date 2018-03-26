package com.act.quzhibo.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Data;
import com.act.quzhibo.ui.fragment.VideoShowOneFragment;
import com.mabeijianxi.smallvideorecord2.StringUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;


public class VideoShowOneForListActivity extends AppCompatActivity {
    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.75f;
    ArrayList<VideoShowOneFragment> videoShowOneFragments = new ArrayList<>();
    @Bind(R.id.verticalviewpager)
    VerticalViewPager verticalviewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showvideoone);
        ButterKnife.bind(this);

    }

    @Bind(R.id.backBtn)
    Button backBtn;

    @OnClick({R.id.backBtn})
    void buttonClick(View view) {
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Data> details = getIntent().getParcelableArrayListExtra("videoDatas");
        if (details != null && details.size() > 0)
            for (int i = 0; i < details.size(); i++) {
                VideoShowOneFragment fragment = new VideoShowOneFragment();
                Data data = details.get(i);
                Bundle bundle = new Bundle();
                bundle.putString("showAvatar", "unshow");
                bundle.putParcelable("videoOneData", data);
                fragment.setArguments(bundle);
                videoShowOneFragments.add(fragment);
            }
        VerticalViewPager verticalViewPager = (VerticalViewPager) findViewById(R.id.verticalviewpager);
        MyForListAdapter dummyAdapter = new MyForListAdapter(getSupportFragmentManager(), videoShowOneFragments);
        verticalViewPager.setAdapter(dummyAdapter);
        verticalViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                ;
                if (position < -1) {
                    view.setAlpha(0);
                } else if (position <= 1) { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                    // Fade the page relative to its size.
                    view.setAlpha(MIN_ALPHA +
                            (scaleFactor - MIN_SCALE) /
                                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));

                } else {
                    view.setAlpha(0);
                }
            }
        });

        verticalviewpager.setCurrentItem(StringUtils.isNotEmpty(getIntent().getStringExtra("position")) ? Integer.parseInt(getIntent().getStringExtra("position")) : 0);
    }
}

class MyForListAdapter extends FragmentPagerAdapter {
    ArrayList<VideoShowOneFragment> videoShowOneFragments;

    public MyForListAdapter(FragmentManager fm, ArrayList<VideoShowOneFragment> videoShowOneFragments) {
        super(fm);
        this.videoShowOneFragments = videoShowOneFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return videoShowOneFragments.get(position);
    }

    @Override
    public int getCount() {
        return videoShowOneFragments.size();
    }


}


