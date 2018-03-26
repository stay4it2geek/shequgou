package com.act.quzhibo.ui.fragment;

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
import com.act.quzhibo.adapter.ConversationAdapterChat;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.i.IMutlipleItem;
import com.act.quzhibo.bean.Conversation;
import com.act.quzhibo.bean.NewFriendConversation;
import com.act.quzhibo.bean.PrivateConversation;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.chat.NewFriend;
import com.act.quzhibo.chat.NewFriendManager;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.event.RefreshEvent;
import com.act.quzhibo.ui.activity.LoginActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.v3.BmobUser;

public class ConversationFragment extends Fragment {

    @Bind(R.id.rc_view)
    RecyclerView rc_view;
    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;
    ConversationAdapterChat adapter;
    LinearLayoutManager layoutManager;
    private IMutlipleItem<Conversation> mutlipleItem;
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
                adapter.getItem(position).onClick(getActivity());
            }

            @Override
            public boolean onItemLongClick(int position,View view) {
                adapter.getItem(position).onLongClick(getActivity(),adapter,position);

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
            mutlipleItem = new IMutlipleItem<Conversation>() {

                @Override
                public int getItemViewType(int postion, Conversation c) {
                    return 0;
                }

                @Override
                public int getItemLayoutId(int viewtype) {
                    return R.layout.item_conversation;
                }

                @Override
                public int getItemCount(List<Conversation> list) {
                    return list.size();
                }
            };
            adapter = new ConversationAdapterChat(getActivity(), mutlipleItem, null);
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
    public void onPause() {
        super.onPause();
    }


    /**
     * 查询本地会话
     */
    public void query() {
        adapter.bindDatas(getConversations());
        adapter.notifyDataSetChanged();
        sw_refresh.setRefreshing(false);
    }

    /**
     * 获取会话列表的数据：增加新朋友会话
     *
     * @return
     */
    private List<Conversation> getConversations() {
        //添加会话
        List<Conversation> conversationList = new ArrayList<>();
        conversationList.clear();
        //TODO 会话：4.2、查询全部会话
        List<BmobIMConversation> list = BmobIM.getInstance().loadAllConversation();
        if (list != null && list.size() > 0) {
            for (BmobIMConversation item : list) {
                switch (item.getConversationType()) {
                    case 1://私聊
                        conversationList.add(new PrivateConversation(item));
                        break;
                    default:
                        break;
                }
            }
        }
        //添加新朋友会话-获取好友请求表中最新一条记录
        List<NewFriend> friends = NewFriendManager.getInstance(getActivity()).getAllNewFriend();
        if (friends != null && friends.size() > 0) {
            conversationList.add(new NewFriendConversation(friends.get(0)));
        }
        //重新排序
        Collections.sort(conversationList);
        return conversationList;
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

        //因为新增`新朋友`这种会话类型
        if (!MyApplicaition.handler.isChatting) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    adapter.bindDatas(getConversations());
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 注册离线消息接收事件
     *
     * @param event
     */
    @Subscribe
    public void onEventAsync(OfflineMessageEvent event) {
        if (!MyApplicaition.handler.isChatting) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    adapter.bindDatas(getConversations());
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 注册消息接收事件
     *
     * @param event 1、与用户相关的由开发者自己维护，SDK内部只存储用户信息
     *              2、开发者获取到信息后，可调用SDK内部提供的方法更新会话
     */
    @Subscribe
    public void onEventAsync(MessageEvent event) {
        if (!MyApplicaition.handler.isChatting) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    adapter.bindDatas(getConversations());
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
