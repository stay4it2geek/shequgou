package com.act.quzhibo.adapter;

import android.app.Activity;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.base.BaseChatRecyclerAdapter;
import com.act.quzhibo.adapter.base.BaseRecyclerHolder;
import com.act.quzhibo.i.IMutlipleItem;
import com.act.quzhibo.bean.AgreeAddFriendMessage;
import com.act.quzhibo.common.Config;
import com.act.quzhibo.chat.NewFriend;
import com.act.quzhibo.chat.NewFriendManager;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.im.UserDao;
import com.act.quzhibo.util.ToastUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


public class NewFriendAdapterChat extends BaseChatRecyclerAdapter<NewFriend> {

    public NewFriendAdapterChat(Activity activity, IMutlipleItem<NewFriend> items, Collection<NewFriend> datas) {
        super(activity, items, datas);
    }

    @Override
    public void bindView(final BaseRecyclerHolder holder, final NewFriend add, int position) {
        holder.setImageView(add == null ? null : add.getAvatar(), R.id.iv_recent_avatar);
        holder.setText(R.id.tv_recent_name, add == null ? "未知" : add.getName());
        holder.setText(R.id.tv_recent_msg, add == null ? "未知" : add.getMsg());
        Integer status = add.getStatus();
        //当状态是未添加或者是已读未添加
        if (status == null || status == Config.STATUS_VERIFY_NONE || status == Config.STATUS_VERIFY_READED) {
            holder.setText(R.id.btn_aggree, "接受");
            holder.setEnabled(R.id.btn_aggree, true);
            holder.setOnClickListener(R.id.btn_aggree, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 1、添加好友
                    agreeAdd(add, new SaveListener<Object>() {
                        @Override
                        public void done(Object o, BmobException e) {
                            if (e == null) {
                                holder.setText(R.id.btn_aggree, "已添加");
                                holder.setEnabled(R.id.btn_aggree, false);
                            } else {
                                holder.setEnabled(R.id.btn_aggree, true);
                                ToastUtil.showToast(activity,"添加好友失败:" + e.getMessage());
                            }
                        }
                    });
                }
            });
        } else {
            holder.setText(R.id.btn_aggree, "已添加");
            holder.setEnabled(R.id.btn_aggree, false);
        }
    }

    /**
     * TODO 好友管理：9.10、添加到好友表中再发送同意添加好友的消息
     *
     * @param add
     * @param listener
     */
    private void agreeAdd(final NewFriend add, final SaveListener<Object> listener) {
        RootUser user = new RootUser();
        user.setObjectId(add.getUid());
        UserDao.getInstance()
                .agreeAddFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            //TODO 2、发送同意添加好友的消息
                            sendAgreeAddFriendMessage(add, listener);
                        } else {
                            listener.done(null, e);
                        }
                    }
                });
    }

    /**
     * 发送同意添加好友的消息
     */
    //TODO 好友管理：9.8、发送同意添加好友
    private void sendAgreeAddFriendMessage(final NewFriend add, final SaveListener<Object> listener) {
        BmobIMUserInfo info = new BmobIMUserInfo(add.getUid(), add.getName(), add.getAvatar());
        //TODO 会话：4.1、创建一个暂态会话入口，发送同意好友请求
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        //TODO 消息：5.1、根据会话入口获取消息管理，发送同意好友请求
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        //而AgreeAddFriendMessage的isTransient设置为false，表明我希望在对方的会话数据库中保存该类型的消息
        AgreeAddFriendMessage msg = new AgreeAddFriendMessage();
        final RootUser currentUser = BmobUser.getCurrentUser(RootUser.class);
        msg.setContent("我通过了你的好友验证请求，我们可以开始 聊天了!");//这句话是直接存储到对方的消息表中的
        Map<String, Object> map = new HashMap<>();
        map.put("msg", currentUser.getUsername() + "同意添加你为好友");//显示在通知栏上面的内容
        map.put("uid", add.getUid());//发送者的uid-方便请求添加的发送方找到该条添加好友的请求
        map.put("time", add.getTime());//添加好友的请求时间
        msg.setExtraMap(map);
        messageManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                if (e == null) {//发送成功
                    //TODO 3、修改本地的好友请求记录
                    NewFriendManager.getInstance(activity).updateNewFriend(add, Config.STATUS_VERIFIED);
                    listener.done(msg, e);
                } else {//发送失败
                    listener.done(msg, e);
                }
            }
        });
    }
}
