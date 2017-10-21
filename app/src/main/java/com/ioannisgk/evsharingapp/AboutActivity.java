package com.ioannisgk.evsharingapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.utils.Settings;

import butterknife.ButterKnife;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_about;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}