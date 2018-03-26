package com.act.quzhibo.im.holder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.act.quzhibo.bean.InterestSubPerson;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.i.OnRecyclerViewListener;
import com.act.quzhibo.ui.activity.InfoNearPersonActivity;

import butterknife.ButterKnife;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;


public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public OnRecyclerViewListener onRecyclerViewListener;
    public Context context;

    public BaseViewHolder(Context context, ViewGroup root, int layoutRes, OnRecyclerViewListener listener) {
        super(LayoutInflater.from(context).inflate(layoutRes, root, false));
        this.context = context;
        ButterKnife.bind(this, itemView);
        this.onRecyclerViewListener = listener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public Context getContext() {
        return itemView.getContext();
    }

    public abstract void bindData(T t);

    @Override
    public void onClick(View v) {
        if (onRecyclerViewListener != null) {
            onRecyclerViewListener.onItemClick(getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onRecyclerViewListener != null) {
            onRecyclerViewListener.onItemLongClick(getAdapterPosition(),v);
        }
        return true;
    }

    public void requestPerson(BmobIMMessage message) {
        BmobQuery<RootUser> rootUserBmobQuery = new BmobQuery<>();
        if (message != null) {
            rootUserBmobQuery.getObject(message.getBmobIMConversation().getConversationId(), new QueryListener<RootUser>() {
                @Override
                public void done(RootUser rootUser, BmobException e) {
                    if (rootUser != null && rootUser.interestSubPerson != null) {
                        BmobQuery<InterestSubPerson> rootUserBmobQuery = new BmobQuery<>();

                        rootUserBmobQuery.getObject(rootUser.interestSubPerson.getObjectId(), new QueryListener<InterestSubPerson>() {
                            @Override
                            public void done(InterestSubPerson person, BmobException e) {
                                if (person != null) {
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.NEAR_USER, person);
                                    intent.setClass(context, InfoNearPersonActivity.class);
                                    context.startActivity(intent);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}