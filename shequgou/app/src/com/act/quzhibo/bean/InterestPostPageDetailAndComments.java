package com.act.quzhibo.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class InterestPostPageDetailAndComments implements Serializable {
    public InterestPostPageContentDetail detail;
    public long totalComments;
    public ArrayList<HotComments> hotComments;
    public ArrayList<InterestPostPageCommentDetail> comments;
    public InterestItemModel item;
    public RewardUsers rewardUsers;
}