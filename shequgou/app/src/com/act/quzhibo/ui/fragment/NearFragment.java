package com.act.quzhibo.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.NearPersonAdapter;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.widget.LoadNetView;
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

public class NearFragment extends BackHandledFragment {
     ArrayList<InterestSubPerson> nearPersonList = new ArrayList<>();
     NearPersonAdapter nearPersonAdapter;
     XRecyclerView recyclerView;
     LoadNetView loadNetView;
     int nearPersonSizeHandler;
    public String lastTime;
     View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_common, null, false);
        recyclerView = (XRecyclerView) view.findViewById(R.id.recyclerview);
        loadNetView = (LoadNetView) view.findViewById(R.id.loadview);
        recyclerView.setPullRefreshEnabled(true);
        recyclerView.setLoadingMoreEnabled(true);
        recyclerView.setLoadingMoreProgressStyle(R.style.Small);
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setNoMore(false);
                        recyclerView.setLoadingMoreEnabled(true);
                        queryData(Constants.REFRESH);
                        recyclerView.refreshComplete();
                    }
                }, 1000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (nearPersonSizeHandler > 0) {
                            queryData(Constants.LOADMORE);
                            recyclerView.loadMoreComplete();
                        } else {
                            recyclerView.setNoMore(true);
                        }
                    }
                }, 1000);
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
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
        return view;
    }

     void queryData(final int actionType) {
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
        query2.addWhereLessThanOrEqualTo("distance", "50");
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
                        nearPersonList.clear();
                        if(nearPersonAdapter!=null){
                            nearPersonAdapter.notifyDataSetChanged();
                        }                    }
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
            ArrayList<InterestSubPerson> personList = (ArrayList<InterestSubPerson>) msg.obj;
            if (msg.what != Constants.NetWorkError) {
                if (nearPersonList != null) {
                    nearPersonSizeHandler = personList.size();
                    nearPersonList.addAll(personList);
                } else {
                    nearPersonSizeHandler = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }

                if (nearPersonAdapter == null) {
                    nearPersonAdapter = new NearPersonAdapter(getActivity(), nearPersonList);
                    recyclerView.setAdapter(nearPersonAdapter);
                } else {
                    nearPersonAdapter.notifyDataSetChanged();
                }

                loadNetView.setVisibility(View.GONE);
                if (nearPersonList.size() == 0) {
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

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
