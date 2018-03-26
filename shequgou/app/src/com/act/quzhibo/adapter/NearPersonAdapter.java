package com.act.quzhibo.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.ui.activity.InfoNearPersonActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class NearPersonAdapter extends RecyclerView.Adapter<NearPersonAdapter.MyViewHolder> {
    private Activity activity;
    private ArrayList<InterestSubPerson> datas;

    public NearPersonAdapter(Activity context, ArrayList<InterestSubPerson> datas) {
        activity = context;
        this.datas = datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.common_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final InterestSubPerson user = datas.get(position);
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        holder.photoImg.setLayoutParams(new FrameLayout.LayoutParams((size.x / 2) - 5, (size.x / 2) - 5));
        holder.nickName.setText(user.username);
        holder.introduce.setText(user.introduce);
        holder.nearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Constants.NEAR_USER, user);
                intent.setClass(activity, InfoNearPersonActivity.class);
                activity.startActivity(intent);
            }
        });
        holder.arealocation.setText("距离你" + user.distance + "千米");
        Glide.with(activity).load(user.absCoverPic).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.photoImg);
    }


    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nickName;
        private ImageView photoImg;
        private TextView arealocation;
        private TextView introduce;
        private RelativeLayout nearLayout;

        public MyViewHolder(View view) {
            super(view);
            photoImg = (ImageView) view.findViewById(R.id.photoImg);
            nickName = (TextView) view.findViewById(R.id.nickName);
            arealocation = (TextView) view.findViewById(R.id.location);
            nearLayout = (RelativeLayout) view.findViewById(R.id.commonLayout);
            introduce = (TextView) view.findViewById(R.id.introduce);
        }
    }
}