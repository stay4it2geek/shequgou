package com.act.quzhibo.adapter;

import android.app.Activity;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.base.BaseChatRecyclerAdapter;
import com.act.quzhibo.adapter.base.BaseRecyclerHolder;
import com.act.quzhibo.i.IMutlipleItem;
import com.act.quzhibo.bean.Conversation;
import com.act.quzhibo.util.TimeUtil;

import java.util.Collection;

/**
 * 使用进一步封装的Conversation
 */
public class ConversationAdapterChat extends BaseChatRecyclerAdapter<Conversation> {

    public ConversationAdapterChat(Activity activity, IMutlipleItem<Conversation> items, Collection<Conversation> datas) {
        super(activity,items,datas);
    }


    @Override
    public void bindView(BaseRecyclerHolder holder, Conversation conversation, int position) {
        holder.setText(R.id.tv_recent_msg,conversation.getLastMessageContent());
        holder.setText(R.id.tv_recent_time, TimeUtil.getChatTime(false,conversation.getLastMessageTime()));
        //会话图标
        Object obj = conversation.getAvatar();
        if(obj instanceof String){
            String avatar=(String)obj;
            holder.setImageView(avatar,R.id.iv_recent_avatar);
        }else{
            holder.setImageView(null, R.id.iv_recent_avatar);
        }
        //会话标题
        holder.setText(R.id.tv_recent_name, conversation.getcName());
        //查询指定未读消息数
        long unread = conversation.getUnReadCount();
        if(unread>0){
            holder.setVisible(R.id.tv_recent_unread, View.VISIBLE);
            holder.setText(R.id.tv_recent_unread, String.valueOf(unread));
        }else{
            holder.setVisible(R.id.tv_recent_unread, View.GONE);
        }
    }
}