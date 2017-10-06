package com.ioannisgk.evsharingapp;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.DateDialog;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.MyTextEncryptor;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.type;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    TextView profileMessage;
    EditText profileName;
    EditText profileDate;
    Button request;
    Button history;
    Button saveProfile;
    Switch switch1;
    Spinner genderSpinner;
    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get activity resources

        profileMessage = (TextView) findViewById(R.id.titleTextView);
        profileName = (EditText) findViewById(R.id.nameEditText);
        profileDate = (EditText) findViewById(R.id.dobEditText);
        request = (Button) findViewById(R.id.requestButton);
        history = (Button) findViewById(R.id.historyButton);
        saveProfile = (Button) findViewById(R.id.saveButton);
        switch1 = (Switch) findViewById(R.id.switch1);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);

        // Set values for genderSpinner options from gender_array

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);

        // Set spinner value to current user gender value
        if (Global.currentUser.getGender().equals("Female")) genderSpinner.setSelection(1);

        // Format date

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String myDate = dateFormat.format(Global.currentUser.getDob());

        // Display user details

        String message = "Welcome back " + Global.currentUser.getUsername();
        profileMessage.setText(message);
        profileName.setText(Global.currentUser.getName());
        profileDate.setText(myDate);

        // Make profile fields inactive and not editable
        alterProfileControls(false);

        // Start RequestActivity when clicking on request button

        request.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i1 = new Intent(ProfileActivity.this, RequestActivity.class);
                startActivity(i1);
            }
        });

        // Start HistoryActivity when clicking on history button

        history.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i2 = new Intent(ProfileActivity.this, HistoryActivity.class);
                startActivity(i2);
            }
        });

        // Saving profile when clicking on save profile

        saveProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String username = Global.currentUser.getUsername();
                final String password = Global.currentUser.getPassword();
                final String name = profileName.getText().toString();
                final String date = profileDate.getText().toString();

                // Validate input data from editing the profile
                boolean dataIsValid = Settings.validateProfileData(name, date, ProfileActivity.this);

                if (dataIsValid == true) {

                    // Format date

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date myDate = null;
                    try {
                        myDate = dateFormat.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Create current user object from the data entered in the registration form
                    User currentUser = new User(Global.currentUser.getId(), username, password, name, gender, myDate);

                    // Execute async task and pass current user object
                    new HttpRequestTask().execute(currentUser);

                    // Update the global user object

                    Global.currentUser.setName(name);
                    Global.currentUser.setGender(gender);
                    Global.currentUser.setDob(myDate);
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
                    alterProfileControls(true);

                } else if (switchState == false) {

                    switch1.setText("Edit mode disabled");
                    alterProfileControls(false);
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
                Boolean registered = restClient.updateUser(tokenInfo, currentUser);

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

                    Settings.showToast(getApplicationContext(), "Profile updated successfully");
                    alterProfileControls(false);

                } else if (registered == false) {
                    Settings.showDialogBox("Profile error", "Could not find user in the database", ProfileActivity.this);
                }

            } else {
                Settings.showDialogBox("Server error", "Could not connect to server", ProfileActivity.this);
            }
        }
    }

    // Create new date dialogue object and show the selected date in the text box

    public void onStart(){
        super.onStart();
        profileDate.setOnFocusChangeListener(new View.OnFocusChangeListener(){
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

    // Method to alter status of profile fields

    private void alterProfileControls(boolean status) {

        profileName.setClickable(status);
        profileName.setFocusable(status);
        profileName.setFocusableInTouchMode(status);

        profileDate.setClickable(status);
        profileDate.setFocusable(status);
        profileDate.setFocusableInTouchMode(status);

        // Disable dropdown menu from spinner

        if (status == false) {
            genderSpinner.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });

        // Enable dropdown menu from spinner

        } else if (status == true) {
            genderSpinner.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
        }

        saveProfile.setEnabled(status);
    }

    // Main menu dropdown

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu (menu);
        MenuItem item1 = menu.add(0, 0, Menu.NONE, "Request");
        MenuItem item2 = menu.add(0, 1, Menu.NONE, "History");
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
                Intent i1 = new Intent(this, RequestActivity.class);
                startActivity (i1);
                return true;
            case 1:
                Intent i2 = new Intent(this, HistoryActivity.class);
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