package com.act.quzhibo.adapter;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Data;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GirListAdapter extends RecyclerView.Adapter<GirListAdapter.MyViewHolder> {

     Point sizePoint;
     Context mContext;
     ArrayList<Data> datas;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public GirListAdapter(Context context, ArrayList<Data> datas, Point sizePoint) {
        mContext = context;
        this.datas = datas;
        this.sizePoint = sizePoint;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            holder.photoImg.setLayoutParams(new FrameLayout.LayoutParams((sizePoint.x / 2), ((sizePoint.y-120)/2)));
            holder.photoImg.setAdjustViewBounds(true);
            holder.photoImg.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(datas.get(position).cover).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.photoImg);//加载网络图片
        }

        holder.photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView photoImg;
        public MyViewHolder(View view) {
            super(view);
            photoImg = (ImageView) view.findViewById(R.id.videoCover);
        }
    }

}
