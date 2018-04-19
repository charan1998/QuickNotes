package com.example.charan.quicknotes;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class NoteAddActivity extends AppCompatActivity {

    TextView descriptionTextView;
    TextView noteTextView;
    Toast mToast;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add);
        this.setFinishOnTouchOutside(false);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getAppUsableScreenSize(this).x - 50;

        this.getWindow().setAttributes(params);


        descriptionTextView = findViewById(R.id.editText);
        noteTextView = findViewById(R.id.editText4);

        sqLiteDatabase = openOrCreateDatabase("notesdb", Context.MODE_PRIVATE, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDatabase.close();
    }

    public void addAction(View view) {
        String description = descriptionTextView.getText().toString().trim();
        String note = noteTextView.getText().toString().trim();
        if (description.equals("") || note.equals("")) {
            if (description.equals("") && note.equals("")) {
                createToast("Enter the note and a short description");
            }
            else if (description.equals("")) {
                createToast("Enter a short description");
            }
            else if (note.equals("")) {
                createToast("Enter the note");
            }
        }
        else {
            description = getDatabaseSafeText(description);
            note = getDatabaseSafeText(note);
            sqLiteDatabase.execSQL("INSERT INTO notes(note, description, dateAdded) VALUES('" + note + "', '" + description + "', '" + getDateTime() + "')");
            createToast("Note added!!");
            Intent intent = new Intent(MainActivity.ACTION);
            sendBroadcast(intent);
        }
        finish();
    }

    public void cancelAction(View view) {
        finish();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager != null ? windowManager.getDefaultDisplay() : null;
        Point size = new Point();
        if (display != null) {
            display.getSize(size);
        }
        return size;
    }

    void createToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        mToast.show();
    }

    String getDatabaseSafeText(String string) {
        StringBuffer stringBuffer = new StringBuffer(string);
        ArrayList<Integer> ind = new ArrayList<>();
        for (int i = 0; i < stringBuffer.length(); i ++) {
            if (stringBuffer.charAt(i) == '\'') {
                ind.add(i);
            }
        }
        int n = 0;
        for (int i : ind) {
            stringBuffer.insert(i + n, '\'');
            n ++;
        }
        return stringBuffer.toString();
    }

    String getDateTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String amPm;
        if (calendar.get(Calendar.AM_PM) == 0) {
            amPm = "AM";
        }
        else {
            amPm = "PM";
        }
        if (minute < 10) {
            return day + "-" + month + "-" + year + " " + hour + ":0" + minute + " " + amPm;
        }
        return day + "-" + month + "-" + year + " " + hour + ":" + minute + " " + amPm;
    }
}
