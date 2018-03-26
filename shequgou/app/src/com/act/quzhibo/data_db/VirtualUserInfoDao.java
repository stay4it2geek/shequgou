package com.act.quzhibo.data_db;

import android.content.Context;
import android.database.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.util.List;

public class VirtualUserInfoDao {

    Context context;
    Dao<VirtualUserInfo, String> dao;

    private static VirtualUserInfoDao instance;

    private VirtualUserInfoDao(Context context) {
        super();
        this.context = context;
        DbHelper mHelper = DbHelper.getInstance(context);
        try {
            dao = mHelper.getDao(VirtualUserInfo.class);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }


    public static VirtualUserInfoDao getInstance(Context context) {
        if (instance == null) {
            synchronized (DbHelper.class) {
                if (instance == null) {
                    instance = new VirtualUserInfoDao(context.getApplicationContext());
                }
            }
        }
        return instance;
    }


    public int add(VirtualUserInfo user) {
        try {
            return dao.create(user);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public VirtualUserInfo query(String userId) {
        try {
            return dao.queryBuilder().where().eq("userId", userId).queryForFirst();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int update(VirtualUserInfo user) {
        try {
            return dao.update(user);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void delete() {
        try {
            List<VirtualUserInfo> users = dao.queryForAll();
            if (users.size() > 0) {
                DeleteBuilder<VirtualUserInfo, String> deleteBuilder = dao.deleteBuilder();
                for (VirtualUserInfo useAge : users) {
                    deleteBuilder.where().eq("userId", useAge.userId);
                    deleteBuilder.delete();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateOnlineTime2Space() {
        try {
            List<VirtualUserInfo> users = dao.queryForAll();
            if (users.size() > 0) {
                for (VirtualUserInfo user : users) {
                    user.onlineTime = "";
                    dao.update(user);}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

}