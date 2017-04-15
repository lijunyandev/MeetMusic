package com.lijunyan.blackmusic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lijunyan on 2017/3/2.
 */

public class PlayListInfo implements Parcelable {
    private int id;
    private String name;
    private int count;

    public PlayListInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
    public static final Creator<PlayListInfo> CREATOR = new Creator<PlayListInfo>() {
        @Override
        public PlayListInfo createFromParcel(Parcel source) {
            return new PlayListInfo(source);
        }

        @Override
        public PlayListInfo[] newArray(int size) {
            return new PlayListInfo[size];
        }
    };


    protected PlayListInfo(Parcel in) {
        this.id = in.readInt();
        this.count = in.readInt();
        this.name = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.count);
        dest.writeString(this.name);
    }
}
