package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PassengersLocation extends AppCompatActivity {

    private Switch switch1;
    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Button btn;
    private LatLng currentlocation;
    private double lat;
    private double lng;
    private String timestamp;
    private boolean switchOnOff;
    public static final String SWITCH1 = "switch1";
    public static final String SHARED_PREFS = "sharedPrefs";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationCallback mLocationCallback;
    private FirebaseUser user;
    private String userID;


//added userid to current location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengers_location);
        sharedPreferences = getSharedPreferences(" ", MODE_PRIVATE);
        switch1 = (Switch) findViewById(R.id.switch1);
    }

    // Fetch the stored data in onResume() Because this is what will be called when the app opens again
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        switchOnOff = sharedPreferences.getBoolean(SWITCH1, false);
        switch1.setChecked(switchOnOff);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getBoolean(SWITCH1, true)) {
            //editor.putBoolean(SWITCH1, true);
            //editor.commit();
            getLocation();
        }
        else {
            check();
        }

    }

    public void check() {
        switch1 = (Switch) findViewById(R.id.switch1);
        sharedPreferences = getSharedPreferences(" ", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        switch1.setChecked(sharedPreferences.getBoolean(SWITCH1, false));
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    editor.putBoolean(SWITCH1, false);
                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                }
                else {
                    getLocation();
                }
            }
        });
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "No permission", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(PassengersLocation.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = new FusedLocationProviderClient(getApplicationContext());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lat = locationResult.getLastLocation().getLatitude();
                lng = locationResult.getLastLocation().getLongitude();
                currentlocation = new LatLng(lat, lng);
                saveCurrentLocation(currentlocation);

            }


        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        /*
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lat = locationResult.getLastLocation().getLatitude();
                lng = locationResult.getLastLocation().getLongitude();
                currentlocation = new LatLng(lat, lng);
                saveCurrentLocation(currentlocation);

            }


        }, null);

         */
    }

    public void saveCurrentLocation(LatLng latlng) {
        //DatabaseReference fireDB = FirebaseDatabase.getInstance().getReference().child("CurrentLocation");
        //String locationID = fireDB.push().getKey();

        //FirebaseUser location = mAuth1.getCurrentUser();
        //timestamp = "25/12/2021"; //date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
        timestamp = simpleDateFormat.format(calendar.getTime());
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        CurrentLocation locationObj = new CurrentLocation(lat, lng, timestamp, userID);


        FirebaseDatabase.getInstance().getReference().child("PassengerLocations")
                .push()
                .setValue(locationObj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(PassengersLocation.this, "Location successful", Toast.LENGTH_LONG).show();
                    /*
                    switch1.setChecked(sharedPreferences.getBoolean(SWITCH1, false));
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                    editor.putBoolean(SWITCH1, false);
                    locationManager.removeUpdates(locationListener);
                    }

            }
        });

                     */

                } else {
                    Toast.makeText(PassengersLocation.this, "Failed!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }




    // Store the data in the SharedPreference in the onPause() method
    // When the user closes the application onPause() will be called and data will be stored
    @Override
    protected void onPause() {
        super.onPause();

        // Creating a shared pref object with a file name "MySharedPref" in private mode

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SWITCH1, switch1.isChecked());

        editor.apply();
        editor.commit();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();

    }
}


