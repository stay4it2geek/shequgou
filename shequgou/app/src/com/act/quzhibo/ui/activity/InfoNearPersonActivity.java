package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.data_db.VirtualUserInfo;
import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.adapter.NearSeeHer20Adapter;
import com.act.quzhibo.adapter.PostImageAdapter;
import com.act.quzhibo.bean.AddFriendMessage;
import com.act.quzhibo.bean.Friend;
import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.bean.MyFocusCommonPerson;
import com.act.quzhibo.bean.NearPhotoEntity;
import com.act.quzhibo.bean.NearSeeHerEntity;
import com.act.quzhibo.bean.NearVideoEntity;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.event.RefreshEvent;
import com.act.quzhibo.i.NewRecordPlayClickListener;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.GlideImageLoader;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.CircleImageView;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.HorizontialListView;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.bumptech.glide.Glide;
import com.youth.banner.Banner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMAudioMessage;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class InfoNearPersonActivity extends BaseActivity {

    File downloadDir = new File(Environment.getExternalStorageDirectory(), "PhotoScanDownload");
    InterestSubPerson nearInfoUser;
    MyFocusCommonPerson person;
    BmobIMUserInfo info;
    RootUser user;
    @Bind(R.id.iv_voice)
    ImageView iv_voice;
    @Bind(R.id.titlebar)
    TitleBarView titlebar;
    @Bind(R.id.loadview)
    LoadNetView loadview;
    @Bind(R.id.audio_layout)
    RelativeLayout audio_layout;
    @Bind(R.id.rl_self_photo)
    RelativeLayout rl_self_photo;
    @Bind(R.id.rl_self_video)
    RelativeLayout rl_self_video;
    @Bind(R.id.last_see_20_rl)
    RelativeLayout last_see_20_rl;
    @Bind(R.id.banner)
    Banner banner;
    @Bind(R.id.sexAndage)
    TextView sexAndAge;
    @Bind(R.id.disMariState)
    TextView disMariState;
    @Bind(R.id.userImage)
    CircleImageView userImage;
    @Bind(R.id.level_img)
    ImageView level_img;
    @Bind(R.id.online_time)
    TextView online_time;
    @Bind(R.id.nickName)
    TextView nickName;
    @Bind(R.id.focus)
    TextView focus;
    @Bind(R.id.disPurpose)
    TextView disPurpose;
    @Bind(R.id.soundLen)
    TextView soundLen;
    @Bind(R.id.isCanDate)
    TextView isCanDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_common_layout);
        user = BmobUser.getCurrentUser(RootUser.class);
        initViews();

        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    @OnClick({R.id.talk_accese, R.id.chat_private, R.id.addFriend, R.id.focus})
    void buttonCliks(View view) {
        switch (view.getId()) {
            case R.id.addFriend:
                sendAddFriendMessage();
                break;
            case R.id.talk_accese:
                startActivity(new Intent(InfoNearPersonActivity.this, GetVipPayActivity.class));
                break;
            case R.id.chat_private:
                if (user != null) {
                    if (BmobIM.getInstance().getCurrentStatus().getMsg().equals("connected")) {
                        chatPrivate();
                    } else {
                        ToastUtil.showToast(InfoNearPersonActivity.this, "网络不给力哦！");
                    }
                } else {
                    startActivity(new Intent(InfoNearPersonActivity.this, LoginActivity.class));
                }
                break;
            case R.id.focus:
                if (user != null) {
                    if (!(focus.getText().toString().trim()).equals(getResources().getString(R.string.cancelFocus))) {
                        if (person == null) {
                            MyFocusCommonPerson myFcPerson = new MyFocusCommonPerson();
                            myFcPerson.rootUser = user;
                            myFcPerson.username = nearInfoUser.username;
                            myFcPerson.userId = nearInfoUser.userId;
                            myFcPerson.photoUrl = nearInfoUser.absCoverPic;
                            if (nearInfoUser.user != null) {
                                myFcPerson.sex = nearInfoUser.user.sex;
                            }
                            myFcPerson.userType = Constants.NEAR;
                            myFcPerson.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        focus.setText(getResources().getString(R.string.cancelFocus));
                                        if (user != null) {
                                            BmobQuery<MyFocusCommonPerson> query = new BmobQuery<>();
                                            query.setLimit(1);
                                            query.addWhereEqualTo("userId", nearInfoUser.userId);
                                            query.addWhereEqualTo("rootUser", user);
                                            query.findObjects(new FindListener<MyFocusCommonPerson>() {
                                                @Override
                                                public void done(List<MyFocusCommonPerson> myFcPersons, BmobException e) {
                                                    if (e == null) {
                                                        if (myFcPersons.size() >= 1) {
                                                            person = myFcPersons.get(0);
                                                            focus.setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        ToastUtil.showToast(InfoNearPersonActivity.this, getResources().getString(R.string.focusOk));
                                    } else {
                                        ToastUtil.showToast(InfoNearPersonActivity.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        } else {
                            person.update(person.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        focus.setText(getResources().getString(R.string.cancelFocus));
                                        if (user != null) {
                                            BmobQuery<MyFocusCommonPerson> query = new BmobQuery<>();
                                            query.setLimit(1);
                                            query.addWhereEqualTo("userId", nearInfoUser.userId);
                                            query.addWhereEqualTo("rootUser", user);
                                            query.findObjects(new FindListener<MyFocusCommonPerson>() {
                                                @Override
                                                public void done(List<MyFocusCommonPerson> myFcPersons, BmobException e) {
                                                    if (e == null) {
                                                        if (myFcPersons.size() >= 1) {
                                                            person = myFcPersons.get(0);
                                                            focus.setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        ToastUtil.showToast(InfoNearPersonActivity.this, getResources().getString(R.string.focusOk));
                                    } else {
                                        ToastUtil.showToast(InfoNearPersonActivity.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        }

                    } else {
                        FragmentDialog.newInstance(false, getResources().getString(R.string.isCancelFocus), "真的要取消关注人家吗？", getResources().getString(R.string.keepFocus), getResources().getString(R.string.cancelFocus), "", "", false, new FragmentDialog.OnClickBottomListener() {
                            @Override
                            public void onPositiveClick(final Dialog dialog, boolean deleteFileSource) {
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegtiveClick(Dialog dialog) {
                                if (person != null) {
                                    person.delete(person.getObjectId(), new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                person = null;
                                                focus.setText(getResources().getString(R.string.focusTa));
                                                ToastUtil.showToast(InfoNearPersonActivity.this, getResources().getString(R.string.cancelFocusOk));
                                            }

                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "");
                    }
                } else {
                    startActivity(new Intent(InfoNearPersonActivity.this, LoginActivity.class));
                }

                break;
        }
    }

    void initViews() {

        titlebar.setVisibility(View.VISIBLE);
        titlebar.setBarTitle("情趣达人档案");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoNearPersonActivity.this.finish();
            }
        });
        nearInfoUser = (InterestSubPerson) getIntent().getSerializableExtra(Constants.NEAR_USER);
        BmobQuery<RootUser> query = new BmobQuery<>();

        if (nearInfoUser.user != null) {
            query.getObject(nearInfoUser.user.getObjectId(), new QueryListener<RootUser>() {
                @Override
                public void done(RootUser dbRootUser, BmobException e) {
                    if (dbRootUser != null) {
                        info = new BmobIMUserInfo(dbRootUser.getObjectId(), dbRootUser.getUsername(), dbRootUser.photoFileUrl);
                    }
                }
            });

        }
        Glide.with(this).load(nearInfoUser.absCoverPic).error(R.drawable.error_img).into(userImage);

        initAddButton();
        initOther();
        initOnlineAndAge();
        initBannerAndAudio();
        getImageAndText();
        getPhotoLibs();
        getSeeHer20person();
        getVideoLibs();

        loadview.setVisibility(View.GONE);
    }

    private void initOther() {
        audio_layout.setVisibility(View.VISIBLE);
        rl_self_photo.setVisibility(View.VISIBLE);
        rl_self_video.setVisibility(View.VISIBLE);
        last_see_20_rl.setVisibility(View.VISIBLE);

        int vip = Integer.parseInt(nearInfoUser.vipType);

        if (nearInfoUser.vipType.equals("0")) {
            isCanDate.setText("先在软件里聊天试试");
        } else {
            isCanDate.setText("见面一起做爱做的事");
        }

        soundLen.setText(nearInfoUser.soundLen == null ? "" : nearInfoUser.soundLen + "秒");
        disPurpose.setText(nearInfoUser.user.disPurpose == null ? "" : nearInfoUser.user.disPurpose);
        disMariState.setText(nearInfoUser.user.disMariState == null ? "" : nearInfoUser.user.disMariState);
        nickName.setText(nearInfoUser.username == null ? "" : nearInfoUser.username);
        if (nearInfoUser.userType.equals("vip")) {
            level_img.setVisibility(View.VISIBLE);
        }
    }


    void initBannerAndAudio() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        banner.setLayoutParams(new FrameLayout.LayoutParams(size.x - 10, size.x - 10));
        if (getIntent() != null) {
            nearInfoUser = (InterestSubPerson) getIntent().getSerializableExtra(Constants.NEAR_USER);
            iv_voice.setVisibility(View.VISIBLE);
            BmobIMAudioMessage message = new BmobIMAudioMessage();
            message.setFromId(nearInfoUser.getObjectId());
            message.setRemoteUrl("http://file.nidong.com/" + nearInfoUser.soundUrl);
            iv_voice.setOnClickListener(new NewRecordPlayClickListener(this, message, iv_voice, true));

            if (nearInfoUser.pics.contains(";")) {
                ArrayList<String> bannerUrls = new ArrayList<>();
                for (String url : Arrays.asList(nearInfoUser.pics.split(";"))) {
                    url = "http://file.nidong.com//" + url;
                    bannerUrls.add(url);
                }
                banner.setImages(bannerUrls).setImageLoader(new GlideImageLoader(R.drawable.placehoder_img)).start();

            } else {
                banner.setImages(Arrays.asList(new String[]{nearInfoUser.pics})).setImageLoader(new GlideImageLoader(R.drawable.placehoder_img)).start();
            }


        }
    }


    void initOnlineAndAge() {

        Random random = new Random();
        int maxMinu = 800;
        int minMinu = 30;

        int maxLineMinu = 1000;

        int onlineTimeMinu = random.nextInt(maxLineMinu) + random.nextInt(minMinu);
        String randomAge = (random.nextInt(10) + 20) + "";
        int seeTimeMinu = random.nextInt(maxMinu) + random.nextInt(minMinu);
        VirtualUserInfoDao dao = VirtualUserInfoDao.getInstance(this);
        VirtualUserInfo dataUser = dao.query(nearInfoUser.userId);

        if (dataUser != null) {
            randomAge = dataUser.userAge != null ? dataUser.userAge : "";
            if (!dataUser.onlineTime.equals("0")) {
                onlineTimeMinu = !TextUtils.isEmpty(dataUser.onlineTime) ? Integer.parseInt(dataUser.onlineTime) : 0;
            } else {
                dataUser.onlineTime = onlineTimeMinu + "";
                dao.update(dataUser);
            }
//            seeTimeMinu = !TextUtils.isEmpty(dataUser.seeMeTime) ? Integer.parseInt(dataUser.seeMeTime) : 0;
        } else {
            VirtualUserInfo user_ = new VirtualUserInfo();
            user_.userAge = randomAge;
            user_.userId = nearInfoUser.userId;
            user_.onlineTime = onlineTimeMinu + "";
            user_.seeMeTime = seeTimeMinu + "";
            dao.add(user_);
        }

        if (onlineTimeMinu != 0) {
            if (onlineTimeMinu % 60 == 0) {
                online_time.setText(onlineTimeMinu / 60 + "小时前在线");
            } else {
                online_time.setText((onlineTimeMinu - (onlineTimeMinu % 60)) / 60 + "小时" + onlineTimeMinu % 60 + "分前在线");
            }
        }

        if (nearInfoUser.user.sex != null) {
            sexAndAge.setText(nearInfoUser.user.sex.equals("女") ? "女 " + randomAge + "岁" : "男 " + randomAge + "岁");
        } else {
            sexAndAge.setText(randomAge + "岁");
        }

    }

    void initAddButton() {

        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user", user);
        query.addWhereEqualTo("friendUser", nearInfoUser.user == null ? "123" : nearInfoUser.user);
        query.order("-updatedAt");
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        findViewById(R.id.addFriend).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.addFriend).setVisibility(View.VISIBLE);
                        findViewById(R.id.addFriend).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (user != null) {
                                    if (BmobIM.getInstance().getCurrentStatus().getMsg().equals("connected")) {
                                        sendAddFriendMessage();
                                    } else {
                                        ToastUtil.showToast(InfoNearPersonActivity.this, "通讯请求中");
                                    }
                                } else {
                                    startActivity(new Intent(InfoNearPersonActivity.this, LoginActivity.class));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (user != null) {
            BmobQuery<MyFocusCommonPerson> query = new BmobQuery<>();
            query.setLimit(1);
            query.addWhereEqualTo("userId", nearInfoUser.userId);
            query.addWhereEqualTo("rootUser", user);
            query.addWhereEqualTo("userType", Constants.NEAR);
            query.findObjects(new FindListener<MyFocusCommonPerson>() {
                @Override
                public void done(List<MyFocusCommonPerson> myFcPersons, BmobException e) {
                    if (e == null) {
                        if (myFcPersons.size() >= 1) {
                            person = myFcPersons.get(0);
                            focus.setText(getResources().getString(R.string.cancelFocus));
                        }
                    }
                }
            });
        }
    }


    void getPhotoLibs() {

        final ArrayList<String> photoImgs = new ArrayList<>();
        final ArrayList<NearPhotoEntity> nearPhotoEntities = CommonUtil.jsonToArrayList(nearInfoUser.photoLibraries, NearPhotoEntity.class);
        HorizontialListView listView = (HorizontialListView) findViewById(R.id.photoLibsList);
        for (NearPhotoEntity entity : nearPhotoEntities) {
            photoImgs.add(entity.url);
        }
        listView.setAdapter(new PostImageAdapter(this, photoImgs, Constants.ITEM_USER_INFO_IMG, true, false));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (nearPhotoEntities.size() == 1) {
                    startActivity(BGAPhotoPreviewActivity.newIntent(InfoNearPersonActivity.this, downloadDir, nearPhotoEntities.get(i).url));
                } else if (nearPhotoEntities.size() > 1) {
                    startActivity(BGAPhotoPreviewActivity.newIntent(InfoNearPersonActivity.this, downloadDir, photoImgs, i));
                }
            }
        });
    }

    void getVideoLibs() {
        final ArrayList<NearVideoEntity> nearVideoEntities = CommonUtil.jsonToArrayList(nearInfoUser.videoLibraries, NearVideoEntity.class);
        HorizontialListView listView = (HorizontialListView) findViewById(R.id.videoLibsList);
        ArrayList<String> imgs = new ArrayList<>();
        for (NearVideoEntity entity : nearVideoEntities) {
            imgs.add(entity.videoPic);
        }
        listView.setAdapter(new PostImageAdapter(this, imgs, Constants.ITEM_USER_INFO_IMG, true, true));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra(Constants.NEAR_USER_VIDEO, nearVideoEntities.get(i));
                intent.setClass(InfoNearPersonActivity.this, NearMediaVideoListActivity.class);
                startActivity(intent);
            }
        });
    }

    void getImageAndText() {

        HorizontialListView listView = (HorizontialListView) findViewById(R.id.txt_img_listview);
        ArrayList<String> imgs = new ArrayList<>();
        if (nearInfoUser.pics != null && nearInfoUser.sharePics.length() > 0) {
            for (String imgUlr : nearInfoUser.sharePics.split(";")) {
                imgs.add(imgUlr);
            }
        }

        listView.setAdapter(new PostImageAdapter(this, imgs, Constants.ITEM_USER_INFO_IMG, true, false));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra(Constants.COMMON_USER_ID, nearInfoUser.userId);
//                intent.setClass(InfoNearPersonActivity.this,NearPersonPostListActivity.class);
//                startActivity(intent);
            }
        });
    }

    void getSeeHer20person() {

        final ArrayList<NearSeeHerEntity> nearSeeHerEntities = CommonUtil.jsonToArrayList(nearInfoUser.viewUsers, NearSeeHerEntity.class);
        HorizontialListView listView = (HorizontialListView) findViewById(R.id.who_see_her_imglist);
        NearSeeHer20Adapter mAdapter = new NearSeeHer20Adapter(nearSeeHerEntities, InfoNearPersonActivity.this, true);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDialog(nearSeeHerEntities.get(i));
            }
        });

    }

    void showDialog(NearSeeHerEntity nearSeeHerEntity) {
        FragmentDialog.newInstance(false, nearSeeHerEntity.nickname, "", "关闭", "", nearSeeHerEntity.headUrl, nearSeeHerEntity.createTime, true, new FragmentDialog.OnClickBottomListener() {
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


    /**
     * 发送添加好友的请求
     */
    void sendAddFriendMessage() {
        if (info != null) {
            BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
            BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
            AddFriendMessage msg = new AddFriendMessage();
            RootUser currentUser = user;
            msg.setContent("很高兴认识你，可以加个好友吗?");//给对方的一个留言信息
            Map<String, Object> map = new HashMap<>();
            map.put("name", currentUser.getUsername());//发送者姓名
            map.put("avatar", currentUser.photoFileUrl);//发送者的头像
            map.put("uid", currentUser.getObjectId());//发送者的uid
            msg.setExtraMap(map);
            messageManager.sendMessage(msg, new MessageSendListener() {
                @Override
                public void done(BmobIMMessage msg, BmobException e) {
                    if (e == null) {//发送成功
                        ToastUtil.showToast(InfoNearPersonActivity.this, "好友请求发送成功，等待验证");
                    } else {//发送失败
                        ToastUtil.showToast(InfoNearPersonActivity.this, "发送失败:" + e.getMessage());
                    }
                }
            });
        }

    }

    /**
     * 与陌生人私聊
     */
    void chatPrivate() {
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("c", conversationEntrance);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清理导致内存泄露的资源
        BmobIM.getInstance().clear();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 注册自定义消息接收事件
     *
     * @param event
     */
    @Subscribe
    public void onEventMain(RefreshEvent event) {
        findViewById(R.id.addFriend).setVisibility(View.GONE);
    }


}


