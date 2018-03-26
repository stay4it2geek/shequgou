package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.MemberAdapter;
import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.bean.Member;
import com.act.quzhibo.bean.MyFocusShower;
import com.act.quzhibo.bean.Room;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.widget.CircleImageView;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.HorizontialListView;
import com.act.quzhibo.widget.UPMarqueeView;
import com.act.quzhibo.download.event.FocusChangeEvent;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.devlin_n.videoplayer.listener.VideoListener;
import com.devlin_n.videoplayer.player.IjkVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import tyrantgit.widget.HeartLayout;

public class VideoPlayerActivityLanscape extends FragmentActivity implements View.OnClickListener {
    private IjkVideoView videoView;
    private MemberAdapter mAdapter;
    private HeartLayout heartLayout;
    private Room room;
    private int onlineCount = 0;
    private String photoUrl;
    private ArrayList<InterestSubPerson> interestSubPersonArrayList = new ArrayList<>();
    public String lastTime;
    private UPMarqueeView upview1;
    private LinearLayout moreView;
    private MyFocusShower mMyFocusShower;
    private CheckBox checkBox;
    RadioButton fullscreen;
    private Random mRandom = new Random();
    Handler handler = new Handler();
    private List<String> data = new ArrayList<>();
    private List<View> views = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewplay);
        room = (Room) getIntent().getSerializableExtra("room");
        initView();
    }

    private Runnable task = new Runnable() {
        public void run() {
            handler.postDelayed(this, 5 * 1000);
            int countRandom = mRandom.nextInt(10);
            onlineCount = Integer.parseInt(room.onlineCount) + countRandom;
            ((TextView) findViewById(R.id.onlineCount)).setText(onlineCount + "人");
        }
    };

    private void queryData() {
        final BmobQuery<InterestSubPerson> query = new BmobQuery<>();
        query.setLimit(20);
        query.order("-updatedAt");
        query.findObjects(new FindListener<InterestSubPerson>() {

            @Override
            public void done(List<InterestSubPerson> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        interestSubPersonArrayList.clear();
                        lastTime = list.get(list.size() - 1).getUpdatedAt();
                        interestSubPersonArrayList.addAll(list);
                        Message message = new Message();
                        message.obj = interestSubPersonArrayList;
                        memberHandler.sendMessage(message);
                    } else {
                        memberHandler.sendEmptyMessage(Constants.NO_MORE);
                    }
                }
            }
        });
    }

    private HorizontialListView horizontialListView;
    Handler memberHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ArrayList<InterestSubPerson> interestSubPersonArrayList = (ArrayList<InterestSubPerson>) msg.obj;
            ArrayList<String> views = new ArrayList<>();
            if (msg.what != Constants.NetWorkError) {
                for (InterestSubPerson person : interestSubPersonArrayList) {
                    views.add(person.viewUsers);
                }
            }
            int max = views.size();
            final ArrayList<Member> members = CommonUtil.jsonToArrayList(views.get(new Random().nextInt(max - 1)), Member.class);
            horizontialListView = (HorizontialListView) findViewById(R.id.list);
            mAdapter = new MemberAdapter(members, VideoPlayerActivityLanscape.this);
            horizontialListView.setAdapter(mAdapter);
            horizontialListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showDialog(members.get(i));
                }
            });
        }
    };

    private void showDialog(Member m) {
        FragmentDialog.newInstance(false, m.nickname, "", "", "关闭", m.headUrl, "", true, new FragmentDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick(Dialog dialog, boolean needDelete) {
                dialog.dismiss();
            }

            @Override
            public void onNegtiveClick(Dialog dialog) {
                dialog.dismiss();
            }
        }).show(getSupportFragmentManager(), "");
    }

    Handler heartHandler = new Handler();
    Runnable heartRunnable = new Runnable() {
        @Override
        public void run() {
            heartLayout.post(new Runnable() {
                @Override
                public void run() {
                    heartLayout.addHeart(randomColor());
                }
            });
            heartHandler.postDelayed(this, 200 * mRandom.nextInt(5));
        }
    };
    Handler countHandler = new Handler();
    Runnable countRunnable = new Runnable() {
        @Override
        public void run() {
            countHandler.post(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    onlineCount = onlineCount + random.nextInt(4);
                    ((TextView) findViewById(R.id.onlineCount)).setText(onlineCount + "人");
                }
            });
            countHandler.postDelayed(this, 7000);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        countHandler.removeCallbacks(countRunnable);
        heartHandler.removeCallbacks(heartRunnable);

    }

    @Override
    public void onResume() {
        super.onResume();
        videoView.start();
        countHandler.postDelayed(countRunnable, 10000);
        heartHandler.postDelayed(heartRunnable, 1000);
        if (BmobUser.getCurrentUser(RootUser.class) != null) {
            BmobQuery<MyFocusShower> query = new BmobQuery<>();
            query.setLimit(1);
            query.addWhereEqualTo("userId", room.userId);
            query.addWhereEqualTo("rootUser", BmobUser.getCurrentUser(RootUser.class));
            query.findObjects(new FindListener<MyFocusShower>() {
                @Override
                public void done(List<MyFocusShower> myFocusShowers, BmobException e) {
                    if (e == null) {
                        if (myFocusShowers.size() >= 1) {
                            mMyFocusShower = myFocusShowers.get(0);
                            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                        }
                    }
                }
            });
        }

        findViewById(R.id.focus_top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (BmobUser.getCurrentUser(RootUser.class) != null) {
                    if (!(((TextView) findViewById(R.id.focus_top)).getText().toString().trim()).equals(getResources().getString(R.string.cancelFocus))) {
                        if (mMyFocusShower == null) {
                            MyFocusShower myFocusShower = new MyFocusShower();
                            myFocusShower.rootUser = BmobUser.getCurrentUser(RootUser.class);
                            myFocusShower.portrait_path_1280 = photoUrl;
                            myFocusShower.nickname = room.nickname;
                            myFocusShower.roomId = room.roomId;
                            myFocusShower.userId = room.userId;
                            myFocusShower.gender = room.gender;
                            if(room.liveStream!=null){
                                myFocusShower.liveStream = room.liveStream;
                            }else{
                                myFocusShower.liveStream  = "http://pull.kktv8.com/livekktv/" + room.roomId + ".flv";
                            }
                            myFocusShower.city = room.city;
                            myFocusShower.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        ToastUtil.showToast(VideoPlayerActivityLanscape.this, getResources().getString(R.string.focusOk));
                                        ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                        if (BmobUser.getCurrentUser(RootUser.class) != null) {
                                            BmobQuery<MyFocusShower> query = new BmobQuery<>();
                                            query.addWhereEqualTo("userId", room.userId);
                                            query.addWhereEqualTo("rootUser", BmobUser.getCurrentUser(RootUser.class));
                                            query.findObjects(new FindListener<MyFocusShower>() {
                                                @Override
                                                public void done(List<MyFocusShower> myFocusShowers, BmobException e) {
                                                    if (e == null) {
                                                        if (myFocusShowers.size() >= 1) {
                                                            VideoPlayerActivityLanscape.this.mMyFocusShower = myFocusShowers.get(0);
                                                            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                    } else {
                                        ToastUtil.showToast(VideoPlayerActivityLanscape.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        } else {
                            mMyFocusShower.update(mMyFocusShower.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        ToastUtil.showToast(VideoPlayerActivityLanscape.this, getResources().getString(R.string.focusOk));
                                        ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                        if (BmobUser.getCurrentUser(RootUser.class) != null) {
                                            BmobQuery<MyFocusShower> query = new BmobQuery<>();
                                            query.addWhereEqualTo("userId", room.userId);
                                            query.addWhereEqualTo("rootUser", BmobUser.getCurrentUser(RootUser.class));
                                            query.findObjects(new FindListener<MyFocusShower>() {
                                                @Override
                                                public void done(List<MyFocusShower> myFocusShowers, BmobException e) {
                                                    if (e == null) {
                                                        if (myFocusShowers.size() >= 1) {
                                                            mMyFocusShower = myFocusShowers.get(0);
                                                            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                    } else {
                                        ToastUtil.showToast(VideoPlayerActivityLanscape.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        }

                    } else {
                        FragmentDialog.newInstance(false, getResources().getString(R.string.isCancelFocus), getResources().getString(R.string.reallyCancelFocus), getResources().getString(R.string.keepFocus), getResources().getString(R.string.cancelFocus), "", "", false, new FragmentDialog.OnClickBottomListener() {
                            @Override
                            public void onPositiveClick(final Dialog dialog, boolean deleteFileSource) {
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegtiveClick(Dialog dialog) {
                                if (mMyFocusShower != null) {
                                    mMyFocusShower.delete(mMyFocusShower.getObjectId(), new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                mMyFocusShower = null;
                                                ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.focusTa));
                                                ToastUtil.showToast(VideoPlayerActivityLanscape.this, getResources().getString(R.string.cancelFocusOk));
                                            }

                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "");
                    }
                } else {
                    startActivity(new Intent(VideoPlayerActivityLanscape.this, LoginActivity.class));
                }
            }
        });


    }

    private int randomColor() {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(FocusChangeEvent event) {

        if (event.focus && event.type.equals("show")) {
            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
        } else {
            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.focusTa));

        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.close == v.getId()) {
            videoView.stopPlayback();
            finish();
        } else if (R.id.addherat == v.getId()) {
            heartLayout.post(new Runnable() {
                @Override
                public void run() {
                    heartLayout.addHeart(randomColor());
                }
            });
        } else if (R.id.send_message == v.getId() || R.id.message == v.getId() || R.id.gift == v.getId()) {
            ToastUtil.showToast(this, "消息加载异常，暂时无法操作该功能");
        }
    }


    private void setView() {
        for (int i = 0; i < data.size(); i = i + 2) {
            moreView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.item_view, null);
            TextView tv1 = (TextView) moreView.findViewById(R.id.message_fail);
            final Handler handler = new Handler();
            moreView.findViewById(R.id.retrylayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            moreView.findViewById(R.id.connetFaillayout).setVisibility(View.VISIBLE);
                            moreView.findViewById(R.id.retrylayout).setVisibility(View.GONE);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    moreView.findViewById(R.id.connetFaillayout).setVisibility(View.GONE);
                                    moreView.findViewById(R.id.retrylayout).setVisibility(View.VISIBLE);
                                }
                            }, 2000);
                        }
                    });

                }
            });
            tv1.setText(data.get(i).toString());
            views.add(moreView);
        }
    }


    private void initdata() {
        data = new ArrayList<>();
        data.add("消息连接异常，正在重试连接");
        data.add("消息连接异常，正在重试连接");
    }

    private void initView() {

        videoView = (IjkVideoView) findViewById(R.id.video);
        fullscreen = (RadioButton) findViewById(R.id.fullscreen);
        if(getIntent().getBooleanExtra("showFullScreen",false)){
            findViewById(R.id.fullScreen).setVisibility(View.VISIBLE);
        }
        fullscreen.setChecked(false);
        fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.stopPlayback();
                finish();
            }
        });

        videoView
                 .addToPlayerManager()
                 .setUrl(room.liveStream).setScreenScale(IjkVideoView.SCREEN_SCALE_16_9)
                 .start();
        videoView.setVideoListener(new VideoListener() {
            @Override
            public void onComplete() {
                findViewById(R.id.bar).setVisibility(View.GONE);
            }

            @Override
            public void onPrepared() {
                findViewById(R.id.bar).setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                findViewById(R.id.bar).setVisibility(View.GONE);
            }

            @Override
            public void onInfo(int i, int i1) {
                findViewById(R.id.bar).setVisibility(View.GONE);
            }
        });

        final FrameLayout infoLayout = (FrameLayout) findViewById(R.id.info_layout);
        infoLayout.setVisibility(View.GONE);
        checkBox = (CheckBox) findViewById(R.id.hideOrSee);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!isChecked) {
                    Animation animOut = AnimationUtils.loadAnimation(VideoPlayerActivityLanscape.this, R.anim.anim_marquee_out);
                    animOut.setDuration(500);
                    infoLayout.startAnimation(animOut);
                    infoLayout.setVisibility(View.GONE);
                } else {
                    Animation animIn = AnimationUtils.loadAnimation(VideoPlayerActivityLanscape.this, R.anim.anim_marquee_in);
                    animIn.setDuration(500);
                    infoLayout.startAnimation(animIn);
                    infoLayout.setVisibility(View.VISIBLE);
                }
            }

        });


        CircleImageView showerAvatar = (CircleImageView) findViewById(R.id.showerAvatar);
        if (!room.portrait_path_1280.contains("http://ures.kktv8.com/kktv")) {
            photoUrl = "http://ures.kktv8.com/kktv" + room.portrait_path_1280;
        } else {
            photoUrl = room.portrait_path_1280;
        }
        Glide.with(VideoPlayerActivityLanscape.this).load(photoUrl).error(R.drawable.error_img).into(showerAvatar);//加载网络图片

        showerAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("room", room);
                intent.putExtra("FromChatFragment", true);
                intent.putExtra("photoUrl", photoUrl);
                intent.setClass(VideoPlayerActivityLanscape.this, ShowerInfoActivity.class);
                startActivity(intent);
            }

        });

        onlineCount = Integer.parseInt(room.onlineCount);
        ((TextView) findViewById(R.id.onlineCount)).setText(onlineCount + "人");
        ((TextView) findViewById(R.id.starValue)).setText(("星光值：" + (Long.parseLong(room.roomId) - 92015634l)).toString().replace("-", ""));
        ((TextView) findViewById(R.id.liveId)).setText("房间号:" + room.roomId);
        ((TextView) findViewById(R.id.userNickName)).setText(room.nickname.replace("k", ""));
        findViewById(R.id.close).setOnClickListener(this);
        queryData();

        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);

        findViewById(R.id.send_message).setOnClickListener(this);

        findViewById(R.id.gift).setOnClickListener(this);

        findViewById(R.id.message).setOnClickListener(this);

        findViewById(R.id.addherat).setOnClickListener(this);

        EventBus.getDefault().register(this);
        handler.postDelayed(task, 2000);//延迟调用
        upview1 = (UPMarqueeView) findViewById(R.id.marqueeView);

        initdata();

        setView();
        upview1.setViews(views);
    }


    @Override
    protected void onStop() {
        super.onStop();
        videoView.pause();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        videoView.stopPlayback();
        finish();
    }

}
