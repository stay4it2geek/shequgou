package com.act.quzhibo.data_db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = ConfigToggleData.TABLE_NAME)
public class ConfigToggleData {

    public static final String TABLE_NAME = "config_toggle_data_tb";

    @DatabaseField(columnName = "versionUpdate")
    public String versionUpdate;

    @DatabaseField(columnName = "commonTabDetail")
    public String commonTabDetail;

    @DatabaseField(columnName = "hideTabView")
    public String hideTabView;

    @DatabaseField(columnName = "squareInterestPost")
    public String squareInterestPost;


    @DatabaseField(columnName = "termOfUse")
    public String termOfUse;

    @DatabaseField(columnName = "hideShowPlates")
    public String hideShowPlates;

    @DatabaseField(columnName = "puaCourseCatogry")
    public String puaCourseCatogry;

    @DatabaseField(columnName = "squareInterestTab")
    public String squareInterestTab;

    @DatabaseField(columnName = "showerInfo")
    public String showerInfo;

    @DatabaseField(columnName = "moneyCourseCatogry")
    public String moneyCourseCatogry;


    @DatabaseField(columnName = "showTabCatagory")
    public String showTabCatagory;


    @DatabaseField(columnName = "interestPostList")
    public String interestPostList;


    @DatabaseField(columnName = "interestPostDetail")
    public String interestPostDetail;


    @DatabaseField(columnName = "onShow")
    public String onShow;








    //空的构造方法一定要有，否则数据库会创建失败
    public ConfigToggleData() {
    }

}
