package com.act.quzhibo.im.holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.download.bean.MediaInfo;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.ui.activity.BGAPhotoPreviewActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.Bind;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * 接收到的文本类型
 */
public class ReceiveImageHolder extends BaseViewHolder {

    @Bind(R.id.iv_avatar)
    protected ImageView iv_avatar;

    @Bind(R.id.tv_time)
    protected TextView tv_time;

    @Bind(R.id.iv_picture)
    protected ImageView iv_picture;
    @Bind(R.id.progress_load)
    protected ProgressBar progress_load;
    BmobIMConversation conversation;



    public ReceiveImageHolder(Context context, BmobIMConversation conversation, ViewGroup root, OnRecyclerViewListener onRecyclerViewListener) {
        super(context, root, R.layout.item_chat_received_image, onRecyclerViewListener);
        this.conversation = conversation;
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage msg = (BmobIMMessage) o;
        //用户信息的获取必须在buildFromDB之前，否则会报错'Entity is detached from DAO context'
        Glide.with(context).load(!TextUtils.isEmpty(conversation.getConversationIcon()) ? conversation.getConversationIcon() : "").asBitmap().error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = dateFormat.format(msg.getCreateTime());
        tv_time.setText(time);
        //可使用buildFromDB方法转化为指定类型的消息
        final BmobIMImageMessage message = BmobIMImageMessage.buildFromDB(false, msg);
        //显示图片

        Glide.with(context).load(message.getRemoteUrl()).asBitmap().placeholder(R.drawable.placehoder_img).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                iv_picture.setImageBitmap(resource);
                progress_load.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStop() {
                super.onStop();
                progress_load.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                super.onLoadCleared(placeholder);
                progress_load.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                progress_load.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                progress_load.setVisibility(View.VISIBLE);
            }
        });

        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerson(message);
            }
        });

        iv_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MediaInfo> mMediaInfos = new ArrayList<>();
                MediaInfo mediaInfo = new MediaInfo("", "", "",message.getContent().split("&").length>1? message.getContent().split("&")[1]:"", "", "");
                mMediaInfos.add(mediaInfo);
                if (mMediaInfos.size() > 0) {
                    context.startActivity(BGAPhotoPreviewActivity.newIntent(context, mMediaInfos, 0, true));
                }

            }
        });

        iv_picture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onRecyclerViewListener != null) {
                    onRecyclerViewListener.onItemLongClick(getAdapterPosition(),v);
                }
                return true;
            }
        });

    }


    public void showTime(boolean isShow) {
        tv_time.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}