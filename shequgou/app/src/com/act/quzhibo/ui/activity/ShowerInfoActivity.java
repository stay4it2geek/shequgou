package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.download.event.FocusChangeEvent;
import com.act.quzhibo.bean.MyFocusShower;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.util.GlideImageLoader;
import com.act.quzhibo.bean.Room;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.LoadNetView;
import com.act.quzhibo.widget.TitleBarView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.youth.banner.Banner;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


public class ShowerInfoActivity extends FragmentActivity {
    private Room room;
    private LoadNetView loadNetView;
    private MyFocusShower mMyFocusShower;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shower_info);
        room = (Room) getIntent().getSerializableExtra("room");
        if (room != null) {
            getData(room.userId);
        }
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        titlebar.setBarTitle("主 播 档 案");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowerInfoActivity.this.finish();
            }
        });
        findViewById(R.id.chat_private).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatPrivate();
            }
        });
        loadNetView = (LoadNetView) findViewById(R.id.loadview);
        loadNetView.setReloadButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNetView.setlayoutVisily(Constants.LOAD);
                getData(room.userId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                            ((TextView) findViewById(R.id.focus)).setText(getResources().getString(R.string.cancelFocus));
                        }
                    }
                }
            });
        }


        findViewById(R.id.focus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BmobUser.getCurrentUser(RootUser.class) != null) {
                    if (!(((TextView) findViewById(R.id.focus)).getText().toString().trim()).equals(getResources().getString(R.string.cancelFocus))) {
                        if (mMyFocusShower == null) {
                            MyFocusShower myFocusShower = new MyFocusShower();
                            myFocusShower.rootUser = BmobUser.getCurrentUser(RootUser.class);
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
                            if (getIntent().getBooleanExtra("FromChatFragment", false)) {
                                myFocusShower.portrait_path_1280 = getIntent().getStringExtra("photoUrl");
                            } else if (getIntent().getBooleanExtra("FromShowListActivity", false)) {
                                myFocusShower.portrait_path_1280 = "http://ures.kktv8.com/kktv" + room.portrait_path_1280;
                            } else {
                                myFocusShower.portrait_path_1280 = room.portrait_path_1280;
                            }

                            myFocusShower.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        ((TextView) findViewById(R.id.focus)).setText(getResources().getString(R.string.cancelFocus));
                                        EventBus.getDefault().post(new FocusChangeEvent(true,getIntent().getIntExtra("showPosition",0),"show"));
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
                                                            ShowerInfoActivity.this.mMyFocusShower = myFocusShowers.get(0);
                                                            ((TextView) findViewById(R.id.focus)).setText(getResources().getString(R.string.cancelFocus));
                                                            EventBus.getDefault().post(new FocusChangeEvent(true,getIntent().getIntExtra("showPosition",0),"show"));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        ToastUtil.showToast(ShowerInfoActivity.this, getResources().getString(R.string.focusOk));
                                    } else {
                                        ToastUtil.showToast(ShowerInfoActivity.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        } else {
                            mMyFocusShower.update(mMyFocusShower.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        ((TextView) findViewById(R.id.focus)).setText(getResources().getString(R.string.cancelFocus));
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
                                                            ShowerInfoActivity.this.mMyFocusShower = myFocusShowers.get(0);
                                                            ((TextView) findViewById(R.id.focus)).setText(getResources().getString(R.string.cancelFocus));
                                                            EventBus.getDefault().post(new FocusChangeEvent(true,getIntent().getIntExtra("showPosition",0),"show"));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        ToastUtil.showToast(ShowerInfoActivity.this, getResources().getString(R.string.focusOk));
                                    } else {
                                        ToastUtil.showToast(ShowerInfoActivity.this, getResources().getString(R.string.focusFail));
                                    }
                                }
                            });
                        }

                    } else {
                        FragmentDialog.newInstance(false, getResources().getString(R.string.isCancelFocus), getResources().getString(R.string.reallyCancelFocus), getResources().getString(R.string.keepFocus), getResources().getString(R.string.cancelFocus),"","",false, new FragmentDialog.OnClickBottomListener() {
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
                                                ((TextView) findViewById(R.id.focus)).setText(getResources().getString(R.string.focusTa));
                                                ToastUtil.showToast(ShowerInfoActivity.this, getResources().getString(R.string.cancelFocusOk));
                                                EventBus.getDefault().post(new FocusChangeEvent(false,getIntent().getIntExtra("showPosition",0),"show"));
                                            }

                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "");
                    }
                } else {
                    startActivity(new Intent(ShowerInfoActivity.this, LoginActivity.class));
                }
            }
        });
    }

    public void getData(final String userId) {
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url = toggles.get(0).showerInfo.replace("USERID", userId);
                    OkHttpClientManager.parseRequest(ShowerInfoActivity.this, url, handler, Constants.REFRESH);
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
                try {
                    JSONObject jsonObject = new JSONObject((String) msg.obj);
                    String fansCount = jsonObject.getString("fansCount");
                    String nickname = jsonObject.getString("nickname");
                    String introduce = jsonObject.getString("introduce");
                    String portrait_img = jsonObject.getString("portrait_path_1280");
                    String gender = jsonObject.getString("gender");
                    JSONObject jsonObject1 = jsonObject.getJSONObject("getPhotoListResult");
                    if (msg.what != Constants.NetWorkError) {
                        ((TextView) findViewById(R.id.fansCount)).setText(fansCount != null ? "粉丝 " + fansCount : "");
                        if (!introduce.equals("") && introduce != null) {
                            findViewById(R.id.introduce).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.introduce)).setText(introduce);
                        }
                        ((TextView) findViewById(R.id.nickName)).setText(nickname != null ? nickname : "");

                        JSONArray photos = jsonObject1.getJSONArray("photoList");

                        List<String> urls = new ArrayList<>();
                        if (photos != null && photos.length() > 0) {
                            for (int i = 0; i < photos.length(); i++) {
                                JSONObject jsonObject2 = photos.getJSONObject(i);
                                String url = (String) jsonObject2.get("photo_path_original");
                                urls.add(url);
                            }
                        } else {
                            urls.clear();
                            urls.add(portrait_img);
                        }
                        final ImageView showerAvatar = (ImageView) findViewById(R.id.userImage);

                            Glide.with(ShowerInfoActivity.this).load(portrait_img).asBitmap().placeholder(R.mipmap.default_head).error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    showerAvatar.setBackgroundDrawable(new BitmapDrawable(resource));
                                }

                                @Override
                                public void onLoadStarted(Drawable placeholder) {
                                    super.onLoadStarted(placeholder);
                                }
                            });
                            ((Banner) findViewById(R.id.banner)).setImages(urls).setImageLoader(new GlideImageLoader(R.mipmap.default_head)).start();
                        }

                        loadNetView.setVisibility(View.GONE);


                } catch (JSONException e) {

                } catch (Exception e) {

                }

            } else {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.RELOAD);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_OK);
        finish();
    }
    BmobIMUserInfo info;

    /**
     * 与陌生人私聊
     */
    void chatPrivate() {
        if(BmobUser.getCurrentUser(RootUser.class)!=null){
        info = new BmobIMUserInfo(room.userId,room.nickname,room.poster_path_1280);
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("c", conversationEntrance);
        startActivity(intent);}else{
            startActivity(new Intent(this,LoginActivity.class));
        }
    }

}
