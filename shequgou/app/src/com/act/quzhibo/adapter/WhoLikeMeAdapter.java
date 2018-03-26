package com.act.quzhibo.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.data_db.VirtualUserInfo;
import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.ui.activity.InfoNearPersonActivity;
import com.act.quzhibo.ui.activity.IntersetPersonPostListActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.Random;

public class WhoLikeMeAdapter extends RecyclerView.Adapter<WhoLikeMeAdapter.MyViewHolder> {
    private Activity activity;
    private ArrayList<InterestSubPerson> datas;

    public WhoLikeMeAdapter(Activity context, ArrayList<InterestSubPerson> datas) {
        activity = context;
        this.datas = datas;
    }

    @Override
    public WhoLikeMeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_who_like_me, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final WhoLikeMeAdapter.MyViewHolder holder, final int position) {
        final InterestSubPerson user = datas.get(position);
        holder.nickName.setText(user.username);
        Random random = new Random();
        int minMinu = 30;
        int maxSeeMinu = 1000;
        int maxOnlineMinu = 900;
        int seeTimeMinu = random.nextInt(maxSeeMinu) + random.nextInt(minMinu);
        int onlineTimeMinu = random.nextInt(maxOnlineMinu) + random.nextInt(minMinu);

        String randomAge = (random.nextInt(10) + 20) + "";

        VirtualUserInfoDao dao = VirtualUserInfoDao.getInstance(activity);
        VirtualUserInfo dataUser = dao.query(user.userId);

        if (dataUser != null) {
            randomAge = dataUser.userAge != null ? dataUser.userAge : "";
            seeTimeMinu = !TextUtils.isEmpty(dataUser.onlineTime) ? Integer.parseInt(dataUser.onlineTime) : 0;
            onlineTimeMinu = !TextUtils.isEmpty(dataUser.onlineTime) ? Integer.parseInt(dataUser.onlineTime) : 0;

        } else {
            VirtualUserInfo user_ = new VirtualUserInfo();
            user_.userAge = randomAge;
            user_.userId = user.userId;
            user_.onlineTime = onlineTimeMinu+"";
            user_.seeMeTime = seeTimeMinu + "";
            dao.add(user_);
        }
        if (seeTimeMinu != 0) {
            if (seeTimeMinu % 60 == 0) {
                holder.seeMeTime.setText(seeTimeMinu / 60 + "小时前看过你");
            } else {
                holder.seeMeTime.setText((seeTimeMinu - (seeTimeMinu % 60)) / 60 + "小时" + seeTimeMinu % 60 + "分钟前看过你");
            }
        }

        if (datas.get(position).user != null && !TextUtils.isEmpty(datas.get(position).user.sex)) {
            holder.sexAndAge.setVisibility(View.VISIBLE);
            holder.sexAndAge.setText(datas.get(position).user.sex.equals("女") ? "女 " + randomAge + "岁" : "男 " + randomAge + "岁");
        }else{
            holder.sexAndAge.setVisibility(View.VISIBLE);
            holder.sexAndAge.setText(randomAge + "岁");
        }


        holder.arealocation.setText("  距离你" + datas.get(position).distance + "公里");
        holder.who_see_me_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (user.photoUrl != null) {
                    intent.putExtra(Constants.COMMON_USER_ID, datas.get(position).userId);
                    intent.setClass(activity, IntersetPersonPostListActivity.class);
                } else {
                    intent.putExtra(Constants.NEAR_USER, datas.get(position));
                    intent.setClass(activity, InfoNearPersonActivity.class);
                }
                activity.startActivity(intent);
            }
        });


        String photoUrl = "";
        if (user.photoUrl != null) {
            photoUrl = user.photoUrl;
        } else {
            photoUrl = user.headUrl;
        }

        Glide.with(activity).load(photoUrl).asBitmap().placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                holder.photoImg.setBackgroundDrawable(new BitmapDrawable(resource));
            }

            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                holder.photoImg.setBackgroundDrawable(placeholder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nickName;
        private ImageView photoImg;
        private TextView arealocation;
        private TextView seeMeTime;
        private TextView sexAndAge;
        private RelativeLayout who_see_me_layout;

        public MyViewHolder(View view) {
            super(view);
            photoImg = (ImageView) view.findViewById(R.id.photoImg);
            nickName = (TextView) view.findViewById(R.id.nick);
            seeMeTime = (TextView) view.findViewById(R.id.seeMeTime);
            arealocation = (TextView) view.findViewById(R.id.location);
            sexAndAge = (TextView) view.findViewById(R.id.sexAndAge);
            who_see_me_layout = (RelativeLayout) view.findViewById(R.id.who_see_me_layout);
            arealocation.setVisibility(View.VISIBLE);
        }
    }

}