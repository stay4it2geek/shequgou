package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.MyPostListAdapter;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.MyPost;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteBatchListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.act.quzhibo.ui.activity.PostAddActivity.EXTRA_MOMENT;

public class MyPostListActivity extends AppCompatActivity {
    public static final int UPLOAD_POST = 1;
    ArrayList<MyPost> myPostList = new ArrayList<>();
    XRecyclerView recyclerView;
    MyPostListAdapter myPostListAdapter;
    LoadNetView loadNetView;
    String lastTime = "";
    int handlerMyPostsSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common);
        recyclerView = (XRecyclerView) findViewById(R.id.recyclerview);
        ViewDataUtil.setLayManager(new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                queryData(Constants.REFRESH);
            }

            @Override
            public void onLoadMore() {
                if (handlerMyPostsSize > 0) {
                    queryData(Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }, this, recyclerView, 1, true, true);

        loadNetView = (LoadNetView) findViewById(R.id.loadview);
        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                queryData(Constants.REFRESH);
            }
        });
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setVisibility(View.VISIBLE);
        findViewById(R.id.showMenuButton).setVisibility(View.VISIBLE);
        titlebar.setBarTitle("我 的 状 态");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPostListActivity.this.finish();
            }
        });
        findViewById(R.id.textBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPostListActivity.this, PostAddActivity.class);
                intent.putExtra("postType", 1);
                startActivityForResult(intent, UPLOAD_POST);
            }
        });
        findViewById(R.id.videoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPostListActivity.this, PostAddActivity.class);
                intent.putExtra("postType", 2);
                startActivityForResult(intent, UPLOAD_POST);


            }
        });
        findViewById(R.id.photoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPostListActivity.this, PostAddActivity.class);
                intent.putExtra("postType", 3);
                startActivityForResult(intent, UPLOAD_POST);
            }
        });
        loadNetView.setLoadButtonListener(new View.OnClickListener() {
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


    void queryData(final int actionType) {
        BmobQuery<MyPost> query = new BmobQuery<>();
        BmobQuery<MyPost> query2 = new BmobQuery<>();
        List<BmobQuery<MyPost>> queries = new ArrayList<>();
        if (actionType == Constants.LOADMORE) {
            Date date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = sdf.parse(lastTime);
                query2.addWhereLessThanOrEqualTo("updatedAt", new BmobDate(date));
                queries.add(query2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        BmobQuery<MyPost> query3 = new BmobQuery<>();

        query3.addWhereEqualTo("user", BmobUser.getCurrentUser(RootUser.class));
        queries.add(query3);
        query.and(queries);
        query.setLimit(10);
        query.order("-updatedAt");
        query.findObjects(new FindListener<MyPost>() {
            @Override
            public void done(List<MyPost> list, BmobException e) {
                if (e == null) {
                    if (actionType == Constants.REFRESH) {
                        myPostList.clear();
                        if (myPostListAdapter != null) {
                            myPostListAdapter.notifyDataSetChanged();
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
            ArrayList<MyPost> myPosts = (ArrayList<MyPost>) msg.obj;
            if (msg.what != Constants.NetWorkError) {

                if (myPosts != null&& myPosts.size()>0) {
                    myPostList.addAll(myPosts);
                    handlerMyPostsSize = myPosts.size();
                } else {
                    handlerMyPostsSize = 0;
                    if (msg.what == Constants.LOADMORE) {
                        recyclerView.setNoMore(true);
                    }
                }

                setAdapterView();
                loadNetView.setVisibility(View.GONE);
                if (myPostList.size() == 0) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD_POST && resultCode == RESULT_OK) {
            MyPost myPost = (MyPost) data.getSerializableExtra(EXTRA_MOMENT);
            myPostList.add(0, myPost);
            setAdapterView();
        }

    }

    void setAdapterView() {
        if (myPostListAdapter == null) {
            myPostListAdapter = new MyPostListAdapter(MyPostListActivity.this, myPostList);
            recyclerView.setAdapter(myPostListAdapter);
            myPostListAdapter.setOnItemClickListener(new MyPostListAdapter.OnMyPostRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(MyPost post) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.POST, post);
                    intent.setClass(MyPostListActivity.this, MyPostDetailActivity.class);
                    startActivityForResult(intent, UPLOAD_POST);
                }

                @Override
                public void onItemDelteClick(final int postion, final MyPost post, ImageView menu, final ArrayList<String> imgs) {
                    final PopupMenu popupMenu = new PopupMenu(MyPostListActivity.this, menu, Gravity.RIGHT);
                    popupMenu.getMenuInflater().inflate(R.menu.post_manger_menu_list, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();
                            if (itemId == R.id.delete) {
                                if (post.type.equals("2")) {
                                    final BmobFile bmobOldFile = new BmobFile();
                                    bmobOldFile.setUrl(post.vedioUrl + "");
                                    bmobOldFile.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {

                                        }
                                    });
                                } else if (post.type.equals("3")) {
                                    String[] urls = imgs.toArray(new String[imgs.size()]);

                                    BmobFile.deleteBatch(urls, new DeleteBatchListener() {

                                        @Override
                                        public void done(String[] failUrls, BmobException e) {

                                        }
                                    });
                                }

                                post.delete(new UpdateListener() {

                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            myPostList.remove(postion);
                                            myPostListAdapter.notifyDataSetChanged();
                                            if (myPostList.size() == 0) {
                                                loadNetView.setVisibility(View.VISIBLE);
                                                loadNetView.setlayoutVisily(Constants.NO_DATA);
                                                return;
                                            }
                                        } else {

                                        }
                                    }
                                });
                            }
                            popupMenu.dismiss();
                            return false;
                        }
                    });
                    popupMenu.show();
                }

            });
        } else {
            myPostListAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.showMenuButton);
        menu.collapse();
        queryData(Constants.REFRESH);
    }
}
