package com.example.charan.quicknotes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class NoteService extends Service {

    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    private static final String CHANNEL_ID = "17268";
    private static final int UNIQUE_ID = 17268;
    RemoteViews remoteViews;
    PendingIntent pendingIntent;
    SQLiteDatabase sqLiteDatabase;

    Boolean first;

    @Override
    public void onCreate() {
        super.onCreate();
        first = false;
        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setTicker("QuickNotes started");
        builder.setCustomContentView(remoteViews);
        builder.setContentTitle("QuickNotes");
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.favicon_round);
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(R.mipmap.favicon_round);
        builder.setContentText("Swipe Down for options");
        builder.setContentIntent(pendingIntent);
        Intent addIntent = new Intent(this, NoteAddActivity.class);
        addIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent addPendingIntent = PendingIntent.getActivity(this, 1, addIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.mipmap.plus_small, "Add", addPendingIntent);
        sqLiteDatabase = openOrCreateDatabase("notesdb", Context.MODE_PRIVATE, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager.notify(UNIQUE_ID, builder.build());
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes(id INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1, note varchar, description varchar, dateAdded varchar)");
        if (first) {
            Intent broadcastIntent = new Intent(MainActivity.ACTION);
            sendBroadcast(broadcastIntent);
            first = false;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
        notificationManager.cancel(UNIQUE_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
