package com.ioannisgk.evsharingapp;

import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ioannisgk.evsharingapp.base.BaseActivity;
import com.ioannisgk.evsharingapp.entities.Station;
import com.ioannisgk.evsharingapp.utils.AuthTokenInfo;
import com.ioannisgk.evsharingapp.utils.Settings;
import com.ioannisgk.evsharingapp.utils.SpringRestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ioannisgk.evsharingapp.R.drawable.ic_offline_24dp;
import static com.ioannisgk.evsharingapp.R.drawable.ic_online_24dp;
import static com.ioannisgk.evsharingapp.R.string.offlineSettingsActivity;
import static com.ioannisgk.evsharingapp.R.string.onlineSettingsActivity;

public class MapActivity extends BaseActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    // Shared preferences for saving/loading settings
    SharedPreferences sharedpreferences;

    private List<Station> currentStations;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private int totalCounter = 0;
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private int[] stationIDs = {-1, -1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Setup toolbar and hide navigation menu options

        ButterKnife.bind(this);
        setupToolbar();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_main).setVisible(false);
        navigationMenu.findItem(R.id.nav_register).setVisible(false);

        // Execute async task to get station objects
        new HttpRequestTask().execute();

        // Get location permissions
        getLocationPermissions();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        Settings.showToast(getApplicationContext(), "Map is ready");
        mMap = googleMap;

        // Wait 1.5 seconds to allow connection and then create and show markers on map

        new CountDownTimer(1500, 1000) {

            public void onFinish() {

                if (mLocationPermissionsGranted) {

                    // Get device current location
                    getDeviceLocation();

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    // Show current location and hide map toolbar

                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.getUiSettings().setMapToolbarEnabled(false);

                    // If current stations are retrieved create and show markers on map

                    if (currentStations != null) {
                        if (currentStations.size() != 0) {

                            // Create markers bounds builder to update camera

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();

                            for (int i = 0; i < currentStations.size(); ++i) {

                                // Iterate current stations list

                                double currentLatitude = currentStations.get(i).getLatitude();
                                double currentLongitude = currentStations.get(i).getLongitude();
                                String currentTitle = currentStations.get(i).getName();
                                LatLng currentPosition = new LatLng (currentLatitude, currentLongitude);

                                // Add markers positions
                                builder.include(currentPosition);

                                // Create and show markers on map

                                Marker mPosition = mMap.addMarker(new MarkerOptions().position(currentPosition).title(currentTitle));
                                mPosition.setTag(false);
                                mMarkerArray.add(mPosition);
                            }

                            // Create bounds based on markers positions and update camera

                            LatLngBounds bounds = builder.build();
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                            mMap.animateCamera(cu);

                        } else {
                            Settings.showDialogBox("Stations error", "Could not find stations in the database", MapActivity.this);
                        }
                    } else {
                        Settings.showDialogBox("Stations error", "Could not find stations in the database", MapActivity.this);
                    }
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {

            }
        }.start();

        // Create click listener for markers
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        // Retrieve status data from the marker
        boolean selected = (boolean) marker.getTag();

        marker.setTag(true);
        totalCounter = totalCounter + 1;

        if ((totalCounter % 2) == 0 ) {

            int i = 0;
            for (Marker currentMarker : mMarkerArray) {

                boolean currentStatus = (boolean) currentMarker.getTag();
                if (currentStatus == true) {

                    // If market tag is true then iterate current stations and save its id to array

                    for (int j = 0; j < currentStations.size(); j++) {
                        if (currentStations.get(j).getName().equals(currentMarker.getTitle())) {
                            stationIDs[i] = currentStations.get(j).getId();
                            i++;
                        }
                    }
                }
            }
            Settings.showToast(getApplicationContext(), "From station: " + marker.getTitle());

        } else {

            for (Marker currentMarker : mMarkerArray) {
                currentMarker.setTag(false);
            }
            marker.setTag(true);
            Settings.showToast(getApplicationContext(), "Destination: " + marker.getTitle());
        }

        return true;
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

                    // Retrieve stations as a list

                    if (currentStations != null) currentStations.clear();
                    currentStations = new ArrayList<Station>(theStations);

                } else if (theStations.size() == 0) {
                    Settings.showDialogBox("Stations error", "Could not find stations in the database", MapActivity.this);
                }

            } else {
                Settings.showDialogBox("Server error", "Could not connect to server", MapActivity.this);
            }
        }
    }

    // Get device current location

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            Location currentLocation = (Location) task.getResult();

                            if (currentLocation == null) {

                                // Provide default current location

                                currentLocation = new Location("");
                                currentLocation.setLatitude(51.454514);
                                currentLocation.setLongitude(-2.587910);
                            }

                        } else {
                            Settings.showToast(getApplicationContext(), "Could not get current location");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Settings.showToast(getApplicationContext(), "Could not get current location");
        }
    }

    // Initialize map

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    // Get location permissions

    private void getLocationPermissions(){

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationPermissionsGranted = true;
                initMap();

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Check location permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){

            case LOCATION_PERMISSION_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){

                            // Permissions failed
                            mLocationPermissionsGranted = false;

                            return;
                        }
                    }

                    // Permissions granted
                    mLocationPermissionsGranted = true;

                    // Initialize map
                    initMap();
                }
            }
        }
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {

        if ((stationIDs[0] >= 0) && (stationIDs[1] >= 0)) {

            System.out.println("----IDs:" + stationIDs[0]);
            System.out.println("----IDs:" + stationIDs[1]);

            // Set selected stations ids to preferences

            sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();

            // Save key value pairs and commit

            editor.putString("startStationID", Integer.toString(stationIDs[0]));
            editor.putString("finishStationID", Integer.toString(stationIDs[1]));
            editor.commit();
        }

        startActivity(new Intent(this, RequestActivity.class));
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        return R.id.nav_main;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}