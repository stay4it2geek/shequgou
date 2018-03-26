package com.act.quzhibo.bean;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.base.BaseChatRecyclerAdapter;
import com.act.quzhibo.ui.activity.ChatActivity;
import com.act.quzhibo.widget.FragmentDialog;

import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMConversationType;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;

/**
 * 私聊会话
 */
public class PrivateConversation extends Conversation {

    private BmobIMConversation conversation;
    private BmobIMMessage lastMsg;

    public PrivateConversation(BmobIMConversation conversation) {
        this.conversation = conversation;
        cType = BmobIMConversationType.setValue(conversation.getConversationType());
        cId = conversation.getConversationId();
        if (cType == BmobIMConversationType.PRIVATE) {
            cName = conversation.getConversationTitle();
            if (TextUtils.isEmpty(cName)) cName = cId;
        } else {
            cName = "未知会话";
        }
        List<BmobIMMessage> msgs = conversation.getMessages();
        if (msgs != null && msgs.size() > 0) {
            lastMsg = msgs.get(0);
        }
    }

    @Override
    public void readAllMessages() {
        conversation.updateLocalCache();
    }

    @Override
    public Object getAvatar() {
        if (cType == BmobIMConversationType.PRIVATE) {
            String avatar = conversation.getConversationIcon();
            if (TextUtils.isEmpty(avatar)) {//头像为空，使用默认头像
                return R.mipmap.default_head;
            } else {
                return avatar;
            }
        } else {
            return R.mipmap.default_head;
        }
    }

    @Override
    public String getLastMessageContent() {
        if (lastMsg != null) {
            String content = lastMsg.getContent();
            if (lastMsg.getMsgType().equals(BmobIMMessageType.TEXT.getType()) || lastMsg.getMsgType().equals("agree")) {
                return content;
            } else if (lastMsg.getMsgType().equals(BmobIMMessageType.IMAGE.getType())) {
                return "[图片]";
            } else if (lastMsg.getMsgType().equals(BmobIMMessageType.VOICE.getType())) {
                return "[语音]";
            } else if (lastMsg.getMsgType().equals(BmobIMMessageType.LOCATION.getType())) {
                return "[位置]";
            } else if (lastMsg.getMsgType().equals(BmobIMMessageType.VIDEO.getType())) {
                return "[视频]";
            } else {//开发者自定义的消息类型，需要自行处理
                return "[未知]";
            }
        } else {//防止消息错乱
            return "";
        }
    }

    @Override
    public long getLastMessageTime() {
        if (lastMsg != null) {
            return lastMsg.getCreateTime();
        } else {
            return 0;
        }
    }

    @Override
    public int getUnReadCount() {
        //查询指定会话下的未读消息数
        return (int) BmobIM.getInstance().getUnReadCount(conversation.getConversationId());
    }

    @Override
    public void onClick(FragmentActivity activity) {
        if (conversation != null) {
            Intent intent = new Intent(activity, ChatActivity.class);
            intent.putExtra("c", conversation);
            activity.startActivity(intent);
        }
    }

    @Override
    public void onLongClick(final FragmentActivity activity, final BaseChatRecyclerAdapter adapter, final int position) {
        FragmentDialog.newInstance(false, "是否删除？", "删除后不可恢复", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                BmobIM.getInstance().deleteConversation(conversation);
                adapter.remove(position);
            }

            @Override
            public void onNegtiveClick(Dialog dialog) {
                dialog.dismiss();

            }
        }).show(activity.getSupportFragmentManager(), "");
    }
}
