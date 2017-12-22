package com.example.android.olaplay;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.id;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.android.olaplay.R.id.cancel_action;
import static com.example.android.olaplay.R.id.last;

/**
 * Created by anish on 12/17/2017.
 */

public class SongListAdapter extends ArrayAdapter<Songs> {
    DatabaseHelper myDb;
    public static final String TAG = "SongListAdapter";
    ImageView coverImage;
    Context mContext;
    Activity mActivity;
    String filePath;
    boolean oneDownload=false;

    public SongListAdapter(Activity context, ArrayList<Songs> songs, Context cont) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, songs);
        myDb=new DatabaseHelper(cont);
        mContext = cont;
        mActivity = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.song_list, parent, false);
        }
        final Songs currentSong = getItem(position);
        coverImage = (ImageView) listItemView.findViewById(R.id.coverimage);
        coverImage.setImageBitmap(getImage(currentSong.getmImage()));
        TextView songName = (TextView) listItemView.findViewById(R.id.song_name);
        TextView artistName = (TextView) listItemView.findViewById(R.id.artist_name);
        ImageView download = (ImageView) listItemView.findViewById(R.id.download);
        final ImageView star =(ImageView) listItemView.findViewById(R.id.starred);
        final String name = currentSong.getmName();
        songName.setText(name);
        artistName.setText(currentSong.getmArtists());

        if(isStarred(name)){
              star.setImageResource(R.drawable.ic_star_black_48dp);
        }
        else{
            star.setImageResource(R.drawable.ic_star_border_black_48dp);
        }

        //WHEN DOWNLOAD BUTTON IS CLICKED

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!oneDownload) {
                    oneDownload=true;
                    if (isDownloaded(name)) {
                        oneDownload=false;
                        Toast.makeText(mContext, "Song Already Downloaded Please Check 'My Downloads'", Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        int count;
                                        try {
                                            File file = createFile(currentSong.getmName());
                                            filePath = file.getAbsolutePath();
                                            final int id = 1;

                                            //THIS IS AN INTENT SERVICE , WHICH IS CALLED WHEN USE WANTS TO CANCEL THE DOWNLOAD
                                            final Intent cancelDownload = new Intent(mActivity, CancelDownload.class);
                                            cancelDownload.putExtra("Songname", name);
                                            cancelDownload.putExtra("Filepath", filePath);


                                            Intent downloadComplete = new Intent(mActivity, DownloadActivity.class);
                                            final NotificationManager mNotifyManager =
                                                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                                            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);

                                            PendingIntent completePending = PendingIntent.getActivity(mContext, 1, downloadComplete, FLAG_UPDATE_CURRENT);
                                            PendingIntent pending = PendingIntent.getService(mContext, 1, cancelDownload, FLAG_UPDATE_CURRENT);


                                            NotificationCompat.Action cancelAction = new NotificationCompat.Action(R.drawable.olaplay_logo, "Cancel Transfer", pending);

                                            mBuilder.setContentTitle("Downloading " + name)
                                                    .setContentText("Download in progress")
                                                    .setSmallIcon(R.drawable.olaplay_logo)
                                                    .addAction(cancelAction);


                                            //DOWNLOADING FUNCTION STARTS FROM HERE
                                            FileOutputStream outputStream = new FileOutputStream(file);
                                            URL url = new URL(currentSong.getmUrl());
                                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                                                String newUrl = httpURLConnection.getHeaderField("Location");
                                                url = new URL(newUrl);
                                                httpURLConnection = (HttpURLConnection) url.openConnection();
                                            }

                                            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream(), 8192);
                                            byte data[] = new byte[1024];
                                            if (isExternalStorageWritable()) {
                                                mBuilder.setProgress(0, 0, true);
                                                mNotifyManager.notify(id, mBuilder.build());


                                                while ((count = in.read(data)) != -1) {

                                                    //file.write
                                                    if (file.exists()) {
                                                        outputStream.write(data, 0, count);
                                                    } else {
                                                        //IF USER CANCELS THE TRANSFER THEN FILE IS DELETED
                                                        //AND THREAD EXISTS FROM HERE
                                                        oneDownload=false;
                                                        return;
                                                    }
                                                }

                                                Cursor res1 = myDb.getId(name);
                                                res1.moveToNext();

                                                //INSERTING DATA INTO download_table
                                                boolean isInserted = myDb.insertDataDownload(name, res1.getInt(0), file.getAbsolutePath());
                                                if (!isInserted) {
                                                    Log.d(TAG, "Cannot insert data into download_table");
                                                }

                                                //INSERTING DATA INTO history_table
                                                isInserted = myDb.insertDataHistory("d", res1.getInt(0), Calendar.getInstance().getTime().toString().substring(0, 16));
                                                if (!isInserted) {
                                                    Log.d(TAG, "Cannot insert data into download_table");
                                                }
                                                mBuilder.setContentTitle("Downloaded");
                                                mBuilder.setContentText("Download complete")
                                                        // Removes the progress bar
                                                        .setProgress(0, 0, false);
                                                mBuilder.mActions.clear();
                                                mBuilder.setContentIntent(completePending);
                                                mNotifyManager.notify(id, mBuilder.build());

                                                outputStream.flush();

                                                oneDownload=false;
                                            } else {
                                                Log.d(TAG, "Space on external storage is not available");
                                            }
                                        } catch (Exception e) {
                                            Log.d(TAG, e.toString());
                                        }
                                        // When the loop is finished, updates the notification

                                    }
                                }
// Starts the thread by calling the run() method in its Runnable
                        ).start();
                    }
                }
                else
                {
                    Toast.makeText(mContext, "Only One Download At a time", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //WHEN STAR BUTTON IS CLICKED TO ADD/REMOVE SONG TO/FROM PLAYLIST

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStarred(name)){
                    myDb.deleteStarred(name);
                    star.setImageResource(R.drawable.ic_star_border_black_48dp);
                    Toast.makeText(mContext,"Removed From Playlist",Toast.LENGTH_SHORT).show();
                }
                else{
                    Cursor res = myDb.getId(name);
                    res.moveToNext();
                    boolean isInserted = myDb.insertDataStarred(name,res.getInt(0));
                    star.setImageResource(R.drawable.ic_star_black_48dp);
                    Toast.makeText(mContext,"Added to Playlist",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return listItemView;
    }

    //CHECK WHETHER FILE IS ALREADY DOWNLOADED OR NOT
    public boolean isDownloaded(String name){
        Cursor res = myDb.getDownloadId(name);
        if(res.getCount()==0){
            return false;
        }
        return true;
    }


    //CHECK WHETHER ALREADY IN PLAYLIST OR NOT
    public boolean isStarred(String name){
        Cursor res = myDb.getStarredId(name);
        if(res.getCount()==0){
            return false;
        }
        return true;
    }


    //CREATE FILE FOR THE SONG TO BE DOWNLOADED
    public File createFile(String name) throws IOException{
        name = name.replace(" ","");
        String fileName = name+".mp3";
        File music = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),fileName);
        return music;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
