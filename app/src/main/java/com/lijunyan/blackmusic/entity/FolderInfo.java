package com.lijunyan.blackmusic.entity;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class FolderInfo {

    private String name;
    private String path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        String code = name + path + count ;
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        FolderInfo info = (FolderInfo) (obj);
        return info.getName().equals(name) && info.getPath().equals(path) && info.getCount() == count ;
    }
}
