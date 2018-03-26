package com.act.quzhibo.bean;

import com.act.quzhibo.chat.NewFriend;

import cn.bmob.v3.BmobUser;

public class RootUser extends BmobUser {
    public Integer vipConis;
    public boolean isVip;
    public String lookVideoCount;
    public String age;
    public String datingthought;
    public String disPurpose;
    public String sex;
    public String provinceAndcity;
    public boolean secretScan;
    public String secretPassword;
    public String photoFileUrl;
    public String lastLoginTime;
    public String canDateThing;
    public boolean hasSetting;
    public InterestSubPerson interestSubPerson;
    public String disMariState;

    public RootUser(){}
    public RootUser(NewFriend friend){
        setObjectId(friend.getUid());
        setUsername(friend.getName());
        setAvatar(friend.getAvatar());
    }

    public String getAvatar() {
        return photoFileUrl;
    }

    public void setAvatar(String photoFileUrl) {
        this.photoFileUrl = photoFileUrl;
    }
}
