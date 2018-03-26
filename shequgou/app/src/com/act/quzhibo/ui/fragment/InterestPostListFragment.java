package com.act.quzhibo.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.InterestPostListAdapter;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.bean.InterestPost;
import com.act.quzhibo.bean.InterestPostListInfoParentData;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.ui.activity.SquareActivity;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.LoadNetView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class InterestPostListFragment extends BackHandledFragment {
    XRecyclerView recyclerView;
    ArrayList<InterestPost> posts = new ArrayList<>();
    int interestPostSize;
    InterestPostListAdapter adapter;
    View view;
    String pid;
    LoadNetView loadNetView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.interest_post_layout, null, false);
        loadNetView = (LoadNetView) view.findViewById(R.id.loadview);
        pid = ((SquareActivity) getActivity()).getPid();
        recyclerView = (XRecyclerView) view.findViewById(R.id.interest_post_list);
        ViewDataUtil.setLayManager(new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                getData(pid, "0", Constants.REFRESH);
            }

            @Override
            public void onLoadMore() {
                if (interestPostSize > 0) {
                    getData(pid, ctime, Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }, getActivity(), recyclerView, 1, true, true);

        getData(pid, "0", Constants.REFRESH);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;     //截断事件的传递
            }
        });
        view.findViewById(R.id.sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(posts, new ComparatorValues());
                if (adapter != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(pid, "0", Constants.REFRESH);
            }
        });

        loadNetView.setLoadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(pid, "0", Constants.REFRESH);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionsMenu menu = (FloatingActionsMenu) view.findViewById(R.id.showMenuButton);
        menu.collapse();
    }

    public static class ComparatorValues implements Comparator<InterestPost> {
        @Override
        public int compare(InterestPost post1, InterestPost post2) {
            long m1 = Long.parseLong(post1.ctime != null ? post1.ctime : "0l");
            long m2 = Long.parseLong(post2.ctime != null ? post2.ctime : "0l");
            int result = 0;
            if (m1 > m2) {
                result = -1;
            }
            if (m1 < m2) {
                result = 1;
            }
            return result;
        }
    }

    String ctime;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != Constants.NetWorkError) {
                final InterestPostListInfoParentData data =
                        CommonUtil.parseJsonWithGson((String) msg.obj, InterestPostListInfoParentData.class);
                if (msg.what == Constants.REFRESH) {
                    posts.clear();
                }
                if (data.result != null && data.result.size() > 0) {
                    interestPostSize = data.result.size();
                    posts.addAll(data.result);
                } else {
                    interestPostSize = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }

                if (interestPostSize > 0) {
                    ctime = data.result.get(interestPostSize - 1).ctime;
                }
                if (adapter == null) {
                    adapter = new InterestPostListAdapter(getActivity(), posts, false);
                    adapter.setOnItemClickListener(new InterestPostListAdapter.OnInterestPostRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(InterestPost post) {
                            IntersetPostDetailFragment fragment = new IntersetPostDetailFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.POST, post);
                            fragment.setArguments(bundle);
                            ViewDataUtil.switchFragment(fragment, R.id.square_interest_plates_layout, getActivity());
                        }
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                loadNetView.setVisibility(View.GONE);
                if (posts.size() == 0) {
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

    public void getData(final String pid, final String htime, final int what) {
        if (pid == null) {
            handler.sendEmptyMessage(Constants.NetWorkError);
            return;
        }
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url = toggles.get(0).squareInterestPost.replace("PID", pid).replace("HTIME", htime);
                    ;
                    OkHttpClientManager.parseRequest(getActivity(), url, handler, what);
                } else {
                    handler.sendEmptyMessage(Constants.NetWorkError);
                }
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
