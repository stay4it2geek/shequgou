package com.act.quzhibo.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.InteretstPostDetailAdapter;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.bean.InterestPost;
import com.act.quzhibo.bean.InterestPostDetailParentData;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.rockerhieu.emojicon.EmojiconEditText;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class IntersetPostDetailActivity extends AppCompatActivity {
     InteretstPostDetailAdapter adapter;
     XRecyclerView recyclerview;
     InterestPost post;
     LoadNetView loadNetView;
     EmojiconEditText commentET;
     TextView commentBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interest_post_detail_layout);
        findViewById(R.id.titlebar).setVisibility(View.VISIBLE);
        recyclerview = (XRecyclerView) findViewById(R.id.postRecyleview);
        recyclerview.setPullRefreshEnabled(false);
        recyclerview.setLoadingMoreEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(linearLayoutManager);
        if (getIntent() != null) {
            post = (InterestPost) getIntent().getSerializableExtra(Constants.POST);
        }
        getData();
        loadNetView = (LoadNetView) findViewById(R.id.loadview);
        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData();
            }
        });
        findViewById(R.id.root_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;     //截断事件的传递
            }
        });
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setBarTitle("状 态 详 情");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntersetPostDetailActivity.this.finish();
            }
        });

        commentET = (EmojiconEditText) findViewById(R.id.comment_et);
        commentBtn = (TextView)findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((commentET.getText().equals("点击这里评论她/他") || commentET.getText().length() == 0)) {
                    ToastUtil.showToast(IntersetPostDetailActivity.this, "您是否忘记了评论内容?");
                } else {
                    ToastUtil.showToast(IntersetPostDetailActivity.this, "正在评论...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(IntersetPostDetailActivity.this, "评论已提交审核");
                            commentET.setText("");
                            InputMethodManager imm = (InputMethodManager) IntersetPostDetailActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(commentBtn.getWindowToken(), 0);
                        }
                    }, 1000);
                }
            }
        });
    }

     void getData() {
         BmobQuery<Toggle> query = new BmobQuery<>();
         query.findObjects(new FindListener<Toggle>() {
             @Override
             public void done(List<Toggle> toggles, BmobException e) {
                 if (e == null && toggles.size() > 0) {

                     String url = toggles.get(0).interestPostDetail.replace(Constants.POST_ID, post.postId);
                     OkHttpClientManager.parseRequest(IntersetPostDetailActivity.this, url, handler, Constants.REFRESH);
                 }else{
                     handler.sendEmptyMessage(Constants.NetWorkError);
                 }
             }
         });

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != Constants.NetWorkError) {
                InterestPostDetailParentData data = CommonUtil.parseJsonWithGson((String) msg.obj, InterestPostDetailParentData.class);
                adapter = new InteretstPostDetailAdapter(post, IntersetPostDetailActivity.this, data.result);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(IntersetPostDetailActivity.this);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerview.setLayoutManager(linearLayoutManager);
                recyclerview.setAdapter(adapter);
                loadNetView.setVisibility(View.GONE);
            } else {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.RELOAD);
            }
        }
    };

}
