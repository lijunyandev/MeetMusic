package com.lijunyan.blackmusic.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.entity.ThemeInfo;
import com.lijunyan.blackmusic.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;

public class ThemeActivity extends BaseActivity {
    public static int THEME_SIZE = 11;
    private String[] themeType = {"哔哩粉", "知乎蓝", "酷安绿","网易红","藤萝紫","碧海蓝","樱草绿","咖啡棕","柠檬橙","星空灰","夜间模式"};
    private int[] colors = {R.color.biliPink, R.color.zhihuBlue, R.color.kuanGreen, R.color.cloudRed,
            R.color.tengluoPurple, R.color.seaBlue, R.color.grassGreen, R.color.coffeeBrown,
            R.color.lemonOrange,R.color.startSkyGray,R.color.nightActionbar};

    private static final String TAG = "ThemeActivity";
    private RecyclerView recyclerView;
    private ThemeAdapter adapter;
    private Toolbar toolbar;
    private int selectTheme = 0;
    private List<ThemeInfo> themeInfoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        selectTheme = MyMusicUtil.getTheme(ThemeActivity.this);
        toolbar = (Toolbar) findViewById(R.id.theme_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        init();
    }

    private void init(){
        for (int i = 0 ;i < themeType.length;i++){
            ThemeInfo info = new ThemeInfo();
            info.setName(themeType[i]);
            info.setColor(colors[i]);
            info.setSelect((selectTheme == i) ? true : false);
            if (i == themeType.length-1){
                info.setBackground(R.color.nightBg);
            }else {
                info.setBackground(R.color.colorWhite);
            }
            themeInfoList.add(info);
        }
        recyclerView = (RecyclerView)findViewById(R.id.theme_rv);
        adapter = new ThemeAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ThemeActivity.this,HomeActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
//                overridePendingTransition(R.anim.in_from_left_anim,R.anim.out_to_right_anim);
                break;
        }
        return true;
    }

    private class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder>{

        public ThemeAdapter() {
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout relativeLayout;
            ImageView circleIv;
            TextView nameTv;
            Button selectBtn;

            public ViewHolder(View itemView) {
                super(itemView);
                this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.theme_item_rl);
                this.circleIv = (ImageView) itemView.findViewById(R.id.theme_iv);
                this.nameTv = (TextView) itemView.findViewById(R.id.theme_name_tv);
                this.selectBtn = (Button) itemView.findViewById(R.id.theme_select_tv);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ThemeActivity.this).inflate(R.layout.change_theme_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final ThemeInfo themeInfo = themeInfoList.get(position);
            if (selectTheme == THEME_SIZE-1){
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_night);
                holder.selectBtn.setBackgroundResource(R.drawable.shape_theme_btn_night);
            }else {
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_day);
                holder.selectBtn.setBackgroundResource(R.drawable.shape_theme_btn_day);
            }
            holder.selectBtn.setPadding(0,0,0,0);
            if (themeInfo.isSelect()){
                holder.circleIv.setImageResource(R.drawable.tick);
                holder.selectBtn.setText("使用中");
                holder.selectBtn.setTextColor(getResources().getColor(themeInfo.getColor()));
            }else {
                holder.circleIv.setImageBitmap(null);
                holder.selectBtn.setText("使用");
                holder.selectBtn.setTextColor(getResources().getColor(R.color.grey500));
            }
            holder.circleIv.setBackgroundResource(themeInfo.getColor());
            holder.nameTv.setTextColor(getResources().getColor(themeInfo.getColor()));
            holder.nameTv.setText(themeInfo.getName());
            holder.selectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshTheme(themeInfo,position);
                }
            });
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshTheme(themeInfo,position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return themeInfoList.size();
        }
    }

    private void refreshTheme(ThemeInfo themeInfo,int position){
        if (position == (THEME_SIZE-1)){
            MyMusicUtil.setNightMode(ThemeActivity.this,true);
        }else if(MyMusicUtil.getNightMode(ThemeActivity.this)){
            MyMusicUtil.setNightMode(ThemeActivity.this,false);
        }
        selectTheme = position;
        MyMusicUtil.setTheme(ThemeActivity.this,position);
        toolbar.setBackgroundColor(getResources().getColor(themeInfo.getColor()));
        recyclerView.setBackgroundColor(getResources().getColor(themeInfo.getBackground()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(themeInfo.getColor()));
        }
        for (ThemeInfo info : themeInfoList){
            if (info.getName().equals(themeInfo.getName())){
                info.setSelect(true);
            }else {
                info.setSelect(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ThemeActivity.this.finish();
    }

}
