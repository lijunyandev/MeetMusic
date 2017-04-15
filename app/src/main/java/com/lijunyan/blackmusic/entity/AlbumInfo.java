package com.lijunyan.blackmusic.entity;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class AlbumInfo {
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
}
