package com.example.max.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationListener locationListener;
    LocationManager locationManager;
    Button callUber;
    boolean isRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        callUber = (Button) findViewById(R.id.callUber);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ParseQuery<ParseObject> query = new ParseQuery<>("Requests");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0){
                        callUber.setText("Cancel Uber");
                        isRequested = true;
                    }else{
                        callUber.setText("Call An Uber");
                        isRequested = false;
                    }
                }else{
                    e.printStackTrace();
                }
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(Context.LOCATION_SERVICE, 0, 0, locationListener);
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (Build.VERSION.SDK_INT <23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }else{

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateMap(location);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }
        }

    }

    public void updateMap(Location location){

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));


    }

    public void callUber(View v){
        if (isRequested) {



        }else{

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                if (location!= null) {
                    ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                    ParseObject object = new ParseObject("Requests");
                    object.put("username", ParseUser.getCurrentUser().getUsername());
                    object.put("location", geoPoint);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null){
                                callUber.setText("Cancel Uber");
                                isRequested = true;

                            }else{
                                Toast.makeText(getApplicationContext(), "Something went wrong. Please Try Again", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });

                }else{
                    Toast.makeText(this, "Location not available. Please Try Again", Toast.LENGTH_SHORT).show();
                }

            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }



        }
    }

    public void logout(View v){

        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e== null){

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }else{
                    e.printStackTrace();
                }
            }
        });
//
//        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
//        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> objects, ParseException e) {
//                if (e==null){
//                    if (objects.size()>0){
//                        for (ParseObject object: objects){
//                            object.deleteInBackground();
//                        }
//
//                    }
//                    ParseUser.logOutInBackground(new LogOutCallback() {
//                        @Override
//                        public void done(ParseException e) {
//                            if (e== null){
//
//                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                                startActivity(i);
//                            }else{
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }else{
//                    e.printStackTrace();
//                }
//            }
//        });


    }


}
