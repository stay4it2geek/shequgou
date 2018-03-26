package com.act.quzhibo.ui.activity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.widget.MyStandardVideoController;
import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.bean.NearVideoEntity;
import com.act.quzhibo.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.devlin_n.videoplayer.player.IjkVideoView;

import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;


public class NearMediaVideoListActivity extends ActivityManagePermission {

     IjkVideoView ijkVideoView;
    NearVideoEntity videoEntity;
     MyStandardVideoController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoEntity = (NearVideoEntity) getIntent().getSerializableExtra(Constants.NEAR_USER_VIDEO);
        setContentView(R.layout.activity_near_video);
        ijkVideoView = (IjkVideoView) findViewById(R.id.videoview);
        controller = new MyStandardVideoController(this);
        controller.setInitData(true, false);
        ijkVideoView.setVideoController(controller);
        if (TextUtils.isEmpty(videoEntity.url)) {
            ToastUtil.showToast(this, "视频地址未找到，无法播放");
            return;
        }
        if (TextUtils.isEmpty(videoEntity.videoPic)) {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bitmap = CommonUtil.createBitmapFromVideoPath(videoEntity.url);
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    controller.getThumb().setImageBitmap(bitmap);
                }
            }.execute();
        } else {
            Glide.with(this)
                    .load(videoEntity.videoPic)
                    .crossFade()
                    .placeholder(android.R.color.darker_gray)
                    .error(R.drawable.error_img).into(controller.getThumb());
        }
        ijkVideoView
                .autoRotate()
                .enableCache()
                .setTitle("")
                .setUrl(videoEntity.url)
                .setScreenScale(IjkVideoView.SCREEN_SCALE_DEFAULT)
                .start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        ijkVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkVideoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkVideoView.release();
    }

    @Override
    public void onBackPressed() {
        if (!ijkVideoView.onBackPressed()) {
            super.onBackPressed();
        }
    }

}
