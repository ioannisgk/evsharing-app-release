package com.ioannisgk.evsharingapp;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.ioannisgk.evsharingapp.entities.User;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    TextView profileMessage;
    EditText profileName;
    EditText profileGender;
    EditText profileDate;
    Button request;
    Button history;
    Button edit;
    Switch switch1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get activity resources

        profileMessage = (TextView) findViewById(R.id.titleTextView);
        profileName = (EditText) findViewById(R.id.nameEditText);
        profileGender = (EditText) findViewById(R.id.genderEditText);
        profileDate = (EditText) findViewById(R.id.dobEditText);
        request = (Button) findViewById(R.id.requestButton);
        history = (Button) findViewById(R.id.historyButton);
        edit = (Button) findViewById(R.id.editButton);
        switch1 = (Switch) findViewById(R.id.switch1);

        // Get the current user object from the previous intent
        User theUser = (User) getIntent().getSerializableExtra("currentUser");

        // Format date

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String myDate = dateFormat.format(theUser.getDob());

        // Display user details

        String message = "Welcome back " + theUser.getUsername();
        profileMessage.setText(message);
        profileName.setText(theUser.getName());
        profileGender.setText(theUser.getGender());
        profileDate.setText(myDate);

        // Make profile fields inactive and not editable

        profileName.setKeyListener(null);
        profileGender.setKeyListener(null);
        profileDate.setKeyListener(null);

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
                Intent i4 = new Intent(this, MainActivity.class);
                startActivity (i4);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
