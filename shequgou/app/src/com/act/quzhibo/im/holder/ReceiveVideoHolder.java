package com.act.quzhibo.im.holder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.NearVideoEntity;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.ui.activity.NearMediaVideoListActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMVideoMessage;

/**
 * 接收到的视频类型--这是举个例子，并没有展示出视频缩略图等信息，开发者可自行设置
 */
public class ReceiveVideoHolder extends BaseViewHolder {

    @Bind(R.id.iv_avatar)
    protected ImageView iv_avatar;
    @Bind(R.id.video_recive)
    protected ImageView video_recive;
    @Bind(R.id.tv_time)
    protected TextView tv_time;
    @Bind(R.id.video_recive_cover)
    protected FrameLayout video_recive_cover;
    @Bind(R.id.tv_message)
    protected EmojiconTextView tv_message;
    private BmobIMConversation conversation;

    public ReceiveVideoHolder(Context context, BmobIMConversation conversation, ViewGroup root, OnRecyclerViewListener onRecyclerViewListener) {
        super(context, root, R.layout.item_chat_received_message, onRecyclerViewListener);
        this.conversation = conversation;
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage msg = (BmobIMMessage) o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = dateFormat.format(msg.getCreateTime());
        tv_time.setText(time);
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
        final BmobIMVideoMessage message = BmobIMVideoMessage.buildFromDB(false, msg);
        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerson(message);
            }
        });
        final String content = message.getContent();
        video_recive_cover.setVisibility(View.VISIBLE);
        tv_message.setVisibility(View.GONE);

        video_recive_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NearVideoEntity nearVideoEntity = new NearVideoEntity();
                nearVideoEntity.url = TextUtils.isEmpty(message.getContent()) ? "" : content.split("&")[1];
                Intent intent = new Intent();
                intent.putExtra(Constants.NEAR_USER_VIDEO, nearVideoEntity);
                intent.setClass(context, NearMediaVideoListActivity.class);
                context.startActivity(intent);
            }
        });


        Glide.with(context).load(TextUtils.isEmpty(content) ? "" : content.split("&")[1]).skipMemoryCache(false).into(video_recive);


        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerson(message);
            }
        });


        video_recive_cover.setOnLongClickListener(new View.OnLongClickListener() {
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