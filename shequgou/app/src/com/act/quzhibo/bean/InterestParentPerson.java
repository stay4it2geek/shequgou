package com.act.quzhibo.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * 趣味帖子的用户实体模型
 */
public class InterestParentPerson extends BmobObject implements Serializable{
    public String userId;
    public String photoUrl;
    public String nick;
    public String disMariState;
    public String disPurpose;
    public String proCode;
    public String cityCode;
    public String areaCode;
    public String vipLevel;
    public String hxNick;
    public String birth;
    public String sex;
    public String official;
    public String exp;
    public String viewTime;


}
