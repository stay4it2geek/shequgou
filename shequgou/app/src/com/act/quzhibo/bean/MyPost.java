package com.act.quzhibo.bean;

import java.io.Serializable;
import java.util.ArrayList;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

public class MyPost extends BmobObject  implements Serializable{
    public String postId;
    public String title;
    public String absText;
    public ArrayList<String> images;
    public String totalImages;
    public String elite;
    public String top;
    public String ctime;
    public String htime;
    public String sName;
    public String totalComments;
    public InterestItemModel itemModel;
    public String plateId;
    public String pageView;
    public String rewards;
    public String type;
    public String heat;
    public String hot;
    public String hasbuy;
    public String vedioUrl;
    public BmobUser user;
}
