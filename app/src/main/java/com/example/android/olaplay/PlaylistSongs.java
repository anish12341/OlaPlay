package com.example.android.olaplay;

/**
 * Created by anish on 12/20/2017.
 */

public class PlaylistSongs {
    private String mName;
    private int mId;
    public String mUrl;
    public PlaylistSongs(String name, int id){
        mName = name;
        mId = id;
    }


    public String getmName(){return mName;}

    public int getmId(){return mId;}

}
