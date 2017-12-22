package com.example.android.olaplay;

import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by anish on 12/17/2017.
 * A DATA PROVIDER CLASS FOR SongListAdapter,PlaylistSongAdapter and DownloadSongListAdapter
 */

public class Songs {
    private String mName,mArtists,mFilepath;
    public String mUrl;
    private byte[] mImage;
    private int mId;
    public Songs(String name, String url, String artists,byte[] image){
        mName = name;
        mUrl = url;
        mArtists = artists;
        mImage = image;
    }

    public Songs(String name, int id){
        mName = name;
        mId = id;
    }

    public Songs(String name, int id, String filepath){
        mName = name;
        mId = id;
        mFilepath = filepath;
    }



    public String getmFilepath(){return mFilepath;}

    public int getmId(){return mId;}

    public String getmName(){return mName;}

    public String getmUrl(){return mUrl;}

    public String getmArtists(){return mArtists;}

    public byte[] getmImage(){return mImage;}
}
