package com.ioannisgk.evsharingapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.MyTextEncryptor;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;

import java.io.File;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_profile).setVisible(false);
        navigationMenu.findItem(R.id.nav_request).setVisible(false);
        navigationMenu.findItem(R.id.nav_history).setVisible(false);
        navigationMenu.findItem(R.id.nav_logout).setVisible(false);

        // Get activity resources

        final EditText loginUsername = (EditText) findViewById(R.id.usernameEditText);
        final EditText loginPassword = (EditText) findViewById(R.id.passwordEditText);
        final Button login = (Button) findViewById(R.id.loginButton);
        final Button register = (Button) findViewById(R.id.registerButton);

        // Get file directory and create or load database with user data

        File myFile = getFilesDir();
        Global.myFile = myFile;
        Global.myDB = Settings.createDB(Global.myDB, Global.myFile);

        // Start RegisterActivity when clicking on Register

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                MainActivity.this.startActivity(registerIntent);
            }
        });

        // Authentication when clicking on Login

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = loginUsername.getText().toString();
                final String password = loginPassword.getText().toString();

                if ((!username.isEmpty()) && (!password.isEmpty())) {

                    // Encrypt current password with text encryptor

                    MyTextEncryptor textEncryptor = new MyTextEncryptor();
                    String encryptedPassword = textEncryptor.encryptPassword(password);

                    // Execute async task and pass username and password typed by the user
                    new HttpRequestTask().execute(username, encryptedPassword);

                } else {
                    Settings.showDialogBox("Login error", "Empty fields detected", MainActivity.this);
                }
            }
        });

        loginUsername.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                loginUsername.setText("");
            }
        });

        loginPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                loginPassword.setText("");
            }
        });
    }

    private class HttpRequestTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {

            // Username and password entered in the login form

            String username = params[0];
            String password = params[1];

            // Try to connect to server and use the web service

            try {

                // Create new SpringRestClient object
                SpringRestClient restClient = new SpringRestClient();

                // Get an access token which will be send with each request
                AuthTokenInfo tokenInfo = restClient.sendTokenRequest();

                // Login via web service and retrieve user object
                Global.currentUser = restClient.loginUser(tokenInfo, username, password);

                return Global.currentUser;

            } catch (RuntimeException e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User theUser) {

            if (theUser != null) {
                if (theUser.getRequestStatus().equals("Success")) {

                    // Start ProfileActivity and pass the user object

                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    MainActivity.this.startActivity(intent);

                    // Kill MainActivity
                    finish();

                } else if (theUser.getRequestStatus().equals("Invalid login details")) {
                    Settings.showDialogBox("Login error", "Invalid login details", MainActivity.this);
                }

            } else {
                Settings.showDialogBox("Server error", "Could not connect to server", MainActivity.this);
            }
        }
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
            case R.id.action_settings:
                Intent i1 = new Intent(this, SettingsActivity.class);
                startActivity (i1);
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