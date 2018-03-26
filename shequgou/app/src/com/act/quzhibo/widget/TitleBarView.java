package com.act.quzhibo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;


public class TitleBarView extends FrameLayout {

    private TextView barTitle;
    private RelativeLayout backbuttonLayout;

    public TitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.title_bar_view, this);

        // 获取控件
        backbuttonLayout = (RelativeLayout) findViewById(R.id.backbuttonLayout);

        barTitle = (TextView) findViewById(R.id.barTitle);


    }

    public void setBarTitle(String title) {
        barTitle.setText(title);
    }

    public void setBackButtonListener(OnClickListener listener) {
        backbuttonLayout.setOnClickListener(listener);
    }

    public void setBackButtonVisi(int visi) {
        backbuttonLayout.setVisibility(visi);
    }

}
