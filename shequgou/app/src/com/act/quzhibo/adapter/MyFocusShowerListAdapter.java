package com.act.quzhibo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.MyFocusShower;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyFocusShowerListAdapter extends RecyclerView.Adapter<MyFocusShowerListAdapter.MyViewHolder> {

    private  int screenWidth;
    private Context mContext;
    private ArrayList<MyFocusShower> datas;
    private OnDeleteListener mListener = null;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public void setDeleteListener(OnDeleteListener listener) {
        mListener = listener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position, MyFocusShower myFocusShower);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public MyFocusShowerListAdapter(Context context, ArrayList<MyFocusShower> datas, int screenWidth) {
        mContext = context;
        this.datas = datas;
        this.screenWidth = screenWidth;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_shower_room_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.photoImg.setLayoutParams(new FrameLayout.LayoutParams((screenWidth / 2 - 10), (screenWidth / 2) - 5));
        holder.photoImg.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(mContext).load(datas.get(position).portrait_path_1280).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.photoImg);
        String nick = TextUtils.isEmpty(datas.get(position).nickname)?"":datas.get(position).nickname.replaceAll("\r|\n", "");
        holder.nickName.setText(nick);
        holder.commonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position, datas.get(position));
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {

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
        private RelativeLayout commonLayout;
        private ImageView photoImg;
        private TextView nickName;
        private TextView delete;

        public MyViewHolder(View view) {
            super(view);
            commonLayout = (RelativeLayout) view.findViewById(R.id.commonLayout);
            photoImg = (ImageView) view.findViewById(R.id.photoImg);
            nickName = (TextView) view.findViewById(R.id.nickName);
            delete = (TextView) view.findViewById(R.id.delete);
            delete.setVisibility(View.VISIBLE);
        }
    }
}
