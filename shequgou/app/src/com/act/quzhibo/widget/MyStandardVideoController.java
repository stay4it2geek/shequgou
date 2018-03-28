package com.act.quzhibo.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.download.callback.OnVideoControllerListner;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.ui.activity.LoginActivity;
import com.act.quzhibo.util.ToastUtil;
import com.devlin_n.videoplayer.controller.StandardVideoController;

import cn.bmob.v3.BmobUser;

public class MyStandardVideoController extends StandardVideoController implements View.OnClickListener {
    public MyStandardVideoController(@NonNull Context context) {
        super(context);
    }

    public MyStandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyStandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    protected PopupMenu popupMenu;

    private void doLockUnlock() {
        if (this.isLocked) {
            this.isLocked = false;
            this.mShowing = false;
            this.gestureEnabled = true;
            this.show();
            this.lock.setSelected(false);
            Toast.makeText(this.getContext(), com.devlin_n.videoplayer.R.string.unlocked, Toast.LENGTH_LONG).show();
        } else {
            this.hide();
            this.isLocked = true;
            this.gestureEnabled = false;
            this.lock.setSelected(true);
            Toast.makeText(this.getContext(), com.devlin_n.videoplayer.R.string.locked, Toast.LENGTH_LONG).show();
        }

        this.mediaPlayer.setLock(this.isLocked);
    }


    public OnVideoControllerListner onVideoControllerListner;


    public void onClick(View v) {
        if (viewVisiable) {
            findViewById(R.id.more_menu).setVisibility(VISIBLE);
            findViewById(R.id.fullscreen).setVisibility(VISIBLE);
        }else{
            findViewById(R.id.more_menu).setVisibility(GONE);
            findViewById(R.id.fullscreen).setVisibility(GONE);
        }
        int i = v.getId();
        if (i != com.devlin_n.videoplayer.R.id.fullscreen && i != com.devlin_n.videoplayer.R.id.back) {
            if (i == com.devlin_n.videoplayer.R.id.lock) {
                this.doLockUnlock();
            } else if (i != com.devlin_n.videoplayer.R.id.iv_play && i != com.devlin_n.videoplayer.R.id.thumb && i != com.devlin_n.videoplayer.R.id.iv_replay) {
                if (i == com.devlin_n.videoplayer.R.id.more_menu) {
                    this.popupMenu.show();
                    this.show();
                }
            } else {
                this.doPauseResume();

            }
        } else if (i == R.id.fullscreen) {
            onVideoControllerListner.onMyVideoController(Constants.FULL_SCREEN);
        }

    }

    @Override
    protected void doPauseResume() {
        if (viewVisiable) {
            findViewById(R.id.more_menu).setVisibility(VISIBLE);
            findViewById(R.id.fullscreen).setVisibility(VISIBLE);
        }else{
            findViewById(R.id.more_menu).setVisibility(GONE);
            findViewById(R.id.fullscreen).setVisibility(GONE);
        }

        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
        } else {
                if (needLoginToStart) {
                    if (BmobUser.getCurrentUser(RootUser.class) == null) {
                        getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                        return;
                    }
                    if (BmobUser.getCurrentUser(RootUser.class).isVip) {
                        this.mediaPlayer.start();
                    } else {
                        ToastUtil.showToast(getContext(), "您还不是会员！请升级会员");
                    }
                } else {
                    this.mediaPlayer.start();
                }
        }
    }

    @Override
    protected void initView() {
        super.initView();
        if (viewVisiable) {
            findViewById(R.id.more_menu).setVisibility(VISIBLE);
            findViewById(R.id.fullscreen).setVisibility(VISIBLE);
        }else{
            findViewById(R.id.more_menu).setVisibility(GONE);
            findViewById(R.id.fullscreen).setVisibility(GONE);
        }
        this.popupMenu = new PopupMenu(this.getContext(), this.moreMenu, Gravity.RIGHT);
        this.popupMenu.getMenuInflater().inflate(R.menu.controller_menu_list, this.popupMenu.getMenu());
        this.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.fullscreen) {
                    onVideoControllerListner.onMyVideoController(Constants.FULL_SCREEN);
                } else if (itemId == R.id.download) {
                    onVideoControllerListner.onMyVideoController(Constants.DOWNLAOD_VIDEO);
                }
                MyStandardVideoController.this.popupMenu.dismiss();
                return false;
            }
        });


    }

    public boolean viewVisiable;
    public boolean needLoginToStart;

    public void setInitData(boolean needLoginToStart, boolean viewVisiable) {
        this.needLoginToStart = needLoginToStart;
        this.viewVisiable = viewVisiable;

    }
}
