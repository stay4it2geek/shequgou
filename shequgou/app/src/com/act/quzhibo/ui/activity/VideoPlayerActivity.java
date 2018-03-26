package com.act.quzhibo.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import com.act.quzhibo.download.event.FocusChangeEvent;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.CircleImageView;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.HorizontialListView;
import com.act.quzhibo.widget.UPMarqueeView;
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

public class VideoPlayerActivity extends FragmentActivity implements View.OnClickListener {
    private static final int OVERLAY_PERMISSION_REQ_CODE = 900;
    private IjkVideoView videoView;
    private MemberAdapter mAdapter;
    private HeartLayout heartLayout;
    private Room room;
    private int onlineCount = 0;
    private String photoUrl;
    public String lastTime;
    private UPMarqueeView upview1;
    private LinearLayout moreView;
    private MyFocusShower mShower;
    private CheckBox checkBox;
    private RadioButton fullscreen;
    private Random mRandom = new Random();
    private Handler handler = new Handler();
    private List<String> data = new ArrayList<>();
    private List<View> views = new ArrayList<>();
    private ArrayList<InterestSubPerson> persons = new ArrayList<>();
    private RootUser user;
    private HorizontialListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewplay);
        user = BmobUser.getCurrentUser(RootUser.class);
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
                        persons.clear();
                        lastTime = list.get(list.size() - 1).getUpdatedAt();
                        persons.addAll(list);
                        Message message = new Message();
                        message.obj = persons;
                        memberHandler.sendMessage(message);
                    } else {
                        memberHandler.sendEmptyMessage(Constants.NO_MORE);
                    }
                }
            }
        });
    }

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
            mListView = (HorizontialListView) findViewById(R.id.list);
            mAdapter = new MemberAdapter(members, VideoPlayerActivity.this);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        if (user != null) {
            BmobQuery<MyFocusShower> query = new BmobQuery<>();
            query.setLimit(1);
            query.addWhereEqualTo("userId", room.userId);
            query.addWhereEqualTo("rootUser", user);
            query.findObjects(new FindListener<MyFocusShower>() {
                @Override
                public void done(List<MyFocusShower> myFocusShowers, BmobException e) {
                    if (e == null) {
                        if (myFocusShowers.size() >= 1) {
                            mShower = myFocusShowers.get(0);
                            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                        }
                    }
                }
            });
        }

        findViewById(R.id.focus_top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (user != null) {
                    if (!(((TextView) findViewById(R.id.focus_top)).getText().toString().trim()).equals(getResources().getString(R.string.cancelFocus))) {
                        if (mShower == null) {
                            MyFocusShower myFocusShower = new MyFocusShower();
                            myFocusShower.rootUser = user;
                            myFocusShower.portrait_path_1280 = photoUrl;
                            myFocusShower.nickname = room.nickname;
                            myFocusShower.roomId = room.roomId;
                            myFocusShower.userId = room.userId;
                            myFocusShower.gender = room.gender;
                            if (room.liveStream != null) {
                                myFocusShower.liveStream = room.liveStream;
                            } else {
                                myFocusShower.liveStream = "http://pull.kktv8.com/livekktv/" + room.roomId + ".flv";
                            }
                            myFocusShower.city = room.city;
                            myFocusShower.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        ToastUtil.showToast(VideoPlayerActivity.this, getResources().getString(R.string.focusOk));
                                        ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                        if (user != null) {
                                            BmobQuery<MyFocusShower> query = new BmobQuery<>();
                                            query.addWhereEqualTo("userId", room.userId);
                                            query.addWhereEqualTo("rootUser", user);
                                            query.findObjects(new FindListener<MyFocusShower>() {
                                                @Override
                                                public void done(List<MyFocusShower> myFocusShowers, BmobException e) {
                                                    if (e == null) {
                                                        if (myFocusShowers.size() >= 1) {
                                                            VideoPlayerActivity.this.mShower = myFocusShowers.get(0);
                                                            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                    } else {
                                        ToastUtil.showToast(VideoPlayerActivity.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        } else {
                            mShower.update(mShower.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        ToastUtil.showToast(VideoPlayerActivity.this, getResources().getString(R.string.focusOk));
                                        ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                        if (user != null) {
                                            BmobQuery<MyFocusShower> query = new BmobQuery<>();
                                            query.addWhereEqualTo("userId", room.userId);
                                            query.addWhereEqualTo("rootUser", user);
                                            query.findObjects(new FindListener<MyFocusShower>() {
                                                @Override
                                                public void done(List<MyFocusShower> myFocusShowers, BmobException e) {
                                                    if (e == null) {
                                                        if (myFocusShowers.size() >= 1) {
                                                            mShower = myFocusShowers.get(0);
                                                            ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                    } else {
                                        ToastUtil.showToast(VideoPlayerActivity.this, getResources().getString(R.string.focusFail));
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
                                if (mShower != null) {
                                    mShower.delete(mShower.getObjectId(), new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                mShower = null;
                                                ((TextView) findViewById(R.id.focus_top)).setText(getResources().getString(R.string.focusTa));
                                                ToastUtil.showToast(VideoPlayerActivity.this, getResources().getString(R.string.cancelFocusOk));
                                            }

                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "");
                    }
                } else {
                    startActivity(new Intent(VideoPlayerActivity.this, LoginActivity.class));
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
            FragmentDialog.newInstance(false, "是否开启小窗口播放", "               开小窗，边看边玩!  \n同意一次“在其他应用上显示”即可", "立即开启", "不看了", "", "", false, new FragmentDialog.OnClickBottomListener() {
                @Override
                public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                    askForPermission();
                }

                @Override
                public void onNegtiveClick(Dialog dialog) {
                    dialog.dismiss();
                    videoView.stopPlayback();
                    finish();
                }
            }).show(getSupportFragmentManager(), "");

        } else if (R.id.addherat == v.getId()) {
            heartLayout.post(new Runnable() {
                @Override
                public void run() {
                    heartLayout.addHeart(randomColor());
                }
            });
        } else if (R.id.send_message == v.getId() || R.id.message == v.getId() || R.id.gift == v.getId()) {
           if(user!=null){
               ToastUtil.showToast(this, "消息加载异常，暂时无法操作该功能");
           }else{
               startActivity(new Intent(VideoPlayerActivity.this, LoginActivity.class));
           }
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
        if (getIntent().getBooleanExtra("showFullScreen", false)) {
            findViewById(R.id.fullScreen).setVisibility(View.VISIBLE);
        }
        fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoPlayerActivity.this, VideoPlayerActivityLanscape.class);
                intent.putExtra("room", room);
                intent.putExtra("showFullScreen", true);
                startActivity(intent);
            }
        });
        fullscreen.setChecked(true);
        videoView.stopFloatWindow();
        videoView
                .addToPlayerManager()
                .setUrl(room.liveStream).setScreenScale(IjkVideoView.SCREEN_SCALE_DEFAULT)
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
                    Animation animOut = AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.anim_marquee_out);
                    animOut.setDuration(500);
                    infoLayout.startAnimation(animOut);
                    infoLayout.setVisibility(View.GONE);
                } else {
                    Animation animIn = AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.anim_marquee_in);
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
        Glide.with(VideoPlayerActivity.this).load(photoUrl).error(R.drawable.error_img).into(showerAvatar);//加载网络图片

        showerAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("room", room);
                intent.putExtra("FromChatFragment", true);
                intent.putExtra("photoUrl", photoUrl);
                intent.setClass(VideoPlayerActivity.this, ShowerInfoActivity.class);
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
        FragmentDialog.newInstance(false, "是否开启小窗口播放", "               开小窗，边听边玩!  \n同意一次“在其他应用上显示”即可", "立即开启", "不看了", "", "", false, new FragmentDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                askForPermission();
            }

            @Override
            public void onNegtiveClick(Dialog dialog) {
                dialog.dismiss();
                videoView.stopPlayback();
                finish();
            }
        }).show(getSupportFragmentManager(), "");
    }

    /**
     * 请求用户给予悬浮窗的权限
     */
    @SuppressLint("NewApi")
    public void askForPermission() {
        if (!Settings.canDrawOverlays(this)) {
            ToastUtil.showToast(VideoPlayerActivity.this, "当前无悬浮窗权限，请授权！");
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            videoView.startFloatWindow();
        }
    }


    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                ToastUtil.showToast(VideoPlayerActivity.this, "权限授予失败，无法开启悬浮窗");
            } else {
                ToastUtil.showToast(VideoPlayerActivity.this, "权限授予成功！");
                videoView.startFloatWindow();
            }

        }
    }
}
