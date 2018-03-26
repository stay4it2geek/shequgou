package com.act.quzhibo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.InterestPlates;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class InterestPlatesListAdapter extends RecyclerView.Adapter<InterestPlatesListAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<InterestPlates> datas;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position, String plateId);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public InterestPlatesListAdapter(Context context, ArrayList<InterestPlates> datas) {
        mContext = context;
        this.datas = datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_interest_plates, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            Glide.with(mContext).load(datas.get(position).imgUrl).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.plateImg);//加载网络图片
            holder.pAbstract.setText(datas.get(position).pAbstract);
            holder.pName.setText(datas.get(position).pName);
            holder.plateLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position, datas.get(position).pid);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView plateImg;
        private TextView pAbstract;
        private TextView pName;
        private RelativeLayout plateLayout;

        public MyViewHolder(View view) {
            super(view);
            plateImg = (ImageView) view.findViewById(R.id.plateImg);
            pAbstract = (TextView) view.findViewById(R.id.pAbstract);
            plateLayout = (RelativeLayout) view.findViewById(R.id.plateLayout);
            pName = (TextView) view.findViewById(R.id.pName);

        }
    }
}
