package com.act.quzhibo.data_db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = SeeVideoCountData.TABLE_NAME)
public class SeeVideoCountData {

    public static final String TABLE_NAME = "seeVideoCount_tb";

    @DatabaseField(columnName = "videoId")
    public String videoId;

    //空的构造方法一定要有，否则数据库会创建失败
    public SeeVideoCountData() {
    }

}
