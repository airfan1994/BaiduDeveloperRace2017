/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.unit;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HintAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ItemClickListener mItemClickListener;
    private Context mContext;
    private List<String> mList = new ArrayList<>();

    public HintAdapter(Context context) {
        this.mContext = context;
    }

    public void addItems(List<String> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.hint_item_view, null);
        // 将全局的监听传递给holder
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TextView textView = (TextView) holder.itemView.findViewById(R.id.hint_tv);
        textView.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemClickListener mListener;

        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            // 将全局的监听赋值给接口
            this.mListener = itemClickListener;
            itemView.setOnClickListener(this);
        }

        /**
         * 实现OnClickListener接口重写的方法
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }
    }

    /**
     * 创建一个回调接口
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param itemClickListener
     */
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }
}
