package com.act.quzhibo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.MyFocusGirl;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyFocusGirlListAdapter extends RecyclerView.Adapter<MyFocusGirlListAdapter.MyViewHolder> {

    private  int screenWidth;
    private Context mContext;
    private ArrayList<MyFocusGirl> datas;
    private OnDeleteListener mListener = null;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public void setDeleteListener(OnDeleteListener listener) {
        mListener = listener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position, MyFocusGirl MyFocusGirl);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public MyFocusGirlListAdapter(Context context, ArrayList<MyFocusGirl> datas, int screenWidth) {
        mContext = context;
        this.datas = datas;
        this.screenWidth = screenWidth;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.photoImg.setLayoutParams(new FrameLayout.LayoutParams((screenWidth / 2)-10, (screenWidth / 2) +175));
        holder.photoImg.setAdjustViewBounds(true);
        holder.photoImg.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(mContext).load(datas.get(position).cover).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.photoImg);
        holder.photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position, datas.get(position));
            }
        });
        holder.deletebtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView photoImg;
        private ImageView deletebtn;

        public MyViewHolder(View view) {
            super(view);

            photoImg = (ImageView) view.findViewById(R.id.videoCover);
            deletebtn = (ImageView) view.findViewById(R.id.deletebtn);
            deletebtn.setVisibility(View.VISIBLE);
        }
    }
}
