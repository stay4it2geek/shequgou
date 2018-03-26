package com.act.quzhibo.im.holder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.io.File;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMSendStatus;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.bean.BmobIMVideoMessage;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.exception.BmobException;

/**
 * 发送的视频类型---这是举个例子，并没有展示出视频缩略图等信息，开发者可自行实现
 */
public class SendVideoHolder extends BaseViewHolder implements View.OnClickListener, View.OnLongClickListener {

    @Bind(R.id.iv_avatar)
    protected ImageView iv_avatar;

    @Bind(R.id.iv_fail_resend)
    protected ImageView iv_fail_resend;
    @Bind(R.id.video_send)
    protected ImageView video_send;
    @Bind(R.id.tv_time)
    protected TextView tv_time;
    @Bind(R.id.tv_message)
    protected EmojiconTextView tv_message;
    @Bind(R.id.video_send_cover)
    protected FrameLayout video_send_cover;
    @Bind(R.id.tv_send_status)
    protected TextView tv_send_status;

    @Bind(R.id.progress_load)
    protected ProgressBar progress_load;

    BmobIMConversation c;

    public SendVideoHolder(Context context, ViewGroup root, BmobIMConversation c, OnRecyclerViewListener listener) {
        super(context, root, R.layout.item_chat_sent_message, listener);
        this.c = c;
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage msg= (BmobIMMessage) o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final BmobIMUserInfo info = msg.getBmobIMUserInfo();
        Glide.with(context).load(info != null ? info.getAvatar() : null).asBitmap().error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
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

        String time = dateFormat.format(message.getCreateTime());
        final String content = message.getContent();
        video_send_cover.setVisibility(View.VISIBLE);
        tv_message.setVisibility(View.GONE);


        Glide.with(context).load(TextUtils.isEmpty(content) ? "" : content.split("&")[0]).skipMemoryCache(false).into(video_send);

        tv_time.setText(time);

        int status = message.getSendStatus();
        if (status == BmobIMSendStatus.SEND_FAILED.getStatus()) {
            iv_fail_resend.setVisibility(View.VISIBLE);
            progress_load.setVisibility(View.GONE);
        } else if (status == BmobIMSendStatus.SENDING.getStatus()) {
            iv_fail_resend.setVisibility(View.GONE);
            progress_load.setVisibility(View.VISIBLE);
        } else {
            iv_fail_resend.setVisibility(View.GONE);
            progress_load.setVisibility(View.GONE);
        }

        video_send_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(TextUtils.isEmpty(message.getContent()) ? "" : content.split("&")[0]);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", file);
                    intent.setDataAndType(contentUri, "video/*");
                } else {
                    uri = Uri.fromFile(file);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(uri, "video/*");
                }
                context.startActivity(intent);

            }
        });

        video_send_cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onRecyclerViewListener != null) {
                    onRecyclerViewListener.onItemLongClick(getAdapterPosition(), v);
                }
                return true;
            }
        });


        //重发
        iv_fail_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.resendMessage(message, new MessageSendListener() {
                    @Override
                    public void onStart(BmobIMMessage msg) {
                        progress_load.setVisibility(View.VISIBLE);
                        iv_fail_resend.setVisibility(View.GONE);
                        tv_send_status.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void done(BmobIMMessage msg, BmobException e) {
                        if (e == null) {
                            tv_send_status.setVisibility(View.VISIBLE);
                            tv_send_status.setText("已发送");
                            iv_fail_resend.setVisibility(View.GONE);
                            progress_load.setVisibility(View.GONE);
                        } else {
                            iv_fail_resend.setVisibility(View.VISIBLE);
                            progress_load.setVisibility(View.GONE);
                            tv_send_status.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }

    public void showTime(boolean isShow) {
        tv_time.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
