package com.act.quzhibo.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import com.act.quzhibo.download.bean.MediaInfoLocal;
import com.act.quzhibo.download.bean.MyDownloadInfLocal;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;



public class DBHelper extends OrmLiteSqliteOpenHelper {

  //  private static final String DB_NAME = "/sdcard/d/data.db";
  private static final String DB_NAME = "data.db";
  private static final int DB_VERSION = 3;

  public DBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
    try {
      TableUtils.createTable(connectionSource, MediaInfoLocal.class);
      TableUtils.createTable(connectionSource, MyDownloadInfLocal.class);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion,
                        int newVersion) {
    try {
      TableUtils.dropTable(connectionSource, MediaInfoLocal.class, true);
      TableUtils.dropTable(connectionSource, MyDownloadInfLocal.class, true);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
