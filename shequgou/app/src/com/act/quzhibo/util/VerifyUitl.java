package com.act.quzhibo.util;


import android.content.Context;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.widget.EditText;

import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.i.OnCheckInputListner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobUser;

public class VerifyUitl {

    Context context;
    OnCheckInputListner listner;

    public VerifyUitl(Context context, OnCheckInputListner listner) {
        this.context = context;
        this.listner = listner;
    }

    public boolean verifyInputTrue(EditText et_c_newpsw,
                                   EditText et_sms_code,
                                   EditText et_newpsw,
                                   EditText et_invite_code,
                                   EditText et_userNick,
                                   EditText et_userPhonenumber,
                                   EditText et_password) {
        if (et_sms_code != null) {
            if (TextUtils.isEmpty(et_sms_code.getText()) || et_sms_code.getText().toString().equals("请输入短信验证码")) {
                ToastUtil.showToast(context, "请输入短信验证码");
                listner.onCheckInputLisner(et_sms_code);
                return false;
            }
        }

        if (et_userPhonenumber != null) {
            if (et_userPhonenumber.getText().toString().equals("手机号") ||
                    et_userPhonenumber.getText().toString().equals("") ||
                    et_userPhonenumber.getText().length() > 11) {
                ToastUtil.showToast(context, "请输入11位数的手机号码");
                listner.onCheckInputLisner(et_userPhonenumber);
                return false;
            } else {
                String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))||(17[4|7])|(18[0,5-9]))\\d{8}$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(et_userPhonenumber.getText().toString());
                if (!m.find()) {
                    ToastUtil.showToast(context, "请输入国内通用的手机号码");
                    listner.onCheckInputLisner(et_userPhonenumber);
                    return false;
                }
            }
        }


        if (et_password != null) {
            if (et_password.getText().toString().equals("密码") || et_password.getText().toString().equals("")) {
                ToastUtil.showToast(context, "请输入密码");
                listner.onCheckInputLisner(et_password);
                return false;
            }
        }


        if (et_userNick != null) {
            if (et_userNick.getText().toString().equals("用户名") ||
                    et_userNick.getText().toString().equals("") ||
                    et_userNick.getText().length() > 20) {
                ToastUtil.showToast(context, "用户名必须填写且少于20个字符");
                listner.onCheckInputLisner(et_userNick);
                return false;
            }
        }

        if (et_invite_code != null) {
            if (et_invite_code.getText().toString().equals("邀请码,务必要填") || et_invite_code.getText().toString().equals("")) {
                ToastUtil.showToast(context, "请输入邀请码");
                listner.onCheckInputLisner(et_invite_code);
                return false;
            }
        }


        if (et_newpsw != null) {
            if (TextUtils.isEmpty(et_newpsw.getText()) || et_newpsw.getText().toString().equals("新密码")) {
                ToastUtil.showToast(context, "请输入新密码");
                listner.onCheckInputLisner(et_newpsw);
                return false;
            }
        }

        if (et_c_newpsw != null) {
            if (TextUtils.isEmpty(et_c_newpsw.getText()) || et_c_newpsw.getText().toString().equals("确认密码")) {
                ToastUtil.showToast(context, "请确认新密码");
                listner.onCheckInputLisner(et_c_newpsw);
                return false;
            }
        }

        if (et_userPhonenumber != null&&BmobUser.getCurrentUser(RootUser.class)!=null) {
            if (!et_userPhonenumber.getText().toString().equals(BmobUser.getCurrentUser(RootUser.class).getMobilePhoneNumber())) {
                ToastUtil.showToast(context, "旧手机号不匹配");
                listner.onCheckInputLisner(et_userPhonenumber);
                return false;
            }
        }
        return true;
    }

}
