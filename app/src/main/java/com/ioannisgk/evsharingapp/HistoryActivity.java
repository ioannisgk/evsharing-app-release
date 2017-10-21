package com.ioannisgk.evsharingapp;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.Settings;

import butterknife.ButterKnife;

public class HistoryActivity extends BaseActivity {
    Button backtoProfile;
    Button deleteHistory;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_main).setVisible(false);
        navigationMenu.findItem(R.id.nav_register).setVisible(false);

        // Get activity resources

        backtoProfile = (Button) findViewById(R.id.backButton);
        deleteHistory = (Button) findViewById(R.id.deleteButton);
        webView = (WebView) findViewById(R.id.dataWebView);

        // Load information from database and show them to the webview
        Settings.loadDB(Global.myDB, Global.databaseFile, webView);

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

                Settings.deleteDB(Global.myDB, Global.databaseFile);
                webView.loadData ("User history is empty", "text/html", "UTF-8");
                Settings.showToast(getApplicationContext(), "User history is deleted");
            }
        });
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
            case R.id.action_about:
                Intent i1 = new Intent(this, AboutActivity.class);
                startActivity (i1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_history;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}