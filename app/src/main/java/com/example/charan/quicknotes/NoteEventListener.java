package com.example.charan.quicknotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NoteEventListener extends BroadcastReceiver {

    SharedPreferences sharedPreferences;
    Boolean quickNoteEnabled;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPreferences = context.getSharedPreferences("notesapp", Context.MODE_PRIVATE);
        quickNoteEnabled = true;

        if (sharedPreferences.contains("quick")) {
            quickNoteEnabled = sharedPreferences.getBoolean("quick", true);
        }

        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()) && quickNoteEnabled) {
            Intent service = new Intent(context.getApplicationContext(), NoteService.class);
            context.startService(service);
        }
    }
}
