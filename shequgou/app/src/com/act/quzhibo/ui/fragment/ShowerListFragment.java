package com.act.quzhibo.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.RoomListAdapter;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.bean.Room;
import com.act.quzhibo.bean.RoomParentList;
import com.act.quzhibo.ui.activity.InfoInterestPersonActivity;
import com.act.quzhibo.ui.activity.ShowerListActivity;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.widget.FragmentDialog;
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

public class ShowerListFragment extends BackHandledFragment {
     String categoryId;
     OnCallShowViewListner onCallShowViewListner;
     XRecyclerView recyclerView;
     ArrayList<Room> rooms = new ArrayList<>();
     int roomTotal = 0;
     int mCurrentCounter = 0;
     int mLastRequstRoomListSize;
     RoomListAdapter adapter;
     int page;
     String offset;
     String categoryTitle;
     View view;
     LoadNetView loadNetView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ShowerListActivity) {
            onCallShowViewListner = (OnCallShowViewListner) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionsMenu menu = (FloatingActionsMenu) view.findViewById(R.id.showMenuButton);
        menu.collapse();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.showlist_fragment, null);
        loadNetView = (LoadNetView) view.findViewById(R.id.loadview);
        recyclerView = (XRecyclerView) view.findViewById(R.id.recyclerview);
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
                        getData(categoryId, "0", Constants.REFRESH);
                        recyclerView.refreshComplete();
                    }
                }, 1000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentCounter < roomTotal && mLastRequstRoomListSize == 0) {
                            getData(categoryId, String.valueOf(page), Constants.LOADMORE);
                            recyclerView.loadMoreComplete();
                        } else {
                            recyclerView.setNoMore(true);
                        }
                    }
                }, 1000);
            }
        });

        categoryId = getArguments().getString(Constants.CATEGORY_ID);
        categoryTitle = getArguments().getString(Constants.CATEGORY_TITLE);
        if (categoryTitle.equals("手机达人") || categoryTitle.contains("手机")) {
            offset = "20";
        } else {
            offset = "40";

        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(categoryId, "0", Constants.REFRESH);
            }
        });

        view.findViewById(R.id.openShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialog.newInstance(false, "温馨提示", "系统升级维护中,暂时关闭开播\n通道,开放时间请等待系统通知", "我知道了", "", "", "", true, new FragmentDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegtiveClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).show(getFragmentManager(), "");
            }
        });
        getData(categoryId, "0", Constants.REFRESH);

        loadNetView.setLoadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(categoryId, "0", Constants.REFRESH);
            }
        });

        view.findViewById(R.id.sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(rooms, new ComparatorValues());
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
        return view;
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RoomParentList roomParentList = CommonUtil.parseJsonWithGson((String) msg.obj, RoomParentList.class);
            if (msg.what != Constants.NetWorkError) {
                if (msg.what == Constants.REFRESH) {
                    page = 20;
                    rooms.clear();
                    mCurrentCounter = 0;
                    roomTotal = Integer.parseInt(roomParentList.roomTotal);
                    mLastRequstRoomListSize = 0;
                } else if (msg.what == Constants.LOADMORE) {
                    page = page * 2;
                    mCurrentCounter += roomParentList.roomList.size();
                    mLastRequstRoomListSize = roomParentList.roomList.size();
                }

                rooms.addAll(roomParentList.roomList);
                if (roomParentList == null || roomParentList.roomList == null || roomParentList.roomList.size() == 0) {
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }
                if (adapter == null) {
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;
                    adapter = new RoomListAdapter(getActivity(), rooms, roomParentList.pathPrefix, screenWidth, categoryTitle);
                    adapter.setOnItemClickListener(new RoomListAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            onCallShowViewListner.onShowVideo(rooms.get(position));
                        }
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                loadNetView.setVisibility(View.GONE);

                if (rooms.size() == 0) {
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

    public static class ComparatorValues implements Comparator<Room> {
        @Override
        public int compare(Room room1, Room room2) {
            int m1 = Integer.parseInt(room1.onlineCount != null && !room1.onlineCount.equals("0") ? room1.onlineCount : "0");
            int m2 = Integer.parseInt(room2.onlineCount != null && !room2.onlineCount.equals("0") ? room2.onlineCount : "0");
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

    public void getData(final String categoryId, final String startPage, final int what) {
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url = toggles.get(0).commonTabDetail.replace("CATEID", categoryId).replace("NUM", String.valueOf(startPage)).replace("OFFSET", offset);
                    OkHttpClientManager.parseRequest(getActivity(), url, handler, what);
                }else{
                    handler.sendEmptyMessage(Constants.NetWorkError);
                }
            }
        });

    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

    public interface OnCallShowViewListner {
        void onShowVideo(Room room);
    }

}