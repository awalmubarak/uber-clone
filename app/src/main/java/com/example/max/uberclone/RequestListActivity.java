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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RequestListActivity extends AppCompatActivity {

    ArrayAdapter adapter;
    ArrayList<String> requests;
    ArrayList<Double> requestsLatitudes;
    ArrayList<Double> requestsLongitudes;
    ArrayList<String> usernames;
    ListView requestList;
    LocationListener locationListener;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);
        setTitle("Nearby Requests");
        requestList = (ListView) findViewById(R.id.requestList);

        requests = new ArrayList<>();
        requestsLatitudes = new ArrayList<>();
        requestsLongitudes = new ArrayList<>();
        usernames = new ArrayList<>();

        requests.add("Getting Nearby Requests");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requests);
        requestList.setAdapter(adapter);
        requestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(RequestListActivity.this, DriverLocationActivity.class);
                if (ActivityCompat.checkSelfPermission(RequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
                Location driverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (requestsLongitudes.size()>position && usernames.size()>position
                        && requestsLatitudes.size()>position
                        && driverLocation != null) {
                    i.putExtra("riderLat", requestsLatitudes.get(position));
                    i.putExtra("riderLon", requestsLongitudes.get(position));
                    i.putExtra("driverLat", driverLocation.getLatitude());
                    i.putExtra("driverLon", driverLocation.getLongitude());
                    i.putExtra("username", usernames.get(position));
                    startActivity(i);
                }

            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateRequestList(location);

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
                updateRequestList(location);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.request_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout){

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
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateRequestList(Location location){
        if (location!= null) {
            final ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
            query.whereNear("location", geoPoint);
            query.whereDoesNotExist("driverUsername");
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e==null){
                        requests.clear();
                        requestsLatitudes.clear();
                        requestsLongitudes.clear();
                        usernames.clear();
                        if (objects.size()>0){
                            for (ParseObject object: objects){
                                ParseGeoPoint location = (ParseGeoPoint) object.get("location");
                                if (location!= null) {
                                    Double distance = geoPoint.distanceInMilesTo(location);
                                    Double mainVal = (double) Math.round(distance * 10) / 10;
                                    requests.add(String.valueOf(mainVal) + " miles");
                                    requestsLatitudes.add(location.getLatitude());
                                    requestsLongitudes.add(location.getLongitude());
                                    usernames.add(object.getString("username"));
                                }
                            }
                        }else{
                            requests.add("No Uber Requests found around you.");
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
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
}
