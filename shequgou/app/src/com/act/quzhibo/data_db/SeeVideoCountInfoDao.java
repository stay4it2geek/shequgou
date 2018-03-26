package com.act.quzhibo.data_db;

import android.content.Context;
import android.database.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.util.ArrayList;
import java.util.List;


public class SeeVideoCountInfoDao {

    Context context;
    Dao<SeeVideoCountData, String> dao;

    private static SeeVideoCountInfoDao instance;

    private SeeVideoCountInfoDao(Context context) {
        super();
        this.context = context;
        DbHelper mHelper = DbHelper.getInstance(context);
        try {
            dao = mHelper.getDao(SeeVideoCountData.class);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }


    public static SeeVideoCountInfoDao getInstance(Context context) {
        if (instance == null) {
            synchronized (DbHelper.class) {
                if (instance == null) {
                    instance = new SeeVideoCountInfoDao(context.getApplicationContext());
                }
            }
        }
        return instance;
    }


    public int add(SeeVideoCountData data) {
        try {
            return dao.create(data);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<SeeVideoCountData> queryAll() {
        try {
            return dao.queryForAll();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete() {
        try {
            List<SeeVideoCountData> users = dao.queryForAll();
            if (users.size() > 0) {
                DeleteBuilder<SeeVideoCountData, String> deleteBuilder = dao.deleteBuilder();
                for (int i=0;i<users.size();i++) {
                    deleteBuilder.delete();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

}