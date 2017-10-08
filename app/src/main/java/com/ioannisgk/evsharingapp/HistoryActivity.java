package com.ioannisgk.evsharingapp;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.Settings;

import java.io.File;

public class HistoryActivity extends AppCompatActivity {
    Button backtoProfile;
    Button deleteHistory;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Add icon to action bar

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo_32x32);

        // Get activity resources

        backtoProfile = (Button) findViewById(R.id.backButton);
        deleteHistory = (Button) findViewById(R.id.deleteButton);
        webView = (WebView) findViewById(R.id.dataWebView);

        // Load information from database and show them to the webview
        Settings.loadDB(Global.myDB, Global.myFile, webView);

        // Start ProfileActivity when clicking on back to profile

        backtoProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i1 = new Intent(HistoryActivity.this, ProfileActivity.class);
                startActivity(i1);
            }
        });

        // Delete user history when clicking on delete history

        deleteHistory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Delete data from database and reload webview

                Settings.deleteDB(Global.myDB, Global.myFile);
                webView.loadData ("User history is empty", "text/html", "UTF-8");
                Settings.showToast(getApplicationContext(), "User history is deleted");
            }
        });
    }

    // Main menu dropdown

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu (menu);
        MenuItem item1 = menu.add(0, 0, Menu.NONE, "Profile");
        MenuItem item2 = menu.add(0, 1, Menu.NONE, "Request");
        MenuItem item3 = menu.add(0, 2, Menu.NONE, "About us");
        MenuItem item4 = menu.add(0, 3, Menu.NONE, "Logout");
        return true;
    }

    // Start new activity depending main menu on selection

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case 0:
                Intent i1 = new Intent(this, ProfileActivity.class);
                startActivity (i1);
                return true;
            case 1:
                Intent i2 = new Intent(this, RequestActivity.class);
                startActivity (i2);
                return true;
            case 2:
                Intent i3 = new Intent(this, AboutActivity.class);
                startActivity (i3);
                return true;
            case 3:
                Global.currentUser = null;
                Intent i4 = new Intent(this, MainActivity.class);
                startActivity (i4);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}