package com.act.quzhibo.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.widget.TitleBarView;
import com.act.quzhibo.widget.UPMarqueeView;

import java.util.ArrayList;


public class GetVipPayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_vip_pay);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle("成为VIP会员");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetVipPayActivity.this.finish();
            }
        });
        ArrayList<View> views = new ArrayList<>();
        UPMarqueeView tipsView = (UPMarqueeView) findViewById(R.id.marqueeView);
        for (int i = 0; i < 6; i++) {
            LinearLayout moreView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.tips_item_view, null);
            TextView tips = (TextView) moreView.findViewById(R.id.tips);
            views.add(tips);
        }
        tipsView.setViews(views);
    }
}
