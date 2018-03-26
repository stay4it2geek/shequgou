package com.act.quzhibo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnCheckInputListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.util.VerifyUitl;
import com.act.quzhibo.widget.TitleBarView;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;


public class ResetPasswordActivity extends BaseActivity {
    @Bind(R.id.et_newpsw)
    EditText et_newpsw;
    @Bind(R.id.et_c_newpsw)
    EditText et_c_newpsw;
    @Bind(R.id.et_sms_code)
    EditText et_sms_code;
    @Bind(R.id.et_userPhonenumber)
    EditText et_userPhonenumber;
    @Bind(R.id.titlebar)
    TitleBarView titlebar;
    @Bind(R.id.getCode_btn)
    Button getCode_btn;

    @OnClick({R.id.getCode_btn, R.id.btn_verify_resetPsw})
    void buttonOnClick(View view) {
        switch (view.getId()) {
            case R.id.getCode_btn:
                getCode();
                break;
            case R.id.btn_verify_resetPsw:
                verifyAndResetPsw();
                break;
        }
    }

    int T = 20;
    Handler mHandler = new Handler();
    Animation shakeAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);
        CommonUtil.fecth(ResetPasswordActivity.this);
        titlebar.setBarTitle(" 重 置 密 码");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setEditext(R.id.isSetPswVisiNew, et_newpsw);
        setEditext(R.id.isSetPswVisiConfirm, et_c_newpsw);
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);

    }

    private void setEditext(int viewId, final EditText editText) {
        editText.setSingleLine(true);
        editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
        ((CheckBox) findViewById(viewId)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
                }
                editText.setSelection(editText.getText().length());

            }
        });
    }

    private void resetPasswordBySMSCodeBtn() {

        VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
            @Override
            public void onCheckInputLisner(EditText editText) {
                editText.startAnimation(shakeAnimation);
            }
        });
        if (uitl.verifyInputTrue(et_c_newpsw, et_sms_code, null, null, null, null, null)) {
            BmobUser.getCurrentUser(RootUser.class).resetPasswordBySMSCode(et_sms_code.getText().toString().trim(), et_c_newpsw.getText().toString(), new UpdateListener() {

                @Override
                public void done(BmobException ex) {
                    if (ex == null) {
                        CommonUtil.fecth(ResetPasswordActivity.this);
                        ToastUtil.showToast(ResetPasswordActivity.this, "密码重置成功");
                        ResetPasswordActivity.this.finish();
                    } else {
                        ToastUtil.showToast(ResetPasswordActivity.this, "密码重置失败：" + "原因是：" + ex.getLocalizedMessage() + ex.getErrorCode());

                    }

                }
            });
        }
    }

    private void getCode() {

        VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
            @Override
            public void onCheckInputLisner(EditText editText) {
                editText.startAnimation(shakeAnimation);
            }
        });
        if (uitl.verifyInputTrue(null, null, null, null, null, et_userPhonenumber, null)) {
            new Thread(new MyCountDownTimer()).start();//开始执行

            BmobSMS.requestSMSCode(et_userPhonenumber.getText().toString(),
                    "您的验证码是`%smscode%`，有效期为`%ttl%`分钟。您正在使用`%appname%`的验证码。【比目科技】", new QueryListener<Integer>() {

                        @Override
                        public void done(Integer o, BmobException e) {
                            if (e == null) {
                                ToastUtil.showToast(ResetPasswordActivity.this, "短信验证码已经发送,序列号是：" + o);
                            } else {
                                ToastUtil.showToast(ResetPasswordActivity.this, "短信验证码发送失败，原因是" + e.getLocalizedMessage());

                            }
                        }
                    });
        }
    }

    private void verifyAndResetPsw() {

        VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
            @Override
            public void onCheckInputLisner(EditText editText) {
                editText.startAnimation(shakeAnimation);
            }
        });
        if (uitl.verifyInputTrue(et_c_newpsw, et_sms_code, et_newpsw, null, null, et_userPhonenumber, null)) {
            BmobSMS.verifySmsCode(et_userPhonenumber.getText().toString(), et_sms_code.getText().toString(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        ToastUtil.showToast(ResetPasswordActivity.this, "验证成功，正在重置密码");
                        resetPasswordBySMSCodeBtn();
                    } else {
                        ToastUtil.showToast(ResetPasswordActivity.this, "验证失败");
                    }
                }
            });
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
                        getCode_btn.setClickable(false);
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
                    getCode_btn.setClickable(true);
                    getCode_btn.setText("点击获取验证码");
                }
            });
            T = 20; //最后再恢复倒计时时长
        }
    }


}
