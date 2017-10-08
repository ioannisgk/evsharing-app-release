package com.ioannisgk.evsharingapp;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.DateDialog;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;
import com.ioannisgk.evsharingapp.utils.MyTextEncryptor;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText regName;
    EditText regDate;
    EditText regUsername;
    EditText regPassword;
    Button register;
    Spinner genderSpinner;
    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Add icon to action bar

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo_32x32);

        // Get activity resources

        regName = (EditText) findViewById(R.id.nameEditText);
        regDate = (EditText) findViewById(R.id.dobEditText);
        regUsername = (EditText) findViewById(R.id.usernameEditText);
        regPassword = (EditText) findViewById(R.id.passwordEditText);
        register = (Button) findViewById(R.id.registerButton);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);

        // Set values for genderSpinner options from gender_array

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);

        // Registration when clicking on Register

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = regName.getText().toString();
                final String date = regDate.getText().toString();
                final String username = regUsername.getText().toString();
                final String password = regPassword.getText().toString();

                // Validate input data from the registration form
                boolean dataIsValid = Settings.validateRegisterData(name, username, password, date, RegisterActivity.this);

                if (dataIsValid == true) {

                    // Format date

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date myDate = null;
                    try {
                        myDate = dateFormat.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Encrypt current password with text encryptor

                    MyTextEncryptor textEncryptor = new MyTextEncryptor();
                    String encryptedPassword = textEncryptor.encryptPassword(password);

                    // Create current user object from the data entered in the registration form
                    User currentUser = new User(username, encryptedPassword, name, gender, myDate);

                    // Execute async task and pass current user object
                    new HttpRequestTask().execute(currentUser);

                }
            }
        });
    }

    private class HttpRequestTask extends AsyncTask<User, Void, Boolean> {
        @Override
        protected Boolean doInBackground(User... params) {

            // User data entered in the registration form
            User currentUser = (User) params[0];

            // Try to connect to server and use the web service

            try {

                // Create new SpringRestClient object
                SpringRestClient restClient = new SpringRestClient();

                // Get an access token which will be send with each request
                AuthTokenInfo tokenInfo = restClient.sendTokenRequest();

                // Login via web service and retrieve user object
                Boolean registered = restClient.createUser(tokenInfo, currentUser);

                return registered;

            } catch (RuntimeException e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean registered) {

            if (registered != null) {
                if (registered == true) {

                    Settings.showDialogBoxSuccess("Success", "Registration was successful", RegisterActivity.this);

                    // Start MainActivity for the user to login

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    RegisterActivity.this.startActivity(intent);

                } else if (registered == false) {
                    Settings.showDialogBox("Register error", "Could not complete registration (email already registered?)", RegisterActivity.this);
                }

            } else {
                Settings.showDialogBox("Server error", "Could not connect to server", RegisterActivity.this);
            }
        }
    }

    // Create new date dialogue object and show the selected date in the text box

    public void onStart(){
        super.onStart();
        regDate.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View view, boolean hasfocus){
                if (hasfocus) {
                    DateDialog dialog = new DateDialog(view);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "DatePicker");
                }
            }
        });
    }

    // Method to populate gender options

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (pos) {
            case 0:
                gender = "Male";
                break;
            case 1:
                gender = "Female";
                break;
        }
    }

    // Method needed for dropdown list

    public void onNothingSelected(AdapterView<?> parent) {

    }

    // Main menu dropdown

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu (menu);
        MenuItem item1 = menu.add(0, 0, Menu.NONE, "User login");
        MenuItem item2 = menu.add(0, 1, Menu.NONE, "About us");
        return true;
    }

    // Start new activity depending main menu on selection

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case 0:
                Intent i1 = new Intent(this, MainActivity.class);
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