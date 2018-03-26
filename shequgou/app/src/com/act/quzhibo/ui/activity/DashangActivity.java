package com.act.quzhibo.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.DaShangAdapter;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.i.OnQueryDataListner;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.util.ViewDataUtil;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

/**
 * Created by asus-pc on 2018/3/22.
 */
public class DashangActivity extends AppCompatActivity {
    private XRecyclerView dashangrecylerview;
    private StaggeredGridLayoutManager gridLayoutManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashanglist);
        dashangrecylerview = (XRecyclerView) findViewById(R.id.dashangrecylerview);
        gridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dashangrecylerview.setLayoutManager(gridLayoutManager);
        dashangrecylerview.setLoadingMoreEnabled(false);
        dashangrecylerview.setPullRefreshEnabled(false);
        DaShangAdapter adapter = new DaShangAdapter(this, getIntent().getStringExtra("girlCoverUrl"), getIntent().getStringExtra("girlNickName"));

        adapter.setOnItemClickListener(new DaShangAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position,String money) {
                ToastUtil.showToast(DashangActivity.this,"ddddd"+money);
            }
        });
        dashangrecylerview.setAdapter(adapter);
    }

    public void initView() {

    }
}
