package com.act.quzhibo.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.act.quzhibo.R;


/**
 * 商品评论界面
 */
public class CommentFragment extends Fragment {


    public CommentFragment() {
    }


    private static CommentFragment fragment = null;

    public static CommentFragment newInstance() {
        if (fragment == null) {
            fragment = new CommentFragment();
        }
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

}
