package com.lijunyan.blackmusic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class SingerInfo implements Parcelable {

    private String name;
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

    @Override
    public int hashCode() {
        String code = name + count;
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        SingerInfo info = (SingerInfo) (obj);
        return info.getName().equals(name) && info.getCount() == count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.count);
    }

    public SingerInfo() {
    }

    protected SingerInfo(Parcel in) {
        this.name = in.readString();
        this.count = in.readInt();
    }

    public static final Parcelable.Creator<SingerInfo> CREATOR = new Parcelable.Creator<SingerInfo>() {
        @Override
        public SingerInfo createFromParcel(Parcel source) {
            return new SingerInfo(source);
        }

        @Override
        public SingerInfo[] newArray(int size) {
            return new SingerInfo[size];
        }
    };
}
