package com.example.android.olaplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.olaplay.SongListAdapter.getImage;

/**
 * Created by anish on 12/20/2017.
 */

public class DownloadSongListAdapter extends ArrayAdapter<Songs> {
    DatabaseHelper myDb_download;
    ImageView coverImage;
    Context mContext;
    public DownloadSongListAdapter(Activity context, ArrayList<Songs> songs, Context cont) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, songs);
        myDb_download = new DatabaseHelper(cont);
        mContext = cont;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.download_song_list, parent, false);
        }
        final Songs currentSong = getItem(position);
        coverImage = (ImageView) listItemView.findViewById(R.id.coverimage_download);
        TextView songName = (TextView) listItemView.findViewById(R.id.song_name_download);
        TextView artistName = (TextView) listItemView.findViewById(R.id.artist_name_download);
        ImageView delete = (ImageView) listItemView.findViewById(R.id.delete);
        int currentId = currentSong.getmId();
        Cursor res_download = myDb_download.getSelectedData(currentId);
        while(res_download.moveToNext()) {
            coverImage.setImageBitmap(getImage(res_download.getBlob(4)));
            songName.setText(res_download.getString(1));
            artistName.setText(res_download.getString(3));
        }

        //THIS LISTENER ALLOWS FILE TO BE DELETED FROM DEVICE

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure you want to delete?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(currentSong.getmFilepath());
                                boolean deleted = file.delete();
                                myDb_download.deleteDownload(currentSong.getmName());
                                Object toRemove = getItem(position);
                                remove((Songs) toRemove);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return listItemView;
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


}
