package com.example.android.olaplay;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

/**
 * Created by anish on 12/21/2017.
 * AN INTENT SERVICE TO FACILITATE CANCELLATION OF DOWNLOAD
 */

public class CancelDownload extends IntentService {

    public CancelDownload(){
        super("CancelDownload");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final NotificationManager mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancel(1);
        File file = new File(intent.getExtras().getString("Filepath"));
        file.delete();
    }
}
