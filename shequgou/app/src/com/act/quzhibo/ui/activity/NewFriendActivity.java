package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.NewFriendAdapterChat;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.i.IMutlipleItem;
import com.act.quzhibo.chat.NewFriend;
import com.act.quzhibo.chat.NewFriendManager;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.TitleBarView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobUser;


/**
 * 新朋友
 */
public class NewFriendActivity extends FragmentActivity {

    @Bind(R.id.ll_root)
    FrameLayout ll_root;
    @Bind(R.id.rc_view)
    RecyclerView rc_view;
    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;
    NewFriendAdapterChat adapter;
    LinearLayoutManager layoutManager;
    IMutlipleItem<NewFriend> mutlipleItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_conversation);
        ButterKnife.bind(this);
        if (BmobUser.getCurrentUser(RootUser.class) != null) {
            findViewById(R.id.tips_rl).setVisibility(View.GONE);
            findViewById(R.id.sw_refresh).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.titlebar).setVisibility(View.VISIBLE);
        //单一布局
        mutlipleItem = new IMutlipleItem<NewFriend>() {

            @Override
            public int getItemViewType(int position, NewFriend c) {
                return 0;
            }

            @Override
            public int getItemLayoutId(int viewtype) {
                return R.layout.item_new_friend;
            }

            @Override
            public int getItemCount(List<NewFriend> list) {
                return list.size();
            }
        };

        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle("新朋友");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewFriendActivity.this.finish();
            }
        });
        adapter = new NewFriendAdapterChat(this, mutlipleItem, null);
        rc_view.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        rc_view.setLayoutManager(layoutManager);
        sw_refresh.setEnabled(true);
        //批量更新未读未认证的消息为已读状态
        NewFriendManager.getInstance(this).updateBatchStatus();
        setListener();

    }

    private void setListener() {
        ll_root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
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

            }

            @Override
            public boolean onItemLongClick(final int position,View view) {

                FragmentDialog.newInstance(false, "是否删除会话？", "删除后不可恢复", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                        NewFriendManager.getInstance(NewFriendActivity.this).deleteNewFriend(adapter.getItem(position));
                        adapter.remove(position);
                    }

                    @Override
                    public void onNegtiveClick(Dialog dialog) {
                        dialog.dismiss();

                    }
                }).show(NewFriendActivity.this.getSupportFragmentManager(), "");
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        sw_refresh.setRefreshing(true);
        query();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 查询本地会话
     */
    public void query() {
        adapter.bindDatas(NewFriendManager.getInstance(this).getAllNewFriend());
        adapter.notifyDataSetChanged();
        sw_refresh.setRefreshing(false);
    }

}
