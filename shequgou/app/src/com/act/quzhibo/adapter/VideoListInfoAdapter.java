package com.act.quzhibo.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Data;
import com.act.quzhibo.bean.GirlVideoListInfo;
import com.act.quzhibo.stackblur.StackBlurManager;
import com.act.quzhibo.widget.CircleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;

public class VideoListInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    LayoutInflater mLayoutInflater;
    Activity activity;
    GirlVideoListInfo headerInfo;
    ArrayList<Data> girlVideos;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public VideoListInfoAdapter(Activity activity, GirlVideoListInfo headerInfo, ArrayList<Data> girlVideos) {
        this.headerInfo = headerInfo;
        this.activity = activity;
        this.girlVideos = girlVideos;
        mLayoutInflater = LayoutInflater.from(activity);

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new Item1ViewHolder(mLayoutInflater.inflate(R.layout.girl_video_header_layout, parent, false));
        } else {
            return new Item2ViewHolder(mLayoutInflater.inflate(R.layout.item_video_layout, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof Item1ViewHolder) {
            StaggeredGridLayoutManager.LayoutParams clp = (StaggeredGridLayoutManager.LayoutParams) ((Item1ViewHolder) holder).headerlayout.getLayoutParams();
            clp.setFullSpan(true);
            Glide.with(activity).load(headerInfo.data.avatar.url).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    StackBlurManager stackBlurManager = new StackBlurManager(activity, resource);
                    ((Item1ViewHolder) holder).backGroundImg.setImageBitmap(stackBlurManager.process(28));
                }
            });
            ((Item1ViewHolder) holder).nickName.setText(headerInfo.data.nickname);
            ((Item1ViewHolder) holder).topics.setText(headerInfo.data.topic);
            ((Item1ViewHolder) holder).videoCount.setText("共有"+girlVideos.size()+"个视频");

            Glide.with(activity).load(headerInfo.data.avatar.url).into(((Item1ViewHolder) holder).userImage);
            ((Item1ViewHolder) holder).tagLayout.removeAllViews();
            for (GirlVideoListInfo.Tag tag : headerInfo.data.tags) {
                Button button = new Button(activity);
                button.setText(tag.name);
                button.setTextSize(13f);
                button.setBackgroundColor(Color.parseColor("#" + tag.color));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 105);
                params.setMargins(5, 15, 5, 5);//设置边距
                button.setLayoutParams(params);
                ((Item1ViewHolder) holder).tagLayout.addView(button);

            }

        } else if (holder instanceof Item2ViewHolder) {
            Glide.with(activity).load(girlVideos.get(position - 1).cover).error(R.drawable.error_img).into(((Item2ViewHolder) holder).videoCover);
            ((Item2ViewHolder) holder).videoCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view, position - 1);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return girlVideos.size() + 1;
    }

    class Item1ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout headerlayout;
        private TextView nickName;
        private TextView topics;
        private CircleImageView userImage;
        private ImageView backGroundImg;
        private LinearLayout tagLayout;
        private TextView  videoCount;

        public Item1ViewHolder(View view) {
            super(view);

            headerlayout = (LinearLayout) view.findViewById(R.id.headerlayout);
            nickName = (TextView) view.findViewById(R.id.nickName);
            topics = (TextView) view.findViewById(R.id.topics);
            tagLayout = (LinearLayout) view.findViewById(R.id.tagLayout);
            userImage = (CircleImageView) view.findViewById(R.id.userImage);
            backGroundImg = (ImageView) view.findViewById(R.id.backGroundImg);
            videoCount = (TextView) view.findViewById(R.id.videoCount);

        }
    }

    class Item2ViewHolder extends RecyclerView.ViewHolder {

        private ImageView videoCover;

        public Item2ViewHolder(View view) {
            super(view);
            videoCover = (ImageView) view.findViewById(R.id.videoCover);
        }
    }
}