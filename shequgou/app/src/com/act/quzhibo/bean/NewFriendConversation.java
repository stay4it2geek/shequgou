package com.act.quzhibo.bean;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.base.BaseChatRecyclerAdapter;
import com.act.quzhibo.common.Config;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.chat.NewFriend;
import com.act.quzhibo.chat.NewFriendManager;
import com.act.quzhibo.ui.activity.NewFriendActivity;
import com.act.quzhibo.widget.FragmentDialog;

public class NewFriendConversation extends Conversation {

    NewFriend lastFriend;

    public NewFriendConversation(NewFriend friend) {
        this.lastFriend = friend;
        this.cName = "新朋友";
    }

    @Override
    public String getLastMessageContent() {
        if (lastFriend != null) {
            Integer status = lastFriend.getStatus();
            String name = lastFriend.getName();
            if (TextUtils.isEmpty(name)) {
                name = lastFriend.getUid();
            }
            //目前的好友请求都是别人发给我的
            if (status == null || status == Config.STATUS_VERIFY_NONE || status == Config.STATUS_VERIFY_READED) {
                return name + "请求添加好友";
            } else {
                return "我已添加" + name;
            }
        } else {
            return "";
        }
    }

    @Override
    public long getLastMessageTime() {
        if (lastFriend != null) {
            return lastFriend.getTime();
        } else {
            return 0;
        }
    }

    @Override
    public Object getAvatar() {
        return R.mipmap.new_friends_icon;
    }

    @Override
    public int getUnReadCount() {
        return NewFriendManager.getInstance(MyApplicaition.INSTANCE()).getNewInvitationCount();
    }

    @Override
    public void readAllMessages() {
        //批量更新未读未认证的消息为已读状态
        NewFriendManager.getInstance(MyApplicaition.INSTANCE()).updateBatchStatus();
    }

    @Override
    public void onClick(FragmentActivity context) {
        Intent intent = new Intent();
        intent.setClass(context, NewFriendActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onLongClick(final FragmentActivity activity, final BaseChatRecyclerAdapter adapter, final int position) {
        FragmentDialog.newInstance(false, "是否删除？", "删除后不可恢复", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                NewFriendManager.getInstance(activity).deleteNewFriend(lastFriend);
                adapter.remove(position);
            }

            @Override
            public void onNegtiveClick(Dialog dialog) {
                dialog.dismiss();

            }
        }).show(activity.getSupportFragmentManager(), "");
    }
}
