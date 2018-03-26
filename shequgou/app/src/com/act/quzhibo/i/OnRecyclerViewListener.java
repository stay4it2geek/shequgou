package com.act.quzhibo.i;

import android.view.View;

import cn.bmob.newim.bean.BmobIMMessage;

/**为RecycleView添加点击事件
 */
public interface OnRecyclerViewListener {
    void onItemClick(int position);
    boolean onItemLongClick(int position,View view);
}
