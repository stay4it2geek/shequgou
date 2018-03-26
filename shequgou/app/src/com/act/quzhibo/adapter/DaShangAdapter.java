package com.act.quzhibo.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.stackblur.StackBlurManager;
import com.act.quzhibo.widget.CircleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;

public class DaShangAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    String nickName;
    String avatarUrl;
    LayoutInflater mLayoutInflater;
    Activity activity;
    ArrayList<String> dashangNumList = new ArrayList<>();
    private int mSelectedItem=-1;

    public DaShangAdapter(Activity activity, String avatarUrl, String nickName) {
        this.activity = activity;
        this.avatarUrl = avatarUrl;
        this.nickName = nickName;

        mLayoutInflater = LayoutInflater.from(activity);
        dashangNumList.add("5");
        dashangNumList.add("10");
        dashangNumList.add("20");
        dashangNumList.add("30");
        dashangNumList.add("50");
        dashangNumList.add("100");
        dashangNumList.add("150");
        dashangNumList.add("200");
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
            return new Item1ViewHolder(mLayoutInflater.inflate(R.layout.dashang, parent, false));
        } else {
            return new Item2ViewHolder(mLayoutInflater.inflate(R.layout.dashangmoneylist, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof Item1ViewHolder) {
            StaggeredGridLayoutManager.LayoutParams clp = (StaggeredGridLayoutManager.LayoutParams) ((Item1ViewHolder) holder).dashangframelayout.getLayoutParams();
            clp.setFullSpan(true);
            Glide.with(activity).load(avatarUrl).into(((Item1ViewHolder) holder).girlAvatar);
            ((Item1ViewHolder) holder).nickName.setText(nickName);
            Glide.with(activity).load(avatarUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    StackBlurManager stackBlurManager = new StackBlurManager(activity, resource);
                    ((Item1ViewHolder) holder).backGroundImg.setImageBitmap(stackBlurManager.process(28));
                }
            });
        } else if (holder instanceof Item2ViewHolder) {
            ((Item2ViewHolder) holder).dashangNumRabtn.setText("打赏"+dashangNumList.get(position-1)+"元");

            ((Item2ViewHolder) holder).dashangNumRabtn.setChecked(position==mSelectedItem);
        }
    }

    @Override
    public int getItemCount() {
        return dashangNumList.size() + 1;
    }

    class Item1ViewHolder extends RecyclerView.ViewHolder {
        private TextView nickName;
        private ImageView backGroundImg;
        private FrameLayout dashangframelayout;
        private CircleImageView girlAvatar;

        public Item1ViewHolder(View view) {
            super(view);
            backGroundImg = (ImageView) view.findViewById(R.id.backGroundImg);
            girlAvatar = (CircleImageView) view.findViewById(R.id.girlAvatar);
            nickName = (TextView) view.findViewById(R.id.nickName);
            dashangframelayout = (FrameLayout) view.findViewById(R.id.dashangframelayout);
        }
    }
    public interface onItemClickListener {
        void onItemClick(View view, int position,String money);
    }

    private onItemClickListener mListener = null;

    public void setOnItemClickListener(onItemClickListener listener) {
        mListener = listener;
    }
    class Item2ViewHolder extends RecyclerView.ViewHolder {
        public RadioButton dashangNumRabtn;
        public Item2ViewHolder( View view) {
            super(view);
            dashangNumRabtn = (RadioButton) view.findViewById(R.id.dashangNumRabtn);
            View.OnClickListener clickListener = new View.OnClickListener() {

                @Override

                public void onClick(View v) {
                    mListener.onItemClick(v, getAdapterPosition()-1,dashangNumList.get(getAdapterPosition()-2));
                    mSelectedItem = getAdapterPosition()-1;
                    notifyDataSetChanged();
                }

            };
            dashangNumRabtn.setOnClickListener(clickListener);

        }

    }
}