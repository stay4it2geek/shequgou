package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.MyFocusGirlListAdapter;
import com.act.quzhibo.bean.MyFocusGirl;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class MyFocusGirlsActvity extends AppCompatActivity {


    XRecyclerView recyclerView;
    MyFocusGirlListAdapter adapter;
    LoadNetView loadNetView;
    String lastTime = "";
    ArrayList<MyFocusGirl> girls = new ArrayList<>();
    int myfocusSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle("我关注的女神");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFocusGirlsActvity.this.finish();
            }
        });
        loadNetView = (LoadNetView) findViewById(R.id.loadview);
        recyclerView = (XRecyclerView) findViewById(R.id.recyclerview);
        ViewDataUtil.setLayManager(new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                queryData(Constants.REFRESH);
            }

            @Override
            public void onLoadMore() {
                if (myfocusSize > 0) {
                    queryData(Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }, this, recyclerView, 2, true, true);
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
        queryData(Constants.REFRESH);

    }

    void queryData(final int actionType) {
        BmobQuery<MyFocusGirl> query = new BmobQuery<>();

        List<BmobQuery<MyFocusGirl>> queries = new ArrayList<>();


        if (actionType == Constants.LOADMORE) {
            Date date = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = sdf.parse(lastTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            query.addWhereLessThanOrEqualTo("updatedAt", new BmobDate(date));
        }

        BmobQuery<MyFocusGirl> query2 = new BmobQuery<>();

        query2.addWhereEqualTo("rootUser", BmobUser.getCurrentUser(RootUser.class));
        queries.add(query2);
        query.and(queries);
        query.setLimit(10);
        query.order("-updatedAt");
        query.findObjects(new FindListener<MyFocusGirl>() {
            @Override
            public void done(List<MyFocusGirl> list, BmobException e) {
                if (e == null) {
                    if (actionType == Constants.REFRESH) {
                        girls.clear();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
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
            ArrayList<MyFocusGirl> girlResult = (ArrayList<MyFocusGirl>) msg.obj;
            if (msg.what != Constants.NetWorkError) {

                if (girlResult != null && girlResult.size() > 0) {
                    girls.addAll(girlResult);
                    myfocusSize = girls.size();
                } else {
                    myfocusSize = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }

                if (adapter == null) {
                    Display display = MyFocusGirlsActvity.this.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;
                    adapter = new MyFocusGirlListAdapter(MyFocusGirlsActvity.this, girls, screenWidth);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new MyFocusGirlListAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position, final MyFocusGirl MyFocusGirl) {

                        }
                    });
                    if (adapter != null) {
                        adapter.setDeleteListener(new MyFocusGirlListAdapter.OnDeleteListener() {
                            @Override
                            public void onDelete(final int position) {
                                FragmentDialog.newInstance(false, getResources().getString(R.string.isCancelFocus), getResources().getString(R.string.reallyCancelFocus), "取消", "确定", "", "", false, new FragmentDialog.OnClickBottomListener() {
                                    @Override
                                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegtiveClick(Dialog dialog) {
                                        girls.get(position).delete(girls.get(position).getObjectId(), new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                if (e == null) {
                                                    girls.remove(position);
                                                    adapter.notifyDataSetChanged();
                                                    if (girls.size() == 0) {
                                                        loadNetView.setVisibility(View.VISIBLE);
                                                        loadNetView.setlayoutVisily(Constants.NO_DATA);
                                                        return;
                                                    }
                                                }
                                            }
                                        });
                                        dialog.dismiss();
                                    }
                                }).show(getSupportFragmentManager(), "");
                            }
                        });
                    }
                } else {
                    adapter.notifyDataSetChanged();
                }

                loadNetView.setVisibility(View.GONE);
                if (girls.size() == 0) {
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
