package com.ioannisgk.evsharingapp;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.entities.Station;
import com.ioannisgk.evsharingapp.responses.AcceptedActivity;
import com.ioannisgk.evsharingapp.responses.DeniedActivity;
import com.ioannisgk.evsharingapp.responses.ErrorActivity;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.Global;
import com.ioannisgk.evsharingapp.utils.MyTextEncryptor;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;
import com.ioannisgk.evsharingapp.utils.TimeDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;

public class RequestActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    EditText profileName;
    EditText requestTime;
    Button sendRequest;
    Button backtoProfile;
    Spinner startStationSpinner;
    Spinner finishStationSpinner;

    ArrayAdapter<String> adapterStartStation;
    ArrayAdapter<String> adapterFinishStation;

    // Arrays with names of stations

    String startStations[];
    String finishStations[];
    String selectedStartStationName;
    String selectedFinishStationName;

    // Arrays with ids of stations

    int startStationsIDs[];
    int finishStationsIDs[];

    int index = 0;
    int selectedStartStationID;
    int selectedFinishStationID;

    // Attributes for TCP connection

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
                        String incomingMessage = in.readLine();

                        // Start response activities based on the server response and pass the user object

                        if (incomingMessage != null) {
                            if (incomingMessage.equals("Accepted")) {

                                String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                                Settings.updateDB(Global.myDB, Global.myFile, currentDate, requestTime.getText().toString(),
                                        selectedStartStationName, selectedFinishStationName, incomingMessage);

                                out.println("bye");
                                Intent intent = new Intent(RequestActivity.this, AcceptedActivity.class);
                                startActivity(intent);
                                finish();

                            } else if (incomingMessage.equals("Denied")) {

                                String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                                Settings.updateDB(Global.myDB, Global.myFile, currentDate, requestTime.getText().toString(),
                                        selectedStartStationName, selectedFinishStationName, incomingMessage);

                                out.println("bye");
                                Intent intent = new Intent(RequestActivity.this, DeniedActivity.class);
                                startActivity(intent);
                                finish();

                            } else {

                                incomingMessage = "Server error";
                                String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                                Settings.updateDB(Global.myDB, Global.myFile, currentDate, requestTime.getText().toString(),
                                        selectedStartStationName, selectedFinishStationName, incomingMessage);

                                out.println("bye");
                                Intent intent = new Intent(RequestActivity.this, ErrorActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_main).setVisible(false);
        navigationMenu.findItem(R.id.nav_register).setVisible(false);

        // Get activity resources

        profileName = (EditText) findViewById(R.id.nameEditText);
        requestTime = (EditText) findViewById(R.id.timeEditText);
        sendRequest = (Button) findViewById(R.id.requestButton);
        backtoProfile = (Button) findViewById(R.id.backButton);
        startStationSpinner = (Spinner) findViewById(R.id.startStationSpinner);
        finishStationSpinner = (Spinner) findViewById(R.id.finishStationSpinner);

        // Display current username
        profileName.setText(Global.currentUser.getUsername());

        // Execute async task to get station objects
        new HttpRequestTask().execute();

        // Start ProfileActivity when clicking on back to profile

        backtoProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i1 = new Intent(RequestActivity.this, ProfileActivity.class);
                startActivity(i1);
            }
        });

        // Start TCP listener in a new thread

        Thread listenerThread = new Thread(new SocketListener());
        listenerThread.start();

        // Sending request when clicking on send request

        sendRequest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final int userId = Global.currentUser.getId();
                final String time = requestTime.getText().toString();

                // Validate input data from editing the profile
                boolean dataIsValid = Settings.validateRequestData(selectedStartStationID, selectedFinishStationID, time, RequestActivity.this);

                if (dataIsValid == true) {

                    // Send TCP message that contains user id, start station id, finish station id and time
                    final String message = userId + " " + selectedStartStationID + " " + selectedFinishStationID + " " + time;

                    // Encrypt message string with text encryptor

                    MyTextEncryptor textEncryptor = new MyTextEncryptor();
                    final String encryptedMessage = textEncryptor.encryptPassword(message);

                    // If there is a TCP connection with the server send encrypted message in a new thread

                    if (out != null) {

                        new Thread() {
                            public void run() {
                                out.println(encryptedMessage);
                            }
                        }.start();

                    } else {

                        String incomingMessage = "Server offline";
                        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                        String currentTime = new SimpleDateFormat("hh:mm").format(new Date());

                        Settings.updateDB(Global.myDB, Global.myFile, currentDate, requestTime.getText().toString(),
                                selectedStartStationName, selectedFinishStationName, incomingMessage);

                        Intent intent = new Intent(RequestActivity.this, ErrorActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    // Create new date dialogue object and show the selected date in the text box

    public void onStart(){
        super.onStart();
        requestTime.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View view, boolean hasfocus){
                if (hasfocus) {
                    TimeDialog dialog = new TimeDialog(view);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "TimePicker");
                }
            }
        });
    }

    private class HttpRequestTask extends AsyncTask<Void, Void, List<Station>> {
        @Override
        protected List<Station> doInBackground(Void... params) {

            // Try to connect to server and use the web service

            try {

                // Create new SpringRestClient object
                SpringRestClient restClient = new SpringRestClient();

                // Get an access token which will be send with each request
                AuthTokenInfo tokenInfo = restClient.sendTokenRequest();

                // Login via web service and retrieve station list
                List<Station> theStations = restClient.getStations(tokenInfo);

                return theStations;

            } catch (RuntimeException e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Station> theStations) {

            if (theStations != null) {
                if (theStations.size() != 0) {

                    // Create arrays with specific length

                    startStations = new String[theStations.size()];
                    finishStations = new String[theStations.size()];
                    startStationsIDs = new int[theStations.size()];
                    finishStationsIDs = new int[theStations.size()];

                    // Iterate list and populate arrays

                    for (int i = 0; i < theStations.size(); ++i) {

                        // Arrays of names of stations

                        startStations[i] = theStations.get(i).getName();
                        finishStations[i] = theStations.get(i).getName();

                        // Arrays of ids of stations

                        startStationsIDs[i] = theStations.get(i).getId();
                        finishStationsIDs[i] = theStations.get(i).getId();
                    }

                    // Retrieve stations and populate spinners from arrays
                    populateSpinners();

                } else if (theStations.size() == 0) {
                    Settings.showDialogBox("Stations error", "Could not find stations in the database", RequestActivity.this);
                }

            } else {
                Settings.showDialogBox("Server error", "Could not connect to server", RequestActivity.this);
            }
        }
    }

    // Method to populate spinners

    public void populateSpinners() {

        // Assign names of stations array to start stations spinner

        adapterStartStation = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, startStations);
        adapterStartStation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startStationSpinner.setAdapter(adapterStartStation);
        startStationSpinner.setOnItemSelectedListener(this);

        // Assign names of stations array to finish stations spinner

        adapterFinishStation = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, finishStations);
        adapterFinishStation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        finishStationSpinner.setAdapter(adapterFinishStation);
        finishStationSpinner.setOnItemSelectedListener(this);

    }

    // Change selected stations ids according to spinner value

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId())
        {
            case R.id.startStationSpinner:
                index = pos;
                selectedStartStationID = startStationsIDs[index];
                selectedStartStationName = startStations[index];
                break;

            case R.id.finishStationSpinner:
                index = pos;
                selectedFinishStationID = finishStationsIDs[index];
                selectedFinishStationName = finishStations[index];
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_request;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}