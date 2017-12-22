package com.example.android.olaplay;

import static android.R.attr.id;

/**
 * Created by anish on 12/21/2017.
 * ACTS AS A DATA PROVIDER CLASS FOR HISTORY
 */

public class History {
    private String mState,mtime;
    private int mId;
    public History(String state, int id, String time){
        mState=state;
        mId = id;
        mtime = time;
    }


    public String getmState(){return mState;}

    public int getmId(){return mId;}

    public String getmtime(){return mtime;}
}
