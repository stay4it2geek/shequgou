package com.act.quzhibo.bean;

import cn.bmob.v3.BmobObject;

/**好友表
 */
public class Friend extends BmobObject {

    private RootUser user;
    private RootUser friendUser;
    private transient String pinyin;
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
    public void setUser(RootUser user) {
        this.user = user;
    }
    public RootUser getFriendUser() {
        return friendUser;
    }
    public void setFriendUser(RootUser friendUser) {
        this.friendUser = friendUser;
    }
}
