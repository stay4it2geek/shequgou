package com.act.quzhibo.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;


public class CommonCourse extends BmobObject implements Serializable {

    public String courseCategoryId;
    public String courseTag;
    public String courseName;
    public String courseAppPrice;
    public String courseMarketPrice;
    public String courseDetail;
    public String leanerCount;
    public BmobFile courseImage;
    public String selectionNum;
    public String courseDetaiId;
    public boolean needPay;
    public String courseUiType;
    public String freePromotion;
    public String downloadUrl;
    public String downloadPsw;
    public String profitPercent;
    public String   promotionMoney;

}
