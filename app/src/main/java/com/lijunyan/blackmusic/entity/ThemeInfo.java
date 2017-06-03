package com.lijunyan.blackmusic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lijunyan on 2017/5/30.
 */

public class ThemeInfo implements Parcelable {
    private String name;
    private int color;
    private int background;
    private boolean isSelect;


    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public ThemeInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.color);
        dest.writeInt(this.background);
        dest.writeByte(this.isSelect ? (byte) 1 : (byte) 0);
    }

    protected ThemeInfo(Parcel in) {
        this.name = in.readString();
        this.color = in.readInt();
        this.background = in.readInt();
        this.isSelect = in.readByte() != 0;
    }

    public static final Creator<ThemeInfo> CREATOR = new Creator<ThemeInfo>() {
        @Override
        public ThemeInfo createFromParcel(Parcel source) {
            return new ThemeInfo(source);
        }

        @Override
        public ThemeInfo[] newArray(int size) {
            return new ThemeInfo[size];
        }
    };
}
