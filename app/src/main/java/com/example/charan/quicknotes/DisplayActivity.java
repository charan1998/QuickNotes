package com.example.charan.quicknotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayActivity extends AppCompatActivity {

    SQLiteDatabase sqLiteDatabase;
    TextView descTextView;
    TextView noteTextView;
    String desc;
    String note;
    int noteId;
    MenuInflater menuInflater;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sqLiteDatabase = openOrCreateDatabase("notesdb", Context.MODE_PRIVATE, null);

        descTextView = findViewById(R.id.textView);
        noteTextView = findViewById(R.id.textView2);

        noteId = getIntent().getIntExtra("id", -1);
        desc = getIntent().getStringExtra("desc");
        note = getIntent().getStringExtra("note");

        descTextView.setText(desc);
        noteTextView.setText(note);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.display_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.delete_action) {
            AlertDialog.Builder alert = new AlertDialog.Builder(DisplayActivity.this);
            alert.setTitle("Delete note");
            alert.setMessage("Are you sure?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sqLiteDatabase.execSQL("DELETE FROM notes WHERE id=" + noteId);
                    Toast.makeText(DisplayActivity.this, "Note deleted!", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    Intent intent = new Intent(MainActivity.ACTION);
                    sendBroadcast(intent);
                    startActivity(new Intent(DisplayActivity.this, MainActivity.class));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
    }
}
