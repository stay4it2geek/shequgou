package com.act.quzhibo.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.ContactAdapterChat;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.i.IMutlipleItem;
import com.act.quzhibo.bean.Friend;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.event.RefreshEvent;
import com.act.quzhibo.im.UserDao;
import com.act.quzhibo.ui.activity.ChatActivity;
import com.act.quzhibo.ui.activity.LoginActivity;
import com.act.quzhibo.ui.activity.NewFriendActivity;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.github.promeg.pinyinhelper.Pinyin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 联系人界面
 */
public class ContactFragment extends Fragment {

    @Bind(R.id.rc_view)
    RecyclerView rc_view;
    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;
    ContactAdapterChat adapter;
    LinearLayoutManager layoutManager;
    private IMutlipleItem<Friend> mutlipleItem;
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        ButterKnife.bind(this, rootView);
        if (BmobUser.getCurrentUser(RootUser.class) != null) {
            rootView.findViewById(R.id.tips_rl).setVisibility(View.GONE);
            rootView.findViewById(R.id.sw_refresh).setVisibility(View.VISIBLE);
        }
        rootView.findViewById(R.id.goToLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        return rootView;
    }


    private void setListener() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                sw_refresh.setRefreshing(true);
                query();
            }
        });
        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                query();
            }
        });
        adapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {//跳转到新朋友页面
                    startActivity(new Intent(getActivity(), NewFriendActivity.class));
                } else {
                    if(BmobIM.getInstance().getCurrentStatus().getMsg().equals("connected")){
                        Friend friend = adapter.getItem(position);
                        RootUser user = friend.getFriendUser();
                        BmobIMUserInfo info = new BmobIMUserInfo(user.getObjectId(), user.getUsername(), user.getAvatar());
                        //TODO 会话：4.1、创建一个常态会话入口，好友聊天
                        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("c", conversationEntrance);
                        startActivity(intent);
                    }else{
                        ToastUtil.showToast(getActivity(),"网络不给力哦！");
                    }

                }
            }

            @Override
            public boolean onItemLongClick(final int position,View view) {
                if (position == 0) {
                    return true;
                }
                FragmentDialog.newInstance(false, "是否删除好友？", "删除后不可恢复", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                        UserDao.getInstance().deleteFriend(adapter.getItem(position),
                                new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            ToastUtil.showToast(getActivity(), "好友删除成功");
                                            adapter.remove(position);
                                        } else {
                                            ToastUtil.showToast(getActivity(), "好友删除失败：");
                                        }
                                    }
                                });

                    }

                    @Override
                    public void onNegtiveClick(Dialog dialog) {
                        dialog.dismiss();

                    }
                }).show(getActivity().getSupportFragmentManager(), "");

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BmobUser.getCurrentUser(RootUser.class) != null) {
            rootView.findViewById(R.id.sw_refresh).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.tips_rl).setVisibility(View.GONE);
            mutlipleItem = new IMutlipleItem<Friend>() {

                @Override
                public int getItemViewType(int postion, Friend friend) {
                    if (postion == 0) {
                        return ContactAdapterChat.TYPE_NEW_FRIEND;
                    } else {
                        return ContactAdapterChat.TYPE_ITEM;
                    }
                }

                @Override
                public int getItemLayoutId(int viewtype) {
                    if (viewtype == ContactAdapterChat.TYPE_NEW_FRIEND) {
                        return R.layout.header_new_friend;
                    } else {
                        return R.layout.item_contact;
                    }
                }

                @Override
                public int getItemCount(List<Friend> list) {
                    return list.size() + 1;
                }
            };

            adapter = new ContactAdapterChat(getActivity(), mutlipleItem, null);
            rc_view.setAdapter(adapter);
            layoutManager = new LinearLayoutManager(getActivity());
            rc_view.setLayoutManager(layoutManager);
            sw_refresh.setEnabled(true);
            setListener();
            sw_refresh.setRefreshing(true);
            query();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * 注册自定义消息接收事件
     *
     * @param event
     */
    @Subscribe
    public void onEventAsync(RefreshEvent event) {
        if (!MyApplicaition.handler.isChatting) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 查询本地会话
     */
    public void query() {
        //TODO 【好友管理】获取好友列表
        UserDao.getInstance().queryFriends(

                new FindListener<Friend>() {
                    @Override
                    public void done(List<Friend> list, BmobException e) {
                        if (e == null) {
                            List<Friend> friends = new ArrayList<>();
                            friends.clear();
                            //添加首字母
                            for (int i = 0; i < list.size(); i++) {
                                Friend friend = list.get(i);
                                String username = friend.getFriendUser().getUsername();
                                if (username != null) {
                                    String pinyin = Pinyin.toPinyin(username.charAt(0));
                                    friend.setPinyin(pinyin.substring(0, 1).toUpperCase());
                                    friends.add(friend);
                                }
                            }
                            adapter.bindDatas(friends);
                            adapter.notifyDataSetChanged();
                            sw_refresh.setRefreshing(false);
                        } else {
                            adapter.bindDatas(null);
                            adapter.notifyDataSetChanged();
                            sw_refresh.setRefreshing(false);
                        }
                    }
                }


        );
    }

}
