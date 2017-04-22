package com.lijunyan.blackmusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.activity.ModelActivity;
import com.lijunyan.blackmusic.adapter.FolderAdapter;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.FolderInfo;

import java.util.List;

/**
 * Created by lijunyan on 2017/3/9.
 */

public class FolderFragment extends Fragment {
    
    private static final String TAG = "FolderFragment";
    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private List<FolderInfo> folderInfoList;
    private DBManager dbManager;
    private Context mContext;

    public FolderFragment() {
        dbManager = DBManager.getInstance(getContext());
        folderInfoList = dbManager.getFolderList();
        Log.e(TAG, "FolderFragment: folderInfoList.size() ="+ folderInfoList.size());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer,container,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.singer_recycler_view);
        adapter = new FolderAdapter(getContext(),folderInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onDeleteMenuClick(View content, int position) {

            }

            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext,ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE,folderInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE,ModelActivity.FOLDER_TYPE);
                intent.putExtra(ModelActivity.KEY_PATH,folderInfoList.get(position).getPath());
                mContext.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        folderInfoList.clear();
        folderInfoList.addAll(dbManager.getFolderList());
        adapter.notifyDataSetChanged();
    }
}
