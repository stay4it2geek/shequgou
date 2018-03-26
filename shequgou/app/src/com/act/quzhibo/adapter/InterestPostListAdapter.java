package com.act.quzhibo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import com.act.quzhibo.data_db.VirtualUserInfo;
import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.bean.InterestParentPerson;
import com.act.quzhibo.bean.InterestPost;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.ui.activity.InfoInterestPersonActivity;
import com.act.quzhibo.util.CommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterestPostListAdapter extends RecyclerView.Adapter<InterestPostListAdapter.MyViewHolder> {
    ArrayList<InterestPost> datas;
    Context context;
    boolean isNeedBlur;
    int personIndex;

    public void setPersonIndex(int personIndex) {
        this.personIndex = personIndex;
    }

    public interface OnInterestPostRecyclerViewItemClickListener {
        void onItemClick(InterestPost post);
    }

    OnInterestPostRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnInterestPostRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public InterestPostListAdapter(Context context, ArrayList<InterestPost> datas, boolean isNeedBlur) {
        this.context = context;
        this.datas = datas;
        this.isNeedBlur = isNeedBlur;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.interest_post_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final InterestParentPerson user = datas.get(position).user;
        final InterestPost post = datas.get(position);
        String nick = user.nick.replaceAll("\r|\n", "");
        holder.nickName.setText(nick);


        long l = System.currentTimeMillis() - Long.parseLong(datas.get(position).ctime);
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);

        Random random = new Random();
        String randomAge = (random.nextInt(10) + 20) + "";
        VirtualUserInfoDao dao = VirtualUserInfoDao.getInstance(context);

        int maxMinu = 800;
        int minMinu = 30;

        VirtualUserInfo dataUser = dao.query(post.user.userId);
        if (dataUser != null) {
            randomAge = dataUser.userAge != null ? dataUser.userAge : "";
        } else {
            VirtualUserInfo user_ = new VirtualUserInfo();
            user_.userAge = randomAge;
            user_.userId = post.user.userId;
            user_.onlineTime = (random.nextInt(maxMinu) + random.nextInt(minMinu)) + "";
            user_.seeMeTime = "0";
            dao.add(user_);
        }

        holder.sexAndAge.setText(datas.get(position).user.sex.equals("2") ? "女 " + randomAge + "岁" : "男 " + randomAge + "岁");

        if (day <= 1) {
            holder.createTime.setText(hour + "小时" + min + "分钟前");
        } else if (day < 30) {
            holder.createTime.setText(day + "天" + hour + "小时前");
        } else if (day > 30) {
            holder.createTime.setText("1个月前");
        }
        holder.title.setText(datas.get(position).title + "");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String newString = datas.get(position).absText;
                Pattern pattern = Pattern.compile("[a-z_]{1,}", Pattern.CASE_INSENSITIVE);
                final Matcher matcher = pattern.matcher(newString);
                while (matcher.find()) {
                    newString = newString.replaceAll(":" + matcher.group().trim() + ":", "<img src='" + MyApplicaition.emotionsKeySrc.get(":" + matcher.group().trim() + ":") + "'>");
                }
                if (newString.contains("null")) {
                    newString = newString.replaceAll("null", R.drawable.kissing_heart + "");
                }
                return newString;
            }

            @Override
            protected void onPostExecute(String newString) {
                super.onPostExecute(newString);
                holder.absText.setText(Html.fromHtml(newString, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        Drawable drawable = null;
                        if (!TextUtils.isEmpty(source) && !source.equals("null")) {
                            int id = Integer.parseInt(source);
                            drawable = context.getResources().getDrawable(id);
                            if (drawable != null) {
                                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                                        drawable.getIntrinsicHeight());
                            }
                        }
                        return drawable;
                    }
                }, null));
            }
        }.execute();


        holder.viewNum.setText(datas.get(position).pageView + "");
        holder.pinglunNum.setText(datas.get(position).totalComments + "");

        if (isNeedBlur) {
            holder.pName.setVisibility(View.VISIBLE);
            holder.pName.setText(post.sName);
        }
        if (datas.get(position).totalImages != null && Integer.parseInt(datas.get(position).totalImages) > 0) {
            holder.imgGridview.setVisibility(View.VISIBLE);
            holder.imgVideolayout.setVisibility(View.GONE);
            holder.imgtotal.setVisibility(View.VISIBLE);
            holder.imgGridview.setAdapter(new PostImageAdapter(context, datas.get(position).images, Constants.ITEM_POST_LIST_IMG, isNeedBlur, false));
            holder.imgtotal.setText("共" + datas.get(position).totalImages + "张");
        } else {
            holder.imgVideolayout.setVisibility(View.VISIBLE);
            holder.imgtotal.setVisibility(View.GONE);
            holder.imgGridview.setVisibility(View.GONE);
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
        holder.imgGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOnItemClickListener.onItemClick(post);
            }
        });

        holder.photoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Constants.POST, post);
                intent.putExtra("FocusPersonIndex",personIndex);
                intent.setClass(context, InfoInterestPersonActivity.class);
                context.startActivity(intent);
            }
        });

            Glide.with(context).load(user.photoUrl).asBitmap().placeholder(R.mipmap.default_head).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    holder.photoImg.setBackgroundDrawable(new BitmapDrawable(resource));
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    holder.photoImg.setBackgroundDrawable(errorDrawable);

                }
            });

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String text = MyApplicaition.proKeySrc.get(datas.get(position).user.proCode) + MyApplicaition.cityKeySrc.get(datas.get(position).user.cityCode);
                return text;
            }

            @Override
            protected void onPostExecute(String text) {
                super.onPostExecute(text);
                holder.arealocation.setText(text);
            }
        }.execute();
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView pName;
        GridView imgGridview;
        TextView viewNum;
        TextView pinglunNum;
        TextView nickName;
        TextView title;
        ImageView photoImg;
        RelativeLayout postlayout;
        TextView absText;
        TextView imgtotal;
        ImageView imgVideo;
        TextView arealocation;
        TextView createTime;
        TextView sexAndAge;
        FrameLayout imgVideolayout;

        public MyViewHolder(View view) {
            super(view);
            pName = (TextView) view.findViewById(R.id.pName);
            photoImg = (ImageView) view.findViewById(R.id.photoImg);
            nickName = (TextView) view.findViewById(R.id.nick);
            title = (TextView) view.findViewById(R.id.title);
            absText = (EmojiconTextView) view.findViewById(R.id.absText);
            createTime = (TextView) view.findViewById(R.id.createTime);
            arealocation = (TextView) view.findViewById(R.id.location);
            sexAndAge = (TextView) view.findViewById(R.id.sexAndAge);
            viewNum = (TextView) view.findViewById(R.id.viewNum);
            pinglunNum = (TextView) view.findViewById(R.id.pinglunNum);
            imgtotal = (TextView) view.findViewById(R.id.imgtotal);
            postlayout = (RelativeLayout) view.findViewById(R.id.postlayout);
            imgGridview = (GridView) view.findViewById(R.id.imgGridview);
            imgVideo = (ImageView) view.findViewById(R.id.imgVideo);
            imgVideolayout = (FrameLayout) view.findViewById(R.id.imgVideolayout);


        }
    }
}