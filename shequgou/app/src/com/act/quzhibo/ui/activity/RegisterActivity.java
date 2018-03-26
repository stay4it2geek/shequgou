package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnCheckInputListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.util.VerifyUitl;
import com.act.quzhibo.widget.TitleBarView;


import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

public class RegisterActivity extends BaseActivity {

    @Bind(R.id.et_userPhonenumber)
    EditText et_userPhonenumber;
    @Bind(R.id.et_sms_code)
    EditText et_sms_code;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.check_agree)
    CheckBox check_agree;
    @Bind(R.id.getCode_btn)
    Button getCode_btn;
    @Bind(R.id.et_userNick)
    EditText et_userNick;
    @Bind(R.id.btn_verify_login)
    Button btn_verify_login;
    @Bind(R.id.titlebar)
    TitleBarView titlebar;

    int T = 20;
    Handler mHandler = new Handler();
    private Animation shakeAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        titlebar.setBarTitle("手 机 注 册");
        et_password.setSingleLine(true);
        et_password.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);

    }

    @OnClick(R.id.verify_fail)
    void verifyFail() {
        startActivity(new Intent(RegisterActivity.this, RegisterNormalActivity.class));
        RegisterActivity.this.finish();
    }


    @OnClick(R.id.getCode_btn)
    void getSmsCode() {

        VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
            @Override
            public void onCheckInputLisner(EditText editText) {
                editText.startAnimation(shakeAnimation);
            }
        });
        if (uitl.verifyInputTrue(null, et_sms_code, null, null, null, et_userPhonenumber, null)) {
            new Thread(new MyCountDownTimer()).start();//开始执行

            BmobSMS.requestSMSCode(et_userPhonenumber.getText().toString(),
                    "您的验证码是`%smscode%`，有效期为`%ttl%`分钟。您正在使用`%appname%`的验证码。【比目科技】", new QueryListener<Integer>() {
                        @Override
                        public void done(Integer o, BmobException e) {
                            if (e == null) {
                                ToastUtil.showToast(RegisterActivity.this, "短信验证码已经发送,序列号是：" + o);
                            } else {
                                ToastUtil.showToast(RegisterActivity.this, "短信验证码发送失败，原因是" + e.getLocalizedMessage());

                            }
                        }
                    });
        }
    }

    @OnCheckedChanged(R.id.isSetPswVisi)
    void onSetPswVisi(boolean isChecked) {
        if (isChecked) {
            et_password.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            et_password.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
        }
        et_password.setSelection(et_password.getText().length());
    }

    @OnCheckedChanged(R.id.check_agree)
    void onCheckAgree(boolean isChecked) {
        if (isChecked) {
            btn_verify_login.setEnabled(true);
        } else {
            btn_verify_login.setEnabled(false);
        }
    }

    @OnClick(R.id.termofuse)
    void startActivity() {
        startActivity(new Intent(RegisterActivity.this, TermOfUseActivity.class));
    }

    @OnClick(R.id.btn_verify_login)
    void verifyAndLogin() {

        VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
            @Override
            public void onCheckInputLisner(EditText editText) {
                editText.startAnimation(shakeAnimation);
            }
        });
        if (uitl.verifyInputTrue(null, et_sms_code, null, null, et_userNick, et_userPhonenumber, et_password)) {

            BmobSMS.verifySmsCode(et_userPhonenumber.getText().toString(), et_sms_code.getText().toString().trim(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        RootUser rootUser = new RootUser();
                        rootUser.setUsername(et_userNick.getText().toString());
                        rootUser.setMobilePhoneNumber(et_userPhonenumber.getText().toString());
                        rootUser.setMobilePhoneNumberVerified(true);
                        ToastUtil.showToast(RegisterActivity.this, "验证成功，自动注册登录中......");
                        rootUser.signOrLoginByMobilePhone(et_userPhonenumber.getText().toString(), et_sms_code.getText().toString().trim(), new LogInListener<RootUser>() {

                            @Override
                            public void done(RootUser user, BmobException e) {
                                if (user != null) {
                                    CommonUtil.fecth(RegisterActivity.this);
                                    ToastUtil.showToast(RegisterActivity.this, "登录成功");
                                    RegisterActivity.this.finish();
                                } else {
                                    findViewById(R.id.verify_fail).setVisibility(View.VISIBLE);
                                    ToastUtil.showToast(RegisterActivity.this, "登录失败，原因是：" + e.getLocalizedMessage());

                                }
                            }

                        });
                    } else {
                        findViewById(R.id.verify_fail).setVisibility(View.VISIBLE);
                        ToastUtil.showToast(RegisterActivity.this, "验证失败，原因是：" + e.getLocalizedMessage());

                    }
                }
            });
        } else {
            ToastUtil.showToast(RegisterActivity.this, "邀请码错误");
        }
    }


    private class MyCountDownTimer implements Runnable {

        @Override
        public void run() {

            //倒计时开始，循环
            while (T > 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        getCode_btn.setEnabled(false);
                        getCode_btn.setText(T + "秒后重新开始");
                    }
                });
                try {
                    Thread.sleep(1000); //强制线程休眠1秒，就是设置倒计时的间隔时间为1秒。
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                T--;
            }

            //倒计时结束，也就是循环结束
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getCode_btn.setEnabled(true);
                    getCode_btn.setText("点击获取验证码");
                }
            });
            T = 20; //最后再恢复倒计时时长
        }
    }

}
