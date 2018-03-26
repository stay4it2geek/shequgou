package com.act.quzhibo.data_db;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DbHelper extends OrmLiteSqliteOpenHelper {

    private static DbHelper instance;

    private DbHelper(Context context) {
        super(context, "data.db", null, 1);
    }
    public static DbHelper getInstance(Context context){
        if(instance == null){
            synchronized (DbHelper.class){
                if(instance == null){
                    instance = new DbHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, VirtualUserInfo.class);
//            TableUtils.createTable(connectionSource, ConfigToggleData.class);
            TableUtils.createTable(connectionSource, SeeVideoCountData.class);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        try {
            TableUtils.dropTable(connectionSource,VirtualUserInfo.class,true);
//            TableUtils.dropTable(connectionSource,ConfigToggleData.class,true);
            TableUtils.dropTable(connectionSource,SeeVideoCountData.class,true);
            onCreate(sqLiteDatabase,connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }


}
