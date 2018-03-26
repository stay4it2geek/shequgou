package com.act.quzhibo.bean;

import java.io.Serializable;
import java.util.ArrayList;
public class InterestPostPageCommentDetail implements Serializable {
    public String cid;
    public InterestParentPerson user;
    public ArrayList<SubComments> subComments;
    public long totalChildren;
    public boolean hot;
    public long floor;
    public long ctime;
    public long likes;
    public String message;
    public boolean delete;
    public boolean liked;
}

