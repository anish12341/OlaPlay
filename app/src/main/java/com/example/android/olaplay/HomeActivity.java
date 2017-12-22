package com.example.android.olaplay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ohoussein.playpause.PlayPauseView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.android.olaplay.R.id.hidden;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private MediaPlayer mediaPlayer;
    //When one song completes next in the queue starts
    private MediaPlayer.OnCompletionListener mCompletionListener=new MediaPlayer.OnCompletionListener(){
        @Override
        public void onCompletion(MediaPlayer mp){
            playNext();
        }
    };

   //OnPrepared listener to listen when data source set for mediaplayer is ready

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

    //Determine what to do when focus changes

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
    SharedPreferences olaPayPreferences;
    SharedPreferences.Editor editor;
    public static final String mypref = "mypref";
    public static final String current = "current";
    public static final String lengthKey = "length";
    public static final String songId = "ID";
    RelativeLayout cordinator;
    Button play,skipNext,skipPrev,first,second,none,last;
    public static final String TAG = "HomeActivity";
    ProgressBar bar,barHidden;
    boolean dbDone = false;
    int lengthOfData,paging=0,length=0;
    int currentPlaying=0;
    int mShortAnimationDuration;
    ImageView dynamicImage;
    TextView dynamicName,dynamicArtist;
    PlayPauseView view1,viewHidden;
    ListView listView;
    String songUrl,currentSongName;
    byte[] currentsongImage;
    ArrayList<Songs> songs;
    SongListAdapter songListAdapter;
    ProgressDialog progressDialog ;
    boolean progressDetermine=false;
    View line;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        olaPayPreferences = getSharedPreferences(mypref,getApplicationContext().MODE_PRIVATE);
        if(olaPayPreferences.contains(lengthKey)){
              //used Shared preference to check prevent making network calls everytime app is running
              length = olaPayPreferences.getInt(lengthKey,0);
        }


        //Check whether phone is connected to internet or not
        if(isConnected()) {

            //Initialize all the views

            myDb = new DatabaseHelper(this);
            //myDb.deleteWhole();
            skipNext = (Button) findViewById(R.id.skip_next);
            skipPrev = (Button) findViewById(R.id.skip_previous);
            dynamicImage = (ImageView) findViewById(R.id.dynamic_image);
            dynamicName = (TextView) findViewById(R.id.dynamic_songname);
            first = (Button) findViewById(R.id.first);
            second = (Button) findViewById(R.id.second);
            none = (Button) findViewById(R.id.none);
            last = (Button) findViewById(R.id.last);
            line = (View) findViewById(R.id.line);
            view1 = (PlayPauseView) findViewById(R.id.play_pause_view);
            bar = (ProgressBar) findViewById(R.id.progressBar);
            mShortAnimationDuration = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
            bar.setVisibility(View.GONE);
            listView = (ListView) findViewById(R.id.listsong);
            mAudioMAnager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mediaPlayer!=null) {
                        view1.toggle();
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.pause();
                        }
                    }
                }
            });


            //Asynctask to make network call to access data of WEB API
            AsyncTaskRunner runner = new AsyncTaskRunner();
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL("http://starlord.hackerearth.com/studio");
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } catch (Exception e) {
                Log.d(TAG, "Error generating HttpURLConnection Object");
            }
            runner.execute(httpURLConnection);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }
        else{

            //jump directly to DownloadActivity.java because internet is not available
            cordinator = (RelativeLayout) findViewById(R.id.cordinator);
            Snackbar snackbar = Snackbar.make(cordinator,"No Internet Connection",Snackbar.LENGTH_INDEFINITE)
                    .setAction("GO OFFLINE",new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent downloadIntent = new Intent(HomeActivity.this,DownloadActivity.class);
                            startActivity(downloadIntent);
                        }
                    });
            snackbar.show();
        }

    }


    //Check whether phone is connected to the net or not


    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null)
            return false;
        return true;
    }

    //Convert JSON inputsream into string

    public String readStream(InputStream in){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line ;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                total.append(line).append("\n");
            }
        }catch (IOException e){
            Log.d(TAG,"Problem reading response");
        }
        return total.toString();
    }



    //Asynctask to make network call



    private class AsyncTaskRunner extends AsyncTask<HttpURLConnection,String,String> {

        String result="";

        boolean isInserted ;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(HomeActivity.this,"Fetching Records","Please Wait!!");
        }


        //This method performs logic to determine whether new data is added inside Web API or not
        //If it did then add new data in local database (SQLITE).

        @Override
        protected String doInBackground(HttpURLConnection... params) {
            try {
                InputStream in = new BufferedInputStream(params[0].getInputStream());
                result = readStream(in);
                JSONArray jsonArray = new JSONArray(result);
                lengthOfData = jsonArray.length();
                if (length == lengthOfData) {
                    return "o";
                } else {
                    try {
                        for (int i = length; i < lengthOfData; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            URL url = new URL(jsonObject.getString("cover_image"));
                            Bitmap bmp = null;
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            bmOptions.inSampleSize = 1;
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                            //From the links , provided in web API . They are being redirected from http
                            //to https . So I had to implement a logic to work its way around .
                            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                                String newUrl = httpURLConnection.getHeaderField("Location");
                                url = new URL(newUrl);
                                httpURLConnection = (HttpURLConnection) url.openConnection();

                            }
                            InputStream stream = httpURLConnection.getInputStream();
                            bmp = BitmapFactory.decodeStream(stream);
                            stream.close();
                            isInserted = myDb.insertDataSong(jsonObject.getString("song"), jsonObject.getString("url"), jsonObject.getString("artists"), getBytes(bmp));
                            if (!isInserted)
                                Log.d(TAG, "Database insertion failed");

                        }
                        editor = olaPayPreferences.edit();
                        editor.putInt(lengthKey,lengthOfData);
                        editor.commit();
                    } catch (Exception e) {
                        Log.d(TAG, "Failure" + e.toString());
                    }
                }
            }catch(Exception e){
                Log.d(TAG, "Failure" + e.toString());
            }
            return "k";
        }

        //Inside onPostExecute mainly implementing paging of the data .

        @Override
        protected void onPostExecute(String s) {
            if (HomeActivity.this.isDestroyed()) {
                return;
            }
            dismissProgressDialog();
            progressDetermine =false;

            if(lengthOfData<=5){                                      //if <5 songs only one page is needed
                first.setVisibility(View.VISIBLE);
                first.setBackgroundColor(Color.parseColor("#111111"));
                first.setTextColor(Color.parseColor("#ffffff"));
                populate(1);
            }
            else if(lengthOfData<=10){                               //if <=10 songs only 2 pages are needed
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

            skipNext.setOnClickListener(new View.OnClickListener() {     //It determines what happens when skipnext is clicked
                @Override
                public void onClick(View v) {
                   playNext();
                }
            });
            skipPrev.setOnClickListener(new View.OnClickListener() {     //It determines what happens when skipprev is clicked
                @Override
                public void onClick(View v) {
                  playPrev();
                }
            });
            if(progressDetermine)
                progressDialog.dismiss();

        }



       //This is the main logic which populates each page using list view and adapter , also set click
        //listener for the elements of list of 5 songs which plays songs


        public void populate(int paging){

            if(songListAdapter!=null) {
                songs.clear();
                songListAdapter.notifyDataSetChanged();
            }

            Cursor res = myDb.getAllData();
            songs=new ArrayList<Songs>();
            int count = 1;
            if(paging!=0)
                   count= paging*5+1;
            int i=0;
            while (i<5 && res.moveToNext()){
                if(res.getInt(0)>=count && res.getInt(0)<=count+4) {
                    songs.add(new Songs(res.getString(1), res.getString(2), res.getString(3), res.getBlob(4)));
                    i++;
                }
            }


            songListAdapter = new SongListAdapter(HomeActivity.this,songs,getApplicationContext());
            listView = (ListView) findViewById(R.id.listsong);
            listView.setAdapter(songListAdapter);
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
                    currentPlaying = ID;                               //This stores value of ID field in song_table
                    currentSongName = res1.getString(1);               //This stores value of song(song name) field in song_table
                    currentsongImage = res1.getBlob(4);                //This stores value of image field in song_table
                    songUrl = res1.getString(2);


                    //These are the components of player which gets changed everytime new song is played


                    dynamicImage.setVisibility(View.VISIBLE);
                    dynamicName.setVisibility(View.VISIBLE);
                    dynamicImage.setImageBitmap(getImage(currentsongImage));
                    dynamicName.setText(currentSongName);


                    //THIS QUERY INSERT INTO history_table WHICH WILL BE USED TO CHECK HISTORY
                    boolean is = myDb.insertDataHistory("p",res1.getInt(0),Calendar.getInstance().getTime().toString().substring(0,16));
                    if(is){
                        Log.d(TAG,"inserted inside history_table");
                    }

                    releaseMediaPlayer();
                    setIt(songUrl);
                }
            });
            progressDetermine = true;
        }
    }


    //THIS METHOD SETS DATA RESOURCES FOR EACH PLAY

    public void setIt(String url){
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

    //TO CONVERT BITMAP INTO BYTES TO BE STORED INSIDE DATABSE

    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        return stream.toByteArray();
    }

    //TO CONVERT BYTE INTO BITMAP TO BE SHOWN INSIDE VIEW

    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


    //THIS FUNCTION IS CALLED WHEN SKIPNEXT IS CLICKED OR WHEN CURRENT SONG IS COMPLETED

    public void playNext() {
        Log.d("SongClick", lengthOfData + "lengthofdata");
        if (currentPlaying < lengthOfData && currentPlaying != 0) {
            if (!mediaPlayer.isPlaying()) {
                view1.toggle();
            }
            currentPlaying += 1;
            releaseMediaPlayer();
            view1.toggle();
            //cursormyDb.
            Cursor res1 = myDb.getSelectedData(currentPlaying);
            res1.moveToNext();
            currentSongName = res1.getString(1);
            currentsongImage = res1.getBlob(4);
            songUrl = res1.getString(2);
            dynamicName.setText(currentSongName);
            dynamicImage.setImageBitmap(getImage(currentsongImage));
            Log.d("SongClickedNext", currentSongName + "  " + songUrl + "  " + currentPlaying);
            setIt(songUrl);
        }
    }



    //THIS FUNCTION IS CALLED WHEN SKIPPREV IS CLICKED OR WHEN CURRENT SONG IS COMPLETED

    public void playPrev(){
        Log.d("SongClick",lengthOfData+"lengthofdata");
        if(currentPlaying>1 && currentPlaying!=0) {
            if(!mediaPlayer.isPlaying()){
                view1.toggle();
            }
            currentPlaying -= 1;
            releaseMediaPlayer();
            view1.toggle();
            //cursormyDb.
            Cursor res1 = myDb.getSelectedData(currentPlaying);
            res1.moveToNext();
            currentSongName = res1.getString(1);
            currentsongImage = res1.getBlob(4);
            songUrl = res1.getString(2);
            dynamicName.setText(currentSongName);
            dynamicImage.setImageBitmap(getImage(currentsongImage));
            Log.d("SongClickedNext", currentSongName+"  "+songUrl + "  " + currentPlaying);
            setIt(songUrl);
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

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }












    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        /*
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }


    //HANDLE NAVIGATION BAR CLICK EVEMTS SUCH AS DOWNLOADS,PLAYLISTS,HISTORY


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.playlists) {
            // Handle the camera action
            Intent playlistIntent = new Intent(HomeActivity.this,PlaylistActivity.class);
            startActivity(playlistIntent);

        } else if (id == R.id.downloads) {
            Intent downloadIntent = new Intent(HomeActivity.this,DownloadActivity.class);
            startActivity(downloadIntent);
        }

        else if (id == R.id.show_history){
            Intent historyIntent = new Intent(HomeActivity.this,HistoryActivity.class);
            startActivity(historyIntent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
