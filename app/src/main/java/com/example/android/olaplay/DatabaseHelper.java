package com.example.android.olaplay;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by anish on 12/17/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Songs.db";
    public static final String TABLE_NAME = "song_table";
    public static final String TABLE_NAME2= "download_table";
    public static final String TABLE_NAME3= "starred_table";
    public static final String TABLE_NAME4= "history_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "song";
    public static final String COL_3 = "url";
    public static final String COL_4 = "artists";
    public static final String COL_5 = "image";
    public static final String COL_6 = "songId";
    public static final String COL_7 = "filepath";
    public static final String COL_8 = "state";
    public static final String COL_9 = "time";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,song TEXT,url TEXT,artists TEXT,image BLOB)");
        db.execSQL("create table " + TABLE_NAME2 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,song TEXT,songId INTEGER,filepath TEXT)");
        db.execSQL("create table " + TABLE_NAME3 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,song TEXT ,songId INTEGER, url )");
        db.execSQL("create table " + TABLE_NAME4 +" (state TEXT ,songId INTEGER,time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("onupgrade","kk");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
        onCreate(db);
    }
    public boolean insertDataSong(String song,String url,String artists,byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,song);
        contentValues.put(COL_3,url);
        contentValues.put(COL_4,artists);
        contentValues.put(COL_5,image);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean insertDataDownload(String song,int songId,String filePath){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,song);
        contentValues.put(COL_6,songId);
        contentValues.put(COL_7,filePath);
        long result = db.insert(TABLE_NAME2,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }
    public boolean insertDataStarred(String song,int songId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,song);
        contentValues.put(COL_6,songId);
        long result = db.insert(TABLE_NAME3,null ,contentValues);
        if(result == -1)
            return false;
        else
            Log.d("starred","added");
        return true;
    }
    public boolean insertDataHistory(String state,int songId,String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_8,state);
        contentValues.put(COL_6,songId);
        contentValues.put(COL_9,time);
        long result = db.insert(TABLE_NAME4,null ,contentValues);
        if(result == -1)
            return false;
        else
            Log.d("starred","added");
        return true;
    }


    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }
    public Cursor getAllDataDownload() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME2,null);
        return res;
    }

    public Cursor getAllDataStarred() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME3,null);
        return res;
    }

    public Cursor getAllDataHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME4,null);
        return res;
    }

    public Cursor getSelectedData(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME+" where ID = '"+id+"'",null);
        return res;
    }

    public  Cursor getSelectedDataPlaylist(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME3+" where ID = '"+id+"'",null);
        return res;
    }

    public  Cursor getSelectedDataDownload(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME2+" where ID = '"+id+"'",null);
        return res;
    }

    public  Cursor getPlaylistID(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID from "+TABLE_NAME3+" where songId = '"+id+"'",null);
        return res;
    }

    public  Cursor getDownloadID(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID from "+TABLE_NAME2+" where songId = '"+id+"'",null);
        return res;
    }


    public Cursor getFilepath(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select filepath from "+TABLE_NAME2+" where ID = '"+id+"'",null);
        return res;

    }

    public Cursor getId(String songName){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID from "+TABLE_NAME+" where song= '"+songName+"'",null);
        return res;
    }
    public Cursor getDownloadId(String songName){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME2+" where song= '"+songName+"'",null);
        return res;
    }

    public Cursor getStarredId(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME3+" where song = '"+name+"'",null);
        return res;
    }

    public void deleteStarred(String name){
        Log.d("starred","deleted");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME3 + " where song = '"+name+"'");
    }

    public void deleteDownload(String name){
        Log.d("starred","deleted");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME2 + " where song = '"+name+"'");
    }
    public void deleteHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME4);
    }

}
