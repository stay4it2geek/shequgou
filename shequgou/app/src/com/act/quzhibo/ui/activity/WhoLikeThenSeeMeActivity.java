package com.act.quzhibo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.WhoLikeMeAdapter;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class WhoLikeThenSeeMeActivity extends FragmentActivity {
    private ArrayList<InterestSubPerson> interestPersonList = new ArrayList<>();
    private WhoLikeMeAdapter whoLikeMeAdapter;
    private XRecyclerView recyclerView;
    private LoadNetView loadNetView;
    private int handlerLiekThenSeeMeSize;
    private String lastTime = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle(getIntent().getBooleanExtra("userType", false) ? "谁来看过我" : "谁关注了我");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoLikeThenSeeMeActivity.this.finish();
            }
        });
        recyclerView = (XRecyclerView) findViewById(R.id.recyclerview);
        loadNetView = (LoadNetView) findViewById(R.id.loadview);
        ViewDataUtil.setLayManager(new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                queryData(Constants.REFRESH);

            }

            @Override
            public void onLoadMore() {
                if (handlerLiekThenSeeMeSize > 0) {
                    queryData(Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        },this,recyclerView,1,true,true);
        queryData(Constants.REFRESH);
        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                queryData(Constants.REFRESH);
            }
        });
        loadNetView.setLoadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                queryData(Constants.REFRESH);
            }
        });
    }

    private void queryData(final int actionType) {
        List<BmobQuery<InterestSubPerson>> queries = new ArrayList<>();
        BmobQuery<InterestSubPerson> query = new BmobQuery<>();
        BmobQuery<InterestSubPerson> query3 = new BmobQuery<>();

        if (actionType == Constants.LOADMORE) {
            Date date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = sdf.parse(lastTime);
                query.addWhereLessThanOrEqualTo("updatedAt", new BmobDate(date));
                queries.add(query);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        BmobQuery<InterestSubPerson> query2 = new BmobQuery<>();
        if (getIntent().getBooleanExtra("userType", false)) {
            query2.addWhereEqualTo("userSeeFlag", "1");
        } else {
            query2.addWhereEqualTo("userFocusFlag", "2");
        }
        queries.add(query2);
        query3.and(queries);
        query3.setLimit(10);
        query3.order("-distance");
        query3.include("user");
        query3.findObjects(new FindListener<InterestSubPerson>() {
            @Override
            public void done(List<InterestSubPerson> list, BmobException e) {
                if (e == null) {
                    if (actionType == Constants.REFRESH) {
                        interestPersonList.clear();
                        if(whoLikeMeAdapter!=null){
                            whoLikeMeAdapter.notifyDataSetChanged();
                        }
                    }
                    if (list.size() > 0) {
                        lastTime = list.get(list.size() - 1).getUpdatedAt();
                    }
                    Message message = new Message();
                    message.obj = list;
                    message.what = actionType;
                    handler.sendMessage(message);
                } else {
                    handler.sendEmptyMessage(Constants.NetWorkError);
                }
            }
        });
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ArrayList<InterestSubPerson> interestSubPersonsn = (ArrayList<InterestSubPerson>) msg.obj;
            if (msg.what != Constants.NetWorkError) {
                if (interestSubPersonsn != null&& interestSubPersonsn.size()>0)  {
                    interestPersonList.addAll(interestSubPersonsn);
                    handlerLiekThenSeeMeSize = interestSubPersonsn.size();
                } else {
                    handlerLiekThenSeeMeSize = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }
                if (whoLikeMeAdapter == null) {
                    whoLikeMeAdapter = new WhoLikeMeAdapter(WhoLikeThenSeeMeActivity.this, interestPersonList);
                    recyclerView.setAdapter(whoLikeMeAdapter);
                } else {
                    whoLikeMeAdapter.notifyDataSetChanged();
                }
                loadNetView.setVisibility(View.GONE);
                if (interestPersonList.size() == 0) {
                    loadNetView.setVisibility(View.VISIBLE);
                    loadNetView.setlayoutVisily(Constants.NO_DATA);
                    return;
                }
            } else {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.RELOAD);
            }
        }
    };
}
