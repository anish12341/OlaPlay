package com.example.android.olaplay;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ohoussein.playpause.PlayPauseView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.R.attr.bitmap;


//THIS ACTIVITY SHOWS LIST OF DOWNLOADED SONGS , MOST OF LOGIC IS SAME AS HOMEACTIVITY
//ONLY DIFFERENCE IS THAT IT GETS ITS DATA SOURCES LOCALLY FROM PHONE WHILE
//HOMEACTIVITY STREAMS IT FROM URL


public class DownloadActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnCompletionListener mCompletionListener=new MediaPlayer.OnCompletionListener(){
        @Override
        public void onCompletion(MediaPlayer mp){
            playNext();
        }
    };

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (!mediaPlayer.isPlaying()) {
                bar.setVisibility(View.GONE);
                view1.setVisibility(View.VISIBLE);
                mediaPlayer.start();
                view1.toggle();
            } else {
                mediaPlayer.pause();
                view1.toggle();
            }
        }
    };

    private AudioManager mAudioMAnager;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

                mediaPlayer.pause();
                view1.toggle();
                mediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // The AUDIOFOCUS_GAIN case means we have regained focus and can resume playback.
                mediaPlayer.start();
                view1.toggle();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // The AUDIOFOCUS_LOSS case means we've lost audio focus and
                // Stop playback and clean up resources
                releaseMediaPlayer();
            }
        }
    };
    DatabaseHelper myDb ;
    RelativeLayout cordinator;
    Button play,skipNext,skipPrev,first,second,none,last;
    TextView noDownloads,dynamicName;
    public static final String TAG = "DownloadActivity";
    ProgressBar bar;
    PlayPauseView view;
    boolean dbDone = false;
    int lengthOfData,paging=0;
    int currentPlaying;
    PlayPauseView view1;
    ImageView dynamicImage;
    ListView listView;
    String songPath,currenSongName;
    byte[] currentSongImage;
    ArrayList<Songs> downloadedSongs;
    DownloadSongListAdapter downloadsongListAdapter;
    ProgressDialog progressDialog ;
    boolean progressDetermine=false;
    Cursor res;
    View line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        //Initialize all the views
        myDb = new DatabaseHelper(this);
        skipNext = (Button) findViewById(R.id.skip_next_download);
        skipPrev = (Button) findViewById(R.id.skip_previous_download);
        dynamicImage = (ImageView) findViewById(R.id.dynamic_image_download);
        dynamicName = (TextView) findViewById(R.id.dynamic_songname_download);
        noDownloads = (TextView) findViewById(R.id.no_downloads);
        first = (Button) findViewById(R.id.first_download);
        second = (Button) findViewById(R.id.second_download);
        none = (Button) findViewById(R.id.none_download);
        last = (Button) findViewById(R.id.last_download);
        line = (View) findViewById(R.id.line);
        view = (PlayPauseView) findViewById(R.id.play_pause_view_download);
        bar = (ProgressBar) findViewById(R.id.progressBar_download);

        bar.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.listsong_download);
        mAudioMAnager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        view1 = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mediaPlayer!=null) {
                   view.toggle();
                   if (!mediaPlayer.isPlaying()) {
                       mediaPlayer.start();
                   } else {
                       mediaPlayer.pause();
                   }
               }
            }
        });

        progressDialog = ProgressDialog.show(DownloadActivity.this,"Fetching Records","Please Wait!!");
        setResources();

    }


        protected void setResources() {
            res = myDb.getAllDataDownload();
            lengthOfData = res.getCount();
            progressDetermine =false;

            if (lengthOfData == 0) {
                progressDialog.dismiss();
                noDownloads.setVisibility(View.VISIBLE);

            }

            else if(lengthOfData<=5){
                first.setVisibility(View.VISIBLE);
                first.setBackgroundColor(Color.parseColor("#111111"));
                first.setTextColor(Color.parseColor("#ffffff"));
                populate(0);
            }
            else if(lengthOfData<=10){
                populate(0);
                first.setVisibility(View.VISIBLE);
                second.setVisibility(View.VISIBLE);
                first.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        first.setBackgroundColor(Color.parseColor("#111111"));
                        first.setTextColor(Color.parseColor("#ffffff"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        populate(0);
                    }
                });
                second.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        second.setBackgroundColor(Color.parseColor("#111111"));
                        second.setTextColor(Color.parseColor("#ffffff"));
                        first.setBackgroundColor(Color.parseColor("#ffffff"));
                        first.setTextColor(Color.parseColor("#111111"));
                        populate(1);
                    }
                });
            }
            else if(lengthOfData<=15){
                populate(0);
                first.setVisibility(View.VISIBLE);
                second.setVisibility(View.VISIBLE);
                none.setVisibility(View.VISIBLE);
                first.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        first.setBackgroundColor(Color.parseColor("#111111"));
                        first.setTextColor(Color.parseColor("#ffffff"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        populate(0);
                    }
                });
                second.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        second.setBackgroundColor(Color.parseColor("#111111"));
                        second.setTextColor(Color.parseColor("#ffffff"));
                        first.setBackgroundColor(Color.parseColor("#ffffff"));
                        first.setTextColor(Color.parseColor("#111111"));
                        none.setBackgroundColor(Color.parseColor("#ffffff"));
                        none.setTextColor(Color.parseColor("#111111"));
                        populate(1);
                    }
                });
                none.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        none.setBackgroundColor(Color.parseColor("#111111"));
                        none.setTextColor(Color.parseColor("#ffffff"));
                        first.setBackgroundColor(Color.parseColor("#ffffff"));
                        first.setTextColor(Color.parseColor("#111111"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        populate(2);
                    }
                });

            }
            else if(lengthOfData<=20){
                populate(0);
                first.setVisibility(View.VISIBLE);
                second.setVisibility(View.VISIBLE);
                none.setVisibility(View.VISIBLE);
                last.setVisibility(View.VISIBLE);
                first.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        first.setBackgroundColor(Color.parseColor("#111111"));
                        first.setTextColor(Color.parseColor("#ffffff"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        none.setBackgroundColor(Color.parseColor("#ffffff"));
                        none.setTextColor(Color.parseColor("#111111"));
                        last.setBackgroundColor(Color.parseColor("#ffffff"));
                        last.setTextColor(Color.parseColor("#111111"));
                        populate(0);
                    }
                });
                second.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        second.setBackgroundColor(Color.parseColor("#111111"));
                        second.setTextColor(Color.parseColor("#ffffff"));
                        first.setBackgroundColor(Color.parseColor("#ffffff"));
                        first.setTextColor(Color.parseColor("#111111"));
                        none.setBackgroundColor(Color.parseColor("#ffffff"));
                        none.setTextColor(Color.parseColor("#111111"));
                        last.setBackgroundColor(Color.parseColor("#ffffff"));
                        last.setTextColor(Color.parseColor("#111111"));
                        populate(1);
                    }
                });
                none.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        none.setBackgroundColor(Color.parseColor("#111111"));
                        none.setTextColor(Color.parseColor("#ffffff"));
                        first.setBackgroundColor(Color.parseColor("#ffffff"));
                        first.setTextColor(Color.parseColor("#111111"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        last.setBackgroundColor(Color.parseColor("#ffffff"));
                        last.setTextColor(Color.parseColor("#111111"));

                        populate(2);
                    }
                });
                last.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        last.setBackgroundColor(Color.parseColor("#111111"));
                        last.setTextColor(Color.parseColor("#ffffff"));
                        first.setBackgroundColor(Color.parseColor("#ffffff"));
                        first.setTextColor(Color.parseColor("#111111"));
                        second.setBackgroundColor(Color.parseColor("#ffffff"));
                        second.setTextColor(Color.parseColor("#111111"));
                        none.setBackgroundColor(Color.parseColor("#ffffff"));
                        none.setTextColor(Color.parseColor("#111111"));

                        populate(3);
                    }
                });
            }
            else{
                first.setVisibility(View.VISIBLE);
                second.setVisibility(View.VISIBLE);
                none.setText("....");
                none.setVisibility(View.VISIBLE);
                last.setVisibility(View.VISIBLE);
            }
            skipNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playNext();
                }
            });
            skipPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   playPrev();
                }
            });
            if(progressDetermine)
                progressDialog.dismiss();

        }






        public void populate(int paging){
            if(downloadsongListAdapter!=null) {
                downloadedSongs.clear();
                downloadsongListAdapter.notifyDataSetChanged();
            }

            res = myDb.getAllDataDownload();
            downloadedSongs=new ArrayList<Songs>();
            int count = 1;
            if(paging!=0)
                count= paging*5+1;
            int i=0;
            while (i<5 && res.moveToNext()){
                if(res.getInt(0)>=count && res.getInt(0)<=count+4) {
                    downloadedSongs.add(new Songs(res.getString(1), res.getInt(2), res.getString(3)));
                    i++;
                }
            }
            downloadsongListAdapter = new DownloadSongListAdapter(DownloadActivity.this,downloadedSongs,DownloadActivity.this);
            listView = (ListView) findViewById(R.id.listsong_download);
            listView.setAdapter(downloadsongListAdapter);
            //setingDatasource();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                        view1.toggle();
                    }
                    Object song = listView.getAdapter().getItem(position);
                    Songs songClicked = (Songs) song;
                    Cursor res = myDb.getId(songClicked.getmName());
                    res.moveToNext();
                    Cursor res1 = myDb.getSelectedData(res.getInt(0));
                    res1.moveToNext();
                    final int ID = res.getInt(0);
                    Cursor res2 = myDb.getDownloadID(ID);
                    res2.moveToNext();
                    currentPlaying = res2.getInt(0);
                    currenSongName = res1.getString(1);
                    currentSongImage = res1.getBlob(4);
                    Cursor res4 = myDb.getFilepath(currentPlaying);
                    res4.moveToNext();
                    songPath=res4.getString(0);
                    dynamicImage.setVisibility(View.VISIBLE);
                    dynamicName.setVisibility(View.VISIBLE);
                    dynamicImage.setImageBitmap(getImage(currentSongImage));
                    dynamicName.setText(currenSongName);
                    releaseMediaPlayer();
                    setIt(songPath);
                }
            });
            progressDetermine = true;
        }


        public void setIt(String url) {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar_download);
            bar.setVisibility(View.VISIBLE);
            view1.setVisibility(View.INVISIBLE);
            int result = mAudioMAnager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Log.d(TAG, "Cannot load media" + e.toString());
                }

                mediaPlayer.setOnPreparedListener(mPreparedListener);
                mediaPlayer.setOnCompletionListener(mCompletionListener);

            }
        }
        public void playNext(){
            if(currentPlaying<lengthOfData && currentPlaying!=0) {
                if(!mediaPlayer.isPlaying()){
                    view1.toggle();
                }
                currentPlaying += 1;
                releaseMediaPlayer();
                view1.toggle();
                Cursor res = myDb.getFilepath(currentPlaying);
                res.moveToNext();
                Cursor res2 = myDb.getSelectedDataDownload(currentPlaying);
                res2.moveToNext();
                Cursor res1 = myDb.getSelectedData(res2.getInt(2));
                res1.moveToNext();
                currenSongName = res1.getString(1);
                currentSongImage = res1.getBlob(4);
                dynamicName.setText(currenSongName);
                dynamicImage.setImageBitmap(getImage(currentSongImage));
                songPath = res.getString(0);
                setIt(songPath);
            }
        }

        public void playPrev(){
            if(currentPlaying>1 && currentPlaying!=0) {
                if(!mediaPlayer.isPlaying()){
                    view1.toggle();
                }
                currentPlaying -= 1;
                releaseMediaPlayer();
                view1.toggle();
                Cursor res = myDb.getFilepath(currentPlaying);
                res.moveToNext();
                Cursor res1 = myDb.getSelectedData(res.getInt(2));
                res1.moveToNext();
                currenSongName = res1.getString(1);
                currentSongImage = res1.getBlob(4);
                dynamicName.setText(currenSongName);
                dynamicImage.setImageBitmap(getImage(currentSongImage));
                songPath = res.getString(0);
                setIt(songPath);
            }
        }
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mediaPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mediaPlayer = null;

            mAudioMAnager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }
    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
