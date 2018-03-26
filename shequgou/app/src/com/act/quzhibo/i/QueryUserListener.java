package com.act.quzhibo.i;

import com.act.quzhibo.bean.RootUser;

import cn.bmob.newim.listener.BmobListener1;
import cn.bmob.v3.exception.BmobException;

public abstract class QueryUserListener extends BmobListener1<RootUser> {

    public abstract void done(RootUser s, BmobException e);

    @Override
    protected void postDone(RootUser o, BmobException e) {
        done(o, e);
    }
}
