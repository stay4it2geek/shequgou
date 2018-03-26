package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.InterestPostListAdapter;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.bean.InterestPost;
import com.act.quzhibo.bean.InterestPostListInfoPersonParentData;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class IntersetPersonPostListActivity extends FragmentActivity {

     XRecyclerView recyclerView;
     ArrayList<InterestPost> posts = new ArrayList<>();
     int interestPostSize;
     InterestPostListAdapter adapter;
     String userId;
     String ctime = "0";
     LoadNetView loadNetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interest_post_layout);
        findViewById(R.id.titlebar).setVisibility(View.VISIBLE);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle("ta的情趣状态");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntersetPersonPostListActivity.this.finish();
            }
        });
        loadNetView = (LoadNetView) findViewById(R.id.loadview);
        userId = getIntent().getStringExtra(Constants.COMMON_USER_ID);
        recyclerView = (XRecyclerView) findViewById(R.id.interest_post_list);
        ViewDataUtil.setLayManager(new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                getData("0", Constants.REFRESH);
            }

            @Override
            public void onLoadMore() {
                if (interestPostSize > 0) {
                    getData(ctime, Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }

            }
        },this,recyclerView,1,true,true);


        getData("0", Constants.REFRESH);


        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData("0", Constants.REFRESH);
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != Constants.NetWorkError) {
                final InterestPostListInfoPersonParentData data =
                        CommonUtil.parseJsonWithGson((String) msg.obj, InterestPostListInfoPersonParentData.class);
                if (data != null && data.result != null) {
                    interestPostSize = data.result.posts.size();
                } else {
                    interestPostSize = 0;
                }

                if (data.result.posts != null && interestPostSize > 0) {
                    ctime = data.result.posts.get(interestPostSize - 1).ctime;
                }
                if (msg.what == Constants.REFRESH) {
                    posts.clear();
                }
                if (posts != null && interestPostSize > 0) {
                    posts.addAll(data.result.posts);
                    if (adapter == null) {
                        adapter = new InterestPostListAdapter(IntersetPersonPostListActivity.this, posts,true);
                        adapter.setPersonIndex(getIntent().getIntExtra("FocusPersonIndex",0));
                        adapter.setOnItemClickListener(new InterestPostListAdapter.OnInterestPostRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(InterestPost post) {
                                RootUser rootUser = BmobUser.getCurrentUser(RootUser.class);
                                if (rootUser == null) {
                                    FragmentDialog.newInstance(false, "请确认您的权限", "你是否未注册或者登录？", "去注册", "去登录","","",false, new FragmentDialog.OnClickBottomListener() {
                                        @Override
                                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                                            startActivity(new Intent(IntersetPersonPostListActivity.this, RegisterActivity.class));

                                        }

                                        @Override
                                        public void onNegtiveClick(Dialog dialog) {
                                            startActivity(new Intent(IntersetPersonPostListActivity.this, LoginActivity.class));

                                        }
                                    }).show(getSupportFragmentManager(), "");
                                } else if (!rootUser.isVip) {
                                    FragmentDialog.newInstance(false, "请确认您的会员权限", "您还不是VIP会员哦", "成为VIP", "取消","","",false, new FragmentDialog.OnClickBottomListener() {
                                        @Override
                                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                                            startActivity(new Intent(IntersetPersonPostListActivity.this, GetVipPayActivity.class));
                                        }

                                        @Override
                                        public void onNegtiveClick(Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show(getSupportFragmentManager(), "");

                                }
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                } else if (interestPostSize == 0) {
                    recyclerView.setNoMore(true);
                }

                loadNetView.setVisibility(View.GONE);
            } else {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.RELOAD);
            }
        }
    };


    public void getData(final String ctime, final int what) {
        if (userId == null) {
            handler.sendEmptyMessage(Constants.NetWorkError);
            return;
        }
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url = toggles.get(0).interestPersonPostLs.replace("USERID", userId).replace("CTIME", ctime);
                    OkHttpClientManager.parseRequest(IntersetPersonPostListActivity.this, url, handler, what);
                }else{
                    handler.sendEmptyMessage(Constants.NetWorkError);
                }
            }
        });
    }

}
