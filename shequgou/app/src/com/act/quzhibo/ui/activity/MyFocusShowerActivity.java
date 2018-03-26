package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.MyFocusShowerListAdapter;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.bean.MyFocusShower;
import com.act.quzhibo.bean.Room;
import com.act.quzhibo.download.event.FocusChangeEvent;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

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

import static com.act.quzhibo.R.drawable.post;

public class MyFocusShowerActivity extends FragmentActivity {

    XRecyclerView recyclerView;
    MyFocusShowerListAdapter adapter;
    LoadNetView loadNetView;
    String lastTime = "";
    ArrayList<MyFocusShower> showers = new ArrayList<>();
    int myfocusSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle("我关注的主播");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFocusShowerActivity.this.finish();
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
        EventBus.getDefault().register(this);

    }

    void queryData(final int actionType) {
        BmobQuery<MyFocusShower> query = new BmobQuery<>();

        List<BmobQuery<MyFocusShower>> queries = new ArrayList<>();


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

        BmobQuery<MyFocusShower> query2 = new BmobQuery<>();

        query2.addWhereEqualTo("rootUser", BmobUser.getCurrentUser(RootUser.class));
        queries.add(query2);
        query.and(queries);
        query.setLimit(10);
        query.order("-updatedAt");
        query.findObjects(new FindListener<MyFocusShower>() {
            @Override
            public void done(List<MyFocusShower> list, BmobException e) {
                if (e == null) {
                    if (actionType == Constants.REFRESH) {
                        showers.clear();
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
            ArrayList<MyFocusShower> showerses = (ArrayList<MyFocusShower>) msg.obj;
            if (msg.what != Constants.NetWorkError) {

                if (showerses != null && showerses.size() > 0) {
                    showers.addAll(showerses);
                    myfocusSize = showerses.size();
                } else {
                    myfocusSize = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }

                if (adapter == null) {
                    Display display = MyFocusShowerActivity.this.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;
                    adapter = new MyFocusShowerListAdapter(MyFocusShowerActivity.this, showers, screenWidth);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new MyFocusShowerListAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position, final MyFocusShower myFocusShower) {
                            requestInfo(myFocusShower.userId);
                        }
                    });
                    if (adapter != null) {
                        adapter.setDeleteListener(new MyFocusShowerListAdapter.OnDeleteListener() {
                            @Override
                            public void onDelete(final int position) {
                                FragmentDialog.newInstance(false, getResources().getString(R.string.isCancelFocus), getResources().getString(R.string.reallyCancelFocus), "取消", "确定", "", "", false, new FragmentDialog.OnClickBottomListener() {
                                    @Override
                                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegtiveClick(Dialog dialog) {
                                        showers.get(position).delete(showers.get(position).getObjectId(), new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                if (e == null) {
                                                    showers.remove(position);
                                                    adapter.notifyDataSetChanged();
                                                    if (showers.size() == 0) {
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
                if (showers.size() == 0) {
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
    protected void onResume() {
        super.onResume();
    }
    @Subscribe
    public void onEventMainThread(FocusChangeEvent event) {
        if (event.type.equals("show") && !event.focus ) {
            showers.remove(event.position);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }
    Handler infoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                JSONObject jsonObject = new JSONObject((String) msg.obj);
                Room room = new Room();
                room.screenType = jsonObject.isNull("screenType") ? "" : jsonObject.getString("screenType");
                room.roomId = jsonObject.isNull("roomId") ? "" : jsonObject.getString("roomId");
                room.nickname = jsonObject.isNull("nickname") ? "" : jsonObject.getString("nickname");
                room.userId = jsonObject.isNull("userId") ? "" : jsonObject.getString("userId");
                room.gender = jsonObject.isNull("gender") ? "" : jsonObject.getString("gender");
                room.liveStream  = "http://pull.kktv8.com/livekktv/" + room.roomId + ".flv";
                room.city = jsonObject.isNull("city") ? "" : jsonObject.getString("city");
                room.liveType = jsonObject.isNull("liveType") ? "" : jsonObject.getString("liveType");
                room.nickname = jsonObject.isNull("nickname") ? "" : jsonObject.getString("nickname");
                room.onlineCount = jsonObject.isNull("onlineCount") ? "" : jsonObject.getString("onlineCount");
                room.portrait_path_1280 = jsonObject.isNull("portrait_path_1280") ? "" : jsonObject.getString("portrait_path_1280");
                Intent intent;
                if (!TextUtils.isEmpty(room.liveStream) && !TextUtils.isEmpty(room.onlineCount) && Integer.parseInt(room.onlineCount) > 10 ) {
                    intent = new Intent(MyFocusShowerActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("showFullScreen", true);
                } else {
                    intent = new Intent(MyFocusShowerActivity.this, ShowerInfoActivity.class);
                    intent.putExtra("showPosition",0);
                    ToastUtil.showToast(MyFocusShowerActivity.this, "该主播未直播哦");
                }
                intent.putExtra("room", room);
                startActivity(intent);
            } catch (JSONException e) {
                ToastUtil.showToast(MyFocusShowerActivity.this, "主播信息加载异常，请稍后重试!");
            }
        }
    };


    void requestInfo(final String showerId) {
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url = toggles.get(0).showerInfo.replace("USERID", showerId);
                    OkHttpClientManager.parseRequest(MyFocusShowerActivity.this, url, infoHandler, Constants.REFRESH);

                }else{
                    handler.sendEmptyMessage(Constants.NetWorkError);
                }
            }
        });

    }


}
