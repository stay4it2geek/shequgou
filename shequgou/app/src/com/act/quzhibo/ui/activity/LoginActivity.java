package com.act.quzhibo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnCheckInputListner;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.VerifyUitl;
import com.act.quzhibo.widget.TitleBarView;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import me.leefeng.promptlibrary.PromptDialog;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

public class LoginActivity extends BaseActivity {

    @Bind(R.id.et_userPhonenumber)
    EditText et_userPhonenumber;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.titlebar)
    TitleBarView titlebar;
    PromptDialog promptDialog;
    Animation shakeAnimation;

    @Override
    protected void onResume() {
        super.onResume();
        et_userPhonenumber.setText(CommonUtil.loadLoginData(this, "account"));
        et_password.setText(CommonUtil.loadLoginData(this, "passWord"));
        ((CheckBox) findViewById(R.id.isSetPswVisi)).setChecked(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        promptDialog = new PromptDialog(this);
        titlebar.setBarTitle("登  录");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.finish();
            }
        });
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        et_password.setSingleLine(true);
        et_password.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);

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

    @OnClick({R.id.btn_login, R.id.tv_frgetPsw, R.id.tv_register})
    void buttonClicks(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                VerifyUitl uitl = new VerifyUitl(this, new OnCheckInputListner() {
                    @Override
                    public void onCheckInputLisner(EditText editText) {
                        editText.startAnimation(shakeAnimation);
                    }
                });
                if (uitl.verifyInputTrue(null, null, null, null, null, et_userPhonenumber, et_password)) {
                    promptDialog.showLoading("正在登录");
                    CommonUtil.saveLoginData(this, et_userPhonenumber.getText().toString(), et_password.getText().toString());
                    BmobUser.loginByAccount(et_userPhonenumber.getText().toString(), et_password.getText().toString(), new LogInListener<RootUser>() {
                        @Override
                        public void done(RootUser user, BmobException e) {
                            if (user != null) {
                                promptDialog.showSuccess("登录成功", true);
                                ;
                                LoginActivity.this.finish();
                            } else {
                                promptDialog.showError("登录失败", true);
                            }
                        }
                    });
                }
                break;
            case R.id.tv_frgetPsw:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                finish();
                break;
            case R.id.tv_register:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (promptDialog.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
