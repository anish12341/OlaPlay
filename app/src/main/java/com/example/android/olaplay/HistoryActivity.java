package com.example.android.olaplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

//THIS METHOD ALLOWS YOU TO SEE HISTORY AND ALSO DELETE THE COMPLETE HISTORY RECORD

public class HistoryActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    HistoryListAdapter historyListAdapter = null;
    ListView historyList;
    Button clearHistory;
    TextView noHistory;
    ArrayList<History> histories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        myDb = new DatabaseHelper(getApplicationContext());
        historyList = (ListView) findViewById(R.id.list_history);
        clearHistory = (Button) findViewById(R.id.clear_history);
        noHistory = (TextView) findViewById(R.id.no_history);
        Cursor res = myDb.getAllDataHistory();
        if (res.getCount() > 0) {
            histories = new ArrayList<History>();
            while (res.moveToNext()) {
                histories.add(new History(res.getString(0), res.getInt(1), res.getString(2)));
            }
            historyListAdapter = new HistoryListAdapter(HistoryActivity.this, histories, getApplicationContext());
            historyList.setAdapter(historyListAdapter);
            clearHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (historyListAdapter != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                        builder.setMessage("Are you sure you want to delete?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        histories.clear();
                                        myDb.deleteHistory();
                                        historyListAdapter.notifyDataSetChanged();
                                        noHistory.setVisibility(View.VISIBLE);
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

                }
            });
        }
        else{
            noHistory.setVisibility(View.VISIBLE);
        }

    }
}
