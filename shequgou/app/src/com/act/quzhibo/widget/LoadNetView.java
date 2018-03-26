package com.act.quzhibo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
public class LoadNetView extends LinearLayout {

     LinearLayout photoalbum_layout;
     LinearLayout vipNulllayout;
     TextView noDownloadedDataText;
     TextView noDownloadingDataText;
     Button reloadbutton;
     Button buybutton;
     LinearLayout loadlayout;
     LinearLayout noDataText;
     LinearLayout reloadlayout;
     LinearLayout video_album_layout;
     Button loadDataButton;

    public LoadNetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.load_net_view, this);
        noDownloadedDataText = (TextView) findViewById(R.id.noDownloadedDataText);
        reloadlayout = (LinearLayout) findViewById(R.id.reloadlayout);
        video_album_layout = (LinearLayout) findViewById(R.id.video_album_layout);
        reloadbutton = (Button) findViewById(R.id.relaodbutton);
        buybutton = (Button) findViewById(R.id.buybutton);
        loadDataButton = (Button) findViewById(R.id.loadDataButton);
        photoalbum_layout = (LinearLayout) findViewById(R.id.photoalbum_layout);
        noDownloadingDataText = (TextView) findViewById(R.id.noDownloadingDataText);
        noDataText = (LinearLayout) findViewById(R.id.noDataText);
        loadlayout = (LinearLayout) findViewById(R.id.loadlayout);
        vipNulllayout = (LinearLayout) findViewById(R.id.VipNulllayout);
        video_album_layout = (LinearLayout) findViewById(R.id.video_album_layout);
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;     //截断事件的传递
            }
        });
    }

    public void setReloadButtonListener(OnClickListener listener) {
        reloadbutton.setOnClickListener(listener);
    }

    public void setLoadButtonListener(OnClickListener listener) {
        loadDataButton.setOnClickListener(listener);
    }


    public void setBuyButtonListener(OnClickListener listener) {
        buybutton.setOnClickListener(listener);
    }

    public void setlayoutVisily(int loadType) {
        if (loadType == Constants.LOAD) {
            noDataText.setVisibility(GONE);
            reloadlayout.setVisibility(View.GONE);
            loadlayout.setVisibility(View.VISIBLE);
        } else if (loadType == Constants.RELOAD) {
            reloadlayout.setVisibility(View.VISIBLE);
            loadlayout.setVisibility(View.GONE);
        } else if (loadType == Constants.BUY_VIP) {
            loadlayout.setVisibility(View.GONE);
            vipNulllayout.setVisibility(View.VISIBLE);
        } else if (loadType == Integer.parseInt(Constants.PHOTO_ALBUM)) {
            loadlayout.setVisibility(View.GONE);
            photoalbum_layout.setVisibility(View.VISIBLE);
        } else if (loadType == Integer.parseInt(Constants.VIDEO_ALBUM)) {
            loadlayout.setVisibility(View.GONE);
            video_album_layout.setVisibility(View.VISIBLE);
        } else if (loadType == Constants.NO_DOWN_DATA) {
            loadlayout.setVisibility(View.GONE);
            noDownloadedDataText.setVisibility(View.VISIBLE);
        } else if (loadType == Constants.NO_DOWNING_DATA) {
            loadlayout.setVisibility(View.GONE);
            noDownloadingDataText.setVisibility(View.VISIBLE);
        } else if (loadType == Constants.NO_DATA) {
            loadlayout.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
        }
    }

}
