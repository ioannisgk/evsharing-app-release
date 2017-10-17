package com.ioannisgk.evsharingapp;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {

    // Attributes for TCP connection

    String incomingMessage;
    public Socket sender;
    public PrintWriter out;
    public BufferedReader in;
    String ipAddress = "178.62.121.237";
    int portNumber = 5566;

    // Listener for TCP message

    class SocketListener implements Runnable {

        public void run() {

            try {

                sender = new Socket(ipAddress, portNumber);
                out = new PrintWriter(sender.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(sender.getInputStream()));

                while (true) {

                    // Receive message from server
                    incomingMessage = in.readLine();

                }
            } catch (IOException e) {

                e.printStackTrace();
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

        // Start TCP listener in a new thread

        Thread listenerThread = new Thread(new SocketListener());
        listenerThread.start();

        // If there is a TCP connection send test message in a new thread

        System.out.println("----" + incomingMessage);
        if (out != null) {

            new Thread() {
                public void run() {
                    out.println("Test");
                }
            }.start();
            System.out.println("----ONLINE");

        } else {

            System.out.println("----OFFLINE");
        }










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