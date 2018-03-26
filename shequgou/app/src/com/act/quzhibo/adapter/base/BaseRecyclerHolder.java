package com.act.quzhibo.adapter.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;



/**
 * 与BaseRecyclerAdapter一起使用
 *
 */
public class BaseRecyclerHolder extends RecyclerView.ViewHolder {

    private final SparseArray<View> mViews;
    private final Context context;
    public  int layoutId;

    public BaseRecyclerHolder(Context context, int layoutId, View itemView) {
        super(itemView);
        this.layoutId =layoutId;
        this.context =context;
        this.mViews = new SparseArray<>(8);
    }

    /**
     * @param viewId
     * @return
     */
    protected <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * @param viewId
     * @param text
     * @return
     */
    public BaseRecyclerHolder setText(int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    /**
     * 设置Enabled
     * @param viewId
     * @param enable
     * @return
     */
    public BaseRecyclerHolder setEnabled(int viewId,boolean enable){
        View v = getView(viewId);
        v.setEnabled(enable);
        return this;
    }

    /**
     * 点击事件
     * @param viewId
     * @param listener
     * @return
     */
    public BaseRecyclerHolder setOnClickListener(int viewId, View.OnClickListener listener){
        View v = getView(viewId);
        v.setOnClickListener(listener);
        return this;
    }

    /**
     * @param viewId
     * @param visibility
     * @return
     */
    public BaseRecyclerHolder setVisible(int viewId,int visibility) {
        View view = getView(viewId);
        view.setVisibility(visibility);
        return this;
    }


    /**
     * @param avatar
     * @param viewId
     * @return
     */
    public BaseRecyclerHolder setImageView(String avatar,int viewId) {
        final ImageView iv_avatar = getView(viewId);
        Glide.with(context).load(avatar).asBitmap().error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                iv_avatar.setBackgroundDrawable(new BitmapDrawable(resource));
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                iv_avatar.setBackgroundDrawable(errorDrawable);

            }
        });

        return this;
    }
}