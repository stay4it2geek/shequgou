package com.act.quzhibo.adapter.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * @param <D>  data type
 * @param <VH> ViewHolder type
 */
public abstract class DowloadBaseRvAdapter<D, VH extends ViewHolder> extends
        RecyclerView.Adapter<VH> {

    protected final Context context;
    protected OnItemClickListener onItemClickListener;
    private List<D> data = new ArrayList<>();

    public DowloadBaseRvAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public D getData(int position) {
        return data.get(position);
    }

    public List<D> getListData() {
        return this.data;
    }

    public void setData(List<D> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * @param onItemClickListener
     */
    public void setOnItemClickListener(
            OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Item click listener.
     */
    public interface OnItemClickListener {

        void onItemClick(int position);
    }


}
