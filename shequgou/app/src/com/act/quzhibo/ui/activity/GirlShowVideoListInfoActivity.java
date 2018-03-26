package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.VideoListInfoAdapter;
import com.act.quzhibo.bean.Data;
import com.act.quzhibo.bean.GirlVideoListInfo;
import com.act.quzhibo.bean.ShowDetailList;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;


public class GirlShowVideoListInfoActivity extends AppCompatActivity {
    private XRecyclerView recyclerView;
    GirlVideoListInfo infoEntity;
    private int girsSize;
    private String startPage;
    private StaggeredGridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list_info);
        infoEntity = (GirlVideoListInfo) getIntent().getSerializableExtra("GirlVideoListInfo");
        recyclerView = (XRecyclerView) findViewById(R.id.videoRecyleview);
        gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        ViewDataUtil.setLayManagerSpecial(gridLayoutManager, new OnQueryDataListner() {
            @Override
            public void onRefresh() {
                getData(infoEntity.data.id, "1", Constants.REFRESH);
            }

            @Override
            public void onLoadMore() {
                if (girsSize > 0) {
                    getData(infoEntity.data.id, (Integer.parseInt(startPage) + 1) + "", Constants.LOADMORE);
                    recyclerView.loadMoreComplete();
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }, GirlShowVideoListInfoActivity.this, recyclerView, 3, true, true);

        getData(infoEntity.data.id, "1", Constants.REFRESH);
    }

    private void getData(String vid, String startPage, int what) {
        if (vid == null) {
            handler.sendEmptyMessage(Constants.NetWorkError);
            return;
        }
        String url = "http://youmei.xiumei99.com/smallvideo/list";
        OkHttpClientManager.parseRequestGirlSmallVideoList(this, url, handler, what, vid, startPage);
    }

    public VideoListInfoAdapter adapter;

    private ArrayList<Data> details = new ArrayList<>();
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != Constants.NetWorkError) {
                ShowDetailList result = CommonUtil.parseJsonWithGson((String) msg.obj, ShowDetailList.class);
                startPage = result.currPage;
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

                if (adapter == null) {
                    adapter = new VideoListInfoAdapter(GirlShowVideoListInfoActivity.this, infoEntity, details);
                    adapter.setOnItemClickListener(new VideoListInfoAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(GirlShowVideoListInfoActivity.this, VideoShowOneForListActivity.class);
                            intent.putParcelableArrayListExtra("videoDatas", details);
                            intent.putExtra("position", position + "");
                            intent.putExtra("cover", details.get(position).cover );

                            startActivity(intent);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

            }
        }
    };

}
