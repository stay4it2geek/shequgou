package com.act.quzhibo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.Room;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.MyViewHolder> {

    private final String cataTitle;
    private int screenWidth;
    private Context mContext;
    private String pathPrefix;
    private ArrayList<Room> datas;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public RoomListAdapter(Context context, ArrayList<Room> datas, String pathPrefix, int screenWidth, String cataTitle) {
        mContext = context;
        this.pathPrefix = pathPrefix;
        this.cataTitle = cataTitle;
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
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            holder.photoImg.setLayoutParams(new FrameLayout.LayoutParams((screenWidth / 2) - 10, (screenWidth / 2)-10 ));
            if (datas.get(position).liveStream != null &&Integer.parseInt(datas.get(position).onlineCount) > 1 ) {
                holder.onlineCount.setVisibility(View.VISIBLE);
                holder.isRelax.setVisibility(View.GONE);
                holder.onlineCount.setText(datas.get(position).onlineCount + "人");
            } else {
                holder.isRelax.setVisibility(View.VISIBLE);
                holder.isRelax.setText("休息中");
                holder.onlineCount.setVisibility(View.GONE);
            }

            holder.photoImg.setAdjustViewBounds(true);
            holder.photoImg.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(pathPrefix + datas.get(position).poster_path_400).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.photoImg);//加载网络图片
        }

        holder.nickName.setText(datas.get(position).nickname.replace("k", ""));
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
        private TextView isRelax;
        private ImageView photoImg;
        private TextView nickName;
        private TextView onlineCount;

        public MyViewHolder(View view) {
            super(view);
            photoImg = (ImageView) view.findViewById(R.id.photoImg);
            nickName = (TextView) view.findViewById(R.id.nickName);
            onlineCount = (TextView) view.findViewById(R.id.onlineCount);
            isRelax = (TextView) view.findViewById(R.id.isRelax);
        }
    }

}
