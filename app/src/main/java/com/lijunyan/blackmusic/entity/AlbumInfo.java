package com.lijunyan.blackmusic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class AlbumInfo implements Parcelable {
    private String name;
    private String singer;
    private int count;

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

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }
    @Override
    public int hashCode() {
        String code = name + singer + count;
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        AlbumInfo info = (AlbumInfo) (obj);
        return info.getName().equals(name) && info.getSinger().equals(singer) && info.getCount() == count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.singer);
        dest.writeInt(this.count);
    }

    public AlbumInfo() {
    }

    protected AlbumInfo(Parcel in) {
        this.name = in.readString();
        this.singer = in.readString();
        this.count = in.readInt();
    }

    public static final Parcelable.Creator<AlbumInfo> CREATOR = new Parcelable.Creator<AlbumInfo>() {
        @Override
        public AlbumInfo createFromParcel(Parcel source) {
            return new AlbumInfo(source);
        }

        @Override
        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }
    };
}
