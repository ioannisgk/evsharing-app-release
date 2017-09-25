package com.ioannisgk.evsharingapp;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import com.ioannisgk.evsharingapp.utils.Settings;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Get activity resources
        final TextView aboutText = (TextView) findViewById(R.id.fileTextView);

        //Initialize variables

        String text = "";
        InputStream file = null;
        int size = 0;
        byte[] buffer = null;

        try {

            // Copy text data from file to buffer

            file = getAssets().open("about.txt");
            size = file.available();
            buffer = new byte[size];
            file.read(buffer);
            file.close();

        } catch (IOException e) {

            Settings.showDialogBox("Memory error", "Application data could not be read", AboutActivity.this);
            e.printStackTrace();
        }

        // Show buffer text data into text field

        text = new String(buffer);
        aboutText.setText(text);
    }
}