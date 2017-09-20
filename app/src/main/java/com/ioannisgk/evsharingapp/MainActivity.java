package com.ioannisgk.evsharingapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;


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

                new HttpRequestTask().execute();

                System.out.println("Button clicked");

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

    private class HttpRequestTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... params) {

            System.out.println("OUT1: ");
            SpringRestClient restClient = new SpringRestClient();
            User theUser = restClient.login();
            System.out.println("OUT1:1 ");
            System.out.println("OUT2: " + theUser);
            return theUser;
        }

        @Override
        protected void onPostExecute(User theUser) {

            System.out.println("OUT2: " + theUser);
        }

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