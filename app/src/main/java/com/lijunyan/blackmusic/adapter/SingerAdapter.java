package com.lijunyan.blackmusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.SingerInfo;

import java.util.List;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class SingerAdapter extends RecyclerView.Adapter<SingerAdapter.ViewHolder>{

    private static final String TAG = SingerAdapter.class.getName();
    private List<SingerInfo> singerInfoList;
    private Context context;
    private DBManager dbManager;
    private OnItemClickListener onItemClickListener;

    public SingerAdapter(Context context, List<SingerInfo> singerInfoList) {
        this.context = context;
        this.singerInfoList = singerInfoList;
        this.dbManager = DBManager.getInstance(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View swipeContent;
        LinearLayout contentLl;
        ImageView singerIv;
        TextView singelName;
        TextView count;
//        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.model_swipemenu_layout);
            this.contentLl = (LinearLayout) itemView.findViewById(R.id.model_music_item_ll);
            this.singerIv = (ImageView) itemView.findViewById(R.id.model_head_iv);
            this.singelName = (TextView) itemView.findViewById(R.id.model_item_name);
            this.count = (TextView) itemView.findViewById(R.id.model_music_count);
//            this.deleteBtn = (Button) itemView.findViewById(R.id.model_swip_delete_menu_btn);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d("aaaa", "onBindViewHolder: position = "+position);
        SingerInfo singer = singerInfoList.get(position);
        holder.singelName.setText(singer.getName());
        holder.singerIv.setImageResource(R.drawable.singer);
        holder.count.setText(singer.getCount()+"首");
        holder.contentLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onContentClick(holder.contentLl,position);
            }
        });

//        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemClickListener.onDeleteMenuClick(holder.swipeContent,position);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return singerInfoList.size();
    }

    public void update(List<SingerInfo> singerInfoList) {
        this.singerInfoList.clear();
        this.singerInfoList.addAll(singerInfoList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onDeleteMenuClick(View content,int position);
        void onContentClick(View content,int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }
}
