package com.act.quzhibo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.stackblur.StackBlurManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;

public class PostImageAdapter extends BaseAdapter {

    private boolean isShowVideoCover;
    private boolean isNeedBlur;
    private int viewHodlerType;
    private Context context;
    private ArrayList<String> imgs;

    public PostImageAdapter(Context context, ArrayList<String> imgs, int viewHodlerType, boolean isNeedBlur, boolean isShowVideoCover) {
        this.context = context;
        this.imgs = imgs;
        this.isNeedBlur = isNeedBlur;
        this.viewHodlerType = viewHodlerType;
        this.isShowVideoCover = isShowVideoCover;
    }

    public int getCount() {
        return imgs.size() > 9 ? 9 : imgs.size();
    }

    public Object getItem(int item) {
        return item;
    }

    public long getItemId(int id) {
        return id;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (viewHodlerType == Constants.ITEM_POST_LIST_IMG) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_post_list_img, parent, false);
            } else if (viewHodlerType == Constants.ITEM_POST_DETAIL_IMG) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_post_detail_img, parent, false);
            }else if (viewHodlerType == Constants.ITEM_VIDEO_DETAIL_IMG) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_video_detail_img, parent, false);
            }else {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_info_common_user_img, parent, false);
                if (isShowVideoCover) {
                    convertView.findViewById(R.id.video_player_cover).setVisibility(View.VISIBLE);
                }
            }
            viewHolder.showImg = (ImageView) convertView.findViewById(R.id.postImg);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RootUser rootUser = BmobUser.getCurrentUser(RootUser.class);
        if (imgs != null && imgs.size() > 0) {
            if (BmobUser.getCurrentUser(RootUser.class) == null) {
                if (isNeedBlur) {
                    blurImage(position, viewHolder);
                } else {
                    Glide.with(context).load(imgs.get(position)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(viewHolder.showImg);
                }
            } else {
                if (isNeedBlur) {
                    if (!rootUser.isVip) {
                        blurImage(position, viewHolder);
                    } else {
                        Glide.with(context).load(imgs.get(position)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(viewHolder.showImg);
                    }
                } else {
                    Glide.with(context).load(imgs.get(position)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(viewHolder.showImg);
                }

            }
        }
        return convertView;
    }

    private void blurImage(int position, final ViewHolder viewHolder) {
        Glide.with(context).load(imgs.get(position)).asBitmap().placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                new AsyncTask<Void, Void, StackBlurManager>() {
                    @Override
                    protected StackBlurManager doInBackground(Void... params) {
                        StackBlurManager stackBlurManager = new StackBlurManager(context, resource);
                        return stackBlurManager;
                    }

                    @Override
                    protected void onPostExecute(StackBlurManager stackBlurManager) {
                        super.onPostExecute(stackBlurManager);
                        viewHolder.showImg.setImageBitmap(stackBlurManager.process(12));
                    }
                }.execute();
            }

            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                viewHolder.showImg.setBackgroundDrawable(placeholder);
            }
        });
    }

    public class ViewHolder {
        ImageView showImg;
    }

}
