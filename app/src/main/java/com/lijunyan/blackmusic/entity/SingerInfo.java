package com.lijunyan.blackmusic.entity;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class SingerInfo {

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
}
