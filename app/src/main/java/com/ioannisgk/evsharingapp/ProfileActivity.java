package com.ioannisgk.evsharingapp;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
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

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.DateDialog;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;

import java.text.ParseException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
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

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_main).setVisible(false);
        navigationMenu.findItem(R.id.nav_register).setVisible(false);

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

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        Snackbar.make(view, "Hello Snackbar!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
        return R.id.nav_profile;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}