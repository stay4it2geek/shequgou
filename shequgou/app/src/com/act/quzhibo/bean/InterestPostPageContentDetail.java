package com.act.quzhibo.bean;

import java.io.Serializable;
import java.util.ArrayList;
public class InterestPostPageContentDetail implements Serializable {
    public String postId;
    public String title;
    public ArrayList<PostContentAndImageDesc> desc;
    public InterestParentPerson user;
    public long ctime;
    public boolean elite;
    public boolean top;
    public long pageView;
    public long rewards;
    public long type;
    public long heat;
    public boolean hasbuy;
}
