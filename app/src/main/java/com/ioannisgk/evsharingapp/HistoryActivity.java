package com.ioannisgk.evsharingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);







    }

    // Main menu dropdown

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu (menu);
        MenuItem item1 = menu.add(0, 0, Menu.NONE, "Profile");
        MenuItem item2 = menu.add(0, 1, Menu.NONE, "Request");
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
                Intent i1 = new Intent(this, ProfileActivity.class);
                startActivity (i1);
                return true;
            case 1:
                Intent i2 = new Intent(this, RequestActivity.class);
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