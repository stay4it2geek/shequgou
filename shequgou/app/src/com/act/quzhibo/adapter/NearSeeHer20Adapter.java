package com.act.quzhibo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.NearSeeHerEntity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class NearSeeHer20Adapter extends BaseAdapter {
    ArrayList<NearSeeHerEntity> members;
    Context context;
    private boolean isShowVideo;

    public NearSeeHer20Adapter(ArrayList<NearSeeHerEntity> members, Context context, boolean isShowVideo) {
        this.members = members;
        this.context = context;
        this.isShowVideo = isShowVideo;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_member, null, false);
            viewHolder.avatarvideo = (ImageView) convertView.findViewById(R.id.avatarvideo);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.postImg);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (!TextUtils.isEmpty(members.get(position).headUrl)) {

            if (isShowVideo) {
                viewHolder.avatarvideo.setVisibility(View.VISIBLE);
                viewHolder.avatar.setVisibility(View.GONE);
                Glide.with(context).load(members.get(position).headUrl).error(R.drawable.error_img).into(viewHolder.avatarvideo);
            } else {
                viewHolder.avatar.setVisibility(View.VISIBLE);
                viewHolder.avatarvideo.setVisibility(View.GONE);
                Glide.with(context).load(members.get(position).headUrl).error(R.drawable.error_img).into(viewHolder.avatar);
            }
        }
        return convertView;
    }

    public class ViewHolder {
        ImageView avatar;
        ImageView avatarvideo;
    }


    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
