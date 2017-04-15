package com.lijunyan.blackmusic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lijunyan.blackmusic.entity.AlbumInfo;
import com.lijunyan.blackmusic.entity.FolderInfo;
import com.lijunyan.blackmusic.entity.MusicInfo;
import com.lijunyan.blackmusic.entity.PlayListInfo;
import com.lijunyan.blackmusic.entity.SingerInfo;
import com.lijunyan.blackmusic.util.ChineseToEnglish;
import com.lijunyan.blackmusic.util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.lijunyan.blackmusic.database.DatabaseHelper.ID_COLUMN;
import static com.lijunyan.blackmusic.database.DatabaseHelper.MUSIC_ID_COLUMN;

public class DBManager {

    private static final String TAG = DBManager.class.getName();
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private static DBManager instance = null;


    /* 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,mFactory);
     * 需要一个context参数 ,所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
     */
    public DBManager(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    // 获取音乐表歌曲数量
    public int getMusicCount(int table) {
        int musicCount = 0;
        Cursor cursor = null;
        switch (table) {
            case Constant.LIST_ALLMUSIC:
                cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null);
                break;
            case Constant.LIST_LASTPLAY:
                cursor = db.query(DatabaseHelper.LAST_PLAY_TABLE, null, null, null, null, null, null);
                break;
            case Constant.LIST_MYLOVE:
                cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, DatabaseHelper.LOVE_COLUMN + " = ? ", new String[]{"" + 1}, null, null, null);
                break;
            case Constant.LIST_MYPLAY:
                cursor = db.query(DatabaseHelper.PLAY_LIST_TABLE, null, null, null, null, null, null);
                break;
        }
        if (cursor.moveToFirst()) {
            musicCount = cursor.getCount();
        }
        if (cursor != null) {
            cursor.close();
        }
        return musicCount;
    }

    public List<MusicInfo> getAllMusicFromMusicTable() {
        Log.d(TAG, "getAllMusicFromMusicTable: ");
        List<MusicInfo> musicInfoList = null;
        Cursor cursor = null;
        db.beginTransaction();
        try {
            cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null);
            musicInfoList = cursorToMusicList(cursor);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (cursor!=null){
                cursor.close();
            }
        }
        return musicInfoList;
    }

    public MusicInfo getSingleMusicFromMusicTable(int id) {
        Log.i(TAG, "getSingleMusicFromMusicTable: ");
        List<MusicInfo> musicInfoList = null;
        MusicInfo musicInfo = null;
        Cursor cursor = null;
        db.beginTransaction();
        try {
            cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null);
            musicInfoList = cursorToMusicList(cursor);
            musicInfo = musicInfoList.get(0);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return musicInfo;
    }

//    public List<Integer> getAllMusicFromLastPlayTable() {
//        Log.d(TAG, "getAllMusicFromLastPlayTable: ");
//        List<Integer> lastList = new ArrayList<>();
//        Cursor cursor = null;
//        try {
//            cursor = db.query(DatabaseHelper.LAST_PLAY_TABLE, null, null, null, null, null, null);
//            Log.e(TAG, "getAllMusicFromLastPlayTable: cursor.getCount() = " + cursor.getCount());
//            while (cursor.moveToNext()) {
//                lastList.add(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID_COLUMN)));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return lastList;
//    }

    public List<MusicInfo> getAllMusicFromTable(int playList) {
        Log.d(TAG, "getAllMusicFromTable: ");
        List<Integer> idList = getMusicList(playList);
        List<MusicInfo> musicList = new ArrayList<>();
        for (int id : idList) {
            musicList.add(getSingleMusicFromMusicTable(id));
        }
//        Cursor cursor = null;
//        try {
//            switch (list){
//                case Constant.LIST_ALLMUSIC:
//                    cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null);
//                    musicList = cursorToMusicList(cursor);
//                    break;
//                case Constant.LIST_LASTPLAY:
//                    List<Integer> idList = new ArrayList<>();
//                    cursor = db.query(DatabaseHelper.LAST_PLAY_TABLE, null, null, null, null, null, null);
//                    while (cursor.moveToNext()) {
//                        idList.add(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID_COLUMN)));
//                    }
//                    for (int id : idList){
//                        musicList.add(getSingleMusicFromMusicTable(id));
//                    }
//                    break;
//                case Constant.LIST_MYLOVE:
//                    cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, DatabaseHelper.LOVE_COLUMN + " = ? ",
//                            new String[]{""+1}, null, null, null);
//                    musicList = cursorToMusicList(cursor);
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            if (cursor != null){
//                cursor.close();
//            }
//        }
        return musicList;
    }

    public List<PlayListInfo> getMyPlayList() {
        List<PlayListInfo> playListInfos = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.PLAY_LIST_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            PlayListInfo playListInfo = new PlayListInfo();
            int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(ID_COLUMN)));
            playListInfo.setId(id);
            playListInfo.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COLUMN)));
            Cursor cursorCount = db.query(DatabaseHelper.PLAY_LISY_MUSIC_TABLE,null, ID_COLUMN + " = ?", new String[]{"" + id}, null,null,null);
            playListInfo.setCount(cursorCount.getCount());
            playListInfos.add(playListInfo);
        }
        return playListInfos;
    }


    public void createPlaylist(String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NAME_COLUMN, name);
        db.insert(DatabaseHelper.PLAY_LIST_TABLE, null, values);
    }

    public List<MusicInfo> getMusicListBySinger(String singer){
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();
        try{
            String sql = "select * from "+DatabaseHelper.MUSIC_TABLE+" where "+ DatabaseHelper.SINGER_COLUMN+" = ? ";
            cursor = db.rawQuery(sql,new String[]{singer});
            musicInfoList = cursorToMusicList(cursor);
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
            if (cursor !=null)
                cursor.close();
        }
        return musicInfoList;
    }

    public List<MusicInfo> getMusicListByAlbum(String album){
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();
        try{
            String sql = "select * from "+DatabaseHelper.MUSIC_TABLE+" where "+ DatabaseHelper.ALBUM_COLUMN+" = ? ";
            cursor = db.rawQuery(sql,new String[]{album});
            musicInfoList = cursorToMusicList(cursor);
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
            if (cursor !=null)
                cursor.close();
        }
        return musicInfoList;
    }

    public List<MusicInfo> getMusicListByFolder(String folder){
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();
        try{
            String sql = "select * from "+DatabaseHelper.MUSIC_TABLE+" where "+ DatabaseHelper.PARENT_PATH_COLUMN+" = ? ";
            cursor = db.rawQuery(sql,new String[]{folder});
            musicInfoList = cursorToMusicList(cursor);
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
            if (cursor !=null)
                cursor.close();
        }
        return musicInfoList;
    }

    public ArrayList<Integer> getMusicIdListByPlaylist(int playlistId){
        Cursor cursor = null;
        db.beginTransaction();
        ArrayList<Integer> list = new ArrayList<Integer>();
        try{
            String sql = "select * from "+DatabaseHelper.PLAY_LISY_MUSIC_TABLE+" where "+ ID_COLUMN+" = ? ";
            cursor = db.rawQuery(sql,new String[]{""+playlistId});
            while (cursor.moveToNext()) {
                int musicId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MUSIC_ID_COLUMN));
                list.add(musicId);
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
            if (cursor !=null)
                cursor.close();
        }
        return list;
    }

    public List<MusicInfo> getMusicListByPlaylist(int playlistId){
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        int id;
        db.beginTransaction();
        try{
            String sql = "select * from "+DatabaseHelper.PLAY_LISY_MUSIC_TABLE+" where "+ ID_COLUMN+" = ? ";
            cursor = db.rawQuery(sql,new String[]{""+playlistId});
            while (cursor.moveToNext()){
                MusicInfo musicInfo = new MusicInfo();
                id =  cursor.getInt(cursor.getColumnIndex(MUSIC_ID_COLUMN));
                musicInfo = getSingleMusicFromMusicTable(id);
                musicInfoList.add(musicInfo);
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
            if (cursor !=null)
                cursor.close();
        }
        return musicInfoList;
    }


    public List<SingerInfo> getSingerList() {
        List<SingerInfo> singerInfos = new ArrayList<>();
        Cursor cursor = null;
        Cursor cursorCount = null;
        db.beginTransaction();
        try {
            String sql = "select * from " + DatabaseHelper.MUSIC_TABLE;
            String sqlC = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.SINGER_COLUMN +
                    " = ? ";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                SingerInfo singerInfo = new SingerInfo();
                String singer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGER_COLUMN));
                cursorCount = db.rawQuery(sqlC, new String[]{singer});
                singerInfo.setName(singer);
                singerInfo.setCount(cursorCount.getCount());
                singerInfos.add(singerInfo);
            }
            //去重复
            Set set = new HashSet(singerInfos);
            singerInfos = new ArrayList<>(set);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
            if (cursorCount != null) {
                cursorCount.close();
            }
        }
        return singerInfos;
    }


    public List<AlbumInfo> getAlbumList() {
        List<AlbumInfo> albumInfos = new ArrayList<>();
        Cursor cursor = null;
        Cursor cursorCount = null;
        db.beginTransaction();
        try {
            String sql = "select * from " + DatabaseHelper.MUSIC_TABLE;
            String sqlC = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.ALBUM_COLUMN +
                    " = ? ";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                AlbumInfo albumInfo = new AlbumInfo();
                String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_COLUMN));
                String singer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGER_COLUMN));
                cursorCount = db.rawQuery(sqlC, new String[]{album});
                albumInfo.setName(album);
                albumInfo.setSinger(singer);
                albumInfo.setCount(cursorCount.getCount());
                albumInfos.add(albumInfo);
            }
            //去重复
            Set set = new HashSet(albumInfos);
            albumInfos = new ArrayList<>(set);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
            if (cursorCount != null) {
                cursorCount.close();
            }
        }
        return albumInfos;
    }


    public List<FolderInfo> getFolderList() {
        List<FolderInfo> folderInfos = new ArrayList<>();
        Cursor cursor = null;
        Cursor cursorCount = null;
        db.beginTransaction();
        try {
            String sql = "select * from " + DatabaseHelper.MUSIC_TABLE;
            String sqlC = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.PARENT_PATH_COLUMN +
                    " = ? ";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                FolderInfo folderInfo = new FolderInfo();
                String parentPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PARENT_PATH_COLUMN));
                cursorCount = db.rawQuery(sqlC, new String[]{parentPath});
                File file = new File(parentPath);
                folderInfo.setName(file.getName());
                folderInfo.setPath(parentPath);
                folderInfo.setCount(cursorCount.getCount());
                folderInfos.add(folderInfo);
            }

            //去重复
            Set set = new HashSet(folderInfos);
            folderInfos = new ArrayList<>(set);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
            if (cursorCount != null) {
                cursorCount.close();
            }
        }
        return folderInfos;
    }

    public void insertMusicListToMusicTable(List<MusicInfo> musicInfoList) {
        Log.d(TAG, "insertMusicListToMusicTable: ");
        for (MusicInfo musicInfo : musicInfoList) {
            insertMusicInfoToMusicTable(musicInfo);
        }
    }


    //添加歌曲到音乐表
    public void insertMusicInfoToMusicTable(MusicInfo musicInfo) {
        ContentValues values;
        Cursor cursor = null;
        int id = 1;
        try {
            values = musicInfoToContentValues(musicInfo);
            String sql = "select max(id) from " + DatabaseHelper.MUSIC_TABLE + ";";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                //设置新添加的ID为最大ID+1
                id = cursor.getInt(0) + 1;
            }
            values.put(ID_COLUMN, id);
//			values.put("mylove",0);
            db.insert(DatabaseHelper.MUSIC_TABLE, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //添加音乐到歌单
    public void addToPlaylist(int playlistId,int musicId){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ID_COLUMN,playlistId);
        values.put(DatabaseHelper.MUSIC_ID_COLUMN,musicId);
        db.insert(DatabaseHelper.PLAY_LISY_MUSIC_TABLE,null,values);
    }
    public void updateAllMusic(List<MusicInfo> musicInfoList) {
        db.beginTransaction();
        try {
            deleteAllTable();
            insertMusicListToMusicTable(musicInfoList);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    //删除数据库中所有的表
    public void deleteAllTable() {
        db.delete(DatabaseHelper.MUSIC_TABLE, null, null);
    }

    //删除指定音乐
    public void deleteMusic(int id) {
        db.delete(DatabaseHelper.MUSIC_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
        db.delete(DatabaseHelper.LAST_PLAY_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
    }

    public void deletePlaylist(int id) {
        db.delete(DatabaseHelper.PLAY_LIST_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
    }

    //根据从哪个activity中发出的移除歌曲指令判断
    public void removeMusic(int id, int witchActivity) {
        switch (witchActivity) {
            case Constant.ACTIVITY_LOCAL:
                db.delete(DatabaseHelper.MUSIC_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
                break;
            case Constant.ACTIVITY_RECENTPLAY:
                db.delete(DatabaseHelper.LAST_PLAY_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
                break;
            case Constant.ACTIVITY_MYLOVE:
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.LOVE_COLUMN, 0);
                db.update(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ? ", new String[]{"" + id});
                break;
        }
    }

    //根据从哪个activity中发出的移除歌曲指令判断
    public int removeMusicFromPlaylist(int musicId, int playlistId) {
        int ret = 0;
        try {
            ret = db.delete(DatabaseHelper.PLAY_LISY_MUSIC_TABLE, ID_COLUMN + " = ? and " + DatabaseHelper.MUSIC_ID_COLUMN
                    + " = ? ", new String[]{"" + playlistId, musicId + ""});
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    // 获取歌曲路径
    public String getMusicPath(int id) {
        Log.d(TAG, "getMusicPath id = " + id);
        if (id == -1) {
            return null;
        }
        String path = null;
        Cursor cursor = null;
        setLastPlay(id);        //每次播放一首新歌前都需要获取歌曲路径，所以可以在此设置最近播放
        try {
            cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null);
            Log.i(TAG, "getCount: " + cursor.getCount());
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PATH_COLUMN));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    //获取音乐表中的第一首音乐的ID
    public int getFirstId(int listNumber) {
        Cursor cursor = null;
        int id = -1;
        try {
            switch (listNumber) {
                case Constant.LIST_ALLMUSIC:
                    cursor = db.rawQuery("select min(id) from " + DatabaseHelper.MUSIC_TABLE, null);
                    break;
                default:
                    Log.i(TAG, "getFirstId: default");
                    break;
            }
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
                Log.d(TAG, "getFirstId min id = " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id;
    }


    // 获取下一首歌曲(id)
    public int getNextMusic(ArrayList<Integer> musicList, int id, int playMode) {
        if (id == -1) {
            return -1;
        }
        //找到当前id在列表的第几个位置（i+1）
        int index = musicList.indexOf(id);
        if (index == -1) {
            return -1;
        }
        // 如果当前是最后一首
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                if ((index + 1) == musicList.size()) {
                    id = musicList.get(0);
                } else {
                    ++index;
                    id = musicList.get(index);
                }
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                break;
            case Constant.PLAYMODE_RANDOM:
                id = getRandomMusic(musicList, id);
                break;
        }
        return id;
    }

    // 获取上一首歌曲(id)
    public int getPreMusic(ArrayList<Integer> musicList, int id, int playMode) {
        if (id == -1) {
            return -1;
        }
        //找到当前id在列表的第几个位置（i+1）
        int index = musicList.indexOf(id);
        if (index == -1) {
            return -1;
        }
        // 如果当前是第一首则返回最后一首
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                if (index == 0) {
                    id = musicList.get(musicList.size());
                } else {
                    --index;
                    id = musicList.get(index);
                }
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                break;
            case Constant.PLAYMODE_RANDOM:
                id = getRandomMusic(musicList, id);
                break;
        }
        return id;
    }

    // 获取歌单列表
    public ArrayList<Integer> getMusicList(int playList) {
        Cursor cursor = null;
        ArrayList<Integer> list = new ArrayList<Integer>();
        int musicId;
        switch (playList) {
            case Constant.LIST_ALLMUSIC:
                cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null);
                break;
            case Constant.LIST_LASTPLAY:
                cursor = db.query(DatabaseHelper.LAST_PLAY_TABLE, null, null, null, null, null, null);
                break;
            case Constant.LIST_MYLOVE:
                cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, DatabaseHelper.LOVE_COLUMN + " = ?", new String[]{"" + 1}, null, null, null);
                break;
            default:
                Log.e(TAG, "getMusicList default");
                break;
        }
        while (cursor.moveToNext()) {
            musicId = cursor.getInt(cursor.getColumnIndex("id"));
            list.add(musicId);
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    // 获取歌曲详细信息
    public ArrayList<String> getMusicInfo(int id) {
        if (id == -1) {
            return null;
        }
        Cursor cursor = null;
        ArrayList<String> musicInfo = new ArrayList<String>();
        cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                musicInfo.add(i, cursor.getString(i));
            }
        } else {
            musicInfo.add("0");
            musicInfo.add("音乐盒");
            musicInfo.add("好音质");
            musicInfo.add("0");
            musicInfo.add("0");
            musicInfo.add("0");
            musicInfo.add("0");
            musicInfo.add("0");
        }
        if (cursor != null) {
            cursor.close();
        }
        return musicInfo;
    }

    //获取随机歌曲
    public int getRandomMusic(ArrayList<Integer> list, int id) {
        int musicId;
        if (id == -1) {
            return -1;
        }
        if (list.isEmpty()) {
            return -1;
        }
        if (list.size() == 1) {
            return id;
        }
        do {
            int count = (int) (Math.random() * list.size());
            musicId = count;
        } while (musicId == id);

        return musicId;

    }

    //保留最近的20首
    public void setLastPlay(int id) {
        Log.i(TAG, "setLastPlay: id = " + id);
        if (id == -1) {
            return;
        }
        ContentValues values = new ContentValues();
        ArrayList<Integer> lastList = new ArrayList<Integer>();
        Cursor cursor = null;
        lastList.add(id);
        try {
            cursor = db.rawQuery("select id from " + DatabaseHelper.LAST_PLAY_TABLE, null);
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) != id) {
                    lastList.add(cursor.getInt(0));
                }
            }
            db.delete(DatabaseHelper.LAST_PLAY_TABLE, null, null);
            if (lastList.size() < 20) {
                for (int i = 0; i < lastList.size(); i++) {
                    values.put(ID_COLUMN, lastList.get(i));
                    db.insert(DatabaseHelper.LAST_PLAY_TABLE, null, values);
                }
            } else {
                for (int i = 0; i < 20; i++) {
                    values.put(ID_COLUMN, lastList.get(i));
                    db.insert(DatabaseHelper.LAST_PLAY_TABLE, null, values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void setMyLove(int id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.LOVE_COLUMN, 1);
        db.update(DatabaseHelper.MUSIC_TABLE, values, ID_COLUMN + " = ? ", new String[]{"" + id});
    }

    //把MusicInfo对象转为ContentValues对象
    public ContentValues musicInfoToContentValues(MusicInfo musicInfo) {
        ContentValues values = new ContentValues();
        try {
//            values.put(DatabaseHelper.ID_COLUMN, musicInfo.getId());
            values.put(DatabaseHelper.NAME_COLUMN, musicInfo.getName());
            values.put(DatabaseHelper.SINGER_COLUMN, musicInfo.getSinger());
            values.put(DatabaseHelper.ALBUM_COLUMN, musicInfo.getAlbum());
            values.put(DatabaseHelper.DURATION_COLUMN, musicInfo.getDuration());
            values.put(DatabaseHelper.PATH_COLUMN, musicInfo.getPath());
            values.put(DatabaseHelper.PARENT_PATH_COLUMN, musicInfo.getParentPath());
            values.put(DatabaseHelper.LOVE_COLUMN, musicInfo.getLove());
            values.put(DatabaseHelper.FIRST_LETTER_COLUMN, "" + ChineseToEnglish.StringToPinyinSpecial(musicInfo.getName()).toUpperCase().charAt(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    //把Cursor对象转为List<MusicInfo>对象
    public List<MusicInfo> cursorToMusicList(Cursor cursor) {
        List<MusicInfo> list = null;
        try {
            if (cursor != null) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(ID_COLUMN));
                    String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COLUMN));
                    String singer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGER_COLUMN));
                    String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_COLUMN));
                    String duration = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DURATION_COLUMN));
                    String path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PATH_COLUMN));
                    String parentPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PARENT_PATH_COLUMN));
                    int love = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.LOVE_COLUMN));
                    String firstLetter = cursor.getString(cursor.getColumnIndex(DatabaseHelper.FIRST_LETTER_COLUMN));

                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setId(id);
                    musicInfo.setName(name);
                    musicInfo.setSinger(singer);
                    musicInfo.setAlbum(album);
                    musicInfo.setPath(path);
                    musicInfo.setParentPath(parentPath);
                    musicInfo.setLove(love);
                    musicInfo.setDuration(duration);
                    musicInfo.setFirstLetter(firstLetter);
                    list.add(musicInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


}
