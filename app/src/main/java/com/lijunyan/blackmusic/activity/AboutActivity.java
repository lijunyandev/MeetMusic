package com.lijunyan.blackmusic.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lijunyan.blackmusic.R;

public class AboutActivity extends BaseActivity{

    private Toolbar toolbar;
    private TextView versionTv;
    private LinearLayout startLl;
    private LinearLayout scoreLl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
    }
    private void init(){
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        versionTv = (TextView) findViewById(R.id.about_version);
        startLl = (LinearLayout) findViewById(R.id.about_start_ll);
        scoreLl = (LinearLayout) findViewById(R.id.about_score_ll);
        versionTv.setText(getVersion());

        startLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/lijunyandev/MeetMusic");
            }
        });
        scoreLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("http://www.coolapk.com/apk/com.lijunyan.blackmusic");
            }
        });
    }


    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return this.getString(R.string.version_name) + version;
        } catch (Exception e) {
            e.printStackTrace();
            return this.getString(R.string.can_not_find_version_name);
        }
    }
}
