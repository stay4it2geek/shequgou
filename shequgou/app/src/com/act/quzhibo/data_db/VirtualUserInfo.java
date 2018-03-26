package com.act.quzhibo.data_db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = VirtualUserInfo.TABLE_NAME)
public class VirtualUserInfo {

    public static final String TABLE_NAME = "BaseDataTable";

    @DatabaseField(columnName = "userId")
    public String userId;

    @DatabaseField(columnName = "userAge")
    public String userAge;

    @DatabaseField(columnName = "onlineTime")
    public String onlineTime;

    @DatabaseField(columnName = "seeMeTime")

    public String seeMeTime;
    //空的构造方法一定要有，否则数据库会创建失败
    public VirtualUserInfo() {
    }

}
