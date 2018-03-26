package com.act.quzhibo.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class ImageUtil {


    public static void load(Context mContext, String uri, ImageView view, int placeholder) {
        Glide.with(mContext)
                .load(uri)
                .placeholder(placeholder)
                .crossFade()
                .into(view);
    }


}