package com.act.quzhibo.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.act.quzhibo.widget.MyStandardVideoController;
import com.act.quzhibo.R;
import com.act.quzhibo.bean.MyPost;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.util.CommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.devlin_n.videoplayer.player.IjkVideoView;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.io.File;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import cn.bmob.v3.BmobUser;

public class MyPostDetailAdapter extends RecyclerView.Adapter<MyPostDetailAdapter.Item1ViewHolder> implements BGANinePhotoLayout.Delegate {
    private LayoutInflater mLayoutInflater;
    private MyPost post;
    private FragmentActivity activity;
    private File downloadDir;

    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        if (ninePhotoLayout.getItemCount() == 1) {
            activity.startActivity(BGAPhotoPreviewActivity.newIntent(activity, downloadDir, ninePhotoLayout.getCurrentClickItem()));
        } else if (ninePhotoLayout.getItemCount() > 1) {
            activity.startActivity(BGAPhotoPreviewActivity.newIntent(activity, downloadDir, ninePhotoLayout.getData(), ninePhotoLayout.getCurrentClickItemPosition()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public MyPostDetailAdapter(MyPost post, FragmentActivity context) {
        this.activity = context;
        this.post = post;
        mLayoutInflater = LayoutInflater.from(context);
        downloadDir = new File(Environment.getExternalStorageDirectory(), "PhotoPickerDownload");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    @Override
    public Item1ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Item1ViewHolder(mLayoutInflater.inflate(R.layout.post_detail_header_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final Item1ViewHolder holder, final int positon) {

            Glide.with(activity).load(BmobUser.getCurrentUser(RootUser.class).photoFileUrl + "").asBitmap().placeholder(R.drawable.placehoder_img).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    holder.userImage.setBackgroundDrawable(new BitmapDrawable(resource));
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    holder.userImage.setBackgroundDrawable(errorDrawable);

                }
            });
        holder.sexAndAge.setText(BmobUser.getCurrentUser(RootUser.class).sex);
        long l = System.currentTimeMillis() - Long.parseLong(CommonUtil.dateToStamp(post.getCreatedAt()));
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        if (day <= 1) {
            holder.createTime.setText(hour + "小时" + min + "分钟前");
        } else if (day < 30) {
            holder.createTime.setText(day + "天" + hour + "小时前");
        } else if (day > 30 && day < 60) {
            holder.createTime.setText("2个月前");
        } else if (day > 90) {
            holder.createTime.setText("3个月前");
        }
        holder.nickName.setText(BmobUser.getCurrentUser(RootUser.class).getUsername());
        holder.title.setText(post.title + "");
        holder.content.setText(post.absText + "");
        holder.areaLocation.setText(BmobUser.getCurrentUser(RootUser.class).provinceAndcity + "");


        holder.ninePhotoLayout.setDelegate(this);
        if(post.images!=null&&post.images.size()>0){
            holder.ninePhotoLayout.setData(post.images);
        }

        if (!TextUtils.isEmpty(post.vedioUrl)) {
            holder.ijkVideoView.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bitmap = CommonUtil.createBitmapFromVideoPath(post.vedioUrl);
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                     holder.controller.getThumb().setImageBitmap(bitmap);
                     holder.ijkVideoView
                            .enableCache()
                            .addToPlayerManager()
                            .setUrl(post.vedioUrl)
                            .setTitle(post.title)
                            .setVideoController( holder.controller);

                }
            }.execute();
        } else {
             holder.ijkVideoView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class Item1ViewHolder extends RecyclerView.ViewHolder {

        private TextView areaLocation;
        private TextView createTime;
        private ImageView userImage;
        private TextView sexAndAge;
        private TextView nickName;
        private TextView title;
        private EmojiconTextView content;
        private BGANinePhotoLayout ninePhotoLayout;
        private IjkVideoView ijkVideoView;
        private MyStandardVideoController controller;

        public Item1ViewHolder(View view) {
            super(view);
            nickName = (TextView) view.findViewById(R.id.nickName);
            createTime = (TextView) view.findViewById(R.id.createTime);
            areaLocation = (TextView) view.findViewById(R.id.location);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            sexAndAge = (TextView) view.findViewById(R.id.sexAndAge);
            nickName = (TextView) view.findViewById(R.id.nickName);
            title = (TextView) view.findViewById(R.id.title);
            content = (EmojiconTextView) view.findViewById(R.id.content);
            ninePhotoLayout = (BGANinePhotoLayout) view.findViewById(R.id.imglistview);
            ijkVideoView = (IjkVideoView) itemView.findViewById(R.id.video_player);
            int widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
            ijkVideoView.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, widthPixels / 14 * 9));
            controller = new MyStandardVideoController(activity);
            controller.setInitData(false, false);
            ijkVideoView.setVideoController(controller);
        }

    }

}

