package com.example.android.olaplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import static android.view.View.Y;

/**
 * Created by anish on 12/21/2017.
 */

public class HistoryListAdapter extends ArrayAdapter<History> {

    DatabaseHelper myDb_history;

    public HistoryListAdapter(Activity context, ArrayList<History> history, Context cont) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, history);
        myDb_history = new DatabaseHelper(cont);

    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.history_list, parent, false);
        }
        final History history = getItem(position);
        TextView state = (TextView) listItemView.findViewById(R.id.state);
        TextView name = (TextView) listItemView.findViewById(R.id.name);
        TextView time = (TextView) listItemView.findViewById(R.id.time_stamp);
        int currentId = history.getmId();
        time.setText(history.getmtime());
        Cursor res_history = myDb_history.getSelectedData(currentId);
        while (res_history.moveToNext()) {
            if (history.getmState().equals("d")) {
                state.setText("DOWNLOADED");
                name.setText(res_history.getString(1));
            } else {
                state.setText("STREAMED");
                name.setText(res_history.getString(1));
            }
        }
            return listItemView;
    }
}
