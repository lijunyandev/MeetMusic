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
import com.lijunyan.blackmusic.adapter.AlbumAdapter;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.AlbumInfo;

import java.util.List;

/**
 * Created by lijunyan on 2017/3/9.
 */

public class AlbumFragment extends Fragment {

    private static final String TAG = "AlbumFragment";
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private List<AlbumInfo> albumInfoList;
    private DBManager dbManager;
    private Context mContext;

    public AlbumFragment() {
        dbManager = DBManager.getInstance(getContext());
        albumInfoList = dbManager.getAlbumList();
        Log.e(TAG, "AlbumFragment: albumInfoList.size() ="+ albumInfoList.size());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void onResume() {
        super.onResume();
        albumInfoList.clear();
        albumInfoList.addAll(dbManager.getAlbumList());
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer,container,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.singer_recycler_view);
        adapter = new AlbumAdapter(getContext(),albumInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onDeleteMenuClick(View content, int position) {

            }

            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext,ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE,albumInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE,ModelActivity.ALBUM_TYPE);
                mContext.startActivity(intent);
            }
        });
        return view;
    }
}
