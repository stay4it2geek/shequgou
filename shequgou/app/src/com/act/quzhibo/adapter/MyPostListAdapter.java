package com.act.quzhibo.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.MyPost;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.util.CommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;

public class MyPostListAdapter extends RecyclerView.Adapter<MyPostListAdapter.MyViewHolder> {
    private ArrayList<MyPost> posts;
    private Activity activity;
    RootUser rootUser = BmobUser.getCurrentUser(RootUser.class);

    public interface OnMyPostRecyclerViewItemClickListener {
        void onItemClick(MyPost post);

        void onItemDelteClick(int position, MyPost post, ImageView imageView, ArrayList<String> imgs);
    }

    private OnMyPostRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnMyPostRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    //适配器初始化
    public MyPostListAdapter(Activity context, ArrayList<MyPost> posts) {
        activity = context;
        this.posts = posts;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.interest_post_list_item, parent, false);//这个布局就是一个imageview用来显示图片
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        String nick = rootUser.getUsername().replaceAll("\r|\n", "");
        final MyPost post = posts.get(position);
        holder.nickName.setText(nick);
        long l = System.currentTimeMillis() - Long.parseLong(CommonUtil.dateToStamp(post.getCreatedAt()));
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        holder.sexAndAge.setText(rootUser.sex + "");
        if (day <= 1) {
            holder.createTime.setText(hour + "小时" + min + "分钟前");
        } else if (day < 30) {
            holder.createTime.setText(day + "天" + hour + "小时前");
        } else if (day > 30 && day < 60) {
            holder.createTime.setText("2个月前");
        } else if (day > 90) {
            holder.createTime.setText("3个月前");
        }
        holder.title.setText(post.title + "");
        holder.absText.setText(post.absText + "");
        holder.viewNum.setText(post.pageView + "");
        holder.commentNum.setText(post.totalComments + "");

        if (post.images != null && post.images.size() > 0) {
            holder.imgVideolayout.setVisibility(View.GONE);
            holder.imgtotal.setVisibility(View.VISIBLE);
            holder.imgGridview.setAdapter(new PostImageAdapter(activity, post.images, Constants.ITEM_POST_DETAIL_IMG, false, false));
            holder.imgtotal.setText("共" + post.totalImages + "张");
        } else {
            holder.imgtotal.setVisibility(View.GONE);
            holder.imgGridview.setVisibility(View.GONE);
            holder.imgVideolayout.setVisibility(View.VISIBLE);
            holder.imgtotal.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(post.vedioUrl)) {
                if (!TextUtils.isEmpty(post.vedioUrl)) {
                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... params) {
                            Bitmap bitmap = CommonUtil.createBitmapFromVideoPath(post.vedioUrl);
                            return bitmap;
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            holder.imgVideo.setImageBitmap(bitmap);
                        }
                    }.execute();
                }
            } else {
                holder.imgVideolayout.setVisibility(View.GONE);
            }
        }
        holder.postlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(post);
            }
        });
        holder.arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemDelteClick(position, post, holder.arrow, post.images);
            }
        });
        if (rootUser.photoFileUrl != null) {
                    Glide.with(activity).load(rootUser.photoFileUrl).asBitmap().placeholder(R.drawable.placehoder_img).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            holder.photoImg.setBackgroundDrawable(new BitmapDrawable(resource));
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            holder.photoImg.setBackgroundDrawable(errorDrawable);
                        }
                    });

        }

        holder.areaLocation.setText(rootUser.provinceAndcity + "");
        holder.imgGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOnItemClickListener.onItemClick(post);
            }
        });
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private GridView imgGridview;
        private TextView viewNum;
        private TextView commentNum;
        private TextView nickName;
        private TextView title;
        private ImageView photoImg;
        private RelativeLayout postlayout;
        private TextView absText;
        private TextView imgtotal;
        private TextView areaLocation;
        private ImageView imgVideo;
        private TextView createTime;
        private TextView sexAndAge;
        private FrameLayout imgVideolayout;
        private ImageView arrow;

        public MyViewHolder(View view) {
            super(view);
            arrow = (ImageView) view.findViewById(R.id.arrow);
            arrow.setVisibility(View.VISIBLE);
            photoImg = (ImageView) view.findViewById(R.id.photoImg);
            nickName = (TextView) view.findViewById(R.id.nick);
            title = (TextView) view.findViewById(R.id.title);
            absText = (EmojiconTextView) view.findViewById(R.id.absText);
            createTime = (TextView) view.findViewById(R.id.createTime);
            areaLocation = (TextView) view.findViewById(R.id.location);
            sexAndAge = (TextView) view.findViewById(R.id.sexAndAge);
            viewNum = (TextView) view.findViewById(R.id.viewNum);
            commentNum = (TextView) view.findViewById(R.id.pinglunNum);
            imgtotal = (TextView) view.findViewById(R.id.imgtotal);
            postlayout = (RelativeLayout) view.findViewById(R.id.postlayout);
            imgVideo = (ImageView) view.findViewById(R.id.imgVideo);
            imgVideolayout = (FrameLayout) view.findViewById(R.id.imgVideolayout);
            imgGridview = (GridView) view.findViewById(R.id.imgGridview);
        }
    }
}