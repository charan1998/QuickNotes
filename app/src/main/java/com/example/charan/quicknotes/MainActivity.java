package com.example.charan.quicknotes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesAdapterOnClickHandler{

    TextView errorTextView;
    SQLiteDatabase sqLiteDatabase;
    RecyclerView notesRecyclerView;
    ArrayList<Integer> ids;
    ArrayList<String> notes;
    ArrayList<String> descriptions;
    ArrayList<String> dates;
    LinearLayoutManager layoutManager;
    NotesAdapter notesAdapter;
    FloatingActionButton addButtton;
    public static final String ACTION = "com.android.DATABASE_CHANGED";
    BroadcastReceiver broadcastReceiver;
    MenuInflater menuInflater;
    Switch actionSwitch;
    Boolean quickAddEnabled;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quickAddEnabled = true;

        sharedPreferences = getSharedPreferences("notesapp", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.contains("quick")) {
            quickAddEnabled = sharedPreferences.getBoolean("quick", true);
        }

        if (!isMyServiceRunning(NoteService.class) && quickAddEnabled) {
            Intent service = new Intent(this, NoteService.class);
            startService(service);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_BOOT_COMPLETED}, 0);
        }

        errorTextView = findViewById(R.id.error_tv);
        notesRecyclerView = findViewById(R.id.notes_rv);
        sqLiteDatabase = openOrCreateDatabase("notesdb", Context.MODE_PRIVATE, null);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        notesRecyclerView.setLayoutManager(layoutManager);
        notesRecyclerView.setHasFixedSize(true);
        notesAdapter = new NotesAdapter(this);
        notesRecyclerView.setAdapter(notesAdapter);
        addButtton = findViewById(R.id.floating_add_button);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setNotes();
            }
        };

        try {
            IntentFilter intentFilter = new IntentFilter(ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (!sharedPreferences.contains("first")) {
            showForFirstTime();
        }

        setNotes();
    }

    public void floatingAddAction(View view) {
        Intent intent = new Intent(this, NoteAddActivity.class);
        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions granted", Toast.LENGTH_LONG).show();
        }
    }

    void setNotes() {
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM notes", null);
            if (cursor.getCount() == 0) {
                notesRecyclerView.setVisibility(View.INVISIBLE);
                errorTextView.setVisibility(View.VISIBLE);
            }
            else {
                errorTextView.setVisibility(View.INVISIBLE);
                notesRecyclerView.setVisibility(View.VISIBLE);
                ids = new ArrayList<>();
                notes = new ArrayList<>();
                descriptions = new ArrayList<>();
                dates = new ArrayList<>();

                while (cursor.moveToNext()) {
                    ids.add(cursor.getInt(0));
                    notes.add(cursor.getString(1));
                    descriptions.add(cursor.getString(2));
                    dates.add(cursor.getString(3));
                }

                notesAdapter.setNotesData(descriptions, notes, dates);

                runLayoutAnimation(notesRecyclerView);
            }
            cursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            notesRecyclerView.setVisibility(View.INVISIBLE);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(int position) {
        Intent displayIntent = new Intent(this, DisplayActivity.class);
        displayIntent.putExtra("id", ids.get(position));
        displayIntent.putExtra("desc", descriptions.get(position));
        displayIntent.putExtra("note", notes.get(position));
        startActivity(displayIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        actionSwitch = menu.findItem(R.id.action_quick).getActionView().findViewById(R.id.switch_action);
        actionSwitch.setChecked(quickAddEnabled);
        actionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent service = new Intent(getApplicationContext(), NoteService.class);
                    startService(service);
                    Toast.makeText(MainActivity.this, "Quick Add started", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent service = new Intent(getApplicationContext(), NoteService.class);
                    stopService(service);
                    Toast.makeText(MainActivity.this, "Quick Add closed", Toast.LENGTH_LONG).show();
                }
                editor.putBoolean("quick", isChecked);
                editor.apply();
            }
        });
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter intentFilter = new IntentFilter(ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            IntentFilter intentFilter = new IntentFilter(ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
        editor.commit();
    }

    void showForFirstTime() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.favicon_round);
        builder.setCancelable(false);
        builder.setTitle("Note");
        builder.setMessage("Use the switch in the action bar to turn on and off the Quick Add service");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        editor.putBoolean("first", false);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear_action) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Clear notes");
            alert.setMessage("Are you sure?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sqLiteDatabase.execSQL("DELETE FROM notes");
                    Toast.makeText(MainActivity.this, "Notes cleared!", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    Intent intent = new Intent(MainActivity.ACTION);
                    sendBroadcast(intent);
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
}