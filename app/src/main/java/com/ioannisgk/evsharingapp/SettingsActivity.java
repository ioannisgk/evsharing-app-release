package com.ioannisgk.evsharingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;

import butterknife.ButterKnife;

import static com.ioannisgk.evsharingapp.R.drawable.accepted_512;
import static com.ioannisgk.evsharingapp.R.drawable.ic_offline_24dp;
import static com.ioannisgk.evsharingapp.R.drawable.ic_online_24dp;
import static com.ioannisgk.evsharingapp.R.string.nameAcceptedActivity;
import static com.ioannisgk.evsharingapp.R.string.offlineSettingsActivity;
import static com.ioannisgk.evsharingapp.R.string.onlineSettingsActivity;

public class SettingsActivity extends BaseActivity {
    TextView messageText;
    ImageView imageView;
    EditText ipaddressSetting;
    EditText portnumberSetting;
    Button loadDefaults;
    Button saveSettings;
    Button back;
    Switch switch1;

    // Shared preferences for saving/loading settings
    SharedPreferences sharedpreferences;

    // Attributes for TCP connection

    String incomingMessage;
    public Socket sender;
    public PrintWriter out;
    public BufferedReader in;

    // Listener for TCP message

    class SocketListener implements Runnable {

        public void run() {

            try {

                sender = new Socket(Global.ipAddress, Global.portNumber);
                out = new PrintWriter(sender.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(sender.getInputStream()));

                while (true) {

                    // Receive message from server
                    incomingMessage = in.readLine();

                }
            } catch (IOException e) {

                //e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();

        if (Global.currentUser != null) {
            navigationMenu.findItem(R.id.nav_main).setVisible(false);
            navigationMenu.findItem(R.id.nav_register).setVisible(false);
        } else {
            navigationMenu.findItem(R.id.nav_profile).setVisible(false);
            navigationMenu.findItem(R.id.nav_request).setVisible(false);
            navigationMenu.findItem(R.id.nav_history).setVisible(false);
            navigationMenu.findItem(R.id.nav_logout).setVisible(false);
        }

        // Get activity resources

        ipaddressSetting = (EditText) findViewById(R.id.ipaddressEditText);
        portnumberSetting = (EditText) findViewById(R.id.portnumberEditText);
        messageText = (TextView) findViewById(R.id.statusTextView);
        imageView = (ImageView) findViewById(R.id.imageImageView);
        back = (Button) findViewById(R.id.backButton);
        loadDefaults = (Button) findViewById(R.id.defaultsButton);
        saveSettings = (Button) findViewById(R.id.saveButton);
        switch1 = (Switch) findViewById(R.id.switch1);

        // Set initial status and text for ip address and port number controls

        if (Global.currentUser != null) {
            back.setText("Back to Profile");
            switch1.setEnabled(false);
        } else {
            back.setText("Back to Login");
            switch1.setEnabled(true);
        }

        // Set default values to settings

        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        ipaddressSetting.setText(sharedpreferences.getString("ipAddress", "178.62.121.237"));
        portnumberSetting.setText(sharedpreferences.getString("portNumber", "8080"));

        // Start TCP listener in a new thread

        Thread listenerThread = new Thread(new SocketListener());
        listenerThread.start();

        // Wait 1.5 seconds to allow connection and then send test message
        // If there is a TCP connection send test message in a new thread

        new CountDownTimer(1500, 1000) {

            public void onFinish() {

                if (out != null) {

                    new Thread() {
                        public void run() {
                            out.println("Test");
                        }
                    }.start();

                    // Change message and image according to server response

                    messageText.setText(onlineSettingsActivity);
                    imageView.setImageResource(ic_online_24dp);

                } else {

                    // Change message and image according to server response

                    messageText.setText(offlineSettingsActivity);
                    imageView.setImageResource(ic_offline_24dp);
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {

            }
        }.start();

        // Make settings fields inactive and not editable
        alterSettingsControls(false);

        // Start corresponding activity (if logged in or logged out) when clicking on back button

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (Global.currentUser != null) {
                    Intent i1 = new Intent(SettingsActivity.this, ProfileActivity.class);
                    startActivity(i1);
                } else {
                    Intent i2 = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(i2);
                }
            }
        });

        // Load default settings when clicking on load defaults button

        loadDefaults.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Update settings control texts

                ipaddressSetting.setText("178.62.121.237");
                portnumberSetting.setText("8080");

            }
        });

        // Saving settings when clicking on save settings

        saveSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String myIpAddress = ipaddressSetting.getText().toString();
                final String myPortNumber = portnumberSetting.getText().toString();

                // Validate input data from editing the profile
                boolean dataIsValid = Settings.validateSettingsData(myIpAddress, myPortNumber, SettingsActivity.this);

                if (dataIsValid == true) {

                    // Save settings values to shared preferences

                    sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    // Save key value pairs and commit

                    editor.putString("ipAddress", myIpAddress);
                    editor.putString("portNumber", myPortNumber);
                    editor.commit();

                    // Update settings control texts

                    ipaddressSetting.setText(sharedpreferences.getString("ipAddress", "178.62.121.237"));
                    portnumberSetting.setText(sharedpreferences.getString("portNumber", "8080"));

                    // Update global settings variables

                    Global.ipAddress = sharedpreferences.getString("ipAddress", "178.62.121.237");
                    Global.portNumber = Integer.parseInt(sharedpreferences.getString("portNumber", "8080"));

                    Settings.showToast(getApplicationContext(), "Settings updated successfully");
                }
            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // check current state of a switch
                Boolean switchState = switch1.isChecked();

                if (switchState == true) {

                    switch1.setText("Edit mode enabled");
                    alterSettingsControls(true);

                } else if (switchState == false) {
                    switch1.setText("Edit mode disabled");
                    alterSettingsControls(false);
                }
            }
        });
    }

    // Method to alter status of settings fields

    private void alterSettingsControls(boolean status) {

        if (status == true) {
            ipaddressSetting.setTextColor(Color.parseColor("#202020"));
            portnumberSetting.setTextColor(Color.parseColor("#202020"));
        } else {
            ipaddressSetting.setTextColor(Color.parseColor("#C0C0C0"));
            portnumberSetting.setTextColor(Color.parseColor("#C0C0C0"));
        }

        ipaddressSetting.setClickable(status);
        ipaddressSetting.setFocusable(status);
        ipaddressSetting.setFocusableInTouchMode(status);

        portnumberSetting.setClickable(status);
        portnumberSetting.setFocusable(status);
        portnumberSetting.setFocusableInTouchMode(status);

        loadDefaults.setEnabled(status);
        saveSettings.setEnabled(status);
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
        return R.id.nav_settings;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}