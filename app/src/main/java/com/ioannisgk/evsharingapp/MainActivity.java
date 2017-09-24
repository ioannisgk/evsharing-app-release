package com.ioannisgk.evsharingapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get activity resources

        final EditText loginUsername = (EditText) findViewById(R.id.usernameEditText);
        final EditText loginPassword = (EditText) findViewById(R.id.passwordEditText);
        final TextView register = (TextView) findViewById(R.id.registerTextView);
        final Button login = (Button) findViewById(R.id.loginButton);

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

                    // Execute async task and pass username and password typed by the user
                    new HttpRequestTask().execute(username, password);

                } else {

                    // Show error message
                    showDialogBox("Login error", "Empty fields detected");
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
                User theUser = restClient.loginUser(tokenInfo, username, password);

                return theUser;

            } catch (RuntimeException e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User theUser) {

            if (theUser != null) {
                if (theUser.getRequestStatus().equals("Success")) {

                    //

                    Global.username = theUser.getUsername();
                    Global.name = theUser.getName();
                    Global.gender = theUser.getGender();
                    Global.dob = theUser.getDob();

                    // Start ProfileActivity and kill MainLoginActivity

                    //Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    //MainActivity.this.startActivity(intent);
                    //finish();

                    System.out.println(Global.username);
                    System.out.println(Global.name);
                    System.out.println(Global.gender);
                    System.out.println(Global.dob);

                } else if (theUser.getRequestStatus().equals("Invalid login details")) {
                    showDialogBox("Login error", "Invalid login details");
                }

            } else {
                showDialogBox("Server error", "Could not connect to server");
            }
        }
    }

    // Show dialogue box message

    private void showDialogBox(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title).setMessage(message).setNegativeButton("Retry", null).create().show();
    }

    // Main menu dropdown

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu (menu);
        MenuItem item1 = menu.add(0, 0, Menu.NONE, "User registration");
        MenuItem item2 = menu.add(0, 1, Menu.NONE, "About us");
        return true;
    }

    // Start new activity depending main menu on selection

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent i1 = new Intent(this, RegisterActivity.class);
                startActivity (i1);
                return true;
            case 1:
                Intent i2 = new Intent(this, AboutActivity.class);
                startActivity (i2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}