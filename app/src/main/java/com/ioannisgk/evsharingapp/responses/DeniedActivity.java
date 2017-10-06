package com.ioannisgk.evsharingapp.responses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ioannisgk.evsharingapp.HistoryActivity;
import com.ioannisgk.evsharingapp.ProfileActivity;
import com.ioannisgk.evsharingapp.R;
import com.ioannisgk.evsharingapp.entities.User;

public class DeniedActivity extends AppCompatActivity {
    Button backtoProfile;
    Button history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denied);

        // Get activity resources

        backtoProfile = (Button) findViewById(R.id.backButton);
        history = (Button) findViewById(R.id.historyButton);

        // Start ProfileActivity when clicking on back to profile

        backtoProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i1 = new Intent(DeniedActivity.this, ProfileActivity.class);
                startActivity(i1);
            }
        });

        // Start HistoryActivity when clicking on history button

        history.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i2 = new Intent(DeniedActivity.this, HistoryActivity.class);
                startActivity(i2);
            }
        });
    }
}