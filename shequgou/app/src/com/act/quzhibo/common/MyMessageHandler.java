package com.act.quzhibo.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.act.quzhibo.bean.AddFriendMessage;
import com.act.quzhibo.bean.AgreeAddFriendMessage;
import com.act.quzhibo.chat.NewFriend;
import com.act.quzhibo.chat.NewFriendManager;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.event.RefreshEvent;
import com.act.quzhibo.im.UserDao;
import com.act.quzhibo.i.UpdateCacheListener;
import com.act.quzhibo.ui.activity.ChatFriendsActivity;
import com.act.quzhibo.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


//集成：自定义消息接收器处理在线消息和离线消息
public class MyMessageHandler extends BmobIMMessageHandler {
    public boolean isChatting;
    private Context context;
    String tips = "";

    public MyMessageHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceive(final MessageEvent event) {
        //当接收到服务器发来的消息时，此方法被调用
        Log.e("onMessageReceive", "" + isChatting);
        executeMessage(event);
    }

    @Override
    public void onOfflineReceive(final OfflineMessageEvent event) {
        Log.e("onOfflineReceive", "" + isChatting);

        //每次调用connect方法时会查询一次离线消息，如果有，此方法会被调用
        Map<String, List<MessageEvent>> map = event.getEventMap();
        Log.e("有", map.size() + "个用户发来离线消息");
        //挨个检测下离线消息所属的用户的信息是否需要更新
        for (Map.Entry<String, List<MessageEvent>> entry : map.entrySet()) {
            List<MessageEvent> list = entry.getValue();
            int size = list.size();
            Log.e("用户", entry.getKey() + "发来" + size + "条消息");
            for (int i = 0; i < size; i++) {
                //处理每条消息
                executeMessage(list.get(i));
            }
        }
    }

    /**
     * 处理消息
     *
     * @param event
     */
    private void executeMessage(final MessageEvent event) {
        //检测用户信息是否需要更新
        UserDao.getInstance().updateUserInfo(event, new UpdateCacheListener() {
            @Override
            public void done(BmobException e) {
                BmobIMMessage msg = event.getMessage();
                processCustomMessage(msg, event);
            }
        });
    }


    /**
     * 处理自定义消息类型
     *
     * @param msg
     */


    private void processCustomMessage(final BmobIMMessage msg, final MessageEvent messageEvent) {
        //消息类型
        String type = msg.getMsgType();
        //发送页面刷新的广播
        if (isChatting) {
            EventBus.getDefault().post(messageEvent);
        } else {
            EventBus.getDefault().post(new RefreshEvent());
        }

        //处理消息
        if (type.equals(AddFriendMessage.ADD)) {//接收到的添加好友的请求
            NewFriend friend = AddFriendMessage.convert(msg);
            //本地好友请求表做下校验，本地没有的才允许显示通知栏--有可能离线消息会有些重复
            long id = NewFriendManager.getInstance(context).insertOrUpdateNewFriend(friend);
            if (id > 0) {
                showAddNotify(friend);
            }
        } else if (type.equals(AgreeAddFriendMessage.AGREE)) {//接收到的对方同意添加自己为好友,此时需要做的事情：1、添加对方为好友，2、显示通知
            AgreeAddFriendMessage agree = AgreeAddFriendMessage.convert(msg);
            addFriend(agree.getFromId());//添加消息的发送方为好友
            //这里应该也需要做下校验--来检测下是否已经同意过该好友请求，我这里省略了
            showAgreeNotify(messageEvent.getFromUserInfo(), agree);
            EventBus.getDefault().post(new RefreshEvent());
        } else {
            final Intent pendingIntent = new Intent(context, ChatFriendsActivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            ToastUtil.showToast(context, msg.getMsgType() + "");

            if ("sound".equals(msg.getMsgType())) {
                tips = "我给你发了一段语音";
            } else if ("video".equals(msg.getMsgType())) {
                tips = "我给你发了一段视频";
            } else if ("location".equals(msg.getMsgType())) {
                tips = "我给你发了我的位置";

            } else if ("image".equals(msg.getMsgType())) {
                tips = "我给你发了一张照片";
            } else {
                tips = msg.getContent();
            }
            Glide.with(context).load(messageEvent.getFromUserInfo().getAvatar()).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap largeIcon, GlideAnimation<? super Bitmap> glideAnimation) {
                    BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                            messageEvent.getFromUserInfo().getName(), tips, messageEvent.getFromUserInfo().getName() + "发来消息", pendingIntent);
                }
            });
        }
    }

    /**
     * 显示对方添加自己为好友的通知
     *
     * @param friend
     */
    private void showAddNotify(final NewFriend friend) {
        final Intent pendingIntent = new Intent(context, ChatFriendsActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //这里可以是应用图标，也可以将聊天头像转成bitmap
        Glide.with(context).load(friend.getAvatar()).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap largeIcon, GlideAnimation<? super Bitmap> glideAnimation) {
                BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                        friend.getName(), friend.getMsg(), friend.getName() + "请求添加你为朋友", pendingIntent);
            }
        });
    }

    /**
     * 显示对方同意添加自己为好友的通知
     *
     * @param info
     * @param agree
     */
    private void showAgreeNotify(final BmobIMUserInfo info, final AgreeAddFriendMessage agree) {
        final Intent pendingIntent = new Intent(context, ChatFriendsActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Glide.with(context).load(info.getAvatar()).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap largeIcon, GlideAnimation<? super Bitmap> glideAnimation) {
                BmobNotificationManager.getInstance(context).showNotification(largeIcon, info.getName(), agree.getMsg(), agree.getMsg(), pendingIntent);

            }
        });
    }

    /**
     * TODO 好友管理：9.11、收到同意添加好友后添加好友
     *
     * @param uid
     */
    private void addFriend(String uid) {
        RootUser user = new RootUser();
        user.setObjectId(uid);
        UserDao.getInstance()
                .agreeAddFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            ToastUtil.showToast(context, "添加成功");
                        }
                    }
                });
    }
}
