package com.ioannisgk.evsharingapp;

import android.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.ioannisgk.evsharingapp.entities.Station;
import com.ioannisgk.evsharingapp.entities.User;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.DateDialog;
import com.ioannisgk.evsharingapp.utils.MyTextEncryptor;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;
import com.ioannisgk.evsharingapp.utils.TimeDialog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RequestActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
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

                        // *** Need to show accepted or not accepted in another way

                        System.out.println(incomingMessage);

                    }
                } catch (IOException e) {

                    Settings.showDialogBox("Network error", "Received corrupted data", RequestActivity.this);
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Get activity resources

        profileName = (EditText) findViewById(R.id.nameEditText);
        requestTime = (EditText) findViewById(R.id.timeEditText);
        sendRequest = (Button) findViewById(R.id.requestButton);
        backtoProfile = (Button) findViewById(R.id.backButton);
        startStationSpinner = (Spinner) findViewById(R.id.startStationSpinner);
        finishStationSpinner = (Spinner) findViewById(R.id.finishStationSpinner);

        // Get the current user object from the previous intent
        final User theUser = (User) getIntent().getSerializableExtra("currentUser");

        // Display current username
        profileName.setText(theUser.getUsername());

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
                final int userId = theUser.getId();
                final String time = requestTime.getText().toString();

                // Validate input data from editing the profile
                boolean dataIsValid = Settings.validateRequestData(selectedStartStationID, selectedFinishStationID, time, RequestActivity.this);

                if (dataIsValid == true) {

                    // Send TCP message that contains user id, start station id, finish station id and time

                    final String message = userId + " " + selectedStartStationID + " " + selectedFinishStationID + " " + time;

                    // Encrypt message string with text encryptor

                    MyTextEncryptor textEncryptor = new MyTextEncryptor();
                    String encryptedMessage = textEncryptor.encryptPassword(message);

                    // Send the message in a new thread

                    // *** Need to send encrypted message

                    new Thread() {
                        public void run() {
                            out.println(message);
                        }
                    }.start();
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
                    Settings.showToast(getApplicationContext(), "Stations retrieved successfully");

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
        startStationSpinner.setAdapter(adapterStartStation);
        startStationSpinner.setOnItemSelectedListener(this);

        // Assign names of stations array to finish stations spinner

        adapterFinishStation = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, finishStations);
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
                break;

            case R.id.finishStationSpinner:
                index = pos;
                selectedFinishStationID = finishStationsIDs[index];
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
        MenuItem item1 = menu.add(0, 0, Menu.NONE, "Profile");
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
                Intent i1 = new Intent(this, ProfileActivity.class);
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