package com.act.quzhibo.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Data;
import com.act.quzhibo.bean.GirlVideoListInfo;
import com.act.quzhibo.bean.MyFocusGirl;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.bean.VideoShowOneEntity;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.data_db.SeeVideoCountData;
import com.act.quzhibo.data_db.SeeVideoCountInfoDao;
import com.act.quzhibo.data_db.VirtualUserInfo;
import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.ui.activity.DashangActivity;
import com.act.quzhibo.ui.activity.GetVipPayActivity;
import com.act.quzhibo.ui.activity.GirlShowVideoListInfoActivity;
import com.act.quzhibo.ui.activity.VideoPlayerActivity;
import com.act.quzhibo.ui.fragment.LazyLoadFragment;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.CircleImageView;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.OnDoubleClickListener;
import com.bumptech.glide.Glide;
import com.devlin_n.videoplayer.listener.VideoListener;
import com.devlin_n.videoplayer.player.IjkVideoView;
import com.mabeijianxi.smallvideorecord2.StringUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class VideoShowOneFragment extends LazyLoadFragment {

    IjkVideoView videoView;
    ImageView dashang;
    ImageView iv_cover;
    int count = 0;
    GirlVideoListInfo infoEntity;
    ProgressBar bar;
    CircleImageView iv_avatar;
    FrameLayout videoFrameLayout;
    RadioButton dianliangRadio;
    private Data videoData;
    MyFocusGirl myFocusGirl;
    private RootUser user;

    @Override
    protected int setContentView() {
        return R.layout.fragment_videoshowone;
    }

    @Override
    protected void lazyLoad() {
        iv_avatar = findViewById(R.id.iv_avatar);
        user = BmobUser.getCurrentUser(RootUser.class);

        if (getArguments().get("showAvatar") == null) {
            iv_avatar.setVisibility(View.VISIBLE);
        }

        videoData = (Data) getArguments().get("videoOneData");
        videoView = findViewById(R.id.videoview);
        iv_cover = findViewById(R.id.iv_cover);
        dashang = findViewById(R.id.dashang);
        videoFrameLayout = findViewById(R.id.videoFrameLayout);
        dianliangRadio = findViewById(R.id.dianliangRadio);
        bar = findViewById(R.id.bar);
        String url = "http://youmei.xiumei99.com/smallvideo/one";
        OkHttpClientManager.parseRequestGirlSmallVideoOne(getActivity(), url, handler, videoData.id);

        BmobQuery<MyFocusGirl> query = new BmobQuery<>();
        query.addWhereEqualTo("videoId", videoData.id);
        query.addWhereEqualTo("rootUser", user);
        query.findObjects(new FindListener<MyFocusGirl>() {
            @Override
            public void done(List<MyFocusGirl> list, BmobException e) {
                if (list != null && list.size() > 0) {
                    dianliangRadio.setChecked(true);
                    myFocusGirl = list.get(0);
                } else {
                    dianliangRadio.setChecked(false);
                    myFocusGirl = null;
                }

            }
        });
        dianliangRadio.setEnabled(false);
        videoFrameLayout.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                if (user != null) {
                    dianliangRadio.setEnabled(true);
                    if (dianliangRadio.isChecked()) {
                        if (myFocusGirl != null)
                            myFocusGirl.delete(myFocusGirl.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        myFocusGirl = null;
                                        dianliangRadio.setChecked(false);
                                        ToastUtil.showToast(getActivity(), "已取消关注");
                                    }
                                }
                            });

                    } else {
                        myFocusGirl = new MyFocusGirl();
                        myFocusGirl.videoId = videoData.id;
                        myFocusGirl.rootUser = user;
                        myFocusGirl.cover = videoData.cover;

                        myFocusGirl.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (e == null) {
                                    dianliangRadio.setChecked(true);
                                    ToastUtil.showToast(getActivity(), "已点赞");
                                }
                            }
                        });
                    }
                } else {
                    ToastUtil.showToast(getActivity(), "请先登录再点赞哦");
                }
            }
        }));
    }

    @Override
    protected void stopLoad() {
        super.stopLoad();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    boolean canLookVideo;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final VideoShowOneEntity entity = CommonUtil.parseJsonWithGson((String) msg.obj, VideoShowOneEntity.class);
            final SeeVideoCountInfoDao dao = SeeVideoCountInfoDao.getInstance(getActivity());
            List<SeeVideoCountData> dataCount = dao.queryAll();

            if (BmobUser.getCurrentUser(RootUser.class) == null
                    || !BmobUser.getCurrentUser(RootUser.class).isVip) {

                if (dataCount == null) {
                    canLookVideo = true;
                    SeeVideoCountData data = new SeeVideoCountData();
                    data.videoId = entity.data.vid;
                    dao.add(data);
                } else {
                    if (dataCount.size() < 10) {
                        canLookVideo = true;
                        SeeVideoCountData data = new SeeVideoCountData();
                        if (!dataCount.contains(entity.data.vid)) {
                            data.videoId = entity.data.vid;
                            dao.add(data);
                        }
                    }
                }

            } else if (BmobUser.getCurrentUser(RootUser.class) != null
                    && BmobUser.getCurrentUser(RootUser.class).isVip) {
                canLookVideo = true;
            }

            if (!canLookVideo) {
                Glide.with(getActivity()).load(videoData.cover).into(iv_cover);//加载视频封面
                bar.setVisibility(View.GONE);
            }
            iv_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), GirlShowVideoListInfoActivity.class);
                    intent.putExtra("GirlVideoListInfo", infoEntity);
                    startActivity(intent);
                }
            });
            OkHttpClientManager.parseRequestGirlBigVideoOne(getActivity(), "http://youmei.xiumei99.com/bigv/one", new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg != null) {
                        infoEntity = CommonUtil.parseJsonWithGson((String) msg.obj, GirlVideoListInfo.class);
                    }
                }
            }, entity.data.vid);
            Glide.with(getActivity()).load(entity.data.avatar.url).into(iv_avatar);//頭像
            dashang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), DashangActivity.class);
                    intent.putExtra("girlCoverUrl", infoEntity.data.avatar.url);
                    intent.putExtra("girlNickName", infoEntity.data.nickname);
                    startActivity(intent);
                }
            });
            if (entity != null && canLookVideo) {

                videoView
                        .addToPlayerManager().useSurfaceView().enableCache()
                        .setUrl(entity.data.url).setScreenScale(IjkVideoView.SCREEN_SCALE_DEFAULT)
                        .start();
                videoView.setVideoListener(new VideoListener() {
                    @Override
                    public void onComplete() {
                        if (count < 100) {
                            videoView.start();
                        }
                        count++;
                    }

                    @Override
                    public void onPrepared() {
                        bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        bar.setVisibility(View.GONE);
                        ToastUtil.showToast(getActivity(), "无法播放该视频，上下滑动切换看其他视频吧！");
                    }

                    @Override
                    public void onInfo(int i, int i1) {
                        bar.setVisibility(View.GONE);
                    }
                });


            } else

            {
                FragmentDialog.newInstance(false, "温馨提示", "非VIP只能观看10个视频，成为VIP会员可以无限观看哦！", "成为VIP", "看10个够了", "", "", false, new FragmentDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                        startActivity(new Intent(getActivity(), GetVipPayActivity.class));
                    }

                    @Override
                    public void onNegtiveClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).show(getChildFragmentManager(), "");
            }
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.resume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

}
