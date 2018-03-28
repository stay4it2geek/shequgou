package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.act.quzhibo.R;
import com.act.quzhibo.data_db.SeeVideoCountInfoDao;
import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.lock_view.LockIndicatorView;
import com.act.quzhibo.lock_view.LockViewGroup;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.SelfDialog;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

public class WelcomeActivity extends ActivityManagePermission {

    RootUser user;
    LockIndicatorView mLockIndicator;
    LockViewGroup mLockViewGroup;
    TextView mTvTips;
    LinearLayout secretView;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        secretView = (LinearLayout) findViewById(R.id.secret_view);
        mLockIndicator = (LockIndicatorView) findViewById(R.id.indicator);
        mTvTips = (TextView) findViewById(R.id.tv_tips);
        mLockViewGroup = (LockViewGroup) findViewById(R.id.lockgroup);
        grantPermission();
    }


    @Override
    protected void onResume() {
        super.onResume();
        RootUser user = BmobUser.getCurrentUser(RootUser.class);
        if (user != null &&user.lastLoginTime!=null) {
            CommonUtil.fecth(this);
            long l = System.currentTimeMillis() - Long.parseLong(user.lastLoginTime);
            long day = l / (24 * 60 * 60 * 1000);
            long hour = (l / (60 * 60 * 1000) - day * 24);
            if (hour > 8) {
                VirtualUserInfoDao.getInstance(this).updateOnlineTime2Space();
            }
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final String showTabList = (String) msg.obj;
            if (!TextUtils.isEmpty(showTabList)) {
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        Intent intent = new Intent();
                        intent.setClass(WelcomeActivity.this, TabMainActivity.class);
                        intent.putExtra(Constants.TAB_PLATE_LIST, showTabList);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        }
    };

    void initData() {
        String[] passWords = user.secretPassword.split(";");
        int[] nums = new int[passWords.length];
        for (int i = 0; i < passWords.length; i++) {
            nums[i] = Integer.parseInt(passWords[i]);
        }
        mLockViewGroup.setAnswer(nums);
        mLockViewGroup.setMaxTryTimes(5);
        mLockViewGroup.setOnLockListener(new LockViewGroup.OnLockListener() {

            @Override
            public void onLockSelected(int id) {
            }

            @Override
            public void onLess4Points() {
                mLockViewGroup.clear2ResetDelay(1200L); //清除错误
                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("至少连接4个点 , 请重新输入");
            }

            @Override
            public void onSaveFirstAnswer(int[] answer) {
                mTvTips.setTextColor(Color.GRAY);
                mTvTips.setText("再次绘制 , 确认解锁图案");
                mLockIndicator.setAnswer(answer);
            }

            @Override
            public void onSucessed(int[] answers) {
                mTvTips.setTextColor(Color.BLACK);
                mTvTips.setText("验证成功");
                secretView.setVisibility(View.GONE);
                secretView.setAnimation(AnimationUtils.makeOutAnimation(WelcomeActivity.this, true));
                OkHttpClientManager.parseRequest(WelcomeActivity.this, url, handler, Constants.REFRESH);
            }

            @Override
            public void onFailed(int mTryTimes) {
                mLockViewGroup.clear2ResetDelay(1400L); //清除错误
                mLockViewGroup.setHapticFeedbackEnabled(true); //手机振动
                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("与上一次绘制不一致 , 请重新绘制");

                if (mTryTimes > 0) {
                    Toast.makeText(WelcomeActivity.this, "剩余尝试机会: " + mTryTimes + " 次", Toast.LENGTH_SHORT).show();
                } else {
                    ToastUtil.showToast(getApplicationContext(), "设置失败");
                    finish();
                }
                Animation shakeAnimation = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.shake);
                mTvTips.startAnimation(shakeAnimation);
            }

            @Override
            public void onSetAnswerInit() {
                mTvTips.setText("绘制解锁图案");
            }
        });

    }

    void grantPermission() {

        final SelfDialog selfDialog = new SelfDialog(WelcomeActivity.this, false);
        askCompactPermissions(new String[]{PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_RECORD_AUDIO, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE}, new PermissionResult() {
            @Override
            public void permissionGranted() {
                BmobQuery<Toggle> query = new BmobQuery<>();
                query.findObjects(new FindListener<Toggle>() {
                    @Override
                    public void done(List<Toggle> list, BmobException e) {
                        if (e == null && list.size() > 0) {
                            url = list.get(0).showTabCatagory;
                            CommonUtil.saveInitData(WelcomeActivity.this, Constants.PUA_COURSE_INFO, list.get(0).puaCourseCatogry);
                            CommonUtil.saveInitData(WelcomeActivity.this, Constants.MONEY_COURSE_INFO, list.get(0).moneyCourseCatogry);
                            CommonUtil.saveInitData(WelcomeActivity.this, Constants.HIDE_TAB_VIEW, list.get(0).hideTabView);
                            CommonUtil.saveInitData(WelcomeActivity.this, Constants.HIDE_SHOW_PLATES, list.get(0).hideShowPlates);
                            doRequest();
                        } else {
                            grantPermission();
                        }
                    }
                });

            }

            @Override
            public void permissionDenied() {
                selfDialog.setTitle("同意权限后才能正常使用哦");
                selfDialog.setMessage("您需要同意以下权限:\n   1. 访问设备上的照片\n   2. 访问手机存储功能\n   3. 获取您的大概位置\n   4. 拍照\n   5. 录像");
                selfDialog.setYesOnclickListener("立即同意", new SelfDialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        grantPermission();
                        selfDialog.dismiss();
                    }
                });
                selfDialog.setNoOnclickListener("取消", new SelfDialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick() {
                        finish();
                    }
                });
                selfDialog.show();
            }

            @Override
            public void permissionForeverDenied() {
                selfDialog.setTitle("同意权限后才能正常使用哦");
                selfDialog.setMessage("您需要同意以下权限:\n   1. 访问设备上的照片\n   2. 访问手机存储功能\n   3. 获取您的大概位置\n   4. 拍照\n   5. 录像");
                selfDialog.setYesOnclickListener("立即同意", new SelfDialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        grantPermission();
                        selfDialog.dismiss();
                    }
                });
                selfDialog.setNoOnclickListener("取消", new SelfDialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick() {
                        selfDialog.dismiss();
                        finish();
                    }
                });
                selfDialog.show();
            }
        });
    }

    void doRequest() {
        user = BmobUser.getCurrentUser(RootUser.class);
        if (user != null) {
            CommonUtil.fecth(this);
            if (user.secretScan) {
                secretView.setVisibility(View.VISIBLE);
                initData();
            } else {
                secretView.setVisibility(View.GONE);
                OkHttpClientManager.parseRequest(this, url, handler, Constants.REFRESH);
            }
        } else {
            secretView.setVisibility(View.GONE);
            OkHttpClientManager.parseRequest(this, url, handler, Constants.REFRESH);
        }
    }


}