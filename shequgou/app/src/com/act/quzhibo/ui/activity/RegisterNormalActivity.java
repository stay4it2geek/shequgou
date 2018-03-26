package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Promotion;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnCheckInputListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.util.VerifyUitl;
import com.act.quzhibo.widget.TitleBarView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;


public class RegisterNormalActivity extends BaseActivity {

    @Bind(R.id.et_userPhonenumber)
    EditText et_userPhonenumber;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.check_agree)
    CheckBox check_agree;
    @Bind(R.id.et_userNick)
    EditText et_userNick;
    @Bind(R.id.rl_sms_code)
    RelativeLayout rl_sms_code;
    @Bind(R.id.verify_fail)
    TextView verify_fail;
    @Bind(R.id.btn_verify_login)
    Button btn_verify_login;
    @Bind(R.id.titlebar)
    TitleBarView titlebar;
    private Animation shakeAnimation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        rl_sms_code.setVisibility(View.GONE);
        verify_fail.setVisibility(View.GONE);
        btn_verify_login.setText("确 认 注 册");
        titlebar.setBarTitle("普 通 注 册");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);

        et_password.setSingleLine(true);
        et_password.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
    }

    @OnClick(R.id.titlebar)
    void finishActivity() {
        RegisterNormalActivity.this.finish();
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

    @OnClick(R.id.termofuse)
    void startActivity() {
        startActivity(new Intent(RegisterNormalActivity.this, TermOfUseActivity.class));
    }

    @OnClick(R.id.btn_verify_login)
    void verifyAndLogin() {
        if (!check_agree.isChecked()) {
            ToastUtil.showToast(this, "请先同意用户协议");
            return;
        }

        VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
            @Override
            public void onCheckInputLisner(EditText editText) {
                editText.startAnimation(shakeAnimation);
            }
        });
        if (uitl.verifyInputTrue(null, null, null, null, et_userNick, et_userPhonenumber, et_password)) {

            final RootUser user = new RootUser();
            user.setUsername(et_userPhonenumber.getText().toString());
            user.setPassword(et_password.getText().toString());
            user.setMobilePhoneNumber(et_userPhonenumber.getText().toString());
            user.setUsername(et_userNick.getText().toString());
            user.signUp(new SaveListener<RootUser>() {
                @Override
                public void done(RootUser refereeUser, BmobException e) {
                    if (e == null) {
                        ToastUtil.showToast(RegisterNormalActivity.this, "注册成功");
                        if (refereeUser != null) {
                            ToastUtil.showToast(RegisterNormalActivity.this, "登录成功");
                            CommonUtil.fecth(RegisterNormalActivity.this);
                            finish();
                        }
                    } else {
                        ToastUtil.showToast(RegisterNormalActivity.this, "登录失败，原因是：" + e.getLocalizedMessage());
                    }
                }
            });
        }

    }
}

