package com.act.quzhibo.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.act.quzhibo.R;
import com.act.quzhibo.adapter.ChatAdapter;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.luban_compress.Luban;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.FileUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.TitleBarView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMAudioMessage;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMLocationMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.bean.BmobIMVideoMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.BmobRecordManager;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.newim.listener.OnRecordChangeListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.act.quzhibo.common.Constants.REQUEST_MAP;
import static com.act.quzhibo.common.Constants.TAKE_PHOTO;
import static com.act.quzhibo.common.Constants.TAKE_VIDEO;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
/**
 * 聊天界面
 */
public class ChatActivity extends FragmentActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    @Bind(R.id.ll_chat)
    LinearLayout ll_chat;

    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;

    @Bind(R.id.rc_view)
    RecyclerView rc_view;

    @Bind(R.id.edit_msg)
    EmojiconEditText edit_msg;

    @Bind(R.id.btn_chat_add)
    Button btn_chat_add;
    @Bind(R.id.btn_chat_emo)
    Button btn_chat_emo;
    @Bind(R.id.btn_speak)
    Button btn_speak;
    @Bind(R.id.btn_chat_voice)
    Button btn_chat_voice;
    @Bind(R.id.btn_chat_keyboard)
    Button btn_chat_keyboard;
    @Bind(R.id.btn_chat_send)
    Button btn_chat_send;

    @Bind(R.id.layout_more)
    LinearLayout layout_more;
    @Bind(R.id.layout_add)
    LinearLayout layout_add;
    @Bind(R.id.layout_emo)
    LinearLayout layout_emo;

    @Bind(R.id.layout_record)
    RelativeLayout layout_record;
    @Bind(R.id.tv_voice_tips)
    TextView tv_voice_tips;
    @Bind(R.id.iv_record)
    ImageView iv_record;

    private Drawable[] mDrawableAnims;// 话筒动画
    private BmobRecordManager mRecordManager;
    private Toast mShortToast;
    private ChatAdapter mAdapter;
    protected LinearLayoutManager mLayoutManager;
    private BmobIMConversation mConversationManager;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_PICK = 2;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();
    public AMapLocation amlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        BmobIMConversation conversationEntrance = (BmobIMConversation) getIntent().getSerializableExtra("c");
        //根据会话入口获取消息管理，聊天页面
        mConversationManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        String title = mConversationManager.getConversationTitle();
        if (title != null && title.length() > 20) {
            title = title.replaceAll(title.substring(5, 15), "......");
        }
        titlebar.setBarTitle("与" + title + "聊天中");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initSwipeLayout();
        initVoiceView();
        initBottomView();
        EventBus.getDefault().register(this);
        initLocation();
    }


    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        AMapLocationListener locationListener = new AMapLocationListener() {

            @Override
            public void onLocationChanged(AMapLocation loc) {
                if (null != loc) {
                    amlocation = loc;
                }
            }
        };
        locationClient.setLocationListener(locationListener);
        startLocation();
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }


    /**
     * 开始定位
     */
    private void startLocation() {
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }


    private void initSwipeLayout() {
        sw_refresh.setEnabled(true);
        mLayoutManager = new LinearLayoutManager(this);
        rc_view.setLayoutManager(mLayoutManager);
        mAdapter = new ChatAdapter(mConversationManager);
        rc_view.setAdapter(mAdapter);
        ll_chat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_chat.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                sw_refresh.setRefreshing(true);
                //自动刷新
                queryMessages(null);
            }
        });
        //下拉加载
        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BmobIMMessage msg = mAdapter.getFirstMessage();
                queryMessages(msg);
            }
        });
        //设置RecyclerView的点击事件
        mAdapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(final int position) {
                BmobIMMessage message = mAdapter.getItem((position));
                String type = message.getMsgType();
                if (type == null) {
                    return;
                } else {
                    if ("location".equals(type)) {
                        BmobIMLocationMessage locMsg = BmobIMLocationMessage.buildFromDB(message);
                        if (CommonUtil.isGdMapInstalled()) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse(CommonUtil.getGdMapUri("高德", amlocation.getLatitude(), amlocation.getLongitude(), "我的位置", locMsg.getLatitude(), locMsg.getLongitude(), locMsg.getAddress())));
                            intent.setPackage("com.autonavi.minimap");
                            startActivity(intent);
                        } else {
                            ToastUtil.showToast(ChatActivity.this, "您没有安装高德地图应用哦");

                        }
                    }
                }

            }

            @Override
            public boolean onItemLongClick(final int position, View view) {
                //删除指定聊天消息
                FragmentDialog.newInstance(false, "是否删除该条消息？", "删除后不可恢复", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                        mConversationManager.deleteMessage(mAdapter.getItem(position));
                        mAdapter.remove(position);
                    }

                    @Override
                    public void onNegtiveClick(Dialog dialog) {
                        dialog.dismiss();

                    }
                }).show(getSupportFragmentManager(), "");
                return false;
            }
        });
    }

    private void initBottomView() {
        edit_msg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                    scrollToBottom();
                }
                return false;
            }
        });
        edit_msg.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                scrollToBottom();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btn_chat_send.setVisibility(View.VISIBLE);
                    btn_chat_keyboard.setVisibility(View.GONE);
                    btn_chat_voice.setVisibility(View.GONE);
                } else {
                    if (btn_chat_voice.getVisibility() != View.VISIBLE) {
                        btn_chat_voice.setVisibility(View.VISIBLE);
                        btn_chat_send.setVisibility(View.GONE);
                        btn_chat_keyboard.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 初始化语音布局
     */
    private void initVoiceView() {
        btn_speak.setOnTouchListener(new VoiceTouchListener());
        initVoiceAnimRes();
        initRecordManager();
    }

    /**
     * 初始化语音动画资源
     */
    private void initVoiceAnimRes() {
        mDrawableAnims = new Drawable[]{
                getResources().getDrawable(R.mipmap.chat_icon_voice2),
                getResources().getDrawable(R.mipmap.chat_icon_voice3),
                getResources().getDrawable(R.mipmap.chat_icon_voice4),
                getResources().getDrawable(R.mipmap.chat_icon_voice5),
                getResources().getDrawable(R.mipmap.chat_icon_voice6)};
    }

    private void initRecordManager() {
        // 语音相关管理器
        mRecordManager = BmobRecordManager.getInstance(this);
        // 设置音量大小监听--在这里开发者可以自己实现：当剩余10秒情况下的给用户的提示，类似微信的语音那样
        mRecordManager.setOnRecordChangeListener(new OnRecordChangeListener() {


            @Override
            public void onVolumeChanged(int value) {
                iv_record.setImageDrawable(mDrawableAnims[value]);

            }

            @Override
            public void onTimeChanged(int recordTime, String localPath) {
                if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1分钟结束，发送消息
                    // 需要重置按钮
                    btn_speak.setPressed(false);
                    btn_speak.setClickable(false);
                    // 取消录音框
                    layout_record.setVisibility(View.INVISIBLE);
                    // 发送语音消息
                    sendVoiceMessage(localPath, recordTime);
                    //是为了防止过了录音时间后，会多发一条语音出去的情况。
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            btn_speak.setClickable(true);
                        }
                    }, 1000);
                }
            }
        });
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(edit_msg, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(edit_msg);
    }

    /**
     * 长按说话
     */
    class VoiceTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!FileUtil.checkSdCard()) {
                        ToastUtil.showToast(ChatActivity.this, "发送语音需要sdcard支持！");
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        layout_record.setVisibility(View.VISIBLE);
                        tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
                        // 开始录音
                        mRecordManager.startRecording(mConversationManager.getConversationId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
                        tv_voice_tips.setTextColor(Color.RED);
                    } else {
                        tv_voice_tips.setText(getString(R.string.voice_up_tips));
                        tv_voice_tips.setTextColor(Color.WHITE);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    layout_record.setVisibility(View.INVISIBLE);
                    try {
                        if (event.getY() < 0) {// 放弃录音
                            mRecordManager.cancelRecording();
                        } else {
                            int recordTime = mRecordManager.stopRecording();
                            if (recordTime > 1) {
                                // 发送语音文件
                                sendVoiceMessage(mRecordManager.getRecordFilePath(mConversationManager.getConversationId()), recordTime);
                            } else {// 录音时间过短，则提示录音过短的提示
                                layout_record.setVisibility(View.GONE);
                                showShortToast().show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }


    /**
     * 显示录音时间过短的Toast
     */
    private Toast showShortToast() {
        if (mShortToast == null) {
            mShortToast = new Toast(this);
        }
        View view = LayoutInflater.from(this).inflate(
                R.layout.include_chat_voice_short, null);
        mShortToast.setView(view);
        mShortToast.setGravity(Gravity.CENTER, 0, 0);
        mShortToast.setDuration(Toast.LENGTH_SHORT);
        return mShortToast;
    }

    @OnClick(R.id.edit_msg)
    public void onEditClick(View view) {
        if (layout_more.getVisibility() == View.VISIBLE) {
            layout_add.setVisibility(View.GONE);
            layout_emo.setVisibility(View.GONE);
            layout_more.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btn_chat_emo)
    public void onEmoClick(View view) {
        if (layout_more.getVisibility() == View.GONE) {
            showEditState(true);
        } else {
            if (layout_add.getVisibility() == View.VISIBLE) {
                layout_add.setVisibility(View.GONE);
                layout_emo.setVisibility(View.VISIBLE);
            } else {
                layout_more.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.btn_chat_add)
    public void onAddClick(View view) {
        if (layout_more.getVisibility() == View.GONE) {
            layout_more.setVisibility(View.VISIBLE);
            layout_add.setVisibility(View.VISIBLE);
            layout_emo.setVisibility(View.GONE);
            hideSoftInputView();
        } else {
            if (layout_emo.getVisibility() == View.VISIBLE) {
                layout_emo.setVisibility(View.GONE);
                layout_add.setVisibility(View.VISIBLE);
            } else {
                layout_more.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.btn_chat_voice)
    public void onVoiceClick(View view) {
        edit_msg.setVisibility(View.GONE);
        layout_more.setVisibility(View.GONE);
        btn_chat_voice.setVisibility(View.GONE);
        btn_chat_keyboard.setVisibility(View.VISIBLE);
        btn_speak.setVisibility(View.VISIBLE);
        hideSoftInputView();
    }

    @OnClick(R.id.btn_chat_keyboard)
    public void onKeyClick(View view) {
        showEditState(false);
    }

    @OnClick(R.id.btn_chat_send)
    public void onSendClick(View view) {
        sendMessage();
    }

    @OnClick(R.id.tv_picture)
    public void onPictureClick(View view) {
        //跳转到调用系统图库
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "请选择图片"), REQUEST_PICK);
    }

    @OnClick(R.id.tv_camera)
    public void onCameraClick(View view) {
        startActivityForResult(new Intent(ChatActivity.this, CameraActivity.class), REQUEST_CAMERA);
    }

    @OnClick(R.id.tv_location)
    public void onLocationClick(View view) {
        if (amlocation != null) {
            FragmentDialog.newInstance(false, "使用精准位置还是附近位置？", "选个你的附近位置就行了", "选择附近位置", "选择当前位置", "", "", false, new FragmentDialog.OnClickBottomListener() {
                @Override
                public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                    Intent intent = new Intent(ChatActivity.this, GaoDeMapActivity.class);
                    intent.putExtra("lng", amlocation.getLongitude());
                    intent.putExtra("lat", amlocation.getLatitude());
                    intent.putExtra("address", amlocation.getAddress());
                    startActivityForResult(intent, Constants.REQUEST_MAP);
                }

                @Override
                public void onNegtiveClick(Dialog dialog) {
                    sendLocationMessage();

                }
            }).show(getSupportFragmentManager(),"");

        } else {
            ToastUtil.showToast(ChatActivity.this, "定位失败,请退出页面重试");
        }


    }

    /**
     * 根据是否点击笑脸来显示文本输入框的状态
     *
     * @param isEmo 用于区分文字和表情
     * @return void
     */
    private void showEditState(boolean isEmo) {
        edit_msg.setVisibility(View.VISIBLE);
        btn_chat_keyboard.setVisibility(View.GONE);
        btn_chat_voice.setVisibility(View.VISIBLE);
        btn_speak.setVisibility(View.GONE);
        edit_msg.requestFocus();
        if (isEmo) {
            layout_more.setVisibility(View.VISIBLE);
            layout_more.setVisibility(View.VISIBLE);
            layout_emo.setVisibility(View.VISIBLE);
            layout_add.setVisibility(View.GONE);
            hideSoftInputView();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
                    .commit();
        } else {
            layout_more.setVisibility(View.GONE);
            showSoftInputView();
        }
    }

    /**
     * 显示软键盘
     */
    public void showSoftInputView() {
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .showSoftInput(edit_msg, 0);
        }
    }

    /**
     * 发送文本消息
     */
    private void sendMessage() {
        String text = edit_msg.getText().toString();
        if (TextUtils.isEmpty(text.trim())) {
            ToastUtil.showToast(ChatActivity.this, "请输入内容");
            return;
        }
        BmobIMTextMessage msg = new BmobIMTextMessage();
        msg.setContent(text);
        mConversationManager.sendMessage(msg, listener);
    }

    /**
     * 发送本地图片文件
     */
    public void sendLocalImageMessage(String path) {
        BmobIMImageMessage image = new BmobIMImageMessage(new File(path));
        mConversationManager.sendMessage(image, listener);
    }


    /**
     * 发送本地视频文件
     */
    private void sendLocalVideoMessage(String path) {
        BmobIMVideoMessage video = new BmobIMVideoMessage(new File(path));
        mConversationManager.sendMessage(video, listener);
    }


    /**
     * 发送语音消息
     *
     * @param local
     * @param length
     * @return void
     * @Title: sendVoiceMessage
     */
    private void sendVoiceMessage(String local, int length) {
        BmobIMAudioMessage audio = new BmobIMAudioMessage(local);
        audio.setDuration(length);
        mConversationManager.sendMessage(audio, listener);
    }


    /**
     * 发送地理位置消息
     */
    public void sendLocationMessage() {

        if (amlocation != null) {
            BmobIMLocationMessage location = new BmobIMLocationMessage(amlocation.getAddress(), amlocation.getLatitude(), amlocation.getLongitude());
            Map<String, Object> map = new HashMap<>();
            map.put("from", "高德地图");
            location.setExtraMap(map);
            mConversationManager.sendMessage(location, listener);
        } else {
            ToastUtil.showToast(ChatActivity.this, "定位失败,请退出页面重试");
        }
    }

    /**
     * 消息发送监听器
     */
    public MessageSendListener listener = new MessageSendListener() {

        @Override
        public void onProgress(int value) {
            super.onProgress(value);
            //文件类型的消息才有进度值
        }

        @Override
        public void onStart(BmobIMMessage msg) {
            super.onStart(msg);
            mAdapter.addMessage(msg);
            edit_msg.setText("");
            scrollToBottom();
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            mAdapter.notifyDataSetChanged();
            edit_msg.setText("");
            scrollToBottom();
            if (e != null) {
                ToastUtil.showToast(ChatActivity.this, e.getMessage());
            }
        }
    };

    /**
     * 首次加载，可设置msg为null，下拉刷新的时候，默认取消息表的第一个msg作为刷新的起始时间点，默认按照消息时间的降序排列
     *
     * @param msg
     */
    public void queryMessages(BmobIMMessage msg) {
        //查询指定会话的消息记录
        mConversationManager.queryMessages(msg, 10, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                sw_refresh.setRefreshing(false);
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        mAdapter.addMessages(list);
                        mLayoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                    }
                }
            }
        });
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void scrollToBottom() {
        mLayoutManager.scrollToPositionWithOffset(mAdapter.getItemCount() - 1, 0);
    }

    private void addMessage2Chat(MessageEvent event) {
        BmobIMMessage msg = event.getMessage();
        if (mConversationManager != null && event != null && mConversationManager.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
                && !msg.isTransient()) {//并且不为暂态消息
            if (mAdapter.findPosition(msg) < 0) {//如果未添加到界面中
                mAdapter.addMessage(msg);
                //更新该会话下面的已读状态
                mConversationManager.updateReceiveStatus(msg);
            }
            scrollToBottom();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (layout_more.getVisibility() == View.VISIBLE) {
                layout_more.setVisibility(View.GONE);
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    protected void onResume() {
        //锁屏期间的收到的未读消息需要添加到聊天界面中
        addUnReadMessage();
        //添加页面消息监听器
        MyApplicaition.handler.isChatting = true;
        // 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知
        BmobNotificationManager.getInstance(this).cancelNotification();
        super.onResume();
    }

    /**
     * 添加未读的通知栏消息到聊天界面
     */
    private void addUnReadMessage() {
        List<MessageEvent> cache = BmobNotificationManager.getInstance(this).getNotificationCacheList();
        if (cache.size() > 0) {
            int size = cache.size();
            for (int i = 0; i < size; i++) {
                MessageEvent event = cache.get(i);
                addMessage2Chat(event);
            }
        }
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //清理资源
        if (mRecordManager != null) {
            mRecordManager.clear();
        }
        MyApplicaition.handler.isChatting = false;
        //更新此会话的所有消息为已读状态
        if (mConversationManager != null) {
            mConversationManager.updateLocalCache();
        }
        hideSoftInputView();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        stopLocation();
        destroyLocation();

        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
        }
    }


    //通知有在线消息接收
    @Subscribe
    public void onEventMain(final MessageEvent event) {
        if (MyApplicaition.handler.isChatting) {
            addMessage2Chat(event);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK) {
            if (resultCode == RESULT_OK) {

                String imgPath = FileUtil.getRealFilePathFromUri(this, data.getData());
                Luban.get(this)
                        .load(new File(imgPath))
                        .putGear(Luban.THIRD_GEAR)
                        .asObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        })
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends File>>() {
                            @Override
                            public Observable<? extends File> call(Throwable throwable) {
                                return Observable.empty();
                            }
                        })
                        .subscribe(new Action1<File>() {
                            @Override
                            public void call(File file) {
                                sendLocalImageMessage(file.getAbsolutePath());
                            }
                        });


            }
        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == TAKE_PHOTO) {
                Luban.get(this)
                        .load(new File(data.getStringExtra("path") != null ? data.getStringExtra("path") : ""))
                        .putGear(Luban.THIRD_GEAR)
                        .asObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        })
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends File>>() {
                            @Override
                            public Observable<? extends File> call(Throwable throwable) {
                                return Observable.empty();
                            }
                        })
                        .subscribe(new Action1<File>() {
                            @Override
                            public void call(File file) {
                                sendLocalImageMessage(file.getAbsolutePath());
                            }
                        });


            } else if (resultCode == TAKE_VIDEO) {
                sendLocalVideoMessage(data.getStringExtra("url") != null ? data.getStringExtra("url") : "");
            }
        } else if (requestCode == REQUEST_MAP) {
            if (resultCode == RESULT_OK) {

                String address = data.getStringExtra("address");
                if (!TextUtils.isEmpty(address)) {
                    BmobIMLocationMessage location = new BmobIMLocationMessage(address, data.getDoubleExtra("lat",0.0), data.getDoubleExtra("lng",0.0));
                    Map<String, Object> map = new HashMap<>();
                    map.put("from", "高德地图");
                    location.setExtraMap(map);
                    mConversationManager.sendMessage(location, listener);
                } else {
                    ToastUtil.showToast(ChatActivity.this, "定位失败,请退出页面重试");
                }
            }

        }

    }

}





