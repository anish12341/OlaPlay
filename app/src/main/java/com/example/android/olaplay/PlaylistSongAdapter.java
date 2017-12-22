package com.example.android.olaplay;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by anish on 12/20/2017.
 */

public class PlaylistSongAdapter extends ArrayAdapter<Songs> {

    DatabaseHelper myDb_playlist;
    Context mContext;
    ImageView coverImage;
    public PlaylistSongAdapter(Activity context, ArrayList<Songs> songs, Context cont) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, songs);
        myDb_playlist = new DatabaseHelper(cont);
        mContext = cont;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_song_list, parent, false);
        }
        final Songs currentSong = getItem(position);
        coverImage = (ImageView) listItemView.findViewById(R.id.coverimage_playlist);
        TextView songName = (TextView) listItemView.findViewById(R.id.song_name_playlist);
        TextView artistName = (TextView) listItemView.findViewById(R.id.artist_name_playlist);
        int currentId = currentSong.getmId();
        Cursor res_playlist = myDb_playlist.getSelectedData(currentId);
        while(res_playlist.moveToNext()) {
            currentSong.mUrl=res_playlist.getString(2);
            coverImage.setImageBitmap(getImage(res_playlist.getBlob(4)));
            songName.setText(res_playlist.getString(1));
            artistName.setText(res_playlist.getString(3));
        }

        return listItemView;
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


}
