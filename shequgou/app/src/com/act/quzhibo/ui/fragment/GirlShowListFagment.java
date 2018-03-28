package com.act.quzhibo.ui.fragment;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.GirListAdapter;
import com.act.quzhibo.bean.Data;
import com.act.quzhibo.bean.ShowDetailList;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.ui.activity.VideoShowOneActivity;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.LoadNetView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;


public class GirlShowListFagment extends Fragment {
    XRecyclerView recyclerView;
    ArrayList<Data> details = new ArrayList<>();
    int girsSize;
    GirListAdapter adapter;
    View view;
    String categoryId;
    LoadNetView loadNetView;
    private String startPage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_layout, null, false);
        loadNetView = (LoadNetView) view.findViewById(R.id.loadview);
        if (getArguments() != null) {
            categoryId = getArguments().getString(Constants.CATEGORY_ID);
        }
        recyclerView = (XRecyclerView) view.findViewById(R.id.recyclerview);
        ViewDataUtil.setLayManager(new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                getData(categoryId, "1", Constants.REFRESH);
            }

            @Override
            public void onLoadMore() {
                if (girsSize > 0) {
                    getData(categoryId, (Integer.parseInt(startPage)+1)+"", Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }, getActivity(), recyclerView, 2, true, true);

        getData(categoryId, "1", Constants.REFRESH);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;     //截断事件的传递
            }
        });

        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(categoryId, "1", Constants.REFRESH);
            }
        });

        loadNetView.setLoadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(categoryId, "1", Constants.REFRESH);
            }
        });
        return view;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != Constants.NetWorkError) {
                ShowDetailList result = CommonUtil.parseJsonWithGson((String) msg.obj, ShowDetailList.class);
                startPage =result.currPage;
                ArrayList<Data> girlDetail = result.data;
                if (msg.what == Constants.REFRESH) {
                    details.clear();
                    startPage = "1";
                }
                if (girlDetail != null && girlDetail.size() > 0) {
                    girsSize = girlDetail.size();
                    details.addAll(girlDetail);
                } else {
                    girsSize = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                if (adapter == null) {
                    adapter = new GirListAdapter(getActivity(), details, size);
                    adapter.setOnItemClickListener(new GirListAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent=  new Intent(getActivity(), VideoShowOneActivity.class);
                            intent.putParcelableArrayListExtra("videoDatas",details);
                            intent.putExtra("position",position+"");
                            intent.putExtra("cover", details.get(position).cover );
                            getActivity().startActivity(intent);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                loadNetView.setVisibility(View.GONE);
                if (details.size() == 0) {
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

    public void getData(final String categoryId, String startPage, final int what) {
        if (categoryId == null) {
            handler.sendEmptyMessage(Constants.NetWorkError);
            return;
        }
        String url = "http://youmei.xiumei99.com/homepage";
        OkHttpClientManager.parseRequestGirlHomePage(getActivity(), url, handler, what, categoryId, startPage);
    }


}
