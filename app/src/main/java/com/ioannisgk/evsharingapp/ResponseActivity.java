package com.ioannisgk.evsharingapp;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioannisgk.evsharingapp.base.BaseActivity;

import butterknife.ButterKnife;

import static com.ioannisgk.evsharingapp.R.drawable.accepted_512;
import static com.ioannisgk.evsharingapp.R.drawable.error_512;
import static com.ioannisgk.evsharingapp.R.drawable.denied_512;
import static com.ioannisgk.evsharingapp.R.string.nameAcceptedActivity;
import static com.ioannisgk.evsharingapp.R.string.nameDeniedActivity;
import static com.ioannisgk.evsharingapp.R.string.nameErrorActivity;

public class ResponseActivity extends BaseActivity {
    TextView messageText;
    ImageView imageView;
    Button backtoProfile;
    Button history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_main).setVisible(false);
        navigationMenu.findItem(R.id.nav_register).setVisible(false);

        // Get activity resources

        messageText = (TextView) findViewById(R.id.messageTextView);
        imageView = (ImageView) findViewById(R.id.imageImageView);
        backtoProfile = (Button) findViewById(R.id.backButton);
        history = (Button) findViewById(R.id.historyButton);

        // Change response message and image according to server response

        String response = getIntent().getExtras().getString("response");

        if (response.equals("Accepted")) {
            messageText.setText(nameAcceptedActivity);
            imageView.setImageResource(accepted_512);

        } else if (response.equals("Denied")) {
            messageText.setText(nameDeniedActivity);
            imageView.setImageResource(denied_512);

        } else {
            messageText.setText(nameErrorActivity);
            imageView.setImageResource(error_512);

        }

        // Start ProfileActivity when clicking on back to profile

        backtoProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i1 = new Intent(ResponseActivity.this, ProfileActivity.class);
                startActivity(i1);
            }
        });

        // Start HistoryActivity when clicking on history button

        history.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i2 = new Intent(ResponseActivity.this, HistoryActivity.class);
                startActivity(i2);
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
        return R.id.nav_main;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}