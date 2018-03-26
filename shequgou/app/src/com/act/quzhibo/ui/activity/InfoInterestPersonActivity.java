package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.data_db.VirtualUserInfo;
import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.adapter.PostImageAdapter;
import com.act.quzhibo.bean.InterestPost;
import com.act.quzhibo.bean.InterestPostListInfoPersonParentData;
import com.act.quzhibo.bean.InterstPostListInfoResult;
import com.act.quzhibo.bean.MyFocusCommonPerson;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.download.event.FocusChangeEvent;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class InfoInterestPersonActivity extends BaseActivity {

    @Bind(R.id.titlebar)
    TitleBarView titlebar;
    @Bind(R.id.loadview)
    LoadNetView loadNetView;
    @Bind(R.id.sexAndage)
    TextView ageText;
    @Bind(R.id.banner)
    Banner banner;
    @Bind(R.id.isCanDate)
    TextView isCanDate;
    @Bind(R.id.textpost)
    TextView textpost;
    @Bind(R.id.disMariState)
    TextView disMariState;
    @Bind(R.id.txt_img_listview)
    HorizontialListView listView;
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
    InterestPost post;
    RootUser user;
    MyFocusCommonPerson person;
    BmobIMUserInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_common_layout);
        ButterKnife.bind(this);
        user = BmobUser.getCurrentUser(RootUser.class);
        initView();
    }

    void initView() {

        titlebar.setBarTitle("情趣达人档案");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoInterestPersonActivity.this.finish();
            }
        });

        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                initView();
            }
        });

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        banner.setLayoutParams(new FrameLayout.LayoutParams(size.x - 10, size.x - 10));

        if (getIntent() != null) {
            post = (InterestPost) getIntent().getSerializableExtra(Constants.POST);
            ArrayList<String> urls = new ArrayList<>();
            if (post.images != null && post.images.size() > 0) {
                urls.addAll(post.images);
            } else {
                urls.add(post.user.photoUrl);
            }
            if (Integer.parseInt(post.user.vipLevel)>5) {
                level_img.setVisibility(View.VISIBLE);
            }

            setUserAvater(urls);
        }
        initOnlineTime();
        getTextAndImageData();
        initOther();
        info = new BmobIMUserInfo(post.user.userId, post.user.nick, post.user.photoUrl);
    }

    void initOther() {
        int vip = Integer.parseInt(post.user.vipLevel);
        if (vip == 0) {
            isCanDate.setText("先在软件里聊天试试");
        } else {
            isCanDate.setText("见面一起做爱做的事");
        }

        disPurpose.setText(post.user.disPurpose);
        disMariState.setText(post.user.disMariState);
        String nick = post.user.nick.replaceAll("\r|\n", "");
        nickName.setText(nick);
    }

    void initOnlineTime() {
        VirtualUserInfoDao dao = VirtualUserInfoDao.getInstance(this);

        Random random = new Random();
        int minMinu = 30;
        int maxSeeMinu = 1000;
        int maxMinu = 900;
        int onlineTimeMinu = random.nextInt(maxMinu) + random.nextInt(minMinu);
        String randomAge = (random.nextInt(10) + 20) + "";
        int seeTimeMinu = random.nextInt(maxSeeMinu) + random.nextInt(minMinu);

        VirtualUserInfo dataUser = dao.query(post.user.userId);

        if (dataUser != null) {
            randomAge = dataUser.userAge != null ? dataUser.userAge : "";
            if (!dataUser.onlineTime.equals("0")) {
                onlineTimeMinu = !TextUtils.isEmpty(dataUser.onlineTime) ? Integer.parseInt(dataUser.onlineTime) : 0;
            } else {
                dataUser.onlineTime = onlineTimeMinu + "";
                dao.update(dataUser);
            }


        } else {
            VirtualUserInfo user_ = new VirtualUserInfo();
            user_.userAge = randomAge;
            user_.userId = post.user.userId;
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
        ageText.setText(randomAge);
    }

    @OnClick({R.id.addFriend,R.id.talk_accese,R.id.focus,R.id.chat_private})
    void buttonClicks(View view) {
        switch (view.getId()) {
            case R.id.talk_accese:
                startActivity(new Intent(this, GetVipPayActivity.class));
                break;
            case R.id.focus:
                if (user != null) {
                    if (!(focus.getText().toString().trim()).equals(getResources().getString(R.string.cancelFocus))) {
                        if (person == null) {
                            MyFocusCommonPerson person = new MyFocusCommonPerson();
                            person.rootUser = user;
                            person.username = post.user.nick;
                            person.userId = post.user.userId;
                            person.photoUrl = post.user.photoUrl;
                            person.sex = post.user.sex;
                            person.userType = Constants.INTEREST;
                            person.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        focus.setText(getResources().getString(R.string.cancelFocus));
                                        if (user != null) {
                                            BmobQuery<MyFocusCommonPerson> query = new BmobQuery<>();
                                            query.setLimit(1);
                                            query.addWhereEqualTo("userId", post.user.userId);
                                            query.addWhereEqualTo("rootUser", user);
                                            query.findObjects(new FindListener<MyFocusCommonPerson>() {
                                                @Override
                                                public void done(List<MyFocusCommonPerson> myFocusCommonPersons, BmobException e) {
                                                    if (e == null) {
                                                        if (myFocusCommonPersons.size() >= 1) {
                                                            InfoInterestPersonActivity.this.person = myFocusCommonPersons.get(0);
                                                            focus.setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        ToastUtil.showToast(InfoInterestPersonActivity.this, getResources().getString(R.string.focusOk));
                                    } else {
                                        ToastUtil.showToast(InfoInterestPersonActivity.this, getResources().getString(R.string.focusFail));
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
                                            query.addWhereEqualTo("userId", post.user.userId);
                                            query.addWhereEqualTo("rootUser", user);
                                            query.findObjects(new FindListener<MyFocusCommonPerson>() {
                                                @Override
                                                public void done(List<MyFocusCommonPerson> persons, BmobException e) {
                                                    if (e == null) {
                                                        if (persons.size() >= 1) {
                                                            person = persons.get(0);
                                                            focus.setText(getResources().getString(R.string.cancelFocus));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        ToastUtil.showToast(InfoInterestPersonActivity.this, getResources().getString(R.string.focusOk));
                                    } else {
                                        ToastUtil.showToast(InfoInterestPersonActivity.this, getResources().getString(R.string.focusFail));
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
                                if (person != null) {
                                    person.delete(person.getObjectId(), new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                focus.setText(getResources().getString(R.string.focusTa));
                                                person = null;
                                                ToastUtil.showToast(InfoInterestPersonActivity.this, getResources().getString(R.string.cancelFocusOk));
                                                EventBus.getDefault().post(new FocusChangeEvent(false,getIntent().getIntExtra("FocusPersonIndex",0),"person"));
                                            }
                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "");
                    }
                } else {
                    startActivity(new Intent(InfoInterestPersonActivity.this, LoginActivity.class));
                }
                break;
            case R.id.chat_private:
                chatPrivate();
                break;
            case R.id.addFriend:
                ToastUtil.showToast(this, "好友请求发送成功，等待验证");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            BmobQuery<MyFocusCommonPerson> query = new BmobQuery<>();
            query.setLimit(1);
            query.addWhereEqualTo("userId", post.user.userId);
            query.addWhereEqualTo("rootUser", user);
            query.addWhereEqualTo("userType", Constants.INTEREST);
            query.findObjects(new FindListener<MyFocusCommonPerson>() {
                @Override
                public void done(List<MyFocusCommonPerson> myFocusCommonPersons, BmobException e) {
                    if (e == null) {
                        if (myFocusCommonPersons.size() >= 1) {
                            person = myFocusCommonPersons.get(0);
                            focus.setText(getResources().getString(R.string.cancelFocus));
                        }
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

    void setUserAvater(ArrayList<String> urls) {
        banner.setImages(urls).setImageLoader(new GlideImageLoader(R.drawable.placehoder_img)).start();
        Glide.with(this).load(post.user.photoUrl).asBitmap().placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(userImage);
    }


    void getTextAndImageData() {
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url = toggles.get(0).interestPersonPostLs.replace("USERID", post.user.userId).replace("CTIME", "0");
                    OkHttpClientManager.parseRequest(InfoInterestPersonActivity.this, url, handler, Constants.REFRESH);
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
            InterestPostListInfoPersonParentData data =
                    CommonUtil.parseJsonWithGson((String) msg.obj, InterestPostListInfoPersonParentData.class);
            InterstPostListInfoResult result;
            if (data != null) {
                result = data.result;
            } else {
                return;
            }
            if (msg.what != Constants.NetWorkError) {

                if (!TextUtils.isEmpty(result.totalNums)) {
                    textpost.setText("图文动态(" + result.totalNums + ")");
                }

                if (result.posts != null && result.posts.size() > 0) {
                    listView = (HorizontialListView) findViewById(R.id.txt_img_listview);
                    ArrayList<String> imgs = new ArrayList<>();
                    for (InterestPost post : data.result.posts) {
                        if (post.images != null && post.images.size() > 0) {
                            imgs.addAll(post.images);
                        }
                    }

                    if (result.posts.size() > 0 && imgs.size() > 0) {
                        listView.setAdapter(new PostImageAdapter(InfoInterestPersonActivity.this, imgs, Constants.ITEM_USER_INFO_IMG, true, false));
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent = new Intent();
                                intent.putExtra(Constants.COMMON_USER_ID, post.user.userId);
                                intent.setClass(InfoInterestPersonActivity.this, IntersetPersonPostListActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
                loadNetView.setVisibility(View.GONE);
            } else {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.RELOAD);
            }
        }
    };

}
